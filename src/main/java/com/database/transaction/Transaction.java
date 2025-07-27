package com.database.transaction;

import com.database.core.Row;
import com.database.core.Table;
import com.database.types.TypedValue;

import java.util.*;

/**
 * Represents a database transaction with ACID properties.
 */
public class Transaction {
    private final String transactionId;
    private final long startTime;
    private TransactionState state;
    private final List<TransactionOperation> operations;
    private final Map<String, List<Row>> snapshotData;
    
    public enum TransactionState {
        ACTIVE,
        COMMITTED,
        ABORTED
    }
    
    public Transaction(String transactionId) {
        this.transactionId = transactionId;
        this.startTime = System.currentTimeMillis();
        this.state = TransactionState.ACTIVE;
        this.operations = new ArrayList<>();
        this.snapshotData = new HashMap<>();
    }
    
    /**
     * Records an operation performed in this transaction.
     */
    public void recordOperation(TransactionOperation operation) {
        if (state != TransactionState.ACTIVE) {
            throw new IllegalStateException("Cannot perform operations on non-active transaction");
        }
        operations.add(operation);
    }
    
    /**
     * Creates a snapshot of table data before modification.
     */
    public void createSnapshot(String tableName, List<Row> data) {
        if (!snapshotData.containsKey(tableName)) {
            // Deep copy of rows
            List<Row> snapshot = new ArrayList<>();
            for (Row row : data) {
                Row copiedRow = new Row(row.getRowId());
                // Copy all values
                for (String columnName : getAllColumnNames(row)) {
                    TypedValue value = row.getValue(columnName);
                    if (value != null) {
                        copiedRow.setValue(columnName, value);
                    }
                }
                snapshot.add(copiedRow);
            }
            snapshotData.put(tableName, snapshot);
        }
    }
    
    private Set<String> getAllColumnNames(Row row) {
        // This is a simplified approach - in a real implementation,
        // we'd use reflection or schema information
        Set<String> columnNames = new HashSet<>();
        // Add common column names for demonstration
        columnNames.addAll(Arrays.asList("id", "name", "age", "department", "salary", "active"));
        return columnNames;
    }
    
    /**
     * Commits the transaction, making all changes permanent.
     */
    public void commit() {
        if (state != TransactionState.ACTIVE) {
            throw new IllegalStateException("Cannot commit non-active transaction");
        }
        
        // In a real implementation, this would write changes to disk
        // and update transaction logs
        state = TransactionState.COMMITTED;
        
        System.out.println("Transaction " + transactionId + " committed with " + 
                          operations.size() + " operations");
    }
    
    /**
     * Rolls back the transaction, undoing all changes.
     */
    public void rollback() {
        if (state != TransactionState.ACTIVE) {
            throw new IllegalStateException("Cannot rollback non-active transaction");
        }
        
        state = TransactionState.ABORTED;
        
        System.out.println("Transaction " + transactionId + " rolled back, " + 
                          operations.size() + " operations undone");
    }
    
    /**
     * Gets the rollback operations to undo this transaction.
     */
    public List<TransactionOperation> getRollbackOperations() {
        List<TransactionOperation> rollbackOps = new ArrayList<>();
        
        // Reverse the operations and create inverse operations
        for (int i = operations.size() - 1; i >= 0; i--) {
            TransactionOperation op = operations.get(i);
            TransactionOperation inverseOp = op.createInverseOperation();
            if (inverseOp != null) {
                rollbackOps.add(inverseOp);
            }
        }
        
        return rollbackOps;
    }
    
    public String getTransactionId() {
        return transactionId;
    }
    
    public long getStartTime() {
        return startTime;
    }
    
    public TransactionState getState() {
        return state;
    }
    
    public List<TransactionOperation> getOperations() {
        return new ArrayList<>(operations);
    }
    
    public Map<String, List<Row>> getSnapshotData() {
        return snapshotData;
    }
    
    @Override
    public String toString() {
        return "Transaction{" +
                "id='" + transactionId + '\'' +
                ", state=" + state +
                ", operations=" + operations.size() +
                ", duration=" + (System.currentTimeMillis() - startTime) + "ms" +
                '}';
    }
}
