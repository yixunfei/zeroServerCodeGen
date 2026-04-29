package com.zero.codegen.runtime.serialize;

import io.netty.buffer.ByteBuf;

public final class VarInt {
    private VarInt(){}

    /**
     * 有符号整型采用 ZigZag + VarInt。
     * 这里保留现有线格式语义，适合允许出现负数的字段。
     */
    public static void writeVarInt(ByteBuf out,int value){
        writeUnsignedVarInt(out, (value<<1)^(value>>31));
    }

    public static int readVarInt(ByteBuf in){
        int raw=readUnsignedVarInt(in);
        return (raw>>>1)^-(raw&1);
    }

    /**
     * 非负整型直接使用无符号 VarInt。
     * 长度、数量、ordinal、位标记索引等天然非负字段必须走这条路径，
     * 这样可以避免 ZigZag 对正数的额外放大。
     */
    public static void writeUnsignedVarInt(ByteBuf out, int value){
        if(value<0){
            throw new IllegalArgumentException("unsigned varint can not encode negative int: "+value);
        }

        if(out.hasArray()){
            out.ensureWritable(5);
            int writerIndex=out.writerIndex();
            byte[] array=out.array();
            int index=out.arrayOffset()+writerIndex;
            if((value & ~0x7F)==0){
                array[index]=(byte) value;
                out.writerIndex(writerIndex+1);
                return;
            }
            if((value & ~0x3FFF)==0){
                array[index]=(byte) ((value & 0x7F) | 0x80);
                array[index+1]=(byte) (value>>>7);
                out.writerIndex(writerIndex+2);
                return;
            }
            if((value & ~0x1F_FFFF)==0){
                array[index]=(byte) ((value & 0x7F) | 0x80);
                array[index+1]=(byte) (((value>>>7) & 0x7F) | 0x80);
                array[index+2]=(byte) (value>>>14);
                out.writerIndex(writerIndex+3);
                return;
            }
            if((value & ~0x0FFF_FFFF)==0){
                array[index]=(byte) ((value & 0x7F) | 0x80);
                array[index+1]=(byte) (((value>>>7) & 0x7F) | 0x80);
                array[index+2]=(byte) (((value>>>14) & 0x7F) | 0x80);
                array[index+3]=(byte) (value>>>21);
                out.writerIndex(writerIndex+4);
                return;
            }
            array[index]=(byte) ((value & 0x7F) | 0x80);
            array[index+1]=(byte) (((value>>>7) & 0x7F) | 0x80);
            array[index+2]=(byte) (((value>>>14) & 0x7F) | 0x80);
            array[index+3]=(byte) (((value>>>21) & 0x7F) | 0x80);
            array[index+4]=(byte) (value>>>28);
            out.writerIndex(writerIndex+5);
            return;
        }

        if((value & ~0x7F)==0){
            out.writeByte(value);
            return;
        }
        if((value & ~0x3FFF)==0){
            out.writeByte((value & 0x7F) | 0x80);
            out.writeByte(value>>>7);
            return;
        }
        if((value & ~0x1F_FFFF)==0){
            out.writeByte((value & 0x7F) | 0x80);
            out.writeByte(((value>>>7) & 0x7F) | 0x80);
            out.writeByte(value>>>14);
            return;
        }
        if((value & ~0x0FFF_FFFF)==0){
            out.writeByte((value & 0x7F) | 0x80);
            out.writeByte(((value>>>7) & 0x7F) | 0x80);
            out.writeByte(((value>>>14) & 0x7F) | 0x80);
            out.writeByte(value>>>21);
            return;
        }
        out.writeByte((value & 0x7F) | 0x80);
        out.writeByte(((value>>>7) & 0x7F) | 0x80);
        out.writeByte(((value>>>14) & 0x7F) | 0x80);
        out.writeByte(((value>>>21) & 0x7F) | 0x80);
        out.writeByte(value>>>28);
    }

    public static int readUnsignedVarInt(ByteBuf in){
        if(in.hasArray()){
            return readUnsignedVarIntFromArray(in);
        }
        return readUnsignedVarIntSlow(in);
    }

    public static void writeVarLong(ByteBuf out,long value){
        writeUnsignedVarLong(out, (value<<1)^(value>>63));
    }

    public static long readVarLong(ByteBuf in){
        long raw=readUnsignedVarLong(in);
        return (raw>>>1)^-(raw&1L);
    }

    /**
     * 非负长整型直接使用无符号 VarLong。
     * 主要用于未来需要长长度或无符号 long key 的场景，避免 Java/C# 两端协议语义不一致。
     */
    public static void writeUnsignedVarLong(ByteBuf out, long value){
        if(value<0){
            throw new IllegalArgumentException("unsigned varlong can not encode negative long: "+value);
        }
        if(out.hasArray()){
            out.ensureWritable(10);
            int writerIndex=out.writerIndex();
            byte[] array=out.array();
            int index=out.arrayOffset()+writerIndex;
            int count=0;
            while((value & ~0x7FL)!=0L){
                array[index+count]=(byte) (((int) value & 0x7F) | 0x80);
                value>>>=7;
                count++;
            }
            array[index+count]=(byte) ((int) value);
            out.writerIndex(writerIndex+count+1);
            return;
        }

        while((value & ~0x7FL)!=0L){
            out.writeByte(((int) value & 0x7F) | 0x80);
            value>>>=7;
        }
        out.writeByte((int) value);
    }

    public static long readUnsignedVarLong(ByteBuf in){
        if(in.hasArray()){
            return readUnsignedVarLongFromArray(in);
        }
        return readUnsignedVarLongSlow(in);
    }

    private static int readUnsignedVarIntFromArray(ByteBuf in){
        int index=in.readerIndex();
        int limit=in.writerIndex();
        byte[] array=in.array();
        int offset=in.arrayOffset();
        if(index>=limit){
            throw new IndexOutOfBoundsException("not enough readable bytes, need=1, readable=0");
        }
        int b0=array[offset+index++] & 0xFF;
        if((b0 & 0x80)==0){
            in.readerIndex(index);
            return b0;
        }
        if(index>=limit){
            throw new IndexOutOfBoundsException("not enough readable bytes for unsigned varint");
        }
        int b1=array[offset+index++] & 0xFF;
        int result=(b0 & 0x7F) | ((b1 & 0x7F) << 7);
        if((b1 & 0x80)==0){
            in.readerIndex(index);
            return result;
        }
        if(index>=limit){
            throw new IndexOutOfBoundsException("not enough readable bytes for unsigned varint");
        }
        int b2=array[offset+index++] & 0xFF;
        result |= (b2 & 0x7F) << 14;
        if((b2 & 0x80)==0){
            in.readerIndex(index);
            return result;
        }
        if(index>=limit){
            throw new IndexOutOfBoundsException("not enough readable bytes for unsigned varint");
        }
        int b3=array[offset+index++] & 0xFF;
        result |= (b3 & 0x7F) << 21;
        if((b3 & 0x80)==0){
            in.readerIndex(index);
            return result;
        }
        if(index>=limit){
            throw new IndexOutOfBoundsException("not enough readable bytes for unsigned varint");
        }
        int b4=array[offset+index++] & 0xFF;
        result |= (b4 & 0x0F) << 28;
        if((b4 & 0xF0)==0){
            in.readerIndex(index);
            return result;
        }
        throw new IllegalArgumentException("unsigned varint is too long");
    }

    private static int readUnsignedVarIntSlow(ByteBuf in){
        int b0=in.readByte() & 0xFF;
        if((b0 & 0x80)==0){
            return b0;
        }
        int b1=in.readByte() & 0xFF;
        int result=(b0 & 0x7F) | ((b1 & 0x7F) << 7);
        if((b1 & 0x80)==0){
            return result;
        }
        int b2=in.readByte() & 0xFF;
        result |= (b2 & 0x7F) << 14;
        if((b2 & 0x80)==0){
            return result;
        }
        int b3=in.readByte() & 0xFF;
        result |= (b3 & 0x7F) << 21;
        if((b3 & 0x80)==0){
            return result;
        }
        int b4=in.readByte() & 0xFF;
        result |= (b4 & 0x0F) << 28;
        if((b4 & 0xF0)==0){
            return result;
        }
        throw new IllegalArgumentException("unsigned varint is too long");
    }

    private static long readUnsignedVarLongFromArray(ByteBuf in){
        int index=in.readerIndex();
        int limit=in.writerIndex();
        byte[] array=in.array();
        int offset=in.arrayOffset();
        if(index>=limit){
            throw new IndexOutOfBoundsException("not enough readable bytes, need=1, readable=0");
        }
        int b0=array[offset+index++] & 0xFF;
        if((b0 & 0x80)==0){
            in.readerIndex(index);
            return b0;
        }
        if(index>=limit){
            throw new IndexOutOfBoundsException("not enough readable bytes for unsigned varlong");
        }
        int b1=array[offset+index++] & 0xFF;
        long result=(b0 & 0x7FL) | ((long) (b1 & 0x7F) << 7);
        if((b1 & 0x80)==0){
            in.readerIndex(index);
            return result;
        }
        if(index>=limit){
            throw new IndexOutOfBoundsException("not enough readable bytes for unsigned varlong");
        }
        int b2=array[offset+index++] & 0xFF;
        result |= (long) (b2 & 0x7F) << 14;
        if((b2 & 0x80)==0){
            in.readerIndex(index);
            return result;
        }
        if(index>=limit){
            throw new IndexOutOfBoundsException("not enough readable bytes for unsigned varlong");
        }
        int b3=array[offset+index++] & 0xFF;
        result |= (long) (b3 & 0x7F) << 21;
        if((b3 & 0x80)==0){
            in.readerIndex(index);
            return result;
        }
        if(index>=limit){
            throw new IndexOutOfBoundsException("not enough readable bytes for unsigned varlong");
        }
        int b4=array[offset+index++] & 0xFF;
        result |= (long) (b4 & 0x7F) << 28;
        if((b4 & 0x80)==0){
            in.readerIndex(index);
            return result;
        }
        if(index>=limit){
            throw new IndexOutOfBoundsException("not enough readable bytes for unsigned varlong");
        }
        int b5=array[offset+index++] & 0xFF;
        result |= (long) (b5 & 0x7F) << 35;
        if((b5 & 0x80)==0){
            in.readerIndex(index);
            return result;
        }
        if(index>=limit){
            throw new IndexOutOfBoundsException("not enough readable bytes for unsigned varlong");
        }
        int b6=array[offset+index++] & 0xFF;
        result |= (long) (b6 & 0x7F) << 42;
        if((b6 & 0x80)==0){
            in.readerIndex(index);
            return result;
        }
        if(index>=limit){
            throw new IndexOutOfBoundsException("not enough readable bytes for unsigned varlong");
        }
        int b7=array[offset+index++] & 0xFF;
        result |= (long) (b7 & 0x7F) << 49;
        if((b7 & 0x80)==0){
            in.readerIndex(index);
            return result;
        }
        if(index>=limit){
            throw new IndexOutOfBoundsException("not enough readable bytes for unsigned varlong");
        }
        int b8=array[offset+index++] & 0xFF;
        result |= (long) (b8 & 0x7F) << 56;
        if((b8 & 0x80)==0){
            in.readerIndex(index);
            return result;
        }
        if(index>=limit){
            throw new IndexOutOfBoundsException("not enough readable bytes for unsigned varlong");
        }
        int b9=array[offset+index++] & 0xFF;
        result |= (long) (b9 & 0x01) << 63;
        if((b9 & 0xFE)==0){
            in.readerIndex(index);
            return result;
        }
        throw new IllegalArgumentException("unsigned varlong is too long");
    }

    private static long readUnsignedVarLongSlow(ByteBuf in){
        int b0=in.readByte() & 0xFF;
        if((b0 & 0x80)==0){
            return b0;
        }
        int b1=in.readByte() & 0xFF;
        long result=(b0 & 0x7FL) | ((long) (b1 & 0x7F) << 7);
        if((b1 & 0x80)==0){
            return result;
        }
        int b2=in.readByte() & 0xFF;
        result |= (long) (b2 & 0x7F) << 14;
        if((b2 & 0x80)==0){
            return result;
        }
        int b3=in.readByte() & 0xFF;
        result |= (long) (b3 & 0x7F) << 21;
        if((b3 & 0x80)==0){
            return result;
        }
        int b4=in.readByte() & 0xFF;
        result |= (long) (b4 & 0x7F) << 28;
        if((b4 & 0x80)==0){
            return result;
        }
        int b5=in.readByte() & 0xFF;
        result |= (long) (b5 & 0x7F) << 35;
        if((b5 & 0x80)==0){
            return result;
        }
        int b6=in.readByte() & 0xFF;
        result |= (long) (b6 & 0x7F) << 42;
        if((b6 & 0x80)==0){
            return result;
        }
        int b7=in.readByte() & 0xFF;
        result |= (long) (b7 & 0x7F) << 49;
        if((b7 & 0x80)==0){
            return result;
        }
        int b8=in.readByte() & 0xFF;
        result |= (long) (b8 & 0x7F) << 56;
        if((b8 & 0x80)==0){
            return result;
        }
        int b9=in.readByte() & 0xFF;
        result |= (long) (b9 & 0x01) << 63;
        if((b9 & 0xFE)==0){
            return result;
        }
        throw new IllegalArgumentException("unsigned varlong is too long");
    }
}
