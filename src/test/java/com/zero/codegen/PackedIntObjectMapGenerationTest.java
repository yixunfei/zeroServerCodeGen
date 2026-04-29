package com.zero.codegen;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class PackedIntObjectMapGenerationTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldGeneratePackedIntKeyObjectMapHotPath() throws Exception {
        Path schema = tempDir.resolve("PackedObject.si");
        Files.writeString(schema, ""
                + "struct BuffNode {\n"
                + "  int id;\n"
                + "  String name;\n"
                + "}\n"
                + "\n"
                + "@hot\n"
                + "struct PackedObjectPayload {\n"
                + "  @packed Map<Integer, BuffNode> buffs;\n"
                + "}\n", StandardCharsets.UTF_8);

        Path outDir = tempDir.resolve("out");
        SiCompiler.compileBatch(
                java.util.List.of(schema.toString()),
                outDir.toString(),
                "com.demo.packedobj",
                "",
                true,
                false,
                "",
                outDir.toString(),
                "",
                outDir.toString(),
                "com.demo.packedobj.bo",
                null,
                false,
                false,
                false,
                false,
                false
        );

        String code = Files.readString(outDir.resolve("com/demo/packedobj/PackedObjectPayload.java"), StandardCharsets.UTF_8);

        assertTrue(code.contains("ByteIO.borrowIntObjectHashMap(0)"));
        assertTrue(code.contains("IntObjectHashMap<BuffNode>"));
        assertTrue(code.contains("int oBuffsKey=ByteIO.readFixedInt("));
        assertTrue(code.contains("oBuffsOldMap==null?null:oBuffsOldMap.getInt(oBuffsKey)"));
        assertTrue(code.contains("ByteIO.readPackedIntObjectMapFast("));
        assertTrue(code.contains("__size += ByteIO.sizeOfSize("));
        assertTrue(code.contains("*4);"));
    }
}
