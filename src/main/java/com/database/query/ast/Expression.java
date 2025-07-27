package com.database.query.ast;

/**
 * Base class for expressions in SQL.
 */
public abstract class Expression {
    
    /**
     * Evaluates this expression and returns the result.
     */
    public abstract Object evaluate();
    
    /**
     * Returns the SQL representation of this expression.
     */
    public abstract String toSQL();
}
