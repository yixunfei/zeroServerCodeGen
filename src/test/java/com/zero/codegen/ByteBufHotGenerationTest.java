package com.zero.codegen;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ByteBufHotGenerationTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldGenerateHotByteBufPathsAndSpecializedRuntimeContainers() throws Exception {
        Path schema = tempDir.resolve("HotByteBuf.si");
        Files.writeString(schema, ""
                + "@hot\n"
                + "struct NestedNode {\n"
                + "  int id;\n"
                + "  long version;\n"
                + "}\n"
                + "\n"
                + "@hot\n"
                + "struct HotByteBufPayload {\n"
                + "  byte[] payload;\n"
                + "  List<Integer> values;\n"
                + "  Map<Integer, NestedNode> index;\n"
                + "}\n", StandardCharsets.UTF_8);

        Path outDir = tempDir.resolve("out");
        SiCompiler.compileBatch(
                java.util.List.of(schema.toString()),
                outDir.toString(),
                "com.demo.hot",
                "",
                true,
                false,
                "",
                outDir.toString(),
                "",
                outDir.toString(),
                "com.demo.hot.bo",
                null,
                false,
                false,
                false,
                false,
                true
        );

        String code = Files.readString(outDir.resolve("com/demo/hot/HotByteBufPayload.java"), StandardCharsets.UTF_8);
        String runtime = Files.readString(outDir.resolve("com/demo/hot/runtime/bytes/ByteIO.java"), StandardCharsets.UTF_8);

        assertTrue(code.contains("public static void readInto(ByteBuf buf, HotByteBufPayload o){"));
        assertTrue(code.contains("ByteIO.readRawByteArray(buf, oPayloadReuse, oPayloadCount);"));
        assertTrue(code.contains("IntArrayList oValuesReuse=(o.values instanceof IntArrayList)"));
        assertTrue(code.contains("IntObjectHashMap<NestedNode> oIndexOldMap=(o.index instanceof IntObjectHashMap)"));
        assertTrue(code.contains("if(thisValuesList instanceof IntArrayList)"));
        assertTrue(code.contains("if(thisIndexMap instanceof IntObjectHashMap)"));
        assertTrue(runtime.contains("public static void readRawByteArray(ByteBuf c, byte[] arr, int count)"));
        assertTrue(runtime.contains("public static IntArrayList borrowIntArrayList(int expectedSize)"));
        assertTrue(runtime.contains("public static <V> IntObjectHashMap<V> borrowIntObjectHashMap(int expectedSize)"));
    }
}
