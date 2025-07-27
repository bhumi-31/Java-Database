package com.database.types;

/**
 * Double precision floating point type implementation for the database engine.
 */
public class DoubleValue extends TypedValue {
    
    public DoubleValue(Double value) {
        super(DataType.DOUBLE, value);
        if (!isValid()) {
            throw new IllegalArgumentException("Invalid double value: " + value);
        }
    }

    public DoubleValue(double value) {
        super(DataType.DOUBLE, value);
        if (!isValid()) {
            throw new IllegalArgumentException("Invalid double value: " + value);
        }
    }

    @Override
    public boolean isValid() {
        return value instanceof Double && !((Double) value).isNaN();
    }

    @Override
    protected int compareValues(TypedValue other) {
        Double thisValue = (Double) this.value;
        Double otherValue = (Double) other.value;
        return thisValue.compareTo(otherValue);
    }

    public double doubleValue() {
        return (Double) value;
    }
}
