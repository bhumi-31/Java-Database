package com.database.query.ast;

import com.database.join.JoinOperator;
import com.database.aggregation.AggregationOperator;

/**
 * AST node for JOIN operations.
 */
public class JoinStatement extends SQLStatement {
    private final String leftTable;
    private final String rightTable;
    private final String leftColumn;
    private final String rightColumn;
    private final JoinOperator.JoinType joinType;
    
    public JoinStatement(String leftTable, String rightTable, String leftColumn, 
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
    public void execute() {
        // Implementation would be handled by query executor
    }
    
    @Override
    public String toSQL() {
        return String.format("SELECT * FROM %s %s JOIN %s ON %s.%s = %s.%s",
                leftTable, joinType.toString(), rightTable,
                leftTable, leftColumn, rightTable, rightColumn);
    }
    
    @Override
    public String toString() {
        return "JoinStatement{" +
                "leftTable='" + leftTable + '\'' +
                ", rightTable='" + rightTable + '\'' +
                ", leftColumn='" + leftColumn + '\'' +
                ", rightColumn='" + rightColumn + '\'' +
                ", joinType=" + joinType +
                '}';
    }
}
