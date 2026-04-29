package com.zero.codegen.runtime.bytes;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;

public final class IntObjectHashMap<V> extends AbstractMap<Integer, V> {
    @FunctionalInterface
    public interface IntObjectConsumer<V> {
        void accept(int key, V value);
    }

    public static final class Cursor<V> {
        private final IntObjectHashMap<V> owner;
        private int index=-1;
        private int key;
        private V value;

        Cursor(IntObjectHashMap<V> owner) {
            this.owner=owner;
        }

        public boolean advance() {
            byte[] states=owner.states;
            Object[] values=owner.values;
            int[] keys=owner.keys;
            while(++index<states.length){
                if(states[index]==USED){
                    key=keys[index];
                    @SuppressWarnings("unchecked")
                    V cast=(V) values[index];
                    value=cast;
                    return true;
                }
            }
            return false;
        }

        public int key() {
            return key;
        }

        public V value() {
            return value;
        }
    }

    private static final byte FREE=0;
    private static final byte USED=1;
    private static final byte REMOVED=2;

    private int[] keys;
    private Object[] values;
    private byte[] states;
    private int size;
    private int threshold;

    public IntObjectHashMap() {
        this(4);
    }

    public IntObjectHashMap(int expectedSize) {
        int capacity=1;
        int target=Math.max(4, expectedSize<<1);
        while(capacity<target){
            capacity<<=1;
        }
        keys=new int[capacity];
        values=new Object[capacity];
        states=new byte[capacity];
        threshold=Math.max(1, capacity*2/3);
    }

    public V getInt(int key) {
        int index=findIndex(key);
        if(index<0){
            return null;
        }
        @SuppressWarnings("unchecked")
        V cast=(V) values[index];
        return cast;
    }

    public V putInt(int key, V value) {
        ensureCapacity(size+1);
        int index=findInsertIndex(key);
        if(states[index]==USED){
            @SuppressWarnings("unchecked")
            V previous=(V) values[index];
            values[index]=value;
            return previous;
        }
        states[index]=USED;
        keys[index]=key;
        values[index]=value;
        size++;
        return null;
    }

    public Cursor<V> cursor() {
        return new Cursor<>(this);
    }

    public void forEachIntEntry(IntObjectConsumer<? super V> consumer) {
        Objects.requireNonNull(consumer, "consumer");
        for(int i=0;i<states.length;i++){
            if(states[i]==USED){
                @SuppressWarnings("unchecked")
                V cast=(V) values[i];
                consumer.accept(keys[i], cast);
            }
        }
    }

    @Override
    public V get(Object key) {
        if(!(key instanceof Integer)){
            return null;
        }
        return getInt((Integer) key);
    }

    @Override
    public V put(Integer key, V value) {
        return putInt(key==null?0:key, value);
    }

    @Override
    public void clear() {
        java.util.Arrays.fill(states, FREE);
        java.util.Arrays.fill(values, null);
        size=0;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public Set<Entry<Integer, V>> entrySet() {
        return new AbstractSet<>() {
            @Override
            public Iterator<Entry<Integer, V>> iterator() {
                return new Iterator<>() {
                    private final Cursor<V> cursor=new Cursor<>(IntObjectHashMap.this);

                    @Override
                    public boolean hasNext() {
                        if(cursor.index>=0 && cursor.index<states.length && states[cursor.index]==USED){
                            int probe=cursor.index+1;
                            while(probe<states.length){
                                if(states[probe]==USED){
                                    return true;
                                }
                                probe++;
                            }
                            return false;
                        }
                        int probe=0;
                        while(probe<states.length){
                            if(states[probe]==USED){
                                return true;
                            }
                            probe++;
                        }
                        return false;
                    }

                    @Override
                    public Entry<Integer, V> next() {
                        if(!cursor.advance()){
                            throw new NoSuchElementException();
                        }
                        return new SimpleImmutableEntry<>(cursor.key(), cursor.value());
                    }
                };
            }

            @Override
            public int size() {
                return IntObjectHashMap.this.size;
            }
        };
    }

    private int findIndex(int key) {
        int mask=keys.length-1;
        int index=mix(key)&mask;
        while(true){
            byte state=states[index];
            if(state==FREE){
                return -1;
            }
            if(state==USED && keys[index]==key){
                return index;
            }
            index=(index+1)&mask;
        }
    }

    private int findInsertIndex(int key) {
        int mask=keys.length-1;
        int index=mix(key)&mask;
        int firstRemoved=-1;
        while(true){
            byte state=states[index];
            if(state==FREE){
                return firstRemoved>=0?firstRemoved:index;
            }
            if(state==USED && keys[index]==key){
                return index;
            }
            if(state==REMOVED && firstRemoved<0){
                firstRemoved=index;
            }
            index=(index+1)&mask;
        }
    }

    public void ensureCapacity(int requiredSize) {
        if(requiredSize<=threshold){
            return;
        }
        rehash(keys.length<<1);
    }

    private void rehash(int newCapacity) {
        int[] oldKeys=keys;
        Object[] oldValues=values;
        byte[] oldStates=states;
        keys=new int[newCapacity];
        values=new Object[newCapacity];
        states=new byte[newCapacity];
        threshold=Math.max(1, newCapacity*2/3);
        int oldSize=size;
        size=0;
        for(int i=0;i<oldStates.length;i++){
            if(oldStates[i]==USED){
                @SuppressWarnings("unchecked")
                V cast=(V) oldValues[i];
                putInt(oldKeys[i], cast);
            }
        }
        size=oldSize;
    }

    private static int mix(int value) {
        int h=value*0x9E3779B9;
        return h^(h>>>16);
    }
}
