package com.zero.codegen.runtime.bytes;

import java.nio.charset.StandardCharsets;

public final class BorrowedString {
    private static final BorrowedString EMPTY = new BorrowedString(BorrowedBytes.empty(), 0, "");

    private final BorrowedBytes bytes;
    private final int byteLength;
    private String value;

    private BorrowedString(BorrowedBytes bytes, int byteLength, String value) {
        this.bytes = bytes;
        this.byteLength = byteLength;
        this.value = value;
    }

    public static BorrowedString empty() {
        return EMPTY;
    }

    public static BorrowedString wrap(byte[] array, int offset, int length) {
        return wrap(array, offset, length, null);
    }

    public static BorrowedString wrap(byte[] array, int offset, int length, Object owner) {
        if(array == null || length == 0){
            return EMPTY;
        }
        return new BorrowedString(BorrowedBytes.wrap(array, offset, length, owner), length, null);
    }

    public static BorrowedString of(String value) {
        if(value == null || value.isEmpty()){
            return EMPTY;
        }
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        return new BorrowedString(BorrowedBytes.copyOf(bytes), bytes.length, value);
    }

    public int byteLength() {
        return byteLength;
    }

    public boolean isEmpty() {
        return byteLength == 0;
    }

    public BorrowedBytes bytes() {
        return bytes;
    }

    public Object owner() {
        return bytes.owner();
    }

    @Override
    public String toString() {
        if(value == null){
            value = new String(bytes.unsafeArray(), bytes.unsafeOffset(), byteLength, StandardCharsets.UTF_8);
        }
        return value;
    }
}
