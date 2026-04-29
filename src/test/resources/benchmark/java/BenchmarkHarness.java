package com.zero.codegen.benchmark.generated;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public final class BenchmarkHarness {
    private static volatile long longSink;
    private static volatile Object objectSink;

    private BenchmarkHarness() {
    }

    public static String run(String resultDirText) throws Exception {
        BenchmarkConfig config = BenchmarkConfig.fromSystemProperties();
        Path resultDir = Path.of(resultDirText);
        Files.createDirectories(resultDir);

        List<BenchmarkCase> cases = new ArrayList<>();
        List<DatasetSize> sizes = new ArrayList<>();
        CrossFrameworkCodecs crossCodecs = new CrossFrameworkCodecs();

        FixedTelemetry fixed = BenchmarkFixtures.fixedTelemetry(512);
        addZeroFixedCases(cases, sizes, "fixed-telemetry", fixed);

        PackedCollections packed = BenchmarkFixtures.packedCollections(777L, 128);
        addZeroPackedCases(cases, sizes, "packed-collections-128", packed);

        addTargetFrameworkCases(cases, sizes, "cross-frame-small", CrossFrameworkFixtures.frame(8, 8, 16, 256));
        addTargetFrameworkCases(cases, sizes, "cross-frame-medium", CrossFrameworkFixtures.frame(64, 64, 128, 2048));
        addTargetFrameworkCases(cases, sizes, "cross-frame-large", CrossFrameworkFixtures.frame(256, 256, 512, 8192));

        addPayloadCases(cases, sizes, crossCodecs, "payload-sparse", BenchmarkFixtures.sparsePayload());
        addPayloadCases(cases, sizes, crossCodecs, "payload-dense-32", BenchmarkFixtures.densePayload(32, 1024, 64));
        addPayloadCases(cases, sizes, crossCodecs, "payload-dense-256", BenchmarkFixtures.densePayload(256, 4096, 256));

        List<BenchmarkResult> results = new ArrayList<>();
        for (BenchmarkCase benchmarkCase : cases) {
            results.add(measure(config, benchmarkCase));
        }

        String markdown = renderMarkdown(config, sizes, results);
        String csv = renderCsv(results);
        Files.writeString(resultDir.resolve("benchmark-report.md"), markdown, StandardCharsets.UTF_8);
        Files.writeString(resultDir.resolve("benchmark-results.csv"), csv, StandardCharsets.UTF_8);

        return "benchmark complete: " + resultDir.resolve("benchmark-report.md").toAbsolutePath();
    }

    public static void main(String[] args) throws Exception {
        String resultDir = args.length == 0 ? "target/benchmark-results" : args[0];
        System.out.println(run(resultDir));
    }

    private static void addZeroFixedCases(List<BenchmarkCase> cases, List<DatasetSize> sizes, String dataset, FixedTelemetry fixed) {
        byte[] cursorBytes = BenchmarkFixtures.encodeCursor(fixed);
        byte[] byteBufBytes = BenchmarkFixtures.encodeByteBuf(fixed);
        assertSameBytes(dataset, cursorBytes, byteBufBytes);
        sizes.add(new DatasetSize(dataset, "zero-codegen", cursorBytes.length));

        cases.add(new BenchmarkCase(dataset, "zero-cursor", "encode", cursorBytes.length, () -> BenchmarkFixtures.encodeCursor(fixed)));
        cases.add(new BenchmarkCase(dataset, "zero-cursor", "decode", cursorBytes.length, () -> BenchmarkFixtures.decodeFixedCursor(cursorBytes)));
        FixedTelemetry readIntoTarget = BenchmarkFixtures.fixedTelemetry(-1);
        cases.add(new BenchmarkCase(dataset, "zero-cursor", "readInto", cursorBytes.length, () -> {
            BenchmarkFixtures.readIntoFixedCursor(cursorBytes, readIntoTarget);
            return readIntoTarget;
        }));
        cases.add(new BenchmarkCase(dataset, "zero-bytebuf", "encode", byteBufBytes.length, () -> BenchmarkFixtures.encodeByteBuf(fixed)));
        cases.add(new BenchmarkCase(dataset, "zero-bytebuf", "decode", byteBufBytes.length, () -> BenchmarkFixtures.decodeFixedByteBuf(byteBufBytes)));
        cases.add(new BenchmarkCase(dataset, "zero-estimatedSize", "size", cursorBytes.length, fixed::estimatedSize));
    }

    private static void addZeroPackedCases(List<BenchmarkCase> cases, List<DatasetSize> sizes, String dataset, PackedCollections packed) {
        byte[] cursorBytes = BenchmarkFixtures.encodeCursor(packed);
        byte[] byteBufBytes = BenchmarkFixtures.encodeByteBuf(packed);
        assertSameBytes(dataset, cursorBytes, byteBufBytes);
        sizes.add(new DatasetSize(dataset, "zero-codegen", cursorBytes.length));

        cases.add(new BenchmarkCase(dataset, "zero-cursor", "encode", cursorBytes.length, () -> BenchmarkFixtures.encodeCursor(packed)));
        cases.add(new BenchmarkCase(dataset, "zero-cursor", "decode", cursorBytes.length, () -> BenchmarkFixtures.decodePackedCursor(cursorBytes)));
        PackedCollections readIntoTarget = BenchmarkFixtures.packedCollections(-1L, 3);
        cases.add(new BenchmarkCase(dataset, "zero-cursor", "readInto", cursorBytes.length, () -> {
            BenchmarkFixtures.readIntoPackedCursor(cursorBytes, readIntoTarget);
            return readIntoTarget;
        }));
        cases.add(new BenchmarkCase(dataset, "zero-bytebuf", "encode", byteBufBytes.length, () -> BenchmarkFixtures.encodeByteBuf(packed)));
        cases.add(new BenchmarkCase(dataset, "zero-bytebuf", "decode", byteBufBytes.length, () -> BenchmarkFixtures.decodePackedByteBuf(byteBufBytes)));
        cases.add(new BenchmarkCase(dataset, "zero-estimatedSize", "size", cursorBytes.length, packed::estimatedSize));
    }

    private static void addTargetFrameworkCases(List<BenchmarkCase> cases, List<DatasetSize> sizes, String dataset, CrossFrameworkFrame frame) throws Exception {
        byte[] zeroCursorBytes = CrossFrameworkFixtures.encodeCursor(frame);
        byte[] zeroByteBufBytes = CrossFrameworkFixtures.encodeByteBuf(frame);
        assertSameBytes(dataset, zeroCursorBytes, zeroByteBufBytes);

        CrossFrameworkDtos.Frame dto = CrossFrameworkDtos.fromGenerated(frame);
        byte[] protobufBytes = TargetFrameworkCodecs.encodeProtobuf(dto);
        byte[] protobufGeneratedBytes = GeneratedProtobufCodecs.encode(dto);
        byte[] flatBuffersBytes = TargetFrameworkCodecs.encodeFlatBuffers(dto);
        byte[] sbeBytes = TargetFrameworkCodecs.encodeSbe(dto);
        byte[] capnProtoBytes = TargetFrameworkCodecs.encodeCapnProto(dto);
        byte[] capnProtoRandomWriteBytes = TargetFrameworkCodecs.writeCapnProtoRandomAccess(dto);

        sanityCheck(dto, TargetFrameworkCodecs.decodeProtobuf(protobufBytes));
        sanityCheck(dto, GeneratedProtobufCodecs.decode(protobufGeneratedBytes));
        sanityCheck(dto, TargetFrameworkCodecs.decodeFlatBuffers(flatBuffersBytes));
        sanityCheck(dto, TargetFrameworkCodecs.decodeSbe(sbeBytes));
        sanityCheck(dto, TargetFrameworkCodecs.decodeCapnProto(capnProtoBytes));
        sanityCheck(dto, TargetFrameworkCodecs.decodeCapnProto(capnProtoRandomWriteBytes));

        sizes.add(new DatasetSize(dataset, "zero-codegen", zeroCursorBytes.length));
        sizes.add(new DatasetSize(dataset, "protobuf-codedstream", protobufBytes.length));
        sizes.add(new DatasetSize(dataset, "protobuf-generated", protobufGeneratedBytes.length));
        sizes.add(new DatasetSize(dataset, "flatbuffers-runtime", flatBuffersBytes.length));
        sizes.add(new DatasetSize(dataset, "sbe-agrona", sbeBytes.length));
        sizes.add(new DatasetSize(dataset, "capnproto-runtime", capnProtoBytes.length));
        sizes.add(new DatasetSize(dataset, "capnproto-random-write", capnProtoRandomWriteBytes.length));

        cases.add(new BenchmarkCase(dataset, "zero-cursor", "encode", zeroCursorBytes.length, () -> CrossFrameworkFixtures.encodeCursor(frame)));
        cases.add(new BenchmarkCase(dataset, "zero-cursor", "decode", zeroCursorBytes.length, () -> CrossFrameworkFixtures.decodeCursor(zeroCursorBytes)));
        CrossFrameworkFrame readIntoTarget = CrossFrameworkFixtures.frame(1, 1, 1, 1);
        cases.add(new BenchmarkCase(dataset, "zero-cursor", "readInto", zeroCursorBytes.length, () -> {
            CrossFrameworkFixtures.readIntoCursor(zeroCursorBytes, readIntoTarget);
            return readIntoTarget;
        }));
        cases.add(new BenchmarkCase(dataset, "zero-bytebuf", "encode", zeroByteBufBytes.length, () -> CrossFrameworkFixtures.encodeByteBuf(frame)));
        cases.add(new BenchmarkCase(dataset, "zero-bytebuf", "decode", zeroByteBufBytes.length, () -> CrossFrameworkFixtures.decodeByteBuf(zeroByteBufBytes)));
        cases.add(new BenchmarkCase(dataset, "zero-estimatedSize", "size", zeroCursorBytes.length, frame::estimatedSize));

        cases.add(new BenchmarkCase(dataset, "protobuf-codedstream", "encode", protobufBytes.length, () -> TargetFrameworkCodecs.encodeProtobuf(dto)));
        cases.add(new BenchmarkCase(dataset, "protobuf-codedstream", "decode", protobufBytes.length, () -> TargetFrameworkCodecs.decodeProtobuf(protobufBytes)));
        cases.add(new BenchmarkCase(dataset, "protobuf-generated", "encode", protobufGeneratedBytes.length, () -> GeneratedProtobufCodecs.encode(dto)));
        cases.add(new BenchmarkCase(dataset, "protobuf-generated", "decode", protobufGeneratedBytes.length, () -> GeneratedProtobufCodecs.decode(protobufGeneratedBytes)));
        cases.add(new BenchmarkCase(dataset, "protobuf-generated", "randomRead", protobufGeneratedBytes.length, () -> GeneratedProtobufCodecs.randomRead(protobufGeneratedBytes)));
        cases.add(new BenchmarkCase(dataset, "flatbuffers-runtime", "encode", flatBuffersBytes.length, () -> TargetFrameworkCodecs.encodeFlatBuffers(dto)));
        cases.add(new BenchmarkCase(dataset, "flatbuffers-runtime", "decode", flatBuffersBytes.length, () -> TargetFrameworkCodecs.decodeFlatBuffers(flatBuffersBytes)));
        cases.add(new BenchmarkCase(dataset, "flatbuffers-runtime", "randomRead", flatBuffersBytes.length, () -> TargetFrameworkCodecs.readFlatBuffersRandomAccess(flatBuffersBytes)));
        cases.add(new BenchmarkCase(dataset, "sbe-agrona", "encode", sbeBytes.length, () -> TargetFrameworkCodecs.encodeSbe(dto)));
        cases.add(new BenchmarkCase(dataset, "sbe-agrona", "decode", sbeBytes.length, () -> TargetFrameworkCodecs.decodeSbe(sbeBytes)));
        cases.add(new BenchmarkCase(dataset, "capnproto-runtime", "encode", capnProtoBytes.length, () -> TargetFrameworkCodecs.encodeCapnProto(dto)));
        cases.add(new BenchmarkCase(dataset, "capnproto-runtime", "decode", capnProtoBytes.length, () -> TargetFrameworkCodecs.decodeCapnProto(capnProtoBytes)));
        cases.add(new BenchmarkCase(dataset, "capnproto-runtime", "randomRead", capnProtoBytes.length, () -> TargetFrameworkCodecs.readCapnProtoRandomAccess(capnProtoBytes)));
        cases.add(new BenchmarkCase(dataset, "capnproto-runtime", "randomWrite", capnProtoRandomWriteBytes.length, () -> TargetFrameworkCodecs.writeCapnProtoRandomAccess(dto)));
    }

    private static void addPayloadCases(List<BenchmarkCase> cases, List<DatasetSize> sizes, CrossFrameworkCodecs crossCodecs, String dataset, BenchmarkPayload payload) throws Exception {
        byte[] cursorBytes = BenchmarkFixtures.encodeCursor(payload);
        byte[] byteBufBytes = BenchmarkFixtures.encodeByteBuf(payload);
        assertSameBytes(dataset, cursorBytes, byteBufBytes);
        BenchmarkDtos.PayloadDto dto = BenchmarkDtos.fromGenerated(payload);

        byte[] jsonBytes = crossCodecs.encodeJson(dto);
        byte[] smileBytes = crossCodecs.encodeSmile(dto);
        byte[] kryoBytes = crossCodecs.encodeKryo(dto);
        byte[] javaBytes = crossCodecs.encodeJava(dto);
        byte[] protoBytes = crossCodecs.encodeProtobuf(dto);

        sanityCheck(dto, crossCodecs.decodeJson(jsonBytes));
        sanityCheck(dto, crossCodecs.decodeSmile(smileBytes));
        sanityCheck(dto, crossCodecs.decodeKryo(kryoBytes));
        sanityCheck(dto, crossCodecs.decodeJava(javaBytes));
        sanityCheck(dto, crossCodecs.decodeProtobuf(protoBytes));

        sizes.add(new DatasetSize(dataset, "zero-codegen", cursorBytes.length));
        sizes.add(new DatasetSize(dataset, "jackson-json", jsonBytes.length));
        sizes.add(new DatasetSize(dataset, "jackson-smile", smileBytes.length));
        sizes.add(new DatasetSize(dataset, "kryo", kryoBytes.length));
        sizes.add(new DatasetSize(dataset, "jdk-serialization", javaBytes.length));
        sizes.add(new DatasetSize(dataset, "protobuf-codedstream", protoBytes.length));

        cases.add(new BenchmarkCase(dataset, "zero-cursor", "encode", cursorBytes.length, () -> BenchmarkFixtures.encodeCursor(payload)));
        cases.add(new BenchmarkCase(dataset, "zero-cursor", "decode", cursorBytes.length, () -> BenchmarkFixtures.decodePayloadCursor(cursorBytes)));
        BenchmarkPayload readIntoTarget = BenchmarkFixtures.densePayload(4, 32, 4);
        cases.add(new BenchmarkCase(dataset, "zero-cursor", "readInto", cursorBytes.length, () -> {
            BenchmarkFixtures.readIntoPayloadCursor(cursorBytes, readIntoTarget);
            return readIntoTarget;
        }));
        cases.add(new BenchmarkCase(dataset, "zero-bytebuf", "encode", byteBufBytes.length, () -> BenchmarkFixtures.encodeByteBuf(payload)));
        cases.add(new BenchmarkCase(dataset, "zero-bytebuf", "decode", byteBufBytes.length, () -> BenchmarkFixtures.decodePayloadByteBuf(byteBufBytes)));
        cases.add(new BenchmarkCase(dataset, "zero-estimatedSize", "size", cursorBytes.length, payload::estimatedSize));

        cases.add(new BenchmarkCase(dataset, "jackson-json", "encode", jsonBytes.length, () -> crossCodecs.encodeJson(dto)));
        cases.add(new BenchmarkCase(dataset, "jackson-json", "decode", jsonBytes.length, () -> crossCodecs.decodeJson(jsonBytes)));
        cases.add(new BenchmarkCase(dataset, "jackson-smile", "encode", smileBytes.length, () -> crossCodecs.encodeSmile(dto)));
        cases.add(new BenchmarkCase(dataset, "jackson-smile", "decode", smileBytes.length, () -> crossCodecs.decodeSmile(smileBytes)));
        cases.add(new BenchmarkCase(dataset, "kryo", "encode", kryoBytes.length, () -> crossCodecs.encodeKryo(dto)));
        cases.add(new BenchmarkCase(dataset, "kryo", "decode", kryoBytes.length, () -> crossCodecs.decodeKryo(kryoBytes)));
        cases.add(new BenchmarkCase(dataset, "jdk-serialization", "encode", javaBytes.length, () -> crossCodecs.encodeJava(dto)));
        cases.add(new BenchmarkCase(dataset, "jdk-serialization", "decode", javaBytes.length, () -> crossCodecs.decodeJava(javaBytes)));
        cases.add(new BenchmarkCase(dataset, "protobuf-codedstream", "encode", protoBytes.length, () -> crossCodecs.encodeProtobuf(dto)));
        cases.add(new BenchmarkCase(dataset, "protobuf-codedstream", "decode", protoBytes.length, () -> crossCodecs.decodeProtobuf(protoBytes)));
    }

    private static BenchmarkResult measure(BenchmarkConfig config, BenchmarkCase benchmarkCase) throws Exception {
        for (int i = 0; i < config.warmupIterations; i++) {
            runOperations(config.operationsPerIteration, benchmarkCase.operation);
        }

        List<Double> nsPerOp = new ArrayList<>();
        List<Double> allocatedPerOp = new ArrayList<>();
        AllocationMeter allocationMeter = AllocationMeter.create();
        for (int i = 0; i < config.measurementIterations; i++) {
            long allocationStart = allocationMeter.bytes();
            long start = System.nanoTime();
            runOperations(config.operationsPerIteration, benchmarkCase.operation);
            long elapsed = System.nanoTime() - start;
            long allocationEnd = allocationMeter.bytes();
            nsPerOp.add(elapsed / (double) config.operationsPerIteration);
            if (allocationStart >= 0 && allocationEnd >= allocationStart) {
                allocatedPerOp.add((allocationEnd - allocationStart) / (double) config.operationsPerIteration);
            }
        }
        nsPerOp.sort(Comparator.naturalOrder());
        allocatedPerOp.sort(Comparator.naturalOrder());

        double mean = nsPerOp.stream().mapToDouble(Double::doubleValue).average().orElse(0D);
        double p50 = percentile(nsPerOp, 0.50D);
        double p95 = percentile(nsPerOp, 0.95D);
        double alloc = allocatedPerOp.isEmpty()
                ? -1D
                : allocatedPerOp.stream().mapToDouble(Double::doubleValue).average().orElse(-1D);
        double opsPerSecond = 1_000_000_000D / mean;
        double mibPerSecond = benchmarkCase.bytesPerOp <= 0 ? 0D : (benchmarkCase.bytesPerOp * opsPerSecond) / (1024D * 1024D);

        return new BenchmarkResult(
                benchmarkCase.dataset,
                benchmarkCase.codec,
                benchmarkCase.operationName,
                benchmarkCase.bytesPerOp,
                mean,
                p50,
                p95,
                opsPerSecond,
                mibPerSecond,
                alloc
        );
    }

    private static void runOperations(int count, ThrowingOperation operation) throws Exception {
        for (int i = 0; i < count; i++) {
            consume(operation.run());
        }
    }

    private static void consume(Object value) {
        if (value instanceof byte[] bytes) {
            longSink += bytes.length;
        } else if (value instanceof Number number) {
            longSink += number.longValue();
        } else if (value instanceof BenchmarkPayload payload) {
            longSink += BenchmarkFixtures.blackHole(payload);
        } else if (value instanceof FixedTelemetry telemetry) {
            longSink += BenchmarkFixtures.blackHole(telemetry);
        } else if (value instanceof PackedCollections packed) {
            longSink += BenchmarkFixtures.blackHole(packed);
        } else if (value instanceof CrossFrameworkFrame frame) {
            longSink += CrossFrameworkFixtures.blackHole(frame);
        } else if (value instanceof CrossFrameworkDtos.Frame frame) {
            longSink += CrossFrameworkDtos.blackHole(frame);
        } else if (value instanceof BenchmarkDtos.PayloadDto dto) {
            longSink += BenchmarkDtos.blackHole(dto);
        } else {
            objectSink = value;
        }
    }

    private static String renderMarkdown(BenchmarkConfig config, List<DatasetSize> sizes, List<BenchmarkResult> results) {
        RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
        StringBuilder sb = new StringBuilder(65536);
        sb.append("# zero-codegen performance benchmark\n\n");
        sb.append("Generated at: ").append(Instant.now()).append("\n\n");
        sb.append("## Configuration\n\n");
        sb.append("- Profile: `").append(config.profile).append("`\n");
        sb.append("- Warmup iterations: ").append(config.warmupIterations).append("\n");
        sb.append("- Measurement iterations: ").append(config.measurementIterations).append("\n");
        sb.append("- Operations per iteration: ").append(config.operationsPerIteration).append("\n");
        sb.append("- JVM: `").append(System.getProperty("java.vm.name")).append(" ")
                .append(System.getProperty("java.vm.version")).append("`\n");
        sb.append("- Java: `").append(System.getProperty("java.version")).append("`\n");
        sb.append("- OS: `").append(System.getProperty("os.name")).append(" ")
                .append(System.getProperty("os.version")).append(" ")
                .append(System.getProperty("os.arch")).append("`\n");
        sb.append("- JVM args: `").append(String.join(" ", runtime.getInputArguments())).append("`\n\n");
        sb.append("`protobuf-codedstream` uses protobuf-java CodedInputStream/CodedOutputStream with an equivalent hand-written schema codec, not protoc generated classes.\n");
        sb.append("`protobuf-generated` uses Java classes generated from src/test/proto/benchmark_cross_frame.proto by protoc during Maven test compilation.\n");
        sb.append("`flatbuffers-runtime` uses FlatBuffers Java runtime builder/table APIs with generated-code-equivalent manual accessors.\n");
        sb.append("`flatbuffers-runtime` randomRead measures field-level random access without full DTO materialization.\n");
        sb.append("`sbe-agrona` uses Agrona DirectBuffer with a fixed-layout SBE-style codec.\n");
        sb.append("`capnproto-runtime` uses Cap'n Proto Java runtime with generated-code-equivalent manual struct factories.\n\n");
        sb.append("`capnproto-runtime` randomRead measures pointer/list random access without full DTO materialization; randomWrite writes fields and list elements in non-declaration order.\n\n");

        sb.append("## Encoded Size\n\n");
        sb.append("| dataset | codec | bytes |\n");
        sb.append("| --- | --- | ---: |\n");
        for (DatasetSize size : sizes) {
            sb.append("| ").append(size.dataset).append(" | ").append(size.codec).append(" | ").append(size.bytes).append(" |\n");
        }

        sb.append("\n## Results\n\n");
        sb.append("| dataset | codec | operation | bytes/op | ns/op mean | ns/op p50 | ns/op p95 | ops/s | MiB/s | alloc B/op |\n");
        sb.append("| --- | --- | --- | ---: | ---: | ---: | ---: | ---: | ---: | ---: |\n");
        for (BenchmarkResult result : results) {
            sb.append("| ")
                    .append(result.dataset).append(" | ")
                    .append(result.codec).append(" | ")
                    .append(result.operation).append(" | ")
                    .append(result.bytesPerOp).append(" | ")
                    .append(format(result.nsPerOpMean)).append(" | ")
                    .append(format(result.nsPerOpP50)).append(" | ")
                    .append(format(result.nsPerOpP95)).append(" | ")
                    .append(format(result.opsPerSecond)).append(" | ")
                    .append(format(result.mibPerSecond)).append(" | ")
                    .append(result.allocatedBytesPerOp < 0 ? "n/a" : format(result.allocatedBytesPerOp))
                    .append(" |\n");
        }

        sb.append("\n## Fastest Encode/Decode Per Payload Dataset\n\n");
        sb.append("| dataset | operation | fastest codec | ns/op mean |\n");
        sb.append("| --- | --- | --- | ---: |\n");
        for (String dataset : results.stream().map(r -> r.dataset).distinct().filter(name -> name.startsWith("payload-") || name.startsWith("cross-frame-")).toList()) {
            appendFastest(sb, results, dataset, "encode");
            appendFastest(sb, results, dataset, "decode");
        }
        sb.append("\nSink: `").append(longSink).append("`, object sink: `").append(objectSink == null ? "null" : objectSink.getClass().getName()).append("`.\n");
        return sb.toString();
    }

    private static void appendFastest(StringBuilder sb, List<BenchmarkResult> results, String dataset, String operation) {
        results.stream()
                .filter(result -> result.dataset.equals(dataset) && result.operation.equals(operation))
                .min(Comparator.comparingDouble(result -> result.nsPerOpMean))
                .ifPresent(result -> sb.append("| ")
                        .append(dataset).append(" | ")
                        .append(operation).append(" | ")
                        .append(result.codec).append(" | ")
                        .append(format(result.nsPerOpMean)).append(" |\n"));
    }

    private static String renderCsv(List<BenchmarkResult> results) {
        StringBuilder sb = new StringBuilder(32768);
        sb.append("dataset,codec,operation,bytes_per_op,ns_per_op_mean,ns_per_op_p50,ns_per_op_p95,ops_per_second,mib_per_second,allocated_bytes_per_op\n");
        for (BenchmarkResult result : results) {
            sb.append(result.dataset).append(',')
                    .append(result.codec).append(',')
                    .append(result.operation).append(',')
                    .append(result.bytesPerOp).append(',')
                    .append(format(result.nsPerOpMean)).append(',')
                    .append(format(result.nsPerOpP50)).append(',')
                    .append(format(result.nsPerOpP95)).append(',')
                    .append(format(result.opsPerSecond)).append(',')
                    .append(format(result.mibPerSecond)).append(',')
                    .append(result.allocatedBytesPerOp < 0 ? "" : format(result.allocatedBytesPerOp))
                    .append('\n');
        }
        return sb.toString();
    }

    private static void sanityCheck(BenchmarkDtos.PayloadDto expected, BenchmarkDtos.PayloadDto actual) {
        long expectedValue = BenchmarkDtos.blackHole(expected);
        long actualValue = BenchmarkDtos.blackHole(actual);
        if (expectedValue != actualValue) {
            throw new IllegalStateException("cross-framework decode sanity check failed: " + expectedValue + " != " + actualValue);
        }
    }

    private static void sanityCheck(CrossFrameworkDtos.Frame expected, CrossFrameworkDtos.Frame actual) {
        long expectedValue = CrossFrameworkDtos.blackHole(expected);
        long actualValue = CrossFrameworkDtos.blackHole(actual);
        if (expectedValue != actualValue) {
            throw new IllegalStateException("target-framework decode sanity check failed: " + expectedValue + " != " + actualValue);
        }
    }

    private static void assertSameBytes(String dataset, byte[] left, byte[] right) {
        if (!java.util.Arrays.equals(left, right)) {
            throw new IllegalStateException(dataset + " cursor and ByteBuf encoders produced different bytes");
        }
    }

    private static double percentile(List<Double> sorted, double percentile) {
        if (sorted.isEmpty()) {
            return 0D;
        }
        int index = (int) Math.ceil(percentile * sorted.size()) - 1;
        return sorted.get(Math.max(0, Math.min(index, sorted.size() - 1)));
    }

    private static String format(double value) {
        return String.format(Locale.ROOT, "%.2f", value);
    }

    private record BenchmarkCase(String dataset, String codec, String operationName, int bytesPerOp, ThrowingOperation operation) {
    }

    private record BenchmarkResult(
            String dataset,
            String codec,
            String operation,
            int bytesPerOp,
            double nsPerOpMean,
            double nsPerOpP50,
            double nsPerOpP95,
            double opsPerSecond,
            double mibPerSecond,
            double allocatedBytesPerOp) {
    }

    private record DatasetSize(String dataset, String codec, int bytes) {
    }

    private interface ThrowingOperation {
        Object run() throws Exception;
    }

    private static final class BenchmarkConfig {
        final String profile;
        final int warmupIterations;
        final int measurementIterations;
        final int operationsPerIteration;

        private BenchmarkConfig(String profile, int warmupIterations, int measurementIterations, int operationsPerIteration) {
            this.profile = profile;
            this.warmupIterations = warmupIterations;
            this.measurementIterations = measurementIterations;
            this.operationsPerIteration = operationsPerIteration;
        }

        static BenchmarkConfig fromSystemProperties() {
            String profile = System.getProperty("zero.benchmark.profile", "quick");
            int warmup;
            int measure;
            int ops;
            switch (profile) {
                case "full" -> {
                    warmup = 5;
                    measure = 8;
                    ops = 3000;
                }
                case "standard" -> {
                    warmup = 3;
                    measure = 6;
                    ops = 1000;
                }
                default -> {
                    profile = "quick";
                    warmup = 2;
                    measure = 4;
                    ops = 200;
                }
            }
            warmup = Integer.getInteger("zero.benchmark.warmupIterations", warmup);
            measure = Integer.getInteger("zero.benchmark.measurementIterations", measure);
            ops = Integer.getInteger("zero.benchmark.operationsPerIteration", ops);
            return new BenchmarkConfig(profile, warmup, measure, ops);
        }
    }

    private static final class AllocationMeter {
        private final com.sun.management.ThreadMXBean bean;
        private final long threadId;

        private AllocationMeter(com.sun.management.ThreadMXBean bean) {
            this.bean = bean;
            this.threadId = Thread.currentThread().getId();
        }

        static AllocationMeter create() {
            java.lang.management.ThreadMXBean raw = ManagementFactory.getThreadMXBean();
            if (raw instanceof com.sun.management.ThreadMXBean bean
                    && bean.isThreadAllocatedMemorySupported()) {
                if (!bean.isThreadAllocatedMemoryEnabled()) {
                    bean.setThreadAllocatedMemoryEnabled(true);
                }
                return new AllocationMeter(bean);
            }
            return new AllocationMeter(null);
        }

        long bytes() {
            return bean == null ? -1L : bean.getThreadAllocatedBytes(threadId);
        }
    }
}
