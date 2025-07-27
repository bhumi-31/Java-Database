package com.database.index;

import java.io.Serializable;

/**
 * Generic interface for database indexes.
 */
public interface Index<K extends Comparable<K>, V> extends Serializable {
    
    /**
     * Inserts a key-value pair into the index.
     */
    void insert(K key, V value);
    
    /**
     * Searches for a value by key.
     */
    V search(K key);
    
    /**
     * Deletes a key-value pair from the index.
     * @return true if the key was found and deleted, false otherwise
     */
    boolean delete(K key);
    
    /**
     * Returns the number of entries in the index.
     */
    int size();
    
    /**
     * Checks if the index is empty.
     */
    boolean isEmpty();
    
    /**
     * Clears all entries from the index.
     */
    void clear();
}
