package com.zero.codegen.runtime.bytes;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

public final class IntIntHashMap extends AbstractMap<Integer, Integer> {
    public static final class Cursor {
        private final IntIntHashMap owner;
        private int index = -1;
        private int key;
        private int value;

        Cursor(IntIntHashMap owner) {
            this.owner = owner;
        }

        public boolean advance() {
            byte[] states = owner.states;
            int[] keys = owner.keys;
            int[] values = owner.values;
            while (++index < states.length) {
                if (states[index] == USED) {
                    key = keys[index];
                    value = values[index];
                    return true;
                }
            }
            return false;
        }

        public int key() {
            return key;
        }

        public int valueInt() {
            return value;
        }
    }

    private static final byte FREE = 0;
    private static final byte USED = 1;
    private static final byte REMOVED = 2;

    private int[] keys;
    private int[] values;
    private byte[] states;
    private int size;
    private int threshold;

    public IntIntHashMap() {
        this(4);
    }

    public IntIntHashMap(int expectedSize) {
        int capacity = 1;
        int target = Math.max(4, expectedSize << 1);
        while (capacity < target) {
            capacity <<= 1;
        }
        keys = new int[capacity];
        values = new int[capacity];
        states = new byte[capacity];
        threshold = Math.max(1, capacity * 2 / 3);
    }

    public int getInt(int key) {
        int index = findIndex(key);
        return index < 0 ? 0 : values[index];
    }

    public Integer getBoxed(int key) {
        int index = findIndex(key);
        return index < 0 ? null : values[index];
    }

    public int putInt(int key, int value) {
        ensureCapacity(size + 1);
        int index = findInsertIndex(key);
        if (states[index] == USED) {
            int previous = values[index];
            values[index] = value;
            return previous;
        }
        states[index] = USED;
        keys[index] = key;
        values[index] = value;
        size++;
        return 0;
    }

    public Cursor cursor() {
        return new Cursor(this);
    }

    @Override
    public Integer get(Object key) {
        if (!(key instanceof Integer)) {
            return null;
        }
        return getBoxed((Integer) key);
    }

    @Override
    public Integer put(Integer key, Integer value) {
        Integer previous = get(key);
        putInt(key == null ? 0 : key, value == null ? 0 : value);
        return previous;
    }

    @Override
    public void clear() {
        java.util.Arrays.fill(states, FREE);
        size = 0;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public Set<Entry<Integer, Integer>> entrySet() {
        return new AbstractSet<>() {
            @Override
            public Iterator<Entry<Integer, Integer>> iterator() {
                return new Iterator<>() {
                    private final Cursor cursor = new Cursor(IntIntHashMap.this);

                    @Override
                    public boolean hasNext() {
                        int probe = cursor.index + 1;
                        while (probe < states.length) {
                            if (states[probe] == USED) {
                                return true;
                            }
                            probe++;
                        }
                        return false;
                    }

                    @Override
                    public Entry<Integer, Integer> next() {
                        if (!cursor.advance()) {
                            throw new NoSuchElementException();
                        }
                        return new SimpleImmutableEntry<>(cursor.key(), cursor.valueInt());
                    }
                };
            }

            @Override
            public int size() {
                return IntIntHashMap.this.size;
            }
        };
    }

    private int findIndex(int key) {
        int mask = keys.length - 1;
        int index = mix(key) & mask;
        while (true) {
            byte state = states[index];
            if (state == FREE) {
                return -1;
            }
            if (state == USED && keys[index] == key) {
                return index;
            }
            index = (index + 1) & mask;
        }
    }

    private int findInsertIndex(int key) {
        int mask = keys.length - 1;
        int index = mix(key) & mask;
        int firstRemoved = -1;
        while (true) {
            byte state = states[index];
            if (state == FREE) {
                return firstRemoved >= 0 ? firstRemoved : index;
            }
            if (state == USED && keys[index] == key) {
                return index;
            }
            if (state == REMOVED && firstRemoved < 0) {
                firstRemoved = index;
            }
            index = (index + 1) & mask;
        }
    }

    public void ensureCapacity(int requiredSize) {
        if (requiredSize <= threshold) {
            return;
        }
        rehash(keys.length << 1);
    }

    private void rehash(int newCapacity) {
        int[] oldKeys = keys;
        int[] oldValues = values;
        byte[] oldStates = states;
        keys = new int[newCapacity];
        values = new int[newCapacity];
        states = new byte[newCapacity];
        threshold = Math.max(1, newCapacity * 2 / 3);
        int oldSize = size;
        size = 0;
        for (int i = 0; i < oldStates.length; i++) {
            if (oldStates[i] == USED) {
                putInt(oldKeys[i], oldValues[i]);
            }
        }
        size = oldSize;
    }

    private static int mix(int value) {
        int h = value * 0x9E3779B9;
        return h ^ (h >>> 16);
    }
}
