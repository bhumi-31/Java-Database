package com.database.query.ast;

/**
 * AST node for transaction operations.
 */
public class TransactionStatement extends SQLStatement {
    private final TransactionType transactionType;
    
    public enum TransactionType {
        BEGIN,
        COMMIT,
        ROLLBACK
    }
    
    public TransactionStatement(TransactionType transactionType) {
        this.transactionType = transactionType;
    }
    
    public TransactionType getTransactionType() { return transactionType; }
    
    @Override
    public void execute() {
        // Implementation would be handled by query executor
    }
    
    @Override
    public String toSQL() {
        switch (transactionType) {
            case BEGIN:
                return "BEGIN TRANSACTION";
            case COMMIT:
                return "COMMIT";
            case ROLLBACK:
                return "ROLLBACK";
            default:
                return "UNKNOWN TRANSACTION";
        }
    }
    
    @Override
    public String toString() {
        return "TransactionStatement{" +
                "transactionType=" + transactionType +
                '}';
    }
}
