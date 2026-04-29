package com.zero.codegen.runtime.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 * Packet 的 payload 抽象。
 *
 * 设计目标：
 * 1. 兼容传统 byte[] 负载；
 * 2. 允许借用线性 buffer，写入 Netty outbound ByteBuf 后立刻归还对象池；
 * 3. 保留一个 materialize 入口，供少量必须拿到独立 byte[] 的旧代码兼容使用。
 */
public interface PacketPayload extends AutoCloseable {
    int length();

    void writeTo(ByteBuf out);

    default ByteBuf asByteBuf() {
        byte[] bytes = materialize();
        return bytes == null || bytes.length == 0 ? Unpooled.EMPTY_BUFFER : Unpooled.wrappedBuffer(bytes);
    }

    byte[] materialize();

    @Override
    void close();
}

