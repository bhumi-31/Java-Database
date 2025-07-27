package com.database.query.ast;

/**
 * Base class for all SQL statement AST nodes.
 */
public abstract class SQLStatement {
    
    /**
     * Executes this SQL statement.
     */
    public abstract void execute();
    
    /**
     * Returns a string representation of this statement.
     */
    public abstract String toSQL();
}
