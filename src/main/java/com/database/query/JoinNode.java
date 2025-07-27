package com.database.query;

import com.database.join.JoinOperator;
import com.database.aggregation.AggregationOperator;

/**
 * AST node for JOIN operations.
 */
public class JoinNode extends ASTNode {
    private final String leftTable;
    private final String rightTable;
    private final String leftColumn;
    private final String rightColumn;
    private final JoinOperator.JoinType joinType;
    
    public JoinNode(String leftTable, String rightTable, String leftColumn, 
                   String rightColumn, JoinOperator.JoinType joinType) {
        this.leftTable = leftTable;
        this.rightTable = rightTable;
        this.leftColumn = leftColumn;
        this.rightColumn = rightColumn;
        this.joinType = joinType;
    }
    
    public String getLeftTable() { return leftTable; }
    public String getRightTable() { return rightTable; }
    public String getLeftColumn() { return leftColumn; }
    public String getRightColumn() { return rightColumn; }
    public JoinOperator.JoinType getJoinType() { return joinType; }
    
    @Override
    public String toString() {
        return "JoinNode{" +
                "leftTable='" + leftTable + '\'' +
                ", rightTable='" + rightTable + '\'' +
                ", leftColumn='" + leftColumn + '\'' +
                ", rightColumn='" + rightColumn + '\'' +
                ", joinType=" + joinType +
                '}';
    }
}

/**
 * AST node for aggregate operations.
 */
class AggregateNode extends ASTNode {
    private final String tableName;
    private final String aggregateColumn;
    private final AggregationOperator.AggregateFunction function;
    private final String groupByColumn;
    
    public AggregateNode(String tableName, String aggregateColumn, 
                        AggregationOperator.AggregateFunction function) {
        this(tableName, aggregateColumn, function, null);
    }
    
    public AggregateNode(String tableName, String aggregateColumn, 
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
    public String toString() {
        return "AggregateNode{" +
                "tableName='" + tableName + '\'' +
                ", aggregateColumn='" + aggregateColumn + '\'' +
                ", function=" + function +
                ", groupByColumn='" + groupByColumn + '\'' +
                '}';
    }
}

/**
 * AST node for transaction operations.
 */
class TransactionNode extends ASTNode {
    private final TransactionType transactionType;
    
    public enum TransactionType {
        BEGIN,
        COMMIT,
        ROLLBACK
    }
    
    public TransactionNode(TransactionType transactionType) {
        this.transactionType = transactionType;
    }
    
    public TransactionType getTransactionType() { return transactionType; }
    
    @Override
    public String toString() {
        return "TransactionNode{" +
                "transactionType=" + transactionType +
                '}';
    }
}
