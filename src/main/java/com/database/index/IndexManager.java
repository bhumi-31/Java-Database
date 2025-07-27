package com.database.index;

import com.database.types.TypedValue;
import java.util.*;

/**
 * Manages multiple indexes for a table and provides unified index operations.
 */
public class IndexManager {
    private final Map<String, Index<TypedValue, Integer>> indexes;
    private String primaryKeyIndex;
    
    public IndexManager() {
        this.indexes = new HashMap<>();
    }

    /**
     * Creates an index on the specified column.
     */
    public void createIndex(String columnName, IndexType type) {
        if (columnName == null || columnName.trim().isEmpty()) {
            throw new IllegalArgumentException("Column name cannot be null or empty");
        }
        
        Index<TypedValue, Integer> index;
        switch (type) {
            case BTREE:
                index = new BTreeIndex<>();
                break;
            case HASH:
                index = new HashIndex<>();
                break;
            default:
                throw new IllegalArgumentException("Unsupported index type: " + type);
        }
        
        indexes.put(columnName, index);
    }

    /**
     * Creates a primary key index (always B-Tree for range support).
     */
    public void createPrimaryKeyIndex(String columnName) {
        createIndex(columnName, IndexType.BTREE);
        this.primaryKeyIndex = columnName;
    }

    /**
     * Drops an index for the specified column.
     */
    public void dropIndex(String columnName) {
        if (columnName.equals(primaryKeyIndex)) {
            throw new IllegalArgumentException("Cannot drop primary key index");
        }
        indexes.remove(columnName);
    }

    /**
     * Inserts a value into all relevant indexes.
     */
    public void insert(Map<String, TypedValue> columnValues, int rowId) {
        for (Map.Entry<String, Index<TypedValue, Integer>> entry : indexes.entrySet()) {
            String columnName = entry.getKey();
            Index<TypedValue, Integer> index = entry.getValue();
            
            TypedValue value = columnValues.get(columnName);
            if (value != null) {
                index.insert(value, rowId);
            }
        }
    }

    /**
     * Removes a value from all relevant indexes.
     */
    public void delete(Map<String, TypedValue> columnValues, int rowId) {
        for (Map.Entry<String, Index<TypedValue, Integer>> entry : indexes.entrySet()) {
            String columnName = entry.getKey();
            Index<TypedValue, Integer> index = entry.getValue();
            
            TypedValue value = columnValues.get(columnName);
            if (value != null) {
                index.delete(value);
            }
        }
    }

    /**
     * Updates indexes when a row is modified.
     */
    public void update(Map<String, TypedValue> oldValues, Map<String, TypedValue> newValues, int rowId) {
        // Remove old values
        delete(oldValues, rowId);
        // Insert new values
        insert(newValues, rowId);
    }

    /**
     * Searches for a row ID using the specified column and value.
     */
    public Integer search(String columnName, TypedValue value) {
        Index<TypedValue, Integer> index = indexes.get(columnName);
        if (index == null) {
            return null; // No index on this column
        }
        return index.search(value);
    }

    /**
     * Performs a range query on a B-Tree index.
     */
    public List<Integer> rangeQuery(String columnName, TypedValue start, TypedValue end) {
        Index<TypedValue, Integer> index = indexes.get(columnName);
        if (index == null || !(index instanceof BTreeIndex)) {
            return new ArrayList<>(); // No B-Tree index on this column
        }
        
        BTreeIndex<TypedValue, Integer> btreeIndex = (BTreeIndex<TypedValue, Integer>) index;
        return btreeIndex.rangeQuery(start, end);
    }

    /**
     * Gets the primary key index for fast primary key lookups.
     */
    public Index<TypedValue, Integer> getPrimaryKeyIndex() {
        if (primaryKeyIndex == null) {
            return null;
        }
        return indexes.get(primaryKeyIndex);
    }

    /**
     * Checks if an index exists on the specified column.
     */
    public boolean hasIndex(String columnName) {
        return indexes.containsKey(columnName);
    }

    /**
     * Gets the type of index on a column.
     */
    public IndexType getIndexType(String columnName) {
        Index<TypedValue, Integer> index = indexes.get(columnName);
        if (index == null) {
            return null;
        }
        
        if (index instanceof BTreeIndex) {
            return IndexType.BTREE;
        } else if (index instanceof HashIndex) {
            return IndexType.HASH;
        }
        
        return null;
    }

    /**
     * Lists all indexed columns.
     */
    public Set<String> getIndexedColumns() {
        return new HashSet<>(indexes.keySet());
    }

    /**
     * Gets statistics about all indexes.
     */
    public Map<String, IndexStats> getIndexStatistics() {
        Map<String, IndexStats> stats = new HashMap<>();
        
        for (Map.Entry<String, Index<TypedValue, Integer>> entry : indexes.entrySet()) {
            String columnName = entry.getKey();
            Index<TypedValue, Integer> index = entry.getValue();
            
            IndexStats indexStats = new IndexStats(
                columnName,
                getIndexType(columnName),
                index.size(),
                columnName.equals(primaryKeyIndex)
            );
            
            stats.put(columnName, indexStats);
        }
        
        return stats;
    }

    /**
     * Clears all indexes.
     */
    public void clear() {
        for (Index<TypedValue, Integer> index : indexes.values()) {
            index.clear();
        }
    }

    /**
     * Index type enumeration.
     */
    public enum IndexType {
        BTREE,
        HASH
    }

    /**
     * Index statistics container.
     */
    public static class IndexStats {
        private final String columnName;
        private final IndexType type;
        private final int size;
        private final boolean isPrimaryKey;

        public IndexStats(String columnName, IndexType type, int size, boolean isPrimaryKey) {
            this.columnName = columnName;
            this.type = type;
            this.size = size;
            this.isPrimaryKey = isPrimaryKey;
        }

        public String getColumnName() { return columnName; }
        public IndexType getType() { return type; }
        public int getSize() { return size; }
        public boolean isPrimaryKey() { return isPrimaryKey; }

        @Override
        public String toString() {
            return "IndexStats{" +
                    "column='" + columnName + '\'' +
                    ", type=" + type +
                    ", size=" + size +
                    ", isPrimaryKey=" + isPrimaryKey +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "IndexManager{" +
                "indexes=" + indexes.size() +
                ", primaryKeyIndex='" + primaryKeyIndex + '\'' +
                '}';
    }
}
