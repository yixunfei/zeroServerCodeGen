package com.zero.codegen;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class DominantMaskGenerationTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldGenerateMultipleDominantMaskFamiliesForHotStructs() throws Exception {
        Path schema = tempDir.resolve("DominantMask.si");
        Files.writeString(schema, ""
                + "@hot\n"
                + "struct DominantMaskPayload {\n"
                + "  int id;\n"
                + "  long version;\n"
                + "  String traceId;\n"
                + "  List<Integer> values;\n"
                + "  Map<Integer, Integer> scores;\n"
                + "}\n", StandardCharsets.UTF_8);

        Path protoOut = tempDir.resolve("proto");
        SiCompiler.compile(schema.toString(), protoOut.toString(), "com.demo.mask", protoOut.toString());

        String code = Files.readString(protoOut.resolve("com/demo/mask/DominantMaskPayload.java"), StandardCharsets.UTF_8);

        assertTrue(code.contains("if(__presence==0x1FL)"));
        assertTrue(code.contains("if(__presence==0xFL)"));
        assertTrue(code.contains("if(__presence==0x7L)"));
        assertTrue(code.contains("if(__presence==0x3L)"));
        assertTrue(code.contains("ByteIO.skipInt(input)"));
        assertTrue(code.contains("ByteIO.skipLong(input)"));
        assertTrue(code.contains("ByteIO.skipString(input)"));
        assertTrue(code.contains("if(oValuesLimit==1)"));
    }
}
