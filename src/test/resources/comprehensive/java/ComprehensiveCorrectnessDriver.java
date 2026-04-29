package com.zero.codegen.comprehensive;

import java.util.Arrays;
import java.util.Map;

public final class ComprehensiveCorrectnessDriver {
    private ComprehensiveCorrectnessDriver() {
    }

    public static void main(String[] args) {
        System.out.print(runChecks());
    }

    public static String runChecks() {
        ComprehensivePayload sparse = ComprehensiveFixtures.buildSparseDefaults();
        ComprehensivePayload dense = ComprehensiveFixtures.buildComplexDense();
        ComprehensivePayload shifted = ComprehensiveFixtures.buildShiftedDense(ComprehensiveFixtures.LARGE_MAP_SIZE);

        byte[] denseCursorBytes = ComprehensiveFixtures.encodeCursor(dense);
        byte[] denseByteBufBytes = ComprehensiveFixtures.encodeByteBuf(dense);
        byte[] sparseCursorBytes = ComprehensiveFixtures.encodeCursor(sparse);

        ComprehensivePayload denseCursorDecoded = ComprehensiveFixtures.decodeCursor(denseCursorBytes);
        ComprehensivePayload denseByteBufDecoded = ComprehensiveFixtures.decodeByteBuf(denseByteBufBytes);
        ComprehensivePayload sparseCursorDecoded = ComprehensiveFixtures.decodeCursor(sparseCursorBytes);

        ComprehensivePayload denseDirtyTarget = ComprehensiveFixtures.buildDirtyPayload();
        Map<Integer, NestedBucket> denseMapRef = denseDirtyTarget.getLargeBuckets();
        ComprehensiveFixtures.readIntoCursor(denseCursorBytes, denseDirtyTarget);

        ComprehensivePayload denseSmallMapTarget = ComprehensiveFixtures.buildComplexDense();
        Map<Integer, NestedBucket> denseSmallMapRef = denseSmallMapTarget.getBucketArray()[0];
        NestedBucket denseKey1Ref = denseSmallMapRef.get(1);
        ComprehensiveFixtures.readIntoCursor(denseCursorBytes, denseSmallMapTarget);

        ComprehensivePayload shiftedTarget = ComprehensiveFixtures.buildComplexDense();
        Map<Integer, NestedBucket> shiftedMapRef = shiftedTarget.getLargeBuckets();
        NestedBucket removedKey2Ref = shiftedMapRef.get(2);
        Map<Integer, NestedBucket> shiftedSmallMapRef = shiftedTarget.getBucketArray()[0];
        NestedBucket shiftedKey1Ref = shiftedSmallMapRef.get(1);
        ComprehensiveFixtures.readIntoCursor(ComprehensiveFixtures.encodeCursor(shifted), shiftedTarget);

        ComprehensivePayload sparseDirtyTarget = ComprehensiveFixtures.buildDirtyPayload();
        Map<Integer, NestedBucket> sparseMapRef = sparseDirtyTarget.getLargeBuckets();
        ComprehensiveFixtures.readIntoCursor(sparseCursorBytes, sparseDirtyTarget);

        String denseSignature = ComprehensiveFixtures.signature(dense);
        String sparseSignature = ComprehensiveFixtures.signature(sparse);

        StringBuilder sb = new StringBuilder();
        append(sb, "denseByteParity", Arrays.equals(denseCursorBytes, denseByteBufBytes));
        append(sb, "denseCursorRoundTrip", denseSignature.equals(ComprehensiveFixtures.signature(denseCursorDecoded)));
        append(sb, "denseByteBufRoundTrip", denseSignature.equals(ComprehensiveFixtures.signature(denseByteBufDecoded)));
        append(sb, "denseReadIntoRoundTrip", denseSignature.equals(ComprehensiveFixtures.signature(denseDirtyTarget)));
        append(sb, "denseReadIntoMapReuse", denseDirtyTarget.getLargeBuckets() == denseMapRef);
        append(sb, "denseReadIntoKey1Reuse", denseSmallMapTarget.getBucketArray()[0] == denseSmallMapRef
                && denseSmallMapTarget.getBucketArray()[0].get(1) == denseKey1Ref);
        append(sb, "shiftedReadIntoMapReuse", shiftedTarget.getLargeBuckets() == shiftedMapRef);
        append(sb, "shiftedReadIntoKey1Reuse", shiftedTarget.getBucketArray()[0] == shiftedSmallMapRef
                && shiftedTarget.getBucketArray()[0].get(1) == shiftedKey1Ref);
        append(sb, "shiftedRemovedKeyGone", !shiftedTarget.getLargeBuckets().containsKey(2));
        append(sb, "shiftedNewKeyAllocated", shiftedTarget.getLargeBuckets().get(356) != null
                && shiftedTarget.getLargeBuckets().get(356) != removedKey2Ref);
        append(sb, "sparseCursorRoundTrip", sparseSignature.equals(ComprehensiveFixtures.signature(sparseCursorDecoded)));
        append(sb, "sparseReadIntoDefaultReset", sparseSignature.equals(ComprehensiveFixtures.signature(sparseDirtyTarget)));
        append(sb, "sparseMapReuse", sparseDirtyTarget.getLargeBuckets() == sparseMapRef
                && sparseDirtyTarget.getLargeBuckets().isEmpty());
        appendValue(sb, "denseBytesSha256", ComprehensiveFixtures.sha256Base64(denseCursorBytes));
        appendValue(sb, "sparseBytesSha256", ComprehensiveFixtures.sha256Base64(sparseCursorBytes));
        appendValue(sb, "densePayloadBytes", Integer.toString(denseCursorBytes.length));
        appendValue(sb, "sparsePayloadBytes", Integer.toString(sparseCursorBytes.length));
        appendValue(sb, "largeMapSize", Integer.toString(dense.getLargeBuckets().size()));
        return sb.toString();
    }

    private static void append(StringBuilder sb, String key, boolean value) {
        appendValue(sb, key, Boolean.toString(value));
    }

    private static void appendValue(StringBuilder sb, String key, String value) {
        sb.append(key).append('=').append(value).append('\n');
    }
}
