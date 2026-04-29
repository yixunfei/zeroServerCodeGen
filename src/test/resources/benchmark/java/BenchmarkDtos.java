package com.zero.codegen.benchmark.generated;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class BenchmarkDtos {
    private BenchmarkDtos() {
    }

    public enum MetricKindDto {
        UNKNOWN,
        TEMPERATURE,
        SPEED,
        LOAD,
        PRESSURE
    }

    public static final class FixedPointDto implements Serializable {
        public int x;
        public long y;
        public float z;
        public double score;
    }

    public static final class FixedTelemetryDto implements Serializable {
        public int id;
        public long time;
        public float ratio;
        public double score;
        public FixedPointDto point;
    }

    public static final class ScalarSampleDto implements Serializable {
        public int code;
        public float f32;
        public double f64;
        public String label;
    }

    public static final class MetricLeafDto implements Serializable {
        public long id;
        public MetricKindDto kind;
        public float ratio;
        public double score;
        public String note;
        public List<Integer> tags = new ArrayList<>();
    }

    public static final class NestedBucketDto implements Serializable {
        public int bucketId;
        public ScalarSampleDto sample;
        public List<MetricLeafDto> leafArray = new ArrayList<>();
        public List<MetricLeafDto> leafList = new ArrayList<>();
        public Map<Integer, MetricLeafDto> leafMap = new LinkedHashMap<>();
        public Map<Integer, List<Integer>> adjacency = new LinkedHashMap<>();
    }

    public static final class PackedCollectionsDto implements Serializable {
        public long uid;
        public List<Integer> values = new ArrayList<>();
        public List<Long> ids = new ArrayList<>();
        public Map<Integer, Integer> scores = new LinkedHashMap<>();
        public Map<Integer, Long> versions = new LinkedHashMap<>();
    }

    public static final class ByteBlobDto implements Serializable {
        public long uid;
        public byte[] raw = new byte[0];
        public int[] samples = new int[0];
        public long[] ticks = new long[0];
        public float[] ratios = new float[0];
        public double[] scores = new double[0];
    }

    public static final class PayloadDto implements Serializable {
        public long requestId;
        public int version;
        public boolean ready;
        public float progress;
        public double threshold;
        public String traceId;
        public String alias;
        public int[] counts = new int[0];
        public ByteBlobDto blob;
        public ScalarSampleDto mainSample;
        public List<ScalarSampleDto> sampleArray = new ArrayList<>();
        public List<ScalarSampleDto> sampleList = new ArrayList<>();
        public List<List<MetricLeafDto>> leafBuckets = new ArrayList<>();
        public List<Set<String>> labelBuckets = new ArrayList<>();
        public List<Map<Integer, NestedBucketDto>> bucketArray = new ArrayList<>();
        public List<Map<Integer, NestedBucketDto>> stagedBuckets = new ArrayList<>();
        public Map<Integer, NestedBucketDto> largeBuckets = new LinkedHashMap<>();
        public PackedCollectionsDto packed;
        public FixedTelemetryDto telemetry;
        public NestedBucketDto primary;
    }

    public static PayloadDto fromGenerated(BenchmarkPayload payload) {
        PayloadDto dto = new PayloadDto();
        dto.requestId = payload.getRequestId();
        dto.version = payload.getVersion();
        dto.ready = payload.getReady();
        dto.progress = payload.getProgress();
        dto.threshold = payload.getThreshold();
        dto.traceId = payload.getTraceId();
        dto.alias = payload.getAlias().orElse(null);
        dto.counts = payload.getCounts();
        dto.blob = fromGenerated(payload.getBlob());
        dto.mainSample = fromGenerated(payload.getMainSample());
        for (ScalarSample sample : payload.getSampleArray()) {
            dto.sampleArray.add(fromGenerated(sample));
        }
        for (ScalarSample sample : payload.getSampleList()) {
            dto.sampleList.add(fromGenerated(sample));
        }
        for (List<MetricLeaf> bucket : payload.getLeafBuckets()) {
            List<MetricLeafDto> items = new ArrayList<>();
            for (MetricLeaf leaf : bucket) {
                items.add(fromGenerated(leaf));
            }
            dto.leafBuckets.add(items);
        }
        for (Set<String> bucket : payload.getLabelBuckets()) {
            dto.labelBuckets.add(new LinkedHashSet<>(bucket));
        }
        for (Map<Integer, NestedBucket> bucket : payload.getBucketArray()) {
            dto.bucketArray.add(fromGeneratedNestedMap(bucket));
        }
        for (Map<Integer, NestedBucket> bucket : payload.getStagedBuckets()) {
            dto.stagedBuckets.add(fromGeneratedNestedMap(bucket));
        }
        dto.largeBuckets = fromGeneratedNestedMap(payload.getLargeBuckets());
        dto.packed = fromGenerated(payload.getPacked());
        dto.telemetry = fromGenerated(payload.getTelemetry());
        dto.primary = fromGenerated(payload.getPrimary());
        return dto;
    }

    public static FixedTelemetryDto fromGenerated(FixedTelemetry telemetry) {
        FixedTelemetryDto dto = new FixedTelemetryDto();
        dto.id = telemetry.getId();
        dto.time = telemetry.getTime();
        dto.ratio = telemetry.getRatio();
        dto.score = telemetry.getScore();
        dto.point = fromGenerated(telemetry.getPoint());
        return dto;
    }

    public static PackedCollectionsDto fromGenerated(PackedCollections packed) {
        PackedCollectionsDto dto = new PackedCollectionsDto();
        dto.uid = packed.getUid();
        dto.values.addAll(packed.getValues());
        dto.ids.addAll(packed.getIds());
        dto.scores.putAll(packed.getScores());
        dto.versions.putAll(packed.getVersions());
        return dto;
    }

    public static long blackHole(PayloadDto dto) {
        long value = dto.requestId ^ dto.version;
        value += dto.counts.length;
        value += dto.sampleArray.size();
        value += dto.sampleList.size();
        value += dto.leafBuckets.size();
        value += dto.labelBuckets.size();
        value += dto.bucketArray.size();
        value += dto.stagedBuckets.size();
        value += dto.largeBuckets.size();
        if (dto.blob != null) {
            value += dto.blob.raw.length + dto.blob.samples.length + dto.blob.ticks.length;
        }
        if (dto.primary != null) {
            value += dto.primary.bucketId;
        }
        return value;
    }

    public static long blackHole(FixedTelemetryDto dto) {
        long value = dto.id ^ dto.time;
        if (dto.point != null) {
            value += dto.point.x + (long) dto.point.z;
        }
        return value;
    }

    public static long blackHole(PackedCollectionsDto dto) {
        return dto.uid + dto.values.size() + dto.ids.size() + dto.scores.size() + dto.versions.size();
    }

    private static FixedPointDto fromGenerated(FixedPoint point) {
        FixedPointDto dto = new FixedPointDto();
        dto.x = point.getX();
        dto.y = point.getY();
        dto.z = point.getZ();
        dto.score = point.getScore();
        return dto;
    }

    private static ScalarSampleDto fromGenerated(ScalarSample sample) {
        ScalarSampleDto dto = new ScalarSampleDto();
        dto.code = sample.getCode();
        dto.f32 = sample.getF32();
        dto.f64 = sample.getF64();
        dto.label = sample.getLabel();
        return dto;
    }

    private static MetricLeafDto fromGenerated(MetricLeaf leaf) {
        MetricLeafDto dto = new MetricLeafDto();
        dto.id = leaf.getId();
        dto.kind = MetricKindDto.valueOf(leaf.getKind().name());
        dto.ratio = leaf.getRatio();
        dto.score = leaf.getScore();
        dto.note = leaf.getNote();
        dto.tags.addAll(leaf.getTags());
        return dto;
    }

    private static NestedBucketDto fromGenerated(NestedBucket bucket) {
        NestedBucketDto dto = new NestedBucketDto();
        dto.bucketId = bucket.getBucketId();
        dto.sample = fromGenerated(bucket.getSample());
        for (MetricLeaf leaf : bucket.getLeafArray()) {
            dto.leafArray.add(fromGenerated(leaf));
        }
        for (MetricLeaf leaf : bucket.getLeafList()) {
            dto.leafList.add(fromGenerated(leaf));
        }
        for (Map.Entry<Integer, MetricLeaf> entry : bucket.getLeafMap().entrySet()) {
            dto.leafMap.put(entry.getKey(), fromGenerated(entry.getValue()));
        }
        for (Map.Entry<Integer, List<Integer>> entry : bucket.getAdjacency().entrySet()) {
            dto.adjacency.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }
        return dto;
    }

    private static Map<Integer, NestedBucketDto> fromGeneratedNestedMap(Map<Integer, NestedBucket> source) {
        Map<Integer, NestedBucketDto> map = new LinkedHashMap<>();
        for (Map.Entry<Integer, NestedBucket> entry : source.entrySet()) {
            map.put(entry.getKey(), fromGenerated(entry.getValue()));
        }
        return map;
    }

    private static ByteBlobDto fromGenerated(ByteBlob blob) {
        ByteBlobDto dto = new ByteBlobDto();
        dto.uid = blob.getUid();
        dto.raw = blob.getRaw();
        dto.samples = blob.getSamples();
        dto.ticks = blob.getTicks();
        dto.ratios = blob.getRatios();
        dto.scores = blob.getScores();
        return dto;
    }
}
