package com.zero.codegen;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class DispatcherGenerationTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldGenerateSparseDispatcherTable() throws Exception {
        Path schema = tempDir.resolve("Dispatch.si");
        Files.writeString(schema, ""
                + "struct Ping {\n"
                + "  int seq;\n"
                + "}\n"
                + "client_to_server:\n"
                + "  ping(Ping value);\n", StandardCharsets.UTF_8);

        Path outDir = tempDir.resolve("out");
        SiCompiler.compileBatch(
                List.of(schema.toString()),
                outDir.toString(),
                "com.demo.dispatch",
                "",
                true,
                false,
                "",
                outDir.toString(),
                "",
                outDir.toString(),
                "com.demo.dispatch.bo",
                null,
                false,
                false,
                false,
                false,
                false
        );

        String code = Files.readString(outDir.resolve("com/demo/dispatch/bo/ProtoDispatchManager.java"), StandardCharsets.UTF_8);
        assertTrue(code.contains("IntObjectHashMap<H> table"));
        assertTrue(code.contains("table.putInt("));
        assertTrue(code.contains("table.getInt(id)"));
        assertTrue(!code.contains("new H[ProtoIds.MAX_ID+1]"));
        assertTrue(!code.contains("id>=table.length"));
    }
}
