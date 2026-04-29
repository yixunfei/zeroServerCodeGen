package com.zero.codegen;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class CsOptimizationGenerationTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldGenerateOptimizedCsCollectionAndMapPaths() throws Exception {
        Path schema = tempDir.resolve("CsPerf.si");
        Files.writeString(schema, ""
                + "struct CsPerfPayload {\n"
                + "  List<Integer> values;\n"
                + "  Map<Integer, String> names;\n"
                + "}\n", StandardCharsets.UTF_8);

        Path csOut = tempDir.resolve("cs");
        SiCompiler.compileBatch(
                java.util.List.of(schema.toString()),
                tempDir.resolve("java").toString(),
                "com.demo.csperf",
                "",
                false,
                true,
                csOut.toString(),
                tempDir.resolve("java-common").toString(),
                csOut.toString(),
                tempDir.resolve("bo").toString(),
                "com.demo.csperf.bo",
                "Demo.CsPerf",
                false,
                false,
                false,
                false,
                false
        );

        String bufUtil = Files.readString(csOut.resolve("BufUtil.cs"), StandardCharsets.UTF_8);
        assertTrue(bufUtil.contains("CollectionsMarshal.AsSpan(list)"));
        assertTrue(bufUtil.contains("BorrowList<T>(n)"));
        assertTrue(bufUtil.contains("BorrowHashSet<T>(n)"));
        assertTrue(bufUtil.contains("if(map is Dictionary<K,V> dict)"));
        assertTrue(bufUtil.contains("BorrowDictionary<K,V>(n)"));
    }
}
