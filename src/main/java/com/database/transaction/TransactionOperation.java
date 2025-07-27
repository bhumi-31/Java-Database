package com.database.transaction;

import com.database.core.Row;
import com.database.types.TypedValue;

import java.util.Map;

/**
 * Represents an operation performed within a transaction.
 */
public class TransactionOperation {
    private final OperationType type;
    private final String tableName;
    private final Row affectedRow;
    private final Row originalRow; // For updates and deletes
    private final long timestamp;
    
    public enum OperationType {
        INSERT,
        UPDATE,
        DELETE
    }
    
    public TransactionOperation(OperationType type, String tableName, Row affectedRow) {
        this(type, tableName, affectedRow, null);
    }
    
    public TransactionOperation(OperationType type, String tableName, Row affectedRow, Row originalRow) {
        this.type = type;
        this.tableName = tableName;
        this.affectedRow = affectedRow;
        this.originalRow = originalRow;
        this.timestamp = System.currentTimeMillis();
    }
    
    /**
     * Creates the inverse operation for rollback purposes.
     */
    public TransactionOperation createInverseOperation() {
        switch (type) {
            case INSERT:
                // Inverse of INSERT is DELETE
                return new TransactionOperation(OperationType.DELETE, tableName, affectedRow);
                
            case DELETE:
                // Inverse of DELETE is INSERT (restore the deleted row)
                return new TransactionOperation(OperationType.INSERT, tableName, affectedRow);
                
            case UPDATE:
                // Inverse of UPDATE is UPDATE with original values
                if (originalRow != null) {
                    return new TransactionOperation(OperationType.UPDATE, tableName, originalRow, affectedRow);
                }
                return null;
                
            default:
                return null;
        }
    }
    
    public OperationType getType() {
        return type;
    }
    
    public String getTableName() {
        return tableName;
    }
    
    public Row getAffectedRow() {
        return affectedRow;
    }
    
    public Row getOriginalRow() {
        return originalRow;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    @Override
    public String toString() {
        return "TransactionOperation{" +
                "type=" + type +
                ", table='" + tableName + '\'' +
                ", rowId=" + (affectedRow != null ? affectedRow.getRowId() : "null") +
                ", timestamp=" + timestamp +
                '}';
    }
}
