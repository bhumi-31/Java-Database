package com.database.query;

import com.database.core.*;
import com.database.storage.StorageEngine;
import com.database.types.*;
import com.database.index.IndexManager;
import com.database.query.ast.*;

import java.util.*;

/**
 * Executes parsed SQL queries against the database engine.
 */
public class QueryExecutor {
    private final StorageEngine storageEngine;
    private final Map<String, IndexManager> tableIndexes;
    
    public QueryExecutor(StorageEngine storageEngine) {
        this.storageEngine = storageEngine;
        this.tableIndexes = new HashMap<>();
    }
    
    /**
     * Registers an index manager for a table.
     */
    public void registerIndexManager(String tableName, IndexManager indexManager) {
        tableIndexes.put(tableName.toLowerCase(), indexManager);
    }
    
    /**
     * Executes a SQL statement and returns the result.
     */
    public QueryResult execute(SQLStatement statement) {
        if (statement instanceof SelectStatement) {
            return executeSelect((SelectStatement) statement);
        } else {
            throw new UnsupportedOperationException("Statement type not yet supported: " + statement.getClass());
        }
    }
    
    /**
     * Executes a SQL string and returns the result.
     */
    public QueryResult execute(String sql) {
        SQLParser parser = new SQLParser();
        SQLStatement statement = parser.parse(sql);
        return execute(statement);
    }
    
    private QueryResult executeSelect(SelectStatement statement) {
        String tableName = statement.getTableName();
        Table table = storageEngine.getTable(tableName);
        
        if (table == null) {
            throw new RuntimeException("Table not found: " + tableName);
        }
        
        // Get all rows from the table
        List<Row> allRows = table.selectAll();
        List<Row> filteredRows = allRows;
        
        // Apply WHERE clause filtering
        if (statement.getWhereClause() != null) {
            filteredRows = applyWhereClause(allRows, statement.getWhereClause(), tableName);
        }
        
        // Apply column projection
        List<Row> projectedRows = applyProjection(filteredRows, statement.getColumns(), table.getSchema());
        
        // Apply DISTINCT if needed
        if (statement.isDistinct()) {
            projectedRows = applyDistinct(projectedRows);
        }
        
        // Apply ORDER BY if specified
        if (statement.getOrderByClause() != null) {
            projectedRows = applyOrderBy(projectedRows, statement.getOrderByClause());
        }
        
        return new QueryResult(projectedRows, getProjectedSchema(statement.getColumns(), table.getSchema()));
    }
    
    private List<Row> applyWhereClause(List<Row> rows, WhereClause whereClause, String tableName) {
        List<Row> filtered = new ArrayList<>();
        Expression condition = whereClause.getCondition();
        
        // Try to use indexes for optimization
        IndexManager indexManager = tableIndexes.get(tableName.toLowerCase());
        if (indexManager != null) {
            List<Row> indexOptimized = tryIndexOptimization(condition, indexManager, rows);
            if (indexOptimized != null) {
                return indexOptimized;
            }
        }
        
        // Fall back to full table scan
        for (Row row : rows) {
            if (evaluateCondition(condition, row)) {
                filtered.add(row);
            }
        }
        
        return filtered;
    }
    
    private List<Row> tryIndexOptimization(Expression condition, IndexManager indexManager, List<Row> allRows) {
        // Simple optimization for equality conditions
        if (condition instanceof BinaryExpression) {
            BinaryExpression binExpr = (BinaryExpression) condition;
            
            if ("=".equals(binExpr.getOperator()) && 
                binExpr.getLeft() instanceof ColumnExpression &&
                binExpr.getRight() instanceof LiteralExpression) {
                
                String columnName = ((ColumnExpression) binExpr.getLeft()).getColumnName();
                Object value = ((LiteralExpression) binExpr.getRight()).getValue();
                
                if (indexManager.hasIndex(columnName)) {
                    TypedValue typedValue = convertToTypedValue(value);
                    if (typedValue != null) {
                        Integer rowIndex = indexManager.search(columnName, typedValue);
                        if (rowIndex != null && rowIndex < allRows.size()) {
                            return Arrays.asList(allRows.get(rowIndex));
                        }
                        return new ArrayList<>(); // No matches
                    }
                }
            }
            
            // Range query optimization for B-Tree indexes
            // This could be expanded to handle BETWEEN, <, >, etc.
        }
        
        return null; // No optimization possible
    }
    
    private TypedValue convertToTypedValue(Object value) {
        if (value instanceof String) {
            return new VarcharValue((String) value);
        } else if (value instanceof Integer) {
            return new IntegerValue((Integer) value);
        } else if (value instanceof Double) {
            return new DoubleValue((Double) value);
        } else if (value instanceof Boolean) {
            return new BooleanValue((Boolean) value);
        }
        return null;
    }
    
    private boolean evaluateCondition(Expression condition, Row row) {
        try {
            return evaluateExpressionWithContext(condition, row);
        } catch (Exception e) {
            return false;
        }
    }
    
    private boolean evaluateExpressionWithContext(Expression expr, Row row) {
        if (expr instanceof BinaryExpression) {
            BinaryExpression binExpr = (BinaryExpression) expr;
            Object leftValue = evaluateWithContext(binExpr.getLeft(), row);
            Object rightValue = evaluateWithContext(binExpr.getRight(), row);
            
            switch (binExpr.getOperator()) {
                case "=":
                    return Objects.equals(leftValue, rightValue);
                case "!=":
                case "<>":
                    return !Objects.equals(leftValue, rightValue);
                case "AND":
                    return isTrue(leftValue) && isTrue(rightValue);
                case "OR":
                    return isTrue(leftValue) || isTrue(rightValue);
                default:
                    return false;
            }
        }
        
        Object result = evaluateWithContext(expr, row);
        return isTrue(result);
    }
    
    private Object evaluateWithContext(Expression expr, Row row) {
        if (expr instanceof ColumnExpression) {
            String columnName = ((ColumnExpression) expr).getColumnName();
            TypedValue value = row.getValue(columnName);
            return value != null ? value.getValue() : null;
        } else if (expr instanceof LiteralExpression) {
            return ((LiteralExpression) expr).getValue();
        }
        
        return expr.evaluate();
    }
    
    private boolean isTrue(Object value) {
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return value != null;
    }
    
    private List<Row> applyProjection(List<Row> rows, List<String> columns, Schema schema) {
        if (columns.contains("*")) {
            return rows; // Return all columns
        }
        
        List<Row> projected = new ArrayList<>();
        for (Row row : rows) {
            Row newRow = new Row();
            for (String column : columns) {
                TypedValue value = row.getValue(column);
                if (value != null) {
                    newRow.setValue(column, value);
                }
            }
            projected.add(newRow);
        }
        
        return projected;
    }
    
    private List<Row> applyDistinct(List<Row> rows) {
        Set<String> seen = new HashSet<>();
        List<Row> distinct = new ArrayList<>();
        
        for (Row row : rows) {
            String rowKey = row.toString(); // Simple distinctness check
            if (!seen.contains(rowKey)) {
                seen.add(rowKey);
                distinct.add(row);
            }
        }
        
        return distinct;
    }
    
    private List<Row> applyOrderBy(List<Row> rows, OrderByClause orderBy) {
        // Simple sorting implementation
        List<Row> sorted = new ArrayList<>(rows);
        String firstColumn = orderBy.getColumns().get(0);
        
        sorted.sort((r1, r2) -> {
            TypedValue v1 = r1.getValue(firstColumn);
            TypedValue v2 = r2.getValue(firstColumn);
            
            if (v1 == null && v2 == null) return 0;
            if (v1 == null) return orderBy.isAscending() ? -1 : 1;
            if (v2 == null) return orderBy.isAscending() ? 1 : -1;
            
            int comparison = v1.compareTo(v2);
            return orderBy.isAscending() ? comparison : -comparison;
        });
        
        return sorted;
    }
    
    private Schema getProjectedSchema(List<String> columns, Schema originalSchema) {
        if (columns.contains("*")) {
            return originalSchema;
        }
        
        List<Column> projectedColumns = new ArrayList<>();
        for (String columnName : columns) {
            for (Column column : originalSchema.getColumns()) {
                if (column.getName().equalsIgnoreCase(columnName)) {
                    projectedColumns.add(column);
                    break;
                }
            }
        }
        
        return new Schema(projectedColumns);
    }
}
