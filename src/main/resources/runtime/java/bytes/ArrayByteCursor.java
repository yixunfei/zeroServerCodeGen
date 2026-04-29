package com.zero.codegen.runtime.bytes;

import java.nio.charset.StandardCharsets;

/**
 * 面向反序列化的只读游标。
 * 它直接包装现成的 payload byte[]，不复制内容，也不参与池化，
 * 这样 dispatch 侧可以零额外数据拷贝地顺序解码。
 */
public final class ArrayByteCursor implements ByteCursor {
    private static final byte[] EMPTY = new byte[0];

    private byte[] array;
    private int writerIndex;
    private int readerIndex;

    public ArrayByteCursor(byte[] array){
        reset(array);
    }

    /**
     * 复用同一个 cursor 包裹新的 payload，避免高频 benchmark/dispatch 场景反复 new 游标对象。
     * 这里只重置读指针与边界，不复制 byte[] 内容。
     */
    public void reset(byte[] array){
        this.array=array==null? EMPTY: array;
        this.writerIndex=this.array.length;
        this.readerIndex=0;
    }

    private void ensureReadable(int length){
        if(length<0 || writerIndex-readerIndex<length){
            throw notEnoughReadable(length);
        }
    }

    private IndexOutOfBoundsException notEnoughReadable(int length){
        return new IndexOutOfBoundsException("not enough readable bytes, need="+length+", readable="+readableBytes());
    }

    /**
     * 为 VarInt/VarLong 热路径提供专用入口。
     * 这里直接操作底层数组与索引，避免每个字节都走接口分派和 ensureReadable 调用。
     */
    int readVarUInt32Fast(){
        int index=readerIndex;
        int limit=writerIndex;
        byte[] local=array;
        if(index>=limit){
            throw notEnoughReadable(1);
        }
        int b0=local[index++] & 0xFF;
        if((b0 & 0x80)==0){
            readerIndex=index;
            return b0;
        }
        if(index>=limit){
            throw notEnoughReadable(1);
        }
        int b1=local[index++] & 0xFF;
        int result=(b0 & 0x7F) | ((b1 & 0x7F) << 7);
        if((b1 & 0x80)==0){
            readerIndex=index;
            return result;
        }
        if(index>=limit){
            throw notEnoughReadable(1);
        }
        int b2=local[index++] & 0xFF;
        result |= (b2 & 0x7F) << 14;
        if((b2 & 0x80)==0){
            readerIndex=index;
            return result;
        }
        if(index>=limit){
            throw notEnoughReadable(1);
        }
        int b3=local[index++] & 0xFF;
        result |= (b3 & 0x7F) << 21;
        if((b3 & 0x80)==0){
            readerIndex=index;
            return result;
        }
        if(index>=limit){
            throw notEnoughReadable(1);
        }
        int b4=local[index++] & 0xFF;
        result |= (b4 & 0x0F) << 28;
        if((b4 & 0xF0)==0){
            readerIndex=index;
            return result;
        }
        throw new IllegalStateException("unsigned varint is too long");
    }

    void skipVarUInt32Fast(){
        int index=readerIndex;
        int limit=writerIndex;
        byte[] local=array;
        if(index>=limit){
            throw notEnoughReadable(1);
        }
        int b0=local[index++] & 0xFF;
        if((b0 & 0x80)==0){
            readerIndex=index;
            return;
        }
        if(index>=limit){
            throw notEnoughReadable(1);
        }
        int b1=local[index++] & 0xFF;
        if((b1 & 0x80)==0){
            readerIndex=index;
            return;
        }
        if(index>=limit){
            throw notEnoughReadable(1);
        }
        int b2=local[index++] & 0xFF;
        if((b2 & 0x80)==0){
            readerIndex=index;
            return;
        }
        if(index>=limit){
            throw notEnoughReadable(1);
        }
        int b3=local[index++] & 0xFF;
        if((b3 & 0x80)==0){
            readerIndex=index;
            return;
        }
        if(index>=limit){
            throw notEnoughReadable(1);
        }
        int b4=local[index++] & 0xFF;
        if((b4 & 0xF0)==0){
            readerIndex=index;
            return;
        }
        throw new IllegalStateException("unsigned varint is too long");
    }

    long readVarUInt64Fast(){
        int index=readerIndex;
        int limit=writerIndex;
        byte[] local=array;
        long result=0L;
        int shift=0;
        while(shift<64){
            if(index>=limit){
                throw notEnoughReadable(1);
            }
            int b=local[index++] & 0xFF;
            result |= (long) (b & 0x7F) << shift;
            if((b & 0x80)==0){
                readerIndex=index;
                return result;
            }
            shift+=7;
        }
        throw new IllegalStateException("unsigned varlong is too long");
    }

    int readFixedIntFast(){
        int index=readerIndex;
        if(writerIndex-index<4){
            throw notEnoughReadable(4);
        }
        byte[] local=array;
        readerIndex=index+4;
        return ((local[index] & 0xFF) << 24)
                | ((local[index+1] & 0xFF) << 16)
                | ((local[index+2] & 0xFF) << 8)
                | (local[index+3] & 0xFF);
    }

    long readFixedLongFast(){
        int index=readerIndex;
        if(writerIndex-index<8){
            throw notEnoughReadable(8);
        }
        byte[] local=array;
        readerIndex=index+8;
        return ((long) (local[index] & 0xFF) << 56)
                | ((long) (local[index+1] & 0xFF) << 48)
                | ((long) (local[index+2] & 0xFF) << 40)
                | ((long) (local[index+3] & 0xFF) << 32)
                | ((long) (local[index+4] & 0xFF) << 24)
                | ((long) (local[index+5] & 0xFF) << 16)
                | ((long) (local[index+6] & 0xFF) << 8)
                | ((long) (local[index+7] & 0xFF));
    }

    void skipVarUInt64Fast(){
        int index=readerIndex;
        int limit=writerIndex;
        byte[] local=array;
        int shift=0;
        while(shift<64){
            if(index>=limit){
                throw notEnoughReadable(1);
            }
            int b=local[index++] & 0xFF;
            if((b & 0x80)==0){
                readerIndex=index;
                return;
            }
            shift+=7;
        }
        throw new IllegalStateException("unsigned varlong is too long");
    }

    /**
     * 直接从底层数组切片解码 UTF-8，避免先拷贝到临时 byte[] 再构造 String。
     */
    String readStringUtf8Fast(int length){
        if(length==0){
            return "";
        }
        int index=readerIndex;
        if(length<0 || writerIndex-index<length){
            throw notEnoughReadable(length);
        }
        readerIndex=index+length;
        return new String(array, index, length, StandardCharsets.UTF_8);
    }

    public byte[] getArray(){
        return array;
    }

    public int getOffset(){
        return readerIndex;
    }

    @Override
    public void writeByte(byte v) {
        throw new UnsupportedOperationException("ArrayByteCursor is read-only");
    }

    @Override
    public void writeBytes(byte[] b, int off, int len) {
        throw new UnsupportedOperationException("ArrayByteCursor is read-only");
    }

    @Override
    public byte readByte() {
        int index=readerIndex;
        if(index>=writerIndex){
            throw notEnoughReadable(1);
        }
        readerIndex=index+1;
        return array[index];
    }

    @Override
    public void readBytes(byte[] b, int off, int len) {
        if(b==null){
            throw new NullPointerException("target bytes can not be null");
        }
        if(off<0 || len<0 || off+len>b.length){
            throw new IndexOutOfBoundsException("read range out of bounds");
        }
        if(len==0){
            return;
        }
        ensureReadable(len);
        System.arraycopy(array,readerIndex,b,off,len);
        readerIndex+=len;
    }

    @Override
    public int readableBytes() {
        return writerIndex-readerIndex;
    }

    @Override
    public int writableBytes() {
        return 0;
    }

    @Override
    public byte[] array() {
        return array;
    }

    @Override
    public int readerIndex() {
        return readerIndex;
    }

    @Override
    public void skip(int length) {
        ensureReadable(length);
        readerIndex+=length;
    }
}

