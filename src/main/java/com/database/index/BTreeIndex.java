package com.database.index;

import java.util.ArrayList;
import java.util.List;

/**
 * B-Tree index implementation for efficient range queries and sorted access.
 */
public class BTreeIndex<K extends Comparable<K>, V> implements Index<K, V> {
    private BTreeNode<K, V> root;
    private final int order;
    private int size;

    public BTreeIndex() {
        this(4); // Default order of 4
    }

    public BTreeIndex(int order) {
        if (order < 3) {
            throw new IllegalArgumentException("B-Tree order must be at least 3");
        }
        this.order = order;
        this.root = new BTreeNode<>(order, true);
        this.size = 0;
    }

    @Override
    public void insert(K key, V value) {
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }

        BTreeNode<K, V> leaf = findLeafNode(key);
        insertIntoLeaf(leaf, key, value);
        size++;
    }

    @Override
    public V search(K key) {
        if (key == null) {
            return null;
        }

        BTreeNode<K, V> node = root;
        
        while (node != null) {
            int index = node.findKeyIndex(key);
            
            if (index < node.getKeys().size() && 
                key.compareTo(node.getKeys().get(index)) == 0) {
                return node.getValues().get(index);
            }
            
            if (node.isLeaf()) {
                return null; // Key not found
            }
            
            node = node.getChildren().get(index);
        }
        
        return null;
    }

    @Override
    public boolean delete(K key) {
        if (key == null) {
            return false;
        }

        boolean deleted = deleteFromTree(root, key);
        if (deleted) {
            size--;
        }
        return deleted;
    }

    /**
     * Performs a range query returning all values with keys between start and end (inclusive).
     */
    public List<V> rangeQuery(K start, K end) {
        List<V> results = new ArrayList<>();
        if (start == null || end == null || start.compareTo(end) > 0) {
            return results;
        }
        
        rangeQueryHelper(root, start, end, results);
        return results;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public void clear() {
        root = new BTreeNode<>(order, true);
        size = 0;
    }

    /**
     * Finds the leaf node where a key should be inserted.
     */
    private BTreeNode<K, V> findLeafNode(K key) {
        BTreeNode<K, V> current = root;
        
        while (!current.isLeaf()) {
            int index = current.findKeyIndex(key);
            current = current.getChildren().get(index);
        }
        
        return current;
    }

    /**
     * Inserts a key-value pair into a leaf node.
     */
    private void insertIntoLeaf(BTreeNode<K, V> leaf, K key, V value) {
        int index = leaf.findKeyIndex(key);
        
        // Check for duplicate key
        if (index < leaf.getKeys().size() && 
            key.compareTo(leaf.getKeys().get(index)) == 0) {
            // Update existing value
            leaf.getValues().set(index, value);
            size--; // Don't increment size for updates
            return;
        }
        
        leaf.insertAt(index, key, value);
        
        if (leaf.isFull()) {
            splitLeaf(leaf);
        }
    }

    /**
     * Splits a leaf node when it becomes too full.
     */
    private void splitLeaf(BTreeNode<K, V> leaf) {
        BTreeNode<K, V> newLeaf = leaf.split();
        K middleKey = leaf.getMiddleKey();
        V middleValue = leaf.getMiddleValue();
        leaf.removeMiddle();
        
        if (leaf == root) {
            // Create new root
            BTreeNode<K, V> newRoot = new BTreeNode<>(order, false);
            newRoot.getKeys().add(middleKey);
            newRoot.getValues().add(middleValue);
            newRoot.getChildren().add(leaf);
            newRoot.getChildren().add(newLeaf);
            
            leaf.setParent(newRoot);
            newLeaf.setParent(newRoot);
            
            root = newRoot;
        } else {
            // Insert middle key into parent
            insertIntoParent(leaf.getParent(), middleKey, middleValue, newLeaf);
        }
    }

    /**
     * Inserts a key into a parent node (used during splitting).
     */
    private void insertIntoParent(BTreeNode<K, V> parent, K key, V value, BTreeNode<K, V> newChild) {
        int index = parent.findKeyIndex(key);
        parent.insertAt(index, key, value);
        parent.getChildren().add(index + 1, newChild);
        newChild.setParent(parent);
        
        if (parent.isFull()) {
            splitInternal(parent);
        }
    }

    /**
     * Splits an internal (non-leaf) node.
     */
    private void splitInternal(BTreeNode<K, V> node) {
        BTreeNode<K, V> newNode = node.split();
        K middleKey = node.getMiddleKey();
        V middleValue = node.getMiddleValue();
        node.removeMiddle();
        
        if (node == root) {
            // Create new root
            BTreeNode<K, V> newRoot = new BTreeNode<>(order, false);
            newRoot.getKeys().add(middleKey);
            newRoot.getValues().add(middleValue);
            newRoot.getChildren().add(node);
            newRoot.getChildren().add(newNode);
            
            node.setParent(newRoot);
            newNode.setParent(newRoot);
            
            root = newRoot;
        } else {
            // Insert middle key into parent
            insertIntoParent(node.getParent(), middleKey, middleValue, newNode);
        }
    }

    /**
     * Deletes a key from the tree.
     */
    private boolean deleteFromTree(BTreeNode<K, V> node, K key) {
        // For now, implement a simple deletion that doesn't handle underflow
        // Full deletion with merging/borrowing will be implemented later
        int index = node.findKeyIndex(key);
        
        if (node.isLeaf()) {
            if (index < node.getKeys().size() && 
                key.compareTo(node.getKeys().get(index)) == 0) {
                node.removeAt(index);
                return true;
            }
            return false;
        } else {
            if (index < node.getKeys().size() && 
                key.compareTo(node.getKeys().get(index)) == 0) {
                // Key found in internal node - for now, just remove it
                // In a full implementation, we'd replace with predecessor/successor
                node.removeAt(index);
                return true;
            } else {
                return deleteFromTree(node.getChildren().get(index), key);
            }
        }
    }

    /**
     * Helper method for range queries.
     */
    private void rangeQueryHelper(BTreeNode<K, V> node, K start, K end, List<V> results) {
        if (node == null) {
            return;
        }
        
        for (int i = 0; i < node.getKeys().size(); i++) {
            K key = node.getKeys().get(i);
            
            // If we haven't reached the start yet, continue to children
            if (key.compareTo(start) < 0) {
                if (!node.isLeaf()) {
                    rangeQueryHelper(node.getChildren().get(i), start, end, results);
                }
                continue;
            }
            
            // If we've passed the end, stop
            if (key.compareTo(end) > 0) {
                break;
            }
            
            // Key is in range
            results.add(node.getValues().get(i));
            
            // Continue to child between this key and next
            if (!node.isLeaf()) {
                rangeQueryHelper(node.getChildren().get(i), start, end, results);
            }
        }
        
        // Check the last child
        if (!node.isLeaf() && !node.getChildren().isEmpty()) {
            rangeQueryHelper(node.getChildren().get(node.getChildren().size() - 1), start, end, results);
        }
    }

    public int getOrder() {
        return order;
    }

    public BTreeNode<K, V> getRoot() {
        return root;
    }

    @Override
    public String toString() {
        return "BTreeIndex{" +
                "order=" + order +
                ", size=" + size +
                ", root=" + root +
                '}';
    }
}
