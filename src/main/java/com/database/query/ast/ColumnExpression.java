package com.database.query.ast;

/**
 * Represents a column reference in an expression.
 */
public class ColumnExpression extends Expression {
    private final String columnName;
    
    public ColumnExpression(String columnName) {
        this.columnName = columnName;
    }
    
    public String getColumnName() {
        return columnName;
    }
    
    @Override
    public Object evaluate() {
        // This will be implemented when we have a row context
        throw new UnsupportedOperationException("Column evaluation requires row context");
    }
    
    @Override
    public String toSQL() {
        return columnName;
    }
}
