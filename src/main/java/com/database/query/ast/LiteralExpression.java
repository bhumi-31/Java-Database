package com.database.query.ast;

/**
 * Represents a literal value in an expression.
 */
public class LiteralExpression extends Expression {
    private final Object value;
    
    public LiteralExpression(Object value) {
        this.value = value;
    }
    
    public Object getValue() {
        return value;
    }
    
    @Override
    public Object evaluate() {
        return value;
    }
    
    @Override
    public String toSQL() {
        if (value == null) {
            return "NULL";
        } else if (value instanceof String) {
            return "'" + value.toString().replace("'", "''") + "'";
        } else {
            return value.toString();
        }
    }
}
