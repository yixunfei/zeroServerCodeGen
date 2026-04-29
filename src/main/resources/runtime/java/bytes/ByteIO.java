package com.zero.codegen.runtime.bytes;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import com.zero.codegen.runtime.serialize.BufUtil;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.ByteOrder;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.RandomAccess;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.IntFunction;

public final class ByteIO {
    private static final byte[] EMPTY_BYTES = new byte[0];
    private static final Unsafe UNSAFE = initUnsafe();
    private static final long BYTE_ARRAY_OFFSET = UNSAFE==null? -1L : UNSAFE.arrayBaseOffset(byte[].class);
    private static final long INT_ARRAY_OFFSET = UNSAFE==null? -1L : UNSAFE.arrayBaseOffset(int[].class);
    private static final long LONG_ARRAY_OFFSET = UNSAFE==null? -1L : UNSAFE.arrayBaseOffset(long[].class);
    private static final long FLOAT_ARRAY_OFFSET = UNSAFE==null? -1L : UNSAFE.arrayBaseOffset(float[].class);
    private static final long DOUBLE_ARRAY_OFFSET = UNSAFE==null? -1L : UNSAFE.arrayBaseOffset(double[].class);
    private static final boolean NATIVE_BIG_ENDIAN = ByteOrder.nativeOrder()==ByteOrder.BIG_ENDIAN;
    private static final int MAX_POOLED_CONTAINERS = 32;
    private static final ThreadLocal<ArrayDeque<ArrayList<?>>> ARRAY_LIST_POOL = ThreadLocal.withInitial(ArrayDeque::new);
    private static final ThreadLocal<ArrayDeque<HashSet<?>>> HASH_SET_POOL = ThreadLocal.withInitial(ArrayDeque::new);
    private static final ThreadLocal<ArrayDeque<LinkedHashSet<?>>> LINKED_HASH_SET_POOL = ThreadLocal.withInitial(ArrayDeque::new);
    private static final ThreadLocal<ArrayDeque<ArrayDeque<?>>> ARRAY_DEQUE_POOL = ThreadLocal.withInitial(ArrayDeque::new);
    private static final ThreadLocal<ArrayDeque<HashMap<?, ?>>> HASH_MAP_POOL = ThreadLocal.withInitial(ArrayDeque::new);
    private static final ThreadLocal<ArrayDeque<LinkedHashMap<?, ?>>> LINKED_HASH_MAP_POOL = ThreadLocal.withInitial(ArrayDeque::new);
    private static final ThreadLocal<ArrayDeque<IntArrayList>> INT_ARRAY_LIST_POOL = ThreadLocal.withInitial(ArrayDeque::new);
    private static final ThreadLocal<ArrayDeque<LongArrayList>> LONG_ARRAY_LIST_POOL = ThreadLocal.withInitial(ArrayDeque::new);
    private static final ThreadLocal<ArrayDeque<IntObjectHashMap<?>>> INT_OBJECT_HASH_MAP_POOL = ThreadLocal.withInitial(ArrayDeque::new);
    private static final ThreadLocal<ArrayDeque<IntIntHashMap>> INT_INT_HASH_MAP_POOL = ThreadLocal.withInitial(ArrayDeque::new);
    private static final ThreadLocal<ArrayDeque<IntLongHashMap>> INT_LONG_HASH_MAP_POOL = ThreadLocal.withInitial(ArrayDeque::new);
    private static final ThreadLocal<Utf8Scratch> UTF8_SCRATCH = ThreadLocal.withInitial(Utf8Scratch::new);
    private static final byte[] ZERO_PADDING_BLOCK = new byte[256];
    private static final int START_WRITE_RESERVE_THRESHOLD = 4096;
    private static volatile boolean UNSAFE_BORROW_ENABLED = false;

    @FunctionalInterface
    public interface PackedIntObjectByteBufReader<V> {
        V read(ByteBuf c);
    }

    @FunctionalInterface
    public interface PackedIntObjectValueReader<V> {
        V read(ByteCursor c);
    }

    private ByteIO(){}

    public static void setUnsafeBorrowEnabled(boolean enabled){
        UNSAFE_BORROW_ENABLED=enabled;
    }

    public static boolean isUnsafeBorrowEnabled(){
        return UNSAFE_BORROW_ENABLED;
    }

    private static Unsafe initUnsafe(){
        try{
            Field field=Unsafe.class.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            return (Unsafe) field.get(null);
        }catch (Throwable ignored){
            return null;
        }
    }

    /**
     * Hash 閻庡湱鎳撳▍鎺擃渶濡鍚囬柟?0.75 閻犳劗鍠曞ù鍥炊閻樿尙鎽嶅Λ鏉垮閸欏﹦鈧懓缍婇崳娲Υ?     * 閺夆晜鐟╅崳閿嬬┍濠靛洤鐦柡浣哥摠閺嗙喐娼婚幇顔炬毈闁挎稑鐭傛导鈺呭礂瀹ュ洤鍔伴悹渚灠缁剁偞绋夋繝鍌氭瘔锟?double/ceil锟?     */
    private static int hashCapacity(int size){
        if(size<=0){
            return 4;
        }
        long capacity=((long) size * 4L + 2L) / 3L;
        return (int) Math.max(4L, capacity);
    }

    public static int sizeOfPresenceBits(int fieldCount){
        return (fieldCount+7)>>>3;
    }

    public static int sizeOfUnsignedVarInt(int v){
        if(v<0){
            throw new IllegalArgumentException("unsigned varint can not encode negative int: "+v);
        }
        if((v & ~0x7F)==0) return 1;
        if((v & ~0x3FFF)==0) return 2;
        if((v & ~0x1FFFFF)==0) return 3;
        if((v & ~0xFFFFFFF)==0) return 4;
        return 5;
    }

    public static int sizeOfUnsignedVarLong(long v){
        if(v<0L){
            throw new IllegalArgumentException("unsigned varlong can not encode negative long: "+v);
        }
        int size=1;
        while((v & ~0x7FL)!=0L){
            v>>>=7;
            size++;
        }
        return size;
    }

    public static int sizeOfVarInt(int v){
        return sizeOfUnsignedVarInt((v<<1)^(v>>31));
    }

    public static int sizeOfVarLong(long v){
        return sizeOfUnsignedVarLong((v<<1)^(v>>63));
    }

    public static int sizeOfInt(int v){ return sizeOfVarInt(v); }
    public static int sizeOfLong(long v){ return sizeOfVarLong(v); }
    public static int sizeOfUInt(int v){ return sizeOfUnsignedVarInt(v); }
    public static int sizeOfULong(long v){ return sizeOfUnsignedVarLong(v); }
    public static int sizeOfSize(int size){ return sizeOfUnsignedVarInt(size); }
    public static int sizeOfShort(short v){ return sizeOfVarInt(v); }
    public static int sizeOfByte(byte v){ return 1; }
    public static int sizeOfBoolean(boolean v){ return 1; }
    public static int sizeOfChar(char v){ return sizeOfUnsignedVarInt(v); }
    public static int sizeOfFloat(float v){ return 4; }
    public static int sizeOfDouble(double v){ return 8; }

    public static boolean shouldReserveOnWriteStart(ByteBuf c){
        return c.writerIndex()==0 && c.writableBytes()<START_WRITE_RESERVE_THRESHOLD;
    }

    public static boolean shouldReserveOnWriteStart(ByteCursor c){
        if(c instanceof LinearByteBuffer){
            LinearByteBuffer buffer=(LinearByteBuffer) c;
            return buffer.writerIndex()==0 && buffer.writableBytes()<START_WRITE_RESERVE_THRESHOLD;
        }
        if(c instanceof NettyCursor){
            ByteBuf buffer=((NettyCursor) c).unwrap();
            return buffer.writerIndex()==0 && buffer.writableBytes()<START_WRITE_RESERVE_THRESHOLD;
        }
        return false;
    }

    public static void reserveForWriteStart(ByteBuf c, int estimatedBytes){
        if(estimatedBytes<=0 || c.writerIndex()!=0){
            return;
        }
        c.ensureWritable(estimatedBytes);
    }

    public static void reserveForWriteStart(ByteCursor c, int estimatedBytes){
        if(estimatedBytes<=0){
            return;
        }
        if(c instanceof LinearByteBuffer){
            LinearByteBuffer buffer=(LinearByteBuffer) c;
            if(buffer.writerIndex()==0){
                buffer.ensureWritable(estimatedBytes);
            }
            return;
        }
        if(c instanceof NettyCursor){
            ByteBuf buffer=((NettyCursor) c).unwrap();
            if(buffer.writerIndex()==0){
                buffer.ensureWritable(estimatedBytes);
            }
        }
    }

    public static int utf8Length(String value){
        if(value==null || value.isEmpty()){
            return 0;
        }
        return ByteBufUtil.utf8Bytes(value);
    }

    public static int sizeOfString(String value){
        int byteLength=utf8Length(value);
        return sizeOfSize(byteLength)+byteLength;
    }

    public static int sizeOfBorrowedBytes(BorrowedBytes value){
        int byteLength=value==null?0:value.length();
        return sizeOfSize(byteLength)+byteLength;
    }

    public static int sizeOfBorrowedString(BorrowedString value){
        int byteLength=value==null?0:value.byteLength();
        return sizeOfSize(byteLength)+byteLength;
    }

    public static int sizeOfBorrowedRawIntArray(IntArrayView value){
        int count=value==null?0:value.count();
        return sizeOfSize(count)+(count<<2);
    }

    public static int sizeOfBorrowedRawLongArray(LongArrayView value){
        int count=value==null?0:value.count();
        return sizeOfSize(count)+(count<<3);
    }

    public static int sizeOfBorrowedRawFloatArray(FloatArrayView value){
        int count=value==null?0:value.count();
        return sizeOfSize(count)+(count<<2);
    }

    public static int sizeOfBorrowedRawDoubleArray(DoubleArrayView value){
        int count=value==null?0:value.count();
        return sizeOfSize(count)+(count<<3);
    }

    private static boolean hasHeapBackingArray(ByteCursor c){
        return c instanceof ArrayByteCursor
                || c instanceof LinearByteBuffer
                || (c instanceof NettyCursor && ((NettyCursor) c).unwrap().hasArray());
    }

    private static boolean hasHeapWritableArray(ByteCursor c){
        return c instanceof LinearByteBuffer
                || (c instanceof NettyCursor && ((NettyCursor) c).unwrap().hasArray());
    }

    private static boolean canUseUnsafeHeapArrayAccess(ByteCursor c){
        return UNSAFE!=null && hasHeapBackingArray(c);
    }

    private static boolean canUseUnsafeHeapWriteArrayAccess(ByteCursor c){
        return UNSAFE!=null && hasHeapWritableArray(c);
    }

    private static boolean canUseUnsafeArrayAccess(ByteCursor c){
        return canUseUnsafeHeapArrayAccess(c);
    }

    private static byte[] backingArray(ByteCursor c){
        return c.array();
    }

    private static int backingOffset(ByteCursor c){
        if(c instanceof ArrayByteCursor){
            return ((ArrayByteCursor) c).getOffset();
        }
        if(c instanceof NettyCursor){
            ByteBuf buf=((NettyCursor) c).unwrap();
            return buf.arrayOffset()+buf.readerIndex();
        }
        return c.readerIndex();
    }

    private static int backingWriteOffset(ByteCursor c){
        if(c instanceof LinearByteBuffer){
            return ((LinearByteBuffer) c).writerIndex();
        }
        if(c instanceof NettyCursor){
            ByteBuf buf=((NettyCursor) c).unwrap();
            return buf.arrayOffset()+buf.writerIndex();
        }
        return -1;
    }

    private static void advanceWrite(ByteCursor c, int bytes){
        if(c instanceof LinearByteBuffer){
            LinearByteBuffer buffer=(LinearByteBuffer) c;
            buffer.setWriterIndex(buffer.writerIndex()+bytes);
            return;
        }
        if(c instanceof NettyCursor){
            ByteBuf buf=((NettyCursor) c).unwrap();
            buf.writerIndex(buf.writerIndex()+bytes);
        }
    }

    private static void ensureHeapWritable(ByteCursor c, int bytes){
        if(c instanceof LinearByteBuffer){
            ((LinearByteBuffer) c).ensureWritable(bytes);
            return;
        }
        if(c instanceof NettyCursor){
            ((NettyCursor) c).unwrap().ensureWritable(bytes);
        }
    }

    private static boolean canUseDirectWriteArrayAccess(ByteCursor c){
        return hasHeapWritableArray(c);
    }

    private static void writeHeapBigEndianInt(byte[] target, int offset, int value){
        if(UNSAFE!=null){
            UNSAFE.putInt(target, BYTE_ARRAY_OFFSET+offset, NATIVE_BIG_ENDIAN? value : Integer.reverseBytes(value));
            return;
        }
        target[offset]=(byte) (value>>>24);
        target[offset+1]=(byte) (value>>>16);
        target[offset+2]=(byte) (value>>>8);
        target[offset+3]=(byte) value;
    }

    private static int readHeapBigEndianInt(byte[] source, int offset){
        if(UNSAFE!=null){
            int value=UNSAFE.getInt(source, BYTE_ARRAY_OFFSET+offset);
            return NATIVE_BIG_ENDIAN? value : Integer.reverseBytes(value);
        }
        return ((source[offset] & 0xFF) << 24)
                | ((source[offset+1] & 0xFF) << 16)
                | ((source[offset+2] & 0xFF) << 8)
                | (source[offset+3] & 0xFF);
    }

    private static void writeHeapBigEndianLong(byte[] target, int offset, long value){
        if(UNSAFE!=null){
            UNSAFE.putLong(target, BYTE_ARRAY_OFFSET+offset, NATIVE_BIG_ENDIAN? value : Long.reverseBytes(value));
            return;
        }
        target[offset]=(byte) (value>>>56);
        target[offset+1]=(byte) (value>>>48);
        target[offset+2]=(byte) (value>>>40);
        target[offset+3]=(byte) (value>>>32);
        target[offset+4]=(byte) (value>>>24);
        target[offset+5]=(byte) (value>>>16);
        target[offset+6]=(byte) (value>>>8);
        target[offset+7]=(byte) value;
    }

    private static long readHeapBigEndianLong(byte[] source, int offset){
        if(UNSAFE!=null){
            long value=UNSAFE.getLong(source, BYTE_ARRAY_OFFSET+offset);
            return NATIVE_BIG_ENDIAN? value : Long.reverseBytes(value);
        }
        return ((long) (source[offset] & 0xFF) << 56)
                | ((long) (source[offset+1] & 0xFF) << 48)
                | ((long) (source[offset+2] & 0xFF) << 40)
                | ((long) (source[offset+3] & 0xFF) << 32)
                | ((long) (source[offset+4] & 0xFF) << 24)
                | ((long) (source[offset+5] & 0xFF) << 16)
                | ((long) (source[offset+6] & 0xFF) << 8)
                | ((long) (source[offset+7] & 0xFF));
    }

    private static void writeHeapLittleEndianShort(byte[] target, int offset, int value){
        short shortValue=(short) value;
        if(UNSAFE!=null){
            UNSAFE.putShort(target, BYTE_ARRAY_OFFSET+offset, NATIVE_BIG_ENDIAN? Short.reverseBytes(shortValue) : shortValue);
            return;
        }
        target[offset]=(byte) shortValue;
        target[offset+1]=(byte) (shortValue>>>8);
    }

    private static int readHeapLittleEndianUnsignedShort(byte[] source, int offset){
        if(UNSAFE!=null){
            short value=UNSAFE.getShort(source, BYTE_ARRAY_OFFSET+offset);
            return (NATIVE_BIG_ENDIAN? Short.reverseBytes(value) : value) & 0xFFFF;
        }
        return (source[offset] & 0xFF) | ((source[offset+1] & 0xFF) << 8);
    }

    private static void writeHeapLittleEndianInt(byte[] target, int offset, int value){
        if(UNSAFE!=null){
            UNSAFE.putInt(target, BYTE_ARRAY_OFFSET+offset, NATIVE_BIG_ENDIAN? Integer.reverseBytes(value) : value);
            return;
        }
        target[offset]=(byte) value;
        target[offset+1]=(byte) (value>>>8);
        target[offset+2]=(byte) (value>>>16);
        target[offset+3]=(byte) (value>>>24);
    }

    private static long readHeapLittleEndianUnsignedInt(byte[] source, int offset){
        if(UNSAFE!=null){
            int value=UNSAFE.getInt(source, BYTE_ARRAY_OFFSET+offset);
            return (NATIVE_BIG_ENDIAN? Integer.reverseBytes(value) : value) & 0xFFFF_FFFFL;
        }
        return (source[offset] & 0xFFL)
                | ((source[offset+1] & 0xFFL) << 8)
                | ((source[offset+2] & 0xFFL) << 16)
                | ((source[offset+3] & 0xFFL) << 24);
    }

    private static void writeHeapLittleEndianLong(byte[] target, int offset, long value){
        if(UNSAFE!=null){
            UNSAFE.putLong(target, BYTE_ARRAY_OFFSET+offset, NATIVE_BIG_ENDIAN? Long.reverseBytes(value) : value);
            return;
        }
        target[offset]=(byte) value;
        target[offset+1]=(byte) (value>>>8);
        target[offset+2]=(byte) (value>>>16);
        target[offset+3]=(byte) (value>>>24);
        target[offset+4]=(byte) (value>>>32);
        target[offset+5]=(byte) (value>>>40);
        target[offset+6]=(byte) (value>>>48);
        target[offset+7]=(byte) (value>>>56);
    }

    private static long readHeapLittleEndianLong(byte[] source, int offset){
        if(UNSAFE!=null){
            long value=UNSAFE.getLong(source, BYTE_ARRAY_OFFSET+offset);
            return NATIVE_BIG_ENDIAN? Long.reverseBytes(value) : value;
        }
        return (source[offset] & 0xFFL)
                | ((source[offset+1] & 0xFFL) << 8)
                | ((source[offset+2] & 0xFFL) << 16)
                | ((source[offset+3] & 0xFFL) << 24)
                | ((source[offset+4] & 0xFFL) << 32)
                | ((source[offset+5] & 0xFFL) << 40)
                | ((source[offset+6] & 0xFFL) << 48)
                | ((source[offset+7] & 0xFFL) << 56);
    }

    private static void writeLittleEndianBytes(ByteCursor c, long bits, int byteCount){
        if(byteCount<=0){
            return;
        }
        if(hasHeapWritableArray(c)){
            ensureHeapWritable(c, byteCount);
            byte[] target=backingArray(c);
            int offset=backingWriteOffset(c);
            switch (byteCount){
                case 1:
                    target[offset]=(byte) bits;
                    break;
                case 2:
                    writeHeapLittleEndianShort(target, offset, (int) bits);
                    break;
                case 4:
                    writeHeapLittleEndianInt(target, offset, (int) bits);
                    break;
                case 8:
                    writeHeapLittleEndianLong(target, offset, bits);
                    break;
                default:
                    for(int i=0;i<byteCount;i++){
                        target[offset+i]=(byte) (bits >>> (i<<3));
                    }
                    break;
            }
            advanceWrite(c, byteCount);
            return;
        }
        for(int i=0;i<byteCount;i++){
            c.writeByte((byte) (bits >>> (i<<3)));
        }
    }

    private static long readLittleEndianBytes(ByteCursor c, int byteCount){
        if(byteCount<=0){
            return 0L;
        }
        if(hasHeapBackingArray(c)){
            byte[] source=backingArray(c);
            int offset=backingOffset(c);
            long value;
            switch (byteCount){
                case 1:
                    value=source[offset] & 0xFFL;
                    break;
                case 2:
                    value=readHeapLittleEndianUnsignedShort(source, offset);
                    break;
                case 4:
                    value=readHeapLittleEndianUnsignedInt(source, offset);
                    break;
                case 8:
                    value=readHeapLittleEndianLong(source, offset);
                    break;
                default:
                    value=0L;
                    for(int i=0;i<byteCount;i++){
                        value |= (long) (source[offset+i] & 0xFF) << (i<<3);
                    }
                    break;
            }
            c.skip(byteCount);
            return value;
        }
        long value=0L;
        for(int i=0;i<byteCount;i++){
            value |= (long) (c.readByte() & 0xFF) << (i<<3);
        }
        return value;
    }

    private static void writeZeroBytes(ByteCursor c, int count){
        while(count>0){
            int chunk=Math.min(count, ZERO_PADDING_BLOCK.length);
            c.writeBytes(ZERO_PADDING_BLOCK, 0, chunk);
            count-=chunk;
        }
    }

    private static boolean tryWriteAsciiString(ByteCursor c, String value){
        int len=value.length();
        for(int i=0;i<len;i++){
            if(value.charAt(i)>0x7F){
                return false;
            }
        }
        writeSize(c, len);
        if(len==0){
            return true;
        }
        if(hasHeapWritableArray(c)){
            ensureHeapWritable(c, len);
            byte[] target=backingArray(c);
            int writeOffset=backingWriteOffset(c);
            for(int i=0;i<len;i++){
                target[writeOffset+i]=(byte) value.charAt(i);
            }
            advanceWrite(c, len);
            return true;
        }
        for(int i=0;i<len;i++){
            c.writeByte((byte) value.charAt(i));
        }
        return true;
    }

    private static final class Utf8Scratch {
        private final CharsetEncoder encoder = StandardCharsets.UTF_8.newEncoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT);
        private byte[] buffer = new byte[256];

        int encode(String value){
            int required=Math.max(1, (int) Math.ceil(value.length()*encoder.maxBytesPerChar()));
            ensureCapacity(required);
            while(true){
                encoder.reset();
                ByteBuffer out=ByteBuffer.wrap(buffer);
                CoderResult result=encoder.encode(CharBuffer.wrap(value), out, true);
                if(result.isOverflow()){
                    grow();
                    continue;
                }
                if(result.isError()){
                    throw codingFailure(result, value);
                }
                CoderResult flush=encoder.flush(out);
                if(flush.isOverflow()){
                    grow();
                    continue;
                }
                if(flush.isError()){
                    throw codingFailure(flush, value);
                }
                return out.position();
            }
        }

        byte[] buffer(){
            return buffer;
        }

        private void ensureCapacity(int required){
            if(buffer.length<required){
                int newCapacity=buffer.length;
                while(newCapacity<required){
                    newCapacity<<=1;
                }
                buffer=Arrays.copyOf(buffer, newCapacity);
            }
        }

        private void grow(){
            ensureCapacity(buffer.length<<1);
        }

        private IllegalStateException codingFailure(CoderResult result, String value){
            try{
                result.throwException();
            }catch (CharacterCodingException e){
                return new IllegalStateException("failed to encode UTF-8 string", e);
            }
            return new IllegalStateException("failed to encode UTF-8 string: "+value);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> ArrayList<T> borrowArrayList(int expectedSize){
        ArrayDeque<ArrayList<?>> pool=ARRAY_LIST_POOL.get();
        ArrayList<T> list=pool.isEmpty()? new ArrayList<>(Math.max(0, expectedSize)) : (ArrayList<T>) pool.pollFirst();
        list.clear();
        list.ensureCapacity(Math.max(0, expectedSize));
        return list;
    }

    public static void recycleArrayList(List<?> list){
        if(!(list instanceof ArrayList) || list.getClass()!=ArrayList.class){
            return;
        }
        @SuppressWarnings("unchecked")
        ArrayList<Object> typed=(ArrayList<Object>) list;
        typed.clear();
        ArrayDeque<ArrayList<?>> pool=ARRAY_LIST_POOL.get();
        if(pool.size()<MAX_POOLED_CONTAINERS){
            pool.addFirst(typed);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> HashSet<T> borrowHashSet(int expectedSize){
        ArrayDeque<HashSet<?>> pool=HASH_SET_POOL.get();
        HashSet<T> set=pool.isEmpty()? new HashSet<>(hashCapacity(expectedSize)) : (HashSet<T>) pool.pollFirst();
        set.clear();
        return set;
    }

    public static void recycleHashSet(Set<?> set){
        if(!(set instanceof HashSet) || set.getClass()!=HashSet.class){
            return;
        }
        @SuppressWarnings("unchecked")
        HashSet<Object> typed=(HashSet<Object>) set;
        typed.clear();
        ArrayDeque<HashSet<?>> pool=HASH_SET_POOL.get();
        if(pool.size()<MAX_POOLED_CONTAINERS){
            pool.addFirst(typed);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> LinkedHashSet<T> borrowLinkedHashSet(int expectedSize){
        ArrayDeque<LinkedHashSet<?>> pool=LINKED_HASH_SET_POOL.get();
        LinkedHashSet<T> set=pool.isEmpty()? new LinkedHashSet<>(hashCapacity(expectedSize)) : (LinkedHashSet<T>) pool.pollFirst();
        set.clear();
        return set;
    }

    public static void recycleLinkedHashSet(Set<?> set){
        if(!(set instanceof LinkedHashSet) || set.getClass()!=LinkedHashSet.class){
            return;
        }
        @SuppressWarnings("unchecked")
        LinkedHashSet<Object> typed=(LinkedHashSet<Object>) set;
        typed.clear();
        ArrayDeque<LinkedHashSet<?>> pool=LINKED_HASH_SET_POOL.get();
        if(pool.size()<MAX_POOLED_CONTAINERS){
            pool.addFirst(typed);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> ArrayDeque<T> borrowArrayDeque(int expectedSize){
        ArrayDeque<ArrayDeque<?>> pool=ARRAY_DEQUE_POOL.get();
        ArrayDeque<T> deque=pool.isEmpty()? new ArrayDeque<>(Math.max(1, expectedSize)) : (ArrayDeque<T>) pool.pollFirst();
        deque.clear();
        return deque;
    }

    public static void recycleArrayDeque(java.util.Queue<?> queue){
        if(!(queue instanceof ArrayDeque) || queue.getClass()!=ArrayDeque.class){
            return;
        }
        @SuppressWarnings("unchecked")
        ArrayDeque<Object> typed=(ArrayDeque<Object>) queue;
        typed.clear();
        ArrayDeque<ArrayDeque<?>> pool=ARRAY_DEQUE_POOL.get();
        if(pool.size()<MAX_POOLED_CONTAINERS){
            pool.addFirst(typed);
        }
    }

    @SuppressWarnings("unchecked")
    public static <K,V> HashMap<K,V> borrowHashMap(int expectedSize){
        ArrayDeque<HashMap<?, ?>> pool=HASH_MAP_POOL.get();
        HashMap<K,V> map=pool.isEmpty()? new HashMap<>(hashCapacity(expectedSize)) : (HashMap<K,V>) pool.pollFirst();
        map.clear();
        return map;
    }

    public static void recycleHashMap(Map<?, ?> map){
        if(!(map instanceof HashMap) || map.getClass()!=HashMap.class){
            return;
        }
        @SuppressWarnings("unchecked")
        HashMap<Object,Object> typed=(HashMap<Object,Object>) map;
        typed.clear();
        ArrayDeque<HashMap<?, ?>> pool=HASH_MAP_POOL.get();
        if(pool.size()<MAX_POOLED_CONTAINERS){
            pool.addFirst(typed);
        }
    }

    @SuppressWarnings("unchecked")
    public static <K,V> LinkedHashMap<K,V> borrowLinkedHashMap(int expectedSize){
        ArrayDeque<LinkedHashMap<?, ?>> pool=LINKED_HASH_MAP_POOL.get();
        LinkedHashMap<K,V> map=pool.isEmpty()? new LinkedHashMap<>(hashCapacity(expectedSize)) : (LinkedHashMap<K,V>) pool.pollFirst();
        map.clear();
        return map;
    }

    public static void recycleLinkedHashMap(Map<?, ?> map){
        if(!(map instanceof LinkedHashMap) || map.getClass()!=LinkedHashMap.class){
            return;
        }
        @SuppressWarnings("unchecked")
        LinkedHashMap<Object,Object> typed=(LinkedHashMap<Object,Object>) map;
        typed.clear();
        ArrayDeque<LinkedHashMap<?, ?>> pool=LINKED_HASH_MAP_POOL.get();
        if(pool.size()<MAX_POOLED_CONTAINERS){
            pool.addFirst(typed);
        }
    }

    public static IntArrayList borrowIntArrayList(int expectedSize){
        ArrayDeque<IntArrayList> pool=INT_ARRAY_LIST_POOL.get();
        IntArrayList list=pool.isEmpty()? new IntArrayList(expectedSize) : pool.pollFirst();
        list.clear();
        list.ensureCapacity(Math.max(0, expectedSize));
        return list;
    }

    public static void recycleIntArrayList(List<?> list){
        if(!(list instanceof IntArrayList)){
            return;
        }
        IntArrayList typed=(IntArrayList) list;
        typed.clear();
        ArrayDeque<IntArrayList> pool=INT_ARRAY_LIST_POOL.get();
        if(pool.size()<MAX_POOLED_CONTAINERS){
            pool.addFirst(typed);
        }
    }

    public static LongArrayList borrowLongArrayList(int expectedSize){
        ArrayDeque<LongArrayList> pool=LONG_ARRAY_LIST_POOL.get();
        LongArrayList list=pool.isEmpty()? new LongArrayList(expectedSize) : pool.pollFirst();
        list.clear();
        list.ensureCapacity(Math.max(0, expectedSize));
        return list;
    }

    public static void recycleLongArrayList(List<?> list){
        if(!(list instanceof LongArrayList)){
            return;
        }
        LongArrayList typed=(LongArrayList) list;
        typed.clear();
        ArrayDeque<LongArrayList> pool=LONG_ARRAY_LIST_POOL.get();
        if(pool.size()<MAX_POOLED_CONTAINERS){
            pool.addFirst(typed);
        }
    }

    @SuppressWarnings("unchecked")
    public static <V> IntObjectHashMap<V> borrowIntObjectHashMap(int expectedSize){
        ArrayDeque<IntObjectHashMap<?>> pool=INT_OBJECT_HASH_MAP_POOL.get();
        IntObjectHashMap<V> map=pool.isEmpty()? new IntObjectHashMap<>(expectedSize) : (IntObjectHashMap<V>) pool.pollFirst();
        map.clear();
        return map;
    }

    public static void recycleIntObjectHashMap(Map<?, ?> map){
        if(!(map instanceof IntObjectHashMap)){
            return;
        }
        IntObjectHashMap<?> typed=(IntObjectHashMap<?>) map;
        typed.clear();
        ArrayDeque<IntObjectHashMap<?>> pool=INT_OBJECT_HASH_MAP_POOL.get();
        if(pool.size()<MAX_POOLED_CONTAINERS){
            pool.addFirst(typed);
        }
    }

    public static IntIntHashMap borrowIntIntHashMap(int expectedSize){
        ArrayDeque<IntIntHashMap> pool=INT_INT_HASH_MAP_POOL.get();
        IntIntHashMap map=pool.isEmpty()? new IntIntHashMap(expectedSize) : pool.pollFirst();
        map.clear();
        return map;
    }

    public static void recycleIntIntHashMap(Map<?, ?> map){
        if(!(map instanceof IntIntHashMap)){
            return;
        }
        IntIntHashMap typed=(IntIntHashMap) map;
        typed.clear();
        ArrayDeque<IntIntHashMap> pool=INT_INT_HASH_MAP_POOL.get();
        if(pool.size()<MAX_POOLED_CONTAINERS){
            pool.addFirst(typed);
        }
    }

    public static IntLongHashMap borrowIntLongHashMap(int expectedSize){
        ArrayDeque<IntLongHashMap> pool=INT_LONG_HASH_MAP_POOL.get();
        IntLongHashMap map=pool.isEmpty()? new IntLongHashMap(expectedSize) : pool.pollFirst();
        map.clear();
        return map;
    }

    public static void recycleIntLongHashMap(Map<?, ?> map){
        if(!(map instanceof IntLongHashMap)){
            return;
        }
        IntLongHashMap typed=(IntLongHashMap) map;
        typed.clear();
        ArrayDeque<IntLongHashMap> pool=INT_LONG_HASH_MAP_POOL.get();
        if(pool.size()<MAX_POOLED_CONTAINERS){
            pool.addFirst(typed);
        }
    }

    public static void writeInt(ByteBuf c,int v){ BufUtil.writeInt(c,v); }
    public static int readInt(ByteBuf c){ return BufUtil.readInt(c); }
    public static void writeLong(ByteBuf c,long v){ BufUtil.writeLong(c,v); }
    public static long readLong(ByteBuf c){ return BufUtil.readLong(c); }
    public static void writeUInt(ByteBuf c,int v){ BufUtil.writeUInt(c,v); }
    public static int readUInt(ByteBuf c){ return BufUtil.readUInt(c); }
    public static void writeULong(ByteBuf c,long v){ BufUtil.writeULong(c,v); }
    public static long readULong(ByteBuf c){ return BufUtil.readULong(c); }
    public static void writeSize(ByteBuf c,int size){ BufUtil.writeSize(c,size); }
    public static int readSize(ByteBuf c){ return BufUtil.readSize(c); }
    public static void writeByte(ByteBuf c,byte v){ BufUtil.writeByte(c,v); }
    public static byte readByte(ByteBuf c){ return BufUtil.readByte(c); }
    public static void writeShort(ByteBuf c,short v){ BufUtil.writeShort(c,v); }
    public static short readShort(ByteBuf c){ return BufUtil.readShort(c); }
    public static void writeBoolean(ByteBuf c,boolean v){ BufUtil.writeBoolean(c,v); }
    public static boolean readBoolean(ByteBuf c){ return BufUtil.readBoolean(c); }
    public static void writeChar(ByteBuf c,char v){ BufUtil.writeChar(c,v); }
    public static char readChar(ByteBuf c){ return BufUtil.readChar(c); }
    public static void writeFloat(ByteBuf c,float v){ BufUtil.writeFloat(c,v); }
    public static float readFloat(ByteBuf c){ return BufUtil.readFloat(c); }
    public static void writeDouble(ByteBuf c,double v){ BufUtil.writeDouble(c,v); }
    public static double readDouble(ByteBuf c){ return BufUtil.readDouble(c); }
    public static void writeString(ByteBuf c,String s){ BufUtil.writeString(c,s); }
    public static String readString(ByteBuf c){ return BufUtil.readString(c); }
    public static void writeBytes(ByteBuf c, byte[] arr){ BufUtil.writeBytes(c, arr); }
    public static byte[] readBytes(ByteBuf c){ return BufUtil.readBytes(c); }
    public static void writeBorrowedBytes(ByteBuf c, BorrowedBytes value){
        BorrowedBytes borrowed=value==null?BorrowedBytes.empty():value;
        writeSize(c, borrowed.length());
        if(borrowed.length()!=0){
            c.writeBytes(borrowed.unsafeArray(), borrowed.unsafeOffset(), borrowed.length());
        }
    }
    public static BorrowedBytes readBorrowedBytes(ByteBuf c){
        int n=readSize(c);
        if(n==0){
            return BorrowedBytes.empty();
        }
        if(c.hasArray()){
            int readerIndex=c.readerIndex();
            BorrowedBytes borrowed=BorrowedBytes.wrap(c.array(), c.arrayOffset()+readerIndex, n);
            c.readerIndex(readerIndex+n);
            return borrowed;
        }
        byte[] bytes=new byte[n];
        c.readBytes(bytes);
        return BorrowedBytes.copyOf(bytes);
    }
    public static byte[] readSampledBytes(ByteBuf c, byte[] reuse, int totalCount, int[] sampleIndices){
        if(totalCount<=0){
            return EMPTY_BYTES;
        }
        if(sampleIndices==null || sampleIndices.length==0){
            c.skipBytes(totalCount);
            return EMPTY_BYTES;
        }
        int selectedCount=0;
        int lastAccepted=-1;
        for(int sampleIndex: sampleIndices){
            if(sampleIndex>=0 && sampleIndex<totalCount && sampleIndex>lastAccepted){
                selectedCount++;
                lastAccepted=sampleIndex;
            }
        }
        if(selectedCount==0){
            c.skipBytes(totalCount);
            return EMPTY_BYTES;
        }
        byte[] out=reuse!=null && reuse.length==selectedCount ? reuse : new byte[selectedCount];
        if(c.hasArray()){
            int baseOffset=c.arrayOffset()+c.readerIndex();
            int outIndex=0;
            int lastIndex=-1;
            byte[] source=c.array();
            for(int sampleIndex: sampleIndices){
                if(sampleIndex>=0 && sampleIndex<totalCount && sampleIndex>lastIndex){
                    out[outIndex++]=source[baseOffset+sampleIndex];
                    lastIndex=sampleIndex;
                }
            }
            c.readerIndex(c.readerIndex()+totalCount);
            return out;
        }
        int cursorIndex=0;
        int outIndex=0;
        int lastIndex=-1;
        for(int sampleIndex: sampleIndices){
            if(sampleIndex<0 || sampleIndex>=totalCount || sampleIndex<=lastIndex){
                continue;
            }
            int delta=sampleIndex-cursorIndex;
            if(delta>0){
                c.skipBytes(delta);
            }
            out[outIndex++]=c.readByte();
            cursorIndex=sampleIndex+1;
            lastIndex=sampleIndex;
        }
        if(totalCount>cursorIndex){
            c.skipBytes(totalCount-cursorIndex);
        }
        return out;
    }
    public static BorrowedBytes readBorrowedBytes(ByteBuf c, int totalCount, int readCount){
        if(totalCount<=0 || readCount<=0){
            if(totalCount>0){
                c.skipBytes(totalCount);
            }
            return BorrowedBytes.empty();
        }
        int actualCount=Math.min(totalCount, readCount);
        if(c.hasArray()){
            int readerIndex=c.readerIndex();
            BorrowedBytes borrowed=BorrowedBytes.wrap(c.array(), c.arrayOffset()+readerIndex, actualCount);
            c.readerIndex(readerIndex+totalCount);
            return borrowed;
        }
        byte[] bytes=new byte[actualCount];
        c.readBytes(bytes);
        if(totalCount>actualCount){
            c.skipBytes(totalCount-actualCount);
        }
        return BorrowedBytes.copyOf(bytes);
    }
    public static BorrowedBytes readSampledBorrowedBytes(ByteBuf c, int totalCount, int[] sampleIndices){
        return BorrowedBytes.copyOf(readSampledBytes(c, null, totalCount, sampleIndices));
    }
    public static void writeBorrowedString(ByteBuf c, BorrowedString value){
        BorrowedString borrowed=value==null?BorrowedString.empty():value;
        writeSize(c, borrowed.byteLength());
        if(borrowed.byteLength()!=0){
            BorrowedBytes bytes=borrowed.bytes();
            c.writeBytes(bytes.unsafeArray(), bytes.unsafeOffset(), bytes.length());
        }
    }
    public static BorrowedString readBorrowedString(ByteBuf c){
        int n=readSize(c);
        if(n==0){
            return BorrowedString.empty();
        }
        if(c.hasArray()){
            int readerIndex=c.readerIndex();
            BorrowedString borrowed=BorrowedString.wrap(c.array(), c.arrayOffset()+readerIndex, n);
            c.readerIndex(readerIndex+n);
            return borrowed;
        }
        byte[] bytes=new byte[n];
        c.readBytes(bytes);
        return BorrowedString.wrap(bytes, 0, n);
    }
    public static void writeBorrowedFixedString(ByteBuf c, BorrowedString value, int fixedLength){
        if(fixedLength<0){
            throw new IllegalArgumentException("fixedLength must be >= 0");
        }
        BorrowedString borrowed=value==null?BorrowedString.empty():value;
        if(borrowed.byteLength()>fixedLength){
            throw new IllegalArgumentException("string encoded length "+borrowed.byteLength()+" exceeds fixedLength "+fixedLength);
        }
        if(borrowed.byteLength()!=0){
            BorrowedBytes bytes=borrowed.bytes();
            c.writeBytes(bytes.unsafeArray(), bytes.unsafeOffset(), bytes.length());
        }
        int padding=fixedLength-borrowed.byteLength();
        if(padding>0){
            c.writeZero(padding);
        }
    }
    public static void writeFixedString(ByteBuf c, String value, int fixedLength){
        if(fixedLength<0){
            throw new IllegalArgumentException("fixedLength must be >= 0");
        }
        byte[] bytes=value==null?EMPTY_BYTES:value.getBytes(StandardCharsets.UTF_8);
        if(bytes.length>fixedLength){
            throw new IllegalArgumentException("string encoded length "+bytes.length+" exceeds fixedLength "+fixedLength);
        }
        c.writeBytes(bytes);
        if(bytes.length<fixedLength){
            c.writeZero(fixedLength-bytes.length);
        }
    }
    public static String readFixedString(ByteBuf c, int fixedLength){
        return readBorrowedFixedString(c, fixedLength).toString();
    }
    public static BorrowedString readBorrowedFixedString(ByteBuf c, int fixedLength){
        if(fixedLength<=0){
            return BorrowedString.empty();
        }
        if(c.hasArray()){
            int readerIndex=c.readerIndex();
            int actualLength=trimTrailingZeros(c.array(), c.arrayOffset()+readerIndex, fixedLength);
            BorrowedString borrowed=BorrowedString.wrap(c.array(), c.arrayOffset()+readerIndex, actualLength);
            c.readerIndex(readerIndex+fixedLength);
            return borrowed;
        }
        byte[] bytes=new byte[fixedLength];
        c.readBytes(bytes);
        return BorrowedString.wrap(bytes, 0, trimTrailingZeros(bytes, 0, fixedLength));
    }
    public static void writeIntArray(ByteBuf c,int[] arr){ BufUtil.writeIntArray(c, arr); }
    public static int[] readIntArray(ByteBuf c){ return BufUtil.readIntArray(c); }
    public static void writeLongArray(ByteBuf c,long[] arr){ BufUtil.writeLongArray(c, arr); }
    public static long[] readLongArray(ByteBuf c){ return BufUtil.readLongArray(c); }
    public static void writeShortArray(ByteBuf c,short[] arr){ BufUtil.writeShortArray(c, arr); }
    public static short[] readShortArray(ByteBuf c){ return BufUtil.readShortArray(c); }
    public static void writeBooleanArray(ByteBuf c,boolean[] arr){ BufUtil.writeBooleanArray(c, arr); }
    public static boolean[] readBooleanArray(ByteBuf c){ return BufUtil.readBooleanArray(c); }
    public static void writeCharArray(ByteBuf c,char[] arr){ BufUtil.writeCharArray(c, arr); }
    public static char[] readCharArray(ByteBuf c){ return BufUtil.readCharArray(c); }
    public static void writeFloatArray(ByteBuf c,float[] arr){ BufUtil.writeFloatArray(c, arr); }
    public static float[] readFloatArray(ByteBuf c){ return BufUtil.readFloatArray(c); }
    public static void writeDoubleArray(ByteBuf c,double[] arr){ BufUtil.writeDoubleArray(c, arr); }
    public static double[] readDoubleArray(ByteBuf c){ return BufUtil.readDoubleArray(c); }
    public static void writePresenceBits(ByteBuf c, long bits, int fieldCount){ BufUtil.writePresenceBits(c, bits, fieldCount); }
    public static long readPresenceBits(ByteBuf c, int fieldCount){ return BufUtil.readPresenceBits(c, fieldCount); }
    public static void writePresenceBits(ByteBuf c, long[] words, int fieldCount){ BufUtil.writePresenceBits(c, words, fieldCount); }
    public static long[] readPresenceWords(ByteBuf c, int fieldCount){ return BufUtil.readPresenceWords(c, fieldCount); }
    public static <T> void writeOptional(ByteBuf c, Optional<T> optional, BiConsumer<ByteBuf,T> writer){ BufUtil.writeOptional(c, optional, writer); }
    public static <T> Optional<T> readOptional(ByteBuf c, Function<ByteBuf,T> reader){ return BufUtil.readOptional(c, reader); }
    public static <T> void writeCollection(ByteBuf c, Collection<T> values, BiConsumer<ByteBuf,T> elemWriter){ BufUtil.writeCollection(c, values, elemWriter); }
    public static <T, C extends Collection<T>> C readCollection(ByteBuf c, IntFunction<C> creator, Function<ByteBuf,T> elemReader){ return BufUtil.readCollection(c, creator, elemReader); }
    public static <T> void writeList(ByteBuf c, List<T> list, BiConsumer<ByteBuf,T> elemWriter){ BufUtil.writeList(c, list, elemWriter); }
    public static <T, L extends List<T>> L readList(ByteBuf c, IntFunction<L> creator, Function<ByteBuf,T> elemReader){ return BufUtil.readList(c, creator, elemReader); }
    public static <T> List<T> readList(ByteBuf c, Function<ByteBuf,T> elemReader){ return BufUtil.readList(c, elemReader); }
    public static <T> void writeSet(ByteBuf c, Set<T> set, BiConsumer<ByteBuf,T> elemWriter){ BufUtil.writeSet(c, set, elemWriter); }
    public static <T, S extends Set<T>> S readSet(ByteBuf c, IntFunction<S> creator, Function<ByteBuf,T> elemReader){ return BufUtil.readSet(c, creator, elemReader); }
    public static <T> Set<T> readSet(ByteBuf c, Function<ByteBuf,T> elemReader){ return BufUtil.readSet(c, elemReader); }
    public static <T> LinkedHashSet<T> readLinkedSet(ByteBuf c, Function<ByteBuf,T> elemReader){ return BufUtil.readLinkedSet(c, elemReader); }
    public static <K,V> void writeMap(ByteBuf c, Map<K,V> map, BiConsumer<ByteBuf,K> keyWriter, BiConsumer<ByteBuf,V> valueWriter){ BufUtil.writeMap(c, map, keyWriter, valueWriter); }
    public static <K,V,M extends Map<K,V>> M readMap(ByteBuf c, IntFunction<M> creator, Function<ByteBuf,K> keyReader, Function<ByteBuf,V> valueReader){ return BufUtil.readMap(c, creator, keyReader, valueReader); }
    public static <K,V> Map<K,V> readMap(ByteBuf c, Function<ByteBuf,K> keyReader, Function<ByteBuf,V> valueReader){ return BufUtil.readMap(c, keyReader, valueReader); }
    public static <K,V> LinkedHashMap<K,V> readLinkedMap(ByteBuf c, Function<ByteBuf,K> keyReader, Function<ByteBuf,V> valueReader){ return BufUtil.readLinkedMap(c, keyReader, valueReader); }
    public static <T> void writeObjectArray(ByteBuf c, T[] arr, BiConsumer<ByteBuf,T> writer){ BufUtil.writeObjectArray(c, arr, writer); }
    public static <T> T[] readObjectArray(ByteBuf c, IntFunction<T[]> creator, Function<ByteBuf,T> reader){ return BufUtil.readObjectArray(c, creator, reader); }
    public static void writeFixedInt(ByteBuf c,int v){ c.writeInt(v); }
    public static int readFixedInt(ByteBuf c){ return c.readInt(); }
    public static void writeFixedLong(ByteBuf c,long v){ c.writeLong(v); }
    public static long readFixedLong(ByteBuf c){ return c.readLong(); }
    public static void writeFixedShort(ByteBuf c,short v){ c.writeShort(v); }
    public static short readFixedShort(ByteBuf c){ return c.readShort(); }
    public static void writeFixedChar(ByteBuf c,char v){ c.writeShort(v); }
    public static char readFixedChar(ByteBuf c){ return (char) c.readUnsignedShort(); }
    public static void writeFixedIntArray(ByteBuf c,int[] arr){
        int[] values=arr==null? new int[0]: arr;
        writeSize(c, values.length);
        if(values.length!=0){
            writeRawIntArray(c, values, values.length);
        }
    }
    public static void writeFixedCountBytes(ByteBuf c, byte[] arr, int fixedCount){
        byte[] values=arr==null? EMPTY_BYTES:arr;
        if(values.length>fixedCount){
            throw new IllegalArgumentException("array length "+values.length+" exceeds fixedCount "+fixedCount);
        }
        c.writeBytes(values);
        if(values.length<fixedCount){
            c.writeZero(fixedCount-values.length);
        }
    }
    public static byte[] readFixedCountBytes(ByteBuf c, int fixedCount){
        if(fixedCount<=0){
            return EMPTY_BYTES;
        }
        byte[] values=new byte[fixedCount];
        c.readBytes(values);
        return values;
    }
    public static void writeBorrowedFixedBytes(ByteBuf c, BorrowedBytes value, int fixedCount){
        BorrowedBytes borrowed=value==null?BorrowedBytes.empty():value;
        if(borrowed.length()>fixedCount){
            throw new IllegalArgumentException("array length "+borrowed.length()+" exceeds fixedCount "+fixedCount);
        }
        if(borrowed.length()!=0){
            c.writeBytes(borrowed.unsafeArray(), borrowed.unsafeOffset(), borrowed.length());
        }
        if(borrowed.length()<fixedCount){
            c.writeZero(fixedCount-borrowed.length());
        }
    }
    public static BorrowedBytes readBorrowedFixedBytes(ByteBuf c, int fixedCount){
        if(fixedCount<=0){
            return BorrowedBytes.empty();
        }
        if(c.hasArray()){
            int readerIndex=c.readerIndex();
            BorrowedBytes borrowed=BorrowedBytes.wrap(c.array(), c.arrayOffset()+readerIndex, fixedCount);
            c.readerIndex(readerIndex+fixedCount);
            return borrowed;
        }
        byte[] values=readFixedCountBytes(c, fixedCount);
        return BorrowedBytes.copyOf(values);
    }
    public static void writeFixedCountIntArray(ByteBuf c, int[] arr, int fixedCount){
        int[] values=arr==null? new int[0]:arr;
        if(values.length>fixedCount){
            throw new IllegalArgumentException("array length "+values.length+" exceeds fixedCount "+fixedCount);
        }
        if(values.length!=0){
            writeRawIntArray(c, values, values.length);
        }
        if(values.length<fixedCount){
            c.writeZero((fixedCount-values.length)<<2);
        }
    }
    public static int[] readFixedCountIntArray(ByteBuf c, int fixedCount){
        if(fixedCount<=0){
            return new int[0];
        }
        int[] values=new int[fixedCount];
        for(int i=0;i<fixedCount;i++) values[i]=c.readInt();
        return values;
    }
    public static IntArrayView readBorrowedFixedIntArray(ByteBuf c, int fixedCount){
        if(fixedCount<=0){
            return IntArrayView.empty();
        }
        int byteCount=fixedCount<<2;
        if(c.hasArray()){
            int readerIndex=c.readerIndex();
            IntArrayView view=IntArrayView.wrap(c.array(), c.arrayOffset()+readerIndex, fixedCount);
            c.readerIndex(readerIndex+byteCount);
            return view;
        }
        int[] values=readFixedCountIntArray(c, fixedCount);
        return IntArrayView.of(values);
    }
    public static void writeFixedCountLongArray(ByteBuf c, long[] arr, int fixedCount){
        long[] values=arr==null? new long[0]:arr;
        if(values.length>fixedCount){
            throw new IllegalArgumentException("array length "+values.length+" exceeds fixedCount "+fixedCount);
        }
        if(values.length!=0){
            writeRawLongArray(c, values, values.length);
        }
        if(values.length<fixedCount){
            c.writeZero((fixedCount-values.length)<<3);
        }
    }
    public static long[] readFixedCountLongArray(ByteBuf c, int fixedCount){
        if(fixedCount<=0){
            return new long[0];
        }
        long[] values=new long[fixedCount];
        for(int i=0;i<fixedCount;i++) values[i]=c.readLong();
        return values;
    }
    public static LongArrayView readBorrowedFixedLongArray(ByteBuf c, int fixedCount){
        if(fixedCount<=0){
            return LongArrayView.empty();
        }
        int byteCount=fixedCount<<3;
        if(c.hasArray()){
            int readerIndex=c.readerIndex();
            LongArrayView view=LongArrayView.wrap(c.array(), c.arrayOffset()+readerIndex, fixedCount);
            c.readerIndex(readerIndex+byteCount);
            return view;
        }
        long[] values=readFixedCountLongArray(c, fixedCount);
        return LongArrayView.of(values);
    }
    public static void writeFixedCountFloatArray(ByteBuf c, float[] arr, int fixedCount){
        float[] values=arr==null? new float[0]:arr;
        if(values.length>fixedCount){
            throw new IllegalArgumentException("array length "+values.length+" exceeds fixedCount "+fixedCount);
        }
        if(values.length!=0){
            writeRawFloatArray(c, values, values.length);
        }
        if(values.length<fixedCount){
            c.writeZero((fixedCount-values.length)<<2);
        }
    }
    public static float[] readFixedCountFloatArray(ByteBuf c, int fixedCount){
        if(fixedCount<=0){
            return new float[0];
        }
        float[] values=new float[fixedCount];
        for(int i=0;i<fixedCount;i++) values[i]=Float.intBitsToFloat(c.readInt());
        return values;
    }
    public static FloatArrayView readBorrowedFixedFloatArray(ByteBuf c, int fixedCount){
        if(fixedCount<=0){
            return FloatArrayView.empty();
        }
        int byteCount=fixedCount<<2;
        if(c.hasArray()){
            int readerIndex=c.readerIndex();
            FloatArrayView view=FloatArrayView.wrap(c.array(), c.arrayOffset()+readerIndex, fixedCount);
            c.readerIndex(readerIndex+byteCount);
            return view;
        }
        float[] values=readFixedCountFloatArray(c, fixedCount);
        return FloatArrayView.of(values);
    }
    public static void writeFixedCountDoubleArray(ByteBuf c, double[] arr, int fixedCount){
        double[] values=arr==null? new double[0]:arr;
        if(values.length>fixedCount){
            throw new IllegalArgumentException("array length "+values.length+" exceeds fixedCount "+fixedCount);
        }
        if(values.length!=0){
            writeRawDoubleArray(c, values, values.length);
        }
        if(values.length<fixedCount){
            c.writeZero((fixedCount-values.length)<<3);
        }
    }
    public static double[] readFixedCountDoubleArray(ByteBuf c, int fixedCount){
        if(fixedCount<=0){
            return new double[0];
        }
        double[] values=new double[fixedCount];
        for(int i=0;i<fixedCount;i++) values[i]=Double.longBitsToDouble(c.readLong());
        return values;
    }
    public static DoubleArrayView readBorrowedFixedDoubleArray(ByteBuf c, int fixedCount){
        if(fixedCount<=0){
            return DoubleArrayView.empty();
        }
        int byteCount=fixedCount<<3;
        if(c.hasArray()){
            int readerIndex=c.readerIndex();
            DoubleArrayView view=DoubleArrayView.wrap(c.array(), c.arrayOffset()+readerIndex, fixedCount);
            c.readerIndex(readerIndex+byteCount);
            return view;
        }
        double[] values=readFixedCountDoubleArray(c, fixedCount);
        return DoubleArrayView.of(values);
    }
    public static int[] readFixedIntArray(ByteBuf c){
        int n=readSize(c);
        int[] values=new int[n];
        readRawIntArray(c, values, n);
        return values;
    }
    public static void writeFixedLongArray(ByteBuf c,long[] arr){
        long[] values=arr==null? new long[0]: arr;
        writeSize(c, values.length);
        if(values.length!=0){
            writeRawLongArray(c, values, values.length);
        }
    }
    public static long[] readFixedLongArray(ByteBuf c){
        int n=readSize(c);
        long[] values=new long[n];
        readRawLongArray(c, values, n);
        return values;
    }
    public static void writeFixedShortArray(ByteBuf c,short[] arr){
        short[] values=arr==null? new short[0]: arr;
        writeSize(c, values.length);
        for(short value: values) c.writeShort(value);
    }
    public static short[] readFixedShortArray(ByteBuf c){
        int n=readSize(c);
        short[] values=new short[n];
        for(int i=0;i<n;i++) values[i]=c.readShort();
        return values;
    }
    public static void writeFixedCharArray(ByteBuf c,char[] arr){
        char[] values=arr==null? new char[0]: arr;
        writeSize(c, values.length);
        for(char value: values) c.writeShort(value);
    }
    public static char[] readFixedCharArray(ByteBuf c){
        int n=readSize(c);
        char[] values=new char[n];
        for(int i=0;i<n;i++) values[i]=(char) c.readUnsignedShort();
        return values;
    }
    public static void writeFixedFloatArray(ByteBuf c,float[] arr){
        float[] values=arr==null? new float[0]: arr;
        writeSize(c, values.length);
        if(values.length!=0){
            writeRawFloatArray(c, values, values.length);
        }
    }
    public static float[] readFixedFloatArray(ByteBuf c){
        int n=readSize(c);
        float[] values=new float[n];
        readRawFloatArray(c, values, n);
        return values;
    }
    public static void writeFixedDoubleArray(ByteBuf c,double[] arr){
        double[] values=arr==null? new double[0]: arr;
        writeSize(c, values.length);
        if(values.length!=0){
            writeRawDoubleArray(c, values, values.length);
        }
    }
    public static double[] readFixedDoubleArray(ByteBuf c){
        int n=readSize(c);
        double[] values=new double[n];
        readRawDoubleArray(c, values, n);
        return values;
    }
    public static void readRawByteArray(ByteBuf c, byte[] arr, int count){
        if(count<=0){
            return;
        }
        c.readBytes(arr, 0, count);
    }
    public static void writeRawIntArray(ByteBuf c, int[] arr, int count){
        if(count<=0){
            return;
        }
        if(c.hasArray() && UNSAFE!=null){
            int byteCount=count<<2;
            c.ensureWritable(byteCount);
            byte[] target=c.array();
            long offset=BYTE_ARRAY_OFFSET+c.arrayOffset()+c.writerIndex();
            if(NATIVE_BIG_ENDIAN){
                for(int i=0;i<count;i++){
                    UNSAFE.putInt(target, offset, arr[i]);
                    offset+=4;
                }
            }else{
                for(int i=0;i<count;i++){
                    UNSAFE.putInt(target, offset, Integer.reverseBytes(arr[i]));
                    offset+=4;
                }
            }
            c.writerIndex(c.writerIndex()+byteCount);
            return;
        }
        for(int i=0;i<count;i++){
            c.writeInt(arr[i]);
        }
    }
    public static void readRawIntArray(ByteBuf c, int[] arr, int count){
        if(count<=0){
            return;
        }
        if(c.hasArray() && UNSAFE!=null){
            byte[] source=c.array();
            long offset=BYTE_ARRAY_OFFSET+c.arrayOffset()+c.readerIndex();
            for(int i=0;i<count;i++){
                int value=UNSAFE.getInt(source, offset);
                arr[i]=NATIVE_BIG_ENDIAN? value : Integer.reverseBytes(value);
                offset+=4;
            }
            c.readerIndex(c.readerIndex()+(count<<2));
            return;
        }
        for(int i=0;i<count;i++) arr[i]=c.readInt();
    }
    public static IntArrayView readBorrowedRawIntArray(ByteBuf c){
        int count=readSize(c);
        if(count==0){
            return IntArrayView.empty();
        }
        int byteCount=count<<2;
        if(c.hasArray()){
            int readerIndex=c.readerIndex();
            IntArrayView view=IntArrayView.wrap(c.array(), c.arrayOffset()+readerIndex, count);
            c.readerIndex(readerIndex+byteCount);
            return view;
        }
        int[] values=new int[count];
        readRawIntArray(c, values, count);
        return IntArrayView.of(values);
    }
    public static IntArrayView readBorrowedRawIntArray(ByteBuf c, int totalCount, int readCount){
        if(totalCount<=0 || readCount<=0){
            if(totalCount>0){
                c.skipBytes(totalCount<<2);
            }
            return IntArrayView.empty();
        }
        int actualCount=Math.min(totalCount, readCount);
        int totalBytes=totalCount<<2;
        if(c.hasArray()){
            int readerIndex=c.readerIndex();
            IntArrayView view=IntArrayView.wrap(c.array(), c.arrayOffset()+readerIndex, actualCount);
            c.readerIndex(readerIndex+totalBytes);
            return view;
        }
        int[] values=new int[actualCount];
        readRawIntArray(c, values, actualCount);
        if(totalCount>actualCount){
            c.skipBytes((totalCount-actualCount)<<2);
        }
        return IntArrayView.of(values);
    }
    public static void writeBorrowedRawIntArray(ByteBuf c, IntArrayView value){
        IntArrayView view=value==null?IntArrayView.empty():value;
        int count=view.count();
        writeSize(c, count);
        if(count==0){
            return;
        }
        byte[] borrowed=view.borrowedArray();
        if(borrowed!=null){
            c.writeBytes(borrowed, view.borrowedOffset(), count<<2);
            return;
        }
        int[] materialized=view.materializedArray();
        if(materialized!=null){
            writeRawIntArray(c, materialized, count);
            return;
        }
        for(int i=0;i<count;i++){
            c.writeInt(view.getInt(i));
        }
    }
    public static void writeBorrowedFixedIntArray(ByteBuf c, IntArrayView value, int fixedCount){
        IntArrayView view=value==null?IntArrayView.empty():value;
        int count=view.count();
        if(count>fixedCount){
            throw new IllegalArgumentException("array length "+count+" exceeds fixedCount "+fixedCount);
        }
        if(count!=0){
            byte[] borrowed=view.borrowedArray();
            if(borrowed!=null){
                c.writeBytes(borrowed, view.borrowedOffset(), count<<2);
            }else{
                int[] materialized=view.materializedArray();
                if(materialized!=null){
                    writeRawIntArray(c, materialized, count);
                }else{
                    for(int i=0;i<count;i++){
                        c.writeInt(view.getInt(i));
                    }
                }
            }
        }
        if(count<fixedCount){
            c.writeZero((fixedCount-count)<<2);
        }
    }
    public static IntArrayList readPackedIntList(ByteBuf c){
        return readPackedIntList(c, null);
    }
    public static IntArrayList readPackedIntList(ByteBuf c, IntArrayList reuse){
        int count=readSize(c);
        IntArrayList list=reuse==null?borrowIntArrayList(count):reuse;
        list.ensureCapacity(count);
        list.resize(count);
        readRawIntArray(c, list.rawArray(), count);
        return list;
    }
    public static void writePackedIntList(ByteBuf c, List<Integer> list){
        int count=list==null?0:list.size();
        writeSize(c, count);
        if(count==0){
            return;
        }
        if(list instanceof IntArrayList){
            IntArrayList typed=(IntArrayList) list;
            writeRawIntArray(c, typed.rawArray(), count);
            return;
        }
        if(list instanceof RandomAccess){
            for(int i=0;i<count;i++){
                Integer value=list.get(i);
                c.writeInt(value==null?0:value);
            }
            return;
        }
        for(Integer value: list){
            c.writeInt(value==null?0:value);
        }
    }
    public static <V> IntObjectHashMap<V> readPackedIntObjectMapFast(ByteBuf c, PackedIntObjectByteBufReader<V> valueReader){
        return readPackedIntObjectMapFast(c, null, valueReader);
    }
    public static <V> IntObjectHashMap<V> readPackedIntObjectMapFast(ByteBuf c, IntObjectHashMap<V> reuse, PackedIntObjectByteBufReader<V> valueReader){
        int count=readSize(c);
        IntObjectHashMap<V> map=reuse==null?borrowIntObjectHashMap(count):reuse;
        map.clear();
        map.ensureCapacity(count);
        for(int i=0;i<count;i++){
            map.putInt(c.readInt(), valueReader.read(c));
        }
        return map;
    }
    public static <V> IntObjectHashMap<V> readPackedIntObjectMap(ByteBuf c, Function<ByteBuf, V> valueReader){
        return readPackedIntObjectMapFast(c, valueReader::apply);
    }
    public static <V> IntObjectHashMap<V> readPackedIntObjectMap(ByteBuf c, IntObjectHashMap<V> reuse, Function<ByteBuf, V> valueReader){
        return readPackedIntObjectMapFast(c, reuse, valueReader::apply);
    }
    public static IntIntHashMap readPackedIntIntMap(ByteBuf c){
        return readPackedIntIntMap(c, null);
    }
    public static IntIntHashMap readPackedIntIntMap(ByteBuf c, IntIntHashMap reuse){
        int count=readSize(c);
        IntIntHashMap map=reuse==null?borrowIntIntHashMap(count):reuse;
        map.clear();
        map.ensureCapacity(count);
        for(int i=0;i<count;i++){
            map.putInt(c.readInt(), c.readInt());
        }
        return map;
    }
    public static void writePackedIntIntMap(ByteBuf c, Map<Integer, Integer> map){
        int count=map==null?0:map.size();
        writeSize(c, count);
        if(count==0){
            return;
        }
        if(map instanceof IntIntHashMap){
            IntIntHashMap.Cursor cursor=((IntIntHashMap) map).cursor();
            while(cursor.advance()){
                c.writeInt(cursor.key());
                c.writeInt(cursor.valueInt());
            }
            return;
        }
        for(Map.Entry<Integer, Integer> entry: map.entrySet()){
            Integer key=entry.getKey();
            Integer value=entry.getValue();
            c.writeInt(key==null?0:key);
            c.writeInt(value==null?0:value);
        }
    }
    public static IntLongHashMap readPackedIntLongMap(ByteBuf c){
        return readPackedIntLongMap(c, null);
    }
    public static IntLongHashMap readPackedIntLongMap(ByteBuf c, IntLongHashMap reuse){
        int count=readSize(c);
        IntLongHashMap map=reuse==null?borrowIntLongHashMap(count):reuse;
        map.clear();
        map.ensureCapacity(count);
        for(int i=0;i<count;i++){
            map.putLong(c.readInt(), c.readLong());
        }
        return map;
    }
    public static void writePackedIntLongMap(ByteBuf c, Map<Integer, Long> map){
        int count=map==null?0:map.size();
        writeSize(c, count);
        if(count==0){
            return;
        }
        if(map instanceof IntLongHashMap){
            IntLongHashMap.Cursor cursor=((IntLongHashMap) map).cursor();
            while(cursor.advance()){
                c.writeInt(cursor.key());
                c.writeLong(cursor.valueLong());
            }
            return;
        }
        for(Map.Entry<Integer, Long> entry: map.entrySet()){
            Integer key=entry.getKey();
            Long value=entry.getValue();
            c.writeInt(key==null?0:key);
            c.writeLong(value==null?0L:value);
        }
    }
    public static <V> void writePackedIntObjectMap(ByteBuf c, Map<Integer, V> map, BiConsumer<ByteBuf, V> valueWriter){
        int count=map==null?0:map.size();
        writeSize(c, count);
        if(count==0){
            return;
        }
        if(map instanceof IntObjectHashMap){
            IntObjectHashMap.Cursor<V> cursor=((IntObjectHashMap<V>) map).cursor();
            while(cursor.advance()){
                c.writeInt(cursor.key());
                valueWriter.accept(c, cursor.value());
            }
            return;
        }
        for(Map.Entry<Integer, V> entry: map.entrySet()){
            Integer key=entry.getKey();
            c.writeInt(key==null?0:key);
            valueWriter.accept(c, entry.getValue());
        }
    }
    public static void writeRawLongArray(ByteBuf c, long[] arr, int count){
        if(count<=0){
            return;
        }
        if(c.hasArray() && UNSAFE!=null){
            int byteCount=count<<3;
            c.ensureWritable(byteCount);
            byte[] target=c.array();
            long offset=BYTE_ARRAY_OFFSET+c.arrayOffset()+c.writerIndex();
            if(NATIVE_BIG_ENDIAN){
                for(int i=0;i<count;i++){
                    UNSAFE.putLong(target, offset, arr[i]);
                    offset+=8;
                }
            }else{
                for(int i=0;i<count;i++){
                    UNSAFE.putLong(target, offset, Long.reverseBytes(arr[i]));
                    offset+=8;
                }
            }
            c.writerIndex(c.writerIndex()+byteCount);
            return;
        }
        for(int i=0;i<count;i++){
            c.writeLong(arr[i]);
        }
    }
    public static void readRawLongArray(ByteBuf c, long[] arr, int count){
        if(count<=0){
            return;
        }
        if(c.hasArray() && UNSAFE!=null){
            byte[] source=c.array();
            long offset=BYTE_ARRAY_OFFSET+c.arrayOffset()+c.readerIndex();
            for(int i=0;i<count;i++){
                long value=UNSAFE.getLong(source, offset);
                arr[i]=NATIVE_BIG_ENDIAN? value : Long.reverseBytes(value);
                offset+=8;
            }
            c.readerIndex(c.readerIndex()+(count<<3));
            return;
        }
        for(int i=0;i<count;i++) arr[i]=c.readLong();
    }
    public static LongArrayView readBorrowedRawLongArray(ByteBuf c){
        int count=readSize(c);
        if(count==0){
            return LongArrayView.empty();
        }
        int byteCount=count<<3;
        if(c.hasArray()){
            int readerIndex=c.readerIndex();
            LongArrayView view=LongArrayView.wrap(c.array(), c.arrayOffset()+readerIndex, count);
            c.readerIndex(readerIndex+byteCount);
            return view;
        }
        long[] values=new long[count];
        readRawLongArray(c, values, count);
        return LongArrayView.of(values);
    }
    public static LongArrayView readBorrowedRawLongArray(ByteBuf c, int totalCount, int readCount){
        if(totalCount<=0 || readCount<=0){
            if(totalCount>0){
                c.skipBytes(totalCount<<3);
            }
            return LongArrayView.empty();
        }
        int actualCount=Math.min(totalCount, readCount);
        int totalBytes=totalCount<<3;
        if(c.hasArray()){
            int readerIndex=c.readerIndex();
            LongArrayView view=LongArrayView.wrap(c.array(), c.arrayOffset()+readerIndex, actualCount);
            c.readerIndex(readerIndex+totalBytes);
            return view;
        }
        long[] values=new long[actualCount];
        readRawLongArray(c, values, actualCount);
        if(totalCount>actualCount){
            c.skipBytes((totalCount-actualCount)<<3);
        }
        return LongArrayView.of(values);
    }
    public static void writeBorrowedRawLongArray(ByteBuf c, LongArrayView value){
        LongArrayView view=value==null?LongArrayView.empty():value;
        int count=view.count();
        writeSize(c, count);
        if(count==0){
            return;
        }
        byte[] borrowed=view.borrowedArray();
        if(borrowed!=null){
            c.writeBytes(borrowed, view.borrowedOffset(), count<<3);
            return;
        }
        long[] materialized=view.materializedArray();
        if(materialized!=null){
            writeRawLongArray(c, materialized, count);
            return;
        }
        for(int i=0;i<count;i++){
            c.writeLong(view.getLong(i));
        }
    }
    public static void writeBorrowedFixedLongArray(ByteBuf c, LongArrayView value, int fixedCount){
        LongArrayView view=value==null?LongArrayView.empty():value;
        int count=view.count();
        if(count>fixedCount){
            throw new IllegalArgumentException("array length "+count+" exceeds fixedCount "+fixedCount);
        }
        if(count!=0){
            byte[] borrowed=view.borrowedArray();
            if(borrowed!=null){
                c.writeBytes(borrowed, view.borrowedOffset(), count<<3);
            }else{
                long[] materialized=view.materializedArray();
                if(materialized!=null){
                    writeRawLongArray(c, materialized, count);
                }else{
                    for(int i=0;i<count;i++){
                        c.writeLong(view.getLong(i));
                    }
                }
            }
        }
        if(count<fixedCount){
            c.writeZero((fixedCount-count)<<3);
        }
    }
    public static LongArrayList readPackedLongList(ByteBuf c){
        return readPackedLongList(c, null);
    }
    public static LongArrayList readPackedLongList(ByteBuf c, LongArrayList reuse){
        int count=readSize(c);
        LongArrayList list=reuse==null?borrowLongArrayList(count):reuse;
        list.ensureCapacity(count);
        list.resize(count);
        readRawLongArray(c, list.rawArray(), count);
        return list;
    }
    public static void writePackedLongList(ByteBuf c, List<Long> list){
        int count=list==null?0:list.size();
        writeSize(c, count);
        if(count==0){
            return;
        }
        if(list instanceof LongArrayList){
            LongArrayList typed=(LongArrayList) list;
            writeRawLongArray(c, typed.rawArray(), count);
            return;
        }
        if(list instanceof RandomAccess){
            for(int i=0;i<count;i++){
                Long value=list.get(i);
                c.writeLong(value==null?0L:value);
            }
            return;
        }
        for(Long value: list){
            c.writeLong(value==null?0L:value);
        }
    }
    public static void writeRawFloatArray(ByteBuf c, float[] arr, int count){
        if(count<=0){
            return;
        }
        if(c.hasArray() && UNSAFE!=null){
            int byteCount=count<<2;
            c.ensureWritable(byteCount);
            byte[] target=c.array();
            long offset=BYTE_ARRAY_OFFSET+c.arrayOffset()+c.writerIndex();
            if(NATIVE_BIG_ENDIAN){
                UNSAFE.copyMemory(arr, FLOAT_ARRAY_OFFSET, target, offset, byteCount);
            }else{
                for(int i=0;i<count;i++){
                    int bits=Float.floatToRawIntBits(arr[i]);
                    UNSAFE.putInt(target, offset, Integer.reverseBytes(bits));
                    offset+=4;
                }
            }
            c.writerIndex(c.writerIndex()+byteCount);
            return;
        }
        for(int i=0;i<count;i++){
            c.writeInt(Float.floatToRawIntBits(arr[i]));
        }
    }
    public static void readRawFloatArray(ByteBuf c, float[] arr, int count){
        if(count<=0){
            return;
        }
        if(c.hasArray() && UNSAFE!=null){
            byte[] source=c.array();
            long offset=BYTE_ARRAY_OFFSET+c.arrayOffset()+c.readerIndex();
            for(int i=0;i<count;i++){
                int bits=UNSAFE.getInt(source, offset);
                arr[i]=Float.intBitsToFloat(NATIVE_BIG_ENDIAN? bits : Integer.reverseBytes(bits));
                offset+=4;
            }
            c.readerIndex(c.readerIndex()+(count<<2));
            return;
        }
        for(int i=0;i<count;i++) arr[i]=Float.intBitsToFloat(c.readInt());
    }
    public static FloatArrayView readBorrowedRawFloatArray(ByteBuf c){
        int count=readSize(c);
        if(count==0){
            return FloatArrayView.empty();
        }
        int byteCount=count<<2;
        if(c.hasArray()){
            int readerIndex=c.readerIndex();
            FloatArrayView view=FloatArrayView.wrap(c.array(), c.arrayOffset()+readerIndex, count);
            c.readerIndex(readerIndex+byteCount);
            return view;
        }
        float[] values=new float[count];
        readRawFloatArray(c, values, count);
        return FloatArrayView.of(values);
    }
    public static FloatArrayView readBorrowedRawFloatArray(ByteBuf c, int totalCount, int readCount){
        if(totalCount<=0 || readCount<=0){
            if(totalCount>0){
                c.skipBytes(totalCount<<2);
            }
            return FloatArrayView.empty();
        }
        int actualCount=Math.min(totalCount, readCount);
        int totalBytes=totalCount<<2;
        if(c.hasArray()){
            int readerIndex=c.readerIndex();
            FloatArrayView view=FloatArrayView.wrap(c.array(), c.arrayOffset()+readerIndex, actualCount);
            c.readerIndex(readerIndex+totalBytes);
            return view;
        }
        float[] values=new float[actualCount];
        readRawFloatArray(c, values, actualCount);
        if(totalCount>actualCount){
            c.skipBytes((totalCount-actualCount)<<2);
        }
        return FloatArrayView.of(values);
    }
    public static void writeBorrowedRawFloatArray(ByteBuf c, FloatArrayView value){
        FloatArrayView view=value==null?FloatArrayView.empty():value;
        int count=view.count();
        writeSize(c, count);
        if(count==0){
            return;
        }
        byte[] borrowed=view.borrowedArray();
        if(borrowed!=null){
            c.writeBytes(borrowed, view.borrowedOffset(), count<<2);
            return;
        }
        float[] materialized=view.materializedArray();
        if(materialized!=null){
            writeRawFloatArray(c, materialized, count);
            return;
        }
        for(int i=0;i<count;i++){
            c.writeInt(Float.floatToRawIntBits(view.getFloat(i)));
        }
    }
    public static void writeBorrowedFixedFloatArray(ByteBuf c, FloatArrayView value, int fixedCount){
        FloatArrayView view=value==null?FloatArrayView.empty():value;
        int count=view.count();
        if(count>fixedCount){
            throw new IllegalArgumentException("array length "+count+" exceeds fixedCount "+fixedCount);
        }
        if(count!=0){
            byte[] borrowed=view.borrowedArray();
            if(borrowed!=null){
                c.writeBytes(borrowed, view.borrowedOffset(), count<<2);
            }else{
                float[] materialized=view.materializedArray();
                if(materialized!=null){
                    writeRawFloatArray(c, materialized, count);
                }else{
                    for(int i=0;i<count;i++){
                        c.writeInt(Float.floatToRawIntBits(view.getFloat(i)));
                    }
                }
            }
        }
        if(count<fixedCount){
            c.writeZero((fixedCount-count)<<2);
        }
    }
    public static void writeRawDoubleArray(ByteBuf c, double[] arr, int count){
        if(count<=0){
            return;
        }
        if(c.hasArray() && UNSAFE!=null){
            int byteCount=count<<3;
            c.ensureWritable(byteCount);
            byte[] target=c.array();
            long offset=BYTE_ARRAY_OFFSET+c.arrayOffset()+c.writerIndex();
            if(NATIVE_BIG_ENDIAN){
                UNSAFE.copyMemory(arr, DOUBLE_ARRAY_OFFSET, target, offset, byteCount);
            }else{
                for(int i=0;i<count;i++){
                    long bits=Double.doubleToRawLongBits(arr[i]);
                    UNSAFE.putLong(target, offset, Long.reverseBytes(bits));
                    offset+=8;
                }
            }
            c.writerIndex(c.writerIndex()+byteCount);
            return;
        }
        for(int i=0;i<count;i++){
            c.writeLong(Double.doubleToRawLongBits(arr[i]));
        }
    }
    public static void readRawDoubleArray(ByteBuf c, double[] arr, int count){
        if(count<=0){
            return;
        }
        if(c.hasArray() && UNSAFE!=null){
            byte[] source=c.array();
            long offset=BYTE_ARRAY_OFFSET+c.arrayOffset()+c.readerIndex();
            for(int i=0;i<count;i++){
                long bits=UNSAFE.getLong(source, offset);
                arr[i]=Double.longBitsToDouble(NATIVE_BIG_ENDIAN? bits : Long.reverseBytes(bits));
                offset+=8;
            }
            c.readerIndex(c.readerIndex()+(count<<3));
            return;
        }
        for(int i=0;i<count;i++) arr[i]=Double.longBitsToDouble(c.readLong());
    }
    public static DoubleArrayView readBorrowedRawDoubleArray(ByteBuf c){
        int count=readSize(c);
        if(count==0){
            return DoubleArrayView.empty();
        }
        int byteCount=count<<3;
        if(c.hasArray()){
            int readerIndex=c.readerIndex();
            DoubleArrayView view=DoubleArrayView.wrap(c.array(), c.arrayOffset()+readerIndex, count);
            c.readerIndex(readerIndex+byteCount);
            return view;
        }
        double[] values=new double[count];
        readRawDoubleArray(c, values, count);
        return DoubleArrayView.of(values);
    }
    public static DoubleArrayView readBorrowedRawDoubleArray(ByteBuf c, int totalCount, int readCount){
        if(totalCount<=0 || readCount<=0){
            if(totalCount>0){
                c.skipBytes(totalCount<<3);
            }
            return DoubleArrayView.empty();
        }
        int actualCount=Math.min(totalCount, readCount);
        int totalBytes=totalCount<<3;
        if(c.hasArray()){
            int readerIndex=c.readerIndex();
            DoubleArrayView view=DoubleArrayView.wrap(c.array(), c.arrayOffset()+readerIndex, actualCount);
            c.readerIndex(readerIndex+totalBytes);
            return view;
        }
        double[] values=new double[actualCount];
        readRawDoubleArray(c, values, actualCount);
        if(totalCount>actualCount){
            c.skipBytes((totalCount-actualCount)<<3);
        }
        return DoubleArrayView.of(values);
    }
    public static void writeBorrowedRawDoubleArray(ByteBuf c, DoubleArrayView value){
        DoubleArrayView view=value==null?DoubleArrayView.empty():value;
        int count=view.count();
        writeSize(c, count);
        if(count==0){
            return;
        }
        byte[] borrowed=view.borrowedArray();
        if(borrowed!=null){
            c.writeBytes(borrowed, view.borrowedOffset(), count<<3);
            return;
        }
        double[] materialized=view.materializedArray();
        if(materialized!=null){
            writeRawDoubleArray(c, materialized, count);
            return;
        }
        for(int i=0;i<count;i++){
            c.writeLong(Double.doubleToRawLongBits(view.getDouble(i)));
        }
    }
    public static void writeBorrowedFixedDoubleArray(ByteBuf c, DoubleArrayView value, int fixedCount){
        DoubleArrayView view=value==null?DoubleArrayView.empty():value;
        int count=view.count();
        if(count>fixedCount){
            throw new IllegalArgumentException("array length "+count+" exceeds fixedCount "+fixedCount);
        }
        if(count!=0){
            byte[] borrowed=view.borrowedArray();
            if(borrowed!=null){
                c.writeBytes(borrowed, view.borrowedOffset(), count<<3);
            }else{
                double[] materialized=view.materializedArray();
                if(materialized!=null){
                    writeRawDoubleArray(c, materialized, count);
                }else{
                    for(int i=0;i<count;i++){
                        c.writeLong(Double.doubleToRawLongBits(view.getDouble(i)));
                    }
                }
            }
        }
        if(count<fixedCount){
            c.writeZero((fixedCount-count)<<3);
        }
    }

    private static void writeUnsignedVarInt0(ByteCursor c, int value){
        if(c instanceof LinearByteBuffer){
            ((LinearByteBuffer) c).writeUnsignedVarIntFast(value);
            return;
        }
        if((value & ~0x7F)==0){
            c.writeByte((byte) value);
            return;
        }
        if((value & ~0x3FFF)==0){
            c.writeByte((byte) ((value & 0x7F) | 0x80));
            c.writeByte((byte) (value >>> 7));
            return;
        }
        if((value & ~0x1F_FFFF)==0){
            c.writeByte((byte) ((value & 0x7F) | 0x80));
            c.writeByte((byte) (((value >>> 7) & 0x7F) | 0x80));
            c.writeByte((byte) (value >>> 14));
            return;
        }
        if((value & ~0x0FFF_FFFF)==0){
            c.writeByte((byte) ((value & 0x7F) | 0x80));
            c.writeByte((byte) (((value >>> 7) & 0x7F) | 0x80));
            c.writeByte((byte) (((value >>> 14) & 0x7F) | 0x80));
            c.writeByte((byte) (value >>> 21));
            return;
        }
        c.writeByte((byte) ((value & 0x7F) | 0x80));
        c.writeByte((byte) (((value >>> 7) & 0x7F) | 0x80));
        c.writeByte((byte) (((value >>> 14) & 0x7F) | 0x80));
        c.writeByte((byte) (((value >>> 21) & 0x7F) | 0x80));
        c.writeByte((byte) (value >>> 28));
    }

    private static int readUnsignedVarInt0(ByteCursor c){
        if(c instanceof ArrayByteCursor){
            return ((ArrayByteCursor) c).readVarUInt32Fast();
        }
        if(c instanceof LinearByteBuffer){
            return ((LinearByteBuffer) c).readVarUInt32Fast();
        }
        if(c instanceof NettyCursor){
            ByteBuf buf=((NettyCursor) c).unwrap();
            if(buf.hasArray()){
                return readUnsignedVarIntFromArray(buf);
            }
        }
        return readUnsignedVarIntSlow(c);
    }

    private static int readUnsignedVarIntFromArray(ByteBuf buf){
        int index=buf.readerIndex();
        int limit=buf.writerIndex();
        byte[] array=buf.array();
        int offset=buf.arrayOffset();
        if(index>=limit){
            throw new IndexOutOfBoundsException("not enough readable bytes, need=1, readable=0");
        }
        int b0=array[offset+index++] & 0xFF;
        if((b0 & 0x80)==0){
            buf.readerIndex(index);
            return b0;
        }
        if(index>=limit){
            throw new IndexOutOfBoundsException("not enough readable bytes for unsigned varint");
        }
        int b1=array[offset+index++] & 0xFF;
        int result=(b0 & 0x7F) | ((b1 & 0x7F) << 7);
        if((b1 & 0x80)==0){
            buf.readerIndex(index);
            return result;
        }
        if(index>=limit){
            throw new IndexOutOfBoundsException("not enough readable bytes for unsigned varint");
        }
        int b2=array[offset+index++] & 0xFF;
        result |= (b2 & 0x7F) << 14;
        if((b2 & 0x80)==0){
            buf.readerIndex(index);
            return result;
        }
        if(index>=limit){
            throw new IndexOutOfBoundsException("not enough readable bytes for unsigned varint");
        }
        int b3=array[offset+index++] & 0xFF;
        result |= (b3 & 0x7F) << 21;
        if((b3 & 0x80)==0){
            buf.readerIndex(index);
            return result;
        }
        if(index>=limit){
            throw new IndexOutOfBoundsException("not enough readable bytes for unsigned varint");
        }
        int b4=array[offset+index++] & 0xFF;
        result |= (b4 & 0x0F) << 28;
        if((b4 & 0xF0)==0){
            buf.readerIndex(index);
            return result;
        }
        throw new IllegalStateException("unsigned varint is too long");
    }

    private static int readUnsignedVarIntSlow(ByteCursor c){
        int b0=c.readByte() & 0xFF;
        if((b0 & 0x80)==0){
            return b0;
        }
        int b1=c.readByte() & 0xFF;
        int result=(b0 & 0x7F) | ((b1 & 0x7F) << 7);
        if((b1 & 0x80)==0){
            return result;
        }
        int b2=c.readByte() & 0xFF;
        result |= (b2 & 0x7F) << 14;
        if((b2 & 0x80)==0){
            return result;
        }
        int b3=c.readByte() & 0xFF;
        result |= (b3 & 0x7F) << 21;
        if((b3 & 0x80)==0){
            return result;
        }
        int b4=c.readByte() & 0xFF;
        result |= (b4 & 0x0F) << 28;
        if((b4 & 0xF0)==0){
            return result;
        }
        throw new IllegalStateException("unsigned varint is too long");
    }

    private static void skipUnsignedVarInt0(ByteCursor c){
        if(c instanceof ArrayByteCursor){
            ((ArrayByteCursor) c).skipVarUInt32Fast();
            return;
        }
        if(c instanceof LinearByteBuffer){
            ((LinearByteBuffer) c).skipVarUInt32Fast();
            return;
        }
        if(c instanceof NettyCursor){
            ByteBuf buf=((NettyCursor) c).unwrap();
            if(buf.hasArray()){
                skipUnsignedVarIntFromArray(buf);
                return;
            }
        }
        skipUnsignedVarIntSlow(c);
    }

    private static void skipUnsignedVarIntFromArray(ByteBuf buf){
        int index=buf.readerIndex();
        int limit=buf.writerIndex();
        byte[] array=buf.array();
        int offset=buf.arrayOffset();
        if(index>=limit){
            throw new IndexOutOfBoundsException("not enough readable bytes, need=1, readable=0");
        }
        int b0=array[offset+index++] & 0xFF;
        if((b0 & 0x80)==0){
            buf.readerIndex(index);
            return;
        }
        if(index>=limit){
            throw new IndexOutOfBoundsException("not enough readable bytes for unsigned varint");
        }
        int b1=array[offset+index++] & 0xFF;
        if((b1 & 0x80)==0){
            buf.readerIndex(index);
            return;
        }
        if(index>=limit){
            throw new IndexOutOfBoundsException("not enough readable bytes for unsigned varint");
        }
        int b2=array[offset+index++] & 0xFF;
        if((b2 & 0x80)==0){
            buf.readerIndex(index);
            return;
        }
        if(index>=limit){
            throw new IndexOutOfBoundsException("not enough readable bytes for unsigned varint");
        }
        int b3=array[offset+index++] & 0xFF;
        if((b3 & 0x80)==0){
            buf.readerIndex(index);
            return;
        }
        if(index>=limit){
            throw new IndexOutOfBoundsException("not enough readable bytes for unsigned varint");
        }
        int b4=array[offset+index++] & 0xFF;
        if((b4 & 0xF0)==0){
            buf.readerIndex(index);
            return;
        }
        throw new IllegalStateException("unsigned varint is too long");
    }

    private static void skipUnsignedVarIntSlow(ByteCursor c){
        for(int i=0;i<5;i++){
            int b=c.readByte() & 0xFF;
            if((b & 0x80)==0){
                return;
            }
        }
        throw new IllegalStateException("unsigned varint is too long");
    }

    private static void writeUnsignedVarLong0(ByteCursor c, long value){
        if(c instanceof LinearByteBuffer){
            ((LinearByteBuffer) c).writeUnsignedVarLongFast(value);
            return;
        }
        if((value & ~0x7FL)==0L){
            c.writeByte((byte) value);
            return;
        }
        c.writeByte((byte)((value & 0x7F) | 0x80));
        value>>>=7;
        if((value & ~0x7FL)==0L){
            c.writeByte((byte) value);
            return;
        }
        c.writeByte((byte)((value & 0x7F) | 0x80));
        value>>>=7;
        if((value & ~0x7FL)==0L){
            c.writeByte((byte) value);
            return;
        }
        c.writeByte((byte)((value & 0x7F) | 0x80));
        value>>>=7;
        if((value & ~0x7FL)==0L){
            c.writeByte((byte) value);
            return;
        }
        c.writeByte((byte)((value & 0x7F) | 0x80));
        value>>>=7;
        if((value & ~0x7FL)==0L){
            c.writeByte((byte) value);
            return;
        }
        c.writeByte((byte)((value & 0x7F) | 0x80));
        value>>>=7;
        if((value & ~0x7FL)==0L){
            c.writeByte((byte) value);
            return;
        }
        c.writeByte((byte)((value & 0x7F) | 0x80));
        value>>>=7;
        if((value & ~0x7FL)==0L){
            c.writeByte((byte) value);
            return;
        }
        c.writeByte((byte)((value & 0x7F) | 0x80));
        value>>>=7;
        if((value & ~0x7FL)==0L){
            c.writeByte((byte) value);
            return;
        }
        c.writeByte((byte)((value & 0x7F) | 0x80));
        value>>>=7;
        if((value & ~0x7FL)==0L){
            c.writeByte((byte) value);
            return;
        }
        c.writeByte((byte)((value & 0x7F) | 0x80));
        value>>>=7;
        c.writeByte((byte) value);
    }

    private static long readUnsignedVarLong0(ByteCursor c){
        if(c instanceof ArrayByteCursor){
            return ((ArrayByteCursor) c).readVarUInt64Fast();
        }
        if(c instanceof LinearByteBuffer){
            return ((LinearByteBuffer) c).readVarUInt64Fast();
        }
        if(c instanceof NettyCursor){
            ByteBuf buf=((NettyCursor) c).unwrap();
            if(buf.hasArray()){
                return readUnsignedVarLongFromArray(buf);
            }
        }
        return readUnsignedVarLongSlow(c);
    }

    private static long readUnsignedVarLongFromArray(ByteBuf buf){
        int index=buf.readerIndex();
        int limit=buf.writerIndex();
        byte[] array=buf.array();
        int offset=buf.arrayOffset();
        long result=0L;
        int shift=0;
        while(shift<64){
            if(index>=limit){
                throw new IndexOutOfBoundsException("not enough readable bytes for unsigned varlong");
            }
            int b=array[offset+index++] & 0xFF;
            result |= (long) (b & 0x7F) << shift;
            if((b & 0x80)==0){
                buf.readerIndex(index);
                return result;
            }
            shift+=7;
        }
        throw new IllegalStateException("unsigned varlong is too long");
    }

    private static long readUnsignedVarLongSlow(ByteCursor c){
        long result=0L;
        int shift=0;
        while(shift<64){
            int b=c.readByte() & 0xFF;
            result |= (long) (b & 0x7F) << shift;
            if((b & 0x80)==0){
                return result;
            }
            shift+=7;
        }
        throw new IllegalStateException("unsigned varlong is too long");
    }

    private static void skipUnsignedVarLong0(ByteCursor c){
        if(c instanceof ArrayByteCursor){
            ((ArrayByteCursor) c).skipVarUInt64Fast();
            return;
        }
        if(c instanceof LinearByteBuffer){
            ((LinearByteBuffer) c).skipVarUInt64Fast();
            return;
        }
        if(c instanceof NettyCursor){
            ByteBuf buf=((NettyCursor) c).unwrap();
            if(buf.hasArray()){
                skipUnsignedVarLongFromArray(buf);
                return;
            }
        }
        skipUnsignedVarLongSlow(c);
    }

    private static void skipUnsignedVarLongFromArray(ByteBuf buf){
        int index=buf.readerIndex();
        int limit=buf.writerIndex();
        byte[] array=buf.array();
        int offset=buf.arrayOffset();
        int shift=0;
        while(shift<64){
            if(index>=limit){
                throw new IndexOutOfBoundsException("not enough readable bytes for unsigned varlong");
            }
            int b=array[offset+index++] & 0xFF;
            if((b & 0x80)==0){
                buf.readerIndex(index);
                return;
            }
            shift+=7;
        }
        throw new IllegalStateException("unsigned varlong is too long");
    }

    private static void skipUnsignedVarLongSlow(ByteCursor c){
        for(int shift=0;shift<64;shift+=7){
            int b=c.readByte() & 0xFF;
            if((b & 0x80)==0){
                return;
            }
        }
        throw new IllegalStateException("unsigned varlong is too long");
    }

    /**
     * VarInt/VarLong 濞寸姴绉堕崝褎绌卞┑鍥х槷闁告鍠愬﹢?wire 闁哄秶鍘х槐锟犲Υ?     * 閺夆晜鐟╅崳鐑藉绩鐟欏嫬鐏囬柍銉︾矊閸欐洟宕楅崡鐐插汲锟?+ 闁绘埈鍙€閻儳顕ラ崟顏嗙懇闁活潿鍔岄悿鍕偝閹垫枼鍋撳┑鍫熺暠缂備礁瀚划鎰板棘閻熸壆纭€闁挎稑濂旂粚鑸电鎼搭垳锟?     * 1. ArrayByteCursor / heap ByteBuf 閻犙呭閺嗙喓绱掗崟顓熺函閻犲洩宕靛ú鍧楀礃濞嗗繑褰ラ悹渚灠缁剁偤锟?     * 2. 闁稿繗娉涢悾?ByteCursor 缂備綀鍛暰閻犙傚嵆閳ь剚姘ㄩ弫銈夋焻閺勫繒甯嗛柨?     * 3. 閹兼潙绻愰崹顏堝礌閺嵮冪閻犱緡鍠栭悺褔鎳為崒姘辨殮闁稿繈鍔嬬粭澶愬矗濮楀牏绀夐柡鍌炩偓娑氣敀闁搞儳鍋涚紞濠冾殽瀹€鍐锟?     */
    public static void writeVarInt(ByteCursor c,int v){
        writeUnsignedVarInt0(c, (v<<1)^(v>>31));
    }
    public static int readVarInt(ByteCursor c){
        int raw=readUnsignedVarInt0(c);
        return (raw>>>1)^-(raw&1);
    }
    public static void skipVarInt(ByteCursor c){
        skipUnsignedVarInt0(c);
    }
    public static void writeVarLong(ByteCursor c,long v){
        writeUnsignedVarLong0(c, (v<<1)^(v>>63));
    }
    public static long readVarLong(ByteCursor c){
        long raw=readUnsignedVarLong0(c);
        return (raw>>>1)^-(raw&1L);
    }
    public static void skipVarLong(ByteCursor c){
        skipUnsignedVarLong0(c);
    }

    /**
     * 濠㈠灈鏅濋崝褔妫冮悙鍓侇槹闁汇劌瀚悺褍鈻撻悽鐢靛煚濞戞挴鍋撻悹褏澧楀Λ銈囩箔閿曗偓瑜拌法绱撻弽顐ゅ灣锟?     * 闁稿繒顭堥悗鐑藉捶閻戞ɑ鐝柡鍕靛灦閺嗚鲸鎯旈敂琛″亾娓氣偓濞夛箓锟?size闁靛棔韬琻um ordinal闁靛棔鍨esence bits 闂傗偓閸喖顔婄紒娑橆槶锟?     */
    public static void writeUnsignedVarInt(ByteCursor c,int v){
        if(v<0){
            throw new IllegalArgumentException("unsigned varint can not encode negative int: "+v);
        }
        writeUnsignedVarInt0(c, v);
    }
    public static int readUnsignedVarInt(ByteCursor c){
        return readUnsignedVarInt0(c);
    }
    public static void skipUnsignedVarInt(ByteCursor c){
        skipUnsignedVarInt0(c);
    }
    public static void writeUnsignedVarLong(ByteCursor c,long v){
        if(v<0L){
            throw new IllegalArgumentException("unsigned varlong can not encode negative long: "+v);
        }
        writeUnsignedVarLong0(c, v);
    }
    public static long readUnsignedVarLong(ByteCursor c){
        return readUnsignedVarLong0(c);
    }
    public static void skipUnsignedVarLong(ByteCursor c){
        skipUnsignedVarLong0(c);
    }

    public static void writeInt(ByteCursor c,int v){ writeVarInt(c,v); }
    public static int readInt(ByteCursor c){ return readVarInt(c); }
    public static void skipInt(ByteCursor c){ skipVarInt(c); }
    public static void writeLong(ByteCursor c,long v){ writeVarLong(c,v); }
    public static long readLong(ByteCursor c){ return readVarLong(c); }
    public static void skipLong(ByteCursor c){ skipVarLong(c); }
    public static void writeFixedInt(ByteCursor c,int v){
        if(c instanceof LinearByteBuffer){
            ((LinearByteBuffer) c).writeFixedIntFast(v);
            return;
        }
        if(hasHeapWritableArray(c)){
            ensureHeapWritable(c, 4);
            writeHeapBigEndianInt(backingArray(c), backingWriteOffset(c), v);
            advanceWrite(c, 4);
            return;
        }
        c.writeByte((byte)(v>>>24));
        c.writeByte((byte)(v>>>16));
        c.writeByte((byte)(v>>>8));
        c.writeByte((byte)v);
    }
    public static int readFixedInt(ByteCursor c){
        if(c instanceof ArrayByteCursor){
            return ((ArrayByteCursor) c).readFixedIntFast();
        }
        if(c instanceof LinearByteBuffer){
            return ((LinearByteBuffer) c).readFixedIntFast();
        }
        if(hasHeapBackingArray(c)){
            int value=readHeapBigEndianInt(backingArray(c), backingOffset(c));
            c.skip(4);
            return value;
        }
        return ((c.readByte() & 0xFF) << 24)
                | ((c.readByte() & 0xFF) << 16)
                | ((c.readByte() & 0xFF) << 8)
                | (c.readByte() & 0xFF);
    }
    public static void writeFixedLong(ByteCursor c,long v){
        if(c instanceof LinearByteBuffer){
            ((LinearByteBuffer) c).writeFixedLongFast(v);
            return;
        }
        if(hasHeapWritableArray(c)){
            ensureHeapWritable(c, 8);
            writeHeapBigEndianLong(backingArray(c), backingWriteOffset(c), v);
            advanceWrite(c, 8);
            return;
        }
        c.writeByte((byte)(v>>>56));
        c.writeByte((byte)(v>>>48));
        c.writeByte((byte)(v>>>40));
        c.writeByte((byte)(v>>>32));
        c.writeByte((byte)(v>>>24));
        c.writeByte((byte)(v>>>16));
        c.writeByte((byte)(v>>>8));
        c.writeByte((byte)v);
    }
    public static long readFixedLong(ByteCursor c){
        if(c instanceof ArrayByteCursor){
            return ((ArrayByteCursor) c).readFixedLongFast();
        }
        if(c instanceof LinearByteBuffer){
            return ((LinearByteBuffer) c).readFixedLongFast();
        }
        if(hasHeapBackingArray(c)){
            long value=readHeapBigEndianLong(backingArray(c), backingOffset(c));
            c.skip(8);
            return value;
        }
        long l=0L;
        for(int i=7;i>=0;i--){
            l|=((long)(c.readByte() & 0xFF)) << (i<<3);
        }
        return l;
    }
    public static void writeUInt(ByteCursor c,int v){ writeUnsignedVarInt(c,v); }
    public static int readUInt(ByteCursor c){ return readUnsignedVarInt(c); }
    public static void skipUInt(ByteCursor c){ skipUnsignedVarInt(c); }
    public static void writeULong(ByteCursor c,long v){ writeUnsignedVarLong(c,v); }
    public static long readULong(ByteCursor c){ return readUnsignedVarLong(c); }
    public static void skipULong(ByteCursor c){ skipUnsignedVarLong(c); }
    public static void writeSize(ByteCursor c,int size){
        if(size<0){
            throw new IllegalArgumentException("size can not be negative: "+size);
        }
        writeUnsignedVarInt(c,size);
    }
    public static int readSize(ByteCursor c){ return readUnsignedVarInt(c); }
    public static void skipSize(ByteCursor c){ skipUnsignedVarInt(c); }
    public static void writeShort(ByteCursor c,short v){ writeVarInt(c,v); }
    public static short readShort(ByteCursor c){ return (short) readVarInt(c); }
    public static void skipShort(ByteCursor c){ skipVarInt(c); }
    public static void writeFixedShort(ByteCursor c,short v){
        if(canUseDirectWriteArrayAccess(c)){
            if(c instanceof LinearByteBuffer){
                ((LinearByteBuffer) c).ensureWritable(2);
            }else if(c instanceof NettyCursor){
                ((NettyCursor) c).unwrap().ensureWritable(2);
            }
            byte[] target=backingArray(c);
            long offset=BYTE_ARRAY_OFFSET+backingWriteOffset(c);
            if(UNSAFE!=null){
                UNSAFE.putShort(target, offset, NATIVE_BIG_ENDIAN? v : Short.reverseBytes(v));
            }else{
                int writeOffset=(int)(offset-BYTE_ARRAY_OFFSET);
                target[writeOffset]=(byte)(v>>>8);
                target[writeOffset+1]=(byte)v;
            }
            advanceWrite(c, 2);
            return;
        }
        c.writeByte((byte)(v>>>8));
        c.writeByte((byte)v);
    }
    public static short readFixedShort(ByteCursor c){
        if(hasHeapBackingArray(c)){
            byte[] source=backingArray(c);
            int offset=backingOffset(c);
            short value;
            if(UNSAFE!=null){
                value=UNSAFE.getShort(source, BYTE_ARRAY_OFFSET+offset);
                value=NATIVE_BIG_ENDIAN? value : Short.reverseBytes(value);
            }else{
                value=(short)(((source[offset] & 0xFF) << 8) | (source[offset+1] & 0xFF));
            }
            c.skip(2);
            return value;
        }
        return (short)(((c.readByte() & 0xFF) << 8) | (c.readByte() & 0xFF));
    }
    public static void writeByte(ByteCursor c,byte v){ c.writeByte(v); }
    public static byte readByte(ByteCursor c){ return c.readByte(); }
    public static void skipByte(ByteCursor c){ c.skip(1); }
    public static void writeBoolean(ByteCursor c,boolean v){ c.writeByte((byte)(v?1:0)); }
    public static boolean readBoolean(ByteCursor c){ return c.readByte()!=0; }
    public static void skipBoolean(ByteCursor c){ c.skip(1); }

    /**
     * char 闁告瑯浜欑槐浼存閻愬墎顦伴柨娑樼灱濞插潡骞掗妷銊ㄦ巢闁哄啰濮烽渚€宕ｆ搴ｆそ闁活喕鐒﹂惁?ZigZag 闁哄洤顕幓锝夊礄閹存瑢锟?     */
    public static void writeChar(ByteCursor c,char v){ writeUnsignedVarInt(c,v); }
    public static char readChar(ByteCursor c){ return (char) readUnsignedVarInt(c); }
    public static void skipChar(ByteCursor c){ skipUnsignedVarInt(c); }
    public static void writeFixedChar(ByteCursor c,char v){ writeFixedShort(c, (short) v); }
    public static char readFixedChar(ByteCursor c){ return (char)(readFixedShort(c) & 0xFFFF); }
    public static void writeFixedIntArray(ByteCursor c,int[] arr){
        int[] values=arr==null? new int[0]: arr;
        writeSize(c, values.length);
        if(values.length==0){
            return;
        }
        int byteCount=values.length<<2;
        if(c instanceof LinearByteBuffer){
            ((LinearByteBuffer) c).ensureWritable(byteCount);
        }else if(c instanceof NettyCursor && ((NettyCursor) c).unwrap().hasArray()){
            ((NettyCursor) c).unwrap().ensureWritable(byteCount);
        }
        if(canUseUnsafeHeapWriteArrayAccess(c)){
            byte[] target=backingArray(c);
            int writeOffset=backingWriteOffset(c);
            if(NATIVE_BIG_ENDIAN && UNSAFE!=null){
                UNSAFE.copyMemory(values, INT_ARRAY_OFFSET, target, BYTE_ARRAY_OFFSET+writeOffset, byteCount);
            }else{
                long offset=BYTE_ARRAY_OFFSET+writeOffset;
                for(int value: values){
                    UNSAFE.putInt(target, offset, Integer.reverseBytes(value));
                    offset+=4;
                }
            }
            advanceWrite(c, byteCount);
            return;
        }
        for(int value: values){
            writeFixedInt(c, value);
        }
    }
    public static void writeFixedCountBytes(ByteCursor c, byte[] arr, int fixedCount){
        byte[] values=arr==null? EMPTY_BYTES:arr;
        if(values.length>fixedCount){
            throw new IllegalArgumentException("array length "+values.length+" exceeds fixedCount "+fixedCount);
        }
        if(values.length!=0){
            c.writeBytes(values,0,values.length);
        }
        int padding=fixedCount-values.length;
        if(padding>0){
            writeZeroBytes(c, padding);
        }
    }
    public static byte[] readFixedCountBytes(ByteCursor c, int fixedCount){
        if(fixedCount<=0){
            return EMPTY_BYTES;
        }
        byte[] values=new byte[fixedCount];
        c.readBytes(values,0,fixedCount);
        return values;
    }
    public static void writeBorrowedFixedBytes(ByteCursor c, BorrowedBytes value, int fixedCount){
        BorrowedBytes borrowed=value==null?BorrowedBytes.empty():value;
        if(borrowed.length()>fixedCount){
            throw new IllegalArgumentException("array length "+borrowed.length()+" exceeds fixedCount "+fixedCount);
        }
        if(borrowed.length()!=0){
            c.writeBytes(borrowed.unsafeArray(), borrowed.unsafeOffset(), borrowed.length());
        }
        int padding=fixedCount-borrowed.length();
        if(padding>0){
            writeZeroBytes(c, padding);
        }
    }
    public static BorrowedBytes readBorrowedFixedBytes(ByteCursor c, int fixedCount){
        if(fixedCount<=0){
            return BorrowedBytes.empty();
        }
        if(canUseUnsafeArrayAccess(c)){
            BorrowedBytes borrowed=BorrowedBytes.wrap(backingArray(c), backingOffset(c), fixedCount);
            c.skip(fixedCount);
            return borrowed;
        }
        byte[] values=readFixedCountBytes(c, fixedCount);
        return BorrowedBytes.copyOf(values);
    }
    public static void writeFixedCountIntArray(ByteCursor c, int[] arr, int fixedCount){
        int[] values=arr==null? new int[0]:arr;
        if(values.length>fixedCount){
            throw new IllegalArgumentException("array length "+values.length+" exceeds fixedCount "+fixedCount);
        }
        for(int i=0;i<fixedCount;i++){
            writeFixedInt(c, i<values.length?values[i]:0);
        }
    }
    public static int[] readFixedCountIntArray(ByteCursor c, int fixedCount){
        if(fixedCount<=0){
            return new int[0];
        }
        int[] values=new int[fixedCount];
        readRawIntArray(c, values, fixedCount);
        return values;
    }
    public static IntArrayView readBorrowedFixedIntArray(ByteCursor c, int fixedCount){
        if(fixedCount<=0){
            return IntArrayView.empty();
        }
        if(canUseUnsafeArrayAccess(c)){
            IntArrayView view=IntArrayView.wrap(backingArray(c), backingOffset(c), fixedCount);
            c.skip(fixedCount<<2);
            return view;
        }
        int[] values=readFixedCountIntArray(c, fixedCount);
        return IntArrayView.of(values);
    }
    public static void writeFixedCountLongArray(ByteCursor c, long[] arr, int fixedCount){
        long[] values=arr==null? new long[0]:arr;
        if(values.length>fixedCount){
            throw new IllegalArgumentException("array length "+values.length+" exceeds fixedCount "+fixedCount);
        }
        for(int i=0;i<fixedCount;i++){
            writeFixedLong(c, i<values.length?values[i]:0L);
        }
    }
    public static long[] readFixedCountLongArray(ByteCursor c, int fixedCount){
        if(fixedCount<=0){
            return new long[0];
        }
        long[] values=new long[fixedCount];
        readRawLongArray(c, values, fixedCount);
        return values;
    }
    public static LongArrayView readBorrowedFixedLongArray(ByteCursor c, int fixedCount){
        if(fixedCount<=0){
            return LongArrayView.empty();
        }
        if(canUseUnsafeArrayAccess(c)){
            LongArrayView view=LongArrayView.wrap(backingArray(c), backingOffset(c), fixedCount);
            c.skip(fixedCount<<3);
            return view;
        }
        long[] values=readFixedCountLongArray(c, fixedCount);
        return LongArrayView.of(values);
    }
    public static void writeFixedCountFloatArray(ByteCursor c, float[] arr, int fixedCount){
        float[] values=arr==null? new float[0]:arr;
        if(values.length>fixedCount){
            throw new IllegalArgumentException("array length "+values.length+" exceeds fixedCount "+fixedCount);
        }
        for(int i=0;i<fixedCount;i++){
            writeFloat(c, i<values.length?values[i]:0F);
        }
    }
    public static float[] readFixedCountFloatArray(ByteCursor c, int fixedCount){
        if(fixedCount<=0){
            return new float[0];
        }
        float[] values=new float[fixedCount];
        readRawFloatArray(c, values, fixedCount);
        return values;
    }
    public static FloatArrayView readBorrowedFixedFloatArray(ByteCursor c, int fixedCount){
        if(fixedCount<=0){
            return FloatArrayView.empty();
        }
        if(canUseUnsafeArrayAccess(c)){
            FloatArrayView view=FloatArrayView.wrap(backingArray(c), backingOffset(c), fixedCount);
            c.skip(fixedCount<<2);
            return view;
        }
        float[] values=readFixedCountFloatArray(c, fixedCount);
        return FloatArrayView.of(values);
    }
    public static void writeFixedCountDoubleArray(ByteCursor c, double[] arr, int fixedCount){
        double[] values=arr==null? new double[0]:arr;
        if(values.length>fixedCount){
            throw new IllegalArgumentException("array length "+values.length+" exceeds fixedCount "+fixedCount);
        }
        for(int i=0;i<fixedCount;i++){
            writeDouble(c, i<values.length?values[i]:0D);
        }
    }
    public static double[] readFixedCountDoubleArray(ByteCursor c, int fixedCount){
        if(fixedCount<=0){
            return new double[0];
        }
        double[] values=new double[fixedCount];
        readRawDoubleArray(c, values, fixedCount);
        return values;
    }
    public static DoubleArrayView readBorrowedFixedDoubleArray(ByteCursor c, int fixedCount){
        if(fixedCount<=0){
            return DoubleArrayView.empty();
        }
        if(canUseUnsafeArrayAccess(c)){
            DoubleArrayView view=DoubleArrayView.wrap(backingArray(c), backingOffset(c), fixedCount);
            c.skip(fixedCount<<3);
            return view;
        }
        double[] values=readFixedCountDoubleArray(c, fixedCount);
        return DoubleArrayView.of(values);
    }
    public static int[] readFixedIntArray(ByteCursor c){
        int n=readSize(c);
        int[] arr=new int[n];
        if(n==0){
            return arr;
        }
        if(canUseUnsafeArrayAccess(c)){
            byte[] source=backingArray(c);
            long offset=BYTE_ARRAY_OFFSET+backingOffset(c);
            for(int i=0;i<n;i++){
                int value=UNSAFE.getInt(source, offset);
                arr[i]=NATIVE_BIG_ENDIAN? value : Integer.reverseBytes(value);
                offset+=4;
            }
            c.skip(n<<2);
            return arr;
        }
        for(int i=0;i<n;i++){
            arr[i]=readFixedInt(c);
        }
        return arr;
    }
    public static void readRawIntArray(ByteCursor c, int[] arr, int count){
        if(count<=0){
            return;
        }
        if(canUseUnsafeArrayAccess(c)){
            byte[] source=backingArray(c);
            long offset=BYTE_ARRAY_OFFSET+backingOffset(c);
            for(int i=0;i<count;i++){
                int value=UNSAFE.getInt(source, offset);
                arr[i]=NATIVE_BIG_ENDIAN? value : Integer.reverseBytes(value);
                offset+=4;
            }
            c.skip(count<<2);
            return;
        }
        for(int i=0;i<count;i++){
            arr[i]=readFixedInt(c);
        }
    }
    public static int[] readRawIntArray(ByteCursor c){
        return readFixedIntArray(c);
    }
    public static IntArrayView readBorrowedRawIntArray(ByteCursor c){
        int count=readSize(c);
        if(count==0){
            return IntArrayView.empty();
        }
        if(canUseUnsafeArrayAccess(c)){
            IntArrayView view=IntArrayView.wrap(backingArray(c), backingOffset(c), count);
            c.skip(count<<2);
            return view;
        }
        int[] values=new int[count];
        readRawIntArray(c, values, count);
        return IntArrayView.of(values);
    }
    public static IntArrayView readBorrowedRawIntArray(ByteCursor c, int totalCount, int readCount){
        if(totalCount<=0 || readCount<=0){
            if(totalCount>0){
                c.skip(totalCount<<2);
            }
            return IntArrayView.empty();
        }
        int actualCount=Math.min(totalCount, readCount);
        if(canUseUnsafeArrayAccess(c)){
            IntArrayView view=IntArrayView.wrap(backingArray(c), backingOffset(c), actualCount);
            c.skip(totalCount<<2);
            return view;
        }
        int[] values=new int[actualCount];
        readRawIntArray(c, values, actualCount);
        if(totalCount>actualCount){
            c.skip((totalCount-actualCount)<<2);
        }
        return IntArrayView.of(values);
    }
    public static void writeBorrowedRawIntArray(ByteCursor c, IntArrayView value){
        IntArrayView view=value==null?IntArrayView.empty():value;
        int count=view.count();
        writeSize(c, count);
        if(count==0){
            return;
        }
        for(int i=0;i<count;i++){
            writeFixedInt(c, view.getInt(i));
        }
    }
    public static void writeBorrowedFixedIntArray(ByteCursor c, IntArrayView value, int fixedCount){
        IntArrayView view=value==null?IntArrayView.empty():value;
        if(view.count()>fixedCount){
            throw new IllegalArgumentException("array length "+view.count()+" exceeds fixedCount "+fixedCount);
        }
        for(int i=0;i<fixedCount;i++){
            writeFixedInt(c, i<view.count()?view.getInt(i):0);
        }
    }
    public static void writeRawIntArray(ByteCursor c, int[] arr){
        writeFixedIntArray(c, arr);
    }
    public static IntArrayList readPackedIntList(ByteCursor c){
        return readPackedIntList(c, null);
    }
    public static IntArrayList readPackedIntList(ByteCursor c, IntArrayList reuse){
        int count=readSize(c);
        IntArrayList list=reuse==null?borrowIntArrayList(count):reuse;
        list.ensureCapacity(count);
        list.resize(count);
        readRawIntArray(c, list.rawArray(), count);
        return list;
    }
    public static void writePackedIntList(ByteCursor c, List<Integer> list){
        int count=list==null?0:list.size();
        writeSize(c, count);
        if(count==0){
            return;
        }
        if(list instanceof IntArrayList){
            IntArrayList typed=(IntArrayList) list;
            int[] raw=typed.rawArray();
            for(int i=0;i<count;i++){
                writeFixedInt(c, raw[i]);
            }
            return;
        }
        if(list instanceof RandomAccess){
            for(int i=0;i<count;i++){
                Integer value=list.get(i);
                writeFixedInt(c, value==null?0:value);
            }
            return;
        }
        for(Integer value: list){
            writeFixedInt(c, value==null?0:value);
        }
    }
    public static <V> IntObjectHashMap<V> readPackedIntObjectMapFast(ByteCursor c, PackedIntObjectValueReader<V> valueReader){
        return readPackedIntObjectMapFast(c, null, valueReader);
    }
    public static <V> IntObjectHashMap<V> readPackedIntObjectMapFast(ByteCursor c, IntObjectHashMap<V> reuse, PackedIntObjectValueReader<V> valueReader){
        int count=readSize(c);
        IntObjectHashMap<V> map=reuse==null?borrowIntObjectHashMap(count):reuse;
        map.clear();
        map.ensureCapacity(count);
        for(int i=0;i<count;i++){
            map.putInt(readFixedInt(c), valueReader.read(c));
        }
        return map;
    }
    public static <V> IntObjectHashMap<V> readPackedIntObjectMap(ByteCursor c, Function<ByteCursor, V> valueReader){
        return readPackedIntObjectMapFast(c, valueReader::apply);
    }
    public static <V> IntObjectHashMap<V> readPackedIntObjectMap(ByteCursor c, IntObjectHashMap<V> reuse, Function<ByteCursor, V> valueReader){
        return readPackedIntObjectMapFast(c, reuse, valueReader::apply);
    }
    public static IntIntHashMap readPackedIntIntMap(ByteCursor c){
        return readPackedIntIntMap(c, null);
    }
    public static IntIntHashMap readPackedIntIntMap(ByteCursor c, IntIntHashMap reuse){
        int count=readSize(c);
        IntIntHashMap map=reuse==null?borrowIntIntHashMap(count):reuse;
        map.clear();
        map.ensureCapacity(count);
        for(int i=0;i<count;i++){
            map.putInt(readFixedInt(c), readFixedInt(c));
        }
        return map;
    }
    public static void writePackedIntIntMap(ByteCursor c, Map<Integer, Integer> map){
        int count=map==null?0:map.size();
        writeSize(c, count);
        if(count==0){
            return;
        }
        if(map instanceof IntIntHashMap){
            IntIntHashMap.Cursor cursor=((IntIntHashMap) map).cursor();
            while(cursor.advance()){
                writeFixedInt(c, cursor.key());
                writeFixedInt(c, cursor.valueInt());
            }
            return;
        }
        for(Map.Entry<Integer, Integer> entry: map.entrySet()){
            Integer key=entry.getKey();
            Integer value=entry.getValue();
            writeFixedInt(c, key==null?0:key);
            writeFixedInt(c, value==null?0:value);
        }
    }
    public static IntLongHashMap readPackedIntLongMap(ByteCursor c){
        return readPackedIntLongMap(c, null);
    }
    public static IntLongHashMap readPackedIntLongMap(ByteCursor c, IntLongHashMap reuse){
        int count=readSize(c);
        IntLongHashMap map=reuse==null?borrowIntLongHashMap(count):reuse;
        map.clear();
        map.ensureCapacity(count);
        for(int i=0;i<count;i++){
            map.putLong(readFixedInt(c), readFixedLong(c));
        }
        return map;
    }
    public static void writePackedIntLongMap(ByteCursor c, Map<Integer, Long> map){
        int count=map==null?0:map.size();
        writeSize(c, count);
        if(count==0){
            return;
        }
        if(map instanceof IntLongHashMap){
            IntLongHashMap.Cursor cursor=((IntLongHashMap) map).cursor();
            while(cursor.advance()){
                writeFixedInt(c, cursor.key());
                writeFixedLong(c, cursor.valueLong());
            }
            return;
        }
        for(Map.Entry<Integer, Long> entry: map.entrySet()){
            Integer key=entry.getKey();
            Long value=entry.getValue();
            writeFixedInt(c, key==null?0:key);
            writeFixedLong(c, value==null?0L:value);
        }
    }
    public static <V> void writePackedIntObjectMap(ByteCursor c, Map<Integer, V> map, BiConsumer<ByteCursor, V> valueWriter){
        int count=map==null?0:map.size();
        writeSize(c, count);
        if(count==0){
            return;
        }
        if(map instanceof IntObjectHashMap){
            IntObjectHashMap.Cursor<V> cursor=((IntObjectHashMap<V>) map).cursor();
            while(cursor.advance()){
                writeFixedInt(c, cursor.key());
                valueWriter.accept(c, cursor.value());
            }
            return;
        }
        for(Map.Entry<Integer, V> entry: map.entrySet()){
            Integer key=entry.getKey();
            writeFixedInt(c, key==null?0:key);
            valueWriter.accept(c, entry.getValue());
        }
    }
    public static void writeFixedLongArray(ByteCursor c,long[] arr){
        long[] values=arr==null? new long[0]: arr;
        writeSize(c, values.length);
        if(values.length==0){
            return;
        }
        int byteCount=values.length<<3;
        if(c instanceof LinearByteBuffer){
            ((LinearByteBuffer) c).ensureWritable(byteCount);
        }else if(c instanceof NettyCursor && ((NettyCursor) c).unwrap().hasArray()){
            ((NettyCursor) c).unwrap().ensureWritable(byteCount);
        }
        if(canUseUnsafeHeapWriteArrayAccess(c)){
            byte[] target=backingArray(c);
            int writeOffset=backingWriteOffset(c);
            if(NATIVE_BIG_ENDIAN && UNSAFE!=null){
                UNSAFE.copyMemory(values, LONG_ARRAY_OFFSET, target, BYTE_ARRAY_OFFSET+writeOffset, byteCount);
            }else{
                long offset=BYTE_ARRAY_OFFSET+writeOffset;
                for(long value: values){
                    UNSAFE.putLong(target, offset, Long.reverseBytes(value));
                    offset+=8;
                }
            }
            advanceWrite(c, byteCount);
            return;
        }
        for(long value: values){
            writeFixedLong(c, value);
        }
    }
    public static long[] readFixedLongArray(ByteCursor c){
        int n=readSize(c);
        long[] arr=new long[n];
        if(n==0){
            return arr;
        }
        if(canUseUnsafeArrayAccess(c)){
            byte[] source=backingArray(c);
            long offset=BYTE_ARRAY_OFFSET+backingOffset(c);
            for(int i=0;i<n;i++){
                long value=UNSAFE.getLong(source, offset);
                arr[i]=NATIVE_BIG_ENDIAN? value : Long.reverseBytes(value);
                offset+=8;
            }
            c.skip(n<<3);
            return arr;
        }
        for(int i=0;i<n;i++){
            arr[i]=readFixedLong(c);
        }
        return arr;
    }
    public static void readRawLongArray(ByteCursor c, long[] arr, int count){
        if(count<=0){
            return;
        }
        if(canUseUnsafeArrayAccess(c)){
            byte[] source=backingArray(c);
            long offset=BYTE_ARRAY_OFFSET+backingOffset(c);
            for(int i=0;i<count;i++){
                long value=UNSAFE.getLong(source, offset);
                arr[i]=NATIVE_BIG_ENDIAN? value : Long.reverseBytes(value);
                offset+=8;
            }
            c.skip(count<<3);
            return;
        }
        for(int i=0;i<count;i++){
            arr[i]=readFixedLong(c);
        }
    }
    public static long[] readRawLongArray(ByteCursor c){
        return readFixedLongArray(c);
    }
    public static LongArrayView readBorrowedRawLongArray(ByteCursor c){
        int count=readSize(c);
        if(count==0){
            return LongArrayView.empty();
        }
        if(canUseUnsafeArrayAccess(c)){
            LongArrayView view=LongArrayView.wrap(backingArray(c), backingOffset(c), count);
            c.skip(count<<3);
            return view;
        }
        long[] values=new long[count];
        readRawLongArray(c, values, count);
        return LongArrayView.of(values);
    }
    public static LongArrayView readBorrowedRawLongArray(ByteCursor c, int totalCount, int readCount){
        if(totalCount<=0 || readCount<=0){
            if(totalCount>0){
                c.skip(totalCount<<3);
            }
            return LongArrayView.empty();
        }
        int actualCount=Math.min(totalCount, readCount);
        if(canUseUnsafeArrayAccess(c)){
            LongArrayView view=LongArrayView.wrap(backingArray(c), backingOffset(c), actualCount);
            c.skip(totalCount<<3);
            return view;
        }
        long[] values=new long[actualCount];
        readRawLongArray(c, values, actualCount);
        if(totalCount>actualCount){
            c.skip((totalCount-actualCount)<<3);
        }
        return LongArrayView.of(values);
    }
    public static void writeBorrowedRawLongArray(ByteCursor c, LongArrayView value){
        LongArrayView view=value==null?LongArrayView.empty():value;
        int count=view.count();
        writeSize(c, count);
        if(count==0){
            return;
        }
        for(int i=0;i<count;i++){
            writeFixedLong(c, view.getLong(i));
        }
    }
    public static void writeBorrowedFixedLongArray(ByteCursor c, LongArrayView value, int fixedCount){
        LongArrayView view=value==null?LongArrayView.empty():value;
        if(view.count()>fixedCount){
            throw new IllegalArgumentException("array length "+view.count()+" exceeds fixedCount "+fixedCount);
        }
        for(int i=0;i<fixedCount;i++){
            writeFixedLong(c, i<view.count()?view.getLong(i):0L);
        }
    }
    public static void writeRawLongArray(ByteCursor c, long[] arr){
        writeFixedLongArray(c, arr);
    }
    public static LongArrayList readPackedLongList(ByteCursor c){
        return readPackedLongList(c, null);
    }
    public static LongArrayList readPackedLongList(ByteCursor c, LongArrayList reuse){
        int count=readSize(c);
        LongArrayList list=reuse==null?borrowLongArrayList(count):reuse;
        list.ensureCapacity(count);
        list.resize(count);
        readRawLongArray(c, list.rawArray(), count);
        return list;
    }
    public static void writePackedLongList(ByteCursor c, List<Long> list){
        int count=list==null?0:list.size();
        writeSize(c, count);
        if(count==0){
            return;
        }
        if(list instanceof LongArrayList){
            LongArrayList typed=(LongArrayList) list;
            long[] raw=typed.rawArray();
            for(int i=0;i<count;i++){
                writeFixedLong(c, raw[i]);
            }
            return;
        }
        if(list instanceof RandomAccess){
            for(int i=0;i<count;i++){
                Long value=list.get(i);
                writeFixedLong(c, value==null?0L:value);
            }
            return;
        }
        for(Long value: list){
            writeFixedLong(c, value==null?0L:value);
        }
    }
    public static void writeFixedShortArray(ByteCursor c,short[] arr){
        short[] values=arr==null? new short[0]: arr;
        writeSize(c, values.length);
        for(short value: values){
            writeFixedShort(c, value);
        }
    }
    public static short[] readFixedShortArray(ByteCursor c){
        int n=readSize(c);
        short[] arr=new short[n];
        for(int i=0;i<n;i++){
            arr[i]=readFixedShort(c);
        }
        return arr;
    }
    public static void writeFixedCharArray(ByteCursor c,char[] arr){
        char[] values=arr==null? new char[0]: arr;
        writeSize(c, values.length);
        for(char value: values){
            writeFixedChar(c, value);
        }
    }
    public static char[] readFixedCharArray(ByteCursor c){
        int n=readSize(c);
        char[] arr=new char[n];
        for(int i=0;i<n;i++){
            arr[i]=readFixedChar(c);
        }
        return arr;
    }
    public static void writeFloat(ByteCursor c,float v){ writeFixedInt(c, Float.floatToIntBits(v)); }
    public static float readFloat(ByteCursor c){ return Float.intBitsToFloat(readFixedInt(c)); }
    public static void skipFloat(ByteCursor c){ c.skip(4); }
    public static void readRawFloatArray(ByteCursor c, float[] arr, int count){
        if(count<=0){
            return;
        }
        if(canUseUnsafeArrayAccess(c)){
            byte[] source=backingArray(c);
            long offset=BYTE_ARRAY_OFFSET+backingOffset(c);
            for(int i=0;i<count;i++){
                int bits=UNSAFE.getInt(source, offset);
                arr[i]=Float.intBitsToFloat(NATIVE_BIG_ENDIAN? bits : Integer.reverseBytes(bits));
                offset+=4;
            }
            c.skip(count<<2);
            return;
        }
        for(int i=0;i<count;i++){
            arr[i]=readFloat(c);
        }
    }
    public static void writeFixedFloatArray(ByteCursor c,float[] arr){
        float[] values=arr==null? new float[0]: arr;
        writeSize(c, values.length);
        if(values.length==0){
            return;
        }
        int byteCount=values.length<<2;
        if(c instanceof LinearByteBuffer){
            ((LinearByteBuffer) c).ensureWritable(byteCount);
        }else if(c instanceof NettyCursor && ((NettyCursor) c).unwrap().hasArray()){
            ((NettyCursor) c).unwrap().ensureWritable(byteCount);
        }
        if(canUseUnsafeHeapWriteArrayAccess(c)){
            byte[] target=backingArray(c);
            int writeOffset=backingWriteOffset(c);
            if(NATIVE_BIG_ENDIAN && UNSAFE!=null){
                UNSAFE.copyMemory(values, FLOAT_ARRAY_OFFSET, target, BYTE_ARRAY_OFFSET+writeOffset, byteCount);
            }else{
                long offset=BYTE_ARRAY_OFFSET+writeOffset;
                for(float value: values){
                    int bits=Float.floatToRawIntBits(value);
                    UNSAFE.putInt(target, offset, Integer.reverseBytes(bits));
                    offset+=4;
                }
            }
            advanceWrite(c, byteCount);
            return;
        }
        for(float value: values){
            writeFloat(c, value);
        }
    }
    public static float[] readFixedFloatArray(ByteCursor c){
        int n=readSize(c);
        float[] arr=new float[n];
        if(n==0){
            return arr;
        }
        if(canUseUnsafeArrayAccess(c)){
            byte[] source=backingArray(c);
            long offset=BYTE_ARRAY_OFFSET+backingOffset(c);
            for(int i=0;i<n;i++){
                int bits=UNSAFE.getInt(source, offset);
                arr[i]=Float.intBitsToFloat(NATIVE_BIG_ENDIAN? bits : Integer.reverseBytes(bits));
                offset+=4;
            }
            c.skip(n<<2);
            return arr;
        }
        for(int i=0;i<n;i++){
            arr[i]=readFloat(c);
        }
        return arr;
    }
    public static float[] readRawFloatArray(ByteCursor c){
        return readFixedFloatArray(c);
    }
    public static FloatArrayView readBorrowedRawFloatArray(ByteCursor c){
        int count=readSize(c);
        if(count==0){
            return FloatArrayView.empty();
        }
        if(canUseUnsafeArrayAccess(c)){
            FloatArrayView view=FloatArrayView.wrap(backingArray(c), backingOffset(c), count);
            c.skip(count<<2);
            return view;
        }
        float[] values=new float[count];
        readRawFloatArray(c, values, count);
        return FloatArrayView.of(values);
    }
    public static FloatArrayView readBorrowedRawFloatArray(ByteCursor c, int totalCount, int readCount){
        if(totalCount<=0 || readCount<=0){
            if(totalCount>0){
                c.skip(totalCount<<2);
            }
            return FloatArrayView.empty();
        }
        int actualCount=Math.min(totalCount, readCount);
        if(canUseUnsafeArrayAccess(c)){
            FloatArrayView view=FloatArrayView.wrap(backingArray(c), backingOffset(c), actualCount);
            c.skip(totalCount<<2);
            return view;
        }
        float[] values=new float[actualCount];
        readRawFloatArray(c, values, actualCount);
        if(totalCount>actualCount){
            c.skip((totalCount-actualCount)<<2);
        }
        return FloatArrayView.of(values);
    }
    public static void writeBorrowedRawFloatArray(ByteCursor c, FloatArrayView value){
        FloatArrayView view=value==null?FloatArrayView.empty():value;
        int count=view.count();
        writeSize(c, count);
        if(count==0){
            return;
        }
        for(int i=0;i<count;i++){
            writeFloat(c, view.getFloat(i));
        }
    }
    public static void writeBorrowedFixedFloatArray(ByteCursor c, FloatArrayView value, int fixedCount){
        FloatArrayView view=value==null?FloatArrayView.empty():value;
        if(view.count()>fixedCount){
            throw new IllegalArgumentException("array length "+view.count()+" exceeds fixedCount "+fixedCount);
        }
        for(int i=0;i<fixedCount;i++){
            writeFloat(c, i<view.count()?view.getFloat(i):0F);
        }
    }
    public static void writeRawFloatArray(ByteCursor c, float[] arr){
        writeFixedFloatArray(c, arr);
    }
    public static void writeDouble(ByteCursor c,double v){ writeFixedLong(c, Double.doubleToLongBits(v)); }
    public static double readDouble(ByteCursor c){ return Double.longBitsToDouble(readFixedLong(c)); }
    public static void skipDouble(ByteCursor c){ c.skip(8); }
    public static void readRawDoubleArray(ByteCursor c, double[] arr, int count){
        if(count<=0){
            return;
        }
        if(canUseUnsafeArrayAccess(c)){
            byte[] source=backingArray(c);
            long offset=BYTE_ARRAY_OFFSET+backingOffset(c);
            for(int i=0;i<count;i++){
                long bits=UNSAFE.getLong(source, offset);
                arr[i]=Double.longBitsToDouble(NATIVE_BIG_ENDIAN? bits : Long.reverseBytes(bits));
                offset+=8;
            }
            c.skip(count<<3);
            return;
        }
        for(int i=0;i<count;i++){
            arr[i]=readDouble(c);
        }
    }
    public static void writeFixedDoubleArray(ByteCursor c,double[] arr){
        double[] values=arr==null? new double[0]: arr;
        writeSize(c, values.length);
        if(values.length==0){
            return;
        }
        int byteCount=values.length<<3;
        if(c instanceof LinearByteBuffer){
            ((LinearByteBuffer) c).ensureWritable(byteCount);
        }else if(c instanceof NettyCursor && ((NettyCursor) c).unwrap().hasArray()){
            ((NettyCursor) c).unwrap().ensureWritable(byteCount);
        }
        if(canUseUnsafeHeapWriteArrayAccess(c)){
            byte[] target=backingArray(c);
            int writeOffset=backingWriteOffset(c);
            if(NATIVE_BIG_ENDIAN && UNSAFE!=null){
                UNSAFE.copyMemory(values, DOUBLE_ARRAY_OFFSET, target, BYTE_ARRAY_OFFSET+writeOffset, byteCount);
            }else{
                long offset=BYTE_ARRAY_OFFSET+writeOffset;
                for(double value: values){
                    long bits=Double.doubleToRawLongBits(value);
                    UNSAFE.putLong(target, offset, Long.reverseBytes(bits));
                    offset+=8;
                }
            }
            advanceWrite(c, byteCount);
            return;
        }
        for(double value: values){
            writeDouble(c, value);
        }
    }
    public static double[] readFixedDoubleArray(ByteCursor c){
        int n=readSize(c);
        double[] arr=new double[n];
        if(n==0){
            return arr;
        }
        if(canUseUnsafeArrayAccess(c)){
            byte[] source=backingArray(c);
            long offset=BYTE_ARRAY_OFFSET+backingOffset(c);
            for(int i=0;i<n;i++){
                long bits=UNSAFE.getLong(source, offset);
                arr[i]=Double.longBitsToDouble(NATIVE_BIG_ENDIAN? bits : Long.reverseBytes(bits));
                offset+=8;
            }
            c.skip(n<<3);
            return arr;
        }
        for(int i=0;i<n;i++){
            arr[i]=readDouble(c);
        }
        return arr;
    }
    public static double[] readRawDoubleArray(ByteCursor c){
        return readFixedDoubleArray(c);
    }
    public static DoubleArrayView readBorrowedRawDoubleArray(ByteCursor c){
        int count=readSize(c);
        if(count==0){
            return DoubleArrayView.empty();
        }
        if(canUseUnsafeArrayAccess(c)){
            DoubleArrayView view=DoubleArrayView.wrap(backingArray(c), backingOffset(c), count);
            c.skip(count<<3);
            return view;
        }
        double[] values=new double[count];
        readRawDoubleArray(c, values, count);
        return DoubleArrayView.of(values);
    }
    public static DoubleArrayView readBorrowedRawDoubleArray(ByteCursor c, int totalCount, int readCount){
        if(totalCount<=0 || readCount<=0){
            if(totalCount>0){
                c.skip(totalCount<<3);
            }
            return DoubleArrayView.empty();
        }
        int actualCount=Math.min(totalCount, readCount);
        if(canUseUnsafeArrayAccess(c)){
            DoubleArrayView view=DoubleArrayView.wrap(backingArray(c), backingOffset(c), actualCount);
            c.skip(totalCount<<3);
            return view;
        }
        double[] values=new double[actualCount];
        readRawDoubleArray(c, values, actualCount);
        if(totalCount>actualCount){
            c.skip((totalCount-actualCount)<<3);
        }
        return DoubleArrayView.of(values);
    }
    public static void writeBorrowedRawDoubleArray(ByteCursor c, DoubleArrayView value){
        DoubleArrayView view=value==null?DoubleArrayView.empty():value;
        int count=view.count();
        writeSize(c, count);
        if(count==0){
            return;
        }
        for(int i=0;i<count;i++){
            writeDouble(c, view.getDouble(i));
        }
    }
    public static void writeBorrowedFixedDoubleArray(ByteCursor c, DoubleArrayView value, int fixedCount){
        DoubleArrayView view=value==null?DoubleArrayView.empty():value;
        if(view.count()>fixedCount){
            throw new IllegalArgumentException("array length "+view.count()+" exceeds fixedCount "+fixedCount);
        }
        for(int i=0;i<fixedCount;i++){
            writeDouble(c, i<view.count()?view.getDouble(i):0D);
        }
    }
    public static void readRawByteArray(ByteCursor c, byte[] arr, int count){
        if(count<=0){
            return;
        }
        if(canUseUnsafeArrayAccess(c)){
            byte[] source=backingArray(c);
            int offset=backingOffset(c);
            System.arraycopy(source, offset, arr, 0, count);
            c.skip(count);
            return;
        }
        c.readBytes(arr, 0, count);
    }
    public static void writeRawDoubleArray(ByteCursor c, double[] arr){
        writeFixedDoubleArray(c, arr);
    }
    public static void writeString(ByteCursor c,String s){
        String value=s==null? "": s;
        if(value.isEmpty()){
            writeSize(c,0);
            return;
        }
        if(tryWriteAsciiString(c, value)){
            return;
        }
        if(c instanceof NettyCursor){
            ByteBuf buf=((NettyCursor) c).unwrap();
            int byteCount=ByteBufUtil.utf8Bytes(value);
            writeSize(c,byteCount);
            ByteBufUtil.writeUtf8(buf, value);
            return;
        }
        Utf8Scratch scratch=UTF8_SCRATCH.get();
        int byteCount=scratch.encode(value);
        writeSize(c,byteCount);
        if(byteCount>0){
            c.writeBytes(scratch.buffer(),0,byteCount);
        }
    }
    public static String readString(ByteCursor c){
        int n=readSize(c);
        if(n==0) return "";
        if(c instanceof ArrayByteCursor){
            return ((ArrayByteCursor) c).readStringUtf8Fast(n);
        }
        if(c instanceof LinearByteBuffer){
            return ((LinearByteBuffer) c).readStringUtf8Fast(n);
        }
        if(c instanceof NettyCursor){
            ByteBuf buf=((NettyCursor) c).unwrap();
            int readerIndex=buf.readerIndex();
            if(buf.hasArray()){
                String value=new String(buf.array(), buf.arrayOffset()+readerIndex, n, StandardCharsets.UTF_8);
                buf.readerIndex(readerIndex+n);
                return value;
            }
            String value=buf.toString(readerIndex, n, StandardCharsets.UTF_8);
            buf.readerIndex(readerIndex+n);
            return value;
        }
        byte[] bytes=new byte[n];
        c.readBytes(bytes,0,n);
        return new String(bytes, StandardCharsets.UTF_8);
    }
    public static BorrowedString readBorrowedString(ByteCursor c){
        int n=readSize(c);
        if(n==0){
            return BorrowedString.empty();
        }
        if(hasHeapBackingArray(c)){
            BorrowedString borrowed=BorrowedString.wrap(backingArray(c), backingOffset(c), n);
            c.skip(n);
            return borrowed;
        }
        byte[] bytes=new byte[n];
        c.readBytes(bytes,0,n);
        return BorrowedString.wrap(bytes, 0, n);
    }
    public static void writeBorrowedString(ByteCursor c, BorrowedString value){
        BorrowedString borrowed=value==null?BorrowedString.empty():value;
        writeSize(c, borrowed.byteLength());
        if(borrowed.byteLength()!=0){
            BorrowedBytes bytes=borrowed.bytes();
            c.writeBytes(bytes.unsafeArray(), bytes.unsafeOffset(), bytes.length());
        }
    }
    public static void writeBorrowedFixedString(ByteCursor c, BorrowedString value, int fixedLength){
        if(fixedLength<0){
            throw new IllegalArgumentException("fixedLength must be >= 0");
        }
        BorrowedString borrowed=value==null?BorrowedString.empty():value;
        if(borrowed.byteLength()>fixedLength){
            throw new IllegalArgumentException("string encoded length "+borrowed.byteLength()+" exceeds fixedLength "+fixedLength);
        }
        if(borrowed.byteLength()!=0){
            BorrowedBytes bytes=borrowed.bytes();
            c.writeBytes(bytes.unsafeArray(), bytes.unsafeOffset(), bytes.length());
        }
        int padding=fixedLength-borrowed.byteLength();
        if(padding>0){
            writeZeroBytes(c, padding);
        }
    }
    public static void writeFixedString(ByteCursor c, String value, int fixedLength){
        if(fixedLength<0){
            throw new IllegalArgumentException("fixedLength must be >= 0");
        }
        String text=value==null? "": value;
        byte[] bytes=text.isEmpty()? EMPTY_BYTES:text.getBytes(StandardCharsets.UTF_8);
        if(bytes.length>fixedLength){
            throw new IllegalArgumentException("string encoded length "+bytes.length+" exceeds fixedLength "+fixedLength);
        }
        if(bytes.length!=0){
            c.writeBytes(bytes, 0, bytes.length);
        }
        int padding=fixedLength-bytes.length;
        if(padding>0){
            writeZeroBytes(c, padding);
        }
    }
    public static String readFixedString(ByteCursor c, int fixedLength){
        return readBorrowedFixedString(c, fixedLength).toString();
    }
    public static BorrowedString readBorrowedFixedString(ByteCursor c, int fixedLength){
        if(fixedLength<=0){
            return BorrowedString.empty();
        }
        if(hasHeapBackingArray(c)){
            byte[] array=backingArray(c);
            int offset=backingOffset(c);
            BorrowedString borrowed=BorrowedString.wrap(array, offset, trimTrailingZeros(array, offset, fixedLength));
            c.skip(fixedLength);
            return borrowed;
        }
        byte[] bytes=new byte[fixedLength];
        c.readBytes(bytes,0,fixedLength);
        return BorrowedString.wrap(bytes, 0, trimTrailingZeros(bytes, 0, fixedLength));
    }
    public static void skipString(ByteCursor c){
        int n=readSize(c);
        if(n!=0){
            c.skip(n);
        }
    }

    public static <T> void writeCollection(ByteCursor c, Collection<T> values, BiConsumer<ByteCursor,T> elemWriter){
        writeSize(c,values==null?0:values.size());
        if(values!=null){
            for(T t: values){
                elemWriter.accept(c,t);
            }
        }
    }
    public static <T, C extends Collection<T>> C readCollection(ByteCursor c, IntFunction<C> creator, Function<ByteCursor,T> elemReader){
        int n=readSize(c);
        C collection=creator.apply(n);
        for(int i=0;i<n;i++){
            collection.add(elemReader.apply(c));
        }
        return collection;
    }
    public static <T> void writeList(ByteCursor c, List<T> list, BiConsumer<ByteCursor,T> elemWriter){
        writeCollection(c,list,elemWriter);
    }
    public static <T, L extends List<T>> L readList(ByteCursor c, IntFunction<L> creator, Function<ByteCursor,T> elemReader){
        return readCollection(c,creator,elemReader);
    }
    public static <T> List<T> readList(ByteCursor c, Function<ByteCursor,T> elemReader){
        return readCollection(c, ArrayList::new, elemReader);
    }
    public static <T> void writeSet(ByteCursor c, Set<T> set, BiConsumer<ByteCursor,T> elemWriter){
        writeCollection(c,set,elemWriter);
    }
    public static <T, S extends Set<T>> S readSet(ByteCursor c, IntFunction<S> creator, Function<ByteCursor,T> elemReader){
        return readCollection(c,creator,elemReader);
    }
    public static <T> Set<T> readSet(ByteCursor c, Function<ByteCursor,T> elemReader){
        return readCollection(c, n->new HashSet<>(hashCapacity(n)), elemReader);
    }
    public static <T> LinkedHashSet<T> readLinkedSet(ByteCursor c, Function<ByteCursor,T> elemReader){
        return readCollection(c, n->new LinkedHashSet<>(hashCapacity(n)), elemReader);
    }
    public static <K,V> void writeMap(ByteCursor c, Map<K,V> map, BiConsumer<ByteCursor,K> keyWriter, BiConsumer<ByteCursor,V> valueWriter){
        writeSize(c,map==null?0:map.size());
        if(map!=null){
            for(Map.Entry<K,V> entry: map.entrySet()){
                keyWriter.accept(c, entry.getKey());
                valueWriter.accept(c, entry.getValue());
            }
        }
    }
    public static <K,V,M extends Map<K,V>> M readMap(ByteCursor c, IntFunction<M> creator, Function<ByteCursor,K> keyReader, Function<ByteCursor,V> valueReader){
        int n=readSize(c);
        M map=creator.apply(n);
        for(int i=0;i<n;i++){
            K key=keyReader.apply(c);
            V value=valueReader.apply(c);
            map.put(key,value);
        }
        return map;
    }
    public static <K,V> Map<K,V> readMap(ByteCursor c, Function<ByteCursor,K> keyReader, Function<ByteCursor,V> valueReader){
        return readMap(c, n->new HashMap<>(hashCapacity(n)), keyReader, valueReader);
    }
    public static <K,V> LinkedHashMap<K,V> readLinkedMap(ByteCursor c, Function<ByteCursor,K> keyReader, Function<ByteCursor,V> valueReader){
        return readMap(c, n->new LinkedHashMap<>(hashCapacity(n)), keyReader, valueReader);
    }

    public static void writePresenceBits(ByteCursor c, long bits, int fieldCount){
        int byteCount=(fieldCount+7)>>>3;
        switch (byteCount){
            case 0:
                return;
            case 1:
                writeLittleEndianBytes(c, bits, 1);
                return;
            case 2:
                writeLittleEndianBytes(c, bits, 2);
                return;
            case 3:
                writeLittleEndianBytes(c, bits, 2);
                c.writeByte((byte) (bits >>> 16));
                return;
            case 4:
                writeLittleEndianBytes(c, bits, 4);
                return;
            case 5:
                writeLittleEndianBytes(c, bits, 4);
                c.writeByte((byte) (bits >>> 32));
                return;
            case 6:
                writeLittleEndianBytes(c, bits, 4);
                writeLittleEndianBytes(c, bits >>> 32, 2);
                return;
            case 7:
                writeLittleEndianBytes(c, bits, 4);
                writeLittleEndianBytes(c, bits >>> 32, 2);
                c.writeByte((byte) (bits >>> 48));
                return;
            case 8:
                writeLittleEndianBytes(c, bits, 8);
                return;
            default:
                for(int i=0;i<byteCount;i++){
                    c.writeByte((byte) ((bits >>> (i<<3)) & 0xFFL));
                }
        }
    }
    public static long readPresenceBits(ByteCursor c, int fieldCount){
        int byteCount=(fieldCount+7)>>>3;
        switch (byteCount){
            case 0:
                return 0L;
            case 1:
                return readLittleEndianBytes(c, 1);
            case 2:
                return readLittleEndianBytes(c, 2);
            case 3:
                return readLittleEndianBytes(c, 2)
                        | ((long) (c.readByte() & 0xFF) << 16);
            case 4:
                return readLittleEndianBytes(c, 4);
            case 5:
                return readLittleEndianBytes(c, 4)
                        | ((long) (c.readByte() & 0xFF) << 32);
            case 6:
                return readLittleEndianBytes(c, 4)
                        | (readLittleEndianBytes(c, 2) << 32);
            case 7:
                return readLittleEndianBytes(c, 4)
                        | (readLittleEndianBytes(c, 2) << 32)
                        | ((long) (c.readByte() & 0xFF) << 48);
            case 8:
                return readLittleEndianBytes(c, 8);
            default:
                long bits=0L;
                for(int i=0;i<byteCount;i++){
                    bits |= (long) (c.readByte() & 0xFF) << (i<<3);
                }
                return bits;
        }
    }
    public static void writePresenceBits(ByteCursor c, long[] words, int fieldCount){
        int remaining=fieldCount;
        int wordCount=(fieldCount+63)>>>6;
        for(int i=0;i<wordCount;i++){
            int bitsForWord=Math.min(remaining, 64);
            writePresenceBits(c, words[i], bitsForWord);
            remaining-=bitsForWord;
        }
    }
    public static long[] readPresenceWords(ByteCursor c, int fieldCount){
        long[] words=new long[(fieldCount+63)>>>6];
        int remaining=fieldCount;
        for(int i=0;i<words.length;i++){
            int bitsForWord=Math.min(remaining, 64);
            words[i]=readPresenceBits(c, bitsForWord);
            remaining-=bitsForWord;
        }
        return words;
    }
    public static boolean isPresenceBitSet(long[] words, int index){
        return (words[index>>>6] & (1L << (index & 63))) != 0L;
    }

    public static <T> void writeOptional(ByteCursor c, Optional<T> optional, BiConsumer<ByteCursor,T> writer){
        boolean present=optional!=null && optional.isPresent();
        writeBoolean(c,present);
        if(present){
            writer.accept(c, optional.get());
        }
    }
    public static <T> Optional<T> readOptional(ByteCursor c, Function<ByteCursor,T> reader){
        if(!readBoolean(c)) return Optional.empty();
        return Optional.ofNullable(reader.apply(c));
    }

    public static void writeBytes(ByteCursor c, byte[] arr){
        writeSize(c, arr==null?0:arr.length);
        if(arr!=null && arr.length>0){
            c.writeBytes(arr,0,arr.length);
        }
    }
    public static byte[] readBytes(ByteCursor c){
        int n=readSize(c);
        if(n==0) return EMPTY_BYTES;
        byte[] arr=new byte[n];
        c.readBytes(arr,0,n);
        return arr;
    }
    public static BorrowedBytes readBorrowedBytes(ByteCursor c){
        int n=readSize(c);
        if(n==0){
            return BorrowedBytes.empty();
        }
        if(hasHeapBackingArray(c)){
            BorrowedBytes borrowed=BorrowedBytes.wrap(backingArray(c), backingOffset(c), n);
            c.skip(n);
            return borrowed;
        }
        byte[] arr=new byte[n];
        c.readBytes(arr,0,n);
        return BorrowedBytes.copyOf(arr);
    }
    public static BorrowedBytes readBorrowedBytes(ByteCursor c, int totalCount, int readCount){
        if(totalCount<=0 || readCount<=0){
            if(totalCount>0){
                c.skip(totalCount);
            }
            return BorrowedBytes.empty();
        }
        int actualCount=Math.min(totalCount, readCount);
        if(hasHeapBackingArray(c)){
            BorrowedBytes borrowed=BorrowedBytes.wrap(backingArray(c), backingOffset(c), actualCount);
            c.skip(totalCount);
            return borrowed;
        }
        byte[] arr=new byte[actualCount];
        c.readBytes(arr,0,actualCount);
        if(totalCount>actualCount){
            c.skip(totalCount-actualCount);
        }
        return BorrowedBytes.copyOf(arr);
    }
    public static void writeBorrowedBytes(ByteCursor c, BorrowedBytes value){
        BorrowedBytes borrowed=value==null?BorrowedBytes.empty():value;
        writeSize(c, borrowed.length());
        if(borrowed.length()!=0){
            c.writeBytes(borrowed.unsafeArray(), borrowed.unsafeOffset(), borrowed.length());
        }
    }
    public static void skipBytes(ByteCursor c){
        int n=readSize(c);
        if(n!=0){
            c.skip(n);
        }
    }
    public static BorrowedBytes readSampledBorrowedBytes(ByteCursor c, int totalCount, int[] sampleIndices){
        return BorrowedBytes.copyOf(readSampledBytes(c, null, totalCount, sampleIndices));
    }
    public static byte[] readSampledBytes(ByteCursor c, byte[] reuse, int totalCount, int[] sampleIndices){
        if(totalCount<=0){
            return EMPTY_BYTES;
        }
        if(sampleIndices==null || sampleIndices.length==0){
            c.skip(totalCount);
            return EMPTY_BYTES;
        }
        int selectedCount=0;
        int lastAccepted=-1;
        for(int sampleIndex: sampleIndices){
            if(sampleIndex>=0 && sampleIndex<totalCount && sampleIndex>lastAccepted){
                selectedCount++;
                lastAccepted=sampleIndex;
            }
        }
        if(selectedCount==0){
            c.skip(totalCount);
            return EMPTY_BYTES;
        }
        byte[] out=reuse!=null && reuse.length==selectedCount ? reuse : new byte[selectedCount];
        if(hasHeapBackingArray(c)){
            byte[] source=backingArray(c);
            int baseOffset=backingOffset(c);
            int outIndex=0;
            int lastIndex=-1;
            for(int sampleIndex: sampleIndices){
                if(sampleIndex>=0 && sampleIndex<totalCount && sampleIndex>lastIndex){
                    out[outIndex++]=source[baseOffset+sampleIndex];
                    lastIndex=sampleIndex;
                }
            }
            c.skip(totalCount);
            return out;
        }
        int cursorIndex=0;
        int outIndex=0;
        int lastIndex=-1;
        for(int sampleIndex: sampleIndices){
            if(sampleIndex<0 || sampleIndex>=totalCount || sampleIndex<=lastIndex){
                continue;
            }
            int delta=sampleIndex-cursorIndex;
            if(delta>0){
                c.skip(delta);
            }
            out[outIndex++]=c.readByte();
            cursorIndex=sampleIndex+1;
            lastIndex=sampleIndex;
        }
        if(totalCount>cursorIndex){
            c.skip(totalCount-cursorIndex);
        }
        return out;
    }
    public static int[] readSampledFixedIntArray(ByteCursor c, int[] reuse, int totalCount, int[] sampleIndices){
        if(totalCount<=0){
            return new int[0];
        }
        if(sampleIndices==null || sampleIndices.length==0){
            c.skip(totalCount<<2);
            return new int[0];
        }
        int selectedCount=0;
        int lastAccepted=-1;
        for(int sampleIndex: sampleIndices){
            if(sampleIndex>=0 && sampleIndex<totalCount && sampleIndex>lastAccepted){
                selectedCount++;
                lastAccepted=sampleIndex;
            }
        }
        if(selectedCount==0){
            c.skip(totalCount<<2);
            return new int[0];
        }
        int[] out=reuse!=null && reuse.length==selectedCount ? reuse : new int[selectedCount];
        if(canUseUnsafeArrayAccess(c)){
            byte[] source=backingArray(c);
            long baseOffset=BYTE_ARRAY_OFFSET+backingOffset(c);
            int outIndex=0;
            int lastIndex=-1;
            for(int sampleIndex: sampleIndices){
                if(sampleIndex>=0 && sampleIndex<totalCount && sampleIndex>lastIndex){
                    int bits=UNSAFE.getInt(source, baseOffset+((long) sampleIndex<<2));
                    out[outIndex++]=NATIVE_BIG_ENDIAN? bits : Integer.reverseBytes(bits);
                    lastIndex=sampleIndex;
                }
            }
            c.skip(totalCount<<2);
            return out;
        }
        int cursorIndex=0;
        int outIndex=0;
        int lastIndex=-1;
        for(int sampleIndex: sampleIndices){
            if(sampleIndex<0 || sampleIndex>=totalCount || sampleIndex<=lastIndex){
                continue;
            }
            int delta=sampleIndex-cursorIndex;
            if(delta>0){
                c.skip(delta<<2);
            }
            out[outIndex++]=readFixedInt(c);
            cursorIndex=sampleIndex+1;
            lastIndex=sampleIndex;
        }
        if(totalCount>cursorIndex){
            c.skip((totalCount-cursorIndex)<<2);
        }
        return out;
    }
    public static long[] readSampledFixedLongArray(ByteCursor c, long[] reuse, int totalCount, int[] sampleIndices){
        if(totalCount<=0){
            return new long[0];
        }
        if(sampleIndices==null || sampleIndices.length==0){
            c.skip(totalCount<<3);
            return new long[0];
        }
        int selectedCount=0;
        int lastAccepted=-1;
        for(int sampleIndex: sampleIndices){
            if(sampleIndex>=0 && sampleIndex<totalCount && sampleIndex>lastAccepted){
                selectedCount++;
                lastAccepted=sampleIndex;
            }
        }
        if(selectedCount==0){
            c.skip(totalCount<<3);
            return new long[0];
        }
        long[] out=reuse!=null && reuse.length==selectedCount ? reuse : new long[selectedCount];
        if(canUseUnsafeArrayAccess(c)){
            byte[] source=backingArray(c);
            long baseOffset=BYTE_ARRAY_OFFSET+backingOffset(c);
            int outIndex=0;
            int lastIndex=-1;
            for(int sampleIndex: sampleIndices){
                if(sampleIndex>=0 && sampleIndex<totalCount && sampleIndex>lastIndex){
                    long bits=UNSAFE.getLong(source, baseOffset+(((long) sampleIndex)<<3));
                    out[outIndex++]=NATIVE_BIG_ENDIAN? bits : Long.reverseBytes(bits);
                    lastIndex=sampleIndex;
                }
            }
            c.skip(totalCount<<3);
            return out;
        }
        int cursorIndex=0;
        int outIndex=0;
        int lastIndex=-1;
        for(int sampleIndex: sampleIndices){
            if(sampleIndex<0 || sampleIndex>=totalCount || sampleIndex<=lastIndex){
                continue;
            }
            int delta=sampleIndex-cursorIndex;
            if(delta>0){
                c.skip(delta<<3);
            }
            out[outIndex++]=readFixedLong(c);
            cursorIndex=sampleIndex+1;
            lastIndex=sampleIndex;
        }
        if(totalCount>cursorIndex){
            c.skip((totalCount-cursorIndex)<<3);
        }
        return out;
    }
    public static float[] readSampledFixedFloatArray(ByteCursor c, float[] reuse, int totalCount, int[] sampleIndices){
        if(totalCount<=0){
            return new float[0];
        }
        if(sampleIndices==null || sampleIndices.length==0){
            c.skip(totalCount<<2);
            return new float[0];
        }
        int selectedCount=0;
        int lastAccepted=-1;
        for(int sampleIndex: sampleIndices){
            if(sampleIndex>=0 && sampleIndex<totalCount && sampleIndex>lastAccepted){
                selectedCount++;
                lastAccepted=sampleIndex;
            }
        }
        if(selectedCount==0){
            c.skip(totalCount<<2);
            return new float[0];
        }
        float[] out=reuse!=null && reuse.length==selectedCount ? reuse : new float[selectedCount];
        if(canUseUnsafeArrayAccess(c)){
            byte[] source=backingArray(c);
            long baseOffset=BYTE_ARRAY_OFFSET+backingOffset(c);
            int outIndex=0;
            int lastIndex=-1;
            for(int sampleIndex: sampleIndices){
                if(sampleIndex>=0 && sampleIndex<totalCount && sampleIndex>lastIndex){
                    int bits=UNSAFE.getInt(source, baseOffset+((long) sampleIndex<<2));
                    out[outIndex++]=Float.intBitsToFloat(NATIVE_BIG_ENDIAN? bits : Integer.reverseBytes(bits));
                    lastIndex=sampleIndex;
                }
            }
            c.skip(totalCount<<2);
            return out;
        }
        int cursorIndex=0;
        int outIndex=0;
        int lastIndex=-1;
        for(int sampleIndex: sampleIndices){
            if(sampleIndex<0 || sampleIndex>=totalCount || sampleIndex<=lastIndex){
                continue;
            }
            int delta=sampleIndex-cursorIndex;
            if(delta>0){
                c.skip(delta<<2);
            }
            out[outIndex++]=readFloat(c);
            cursorIndex=sampleIndex+1;
            lastIndex=sampleIndex;
        }
        if(totalCount>cursorIndex){
            c.skip((totalCount-cursorIndex)<<2);
        }
        return out;
    }
    public static double[] readSampledFixedDoubleArray(ByteCursor c, double[] reuse, int totalCount, int[] sampleIndices){
        if(totalCount<=0){
            return new double[0];
        }
        if(sampleIndices==null || sampleIndices.length==0){
            c.skip(totalCount<<3);
            return new double[0];
        }
        int selectedCount=0;
        int lastAccepted=-1;
        for(int sampleIndex: sampleIndices){
            if(sampleIndex>=0 && sampleIndex<totalCount && sampleIndex>lastAccepted){
                selectedCount++;
                lastAccepted=sampleIndex;
            }
        }
        if(selectedCount==0){
            c.skip(totalCount<<3);
            return new double[0];
        }
        double[] out=reuse!=null && reuse.length==selectedCount ? reuse : new double[selectedCount];
        if(canUseUnsafeArrayAccess(c)){
            byte[] source=backingArray(c);
            long baseOffset=BYTE_ARRAY_OFFSET+backingOffset(c);
            int outIndex=0;
            int lastIndex=-1;
            for(int sampleIndex: sampleIndices){
                if(sampleIndex>=0 && sampleIndex<totalCount && sampleIndex>lastIndex){
                    long bits=UNSAFE.getLong(source, baseOffset+(((long) sampleIndex)<<3));
                    out[outIndex++]=Double.longBitsToDouble(NATIVE_BIG_ENDIAN? bits : Long.reverseBytes(bits));
                    lastIndex=sampleIndex;
                }
            }
            c.skip(totalCount<<3);
            return out;
        }
        int cursorIndex=0;
        int outIndex=0;
        int lastIndex=-1;
        for(int sampleIndex: sampleIndices){
            if(sampleIndex<0 || sampleIndex>=totalCount || sampleIndex<=lastIndex){
                continue;
            }
            int delta=sampleIndex-cursorIndex;
            if(delta>0){
                c.skip(delta<<3);
            }
            out[outIndex++]=readDouble(c);
            cursorIndex=sampleIndex+1;
            lastIndex=sampleIndex;
        }
        if(totalCount>cursorIndex){
            c.skip((totalCount-cursorIndex)<<3);
        }
        return out;
    }
    public static void writeIntArray(ByteCursor c,int[] arr){
        writeSize(c, arr==null?0:arr.length);
        if(arr!=null){
            for(int v: arr) writeInt(c,v);
        }
    }
    public static int[] readIntArray(ByteCursor c){
        int n=readSize(c);
        int[] arr=new int[n];
        for(int i=0;i<n;i++) arr[i]=readInt(c);
        return arr;
    }
    public static void writeLongArray(ByteCursor c,long[] arr){
        writeSize(c, arr==null?0:arr.length);
        if(arr!=null){
            for(long v: arr) writeLong(c,v);
        }
    }
    public static long[] readLongArray(ByteCursor c){
        int n=readSize(c);
        long[] arr=new long[n];
        for(int i=0;i<n;i++) arr[i]=readLong(c);
        return arr;
    }
    public static void writeShortArray(ByteCursor c,short[] arr){
        writeSize(c, arr==null?0:arr.length);
        if(arr!=null){
            for(short v: arr) writeShort(c,v);
        }
    }
    public static short[] readShortArray(ByteCursor c){
        int n=readSize(c);
        short[] arr=new short[n];
        for(int i=0;i<n;i++) arr[i]=readShort(c);
        return arr;
    }
    public static void writeBooleanArray(ByteCursor c,boolean[] arr){
        writeSize(c, arr==null?0:arr.length);
        if(arr!=null){
            for(boolean v: arr) writeBoolean(c,v);
        }
    }
    public static boolean[] readBooleanArray(ByteCursor c){
        int n=readSize(c);
        boolean[] arr=new boolean[n];
        for(int i=0;i<n;i++) arr[i]=readBoolean(c);
        return arr;
    }
    public static void writeCharArray(ByteCursor c,char[] arr){
        writeSize(c, arr==null?0:arr.length);
        if(arr!=null){
            for(char v: arr) writeChar(c,v);
        }
    }
    public static char[] readCharArray(ByteCursor c){
        int n=readSize(c);
        char[] arr=new char[n];
        for(int i=0;i<n;i++) arr[i]=readChar(c);
        return arr;
    }
    public static void writeFloatArray(ByteCursor c,float[] arr){
        writeFixedFloatArray(c, arr);
    }
    public static float[] readFloatArray(ByteCursor c){
        return readFixedFloatArray(c);
    }
    public static void writeDoubleArray(ByteCursor c,double[] arr){
        writeFixedDoubleArray(c, arr);
    }
    public static double[] readDoubleArray(ByteCursor c){
        return readFixedDoubleArray(c);
    }
    public static <T> void writeObjectArray(ByteCursor c, T[] arr, BiConsumer<ByteCursor,T> writer){
        writeSize(c, arr==null?0:arr.length);
        if(arr!=null){
            for(T value: arr){
                writer.accept(c,value);
            }
        }
    }
    public static <T> T[] readObjectArray(ByteCursor c, IntFunction<T[]> creator, Function<ByteCursor,T> reader){
        int n=readSize(c);
        T[] arr=creator.apply(n);
        for(int i=0;i<n;i++) arr[i]=reader.apply(c);
        return arr;
    }

    private static int trimTrailingZeros(byte[] bytes, int offset, int length){
        int end=offset+length;
        while(end>offset && bytes[end-1]==0){
            end--;
        }
        return end-offset;
    }
}




