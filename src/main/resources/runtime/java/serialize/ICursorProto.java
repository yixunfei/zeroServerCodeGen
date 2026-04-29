package com.zero.codegen.runtime.serialize;

import com.zero.codegen.runtime.bytes.ByteCursor;
import com.zero.codegen.runtime.bytes.NettyCursor;
import io.netty.buffer.ByteBuf;

/**
 * 同时暴露 ByteBuf 兼容接口与 ByteCursor 快路径的协议对象。
 * 老调用方仍然可以直接写 ByteBuf；新热路径则可以直接写线性 buffer，
 * 避免在协议层和 Netty Buffer 之间来回适配。
 */
public interface ICursorProto extends IProto {
    void writeTo(ByteCursor cursor);

    default int estimatedSize(){
        return -1;
    }

    @Override
    default void writeTo(ByteBuf buf){
        writeTo(new NettyCursor(buf));
    }
}

