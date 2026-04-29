package com.zero.codegen.runtime.bytes;

import java.util.AbstractList;
import java.util.Arrays;
import java.util.RandomAccess;

public final class LongArrayList extends AbstractList<Long> implements RandomAccess {
    private static final long[] EMPTY = new long[0];

    private long[] elements = EMPTY;
    private int size;

    public LongArrayList() {
    }

    public LongArrayList(int initialCapacity) {
        ensureCapacity(initialCapacity);
    }

    public long getLong(int index) {
        if(index<0 || index>=size){
            throw new IndexOutOfBoundsException("index="+index+", size="+size);
        }
        return elements[index];
    }

    public long[] rawArray() {
        return elements;
    }

    public void addLong(long value) {
        ensureCapacity(size+1);
        elements[size++]=value;
        modCount++;
    }

    public long setLong(int index, long value) {
        long previous=getLong(index);
        elements[index]=value;
        return previous;
    }

    public void truncate(int newSize) {
        if(newSize<0 || newSize>size){
            throw new IndexOutOfBoundsException("newSize="+newSize+", size="+size);
        }
        if(newSize!=size){
            size=newSize;
            modCount++;
        }
    }

    public void resize(int newSize) {
        if(newSize<0){
            throw new IndexOutOfBoundsException("newSize="+newSize+", size="+size);
        }
        ensureCapacity(newSize);
        if(newSize!=size){
            size=newSize;
            modCount++;
        }
    }

    public void ensureCapacity(int capacity) {
        if(capacity<=elements.length){
            return;
        }
        int newCapacity=Math.max(4, elements.length==0?4:elements.length);
        while(newCapacity<capacity){
            newCapacity<<=1;
        }
        elements=Arrays.copyOf(elements, newCapacity);
    }

    @Override
    public Long get(int index) {
        return getLong(index);
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean add(Long value) {
        addLong(value==null?0L:value);
        return true;
    }

    @Override
    public void add(int index, Long element) {
        if(index<0 || index>size){
            throw new IndexOutOfBoundsException("index="+index+", size="+size);
        }
        ensureCapacity(size+1);
        System.arraycopy(elements, index, elements, index+1, size-index);
        elements[index]=element==null?0L:element;
        size++;
        modCount++;
    }

    @Override
    public Long set(int index, Long element) {
        return setLong(index, element==null?0L:element);
    }

    @Override
    public Long remove(int index) {
        long previous=getLong(index);
        int moved=size-index-1;
        if(moved>0){
            System.arraycopy(elements, index+1, elements, index, moved);
        }
        size--;
        modCount++;
        return previous;
    }

    @Override
    public void clear() {
        if(size!=0){
            size=0;
            modCount++;
        }
    }

    @Override
    protected void removeRange(int fromIndex, int toIndex) {
        if(fromIndex<0 || toIndex>size || fromIndex>toIndex){
            throw new IndexOutOfBoundsException("from="+fromIndex+", to="+toIndex+", size="+size);
        }
        int moved=size-toIndex;
        if(moved>0){
            System.arraycopy(elements, toIndex, elements, fromIndex, moved);
        }
        size-=toIndex-fromIndex;
        modCount++;
    }
}
