package com.zero.codegen.runtime.netty;

import com.zero.codegen.runtime.bytes.LinearBufferPool;
import com.zero.codegen.runtime.bytes.LinearByteBuffer;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.util.Arrays;

/**
 * 借用 LinearByteBuffer 作为一次性发送负载。
 *
 * 注意：
 * 1. 该对象只能用于单次发送；
 * 2. 写入 Netty outbound ByteBuf 后就会归还底层 LinearByteBuffer；
 * 3. 如果旧代码显式调用 materialize()，这里会退化成一次拷贝，但不会影响正常发送快路径。
 */
public final class LinearBufferPacketPayload implements PacketPayload {
    private LinearByteBuffer buffer;
    private final int offset;
    private final int length;

    private LinearBufferPacketPayload(LinearByteBuffer buffer) {
        this.buffer = buffer;
        this.offset = buffer.readerIndex();
        this.length = buffer.readableBytes();
    }

    public static LinearBufferPacketPayload wrap(LinearByteBuffer buffer) {
        if (buffer == null) {
            throw new NullPointerException("buffer can not be null");
        }
        return new LinearBufferPacketPayload(buffer);
    }

    @Override
    public int length() {
        return length;
    }

    @Override
    public void writeTo(ByteBuf out) {
        LinearByteBuffer current = buffer;
        if (current == null) {
            throw new IllegalStateException("payload already released");
        }
        if (length > 0) {
            out.writeBytes(current.array(), offset, length);
        }
    }

    @Override
    public ByteBuf asByteBuf() {
        LinearByteBuffer current = buffer;
        if (current == null) {
            throw new IllegalStateException("payload already released");
        }
        return length == 0 ? Unpooled.EMPTY_BUFFER : Unpooled.wrappedBuffer(current.array(), offset, length);
    }

    @Override
    public byte[] materialize() {
        LinearByteBuffer current = buffer;
        if (current == null) {
            throw new IllegalStateException("payload already released");
        }
        return Arrays.copyOfRange(current.array(), offset, offset + length);
    }

    @Override
    public void close() {
        LinearByteBuffer current = buffer;
        if (current == null) {
            return;
        }
        buffer = null;
        LinearBufferPool.release(current);
    }
}

