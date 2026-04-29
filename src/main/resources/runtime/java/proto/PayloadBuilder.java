package com.zero.codegen.runtime.proto;

import com.zero.codegen.runtime.bytes.ByteCursor;
import com.zero.codegen.runtime.bytes.LinearBufferPool;
import com.zero.codegen.runtime.bytes.LinearByteBuffer;
import com.zero.codegen.runtime.netty.LinearBufferPacketPayload;
import com.zero.codegen.runtime.netty.Packet;
import com.zero.codegen.runtime.netty.PacketPayload;
import com.zero.codegen.runtime.serialize.ICursorProto;
import io.netty.channel.Channel;

import java.util.function.Consumer;

public final class PayloadBuilder {
    private PayloadBuilder(){}

    /**
     * 构建一个独立 payload 字节数组。
     *
     * 适合离线比对、测试或需要立即拿到稳定 `byte[]` 的场景。
     * 这里会从 `LinearBufferPool` 借一个线性 buffer，写完后再归还。
     */
    public static byte[] build(Consumer<ByteCursor> writer){
        try(PacketPayload payload = buildPayloadSized(-1, writer)){
            return payload.materialize();
        }
    }

    public static byte[] buildProto(ICursorProto proto){
        if(proto==null){
            throw new NullPointerException("proto can not be null");
        }
        try(PacketPayload payload = buildPayloadProto(proto)){
            return payload.materialize();
        }
    }

    public static byte[] buildSized(int expectedBytes, Consumer<ByteCursor> writer){
        try(PacketPayload payload = buildPayloadSized(expectedBytes, writer)){
            return payload.materialize();
        }
    }

    public static PacketPayload buildPayload(Consumer<ByteCursor> writer){
        return buildPayloadSized(-1, writer);
    }

    public static PacketPayload buildView(Consumer<ByteCursor> writer){
        return buildPayloadSized(-1, writer);
    }

    public static PacketPayload buildPayloadProto(ICursorProto proto){
        if(proto==null){
            throw new NullPointerException("proto can not be null");
        }
        return buildPayloadSized(proto.estimatedSize(), proto::writeTo);
    }

    public static PacketPayload buildView(ICursorProto proto){
        return buildPayloadProto(proto);
    }

    public static PacketPayload buildPayloadSized(int expectedBytes, Consumer<ByteCursor> writer){
        LinearByteBuffer buffer= expectedBytes>0 ? LinearBufferPool.acquire(expectedBytes) : LinearBufferPool.acquire();
        boolean released=false;
        try{
            writer.accept(buffer);
            PacketPayload payload = LinearBufferPacketPayload.wrap(buffer);
            released=true;
            return payload;
        }finally {
            if(!released){
                LinearBufferPool.release(buffer);
            }
        }
    }

    /**
     * 直接发送协议包。
     *
     * 关键点：
     * 1. 仍然先写入独立线性 buffer，而不是在业务侧直接操作 Netty ByteBuf。
     * 2. 发送阶段通过 `LinearBufferPacketPayload` 包装已有内容，减少中间对象和额外复制。
     * 3. 如果 channel 不可用，直接返回，不做无意义构包。
     */
    public static void send(Channel ch, int id, Consumer<ByteCursor> writer){
        sendSized(ch, id, -1, writer);
    }

    public static void sendProto(Channel ch, int id, ICursorProto proto){
        if(proto==null){
            throw new NullPointerException("proto can not be null");
        }
        sendSized(ch, id, proto.estimatedSize(), proto::writeTo);
    }

    public static void sendSized(Channel ch, int id, int expectedBytes, Consumer<ByteCursor> writer){
        if(ch==null || !ch.isActive()){
            return;
        }
        LinearByteBuffer buffer= expectedBytes>0 ? LinearBufferPool.acquire(expectedBytes) : LinearBufferPool.acquire();
        try{
            writer.accept(buffer);
            ch.writeAndFlush(new Packet(id, LinearBufferPacketPayload.wrap(buffer)));
            buffer=null;
        }finally {
            if(buffer!=null){
                LinearBufferPool.release(buffer);
            }
        }
    }

    public static void writeDirect(Channel ch, int id, Consumer<ByteCursor> writer){
        send(ch,id,writer);
    }

    public static void writeDirectProto(Channel ch, int id, ICursorProto proto){
        sendProto(ch,id,proto);
    }
}

