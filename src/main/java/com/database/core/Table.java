package com.database.core;

import com.database.types.TypedValue;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Represents a database table with schema and data storage.
 */
public class Table implements Serializable {
    private final String name;
    private final Schema schema;
    private final List<Row> rows;
    private final AtomicInteger nextRowId;
    private final Map<Object, Integer> primaryKeyIndex; // Maps PK values to row indices

    public Table(String name, Schema schema) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Table name cannot be null or empty");
        }
        if (schema == null) {
            throw new IllegalArgumentException("Schema cannot be null");
        }

        this.name = name.trim();
        this.schema = schema;
        this.rows = new ArrayList<>();
        this.nextRowId = new AtomicInteger(1);
        this.primaryKeyIndex = new HashMap<>();
    }

    /**
     * Inserts a new row into the table.
     */
    public void insert(Row row) {
        if (row == null) {
            throw new IllegalArgumentException("Row cannot be null");
        }

        // Validate row against schema
        if (!schema.validateRow(row)) {
            throw new IllegalArgumentException("Row does not match table schema");
        }

        // Check primary key constraints
        if (schema.getPrimaryKeyColumn() != null) {
            TypedValue pkValue = row.getValue(schema.getPrimaryKeyColumn());
            if (pkValue == null) {
                throw new IllegalArgumentException("Primary key cannot be null");
            }
            if (primaryKeyIndex.containsKey(pkValue.getValue())) {
                throw new IllegalArgumentException("Primary key value already exists: " + pkValue);
            }
            primaryKeyIndex.put(pkValue.getValue(), rows.size());
        }

        synchronized (rows) {
            rows.add(row);
        }
    }

    /**
     * Selects all rows from the table.
     */
    public List<Row> selectAll() {
        synchronized (rows) {
            return new ArrayList<>(rows);
        }
    }

    /**
     * Finds a row by its row ID.
     */
    public Row findRowById(int rowId) {
        synchronized (rows) {
            for (Row row : rows) {
                if (row.getRowId() == rowId) {
                    return row;
                }
            }
        }
        return null;
    }

    /**
     * Finds a row by primary key value.
     */
    public Row findRowByPrimaryKey(Object pkValue) {
        if (schema.getPrimaryKeyColumn() == null || pkValue == null) {
            return null;
        }
        
        Integer index = primaryKeyIndex.get(pkValue);
        if (index != null && index < rows.size()) {
            return rows.get(index);
        }
        return null;
    }

    /**
     * Updates a row identified by row ID.
     */
    public boolean update(int rowId, Map<String, TypedValue> updates) {
        if (updates == null || updates.isEmpty()) {
            return false;
        }

        synchronized (rows) {
            for (int i = 0; i < rows.size(); i++) {
                Row row = rows.get(i);
                if (row.getRowId() == rowId) {
                    // Create updated row
                    Row updatedRow = row.deepCopy();
                    
                    // Apply updates
                    for (Map.Entry<String, TypedValue> update : updates.entrySet()) {
                        String columnName = update.getKey();
                        TypedValue newValue = update.getValue();
                        
                        // Validate column exists
                        if (!schema.hasColumn(columnName)) {
                            throw new IllegalArgumentException("Column does not exist: " + columnName);
                        }
                        
                        // Don't allow primary key updates for now
                        if (columnName.equalsIgnoreCase(schema.getPrimaryKeyColumn())) {
                            throw new IllegalArgumentException("Cannot update primary key column");
                        }
                        
                        updatedRow.setValue(columnName, newValue);
                    }
                    
                    // Validate updated row
                    if (!schema.validateRow(updatedRow)) {
                        throw new IllegalArgumentException("Updated row does not match schema");
                    }
                    
                    // Replace the row
                    rows.set(i, updatedRow);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Deletes a row identified by row ID.
     */
    public boolean delete(int rowId) {
        synchronized (rows) {
            for (int i = 0; i < rows.size(); i++) {
                Row row = rows.get(i);
                if (row.getRowId() == rowId) {
                    // Remove from primary key index if applicable
                    if (schema.getPrimaryKeyColumn() != null) {
                        TypedValue pkValue = row.getValue(schema.getPrimaryKeyColumn());
                        if (pkValue != null) {
                            primaryKeyIndex.remove(pkValue.getValue());
                        }
                    }
                    
                    rows.remove(i);
                    
                    // Update primary key index for remaining rows
                    updatePrimaryKeyIndex();
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Rebuilds the primary key index after row deletions.
     */
    private void updatePrimaryKeyIndex() {
        if (schema.getPrimaryKeyColumn() == null) {
            return;
        }
        
        primaryKeyIndex.clear();
        for (int i = 0; i < rows.size(); i++) {
            Row row = rows.get(i);
            TypedValue pkValue = row.getValue(schema.getPrimaryKeyColumn());
            if (pkValue != null) {
                primaryKeyIndex.put(pkValue.getValue(), i);
            }
        }
    }

    /**
     * Returns the table name.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the table schema.
     */
    public Schema getSchema() {
        return schema;
    }

    /**
     * Returns the number of rows in the table.
     */
    public int getRowCount() {
        synchronized (rows) {
            return rows.size();
        }
    }

    /**
     * Checks if the table is empty.
     */
    public boolean isEmpty() {
        return getRowCount() == 0;
    }

    @Override
    public String toString() {
        return "Table{" +
                "name='" + name + '\'' +
                ", schema=" + schema +
                ", rowCount=" + getRowCount() +
                '}';
    }
}
