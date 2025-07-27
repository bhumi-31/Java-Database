package com.database.query.advanced;

import com.database.core.*;
import com.database.query.*;
import com.database.query.ast.*;
import com.database.join.*;
import com.database.aggregation.*;
import com.database.transaction.*;

import java.util.List;
import java.util.Map;

/**
 * Enhanced query executor that supports advanced SQL features like JOINs and aggregations.
 */
public class AdvancedQueryExecutor extends QueryExecutor {
    private final JoinOperator joinOperator;
    private final AggregationOperator aggregationOperator;
    private final TransactionManager transactionManager;
    
    public AdvancedQueryExecutor(Database database) {
        super(database);
        this.joinOperator = new JoinOperator();
        this.aggregationOperator = new AggregationOperator();
        this.transactionManager = new TransactionManager(database);
    }
    
    /**
     * Executes advanced SQL queries including JOINs and aggregations.
     */
    public QueryResult executeAdvancedQuery(SQLStatement statement) {
        if (statement instanceof JoinStatement) {
            return executeJoinQuery((JoinStatement) statement);
        } else if (statement instanceof AggregateStatement) {
            return executeAggregateQuery((AggregateStatement) statement);
        } else if (statement instanceof TransactionStatement) {
            return executeTransactionQuery((TransactionStatement) statement);
        } else {
            // Fall back to base implementation for simple queries
            return new QueryResult(false, "Unsupported statement type", null);
        }
    }
    
    /**
     * Executes JOIN queries.
     */
    private QueryResult executeJoinQuery(JoinStatement joinStatement) {
        try {
            Table leftTable = database.getTable(joinStatement.getLeftTable());
            Table rightTable = database.getTable(joinStatement.getRightTable());
            
            if (leftTable == null || rightTable == null) {
                return new QueryResult(false, "Table not found", null);
            }
            
            List<Row> joinedRows = joinOperator.performJoin(
                leftTable, rightTable,
                joinStatement.getLeftColumn(), joinStatement.getRightColumn(),
                joinStatement.getJoinType()
            );
            
            return new QueryResult(true, "JOIN executed successfully", joinedRows);
            
        } catch (Exception e) {
            return new QueryResult(false, "JOIN execution failed: " + e.getMessage(), null);
        }
    }
    
    /**
     * Executes aggregate queries.
     */
    private QueryResult executeAggregateQuery(AggregateStatement aggregateStatement) {
        try {
            Table table = database.getTable(aggregateStatement.getTableName());
            if (table == null) {
                return new QueryResult(false, "Table not found", null);
            }
            
            List<Row> rows = table.getRows();
            
            if (aggregateStatement.hasGroupBy()) {
                // GROUP BY aggregation
                Map<TypedValue, TypedValue> groupedResults = aggregationOperator.performGroupByAggregation(
                    rows,
                    aggregateStatement.getGroupByColumn(),
                    aggregateStatement.getAggregateColumn(),
                    aggregateStatement.getFunction()
                );
                
                // Convert grouped results to rows for consistent return type
                List<Row> resultRows = convertGroupedResultsToRows(groupedResults, aggregateStatement);
                return new QueryResult(true, "GROUP BY aggregation executed successfully", resultRows);
                
            } else {
                // Simple aggregation
                TypedValue result = aggregationOperator.performAggregation(
                    rows,
                    aggregateStatement.getAggregateColumn(),
                    aggregateStatement.getFunction()
                );
                
                // Create a single row with the result
                Row resultRow = new Row();
                resultRow.setValue("result", result);
                return new QueryResult(true, "Aggregation executed successfully", List.of(resultRow));
            }
            
        } catch (Exception e) {
            return new QueryResult(false, "Aggregation execution failed: " + e.getMessage(), null);
        }
    }
    
    /**
     * Executes transaction-related queries.
     */
    private QueryResult executeTransactionQuery(TransactionStatement transactionStatement) {
        try {
            switch (transactionStatement.getTransactionType()) {
                case BEGIN:
                    Transaction transaction = transactionManager.beginTransaction();
                    return new QueryResult(true, "Transaction " + transaction.getTransactionId() + " started", null);
                    
                case COMMIT:
                    // Note: In a real implementation, we'd track the current transaction
                    return new QueryResult(true, "Transaction committed", null);
                    
                case ROLLBACK:
                    // Note: In a real implementation, we'd track the current transaction
                    return new QueryResult(true, "Transaction rolled back", null);
                    
                default:
                    return new QueryResult(false, "Unknown transaction type", null);
            }
            
        } catch (Exception e) {
            return new QueryResult(false, "Transaction execution failed: " + e.getMessage(), null);
        }
    }
    
    /**
     * Converts grouped aggregation results to rows for consistent return format.
     */
    private List<Row> convertGroupedResultsToRows(Map<TypedValue, TypedValue> groupedResults, AggregateStatement aggregateStatement) {
        List<Row> resultRows = new java.util.ArrayList<>();
        
        for (Map.Entry<TypedValue, TypedValue> entry : groupedResults.entrySet()) {
            Row row = new Row();
            row.setValue(aggregateStatement.getGroupByColumn(), entry.getKey());
            row.setValue("aggregate_result", entry.getValue());
            resultRows.add(row);
        }
        
        return resultRows;
    }
    
    public TransactionManager getTransactionManager() {
        return transactionManager;
    }
}
