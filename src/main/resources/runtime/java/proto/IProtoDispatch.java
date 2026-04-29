package com.zero.codegen.runtime.proto;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;

public interface IProtoDispatch {
    void dispatch(Channel channel, int id, ByteBuf payload);

    default void dispatch(Channel channel, int id, byte[] payload){
        dispatch(channel, id, payload == null || payload.length == 0 ? Unpooled.EMPTY_BUFFER : Unpooled.wrappedBuffer(payload));
    }
}
