package com.database.demo;

import com.database.core.*;
import com.database.types.*;
import com.database.transaction.*;
import com.database.join.*;
import com.database.aggregation.*;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;

/**
 * Demonstrates Phase 4 advanced features including transactions, joins, and aggregations.
 */
public class Phase4Demo {
    public static void main(String[] args) {
        System.out.println("=== Phase 4 Advanced Features Demo ===\n");
        
        // Create database and tables
        Database database = new Database(1L);
        setupTables(database);
        
        // Demonstrate transactions
        demonstrateTransactions(database);
        
        // Demonstrate joins
        demonstrateJoins(database);
        
        // Demonstrate aggregations
        demonstrateAggregations(database);
        
        System.out.println("\n=== Phase 4 Demo Complete ===");
    }
    
    private static void setupTables(Database database) {
        System.out.println("Setting up demo tables...");
        
        // Create Users table
        List<Column> userColumns = new ArrayList<>();
        userColumns.add(new Column("id", DataType.INTEGER, false, true, false));
        userColumns.add(new Column("name", DataType.STRING, false, false, false));
        userColumns.add(new Column("age", DataType.INTEGER, false, false, false));
        userColumns.add(new Column("department_id", DataType.INTEGER, false, false, false));
        
        Schema userSchema = new Schema(userColumns);
        Table usersTable = new Table("users", userSchema);
        database.createTable("users", userSchema);
        
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
        
        // Create Departments table
        List<Column> deptColumns = new ArrayList<>();
        deptColumns.add(new Column("id", DataType.INTEGER, false, true, false));
        deptColumns.add(new Column("name", DataType.STRING, false, false, false));
        deptColumns.add(new Column("budget", DataType.DOUBLE, false, false, false));
        
        Schema deptSchema = new Schema(deptColumns);
        Table departmentsTable = new Table("departments", deptSchema);
        database.createTable("departments", deptSchema);
        
        // Insert department data
        Row dept1 = new Row();
        dept1.setValue("id", new IntegerValue(1));
        dept1.setValue("name", new StringValue("Engineering"));
        dept1.setValue("budget", new DoubleValue(100000.0));
        departmentsTable.insert(dept1);
        
        Row dept2 = new Row();
        dept2.setValue("id", new IntegerValue(2));
        dept2.setValue("name", new StringValue("Marketing"));
        dept2.setValue("budget", new DoubleValue(75000.0));
        departmentsTable.insert(dept2);
        
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
        
        // Simulate an update operation
        Row existingUser = usersTable.getRows().get(0); // Get first user
        Row updatedUser = new Row();
        updatedUser.setValue("id", existingUser.getValue("id"));
        updatedUser.setValue("name", existingUser.getValue("name"));
        updatedUser.setValue("age", new IntegerValue(31)); // Update age
        updatedUser.setValue("department_id", existingUser.getValue("department_id"));
        
        TransactionOperation updateOp = new TransactionOperation(
            TransactionOperation.OperationType.UPDATE, "users", updatedUser, existingUser);
        transaction.recordOperation(updateOp);
        
        System.out.println("Added " + transaction.getOperations().size() + " operations to transaction");
        
        // Commit the transaction
        boolean committed = transactionManager.commitTransaction(transaction);
        System.out.println("Transaction committed: " + committed);
        System.out.println("Users table now has " + usersTable.getRows().size() + " rows\n");
    }
    
    private static void demonstrateJoins(Database database) {
        System.out.println("=== Join Operations Demo ===");
        
        Table usersTable = database.getTable("users");
        Table departmentsTable = database.getTable("departments");
        JoinOperator joinOperator = new JoinOperator();
        
        // Perform INNER JOIN
        System.out.println("INNER JOIN (users and departments):");
        List<Row> innerJoinResult = joinOperator.performJoin(
            usersTable, departmentsTable, "department_id", "id", JoinOperator.JoinType.INNER);
        
        for (Row row : innerJoinResult) {
            System.out.println("User: " + row.getField("name") + 
                             ", Department: " + row.getField("right_name") +
                             ", Budget: " + row.getField("right_budget"));
        }
        
        // Perform LEFT JOIN
        System.out.println("\nLEFT JOIN (users and departments):");
        List<Row> leftJoinResult = joinOperator.performJoin(
            usersTable, departmentsTable, "department_id", "id", JoinOperator.JoinType.LEFT);
        
        System.out.println("Left join returned " + leftJoinResult.size() + " rows");
        
        System.out.println();
    }
    
    private static void demonstrateAggregations(Database database) {
        System.out.println("=== Aggregation Functions Demo ===");
        
        Table usersTable = database.getTable("users");
        AggregationOperator aggregationOperator = new AggregationOperator();
        
        List<Row> users = usersTable.getAllRows();
        
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
