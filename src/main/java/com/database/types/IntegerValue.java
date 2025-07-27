package com.database.types;

/**
 * Integer type implementation for the database engine.
 */
public class IntegerValue extends TypedValue {
    
    public IntegerValue(Integer value) {
        super(DataType.INTEGER, value);
        if (!isValid()) {
            throw new IllegalArgumentException("Invalid integer value: " + value);
        }
    }

    public IntegerValue(int value) {
        super(DataType.INTEGER, value);
        if (!isValid()) {
            throw new IllegalArgumentException("Invalid integer value: " + value);
        }
    }

    @Override
    public boolean isValid() {
        return value instanceof Integer;
    }

    @Override
    protected int compareValues(TypedValue other) {
        Integer thisValue = (Integer) this.value;
        Integer otherValue = (Integer) other.value;
        return thisValue.compareTo(otherValue);
    }

    public int intValue() {
        return (Integer) value;
    }
}
