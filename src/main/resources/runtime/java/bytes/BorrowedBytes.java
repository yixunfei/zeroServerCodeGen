package com.zero.codegen.runtime.bytes;

import java.nio.ByteBuffer;
import java.util.Arrays;

public final class BorrowedBytes {
    private static final BorrowedBytes EMPTY = new BorrowedBytes(new byte[0], 0, 0, null, null);

    private final byte[] array;
    private final int offset;
    private final int length;
    private final Object owner;
    private byte[] materialized;

    private BorrowedBytes(byte[] array, int offset, int length, byte[] materialized, Object owner) {
        this.array = array;
        this.offset = offset;
        this.length = length;
        this.materialized = materialized;
        this.owner = owner;
    }

    public static BorrowedBytes empty() {
        return EMPTY;
    }

    public static BorrowedBytes wrap(byte[] array, int offset, int length) {
        return wrap(array, offset, length, null);
    }

    public static BorrowedBytes wrap(byte[] array, int offset, int length, Object owner) {
        if(array == null || length == 0){
            return EMPTY;
        }
        if(!ByteIO.isUnsafeBorrowEnabled()){
            return copyOf(Arrays.copyOfRange(array, offset, offset + length));
        }
        return new BorrowedBytes(array, offset, length, null, owner);
    }

    public static BorrowedBytes copyOf(byte[] bytes) {
        if(bytes == null || bytes.length == 0){
            return EMPTY;
        }
        return new BorrowedBytes(bytes, 0, bytes.length, bytes, null);
    }

    public int length() {
        return length;
    }

    public boolean isEmpty() {
        return length == 0;
    }

    public byte byteAt(int index) {
        if(index < 0 || index >= length){
            throw new IndexOutOfBoundsException("index=" + index + ", length=" + length);
        }
        return array[offset + index];
    }

    public ByteBuffer asByteBuffer() {
        return ByteBuffer.wrap(array, offset, length).slice();
    }

    public byte[] toByteArray() {
        if(length == 0){
            return new byte[0];
        }
        if(materialized == null || materialized.length != length){
            materialized = Arrays.copyOfRange(array, offset, offset + length);
        }
        return Arrays.copyOf(materialized, materialized.length);
    }

    public byte[] unsafeArray() {
        return array;
    }

    public int unsafeOffset() {
        return offset;
    }

    public Object owner() {
        return owner;
    }
}
