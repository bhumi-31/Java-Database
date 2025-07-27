package com.database.query.ast;

import com.database.aggregation.AggregationOperator;

/**
 * AST node for aggregate operations.
 */
public class AggregateStatement extends SQLStatement {
    private final String tableName;
    private final String aggregateColumn;
    private final AggregationOperator.AggregateFunction function;
    private final String groupByColumn;
    
    public AggregateStatement(String tableName, String aggregateColumn, 
                             AggregationOperator.AggregateFunction function) {
        this(tableName, aggregateColumn, function, null);
    }
    
    public AggregateStatement(String tableName, String aggregateColumn, 
                             AggregationOperator.AggregateFunction function, 
                             String groupByColumn) {
        this.tableName = tableName;
        this.aggregateColumn = aggregateColumn;
        this.function = function;
        this.groupByColumn = groupByColumn;
    }
    
    public String getTableName() { return tableName; }
    public String getAggregateColumn() { return aggregateColumn; }
    public AggregationOperator.AggregateFunction getFunction() { return function; }
    public String getGroupByColumn() { return groupByColumn; }
    public boolean hasGroupBy() { return groupByColumn != null; }
    
    @Override
    public void execute() {
        // Implementation would be handled by query executor
    }
    
    @Override
    public String toSQL() {
        String sql = String.format("SELECT %s(%s) FROM %s", 
                function.toString(), aggregateColumn != null ? aggregateColumn : "*", tableName);
        if (hasGroupBy()) {
            sql += " GROUP BY " + groupByColumn;
        }
        return sql;
    }
    
    @Override
    public String toString() {
        return "AggregateStatement{" +
                "tableName='" + tableName + '\'' +
                ", aggregateColumn='" + aggregateColumn + '\'' +
                ", function=" + function +
                ", groupByColumn='" + groupByColumn + '\'' +
                '}';
    }
}
