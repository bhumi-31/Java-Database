package com.database.types;

/**
 * Boolean type implementation for the database engine.
 */
public class BooleanValue extends TypedValue {
    
    public BooleanValue(Boolean value) {
        super(DataType.BOOLEAN, value);
        if (!isValid()) {
            throw new IllegalArgumentException("Invalid boolean value: " + value);
        }
    }

    public BooleanValue(boolean value) {
        super(DataType.BOOLEAN, value);
        if (!isValid()) {
            throw new IllegalArgumentException("Invalid boolean value: " + value);
        }
    }

    @Override
    public boolean isValid() {
        return value instanceof Boolean;
    }

    @Override
    protected int compareValues(TypedValue other) {
        Boolean thisValue = (Boolean) this.value;
        Boolean otherValue = (Boolean) other.value;
        return thisValue.compareTo(otherValue);
    }

    public boolean booleanValue() {
        return (Boolean) value;
    }
}
