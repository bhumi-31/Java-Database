package com.database.index;

import java.util.HashMap;
import java.util.Map;

/**
 * Hash-based index implementation for fast equality lookups.
 * Provides O(1) average time complexity for insert, search, and delete operations.
 */
public class HashIndex<K extends Comparable<K>, V> implements Index<K, V> {
    private final Map<K, V> hashMap;
    
    public HashIndex() {
        this.hashMap = new HashMap<>();
    }
    
    public HashIndex(int initialCapacity) {
        this.hashMap = new HashMap<>(initialCapacity);
    }

    @Override
    public void insert(K key, V value) {
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }
        hashMap.put(key, value);
    }

    @Override
    public V search(K key) {
        if (key == null) {
            return null;
        }
        return hashMap.get(key);
    }

    @Override
    public boolean delete(K key) {
        if (key == null) {
            return false;
        }
        return hashMap.remove(key) != null;
    }

    @Override
    public int size() {
        return hashMap.size();
    }

    @Override
    public boolean isEmpty() {
        return hashMap.isEmpty();
    }

    @Override
    public void clear() {
        hashMap.clear();
    }

    /**
     * Check if the index contains the specified key.
     */
    public boolean containsKey(K key) {
        return hashMap.containsKey(key);
    }

    /**
     * Check if the index contains the specified value.
     */
    public boolean containsValue(V value) {
        return hashMap.containsValue(value);
    }

    @Override
    public String toString() {
        return "HashIndex{" +
                "size=" + size() +
                ", entries=" + hashMap +
                '}';
    }
}
