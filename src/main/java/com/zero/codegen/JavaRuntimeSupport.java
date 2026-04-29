package com.zero.codegen;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

final class JavaRuntimeSupport {
    private static final String TEMPLATE_ROOT = "/runtime/java/";
    private static final String TEMPLATE_PACKAGE_ROOT = "com.zero.codegen.runtime";
    private static final List<String> TEMPLATE_FILES = List.of(
            "bytes/ByteCursor.java",
            "bytes/NettyCursor.java",
            "bytes/ArrayByteCursor.java",
            "bytes/ByteIO.java",
            "bytes/IntArrayList.java",
            "bytes/LongArrayList.java",
            "bytes/IntObjectHashMap.java",
            "bytes/IntIntHashMap.java",
            "bytes/IntLongHashMap.java",
            "bytes/BorrowedBytes.java",
            "bytes/BorrowedString.java",
            "bytes/IntArrayView.java",
            "bytes/LongArrayView.java",
            "bytes/FloatArrayView.java",
            "bytes/DoubleArrayView.java",
            "bytes/LinearByteBuffer.java",
            "bytes/LinearBufferPool.java",
            "serialize/IProto.java",
            "serialize/ICursorProto.java",
            "serialize/VarInt.java",
            "serialize/BufUtil.java",
            "proto/PayloadBuilder.java",
            "proto/IProtoDispatch.java",
            "netty/PacketPayload.java",
            "netty/ByteArrayPacketPayload.java",
            "netty/Packet.java",
            "netty/LinearBufferPacketPayload.java"
    );

    private JavaRuntimeSupport() {
    }

    static String runtimeRootPackage(String protoPkg) {
        return protoPkg + ".runtime";
    }

    static String bytesPackage(String protoPkg) {
        return runtimeRootPackage(protoPkg) + ".bytes";
    }

    static String serializePackage(String protoPkg) {
        return runtimeRootPackage(protoPkg) + ".serialize";
    }

    static String protoPackage(String protoPkg) {
        return runtimeRootPackage(protoPkg) + ".proto";
    }

    static String nettyPackage(String protoPkg) {
        return runtimeRootPackage(protoPkg) + ".netty";
    }

    static void writeRuntimeSources(String outRoot, String protoPkg) throws IOException {
        for (String templateFile : TEMPLATE_FILES) {
            String content = rewritePackages(loadTemplate(templateFile), protoPkg);
            String subPackage = templateFile.substring(0, templateFile.indexOf('/'));
            String fileName = templateFile.substring(templateFile.lastIndexOf('/') + 1);
            Path targetDir = SiCompiler.packageDir(outRoot, runtimeRootPackage(protoPkg) + "." + subPackage);
            Files.createDirectories(targetDir);
            SiCompiler.writeStringIfChanged(targetDir.resolve(fileName), content);
        }
    }

    private static String loadTemplate(String templateFile) throws IOException {
        try (InputStream stream = JavaRuntimeSupport.class.getResourceAsStream(TEMPLATE_ROOT + templateFile)) {
            if (stream == null) {
                throw new IOException("Missing runtime template: " + templateFile);
            }
            String text = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            if (!text.isEmpty() && text.charAt(0) == '\uFEFF') {
                return text.substring(1);
            }
            return text;
        }
    }

    private static String rewritePackages(String content, String protoPkg) {
        return content
                .replace(TEMPLATE_PACKAGE_ROOT + ".bytes", bytesPackage(protoPkg))
                .replace(TEMPLATE_PACKAGE_ROOT + ".serialize", serializePackage(protoPkg))
                .replace(TEMPLATE_PACKAGE_ROOT + ".proto", protoPackage(protoPkg))
                .replace(TEMPLATE_PACKAGE_ROOT + ".netty", nettyPackage(protoPkg));
    }
}
