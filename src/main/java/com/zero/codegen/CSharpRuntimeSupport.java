package com.zero.codegen;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

final class CSharpRuntimeSupport {
    private CSharpRuntimeSupport() {
    }

    static String runtimeNamespace(String protocolNamespace) {
        if (protocolNamespace == null || protocolNamespace.isBlank()) {
            return "Runtime";
        }
        return protocolNamespace + ".Runtime";
    }

    static void writeRuntimeSources(String outRoot, String runtimeNamespace) throws IOException {
        if (outRoot == null || outRoot.isBlank()) {
            throw new IllegalArgumentException("C# common output path can not be blank");
        }
        Path outDir = Path.of(outRoot);
        Files.createDirectories(outDir);
        SiCompiler.writeStringIfChanged(outDir.resolve("BufUtil.cs"), SiCompiler.Cs.generateBufUtil(runtimeNamespace));
    }
}
