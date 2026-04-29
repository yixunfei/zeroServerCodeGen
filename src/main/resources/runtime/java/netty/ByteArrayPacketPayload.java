package com.zero.codegen.runtime.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.util.Arrays;

final class ByteArrayPacketPayload implements PacketPayload {
    private final byte[] array;
    private final int offset;
    private final int length;

    ByteArrayPacketPayload(byte[] array) {
        this(array, 0, array == null ? 0 : array.length);
    }

    ByteArrayPacketPayload(byte[] array, int offset, int length) {
        this.array = array == null ? new byte[0] : array;
        this.offset = offset;
        this.length = length;
    }

    @Override
    public int length() {
        return length;
    }

    @Override
    public void writeTo(ByteBuf out) {
        if (length > 0) {
            out.writeBytes(array, offset, length);
        }
    }

    @Override
    public ByteBuf asByteBuf() {
        return length == 0 ? Unpooled.EMPTY_BUFFER : Unpooled.wrappedBuffer(array, offset, length);
    }

    @Override
    public byte[] materialize() {
        if (offset == 0 && length == array.length) {
            return array;
        }
        return Arrays.copyOfRange(array, offset, offset + length);
    }

    @Override
    public void close() {
        // byte[] 由 GC 管理，这里不需要回收动作
    }
}

