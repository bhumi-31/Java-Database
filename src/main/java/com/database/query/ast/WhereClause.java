package com.database.query.ast;

/**
 * Represents a WHERE clause with conditional expressions.
 */
public class WhereClause {
    private final Expression condition;
    
    public WhereClause(Expression condition) {
        this.condition = condition;
    }
    
    public Expression getCondition() {
        return condition;
    }
    
    public String toSQL() {
        return condition != null ? condition.toSQL() : "";
    }
    
    @Override
    public String toString() {
        return toSQL();
    }
}
