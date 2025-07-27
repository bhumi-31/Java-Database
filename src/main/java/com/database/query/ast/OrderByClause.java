package com.database.query.ast;

import java.util.List;

/**
 * Represents an ORDER BY clause.
 */
public class OrderByClause {
    private final List<String> columns;
    private final boolean ascending;
    
    public OrderByClause(List<String> columns, boolean ascending) {
        this.columns = columns;
        this.ascending = ascending;
    }
    
    public List<String> getColumns() {
        return columns;
    }
    
    public boolean isAscending() {
        return ascending;
    }
    
    public String toSQL() {
        StringBuilder sql = new StringBuilder();
        sql.append(String.join(", ", columns));
        if (!ascending) {
            sql.append(" DESC");
        }
        return sql.toString();
    }
    
    @Override
    public String toString() {
        return toSQL();
    }
}
