package com.database.core;

import com.database.types.TypedValue;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Represents a single row of data in a database table.
 */
public class Row implements Serializable {
    private static final AtomicInteger ID_GENERATOR = new AtomicInteger(1);
    
    private final int rowId;
    private final Map<String, TypedValue> values;

    public Row() {
        this.rowId = ID_GENERATOR.getAndIncrement();
        this.values = new HashMap<>();
    }

    public Row(int rowId) {
        this.rowId = rowId;
        this.values = new HashMap<>();
    }

    /**
     * Gets the unique row identifier.
     */
    public int getRowId() {
        return rowId;
    }

    /**
     * Gets the value for the specified column (case-insensitive).
     */
    public TypedValue getValue(String columnName) {
        if (columnName == null) {
            return null;
        }
        return values.get(columnName.toLowerCase());
    }

    /**
     * Sets the value for the specified column (case-insensitive).
     */
    public void setValue(String columnName, TypedValue value) {
        if (columnName == null) {
            throw new IllegalArgumentException("Column name cannot be null");
        }
        values.put(columnName.toLowerCase(), value);
    }

    /**
     * Removes the value for the specified column.
     */
    public TypedValue removeValue(String columnName) {
        if (columnName == null) {
            return null;
        }
        return values.remove(columnName.toLowerCase());
    }

    /**
     * Checks if this row has a value for the specified column.
     */
    public boolean hasValue(String columnName) {
        if (columnName == null) {
            return false;
        }
        return values.containsKey(columnName.toLowerCase());
    }

    /**
     * Returns all column names that have values in this row.
     */
    public java.util.Set<String> getColumnNames() {
        return new java.util.HashSet<>(values.keySet());
    }

    /**
     * Returns the number of columns with values in this row.
     */
    public int getValueCount() {
        return values.size();
    }

    /**
     * Creates a copy of this row with the same values but a new row ID.
     */
    public Row copy() {
        Row newRow = new Row();
        newRow.values.putAll(this.values);
        return newRow;
    }

    /**
     * Creates a copy of this row with the same row ID and values.
     */
    public Row deepCopy() {
        Row newRow = new Row(this.rowId);
        newRow.values.putAll(this.values);
        return newRow;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        Row row = (Row) obj;
        return rowId == row.rowId && values.equals(row.values);
    }

    @Override
    public int hashCode() {
        int result = rowId;
        result = 31 * result + values.hashCode();
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Row{id=").append(rowId).append(", values={");
        boolean first = true;
        for (Map.Entry<String, TypedValue> entry : values.entrySet()) {
            if (!first) sb.append(", ");
            sb.append(entry.getKey()).append("=").append(entry.getValue());
            first = false;
        }
        sb.append("}}");
        return sb.toString();
    }
}
