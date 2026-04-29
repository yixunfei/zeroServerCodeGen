package com.zero.codegen.runtime.bytes;

import io.netty.buffer.ByteBuf;

public class NettyCursor implements ByteCursor {
    private static final ThreadLocal<NettyCursor> LOCAL = ThreadLocal.withInitial(NettyCursor::new);

    private ByteBuf buf;

    private NettyCursor(){
    }

    public NettyCursor(ByteBuf buf){
        reset(buf);
    }

    public static NettyCursor threadLocal(ByteBuf buf){
        return LOCAL.get().reset(buf);
    }

    public NettyCursor reset(ByteBuf buf){
        if(buf==null){
            throw new NullPointerException("ByteBuf can not be null");
        }
        this.buf=buf;
        return this;
    }

    @Override
    public void writeByte(byte v) { buf.writeByte(v); }
    @Override
    public void writeBytes(byte[] b, int off, int len) { buf.writeBytes(b, off, len); }
    @Override
    public byte readByte() { return buf.readByte(); }
    @Override
    public void readBytes(byte[] b, int off, int len) { buf.readBytes(b, off, len); }
    @Override
    public int readableBytes() { return buf.readableBytes(); }
    @Override
    public int writableBytes() { return buf.writableBytes(); }
    @Override
    public byte[] array() {
        if(!buf.hasArray()){
            throw new UnsupportedOperationException("underlying ByteBuf is not heap-backed");
        }
        return buf.array();
    }
    @Override
    public int readerIndex() { return buf.readerIndex(); }
    @Override
    public void skip(int length) { buf.skipBytes(length); }
    public ByteBuf unwrap(){ return buf; }
}


