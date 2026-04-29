package com.zero.codegen.runtime.bytes;

public final class FloatArrayView {
    private static final FloatArrayView EMPTY = new FloatArrayView(new float[0], 0, null, 0, null);

    private final float[] materialized;
    private final byte[] array;
    private final int offset;
    private final int count;
    private final Object owner;

    private FloatArrayView(float[] materialized, int count, byte[] array, int offset, Object owner) {
        this.materialized = materialized;
        this.array = array;
        this.offset = offset;
        this.count = count;
        this.owner = owner;
    }

    public static FloatArrayView empty() {
        return EMPTY;
    }

    public static FloatArrayView wrap(byte[] array, int offset, int count) {
        return wrap(array, offset, count, null);
    }

    public static FloatArrayView wrap(byte[] array, int offset, int count, Object owner) {
        if(count == 0){
            return EMPTY;
        }
        if(!ByteIO.isUnsafeBorrowEnabled()){
            float[] values=new float[count];
            for(int i=0;i<count;i++){
                int base=offset + (i<<2);
                int bits=((array[base] & 0xFF) << 24)
                        | ((array[base + 1] & 0xFF) << 16)
                        | ((array[base + 2] & 0xFF) << 8)
                        | (array[base + 3] & 0xFF);
                values[i]=Float.intBitsToFloat(bits);
            }
            return of(values);
        }
        return new FloatArrayView(null, count, array, offset, owner);
    }

    public static FloatArrayView of(float[] values) {
        if(values == null || values.length == 0){
            return EMPTY;
        }
        return new FloatArrayView(values, values.length, null, 0, null);
    }

    public int count() {
        return count;
    }

    float[] materializedArray() {
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

    public float getFloat(int index) {
        if(index < 0 || index >= count){
            throw new IndexOutOfBoundsException("index=" + index + ", count=" + count);
        }
        if(materialized != null){
            return materialized[index];
        }
        int base = offset + (index << 2);
        int bits = ((array[base] & 0xFF) << 24)
                | ((array[base + 1] & 0xFF) << 16)
                | ((array[base + 2] & 0xFF) << 8)
                | (array[base + 3] & 0xFF);
        return Float.intBitsToFloat(bits);
    }

    public float[] toFloatArray() {
        float[] out = new float[count];
        for(int i = 0; i < count; i++){
            out[i] = getFloat(i);
        }
        return out;
    }
}
