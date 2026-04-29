package com.zero.codegen.runtime.serialize;

import io.netty.buffer.ByteBuf;

public interface IProto {
    void writeTo(ByteBuf buf);
}

