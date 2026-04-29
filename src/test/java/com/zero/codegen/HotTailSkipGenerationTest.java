package com.zero.codegen;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class HotTailSkipGenerationTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldGenerateSkipManyForHotLeafStructsAndUseItForProjectedListTail() throws Exception {
        Path schema = tempDir.resolve("HotTail.si");
        Files.writeString(schema, ""
                + "@hot\n"
                + "struct HotLeaf {\n"
                + "  int id;\n"
                + "  int value;\n"
                + "}\n"
                + "\n"
                + "@hot\n"
                + "struct HotHolder {\n"
                + "  List<HotLeaf> items;\n"
                + "}\n", StandardCharsets.UTF_8);

        Path protoOut = tempDir.resolve("proto");
        SiCompiler.compile(schema.toString(), protoOut.toString(), "com.demo.tail", protoOut.toString());

        String leafCode = Files.readString(protoOut.resolve("com/demo/tail/HotLeaf.java"), StandardCharsets.UTF_8);
        String holderCode = Files.readString(protoOut.resolve("com/demo/tail/HotHolder.java"), StandardCharsets.UTF_8);

        assertTrue(leafCode.contains("public static void skipMany(ByteCursor input, int count)"));
        assertTrue(holderCode.contains("if(oItemsLimit==1)"));
        assertTrue(holderCode.contains("HotLeaf.skipMany(input, oItemsCount-1)"));
        assertTrue(holderCode.contains("HotLeaf.skipMany(input, oItemsCount-oItemsReadCount)"));
    }
}
