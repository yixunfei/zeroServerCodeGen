package com.zero.codegen;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StandaloneGenerationRoundTripTest {
    private static final String GENERATED_PACKAGE = "com.zero.codegen.comprehensive";
    private static final Path DRIVER_SOURCE_DIR = Path.of("src/test/resources/comprehensive/java");

    @TempDir
    Path tempDir;

    @Test
    void generatedProtocolCompilesAndRoundTripsWithoutExternalZeroProtocolModule() throws Exception {
        Path generatedSources = tempDir.resolve("generated-src");
        Path compiledClasses = tempDir.resolve("classes");

        SiCompiler.compileBatch(
                List.of("src/test/resources/comprehensive/ComprehensiveProtocol.si"),
                generatedSources.toString(),
                GENERATED_PACKAGE,
                "",
                true,
                false,
                "",
                generatedSources.toString(),
                "",
                generatedSources.toString(),
                GENERATED_PACKAGE + ".bo",
                null,
                false,
                false,
                false,
                false,
                false,
                true
        );
        copyDriverSource("ComprehensiveFixtures.java", generatedSources);
        copyDriverSource("ComprehensiveCorrectnessDriver.java", generatedSources);

        compileGeneratedSources(generatedSources, compiledClasses);
        String report = runGeneratedCorrectnessDriver(compiledClasses);

        assertReportValue(report, "denseByteParity", "true");
        assertReportValue(report, "denseCursorRoundTrip", "true");
        assertReportValue(report, "denseByteBufRoundTrip", "true");
        assertReportValue(report, "denseReadIntoRoundTrip", "true");
        assertReportValue(report, "shiftedRemovedKeyGone", "true");
        assertReportValue(report, "shiftedNewKeyAllocated", "true");
        assertReportValue(report, "sparseCursorRoundTrip", "true");
        assertReportValue(report, "sparseReadIntoDefaultReset", "true");
        assertTrue(report.contains("denseBytesSha256="), "driver report should include dense byte signature\n" + report);
        assertTrue(report.contains("sparseBytesSha256="), "driver report should include sparse byte signature\n" + report);
    }

    private static void copyDriverSource(String fileName, Path generatedSources) throws Exception {
        String source = Files.readString(DRIVER_SOURCE_DIR.resolve(fileName), StandardCharsets.UTF_8);
        Path target = generatedSources.resolve(GENERATED_PACKAGE.replace('.', '/')).resolve(fileName);
        Files.createDirectories(target.getParent());
        Files.writeString(target, source, StandardCharsets.UTF_8);
    }

    private static void compileGeneratedSources(Path generatedSources, Path compiledClasses) throws Exception {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        assertNotNull(compiler, "tests must run on a JDK because generated sources are compiled in-process");
        Files.createDirectories(compiledClasses);

        List<String> args = new ArrayList<>();
        args.add("-encoding");
        args.add("UTF-8");
        args.add("-source");
        args.add("21");
        args.add("-target");
        args.add("21");
        args.add("-proc:none");
        args.add("-classpath");
        args.add(System.getProperty("java.class.path"));
        args.add("-d");
        args.add(compiledClasses.toString());
        try (Stream<Path> walk = Files.walk(generatedSources)) {
            walk.filter(path -> Files.isRegularFile(path) && path.getFileName().toString().endsWith(".java"))
                    .map(Path::toString)
                    .forEach(args::add);
        }

        ByteArrayOutputStream compilerOutput = new ByteArrayOutputStream();
        int exitCode = compiler.run(null, compilerOutput, compilerOutput, args.toArray(String[]::new));
        assertEquals(0, exitCode, compilerOutput.toString(StandardCharsets.UTF_8));
    }

    private static String runGeneratedCorrectnessDriver(Path compiledClasses) throws Exception {
        try (URLClassLoader loader = new URLClassLoader(
                new URL[]{compiledClasses.toUri().toURL()},
                Thread.currentThread().getContextClassLoader())) {
            Class<?> driver = Class.forName(GENERATED_PACKAGE + ".ComprehensiveCorrectnessDriver", true, loader);
            Object result = driver.getMethod("runChecks").invoke(null);
            return String.valueOf(result);
        }
    }

    private static void assertReportValue(String report, String key, String expectedValue) {
        for (String line : report.split("\\R")) {
            if (line.startsWith(key + "=")) {
                assertEquals(expectedValue, line.substring(key.length() + 1), key + " failed\n" + report);
                return;
            }
        }
        throw new AssertionError("missing report key " + key + "\n" + report);
    }
}
