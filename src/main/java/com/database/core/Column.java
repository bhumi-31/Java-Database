package com.database.core;

import com.database.types.DataType;
import java.io.Serializable;

/**
 * Represents a column definition in a database table.
 */
public class Column implements Serializable {
    private final String name;
    private final DataType type;
    private final boolean nullable;
    private final boolean primaryKey;
    private final int maxLength; // for VARCHAR types

    public Column(String name, DataType type, boolean nullable, boolean primaryKey, int maxLength) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Column name cannot be null or empty");
        }
        if (type == null) {
            throw new IllegalArgumentException("Column type cannot be null");
        }
        
        this.name = name.trim();
        this.type = type;
        this.nullable = nullable;
        this.primaryKey = primaryKey;
        this.maxLength = maxLength;
    }

    public Column(String name, DataType type, boolean nullable, boolean primaryKey) {
        this(name, type, nullable, primaryKey, type == DataType.VARCHAR ? 255 : 0);
    }

    public Column(String name, DataType type) {
        this(name, type, true, false);
    }

    public String getName() {
        return name;
    }

    public DataType getType() {
        return type;
    }

    public boolean isNullable() {
        return nullable;
    }

    public boolean isPrimaryKey() {
        return primaryKey;
    }

    public int getMaxLength() {
        return maxLength;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        Column column = (Column) obj;
        return nullable == column.nullable &&
               primaryKey == column.primaryKey &&
               maxLength == column.maxLength &&
               name.equals(column.name) &&
               type == column.type;
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + type.hashCode();
        result = 31 * result + (nullable ? 1 : 0);
        result = 31 * result + (primaryKey ? 1 : 0);
        result = 31 * result + maxLength;
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(name).append(" ").append(type);
        if (type == DataType.VARCHAR && maxLength > 0) {
            sb.append("(").append(maxLength).append(")");
        }
        if (primaryKey) {
            sb.append(" PRIMARY KEY");
        }
        if (!nullable) {
            sb.append(" NOT NULL");
        }
        return sb.toString();
    }
}
