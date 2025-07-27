package com.database.join;

import com.database.core.Row;
import com.database.core.Schema;
import com.database.core.Table;
import com.database.core.Column;
import com.database.types.TypedValue;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles different types of join operations between tables.
 */
public class JoinOperator {
    
    public enum JoinType {
        INNER,
        LEFT,
        RIGHT,
        FULL
    }
    
    /**
     * Performs a join operation between two tables.
     */
    public List<Row> performJoin(Table leftTable, Table rightTable, 
                                String leftColumn, String rightColumn, 
                                JoinType joinType) {
        switch (joinType) {
            case INNER:
                return innerJoin(leftTable, rightTable, leftColumn, rightColumn);
            case LEFT:
                return leftJoin(leftTable, rightTable, leftColumn, rightColumn);
            case RIGHT:
                return rightJoin(leftTable, rightTable, leftColumn, rightColumn);
            case FULL:
                return fullJoin(leftTable, rightTable, leftColumn, rightColumn);
            default:
                throw new IllegalArgumentException("Unsupported join type: " + joinType);
        }
    }
    
    /**
     * Performs an INNER JOIN between two tables.
     */
    private List<Row> innerJoin(Table leftTable, Table rightTable, 
                               String leftColumn, String rightColumn) {
        List<Row> result = new ArrayList<>();
        
        for (Row leftRow : leftTable.selectAll()) {
            TypedValue leftValue = leftRow.getValue(leftColumn);
            if (leftValue == null) continue;
            
            for (Row rightRow : rightTable.selectAll()) {
                TypedValue rightValue = rightRow.getValue(rightColumn);
                if (rightValue != null && leftValue.equals(rightValue)) {
                    Row joinedRow = createJoinedRow(leftRow, rightRow, leftTable.getSchema(), rightTable.getSchema());
                    result.add(joinedRow);
                }
            }
        }
        
        return result;
    }
    
    /**
     * Performs a LEFT JOIN between two tables.
     */
    private List<Row> leftJoin(Table leftTable, Table rightTable, 
                              String leftColumn, String rightColumn) {
        List<Row> result = new ArrayList<>();
        
        for (Row leftRow : leftTable.selectAll()) {
            TypedValue leftValue = leftRow.getValue(leftColumn);
            boolean matchFound = false;
            
            if (leftValue != null) {
                for (Row rightRow : rightTable.selectAll()) {
                    TypedValue rightValue = rightRow.getValue(rightColumn);
                    if (rightValue != null && leftValue.equals(rightValue)) {
                        Row joinedRow = createJoinedRow(leftRow, rightRow, leftTable.getSchema(), rightTable.getSchema());
                        result.add(joinedRow);
                        matchFound = true;
                    }
                }
            }
            
            // If no match found, include left row with null values for right table
            if (!matchFound) {
                Row joinedRow = createJoinedRowWithNulls(leftRow, leftTable.getSchema(), rightTable.getSchema(), true);
                result.add(joinedRow);
            }
        }
        
        return result;
    }
    
    /**
     * Performs a RIGHT JOIN between two tables.
     */
    private List<Row> rightJoin(Table leftTable, Table rightTable, 
                               String leftColumn, String rightColumn) {
        List<Row> result = new ArrayList<>();
        
        for (Row rightRow : rightTable.selectAll()) {
            TypedValue rightValue = rightRow.getValue(rightColumn);
            boolean matchFound = false;
            
            if (rightValue != null) {
                for (Row leftRow : leftTable.selectAll()) {
                    TypedValue leftValue = leftRow.getValue(leftColumn);
                    if (leftValue != null && leftValue.equals(rightValue)) {
                        Row joinedRow = createJoinedRow(leftRow, rightRow, leftTable.getSchema(), rightTable.getSchema());
                        result.add(joinedRow);
                        matchFound = true;
                    }
                }
            }
            
            // If no match found, include right row with null values for left table
            if (!matchFound) {
                Row joinedRow = createJoinedRowWithNulls(rightRow, leftTable.getSchema(), rightTable.getSchema(), false);
                result.add(joinedRow);
            }
        }
        
        return result;
    }
    
    /**
     * Performs a FULL OUTER JOIN between two tables.
     */
    private List<Row> fullJoin(Table leftTable, Table rightTable, 
                              String leftColumn, String rightColumn) {
        List<Row> result = new ArrayList<>();
        List<Row> rightRowsMatched = new ArrayList<>();
        
        // Process left table rows
        for (Row leftRow : leftTable.selectAll()) {
            TypedValue leftValue = leftRow.getValue(leftColumn);
            boolean matchFound = false;
            
            if (leftValue != null) {
                for (Row rightRow : rightTable.selectAll()) {
                    TypedValue rightValue = rightRow.getValue(rightColumn);
                    if (rightValue != null && leftValue.equals(rightValue)) {
                        Row joinedRow = createJoinedRow(leftRow, rightRow, leftTable.getSchema(), rightTable.getSchema());
                        result.add(joinedRow);
                        rightRowsMatched.add(rightRow);
                        matchFound = true;
                    }
                }
            }
            
            // If no match found, include left row with null values for right table
            if (!matchFound) {
                Row joinedRow = createJoinedRowWithNulls(leftRow, leftTable.getSchema(), rightTable.getSchema(), true);
                result.add(joinedRow);
            }
        }
        
        // Process unmatched right table rows
        for (Row rightRow : rightTable.selectAll()) {
            if (!rightRowsMatched.contains(rightRow)) {
                Row joinedRow = createJoinedRowWithNulls(rightRow, leftTable.getSchema(), rightTable.getSchema(), false);
                result.add(joinedRow);
            }
        }
        
        return result;
    }
    
    /**
     * Creates a joined row by combining fields from both tables.
     */
    private Row createJoinedRow(Row leftRow, Row rightRow, Schema leftSchema, Schema rightSchema) {
        Row joinedRow = new Row();
        
        // Add fields from left table
        for (Column column : leftSchema.getColumns()) {
            joinedRow.setValue(column.getName(), leftRow.getValue(column.getName()));
        }
        
        // Add fields from right table
        for (Column column : rightSchema.getColumns()) {
            joinedRow.setValue(column.getName(), rightRow.getValue(column.getName()));
        }
        
        return joinedRow;
    }
    
    /**
     * Creates a joined row with null values for one side of the join.
     */
    private Row createJoinedRowWithNulls(Row sourceRow, Schema leftSchema, Schema rightSchema, boolean isLeft) {
        Row joinedRow = new Row();
        
        if (isLeft) {
            // Source is from left table, add values for left and nulls for right
            for (Column column : leftSchema.getColumns()) {
                joinedRow.setValue(column.getName(), sourceRow.getValue(column.getName()));
            }
            for (Column column : rightSchema.getColumns()) {
                joinedRow.setValue(column.getName(), null);
            }
        } else {
            // Source is from right table, add nulls for left and values for right
            for (Column column : leftSchema.getColumns()) {
                joinedRow.setValue(column.getName(), null);
            }
            for (Column column : rightSchema.getColumns()) {
                joinedRow.setValue(column.getName(), sourceRow.getValue(column.getName()));
            }
        }
        
        return joinedRow;
    }
    
    /**
     * Creates a schema for the joined result.
     */
    public Schema createJoinedSchema(Schema leftSchema, Schema rightSchema) {
        List<Column> joinedColumns = new ArrayList<>();
        
        // Add columns from left table
        for (Column column : leftSchema.getColumns()) {
            joinedColumns.add(new Column(column.getName(), column.getType(), column.isNullable(), false));
        }
        
        // Add columns from right table
        for (Column column : rightSchema.getColumns()) {
            joinedColumns.add(new Column(column.getName(), column.getType(), column.isNullable(), false));
        }
        
        return new Schema(joinedColumns);
    }
}
