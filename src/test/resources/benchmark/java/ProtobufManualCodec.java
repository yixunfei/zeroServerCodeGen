package com.zero.codegen.benchmark.generated;

import com.google.protobuf.CodedInputStream;
import com.google.protobuf.CodedOutputStream;
import com.google.protobuf.WireFormat;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

final class ProtobufManualCodec {
    private ProtobufManualCodec() {
    }

    static byte[] encodePayload(BenchmarkDtos.PayloadDto payload) throws Exception {
        return write(out -> {
            out.writeInt64(1, payload.requestId);
            out.writeInt32(2, payload.version);
            out.writeBool(3, payload.ready);
            out.writeFloat(4, payload.progress);
            out.writeDouble(5, payload.threshold);
            writeString(out, 6, payload.traceId);
            writeString(out, 7, payload.alias);
            for (int value : payload.counts) {
                out.writeInt32(8, value);
            }
            writeBytes(out, 9, encodeBlob(payload.blob));
            writeBytes(out, 10, encodeScalar(payload.mainSample));
            for (BenchmarkDtos.ScalarSampleDto sample : payload.sampleArray) {
                writeBytes(out, 11, encodeScalar(sample));
            }
            for (BenchmarkDtos.ScalarSampleDto sample : payload.sampleList) {
                writeBytes(out, 12, encodeScalar(sample));
            }
            for (List<BenchmarkDtos.MetricLeafDto> bucket : payload.leafBuckets) {
                writeBytes(out, 13, encodeLeafList(bucket));
            }
            for (java.util.Set<String> bucket : payload.labelBuckets) {
                writeBytes(out, 14, encodeStringSet(bucket));
            }
            for (Map<Integer, BenchmarkDtos.NestedBucketDto> bucket : payload.bucketArray) {
                writeBytes(out, 15, encodeNestedMap(bucket));
            }
            for (Map<Integer, BenchmarkDtos.NestedBucketDto> bucket : payload.stagedBuckets) {
                writeBytes(out, 16, encodeNestedMap(bucket));
            }
            for (Map.Entry<Integer, BenchmarkDtos.NestedBucketDto> entry : payload.largeBuckets.entrySet()) {
                writeBytes(out, 17, encodeNestedMapEntry(entry.getKey(), entry.getValue()));
            }
            writeBytes(out, 18, encodePacked(payload.packed));
            writeBytes(out, 19, encodeTelemetry(payload.telemetry));
            writeBytes(out, 20, encodeNested(payload.primary));
        });
    }

    static BenchmarkDtos.PayloadDto decodePayload(byte[] bytes) throws Exception {
        CodedInputStream input = CodedInputStream.newInstance(bytes);
        BenchmarkDtos.PayloadDto payload = new BenchmarkDtos.PayloadDto();
        List<Integer> counts = new ArrayList<>();
        while (!input.isAtEnd()) {
            int tag = input.readTag();
            switch (WireFormat.getTagFieldNumber(tag)) {
                case 1 -> payload.requestId = input.readInt64();
                case 2 -> payload.version = input.readInt32();
                case 3 -> payload.ready = input.readBool();
                case 4 -> payload.progress = input.readFloat();
                case 5 -> payload.threshold = input.readDouble();
                case 6 -> payload.traceId = input.readString();
                case 7 -> payload.alias = input.readString();
                case 8 -> counts.add(input.readInt32());
                case 9 -> payload.blob = decodeBlob(input.readByteArray());
                case 10 -> payload.mainSample = decodeScalar(input.readByteArray());
                case 11 -> payload.sampleArray.add(decodeScalar(input.readByteArray()));
                case 12 -> payload.sampleList.add(decodeScalar(input.readByteArray()));
                case 13 -> payload.leafBuckets.add(decodeLeafList(input.readByteArray()));
                case 14 -> payload.labelBuckets.add(decodeStringSet(input.readByteArray()));
                case 15 -> payload.bucketArray.add(decodeNestedMap(input.readByteArray()));
                case 16 -> payload.stagedBuckets.add(decodeNestedMap(input.readByteArray()));
                case 17 -> {
                    Map.Entry<Integer, BenchmarkDtos.NestedBucketDto> entry = decodeNestedMapEntry(input.readByteArray());
                    payload.largeBuckets.put(entry.getKey(), entry.getValue());
                }
                case 18 -> payload.packed = decodePacked(input.readByteArray());
                case 19 -> payload.telemetry = decodeTelemetry(input.readByteArray());
                case 20 -> payload.primary = decodeNested(input.readByteArray());
                default -> input.skipField(tag);
            }
        }
        payload.counts = counts.stream().mapToInt(Integer::intValue).toArray();
        return payload;
    }

    private static byte[] encodeTelemetry(BenchmarkDtos.FixedTelemetryDto telemetry) throws Exception {
        return write(out -> {
            out.writeInt32(1, telemetry.id);
            out.writeInt64(2, telemetry.time);
            out.writeFloat(3, telemetry.ratio);
            out.writeDouble(4, telemetry.score);
            writeBytes(out, 5, encodePoint(telemetry.point));
        });
    }

    private static BenchmarkDtos.FixedTelemetryDto decodeTelemetry(byte[] bytes) throws Exception {
        CodedInputStream input = CodedInputStream.newInstance(bytes);
        BenchmarkDtos.FixedTelemetryDto telemetry = new BenchmarkDtos.FixedTelemetryDto();
        while (!input.isAtEnd()) {
            int tag = input.readTag();
            switch (WireFormat.getTagFieldNumber(tag)) {
                case 1 -> telemetry.id = input.readInt32();
                case 2 -> telemetry.time = input.readInt64();
                case 3 -> telemetry.ratio = input.readFloat();
                case 4 -> telemetry.score = input.readDouble();
                case 5 -> telemetry.point = decodePoint(input.readByteArray());
                default -> input.skipField(tag);
            }
        }
        return telemetry;
    }

    private static byte[] encodePoint(BenchmarkDtos.FixedPointDto point) throws Exception {
        return write(out -> {
            out.writeInt32(1, point.x);
            out.writeInt64(2, point.y);
            out.writeFloat(3, point.z);
            out.writeDouble(4, point.score);
        });
    }

    private static BenchmarkDtos.FixedPointDto decodePoint(byte[] bytes) throws Exception {
        CodedInputStream input = CodedInputStream.newInstance(bytes);
        BenchmarkDtos.FixedPointDto point = new BenchmarkDtos.FixedPointDto();
        while (!input.isAtEnd()) {
            int tag = input.readTag();
            switch (WireFormat.getTagFieldNumber(tag)) {
                case 1 -> point.x = input.readInt32();
                case 2 -> point.y = input.readInt64();
                case 3 -> point.z = input.readFloat();
                case 4 -> point.score = input.readDouble();
                default -> input.skipField(tag);
            }
        }
        return point;
    }

    private static byte[] encodeScalar(BenchmarkDtos.ScalarSampleDto sample) throws Exception {
        return write(out -> {
            out.writeInt32(1, sample.code);
            out.writeFloat(2, sample.f32);
            out.writeDouble(3, sample.f64);
            writeString(out, 4, sample.label);
        });
    }

    private static BenchmarkDtos.ScalarSampleDto decodeScalar(byte[] bytes) throws Exception {
        CodedInputStream input = CodedInputStream.newInstance(bytes);
        BenchmarkDtos.ScalarSampleDto sample = new BenchmarkDtos.ScalarSampleDto();
        while (!input.isAtEnd()) {
            int tag = input.readTag();
            switch (WireFormat.getTagFieldNumber(tag)) {
                case 1 -> sample.code = input.readInt32();
                case 2 -> sample.f32 = input.readFloat();
                case 3 -> sample.f64 = input.readDouble();
                case 4 -> sample.label = input.readString();
                default -> input.skipField(tag);
            }
        }
        return sample;
    }

    private static byte[] encodeLeaf(BenchmarkDtos.MetricLeafDto leaf) throws Exception {
        return write(out -> {
            out.writeInt64(1, leaf.id);
            out.writeEnum(2, leaf.kind.ordinal());
            out.writeFloat(3, leaf.ratio);
            out.writeDouble(4, leaf.score);
            writeString(out, 5, leaf.note);
            for (Integer tag : leaf.tags) {
                out.writeInt32(6, tag);
            }
        });
    }

    private static BenchmarkDtos.MetricLeafDto decodeLeaf(byte[] bytes) throws Exception {
        CodedInputStream input = CodedInputStream.newInstance(bytes);
        BenchmarkDtos.MetricLeafDto leaf = new BenchmarkDtos.MetricLeafDto();
        leaf.kind = BenchmarkDtos.MetricKindDto.UNKNOWN;
        while (!input.isAtEnd()) {
            int tag = input.readTag();
            switch (WireFormat.getTagFieldNumber(tag)) {
                case 1 -> leaf.id = input.readInt64();
                case 2 -> leaf.kind = BenchmarkDtos.MetricKindDto.values()[input.readEnum()];
                case 3 -> leaf.ratio = input.readFloat();
                case 4 -> leaf.score = input.readDouble();
                case 5 -> leaf.note = input.readString();
                case 6 -> leaf.tags.add(input.readInt32());
                default -> input.skipField(tag);
            }
        }
        return leaf;
    }

    private static byte[] encodeNested(BenchmarkDtos.NestedBucketDto bucket) throws Exception {
        return write(out -> {
            out.writeInt32(1, bucket.bucketId);
            writeBytes(out, 2, encodeScalar(bucket.sample));
            for (BenchmarkDtos.MetricLeafDto leaf : bucket.leafArray) {
                writeBytes(out, 3, encodeLeaf(leaf));
            }
            for (BenchmarkDtos.MetricLeafDto leaf : bucket.leafList) {
                writeBytes(out, 4, encodeLeaf(leaf));
            }
            for (Map.Entry<Integer, BenchmarkDtos.MetricLeafDto> entry : bucket.leafMap.entrySet()) {
                writeBytes(out, 5, encodeLeafMapEntry(entry.getKey(), entry.getValue()));
            }
            for (Map.Entry<Integer, List<Integer>> entry : bucket.adjacency.entrySet()) {
                writeBytes(out, 6, encodeIntListMapEntry(entry.getKey(), entry.getValue()));
            }
        });
    }

    private static BenchmarkDtos.NestedBucketDto decodeNested(byte[] bytes) throws Exception {
        CodedInputStream input = CodedInputStream.newInstance(bytes);
        BenchmarkDtos.NestedBucketDto bucket = new BenchmarkDtos.NestedBucketDto();
        while (!input.isAtEnd()) {
            int tag = input.readTag();
            switch (WireFormat.getTagFieldNumber(tag)) {
                case 1 -> bucket.bucketId = input.readInt32();
                case 2 -> bucket.sample = decodeScalar(input.readByteArray());
                case 3 -> bucket.leafArray.add(decodeLeaf(input.readByteArray()));
                case 4 -> bucket.leafList.add(decodeLeaf(input.readByteArray()));
                case 5 -> {
                    Map.Entry<Integer, BenchmarkDtos.MetricLeafDto> entry = decodeLeafMapEntry(input.readByteArray());
                    bucket.leafMap.put(entry.getKey(), entry.getValue());
                }
                case 6 -> {
                    Map.Entry<Integer, List<Integer>> entry = decodeIntListMapEntry(input.readByteArray());
                    bucket.adjacency.put(entry.getKey(), entry.getValue());
                }
                default -> input.skipField(tag);
            }
        }
        return bucket;
    }

    private static byte[] encodeBlob(BenchmarkDtos.ByteBlobDto blob) throws Exception {
        return write(out -> {
            out.writeInt64(1, blob.uid);
            out.writeByteArray(2, blob.raw);
            for (int value : blob.samples) {
                out.writeInt32(3, value);
            }
            for (long value : blob.ticks) {
                out.writeInt64(4, value);
            }
            for (float value : blob.ratios) {
                out.writeFloat(5, value);
            }
            for (double value : blob.scores) {
                out.writeDouble(6, value);
            }
        });
    }

    private static BenchmarkDtos.ByteBlobDto decodeBlob(byte[] bytes) throws Exception {
        CodedInputStream input = CodedInputStream.newInstance(bytes);
        BenchmarkDtos.ByteBlobDto blob = new BenchmarkDtos.ByteBlobDto();
        List<Integer> samples = new ArrayList<>();
        List<Long> ticks = new ArrayList<>();
        List<Float> ratios = new ArrayList<>();
        List<Double> scores = new ArrayList<>();
        while (!input.isAtEnd()) {
            int tag = input.readTag();
            switch (WireFormat.getTagFieldNumber(tag)) {
                case 1 -> blob.uid = input.readInt64();
                case 2 -> blob.raw = input.readByteArray();
                case 3 -> samples.add(input.readInt32());
                case 4 -> ticks.add(input.readInt64());
                case 5 -> ratios.add(input.readFloat());
                case 6 -> scores.add(input.readDouble());
                default -> input.skipField(tag);
            }
        }
        blob.samples = samples.stream().mapToInt(Integer::intValue).toArray();
        blob.ticks = ticks.stream().mapToLong(Long::longValue).toArray();
        blob.ratios = toFloatArray(ratios);
        blob.scores = scores.stream().mapToDouble(Double::doubleValue).toArray();
        return blob;
    }

    private static byte[] encodePacked(BenchmarkDtos.PackedCollectionsDto packed) throws Exception {
        return write(out -> {
            out.writeInt64(1, packed.uid);
            for (Integer value : packed.values) {
                out.writeInt32(2, value);
            }
            for (Long value : packed.ids) {
                out.writeInt64(3, value);
            }
            for (Map.Entry<Integer, Integer> entry : packed.scores.entrySet()) {
                writeBytes(out, 4, encodeIntIntEntry(entry.getKey(), entry.getValue()));
            }
            for (Map.Entry<Integer, Long> entry : packed.versions.entrySet()) {
                writeBytes(out, 5, encodeIntLongEntry(entry.getKey(), entry.getValue()));
            }
        });
    }

    private static BenchmarkDtos.PackedCollectionsDto decodePacked(byte[] bytes) throws Exception {
        CodedInputStream input = CodedInputStream.newInstance(bytes);
        BenchmarkDtos.PackedCollectionsDto packed = new BenchmarkDtos.PackedCollectionsDto();
        while (!input.isAtEnd()) {
            int tag = input.readTag();
            switch (WireFormat.getTagFieldNumber(tag)) {
                case 1 -> packed.uid = input.readInt64();
                case 2 -> packed.values.add(input.readInt32());
                case 3 -> packed.ids.add(input.readInt64());
                case 4 -> {
                    int[] entry = decodeIntIntEntry(input.readByteArray());
                    packed.scores.put(entry[0], entry[1]);
                }
                case 5 -> {
                    LongEntry entry = decodeIntLongEntry(input.readByteArray());
                    packed.versions.put(entry.key, entry.value);
                }
                default -> input.skipField(tag);
            }
        }
        return packed;
    }

    private static byte[] encodeLeafList(List<BenchmarkDtos.MetricLeafDto> leaves) throws Exception {
        return write(out -> {
            for (BenchmarkDtos.MetricLeafDto leaf : leaves) {
                writeBytes(out, 1, encodeLeaf(leaf));
            }
        });
    }

    private static List<BenchmarkDtos.MetricLeafDto> decodeLeafList(byte[] bytes) throws Exception {
        CodedInputStream input = CodedInputStream.newInstance(bytes);
        List<BenchmarkDtos.MetricLeafDto> leaves = new ArrayList<>();
        while (!input.isAtEnd()) {
            int tag = input.readTag();
            if (WireFormat.getTagFieldNumber(tag) == 1) {
                leaves.add(decodeLeaf(input.readByteArray()));
            } else {
                input.skipField(tag);
            }
        }
        return leaves;
    }

    private static byte[] encodeStringSet(java.util.Set<String> values) throws Exception {
        return write(out -> {
            for (String value : values) {
                writeString(out, 1, value);
            }
        });
    }

    private static java.util.Set<String> decodeStringSet(byte[] bytes) throws Exception {
        CodedInputStream input = CodedInputStream.newInstance(bytes);
        java.util.Set<String> values = new LinkedHashSet<>();
        while (!input.isAtEnd()) {
            int tag = input.readTag();
            if (WireFormat.getTagFieldNumber(tag) == 1) {
                values.add(input.readString());
            } else {
                input.skipField(tag);
            }
        }
        return values;
    }

    private static byte[] encodeNestedMap(Map<Integer, BenchmarkDtos.NestedBucketDto> map) throws Exception {
        return write(out -> {
            for (Map.Entry<Integer, BenchmarkDtos.NestedBucketDto> entry : map.entrySet()) {
                writeBytes(out, 1, encodeNestedMapEntry(entry.getKey(), entry.getValue()));
            }
        });
    }

    private static Map<Integer, BenchmarkDtos.NestedBucketDto> decodeNestedMap(byte[] bytes) throws Exception {
        CodedInputStream input = CodedInputStream.newInstance(bytes);
        Map<Integer, BenchmarkDtos.NestedBucketDto> map = new LinkedHashMap<>();
        while (!input.isAtEnd()) {
            int tag = input.readTag();
            if (WireFormat.getTagFieldNumber(tag) == 1) {
                Map.Entry<Integer, BenchmarkDtos.NestedBucketDto> entry = decodeNestedMapEntry(input.readByteArray());
                map.put(entry.getKey(), entry.getValue());
            } else {
                input.skipField(tag);
            }
        }
        return map;
    }

    private static byte[] encodeNestedMapEntry(int key, BenchmarkDtos.NestedBucketDto value) throws Exception {
        return write(out -> {
            out.writeInt32(1, key);
            writeBytes(out, 2, encodeNested(value));
        });
    }

    private static Map.Entry<Integer, BenchmarkDtos.NestedBucketDto> decodeNestedMapEntry(byte[] bytes) throws Exception {
        CodedInputStream input = CodedInputStream.newInstance(bytes);
        int key = 0;
        BenchmarkDtos.NestedBucketDto value = null;
        while (!input.isAtEnd()) {
            int tag = input.readTag();
            switch (WireFormat.getTagFieldNumber(tag)) {
                case 1 -> key = input.readInt32();
                case 2 -> value = decodeNested(input.readByteArray());
                default -> input.skipField(tag);
            }
        }
        return Map.entry(key, value);
    }

    private static byte[] encodeLeafMapEntry(int key, BenchmarkDtos.MetricLeafDto value) throws Exception {
        return write(out -> {
            out.writeInt32(1, key);
            writeBytes(out, 2, encodeLeaf(value));
        });
    }

    private static Map.Entry<Integer, BenchmarkDtos.MetricLeafDto> decodeLeafMapEntry(byte[] bytes) throws Exception {
        CodedInputStream input = CodedInputStream.newInstance(bytes);
        int key = 0;
        BenchmarkDtos.MetricLeafDto value = null;
        while (!input.isAtEnd()) {
            int tag = input.readTag();
            switch (WireFormat.getTagFieldNumber(tag)) {
                case 1 -> key = input.readInt32();
                case 2 -> value = decodeLeaf(input.readByteArray());
                default -> input.skipField(tag);
            }
        }
        return Map.entry(key, value);
    }

    private static byte[] encodeIntListMapEntry(int key, List<Integer> values) throws Exception {
        return write(out -> {
            out.writeInt32(1, key);
            for (Integer value : values) {
                out.writeInt32(2, value);
            }
        });
    }

    private static Map.Entry<Integer, List<Integer>> decodeIntListMapEntry(byte[] bytes) throws Exception {
        CodedInputStream input = CodedInputStream.newInstance(bytes);
        int key = 0;
        List<Integer> values = new ArrayList<>();
        while (!input.isAtEnd()) {
            int tag = input.readTag();
            switch (WireFormat.getTagFieldNumber(tag)) {
                case 1 -> key = input.readInt32();
                case 2 -> values.add(input.readInt32());
                default -> input.skipField(tag);
            }
        }
        return Map.entry(key, values);
    }

    private static byte[] encodeIntIntEntry(int key, int value) throws Exception {
        return write(out -> {
            out.writeInt32(1, key);
            out.writeInt32(2, value);
        });
    }

    private static int[] decodeIntIntEntry(byte[] bytes) throws Exception {
        CodedInputStream input = CodedInputStream.newInstance(bytes);
        int key = 0;
        int value = 0;
        while (!input.isAtEnd()) {
            int tag = input.readTag();
            switch (WireFormat.getTagFieldNumber(tag)) {
                case 1 -> key = input.readInt32();
                case 2 -> value = input.readInt32();
                default -> input.skipField(tag);
            }
        }
        return new int[]{key, value};
    }

    private static byte[] encodeIntLongEntry(int key, long value) throws Exception {
        return write(out -> {
            out.writeInt32(1, key);
            out.writeInt64(2, value);
        });
    }

    private static LongEntry decodeIntLongEntry(byte[] bytes) throws Exception {
        CodedInputStream input = CodedInputStream.newInstance(bytes);
        LongEntry entry = new LongEntry();
        while (!input.isAtEnd()) {
            int tag = input.readTag();
            switch (WireFormat.getTagFieldNumber(tag)) {
                case 1 -> entry.key = input.readInt32();
                case 2 -> entry.value = input.readInt64();
                default -> input.skipField(tag);
            }
        }
        return entry;
    }

    private static void writeString(CodedOutputStream out, int fieldNumber, String value) throws Exception {
        if (value != null && !value.isEmpty()) {
            out.writeString(fieldNumber, value);
        }
    }

    private static void writeBytes(CodedOutputStream out, int fieldNumber, byte[] bytes) throws Exception {
        if (bytes != null) {
            out.writeByteArray(fieldNumber, bytes);
        }
    }

    private static byte[] write(ProtoWriter writer) throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream(4096);
        CodedOutputStream out = CodedOutputStream.newInstance(bytes);
        writer.write(out);
        out.flush();
        return bytes.toByteArray();
    }

    private static float[] toFloatArray(List<Float> values) {
        float[] result = new float[values.size()];
        for (int i = 0; i < values.size(); i++) {
            result[i] = values.get(i);
        }
        return result;
    }

    private interface ProtoWriter {
        void write(CodedOutputStream out) throws Exception;
    }

    private static final class LongEntry {
        int key;
        long value;
    }
}
