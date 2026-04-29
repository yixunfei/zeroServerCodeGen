package com.zero.codegen;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class CommonOutOptionTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldExportJavaAndCSharpCommonToDedicatedPaths() throws Exception {
        Path javaProtoOut = tempDir.resolve("java-proto");
        Path javaCommonOut = tempDir.resolve("java-common");
        Path csProtoOut = tempDir.resolve("cs-proto");
        Path csCommonOut = tempDir.resolve("cs-common");

        SiCompiler.compileBatch(
                List.of("src/main/resources/example/Player.si"),
                javaProtoOut.toString(),
                "com.demo.protocol",
                "",
                true,
                true,
                csProtoOut.toString(),
                javaCommonOut.toString(),
                csCommonOut.toString(),
                javaProtoOut.toString(),
                "com.demo.protocol.bo",
                "Demo.Protocol",
                false,
                false,
                false,
                false,
                false
        );

        Path javaStruct = javaProtoOut.resolve("com/demo/protocol/Player.java");
        Path javaCommon = javaCommonOut.resolve("com/demo/protocol/runtime/bytes/ByteIO.java");
        Path csStruct = csProtoOut.resolve("Player.cs");
        Path csCommon = csCommonOut.resolve("BufUtil.cs");

        assertTrue(Files.exists(javaStruct), "missing generated Java protocol struct");
        assertTrue(Files.exists(javaCommon), "missing generated Java common runtime");
        assertTrue(Files.exists(csStruct), "missing generated C# protocol struct");
        assertTrue(Files.exists(csCommon), "missing generated C# common runtime");

        String javaStructCode = Files.readString(javaStruct, StandardCharsets.UTF_8);
        String javaCommonCode = Files.readString(javaCommon, StandardCharsets.UTF_8);
        String csStructCode = Files.readString(csStruct, StandardCharsets.UTF_8);
        String csCommonCode = Files.readString(csCommon, StandardCharsets.UTF_8);

        assertTrue(javaStructCode.contains("import com.demo.protocol.runtime.bytes.*;"));
        assertTrue(javaCommonCode.contains("package com.demo.protocol.runtime.bytes;"));
        assertTrue(javaCommonCode.contains("setUnsafeBorrowEnabled"));
        assertTrue(!javaCommonCode.contains("jdk.incubator.vector.ByteVector"));
        assertTrue(csStructCode.contains("using Demo.Protocol.Runtime;"));
        assertTrue(csCommonCode.contains("namespace Demo.Protocol.Runtime"));
    }

    @Test
    void shouldRemoveStaleGeneratedFilesWhenCleanGeneratedOutputsEnabled() throws Exception {
        Path javaProtoOut = tempDir.resolve("java-clean");
        Path javaCommonOut = tempDir.resolve("java-clean-common");
        Path staleProto = javaProtoOut.resolve("com/demo/clean/Stale.java");
        Path staleRuntime = javaCommonOut.resolve("com/demo/clean/runtime/bytes/StaleRuntime.java");

        SiCompiler.compileBatch(
                List.of("src/main/resources/example/Player.si"),
                javaProtoOut.toString(),
                "com.demo.clean",
                "",
                true,
                false,
                "",
                javaCommonOut.toString(),
                "",
                javaProtoOut.toString(),
                "com.demo.clean.bo",
                null,
                false,
                false,
                false,
                false,
                false
        );

        Files.createDirectories(staleProto.getParent());
        Files.createDirectories(staleRuntime.getParent());
        Files.writeString(staleProto, "class Stale {}", StandardCharsets.UTF_8);
        Files.writeString(staleRuntime, "class StaleRuntime {}", StandardCharsets.UTF_8);

        SiCompiler.compileBatch(
                List.of("src/main/resources/example/Player.si"),
                javaProtoOut.toString(),
                "com.demo.clean",
                "",
                true,
                false,
                "",
                javaCommonOut.toString(),
                "",
                javaProtoOut.toString(),
                "com.demo.clean.bo",
                null,
                false,
                false,
                false,
                false,
                false,
                true
        );

        assertTrue(!Files.exists(staleProto), "stale generated proto file should be removed");
        assertTrue(!Files.exists(staleRuntime), "stale generated runtime file should be removed");
    }
}
