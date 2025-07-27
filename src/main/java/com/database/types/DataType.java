package com.database.types;

/**
 * Enumeration of supported data types in the database engine.
 */
public enum DataType {
    INTEGER("INTEGER", Integer.class),
    VARCHAR("VARCHAR", String.class),
    BOOLEAN("BOOLEAN", Boolean.class),
    DOUBLE("DOUBLE", Double.class);

    private final String name;
    private final Class<?> javaType;

    DataType(String name, Class<?> javaType) {
        this.name = name;
        this.javaType = javaType;
    }

    public String getName() {
        return name;
    }

    public Class<?> getJavaType() {
        return javaType;
    }

    public static DataType fromString(String typeName) {
        for (DataType type : values()) {
            if (type.name.equalsIgnoreCase(typeName)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown data type: " + typeName);
    }

    @Override
    public String toString() {
        return name;
    }
}
