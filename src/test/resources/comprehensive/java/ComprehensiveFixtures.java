package com.zero.codegen.comprehensive;

import com.zero.codegen.comprehensive.runtime.bytes.ArrayByteCursor;
import com.zero.codegen.comprehensive.runtime.proto.PayloadBuilder;
import com.zero.codegen.comprehensive.runtime.serialize.ICursorProto;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@SuppressWarnings({"rawtypes", "unchecked"})
public final class ComprehensiveFixtures {
    public static final int LARGE_MAP_SIZE = 256;

    private ComprehensiveFixtures() {
    }

    public static ComprehensivePayload buildSparseDefaults() {
        ComprehensivePayload payload = new ComprehensivePayload();
        payload.setRequestId(1001L);
        payload.setVersion(0);
        payload.setReady(false);
        payload.setProgress(0F);
        payload.setThreshold(0D);
        payload.setTraceId("");
        payload.setAlias(Optional.empty());
        payload.setCounts(new int[0]);
        payload.setMainSample(emptyScalarSample());
        payload.setSampleArray(new ScalarSample[0]);
        payload.setSampleList(new ArrayList<>());
        payload.setLeafBuckets(new List[0]);
        payload.setLabelBuckets(new Set[0]);
        payload.setBucketArray(new Map[0]);
        payload.setStagedBuckets(new ArrayList<>());
        payload.setLargeBuckets(new LinkedHashMap<>());
        payload.setPrimary(emptyNestedBucket());
        return payload;
    }

    public static ComprehensivePayload buildComplexDense() {
        return buildComplexDense(LARGE_MAP_SIZE);
    }

    public static ComprehensivePayload buildComplexDense(int largeMapSize) {
        ComprehensivePayload payload = new ComprehensivePayload();
        payload.setRequestId(998877665544L);
        payload.setVersion(7);
        payload.setReady(true);
        payload.setProgress(0.875F);
        payload.setThreshold(9876.125D);
        payload.setTraceId("trace-comprehensive-dense");
        payload.setAlias(Optional.of("dense-alias"));
        payload.setCounts(new int[]{1, 3, 5, 8, 13, 21});
        payload.setMainSample(scalarSample(100, 12.5F, 1024.75D, "main-sample"));
        payload.setSampleArray(new ScalarSample[]{
                scalarSample(101, 4.25F, 88.125D, "array-a"),
                scalarSample(102, -3.5F, -0.25D, "array-b")
        });
        payload.setSampleList(new ArrayList<>(List.of(
                scalarSample(103, 99.125F, 4096.5D, "list-a"),
                scalarSample(104, -11.75F, 0.5D, "list-b"),
                scalarSample(105, 0.0F, 1.0D / 3.0D, "list-c")
        )));
        payload.setLeafBuckets(new List[]{
                new ArrayList<>(List.of(
                        metricLeaf(1001L, MetricKind.TEMPERATURE, 0.125F, 11.5D, "bucket-0-a", 7, 9, 12),
                        metricLeaf(1002L, MetricKind.LOAD, 0.875F, 45.75D, "bucket-0-b", 3, 6))),
                new ArrayList<>(List.of(
                        metricLeaf(1003L, MetricKind.SPEED, 1.5F, 88.0D, "bucket-1-a", 4, 8),
                        metricLeaf(1004L, MetricKind.PRESSURE, 2.5F, 123.25D, "bucket-1-b", 5, 10, 15)))
        });
        payload.setLabelBuckets(new Set[]{
                new LinkedHashSet<>(List.of("solo")),
                new LinkedHashSet<>(List.of("duo"))
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

        payload.setLargeBuckets(buildLargeBuckets(largeMapSize, false));
        payload.setPrimary(nestedBucket(501, 51, "primary"));
        return payload;
    }

    public static ComprehensivePayload buildShiftedDense(int largeMapSize) {
        ComprehensivePayload payload = buildComplexDense(largeMapSize);
        Map<Integer, NestedBucket> shifted = new LinkedHashMap<>();
        for (Map.Entry<Integer, NestedBucket> entry : payload.getLargeBuckets().entrySet()) {
            if (entry.getKey() != 2) {
                shifted.put(entry.getKey(), entry.getValue());
            }
        }
        shifted.put(356, nestedBucket(856, 856, "shifted-new"));
        payload.setLargeBuckets(shifted);
        payload.setTraceId("trace-comprehensive-shifted");
        return payload;
    }

    public static ComprehensivePayload buildDirtyPayload() {
        ComprehensivePayload payload = new ComprehensivePayload();
        payload.setRequestId(555L);
        payload.setVersion(99);
        payload.setReady(true);
        payload.setProgress(-1.25F);
        payload.setThreshold(-99.5D);
        payload.setTraceId("dirty-trace");
        payload.setAlias(Optional.of("dirty-alias"));
        payload.setCounts(new int[]{99, 98, 97});
        payload.setMainSample(scalarSample(999, -1.5F, -5.5D, "dirty-main"));
        payload.setSampleArray(new ScalarSample[]{scalarSample(998, 3.5F, 7.5D, "dirty-array")});
        payload.setSampleList(new ArrayList<>(List.of(scalarSample(997, 4.5F, 8.5D, "dirty-list"))));
        payload.setLeafBuckets(new List[]{new ArrayList<>(List.of(metricLeaf(4001L, MetricKind.LOAD, 9.5F, 10.5D, "dirty-leaf", 1, 2, 3)))});
        payload.setLabelBuckets(new Set[]{new LinkedHashSet<>(List.of("dirty"))});
        Map<Integer, NestedBucket> dirtyBucket = new LinkedHashMap<>();
        dirtyBucket.put(1, nestedBucket(901, 901, "dirty-bucket"));
        payload.setBucketArray(new Map[]{dirtyBucket});
        List<Map<Integer, NestedBucket>> staged = new ArrayList<>();
        staged.add(new LinkedHashMap<>(Map.of(77, nestedBucket(977, 977, "dirty-staged"))));
        payload.setStagedBuckets(staged);
        payload.setLargeBuckets(buildLargeBuckets(48, true));
        payload.setPrimary(nestedBucket(777, 777, "dirty-primary"));
        return payload;
    }

    public static byte[] encodeCursor(ICursorProto payload) {
        return PayloadBuilder.build(payload::writeTo);
    }

    public static byte[] encodeByteBuf(ICursorProto payload) {
        ByteBuf buf = Unpooled.buffer(1024);
        try {
            payload.writeTo(buf);
            byte[] bytes = new byte[buf.readableBytes()];
            buf.readBytes(bytes);
            return bytes;
        } finally {
            buf.release();
        }
    }

    public static ComprehensivePayload decodeCursor(byte[] bytes) {
        return ComprehensivePayload.readFrom(new ArrayByteCursor(bytes));
    }

    public static ComprehensivePayload decodeByteBuf(byte[] bytes) {
        ByteBuf buffer = Unpooled.wrappedBuffer(bytes);
        try {
            return ComprehensivePayload.readFrom(buffer);
        } finally {
            buffer.release();
        }
    }

    public static void readIntoCursor(byte[] bytes, ComprehensivePayload existing) {
        ComprehensivePayload.readInto(new ArrayByteCursor(bytes), existing);
    }

    public static String sha256Base64(byte[] bytes) {
        try {
            return Base64.getEncoder().encodeToString(MessageDigest.getInstance("SHA-256").digest(bytes));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Missing SHA-256", e);
        }
    }

    public static String signature(ComprehensivePayload payload) {
        StringBuilder sb = new StringBuilder(16384);
        appendPayload(sb, payload);
        return sha256Base64(sb.toString().getBytes(StandardCharsets.UTF_8));
    }

    public static String summary(ComprehensivePayload payload) {
        return "signature=" + signature(payload)
                + ",requestId=" + payload.getRequestId()
                + ",traceId=" + payload.getTraceId()
                + ",largeBuckets=" + payload.getLargeBuckets().size();
    }

    public static long blackHoleValue(ComprehensivePayload payload) {
        long value = payload.getRequestId() ^ payload.getVersion();
        value += payload.getCounts().length;
        value += payload.getSampleArray().length;
        value += payload.getSampleList().size();
        value += payload.getLeafBuckets().length;
        value += payload.getLabelBuckets().length;
        value += payload.getBucketArray().length;
        value += payload.getStagedBuckets().size();
        value += payload.getLargeBuckets().size();
        if (payload.getPrimary() != null) {
            value += payload.getPrimary().getBucketId();
        }
        return value;
    }

    private static Map<Integer, NestedBucket> buildLargeBuckets(int size, boolean dirty) {
        Map<Integer, NestedBucket> buckets = new LinkedHashMap<>();
        for (int i = 1; i <= size; i++) {
            int seed = dirty ? 500 + i : i;
            buckets.put(i, nestedBucket(2000 + i, seed, dirty ? "dirty-large" : "large"));
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

    private static ScalarSample emptyScalarSample() {
        return scalarSample(0, 0F, 0D, "");
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
        bucket.setLeafList(new ArrayList<>(List.of(
                metricLeaf(seed * 10L + 3L, MetricKind.LOAD, seed / 70.0F, seed * 3.5D, prefix + "-list-a", seed + 5),
                metricLeaf(seed * 10L + 4L, MetricKind.PRESSURE, seed / 60.0F, seed * 4.5D, prefix + "-list-b", seed + 6, seed + 7)
        )));

        Map<Integer, MetricLeaf> leafMap = new LinkedHashMap<>();
        leafMap.put(1, metricLeaf(seed * 10L + 5L, MetricKind.LOAD, seed / 55.0F, seed * 5.5D, prefix + "-map-a", seed + 8));
        leafMap.put(2, metricLeaf(seed * 10L + 6L, MetricKind.PRESSURE, seed / 45.0F, seed * 6.5D, prefix + "-map-b", seed + 9, seed + 10));
        bucket.setLeafMap(leafMap);

        Map<Integer, List<Integer>> adjacency = new LinkedHashMap<>();
        adjacency.put(1, new ArrayList<>(List.of(seed, seed + 1, seed + 2)));
        adjacency.put(2, new ArrayList<>(List.of(seed + 3, seed + 4)));
        bucket.setAdjacency(adjacency);
        return bucket;
    }

    private static NestedBucket emptyNestedBucket() {
        NestedBucket bucket = new NestedBucket();
        bucket.setBucketId(0);
        bucket.setSample(emptyScalarSample());
        bucket.setLeafArray(new MetricLeaf[0]);
        bucket.setLeafList(new ArrayList<>());
        bucket.setLeafMap(new LinkedHashMap<>());
        bucket.setAdjacency(new LinkedHashMap<>());
        return bucket;
    }

    private static void appendPayload(StringBuilder sb, ComprehensivePayload payload) {
        appendLong(sb, payload.getRequestId());
        appendInt(sb, payload.getVersion());
        appendBoolean(sb, payload.getReady());
        appendFloat(sb, payload.getProgress());
        appendDouble(sb, payload.getThreshold());
        appendString(sb, payload.getTraceId());
        appendBoolean(sb, payload.getAlias().isPresent());
        if (payload.getAlias().isPresent()) {
            appendString(sb, payload.getAlias().orElseThrow());
        }
        appendIntArray(sb, payload.getCounts());
        appendScalarSample(sb, payload.getMainSample());
        appendScalarArray(sb, payload.getSampleArray());
        appendScalarList(sb, payload.getSampleList());
        appendLeafBucketArray(sb, payload.getLeafBuckets());
        appendLabelBucketArray(sb, payload.getLabelBuckets());
        appendBucketArray(sb, payload.getBucketArray());
        appendStagedBuckets(sb, payload.getStagedBuckets());
        appendNestedBucketMap(sb, payload.getLargeBuckets());
        appendNestedBucket(sb, payload.getPrimary());
    }

    private static void appendScalarSample(StringBuilder sb, ScalarSample sample) {
        if (sample == null) {
            sb.append("null;");
            return;
        }
        appendInt(sb, sample.getCode());
        appendFloat(sb, sample.getF32());
        appendDouble(sb, sample.getF64());
        appendString(sb, sample.getLabel());
    }

    private static void appendMetricLeaf(StringBuilder sb, MetricLeaf leaf) {
        if (leaf == null) {
            sb.append("null;");
            return;
        }
        appendLong(sb, leaf.getId());
        appendString(sb, leaf.getKind().name());
        appendFloat(sb, leaf.getRatio());
        appendDouble(sb, leaf.getScore());
        appendString(sb, leaf.getNote());
        appendIntList(sb, leaf.getTags());
    }

    private static void appendNestedBucket(StringBuilder sb, NestedBucket bucket) {
        if (bucket == null) {
            sb.append("null;");
            return;
        }
        appendInt(sb, bucket.getBucketId());
        appendScalarSample(sb, bucket.getSample());
        appendMetricLeafArray(sb, bucket.getLeafArray());
        appendMetricLeafList(sb, bucket.getLeafList());
        appendMetricLeafMap(sb, bucket.getLeafMap());
        appendAdjacency(sb, bucket.getAdjacency());
    }

    private static void appendIntArray(StringBuilder sb, int[] values) {
        sb.append(values.length).append(';');
        for (int value : values) {
            appendInt(sb, value);
        }
    }

    private static void appendScalarArray(StringBuilder sb, ScalarSample[] values) {
        sb.append(values.length).append(';');
        for (ScalarSample value : values) {
            appendScalarSample(sb, value);
        }
    }

    private static void appendScalarList(StringBuilder sb, List<ScalarSample> values) {
        sb.append(values.size()).append(';');
        for (ScalarSample value : values) {
            appendScalarSample(sb, value);
        }
    }

    private static void appendLeafBucketArray(StringBuilder sb, List<MetricLeaf>[] values) {
        sb.append(values.length).append(';');
        for (List<MetricLeaf> bucket : values) {
            appendMetricLeafList(sb, bucket);
        }
    }

    private static void appendLabelBucketArray(StringBuilder sb, Set<String>[] values) {
        sb.append(values.length).append(';');
        for (Set<String> bucket : values) {
            List<String> sorted = new ArrayList<>(bucket);
            Collections.sort(sorted);
            sb.append(sorted.size()).append(';');
            for (String value : sorted) {
                appendString(sb, value);
            }
        }
    }

    private static void appendBucketArray(StringBuilder sb, Map<Integer, NestedBucket>[] values) {
        sb.append(values.length).append(';');
        for (Map<Integer, NestedBucket> value : values) {
            appendNestedBucketMap(sb, value);
        }
    }

    private static void appendStagedBuckets(StringBuilder sb, List<Map<Integer, NestedBucket>> values) {
        sb.append(values.size()).append(';');
        for (Map<Integer, NestedBucket> value : values) {
            appendNestedBucketMap(sb, value);
        }
    }

    private static void appendNestedBucketMap(StringBuilder sb, Map<Integer, NestedBucket> map) {
        List<Integer> keys = new ArrayList<>(map.keySet());
        Collections.sort(keys);
        sb.append(keys.size()).append(';');
        for (Integer key : keys) {
            appendInt(sb, key);
            appendNestedBucket(sb, map.get(key));
        }
    }

    private static void appendMetricLeafArray(StringBuilder sb, MetricLeaf[] values) {
        sb.append(values.length).append(';');
        for (MetricLeaf value : values) {
            appendMetricLeaf(sb, value);
        }
    }

    private static void appendMetricLeafList(StringBuilder sb, List<MetricLeaf> values) {
        sb.append(values.size()).append(';');
        for (MetricLeaf value : values) {
            appendMetricLeaf(sb, value);
        }
    }

    private static void appendMetricLeafMap(StringBuilder sb, Map<Integer, MetricLeaf> map) {
        List<Integer> keys = new ArrayList<>(map.keySet());
        Collections.sort(keys);
        sb.append(keys.size()).append(';');
        for (Integer key : keys) {
            appendInt(sb, key);
            appendMetricLeaf(sb, map.get(key));
        }
    }

    private static void appendAdjacency(StringBuilder sb, Map<Integer, List<Integer>> adjacency) {
        List<Integer> keys = new ArrayList<>(adjacency.keySet());
        Collections.sort(keys);
        sb.append(keys.size()).append(';');
        for (Integer key : keys) {
            appendInt(sb, key);
            appendIntList(sb, adjacency.get(key));
        }
    }

    private static void appendIntList(StringBuilder sb, List<Integer> values) {
        sb.append(values.size()).append(';');
        for (Integer value : values) {
            appendInt(sb, value);
        }
    }

    private static void appendBoolean(StringBuilder sb, boolean value) {
        sb.append(value ? '1' : '0').append(';');
    }

    private static void appendInt(StringBuilder sb, int value) {
        sb.append(value).append(';');
    }

    private static void appendLong(StringBuilder sb, long value) {
        sb.append(value).append(';');
    }

    private static void appendFloat(StringBuilder sb, float value) {
        sb.append(Float.floatToIntBits(value)).append(';');
    }

    private static void appendDouble(StringBuilder sb, double value) {
        sb.append(Double.doubleToLongBits(value)).append(';');
    }

    private static void appendString(StringBuilder sb, String value) {
        String safe = value == null ? "<null>" : value;
        sb.append(safe.length()).append(':').append(safe).append(';');
    }
}
