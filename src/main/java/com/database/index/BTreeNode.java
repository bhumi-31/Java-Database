package com.database.index;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * B-Tree node implementation for the database index.
 */
public class BTreeNode<K extends Comparable<K>, V> implements Serializable {
    private final int order;
    private final List<K> keys;
    private final List<V> values;
    private final List<BTreeNode<K, V>> children;
    private boolean isLeaf;
    private BTreeNode<K, V> parent;

    public BTreeNode(int order, boolean isLeaf) {
        this.order = order;
        this.keys = new ArrayList<>();
        this.values = new ArrayList<>();
        this.children = new ArrayList<>();
        this.isLeaf = isLeaf;
        this.parent = null;
    }

    /**
     * Checks if the node is full (has maximum number of keys).
     */
    public boolean isFull() {
        return keys.size() >= order - 1;
    }

    /**
     * Checks if the node is at minimum capacity.
     */
    public boolean isMinimal() {
        return keys.size() <= (order - 1) / 2;
    }

    /**
     * Finds the index where a key should be inserted or where it exists.
     */
    public int findKeyIndex(K key) {
        int left = 0, right = keys.size() - 1;
        
        while (left <= right) {
            int mid = (left + right) / 2;
            int cmp = key.compareTo(keys.get(mid));
            
            if (cmp == 0) {
                return mid; // Exact match
            } else if (cmp < 0) {
                right = mid - 1;
            } else {
                left = mid + 1;
            }
        }
        
        return left; // Insert position
    }

    /**
     * Inserts a key-value pair at the specified index.
     */
    public void insertAt(int index, K key, V value) {
        keys.add(index, key);
        values.add(index, value);
    }

    /**
     * Removes a key-value pair at the specified index.
     */
    public void removeAt(int index) {
        keys.remove(index);
        values.remove(index);
    }

    /**
     * Splits this node when it becomes too full.
     * Returns the new right node created from the split.
     */
    public BTreeNode<K, V> split() {
        int midIndex = keys.size() / 2;
        
        // Create new right node
        BTreeNode<K, V> rightNode = new BTreeNode<>(order, isLeaf);
        rightNode.parent = this.parent;
        
        // Move half the keys and values to the new node
        for (int i = midIndex + 1; i < keys.size(); ) {
            rightNode.keys.add(keys.remove(i));
            rightNode.values.add(values.remove(i));
        }
        
        // If not leaf, move children too
        if (!isLeaf) {
            for (int i = midIndex + 1; i < children.size(); ) {
                BTreeNode<K, V> child = children.remove(i);
                rightNode.children.add(child);
                child.parent = rightNode;
            }
        }
        
        return rightNode;
    }

    /**
     * Gets the middle key for splitting (will be promoted to parent).
     */
    public K getMiddleKey() {
        return keys.get(keys.size() / 2);
    }

    /**
     * Gets the middle value for splitting.
     */
    public V getMiddleValue() {
        return values.get(values.size() / 2);
    }

    /**
     * Removes the middle key-value pair (used during splitting).
     */
    public void removeMiddle() {
        int midIndex = keys.size() / 2;
        keys.remove(midIndex);
        values.remove(midIndex);
    }

    // Getters and setters
    public List<K> getKeys() { return keys; }
    public List<V> getValues() { return values; }
    public List<BTreeNode<K, V>> getChildren() { return children; }
    public boolean isLeaf() { return isLeaf; }
    public void setLeaf(boolean leaf) { isLeaf = leaf; }
    public BTreeNode<K, V> getParent() { return parent; }
    public void setParent(BTreeNode<K, V> parent) { this.parent = parent; }
    public int getOrder() { return order; }

    @Override
    public String toString() {
        return "BTreeNode{" +
                "keys=" + keys +
                ", isLeaf=" + isLeaf +
                ", children=" + children.size() +
                '}';
    }
}
