package com.zero.codegen;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class FieldMetadataGenerationTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldGenerateBorrowFixedAndPackedJavaPaths() throws Exception {
        Path schema = tempDir.resolve("FieldHints.si");
        Files.writeString(schema, ""
                + "struct FieldHints {\n"
                + "  @borrow byte[] payload;\n"
                + "  @borrow String nick;\n"
                + "  @borrow int[] samples;\n"
                + "  @fixed(16) String fixedNick;\n"
                + "  @fixed(8) int[] fixedSamples;\n"
                + "  @packed List<Integer> values;\n"
                + "  @packed Map<Integer, Integer> ext;\n"
                + "}\n", StandardCharsets.UTF_8);

        Path outDir = tempDir.resolve("out");
        SiCompiler.compileBatch(
                java.util.List.of(schema.toString()),
                outDir.toString(),
                "com.demo.hints",
                "",
                true,
                false,
                "",
                outDir.toString(),
                "",
                outDir.toString(),
                "com.demo.hints.bo",
                null,
                false,
                false,
                false,
                false,
                true
        );

        String code = Files.readString(outDir.resolve("com/demo/hints/FieldHints.java"), StandardCharsets.UTF_8);

        assertTrue(code.contains("BorrowedBytes payload;"));
        assertTrue(code.contains("BorrowedString nick;"));
        assertTrue(code.contains("IntArrayView samples;"));
        assertTrue(code.contains("ByteIO.readBorrowedBytes("));
        assertTrue(code.contains("ByteIO.readBorrowedString("));
        assertTrue(code.contains("ByteIO.readBorrowedRawIntArray("));
        assertTrue(code.contains("ByteIO.writeBorrowedBytes("));
        assertTrue(code.contains("ByteIO.writeBorrowedString("));
        assertTrue(code.contains("ByteIO.writeBorrowedRawIntArray("));
        assertTrue(code.contains("ByteIO.readFixedString("));
        assertTrue(code.contains("ByteIO.writeFixedString("));
        assertTrue(code.contains("ByteIO.readFixedCountIntArray("));
        assertTrue(code.contains("ByteIO.writeFixedCountIntArray("));
        assertTrue(code.contains("ByteIO.readPackedIntList("));
        assertTrue(code.contains("ByteIO.writePackedIntList("));
        assertTrue(code.contains("ByteIO.readPackedIntIntMap("));
        assertTrue(code.contains("ByteIO.writePackedIntIntMap("));
        assertTrue(code.contains("ByteIO.readSampledBorrowedBytes("));
        assertTrue(code.contains("ByteIO.readBorrowedBytes(input, "));
    }
}
