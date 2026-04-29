package com.zero.codegen.runtime.bytes;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * 面向协议顺序读写的线性 buffer。
 *
 * 这里刻意不用环形结构，原因是当前协议 payload 的典型模式非常固定：
 * 1. 顺序写入一次。
 * 2. 转成 byte[] 或直接包装后发送。
 * 3. 或者顺序读取一次。
 *
 * 在线性场景下，环形 buffer 往往会带来：
 * - 更多取模计算
 * - 跨段复制
 * - 更复杂的边界判断
 *
 * 对追求极致吞吐的协议编码路径来说，这些复杂度通常不划算。
 */
public final class LinearByteBuffer implements ByteCursor {
    private static final int MIN_CAPACITY = 256;

    private byte[] array;
    private int readerIndex;
    private int writerIndex;

    public LinearByteBuffer(){
        this(MIN_CAPACITY);
    }

    public LinearByteBuffer(int initialCapacity){
        this.array=new byte[normalizeCapacity(initialCapacity)];
    }

    private static int normalizeCapacity(int capacity){
        int normalized=Math.max(MIN_CAPACITY, capacity);
        if(normalized==Integer.MAX_VALUE){
            return Integer.MAX_VALUE;
        }
        if((normalized & (normalized-1))==0){
            return normalized;
        }
        int next=Integer.highestOneBit(normalized)<<1;
        if(next<=0){
            return Integer.MAX_VALUE;
        }
        return next;
    }

    /**
     * 清空读写索引，但保留底层数组，供池化后的下一次复用。
     */
    public void clear(){
        readerIndex=0;
        writerIndex=0;
    }

    /**
     * 供对象池回收时调用。
     *
     * 如果这个 buffer 曾经因为大包扩容得很大，这里会在回池前主动降容。
     * 这样可以避免“偶发一次超大包，池里长期挂着大数组”的问题。
     */
    public void resetForPool(int defaultCapacity, int maxRetainedCapacity){
        clear();
        if(array.length>maxRetainedCapacity){
            array=new byte[normalizeCapacity(defaultCapacity)];
        }
    }

    public int capacity(){
        return array.length;
    }

    public int readerIndex(){
        return readerIndex;
    }

    public int writerIndex(){
        return writerIndex;
    }

    /**
     * 暴露底层数组，只给高性能只读发送链路使用。
     *
     * 调用方必须严格遵守：
     * 1. 不能修改返回数组内容。
     * 2. 只能读取 `[readerIndex, writerIndex)` 区间。
     * 3. buffer 归还对象池后，不能继续持有该数组引用。
     */
    public byte[] array(){
        return array;
    }

    public void setReaderIndex(int readerIndex){
        if(readerIndex<0 || readerIndex>writerIndex){
            throw new IndexOutOfBoundsException("readerIndex out of range: "+readerIndex);
        }
        this.readerIndex=readerIndex;
    }

    public void setWriterIndex(int writerIndex){
        if(writerIndex<readerIndex || writerIndex>array.length){
            throw new IndexOutOfBoundsException("writerIndex out of range: "+writerIndex);
        }
        this.writerIndex=writerIndex;
    }

    public void ensureWritable(int minWritableBytes){
        if(minWritableBytes<0){
            throw new IllegalArgumentException("minWritableBytes can not be negative: "+minWritableBytes);
        }
        long requiredLong=(long) writerIndex + minWritableBytes;
        if(requiredLong<=array.length){
            return;
        }
        if(requiredLong>Integer.MAX_VALUE){
            throw new OutOfMemoryError("required buffer size exceeds max array size: "+requiredLong);
        }
        int required=(int) requiredLong;
        int newCapacity=normalizeCapacity(required);
        if(readerIndex==0 && writerIndex==0){
            array=new byte[newCapacity];
            return;
        }
        array=Arrays.copyOf(array,newCapacity);
    }

    private void ensureReadable(int length){
        if(length<0 || readerIndex+length>writerIndex){
            throw new IndexOutOfBoundsException("not enough readable bytes, need="+length+", readable="+readableBytes());
        }
    }

    void writeUnsignedVarIntFast(int value){
        ensureWritable(5);
        int index=writerIndex;
        byte[] local=array;
        while((value & ~0x7F)!=0){
            local[index++]=(byte) ((value & 0x7F) | 0x80);
            value>>>=7;
        }
        local[index++]=(byte) value;
        writerIndex=index;
    }

    int readVarUInt32Fast(){
        int index=readerIndex;
        int limit=writerIndex;
        byte[] local=array;
        if(index>=limit){
            throw new IndexOutOfBoundsException("not enough readable bytes, need=1, readable=0");
        }
        int b0=local[index++] & 0xFF;
        if((b0 & 0x80)==0){
            readerIndex=index;
            return b0;
        }
        if(index>=limit){
            throw new IndexOutOfBoundsException("not enough readable bytes for unsigned varint");
        }
        int b1=local[index++] & 0xFF;
        int result=(b0 & 0x7F) | ((b1 & 0x7F) << 7);
        if((b1 & 0x80)==0){
            readerIndex=index;
            return result;
        }
        if(index>=limit){
            throw new IndexOutOfBoundsException("not enough readable bytes for unsigned varint");
        }
        int b2=local[index++] & 0xFF;
        result |= (b2 & 0x7F) << 14;
        if((b2 & 0x80)==0){
            readerIndex=index;
            return result;
        }
        if(index>=limit){
            throw new IndexOutOfBoundsException("not enough readable bytes for unsigned varint");
        }
        int b3=local[index++] & 0xFF;
        result |= (b3 & 0x7F) << 21;
        if((b3 & 0x80)==0){
            readerIndex=index;
            return result;
        }
        if(index>=limit){
            throw new IndexOutOfBoundsException("not enough readable bytes for unsigned varint");
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
            throw new IndexOutOfBoundsException("not enough readable bytes, need=1, readable=0");
        }
        int b0=local[index++] & 0xFF;
        if((b0 & 0x80)==0){
            readerIndex=index;
            return;
        }
        if(index>=limit){
            throw new IndexOutOfBoundsException("not enough readable bytes for unsigned varint");
        }
        int b1=local[index++] & 0xFF;
        if((b1 & 0x80)==0){
            readerIndex=index;
            return;
        }
        if(index>=limit){
            throw new IndexOutOfBoundsException("not enough readable bytes for unsigned varint");
        }
        int b2=local[index++] & 0xFF;
        if((b2 & 0x80)==0){
            readerIndex=index;
            return;
        }
        if(index>=limit){
            throw new IndexOutOfBoundsException("not enough readable bytes for unsigned varint");
        }
        int b3=local[index++] & 0xFF;
        if((b3 & 0x80)==0){
            readerIndex=index;
            return;
        }
        if(index>=limit){
            throw new IndexOutOfBoundsException("not enough readable bytes for unsigned varint");
        }
        int b4=local[index++] & 0xFF;
        if((b4 & 0xF0)==0){
            readerIndex=index;
            return;
        }
        throw new IllegalStateException("unsigned varint is too long");
    }

    void writeUnsignedVarLongFast(long value){
        ensureWritable(10);
        int index=writerIndex;
        byte[] local=array;
        while((value & ~0x7FL)!=0L){
            local[index++]=(byte) ((value & 0x7F) | 0x80);
            value>>>=7;
        }
        local[index++]=(byte) value;
        writerIndex=index;
    }

    long readVarUInt64Fast(){
        int index=readerIndex;
        int limit=writerIndex;
        byte[] local=array;
        long result=0L;
        int shift=0;
        while(shift<64){
            if(index>=limit){
                throw new IndexOutOfBoundsException("not enough readable bytes for unsigned varlong");
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

    void skipVarUInt64Fast(){
        int index=readerIndex;
        int limit=writerIndex;
        byte[] local=array;
        int shift=0;
        while(shift<64){
            if(index>=limit){
                throw new IndexOutOfBoundsException("not enough readable bytes for unsigned varlong");
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

    void writeFixedIntFast(int value){
        ensureWritable(4);
        int index=writerIndex;
        byte[] local=array;
        local[index]=(byte) (value>>>24);
        local[index+1]=(byte) (value>>>16);
        local[index+2]=(byte) (value>>>8);
        local[index+3]=(byte) value;
        writerIndex=index+4;
    }

    int readFixedIntFast(){
        ensureReadable(4);
        int index=readerIndex;
        byte[] local=array;
        readerIndex=index+4;
        return ((local[index] & 0xFF) << 24)
                | ((local[index+1] & 0xFF) << 16)
                | ((local[index+2] & 0xFF) << 8)
                | (local[index+3] & 0xFF);
    }

    void writeFixedLongFast(long value){
        ensureWritable(8);
        int index=writerIndex;
        byte[] local=array;
        local[index]=(byte) (value>>>56);
        local[index+1]=(byte) (value>>>48);
        local[index+2]=(byte) (value>>>40);
        local[index+3]=(byte) (value>>>32);
        local[index+4]=(byte) (value>>>24);
        local[index+5]=(byte) (value>>>16);
        local[index+6]=(byte) (value>>>8);
        local[index+7]=(byte) value;
        writerIndex=index+8;
    }

    long readFixedLongFast(){
        ensureReadable(8);
        int index=readerIndex;
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

    String readStringUtf8Fast(int length){
        if(length==0){
            return "";
        }
        ensureReadable(length);
        int index=readerIndex;
        readerIndex=index+length;
        return new String(array, index, length, StandardCharsets.UTF_8);
    }

    @Override
    public void writeByte(byte v) {
        ensureWritable(1);
        array[writerIndex++]=v;
    }

    @Override
    public void writeBytes(byte[] b, int off, int len) {
        if(b==null){
            throw new NullPointerException("source bytes can not be null");
        }
        if(off<0 || len<0 || off+len>b.length){
            throw new IndexOutOfBoundsException("write range out of bounds");
        }
        ensureWritable(len);
        System.arraycopy(b,off,array,writerIndex,len);
        writerIndex+=len;
    }

    @Override
    public byte readByte() {
        ensureReadable(1);
        return array[readerIndex++];
    }

    @Override
    public void readBytes(byte[] b, int off, int len) {
        if(b==null){
            throw new NullPointerException("target bytes can not be null");
        }
        if(off<0 || len<0 || off+len>b.length){
            throw new IndexOutOfBoundsException("read range out of bounds");
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
        return array.length-writerIndex;
    }

    /**
     * 拷贝当前可读区间。
     *
     * 如果最终消费方必须拿到独立 `byte[]`，这里就是唯一必要的复制点。
     * 其余写入过程尽量都在同一个线性数组上完成。
     */
    public byte[] toByteArray(){
        return Arrays.copyOfRange(array, readerIndex, writerIndex);
    }

    @Override
    public void skip(int length) {
        ensureReadable(length);
        readerIndex+=length;
    }
}

