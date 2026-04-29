package com.zero.codegen.runtime.serialize;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.IntFunction;

public final class BufUtil {
    private BufUtil(){}

    /**
     * Hash 结构按 0.75 负载因子预估容量。
     * 这里改成整数运算，避免序列化热路径上反复走 double/ceil。
     */
    private static int hashCapacity(int size){
        if(size<=0){
            return 4;
        }
        long capacity=((long) size * 4L + 2L) / 3L;
        return (int) Math.max(4L, capacity);
    }
    public static void writeInt(ByteBuf out,int v){ VarInt.writeVarInt(out,v); }
    public static int readInt(ByteBuf in){ return VarInt.readVarInt(in); }
    public static void writeLong(ByteBuf out,long v){ VarInt.writeVarLong(out,v); }
    public static long readLong(ByteBuf in){ return VarInt.readVarLong(in); }

    /**
     * 非负字段统一走无符号编码。
     * 这组方法专门给长度、集合 size、enum ordinal、char 等只会非负的数据使用。
     */
    public static void writeUInt(ByteBuf out,int v){ VarInt.writeUnsignedVarInt(out,v); }
    public static int readUInt(ByteBuf in){ return VarInt.readUnsignedVarInt(in); }
    public static void writeULong(ByteBuf out,long v){ VarInt.writeUnsignedVarLong(out,v); }
    public static long readULong(ByteBuf in){ return VarInt.readUnsignedVarLong(in); }
    public static void writeSize(ByteBuf out,int size){
        if(size<0){
            throw new IllegalArgumentException("size can not be negative: "+size);
        }
        VarInt.writeUnsignedVarInt(out,size);
    }
    public static int readSize(ByteBuf in){ return VarInt.readUnsignedVarInt(in); }

    public static void writeByte(ByteBuf out,byte v){ out.writeByte(v); }
    public static byte readByte(ByteBuf in){ return in.readByte(); }
    public static void writeShort(ByteBuf out,short v){ VarInt.writeVarInt(out, v); }
    public static short readShort(ByteBuf in){ return (short) VarInt.readVarInt(in); }
    public static void writeBoolean(ByteBuf out,boolean v){ out.writeBoolean(v); }
    public static boolean readBoolean(ByteBuf in){ return in.readBoolean(); }

    /**
     * char 天然非负，继续走 ZigZag 会让大部分字符长度膨胀 1 bit。
     */
    public static void writeChar(ByteBuf out,char v){ VarInt.writeUnsignedVarInt(out, v); }
    public static char readChar(ByteBuf in){ return (char) VarInt.readUnsignedVarInt(in); }
    public static void writeFloat(ByteBuf out,float v){ out.writeInt(Float.floatToIntBits(v)); }
    public static float readFloat(ByteBuf in){ return Float.intBitsToFloat(in.readInt()); }
    public static void writeDouble(ByteBuf out,double v){ out.writeLong(Double.doubleToLongBits(v)); }
    public static double readDouble(ByteBuf in){ return Double.longBitsToDouble(in.readLong()); }

    /**
     * 字符串长度改走无符号 size 编码。
     * 这样长度本身不会被 ZigZag 放大，协议体更紧凑。
     */
    public static void writeString(ByteBuf out,String s){
        String value=s==null? "": s;
        int len=ByteBufUtil.utf8Bytes(value);
        writeSize(out,len);
        if(len>0){
            ByteBufUtil.writeUtf8(out, value);
        }
    }
    public static String readString(ByteBuf in){
        int len=readSize(in);
        if(len==0) return "";
        String value=in.toString(in.readerIndex(), len, StandardCharsets.UTF_8);
        in.skipBytes(len);
        return value;
    }
    public static <T> void writeCollection(ByteBuf out, Collection<T> values, BiConsumer<ByteBuf,T> elemWriter){
        writeSize(out,values==null?0:values.size());
        if(values!=null){
            for(T t:values){ elemWriter.accept(out,t); }
        }
    }
    public static <T, C extends Collection<T>> C readCollection(ByteBuf in, IntFunction<C> creator, Function<ByteBuf,T> elemReader){
        int n=readSize(in);
        C collection=creator.apply(n);
        for(int i=0;i<n;i++){ collection.add(elemReader.apply(in)); }
        return collection;
    }
    public static <T> void writeList(ByteBuf out, List<T> list, BiConsumer<ByteBuf,T> elemWriter){
        writeCollection(out, list, elemWriter);
    }
    public static <T, L extends List<T>> L readList(ByteBuf in, IntFunction<L> creator, Function<ByteBuf,T> elemReader){
        return readCollection(in, creator, elemReader);
    }
    public static <T> List<T> readList(ByteBuf in, Function<ByteBuf,T> elemReader){
        return readCollection(in, ArrayList::new, elemReader);
    }
    public static <K,V> void writeMap(ByteBuf out, Map<K,V> map, BiConsumer<ByteBuf,K> keyWriter, BiConsumer<ByteBuf,V> valWriter){
        writeSize(out,map==null?0:map.size());
        if(map!=null){
            for(Map.Entry<K,V> e: map.entrySet()){
                keyWriter.accept(out,e.getKey());
                valWriter.accept(out,e.getValue());
            }
        }
    }
    public static <K,V,M extends Map<K,V>> M readMap(ByteBuf in, IntFunction<M> creator, Function<ByteBuf,K> keyReader, Function<ByteBuf,V> valReader){
        int n=readSize(in);
        M map=creator.apply(n);
        for(int i=0;i<n;i++){
            K k=keyReader.apply(in);
            V v=valReader.apply(in);
            map.put(k,v);
        }
        return map;
    }
    public static <K,V> Map<K,V> readMap(ByteBuf in, Function<ByteBuf,K> keyReader, Function<ByteBuf,V> valReader){
        return readMap(in, n->new HashMap<>(hashCapacity(n)), keyReader, valReader);
    }
    public static <T> void writeSet(ByteBuf out, Set<T> set, BiConsumer<ByteBuf,T> elemWriter){
        writeCollection(out, set, elemWriter);
    }
    public static <T, S extends Set<T>> S readSet(ByteBuf in, IntFunction<S> creator, Function<ByteBuf,T> elemReader){
        return readCollection(in, creator, elemReader);
    }
    public static <T> Set<T> readSet(ByteBuf in, Function<ByteBuf,T> elemReader){
        return readCollection(in, n->new HashSet<>(hashCapacity(n)), elemReader);
    }
    public static <T> LinkedHashSet<T> readLinkedSet(ByteBuf in, Function<ByteBuf,T> elemReader){
        return readCollection(in, n->new LinkedHashSet<>(hashCapacity(n)), elemReader);
    }
    private static void writePresenceChunk(ByteBuf out, long bits, int byteCount){
        switch (byteCount){
            case 0:
                return;
            case 1:
                out.writeByte((int) bits);
                return;
            case 2:
                out.writeShortLE((int) bits);
                return;
            case 4:
                out.writeIntLE((int) bits);
                return;
            case 8:
                out.writeLongLE(bits);
                return;
            default:
                for(int i=0;i<byteCount;i++){
                    out.writeByte((int) (bits >>> (i<<3)));
                }
        }
    }
    private static long readPresenceChunk(ByteBuf in, int byteCount){
        switch (byteCount){
            case 0:
                return 0L;
            case 1:
                return in.readUnsignedByte();
            case 2:
                return in.readUnsignedShortLE();
            case 4:
                return in.readUnsignedIntLE();
            case 8:
                return in.readLongLE();
            default:
                long bits=0L;
                for(int i=0;i<byteCount;i++){
                    bits |= (long) in.readUnsignedByte() << (i<<3);
                }
                return bits;
        }
    }
    public static void writePresenceBits(ByteBuf out, long bits, int fieldCount){
        int byteCount=(fieldCount+7)>>>3;
        switch (byteCount){
            case 3:
                writePresenceChunk(out, bits, 2);
                out.writeByte((int) (bits >>> 16));
                return;
            case 5:
                writePresenceChunk(out, bits, 4);
                out.writeByte((int) (bits >>> 32));
                return;
            case 6:
                writePresenceChunk(out, bits, 4);
                writePresenceChunk(out, bits >>> 32, 2);
                return;
            case 7:
                writePresenceChunk(out, bits, 4);
                writePresenceChunk(out, bits >>> 32, 2);
                out.writeByte((int) (bits >>> 48));
                return;
            default:
                writePresenceChunk(out, bits, byteCount);
        }
    }
    public static long readPresenceBits(ByteBuf in, int fieldCount){
        int byteCount=(fieldCount+7)>>>3;
        switch (byteCount){
            case 3:
                return readPresenceChunk(in, 2) | ((long) in.readUnsignedByte() << 16);
            case 5:
                return readPresenceChunk(in, 4) | ((long) in.readUnsignedByte() << 32);
            case 6:
                return readPresenceChunk(in, 4) | (readPresenceChunk(in, 2) << 32);
            case 7:
                return readPresenceChunk(in, 4)
                        | (readPresenceChunk(in, 2) << 32)
                        | ((long) in.readUnsignedByte() << 48);
            default:
                return readPresenceChunk(in, byteCount);
        }
    }
    public static void writePresenceBits(ByteBuf out, long[] words, int fieldCount){
        int remaining=fieldCount;
        for(int i=0;i<words.length && remaining>0;i++){
            int bitsForWord=Math.min(remaining, 64);
            writePresenceBits(out, words[i], bitsForWord);
            remaining-=bitsForWord;
        }
    }
    public static long[] readPresenceWords(ByteBuf in, int fieldCount){
        long[] words=new long[(fieldCount+63)>>>6];
        int remaining=fieldCount;
        for(int i=0;i<words.length && remaining>0;i++){
            int bitsForWord=Math.min(remaining, 64);
            words[i]=readPresenceBits(in, bitsForWord);
            remaining-=bitsForWord;
        }
        return words;
    }
    public static boolean isPresenceBitSet(long[] words, int index){
        return (words[index>>>6] & (1L << (index&63))) != 0L;
    }
    public static <T> void writeOptional(ByteBuf out, Optional<T> optional, BiConsumer<ByteBuf,T> writer){
        boolean present=optional!=null && optional.isPresent();
        writeBoolean(out, present);
        if(present){
            writer.accept(out, optional.get());
        }
    }
    public static <T> Optional<T> readOptional(ByteBuf in, Function<ByteBuf,T> reader){
        if(!readBoolean(in)) return Optional.empty();
        return Optional.ofNullable(reader.apply(in));
    }
    public static <K,V> LinkedHashMap<K,V> readLinkedMap(ByteBuf in, Function<ByteBuf,K> keyReader, Function<ByteBuf,V> valReader){
        return readMap(in, n->new LinkedHashMap<>(hashCapacity(n)), keyReader, valReader);
    }
    public static void writeBytes(ByteBuf out, byte[] arr){
        writeSize(out, arr==null?0:arr.length);
        if(arr!=null) out.writeBytes(arr);
    }
    public static byte[] readBytes(ByteBuf in){
        int n=readSize(in);
        if(n==0) return new byte[0];
        byte[] a=new byte[n];
        in.readBytes(a);
        return a;
    }
    public static void writeIntArray(ByteBuf out,int[] arr){
        writeSize(out, arr==null?0:arr.length);
        if(arr!=null) for(int v: arr) writeInt(out,v);
    }
    public static int[] readIntArray(ByteBuf in){
        int n=readSize(in);
        int[] a=new int[n];
        for(int i=0;i<n;i++) a[i]=readInt(in);
        return a;
    }
    public static void writeLongArray(ByteBuf out,long[] arr){
        writeSize(out, arr==null?0:arr.length);
        if(arr!=null) for(long v: arr) writeLong(out,v);
    }
    public static long[] readLongArray(ByteBuf in){
        int n=readSize(in);
        long[] a=new long[n];
        for(int i=0;i<n;i++) a[i]=readLong(in);
        return a;
    }
    public static void writeShortArray(ByteBuf out,short[] arr){
        writeSize(out, arr==null?0:arr.length);
        if(arr!=null) for(short v: arr) writeShort(out,v);
    }
    public static short[] readShortArray(ByteBuf in){
        int n=readSize(in);
        short[] a=new short[n];
        for(int i=0;i<n;i++) a[i]=readShort(in);
        return a;
    }
    public static void writeBooleanArray(ByteBuf out,boolean[] arr){
        writeSize(out, arr==null?0:arr.length);
        if(arr!=null) for(boolean v: arr) writeBoolean(out,v);
    }
    public static boolean[] readBooleanArray(ByteBuf in){
        int n=readSize(in);
        boolean[] a=new boolean[n];
        for(int i=0;i<n;i++) a[i]=readBoolean(in);
        return a;
    }
    public static void writeCharArray(ByteBuf out,char[] arr){
        writeSize(out, arr==null?0:arr.length);
        if(arr!=null) for(char v: arr) writeChar(out,v);
    }
    public static char[] readCharArray(ByteBuf in){
        int n=readSize(in);
        char[] a=new char[n];
        for(int i=0;i<n;i++) a[i]=readChar(in);
        return a;
    }
    public static void writeFloatArray(ByteBuf out,float[] arr){
        writeSize(out, arr==null?0:arr.length);
        if(arr!=null) for(float v: arr) writeFloat(out,v);
    }
    public static float[] readFloatArray(ByteBuf in){
        int n=readSize(in);
        float[] a=new float[n];
        for(int i=0;i<n;i++) a[i]=readFloat(in);
        return a;
    }
    public static void writeDoubleArray(ByteBuf out,double[] arr){
        writeSize(out, arr==null?0:arr.length);
        if(arr!=null) for(double v: arr) writeDouble(out,v);
    }
    public static double[] readDoubleArray(ByteBuf in){
        int n=readSize(in);
        double[] a=new double[n];
        for(int i=0;i<n;i++) a[i]=readDouble(in);
        return a;
    }
    public static <T> void writeObjectArray(ByteBuf out, T[] arr, BiConsumer<ByteBuf,T> writer){
        writeSize(out, arr==null?0:arr.length);
        if(arr!=null) for(T v: arr) writer.accept(out,v);
    }
    public static <T> T[] readObjectArray(ByteBuf in, IntFunction<T[]> creator, Function<ByteBuf,T> reader){
        int n=readSize(in);
        T[] arr=creator.apply(n);
        for(int i=0;i<n;i++) arr[i]=reader.apply(in);
        return arr;
    }
}

