package com.zero.codegen.runtime.bytes;

public final class LongArrayView {
    private static final LongArrayView EMPTY = new LongArrayView(new long[0], 0, null, 0, null);

    private final long[] materialized;
    private final byte[] array;
    private final int offset;
    private final int count;
    private final Object owner;

    private LongArrayView(long[] materialized, int count, byte[] array, int offset, Object owner) {
        this.materialized = materialized;
        this.array = array;
        this.offset = offset;
        this.count = count;
        this.owner = owner;
    }

    public static LongArrayView empty() {
        return EMPTY;
    }

    public static LongArrayView wrap(byte[] array, int offset, int count) {
        return wrap(array, offset, count, null);
    }

    public static LongArrayView wrap(byte[] array, int offset, int count, Object owner) {
        if(count == 0){
            return EMPTY;
        }
        if(!ByteIO.isUnsafeBorrowEnabled()){
            long[] values=new long[count];
            for(int i=0;i<count;i++){
                int base=offset + (i<<3);
                values[i]=((long)(array[base] & 0xFF) << 56)
                        | ((long)(array[base + 1] & 0xFF) << 48)
                        | ((long)(array[base + 2] & 0xFF) << 40)
                        | ((long)(array[base + 3] & 0xFF) << 32)
                        | ((long)(array[base + 4] & 0xFF) << 24)
                        | ((long)(array[base + 5] & 0xFF) << 16)
                        | ((long)(array[base + 6] & 0xFF) << 8)
                        | ((long)(array[base + 7] & 0xFF));
            }
            return of(values);
        }
        return new LongArrayView(null, count, array, offset, owner);
    }

    public static LongArrayView of(long[] values) {
        if(values == null || values.length == 0){
            return EMPTY;
        }
        return new LongArrayView(values, values.length, null, 0, null);
    }

    public int count() {
        return count;
    }

    long[] materializedArray() {
        return materialized;
    }

    byte[] borrowedArray() {
        return array;
    }

    int borrowedOffset() {
        return offset;
    }

    public Object owner() {
        return owner;
    }

    public long getLong(int index) {
        if(index < 0 || index >= count){
            throw new IndexOutOfBoundsException("index=" + index + ", count=" + count);
        }
        if(materialized != null){
            return materialized[index];
        }
        int base = offset + (index << 3);
        return ((long)(array[base] & 0xFF) << 56)
                | ((long)(array[base + 1] & 0xFF) << 48)
                | ((long)(array[base + 2] & 0xFF) << 40)
                | ((long)(array[base + 3] & 0xFF) << 32)
                | ((long)(array[base + 4] & 0xFF) << 24)
                | ((long)(array[base + 5] & 0xFF) << 16)
                | ((long)(array[base + 6] & 0xFF) << 8)
                | ((long)(array[base + 7] & 0xFF));
    }

    public long[] toLongArray() {
        long[] out = new long[count];
        for(int i = 0; i < count; i++){
            out[i] = getLong(i);
        }
        return out;
    }
}
