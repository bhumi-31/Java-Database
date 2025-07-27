package com.database.types;

import java.io.Serializable;

/**
 * Abstract base class for all typed values in the database.
 * Provides type safety and validation for stored data.
 */
public abstract class TypedValue implements Comparable<TypedValue>, Serializable {
    protected final DataType type;
    protected final Object value;

    protected TypedValue(DataType type, Object value) {
        this.type = type;
        this.value = value;
        // Note: isValid() is not called here to allow subclasses to initialize their fields first
        // Subclasses should validate in their constructors after initialization
    }

    /**
     * Validates that the value is appropriate for the data type.
     */
    public abstract boolean isValid();

    /**
     * Returns the data type of this value.
     */
    public DataType getType() {
        return type;
    }

    /**
     * Returns the raw value object.
     */
    public Object getValue() {
        return value;
    }

    /**
     * Compares this TypedValue with another for ordering.
     * Only values of the same type can be compared.
     */
    @Override
    public int compareTo(TypedValue other) {
        if (other == null) {
            return 1;
        }
        if (this.type != other.type) {
            throw new IllegalArgumentException("Cannot compare values of different types: " + 
                this.type + " and " + other.type);
        }
        return compareValues(other);
    }

    /**
     * Compare values of the same type. Subclasses must implement this.
     */
    protected abstract int compareValues(TypedValue other);

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        TypedValue that = (TypedValue) obj;
        return type == that.type && 
               (value != null ? value.equals(that.value) : that.value == null);
    }

    @Override
    public int hashCode() {
        int result = type.hashCode();
        result = 31 * result + (value != null ? value.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return value != null ? value.toString() : "NULL";
    }
}
