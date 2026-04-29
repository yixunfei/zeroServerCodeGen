package com.zero.codegen;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EstimatedSizeGenerationTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldGenerateEstimatedSizeAndSizedPayloadBuilderApis() throws Exception {
        Path schema = tempDir.resolve("Estimated.si");
        Files.writeString(schema, ""
                + "@fixed\n"
                + "struct FixedPoint {\n"
                + "  int x;\n"
                + "  long y;\n"
                + "}\n"
                + "\n"
                + "struct Child {\n"
                + "  int id;\n"
                + "  String nick;\n"
                + "}\n"
                + "\n"
                + "struct EstimatedPayload {\n"
                + "  int id;\n"
                + "  Child child;\n"
                + "  String nick;\n"
                + "  byte[] raw;\n"
                + "  @borrow byte[] borrowed;\n"
                + "  @packed List<Integer> values;\n"
                + "  Map<Integer, Child> nodes;\n"
                + "}\n"
                + "\n"
                + "server_to_client:\n"
                + "  push(EstimatedPayload payload, Optional<Integer> version, int seq);\n", StandardCharsets.UTF_8);

        Path protoOut = tempDir.resolve("proto");
        SiCompiler.compileBatch(
                java.util.List.of(schema.toString()),
                protoOut.toString(),
                "com.demo.estimate",
                "",
                true,
                false,
                "",
                protoOut.toString(),
                "",
                protoOut.toString(),
                "com.demo.estimate.bo",
                null,
                false,
                false,
                false,
                false,
                false
        );

        String payloadCode = Files.readString(protoOut.resolve("com/demo/estimate/EstimatedPayload.java"), StandardCharsets.UTF_8);
        String childCode = Files.readString(protoOut.resolve("com/demo/estimate/Child.java"), StandardCharsets.UTF_8);
        String fixedCode = Files.readString(protoOut.resolve("com/demo/estimate/FixedPoint.java"), StandardCharsets.UTF_8);
        String boCode = Files.readString(protoOut.resolve("com/demo/estimate/bo/IEstimatedBO.java"), StandardCharsets.UTF_8);
        String payloadBuilder = Files.readString(protoOut.resolve("com/demo/estimate/runtime/proto/PayloadBuilder.java"), StandardCharsets.UTF_8);
        String cursorProto = Files.readString(protoOut.resolve("com/demo/estimate/runtime/serialize/ICursorProto.java"), StandardCharsets.UTF_8);
        String linearByteBuffer = Files.readString(protoOut.resolve("com/demo/estimate/runtime/bytes/LinearByteBuffer.java"), StandardCharsets.UTF_8);

        assertTrue(payloadCode.contains("public int estimatedSize(){"));
        assertTrue(payloadCode.contains("ByteIO.sizeOfPresenceBits("));
        assertTrue(payloadCode.contains("ByteIO.sizeOfBorrowedBytes(this.borrowed)"));
        assertTrue(payloadCode.contains("this.child.estimatedSize()"));
        assertTrue(payloadCode.contains("for(Map.Entry<Integer,Child>"));
        assertTrue(payloadCode.contains("if(ByteIO.shouldReserveOnWriteStart(buf)){"));
        assertTrue(payloadCode.contains("ByteIO.reserveForWriteStart(buf, estimatedSize());"));
        assertTrue(payloadCode.contains("if(ByteIO.shouldReserveOnWriteStart(output)){"));
        assertTrue(payloadCode.contains("ByteIO.reserveForWriteStart(output, estimatedSize());"));

        assertTrue(childCode.contains("public int estimatedSize(){"));
        assertTrue(childCode.contains("ByteIO.sizeOfString(this.nick)"));

        assertTrue(fixedCode.contains("public int estimatedSize(){"));
        assertTrue(fixedCode.contains("int __size=0;"));
        assertFalse(fixedCode.contains("ByteIO.sizeOfPresenceBits("));

        assertTrue(boCode.contains("int __expectedSize=ByteIO.sizeOfPresenceBits("));
        assertTrue(boCode.contains("payload.estimatedSize()"));
        assertTrue(boCode.contains("PayloadBuilder.sendSized("));

        assertTrue(payloadBuilder.contains("public static byte[] buildProto(ICursorProto proto)"));
        assertTrue(payloadBuilder.contains("public static byte[] buildSized(int expectedBytes, Consumer<ByteCursor> writer)"));
        assertTrue(payloadBuilder.contains("public static PacketPayload buildPayload(Consumer<ByteCursor> writer)"));
        assertTrue(payloadBuilder.contains("public static PacketPayload buildView(Consumer<ByteCursor> writer)"));
        assertTrue(payloadBuilder.contains("public static PacketPayload buildPayloadSized(int expectedBytes, Consumer<ByteCursor> writer)"));
        assertTrue(payloadBuilder.contains("public static void sendProto(Channel ch, int id, ICursorProto proto)"));
        assertTrue(payloadBuilder.contains("public static void sendSized(Channel ch, int id, int expectedBytes, Consumer<ByteCursor> writer)"));
        assertTrue(cursorProto.contains("default int estimatedSize()"));
        assertTrue(payloadBuilder.contains("buildPayloadSized(proto.estimatedSize(), proto::writeTo)"));
        assertTrue(linearByteBuffer.contains("if(readerIndex==0 && writerIndex==0){"));
    }
}
