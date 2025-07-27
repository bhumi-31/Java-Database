package com.database.demo;

import com.database.core.*;
import com.database.types.*;
import com.database.transaction.*;
import com.database.aggregation.*;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;

/**
 * Simplified Phase 4 demonstration focusing on core advanced features.
 */
public class SimplePhase4Demo {
    // Type alias for easier usage
    private static class StringValue extends VarcharValue {
        public StringValue(String value) {
            super(value);
        }
    }
    
    public static void main(String[] args) {
        System.out.println("=== Phase 4 Advanced Features Demo (Simplified) ===\n");
        
        // Create database and tables with sufficient memory
        Database database = new Database(10_000_000L); // 10MB should be plenty
        setupTables(database);
        
        // Demonstrate transactions
        demonstrateTransactions(database);
        
        // Demonstrate aggregations
        demonstrateAggregations(database);
        
        System.out.println("\n=== Phase 4 Demo Complete ===");
    }
    
    private static void setupTables(Database database) {
        System.out.println("Setting up demo tables...");
        
        // Create Users table
        List<Column> userColumns = new ArrayList<>();
        userColumns.add(new Column("id", DataType.INTEGER, false, true, 0));
        userColumns.add(new Column("name", DataType.VARCHAR, false, false, 255));
        userColumns.add(new Column("age", DataType.INTEGER, false, false, 0));
        userColumns.add(new Column("department_id", DataType.INTEGER, false, false, 0));
        
        Schema userSchema = new Schema(userColumns);
        database.createTable("users", userSchema);
        Table usersTable = database.getTable("users");
        
        // Insert user data
        Row user1 = new Row();
        user1.setValue("id", new IntegerValue(1));
        user1.setValue("name", new StringValue("Alice"));
        user1.setValue("age", new IntegerValue(30));
        user1.setValue("department_id", new IntegerValue(1));
        usersTable.insert(user1);
        
        Row user2 = new Row();
        user2.setValue("id", new IntegerValue(2));
        user2.setValue("name", new StringValue("Bob"));
        user2.setValue("age", new IntegerValue(25));
        user2.setValue("department_id", new IntegerValue(2));
        usersTable.insert(user2);
        
        Row user3 = new Row();
        user3.setValue("id", new IntegerValue(3));
        user3.setValue("name", new StringValue("Charlie"));
        user3.setValue("age", new IntegerValue(35));
        user3.setValue("department_id", new IntegerValue(1));
        usersTable.insert(user3);
        
        System.out.println("Tables created with sample data.\n");
    }
    
    private static void demonstrateTransactions(Database database) {
        System.out.println("=== Transaction Management Demo ===");
        
        TransactionManager transactionManager = new TransactionManager(database);
        Table usersTable = database.getTable("users");
        
        // Begin a transaction
        Transaction transaction = transactionManager.beginTransaction();
        System.out.println("Started transaction: " + transaction.getTransactionId());
        
        // Add operations to the transaction
        Row newUser = new Row();
        newUser.setValue("id", new IntegerValue(4));
        newUser.setValue("name", new StringValue("Diana"));
        newUser.setValue("age", new IntegerValue(28));
        newUser.setValue("department_id", new IntegerValue(2));
        
        TransactionOperation insertOp = new TransactionOperation(
            TransactionOperation.OperationType.INSERT, "users", newUser);
        transaction.recordOperation(insertOp);
        
        System.out.println("Added " + transaction.getOperations().size() + " operations to transaction");
        
        // For demo purposes, let's show the transaction state
        System.out.println("Transaction state: " + transaction.getState());
        System.out.println("Current users table has " + usersTable.getRowCount() + " rows");
        
        // In a real implementation, we would commit the transaction
        // For this demo, we'll just show the concept
        System.out.println("Transaction management demonstrated successfully.\n");
    }
    
    private static void demonstrateAggregations(Database database) {
        System.out.println("=== Aggregation Functions Demo ===");
        
        Table usersTable = database.getTable("users");
        AggregationOperator aggregationOperator = new AggregationOperator();
        
        List<Row> users = usersTable.selectAll();
        
        // Demonstrate COUNT
        TypedValue count = aggregationOperator.performAggregation(
            users, null, AggregationOperator.AggregateFunction.COUNT);
        System.out.println("COUNT(*): " + count);
        
        // Demonstrate AVG (average age)
        TypedValue avgAge = aggregationOperator.performAggregation(
            users, "age", AggregationOperator.AggregateFunction.AVG);
        System.out.println("AVG(age): " + avgAge);
        
        // Demonstrate MIN and MAX age
        TypedValue minAge = aggregationOperator.performAggregation(
            users, "age", AggregationOperator.AggregateFunction.MIN);
        TypedValue maxAge = aggregationOperator.performAggregation(
            users, "age", AggregationOperator.AggregateFunction.MAX);
        System.out.println("MIN(age): " + minAge);
        System.out.println("MAX(age): " + maxAge);
        
        // Demonstrate GROUP BY with aggregation
        System.out.println("\nGROUP BY department_id with COUNT:");
        Map<TypedValue, TypedValue> groupedCount = aggregationOperator.performGroupByAggregation(
            users, "department_id", null, AggregationOperator.AggregateFunction.COUNT);
        
        for (Map.Entry<TypedValue, TypedValue> entry : groupedCount.entrySet()) {
            System.out.println("Department " + entry.getKey() + ": " + entry.getValue() + " users");
        }
        
        System.out.println("\nGROUP BY department_id with AVG(age):");
        Map<TypedValue, TypedValue> groupedAvgAge = aggregationOperator.performGroupByAggregation(
            users, "department_id", "age", AggregationOperator.AggregateFunction.AVG);
        
        for (Map.Entry<TypedValue, TypedValue> entry : groupedAvgAge.entrySet()) {
            System.out.println("Department " + entry.getKey() + " avg age: " + entry.getValue());
        }
    }
}
