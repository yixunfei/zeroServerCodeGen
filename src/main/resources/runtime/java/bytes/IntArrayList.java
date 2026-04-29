package com.zero.codegen.runtime.bytes;

import java.util.AbstractList;
import java.util.Arrays;
import java.util.RandomAccess;

public final class IntArrayList extends AbstractList<Integer> implements RandomAccess {
    private static final int[] EMPTY = new int[0];

    private int[] elements = EMPTY;
    private int size;

    public IntArrayList() {
    }

    public IntArrayList(int initialCapacity) {
        ensureCapacity(initialCapacity);
    }

    public int getInt(int index) {
        if(index<0 || index>=size){
            throw new IndexOutOfBoundsException("index="+index+", size="+size);
        }
        return elements[index];
    }

    public int[] rawArray() {
        return elements;
    }

    public void addInt(int value) {
        ensureCapacity(size+1);
        elements[size++]=value;
        modCount++;
    }

    public int setInt(int index, int value) {
        int previous=getInt(index);
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
    public Integer get(int index) {
        return getInt(index);
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean add(Integer value) {
        addInt(value==null?0:value);
        return true;
    }

    @Override
    public void add(int index, Integer element) {
        if(index<0 || index>size){
            throw new IndexOutOfBoundsException("index="+index+", size="+size);
        }
        ensureCapacity(size+1);
        System.arraycopy(elements, index, elements, index+1, size-index);
        elements[index]=element==null?0:element;
        size++;
        modCount++;
    }

    @Override
    public Integer set(int index, Integer element) {
        return setInt(index, element==null?0:element);
    }

    @Override
    public Integer remove(int index) {
        int previous=getInt(index);
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
