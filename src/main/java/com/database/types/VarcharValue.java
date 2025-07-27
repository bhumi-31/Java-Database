package com.database.types;

/**
 * Variable character string type implementation for the database engine.
 */
public class VarcharValue extends TypedValue {
    private final int maxLength;

    public VarcharValue(String value, int maxLength) {
        super(DataType.VARCHAR, value);
        this.maxLength = maxLength;
        if (!isValid()) {
            throw new IllegalArgumentException("String length " + 
                (value != null ? value.length() : 0) + " exceeds maximum length " + maxLength);
        }
    }

    public VarcharValue(String value) {
        this(value, 255); // Default max length
    }

    @Override
    public boolean isValid() {
        if (!(value instanceof String)) {
            return false;
        }
        String strValue = (String) value;
        return strValue == null || strValue.length() <= maxLength;
    }

    @Override
    protected int compareValues(TypedValue other) {
        String thisValue = (String) this.value;
        String otherValue = (String) other.value;
        
        if (thisValue == null && otherValue == null) return 0;
        if (thisValue == null) return -1;
        if (otherValue == null) return 1;
        
        return thisValue.compareTo(otherValue);
    }

    public String stringValue() {
        return (String) value;
    }

    public int getMaxLength() {
        return maxLength;
    }
}
