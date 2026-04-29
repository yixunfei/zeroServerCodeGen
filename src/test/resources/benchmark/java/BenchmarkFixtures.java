package com.zero.codegen.benchmark.generated;

import com.zero.codegen.benchmark.generated.runtime.bytes.ArrayByteCursor;
import com.zero.codegen.benchmark.generated.runtime.proto.PayloadBuilder;
import com.zero.codegen.benchmark.generated.runtime.serialize.ICursorProto;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@SuppressWarnings({"rawtypes", "unchecked"})
public final class BenchmarkFixtures {
    private BenchmarkFixtures() {
    }

    public static BenchmarkPayload sparsePayload() {
        BenchmarkPayload payload = new BenchmarkPayload();
        payload.setRequestId(1001L);
        payload.setVersion(0);
        payload.setReady(false);
        payload.setProgress(0F);
        payload.setThreshold(0D);
        payload.setTraceId("");
        payload.setAlias(Optional.empty());
        payload.setCounts(new int[0]);
        payload.setBlob(blob(0, 0));
        payload.setMainSample(scalarSample(0, 0F, 0D, ""));
        payload.setSampleArray(new ScalarSample[0]);
        payload.setSampleList(new ArrayList<>());
        payload.setLeafBuckets(new List[0]);
        payload.setLabelBuckets(new Set[0]);
        payload.setBucketArray(new Map[0]);
        payload.setStagedBuckets(new ArrayList<>());
        payload.setLargeBuckets(new LinkedHashMap<>());
        payload.setPacked(packedCollections(1001L, 0));
        payload.setTelemetry(fixedTelemetry(0));
        payload.setPrimary(nestedBucket(0, 0, "empty"));
        return payload;
    }

    public static BenchmarkPayload densePayload(int largeMapSize, int blobBytes, int packedSize) {
        BenchmarkPayload payload = new BenchmarkPayload();
        payload.setRequestId(998877665544L + largeMapSize);
        payload.setVersion(7);
        payload.setReady(true);
        payload.setProgress(0.875F);
        payload.setThreshold(9876.125D);
        payload.setTraceId("trace-benchmark-" + largeMapSize);
        payload.setAlias(Optional.of("alias-" + largeMapSize));
        payload.setCounts(new int[]{1, 3, 5, 8, 13, 21, 34, 55});
        payload.setBlob(blob(42, blobBytes));
        payload.setMainSample(scalarSample(100, 12.5F, 1024.75D, "main-sample"));
        payload.setSampleArray(new ScalarSample[]{
                scalarSample(101, 4.25F, 88.125D, "array-a"),
                scalarSample(102, -3.5F, -0.25D, "array-b"),
                scalarSample(103, 7.0F, 100.5D, "array-c")
        });
        payload.setSampleList(new ArrayList<>(List.of(
                scalarSample(201, 99.125F, 4096.5D, "list-a"),
                scalarSample(202, -11.75F, 0.5D, "list-b"),
                scalarSample(203, 0.0F, 1.0D / 3.0D, "list-c"))));

        payload.setLeafBuckets(new List[]{
                new ArrayList<>(List.of(
                        metricLeaf(1001L, MetricKind.TEMPERATURE, 0.125F, 11.5D, "bucket-0-a", 7, 9, 12),
                        metricLeaf(1002L, MetricKind.LOAD, 0.875F, 45.75D, "bucket-0-b", 3, 6))),
                new ArrayList<>(List.of(
                        metricLeaf(1003L, MetricKind.SPEED, 1.5F, 88.0D, "bucket-1-a", 4, 8),
                        metricLeaf(1004L, MetricKind.PRESSURE, 2.5F, 123.25D, "bucket-1-b", 5, 10, 15)))
        });
        payload.setLabelBuckets(new Set[]{
                new LinkedHashSet<>(List.of("solo", "ranked")),
                new LinkedHashSet<>(List.of("duo", "arena"))
        });

        Map<Integer, NestedBucket> firstBucket = new LinkedHashMap<>();
        firstBucket.put(1, nestedBucket(11, 11, "bucket-array-a"));
        firstBucket.put(2, nestedBucket(12, 12, "bucket-array-b"));
        Map<Integer, NestedBucket> secondBucket = new LinkedHashMap<>();
        secondBucket.put(7, nestedBucket(17, 17, "bucket-array-c"));
        payload.setBucketArray(new Map[]{firstBucket, secondBucket});

        List<Map<Integer, NestedBucket>> stagedBuckets = new ArrayList<>();
        Map<Integer, NestedBucket> stagedA = new LinkedHashMap<>();
        stagedA.put(101, nestedBucket(101, 21, "staged-a"));
        stagedA.put(102, nestedBucket(102, 22, "staged-b"));
        Map<Integer, NestedBucket> stagedB = new LinkedHashMap<>();
        stagedB.put(201, nestedBucket(201, 31, "staged-c"));
        stagedBuckets.add(stagedA);
        stagedBuckets.add(stagedB);
        payload.setStagedBuckets(stagedBuckets);

        payload.setLargeBuckets(buildLargeBuckets(largeMapSize));
        payload.setPacked(packedCollections(largeMapSize, packedSize));
        payload.setTelemetry(fixedTelemetry(largeMapSize));
        payload.setPrimary(nestedBucket(501, 51, "primary"));
        return payload;
    }

    public static FixedTelemetry fixedTelemetry(int seed) {
        FixedPoint point = new FixedPoint();
        point.setX(seed + 1);
        point.setY(123456789L + seed);
        point.setZ(seed / 10.0F);
        point.setScore(seed * 2.5D + 0.125D);

        FixedTelemetry telemetry = new FixedTelemetry();
        telemetry.setId(seed);
        telemetry.setTime(1700000000000L + seed);
        telemetry.setRatio(seed / 100.0F);
        telemetry.setScore(seed * 7.25D);
        telemetry.setPoint(point);
        return telemetry;
    }

    public static PackedCollections packedCollections(long uid, int count) {
        PackedCollections packed = new PackedCollections();
        packed.setUid(uid);
        List<Integer> values = new ArrayList<>(count);
        List<Long> ids = new ArrayList<>(count);
        Map<Integer, Integer> scores = new LinkedHashMap<>();
        Map<Integer, Long> versions = new LinkedHashMap<>();
        for (int i = 0; i < count; i++) {
            values.add(i * 3 + 1);
            ids.add(9000000000L + i);
            scores.put(i, i * 17);
            versions.put(i, 100000L + i * 37L);
        }
        packed.setValues(values);
        packed.setIds(ids);
        packed.setScores(scores);
        packed.setVersions(versions);
        return packed;
    }

    public static byte[] encodeCursor(ICursorProto payload) {
        return PayloadBuilder.build(payload::writeTo);
    }

    public static byte[] encodeByteBuf(ICursorProto payload) {
        ByteBuf buf = Unpooled.buffer(Math.max(256, payload.estimatedSize()));
        try {
            payload.writeTo(buf);
            byte[] bytes = new byte[buf.readableBytes()];
            buf.readBytes(bytes);
            return bytes;
        } finally {
            buf.release();
        }
    }

    public static BenchmarkPayload decodePayloadCursor(byte[] bytes) {
        return BenchmarkPayload.readFrom(new ArrayByteCursor(bytes));
    }

    public static BenchmarkPayload decodePayloadByteBuf(byte[] bytes) {
        ByteBuf buffer = Unpooled.wrappedBuffer(bytes);
        try {
            return BenchmarkPayload.readFrom(buffer);
        } finally {
            buffer.release();
        }
    }

    public static void readIntoPayloadCursor(byte[] bytes, BenchmarkPayload target) {
        BenchmarkPayload.readInto(new ArrayByteCursor(bytes), target);
    }

    public static FixedTelemetry decodeFixedCursor(byte[] bytes) {
        return FixedTelemetry.readFrom(new ArrayByteCursor(bytes));
    }

    public static FixedTelemetry decodeFixedByteBuf(byte[] bytes) {
        ByteBuf buffer = Unpooled.wrappedBuffer(bytes);
        try {
            return FixedTelemetry.readFrom(buffer);
        } finally {
            buffer.release();
        }
    }

    public static void readIntoFixedCursor(byte[] bytes, FixedTelemetry target) {
        FixedTelemetry.readInto(new ArrayByteCursor(bytes), target);
    }

    public static PackedCollections decodePackedCursor(byte[] bytes) {
        return PackedCollections.readFrom(new ArrayByteCursor(bytes));
    }

    public static PackedCollections decodePackedByteBuf(byte[] bytes) {
        ByteBuf buffer = Unpooled.wrappedBuffer(bytes);
        try {
            return PackedCollections.readFrom(buffer);
        } finally {
            buffer.release();
        }
    }

    public static void readIntoPackedCursor(byte[] bytes, PackedCollections target) {
        PackedCollections.readInto(new ArrayByteCursor(bytes), target);
    }

    public static long blackHole(BenchmarkPayload payload) {
        long value = payload.getRequestId() ^ payload.getVersion();
        value += payload.getCounts().length;
        value += payload.getSampleArray().length;
        value += payload.getSampleList().size();
        value += payload.getLeafBuckets().length;
        value += payload.getLabelBuckets().length;
        value += payload.getBucketArray().length;
        value += payload.getStagedBuckets().size();
        value += payload.getLargeBuckets().size();
        value += payload.getPacked().getValues().size();
        value += payload.getBlob().getRaw().length;
        if (payload.getPrimary() != null) {
            value += payload.getPrimary().getBucketId();
        }
        return value;
    }

    public static long blackHole(FixedTelemetry telemetry) {
        return telemetry.getId() ^ telemetry.getTime() ^ telemetry.getPoint().getY();
    }

    public static long blackHole(PackedCollections packed) {
        return packed.getUid() + packed.getValues().size() + packed.getIds().size()
                + packed.getScores().size() + packed.getVersions().size();
    }

    private static ByteBlob blob(long uid, int bytes) {
        ByteBlob blob = new ByteBlob();
        blob.setUid(uid);
        byte[] raw = new byte[bytes];
        int[] samples = new int[Math.max(0, bytes / 16)];
        long[] ticks = new long[Math.max(0, bytes / 32)];
        float[] ratios = new float[Math.max(0, bytes / 32)];
        double[] scores = new double[Math.max(0, bytes / 64)];
        for (int i = 0; i < raw.length; i++) {
            raw[i] = (byte) (i * 31 + 7);
        }
        for (int i = 0; i < samples.length; i++) {
            samples[i] = i * 5 + 1;
        }
        for (int i = 0; i < ticks.length; i++) {
            ticks[i] = 1700000000000L + i * 13L;
        }
        for (int i = 0; i < ratios.length; i++) {
            ratios[i] = i / 10.0F;
        }
        for (int i = 0; i < scores.length; i++) {
            scores[i] = i * 1.25D;
        }
        blob.setRaw(raw);
        blob.setSamples(samples);
        blob.setTicks(ticks);
        blob.setRatios(ratios);
        blob.setScores(scores);
        return blob;
    }

    private static Map<Integer, NestedBucket> buildLargeBuckets(int size) {
        Map<Integer, NestedBucket> buckets = new LinkedHashMap<>();
        for (int i = 1; i <= size; i++) {
            buckets.put(i, nestedBucket(2000 + i, i, "large"));
        }
        return buckets;
    }

    private static ScalarSample scalarSample(int code, float f32, double f64, String label) {
        ScalarSample sample = new ScalarSample();
        sample.setCode(code);
        sample.setF32(f32);
        sample.setF64(f64);
        sample.setLabel(label);
        return sample;
    }

    private static MetricLeaf metricLeaf(long id, MetricKind kind, float ratio, double score, String note, int... tags) {
        MetricLeaf leaf = new MetricLeaf();
        leaf.setId(id);
        leaf.setKind(kind);
        leaf.setRatio(ratio);
        leaf.setScore(score);
        leaf.setNote(note);
        leaf.setTags(new ArrayList<>(Arrays.stream(tags).boxed().toList()));
        return leaf;
    }

    private static NestedBucket nestedBucket(int bucketId, int seed, String prefix) {
        NestedBucket bucket = new NestedBucket();
        bucket.setBucketId(bucketId);
        bucket.setSample(scalarSample(seed, seed / 10.0F, seed * 3.25D, prefix + "-sample"));

        MetricLeaf first = metricLeaf(seed * 10L + 1L, MetricKind.TEMPERATURE, seed / 100.0F, seed * 1.5D, prefix + "-leaf-a", seed, seed + 1);
        MetricLeaf second = metricLeaf(seed * 10L + 2L, MetricKind.SPEED, seed / 80.0F, seed * 2.5D, prefix + "-leaf-b", seed + 2, seed + 3, seed + 4);
        bucket.setLeafArray(new MetricLeaf[]{first, second});
        bucket.setLeafList(new ArrayList<>(List.of(first, second)));

        Map<Integer, MetricLeaf> leafMap = new LinkedHashMap<>();
        leafMap.put(seed, first);
        leafMap.put(seed + 1, second);
        bucket.setLeafMap(leafMap);

        Map<Integer, List<Integer>> adjacency = new LinkedHashMap<>();
        adjacency.put(seed, new ArrayList<>(List.of(seed + 10, seed + 11)));
        adjacency.put(seed + 1, new ArrayList<>(List.of(seed + 20, seed + 21, seed + 22)));
        bucket.setAdjacency(adjacency);
        return bucket;
    }
}
