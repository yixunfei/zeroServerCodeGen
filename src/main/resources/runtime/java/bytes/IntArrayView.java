package com.zero.codegen.runtime.bytes;

public final class IntArrayView {
    private static final IntArrayView EMPTY = new IntArrayView(new int[0], 0, null, 0, null, 0);

    private final int[] materialized;
    private final byte[] array;
    private final int offset;
    private final int count;
    private final Object owner;

    private IntArrayView(int[] materialized, int count, byte[] array, int offset, Object owner, int ignored) {
        this.materialized = materialized;
        this.array = array;
        this.offset = offset;
        this.count = count;
        this.owner = owner;
    }

    public static IntArrayView empty() {
        return EMPTY;
    }

    public static IntArrayView wrap(byte[] array, int offset, int count) {
        return wrap(array, offset, count, null);
    }

    public static IntArrayView wrap(byte[] array, int offset, int count, Object owner) {
        if(count == 0){
            return EMPTY;
        }
        if(!ByteIO.isUnsafeBorrowEnabled()){
            int[] values=new int[count];
            for(int i=0;i<count;i++){
                int base=offset + (i<<2);
                values[i]=((array[base] & 0xFF) << 24)
                        | ((array[base + 1] & 0xFF) << 16)
                        | ((array[base + 2] & 0xFF) << 8)
                        | (array[base + 3] & 0xFF);
            }
            return of(values);
        }
        return new IntArrayView(null, count, array, offset, owner, 0);
    }

    public static IntArrayView of(int[] values) {
        if(values == null || values.length == 0){
            return EMPTY;
        }
        return new IntArrayView(values, values.length, null, 0, null, 0);
    }

    public int count() {
        return count;
    }

    int[] materializedArray() {
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

    public int getInt(int index) {
        if(index < 0 || index >= count){
            throw new IndexOutOfBoundsException("index=" + index + ", count=" + count);
        }
        if(materialized != null){
            return materialized[index];
        }
        int base = offset + (index << 2);
        return ((array[base] & 0xFF) << 24)
                | ((array[base + 1] & 0xFF) << 16)
                | ((array[base + 2] & 0xFF) << 8)
                | (array[base + 3] & 0xFF);
    }

    public int[] toIntArray() {
        int[] out = new int[count];
        for(int i = 0; i < count; i++){
            out[i] = getInt(i);
        }
        return out;
    }
}
