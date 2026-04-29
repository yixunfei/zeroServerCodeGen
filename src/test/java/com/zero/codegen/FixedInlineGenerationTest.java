package com.zero.codegen;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FixedInlineGenerationTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldGenerateFixedLayoutAndInlineSmallStructs() throws Exception {
        Path schema = tempDir.resolve("FixedInline.si");
        Files.writeString(schema, ""
                + "@fixed\n"
                + "@inline\n"
                + "struct SmallFixed {\n"
                + "  int x;\n"
                + "  long y;\n"
                + "  float z;\n"
                + "}\n"
                + "\n"
                + "@fixed\n"
                + "struct OuterFixed {\n"
                + "  int id;\n"
                + "  SmallFixed point;\n"
                + "  double score;\n"
                + "}\n"
                + "\n"
                + "@inline\n"
                + "struct SmallInline {\n"
                + "  int left;\n"
                + "  int right;\n"
                + "}\n"
                + "\n"
                + "struct OuterInline {\n"
                + "  int id;\n"
                + "  SmallInline nested;\n"
                + "  Optional<Integer> version;\n"
                + "}\n"
                + "\n"
                + "@fixed\n"
                + "struct FixedNode {\n"
                + "  int x;\n"
                + "  long y;\n"
                + "}\n"
                + "\n"
                + "@fixed\n"
                + "struct FixedNodeHolder {\n"
                + "  FixedNode[] nodes;\n"
                + "  int[] samples;\n"
                + "}\n"
                + "\n"
                + "struct ContainerHolder {\n"
                + "  List<Integer> values;\n"
                + "  Map<Integer, String> names;\n"
                + "}\n", StandardCharsets.UTF_8);

        Path protoOut = tempDir.resolve("proto");
        Path csOut = tempDir.resolve("cs");
        Path simdOut = tempDir.resolve("simd-java");
        SiCompiler.compile(schema.toString(), protoOut.toString(), "com.demo.fixed", protoOut.toString());
        SiCompiler.compileBatch(
                java.util.List.of(schema.toString()),
                protoOut.toString(),
                "com.demo.fixed",
                "",
                true,
                true,
                csOut.toString(),
                protoOut.toString(),
                csOut.toString(),
                protoOut.toString(),
                "com.demo.fixed.bo",
                "Demo.Fixed",
                false,
                false,
                false,
                false,
                false
        );
        SiCompiler.compileBatch(
                java.util.List.of(schema.toString()),
                simdOut.toString(),
                "com.demo.fixed.simd",
                "",
                true,
                false,
                "",
                simdOut.toString(),
                "",
                simdOut.toString(),
                "com.demo.fixed.simd.bo",
                null,
                false,
                false,
                false,
                false,
                true
        );

        String smallCode = Files.readString(protoOut.resolve("com/demo/fixed/SmallFixed.java"), StandardCharsets.UTF_8);
        String outerCode = Files.readString(protoOut.resolve("com/demo/fixed/OuterFixed.java"), StandardCharsets.UTF_8);
        String outerInlineCode = Files.readString(protoOut.resolve("com/demo/fixed/OuterInline.java"), StandardCharsets.UTF_8);
        String outerInlineSimdCode = Files.readString(simdOut.resolve("com/demo/fixed/simd/OuterInline.java"), StandardCharsets.UTF_8);
        String fixedNodeHolderSimdCode = Files.readString(simdOut.resolve("com/demo/fixed/simd/FixedNodeHolder.java"), StandardCharsets.UTF_8);
        String fixedNodeHolderCsCode = Files.readString(csOut.resolve("FixedNodeHolder.cs"), StandardCharsets.UTF_8);
        String containerHolderCsCode = Files.readString(csOut.resolve("ContainerHolder.cs"), StandardCharsets.UTF_8);
        String smallCsCode = Files.readString(csOut.resolve("SmallFixed.cs"), StandardCharsets.UTF_8);
        String outerCsCode = Files.readString(csOut.resolve("OuterFixed.cs"), StandardCharsets.UTF_8);
        String outerInlineCsCode = Files.readString(csOut.resolve("OuterInline.cs"), StandardCharsets.UTF_8);

        assertTrue(smallCode.contains("@fixed"));
        assertTrue(smallCode.contains("ByteIO.readFixedInt(input)"));
        assertTrue(smallCode.contains("ByteIO.writeFixedLong(output"));
        assertTrue(smallCode.contains("input.skip(16);"));
        assertFalse(smallCode.contains("writePresenceBits"));

        assertTrue(outerCode.contains("ByteIO.readFixedInt(input)"));
        assertTrue(outerCode.contains("oPointReuse"));
        assertTrue(outerCode.contains("thisPointValue"));
        assertFalse(outerCode.contains("SmallFixed.readFrom(input)"));
        assertFalse(outerCode.contains("writePresenceBits"));
        assertTrue(outerInlineCode.contains("oNestedReuse"));
        assertTrue(outerInlineCode.contains("oNestedPresence"));
        assertFalse(outerInlineCode.contains("SmallInline.readInto"));
        assertTrue(outerInlineSimdCode.contains("oNestedPresence"));
        assertFalse(outerInlineSimdCode.contains("long __presence=ByteIO.readPresenceBits(input, 2);\n        {\n            SmallInline oNestedReuse"));
        assertTrue(fixedNodeHolderSimdCode.contains("MemorySegment.ofArray(input.array())"));
        assertTrue(fixedNodeHolderSimdCode.contains("intoMemorySegment"));
        assertTrue(fixedNodeHolderSimdCode.contains("writeTo(NettyCursor.threadLocal(buf))"));

        assertTrue(smallCsCode.contains("BufUtil.ReadFixedInt(r)"));
        assertTrue(smallCsCode.contains("BufUtil.WriteFixedLong(w"));
        assertFalse(smallCsCode.contains("WritePresenceBits"));
        assertTrue(outerCsCode.contains("PointReuse"));
        assertTrue(outerCsCode.contains("PointValue"));
        assertFalse(outerCsCode.contains("WritePresenceBits"));
        assertTrue(outerInlineCsCode.contains("oNestedReuse"));
        assertFalse(outerInlineCsCode.contains("SmallInline.ReadInto"));
        assertFalse(fixedNodeHolderCsCode.contains("BufUtil.ReadObjectArray"));
        assertFalse(fixedNodeHolderCsCode.contains("BufUtil.WriteObjectArray"));
        assertTrue(Files.readString(protoOut.resolve("com/demo/fixed/FixedNodeHolder.java"), StandardCharsets.UTF_8).contains("samplesIndices"));
        assertTrue(Files.readString(protoOut.resolve("com/demo/fixed/FixedNodeHolder.java"), StandardCharsets.UTF_8).contains("ByteIO.readSampledFixedIntArray"));
        assertTrue(containerHolderCsCode.contains("BufUtil.BorrowList<int>"));
        assertTrue(containerHolderCsCode.contains("BufUtil.BorrowDictionary<int,string>"));
    }
}
