package com.zero.codegen.benchmark;

import com.zero.codegen.SiCompiler;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public final class CodegenBenchmarkLauncher {
    private static final String GENERATED_PACKAGE = "com.zero.codegen.benchmark.generated";
    private static final Path DRIVER_SOURCE_DIR = Path.of("src/test/resources/benchmark/java");
    private static final Path SCHEMA = Path.of("src/test/resources/benchmark/BenchmarkProtocol.si");

    private CodegenBenchmarkLauncher() {
    }

    public static void main(String[] args) throws Exception {
        Path workDir = Path.of(System.getProperty("zero.benchmark.workDir", "target/benchmark-work"));
        Path generatedSources = workDir.resolve("generated-src");
        Path compiledClasses = workDir.resolve("classes");
        Path resultDir = Path.of(System.getProperty("zero.benchmark.resultDir", "target/benchmark-results"));

        Files.createDirectories(workDir);
        SiCompiler.compileBatch(
                List.of(SCHEMA.toString()),
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
                Boolean.getBoolean("zero.benchmark.simd"),
                true
        );

        copyDriverSources(generatedSources);
        compileGeneratedSources(generatedSources, compiledClasses);

        try (URLClassLoader loader = new URLClassLoader(
                new URL[]{compiledClasses.toUri().toURL()},
                Thread.currentThread().getContextClassLoader())) {
            Class<?> harness = Class.forName(GENERATED_PACKAGE + ".BenchmarkHarness", true, loader);
            Object result = harness.getMethod("run", String.class).invoke(null, resultDir.toString());
            System.out.println(result);
        }
    }

    private static void copyDriverSources(Path generatedSources) throws Exception {
        Path targetDir = generatedSources.resolve(GENERATED_PACKAGE.replace('.', '/'));
        Files.createDirectories(targetDir);
        try (Stream<Path> walk = Files.walk(DRIVER_SOURCE_DIR)) {
            for (Path source : walk.filter(path -> Files.isRegularFile(path) && path.getFileName().toString().endsWith(".java")).toList()) {
                String text = Files.readString(source, StandardCharsets.UTF_8);
                Files.writeString(targetDir.resolve(source.getFileName()), text, StandardCharsets.UTF_8);
            }
        }
    }

    private static void compileGeneratedSources(Path generatedSources, Path compiledClasses) throws Exception {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            throw new IllegalStateException("Benchmarks must run on a JDK because generated sources are compiled in-process");
        }
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
        args.add(buildCompileClasspath());
        args.add("-d");
        args.add(compiledClasses.toString());
        try (Stream<Path> walk = Files.walk(generatedSources)) {
            walk.filter(path -> Files.isRegularFile(path) && path.getFileName().toString().endsWith(".java"))
                    .map(Path::toString)
                    .forEach(args::add);
        }

        ByteArrayOutputStream compilerOutput = new ByteArrayOutputStream();
        int exitCode = compiler.run(null, compilerOutput, compilerOutput, args.toArray(String[]::new));
        if (exitCode != 0) {
            throw new IllegalStateException(compilerOutput.toString(StandardCharsets.UTF_8));
        }
    }

    private static String buildCompileClasspath() {
        Set<String> entries = new LinkedHashSet<>();
        String propertyClasspath = System.getProperty("java.class.path", "");
        if (!propertyClasspath.isBlank()) {
            entries.addAll(List.of(propertyClasspath.split(File.pathSeparator)));
        }
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        while (loader != null) {
            if (loader instanceof URLClassLoader urlClassLoader) {
                for (URL url : urlClassLoader.getURLs()) {
                    if ("file".equalsIgnoreCase(url.getProtocol())) {
                        try {
                            entries.add(Path.of(url.toURI()).toString());
                        } catch (Exception ignored) {
                            entries.add(url.getPath());
                        }
                    }
                }
            }
            loader = loader.getParent();
        }
        return String.join(File.pathSeparator, entries);
    }
}
