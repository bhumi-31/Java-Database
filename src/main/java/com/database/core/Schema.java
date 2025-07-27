package com.database.core;

import com.database.types.DataType;
import java.io.Serializable;
import java.util.*;

/**
 * Represents the schema (structure) of a database table.
 * Contains column definitions and validation logic.
 */
public class Schema implements Serializable {
    private final List<Column> columns;
    private final String primaryKeyColumn;
    private final Map<String, Column> columnMap;

    public Schema(List<Column> columns) {
        if (columns == null || columns.isEmpty()) {
            throw new IllegalArgumentException("Schema must have at least one column");
        }

        this.columns = new ArrayList<>(columns);
        this.columnMap = new HashMap<>();
        
        String pkColumn = null;
        Set<String> columnNames = new HashSet<>();
        
        for (Column column : columns) {
            String name = column.getName().toLowerCase();
            
            // Check for duplicate column names
            if (columnNames.contains(name)) {
                throw new IllegalArgumentException("Duplicate column name: " + column.getName());
            }
            columnNames.add(name);
            columnMap.put(name, column);
            
            // Track primary key column
            if (column.isPrimaryKey()) {
                if (pkColumn != null) {
                    throw new IllegalArgumentException("Multiple primary key columns not supported");
                }
                pkColumn = column.getName();
            }
        }
        
        this.primaryKeyColumn = pkColumn;
    }

    /**
     * Validates that a row conforms to this schema.
     */
    public boolean validateRow(Row row) {
        if (row == null) {
            return false;
        }

        // Check that all non-nullable columns have values
        for (Column column : columns) {
            String columnName = column.getName();
            if (!column.isNullable() && row.getValue(columnName) == null) {
                return false;
            }
            
            // Validate data types
            if (row.getValue(columnName) != null) {
                if (row.getValue(columnName).getType() != column.getType()) {
                    return false;
                }
            }
        }
        
        return true;
    }

    /**
     * Gets a column by name (case-insensitive).
     */
    public Column getColumn(String name) {
        if (name == null) {
            return null;
        }
        return columnMap.get(name.toLowerCase());
    }

    /**
     * Checks if a column exists in this schema.
     */
    public boolean hasColumn(String name) {
        return getColumn(name) != null;
    }

    /**
     * Returns all columns in this schema.
     */
    public List<Column> getColumns() {
        return new ArrayList<>(columns);
    }

    /**
     * Returns the primary key column name, or null if no primary key.
     */
    public String getPrimaryKeyColumn() {
        return primaryKeyColumn;
    }

    /**
     * Returns the number of columns in this schema.
     */
    public int getColumnCount() {
        return columns.size();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        Schema schema = (Schema) obj;
        return columns.equals(schema.columns);
    }

    @Override
    public int hashCode() {
        return columns.hashCode();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Schema{columns=[");
        for (int i = 0; i < columns.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(columns.get(i).toString());
        }
        sb.append("]}");
        return sb.toString();
    }
}
