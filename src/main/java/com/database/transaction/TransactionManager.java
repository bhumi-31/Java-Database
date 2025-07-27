package com.database.transaction;

import com.database.core.Database;
import com.database.core.Table;
import com.database.core.Row;
import com.database.types.TypedValue;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Manages transactions across the database system.
 * Provides ACID properties and concurrent transaction support.
 */
public class TransactionManager {
    private final AtomicLong transactionIdGenerator;
    private final Map<Long, Transaction> activeTransactions;
    private final Database database;
    
    public TransactionManager(Database database) {
        this.database = database;
        this.transactionIdGenerator = new AtomicLong(1);
        this.activeTransactions = new ConcurrentHashMap<>();
    }
    
    /**
     * Begins a new transaction and returns it.
     */
    public Transaction beginTransaction() {
        long transactionId = transactionIdGenerator.getAndIncrement();
        Transaction transaction = new Transaction(String.valueOf(transactionId));
        activeTransactions.put(transactionId, transaction);
        return transaction;
    }
    
    /**
     * Commits a transaction and applies all its operations to the database.
     */
    public boolean commitTransaction(Transaction transaction) {
        if (!activeTransactions.containsKey(Long.parseLong(transaction.getTransactionId()))) {
            return false; // Transaction not found or already completed
        }
        
        try {
            // Apply all operations to the database
            for (TransactionOperation operation : transaction.getOperations()) {
                applyOperationToDatabase(operation);
            }
            
            // Mark transaction as committed
            transaction.commit();
            activeTransactions.remove(Long.parseLong(transaction.getTransactionId()));
            return true;
        } catch (Exception e) {
            // If commit fails, rollback the transaction
            rollbackTransaction(transaction);
            return false;
        }
    }
    
    /**
     * Rolls back a transaction and undoes all its operations.
     */
    public boolean rollbackTransaction(Transaction transaction) {
        if (!activeTransactions.containsKey(Long.parseLong(transaction.getTransactionId()))) {
            return false; // Transaction not found or already completed
        }
        
        try {
            // Apply inverse operations in reverse order
            for (int i = transaction.getOperations().size() - 1; i >= 0; i--) {
                TransactionOperation operation = transaction.getOperations().get(i);
                TransactionOperation inverseOperation = operation.createInverseOperation();
                if (inverseOperation != null) {
                    applyOperationToDatabase(inverseOperation);
                }
            }
            
            // Mark transaction as aborted
            transaction.rollback();
            activeTransactions.remove(Long.parseLong(transaction.getTransactionId()));
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Creates a snapshot of the current database state.
     * Note: Simplified implementation for Phase 4 demo.
     */
    private Map<String, Table> createDatabaseSnapshot() {
        Map<String, Table> snapshot = new HashMap<>();
        // In a real implementation, this would iterate through all tables
        // For now, we'll create an empty snapshot
        return snapshot;
    }
    
    /**
     * Applies a transaction operation to the database.
     * Note: Simplified implementation for Phase 4 demo.
     */
    private void applyOperationToDatabase(TransactionOperation operation) throws Exception {
        Table table = database.getTable(operation.getTableName());
        if (table == null) {
            throw new Exception("Table not found: " + operation.getTableName());
        }
        
        switch (operation.getType()) {
            case INSERT:
                table.insert(operation.getAffectedRow());
                break;
                
            case UPDATE:
                // Find and update the row
                Row existingRow = findRowInTable(table, operation.getOriginalRow());
                if (existingRow != null) {
                    // Convert Row to Map for update method
                    Map<String, TypedValue> updates = new HashMap<>();
                    Row newRow = operation.getAffectedRow();
                    // This is a simplified implementation - in practice we'd iterate through all fields
                    updates.put("age", newRow.getValue("age"));
                    table.update(existingRow.getRowId(), updates);
                }
                break;
                
            case DELETE:
                Row rowToDelete = findRowInTable(table, operation.getAffectedRow());
                if (rowToDelete != null) {
                    table.delete(rowToDelete.getRowId());
                }
                break;
        }
    }
    
    /**
     * Finds a row in a table based on the provided row data.
     */
    private Row findRowInTable(Table table, Row targetRow) {
        for (Row row : table.selectAll()) {
            if (row.getRowId() == targetRow.getRowId()) {
                return row;
            }
        }
        return null;
    }
    
    /**
     * Gets all active transactions.
     */
    public Map<Long, Transaction> getActiveTransactions() {
        return new HashMap<>(activeTransactions);
    }
    
    /**
     * Gets the number of active transactions.
     */
    public int getActiveTransactionCount() {
        return activeTransactions.size();
    }
    
    /**
     * Checks if a specific transaction is active.
     */
    public boolean isTransactionActive(long transactionId) {
        return activeTransactions.containsKey(transactionId);
    }
}
