package com.database.query.ast;

import java.util.List;

/**
 * Represents a SELECT statement in the AST.
 */
public class SelectStatement extends SQLStatement {
    private final List<String> columns;
    private final String tableName;
    private final WhereClause whereClause;
    private final OrderByClause orderByClause;
    private final boolean distinct;
    
    public SelectStatement(List<String> columns, String tableName, WhereClause whereClause, 
                          OrderByClause orderByClause, boolean distinct) {
        this.columns = columns;
        this.tableName = tableName;
        this.whereClause = whereClause;
        this.orderByClause = orderByClause;
        this.distinct = distinct;
    }
    
    public List<String> getColumns() {
        return columns;
    }
    
    public String getTableName() {
        return tableName;
    }
    
    public WhereClause getWhereClause() {
        return whereClause;
    }
    
    public OrderByClause getOrderByClause() {
        return orderByClause;
    }
    
    public boolean isDistinct() {
        return distinct;
    }
    
    @Override
    public void execute() {
        // Implementation will be added when we create the QueryExecutor
        throw new UnsupportedOperationException("Execute method not yet implemented");
    }
    
    @Override
    public String toSQL() {
        StringBuilder sql = new StringBuilder();
        
        sql.append("SELECT ");
        if (distinct) {
            sql.append("DISTINCT ");
        }
        
        if (columns.isEmpty() || columns.contains("*")) {
            sql.append("*");
        } else {
            sql.append(String.join(", ", columns));
        }
        
        sql.append(" FROM ").append(tableName);
        
        if (whereClause != null) {
            sql.append(" WHERE ").append(whereClause.toSQL());
        }
        
        if (orderByClause != null) {
            sql.append(" ORDER BY ").append(orderByClause.toSQL());
        }
        
        return sql.toString();
    }
    
    @Override
    public String toString() {
        return toSQL();
    }
}
