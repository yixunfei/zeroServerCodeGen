package com.zero.codegen;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FieldAnnotationParseTest {

    @Test
    void shouldParseFieldLevelAnnotations() {
        String schema = ""
                + "struct HintPayload {\n"
                + "  @fixed(16) String nick;\n"
                + "  @borrow byte[] payload;\n"
                + "  @packed List<Integer> values;\n"
                + "  @borrow @packed List<Long> ids;\n"
                + "}\n";

        List<SiCompiler.Struct> structs = SiCompiler.parseStructs(schema);
        SiCompiler.Struct struct = structs.get(0);

        SiCompiler.Field nick = struct.fields.get(0);
        assertEquals("String", nick.type);
        assertEquals(16, nick.fixedLength);
        assertFalse(nick.packed);
        assertFalse(nick.borrow);

        SiCompiler.Field payload = struct.fields.get(1);
        assertEquals("byte[]", payload.type);
        assertTrue(payload.borrow);
        assertFalse(payload.packed);

        SiCompiler.Field values = struct.fields.get(2);
        assertEquals("List<Integer>", values.type);
        assertTrue(values.packed);
        assertFalse(values.borrow);

        SiCompiler.Field ids = struct.fields.get(3);
        assertEquals("List<Long>", ids.type);
        assertTrue(ids.packed);
        assertTrue(ids.borrow);
    }
}
