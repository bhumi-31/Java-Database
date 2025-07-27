package com.database.demo;

import com.database.core.*;
import com.database.storage.StorageEngine;
import com.database.types.*;
import com.database.index.IndexManager;
import com.database.query.*;
import com.database.query.ast.SQLStatement;

import java.util.*;

/**
 * Comprehensive demo showcasing Phase 3 - SQL Query Processing capabilities.
 */
public class QueryProcessingDemo {
    
    public static void main(String[] args) {
        System.out.println("=== Custom Database Engine - Phase 3 Query Processing Demo ===");
        
        try {
            demonstrateQueryProcessing();
        } catch (Exception e) {
            System.err.println("Error during demo: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void demonstrateQueryProcessing() {
        System.out.println("\n1. Setting up database and tables...");
        
        // Create storage engine and query executor
        StorageEngine storage = new StorageEngine();
        QueryExecutor queryExecutor = new QueryExecutor(storage);
        
        // Create employees table
        setupEmployeesTable(storage, queryExecutor);
        
        System.out.println("\n2. Testing SQL Parser...");
        testSQLParser();
        
        System.out.println("\n3. Executing SQL queries...");
        executeSQLQueries(queryExecutor);
        
        System.out.println("\n4. Testing index-optimized queries...");
        testIndexOptimization(queryExecutor);
        
        System.out.println("\n5. Complex query examples...");
        demonstrateComplexQueries(queryExecutor);
        
        System.out.println("\n=== Phase 3 Query Processing Demo Complete ===");
    }
    
    private static void setupEmployeesTable(StorageEngine storage, QueryExecutor queryExecutor) {
        // Create employees table schema
        List<Column> employeeColumns = Arrays.asList(
            new Column("id", DataType.INTEGER, false, true),
            new Column("name", DataType.VARCHAR, false, false),
            new Column("age", DataType.INTEGER, false, false),
            new Column("department", DataType.VARCHAR, false, false),
            new Column("salary", DataType.DOUBLE, false, false),
            new Column("active", DataType.BOOLEAN, false, false)
        );
        
        Schema employeeSchema = new Schema(employeeColumns);
        Table employees = storage.createTable("employees", employeeSchema);
        
        // Create and register index manager
        IndexManager indexManager = new IndexManager();
        indexManager.createPrimaryKeyIndex("id");
        indexManager.createIndex("name", IndexManager.IndexType.HASH);
        indexManager.createIndex("age", IndexManager.IndexType.BTREE);
        indexManager.createIndex("department", IndexManager.IndexType.HASH);
        queryExecutor.registerIndexManager("employees", indexManager);
        
        // Insert sample data
        insertSampleData(employees, indexManager);
        
        System.out.println("Created 'employees' table with " + employees.getRowCount() + " rows");
        System.out.println("Registered indexes: id (PK), name (HASH), age (BTREE), department (HASH)");
    }
    
    private static void insertSampleData(Table table, IndexManager indexManager) {
        String[][] data = {
            {"1", "Alice Johnson", "28", "Engineering", "75000", "true"},
            {"2", "Bob Smith", "35", "Sales", "65000", "true"},
            {"3", "Carol Davis", "42", "Engineering", "85000", "false"},
            {"4", "David Wilson", "29", "Marketing", "60000", "true"},
            {"5", "Eve Brown", "31", "Engineering", "78000", "true"},
            {"6", "Frank Miller", "38", "Sales", "70000", "true"},
            {"7", "Grace Lee", "26", "Marketing", "58000", "true"},
            {"8", "Henry Chen", "45", "Engineering", "92000", "true"},
            {"9", "Ivy Taylor", "33", "Sales", "72000", "false"},
            {"10", "Jack Wilson", "39", "Marketing", "65000", "true"}
        };
        
        for (String[] rowData : data) {
            Map<String, TypedValue> values = new HashMap<>();
            values.put("id", new IntegerValue(Integer.parseInt(rowData[0])));
            values.put("name", new VarcharValue(rowData[1]));
            values.put("age", new IntegerValue(Integer.parseInt(rowData[2])));
            values.put("department", new VarcharValue(rowData[3]));
            values.put("salary", new DoubleValue(Double.parseDouble(rowData[4])));
            values.put("active", new BooleanValue(Boolean.parseBoolean(rowData[5])));
            
            Row row = new Row();
            for (Map.Entry<String, TypedValue> entry : values.entrySet()) {
                row.setValue(entry.getKey(), entry.getValue());
            }
            
            table.insert(row);
            indexManager.insert(values, table.getRowCount() - 1);
        }
    }
    
    private static void testSQLParser() {
        SQLParser parser = new SQLParser();
        
        String[] testQueries = {
            "SELECT * FROM employees",
            "SELECT name, age FROM employees WHERE department = 'Engineering'",
            "SELECT name FROM employees WHERE age > 30 AND active = true",
            "SELECT DISTINCT department FROM employees",
            "SELECT * FROM employees WHERE name = 'Alice Johnson'"
        };
        
        for (String sql : testQueries) {
            try {
                System.out.println("\nParsing: " + sql);
                SQLStatement statement = parser.parse(sql);
                System.out.println("  Parsed successfully: " + statement.getClass().getSimpleName());
                System.out.println("  Reconstructed SQL: " + statement.toSQL());
            } catch (Exception e) {
                System.out.println("  Parse error: " + e.getMessage());
            }
        }
    }
    
    private static void executeSQLQueries(QueryExecutor queryExecutor) {
        String[] queries = {
            "SELECT * FROM employees",
            "SELECT name, age, department FROM employees",
            "SELECT * FROM employees WHERE department = 'Engineering'",
            "SELECT name FROM employees WHERE age > 35"
        };
        
        for (String sql : queries) {
            System.out.println("\n--- Executing: " + sql + " ---");
            try {
                QueryResult result = queryExecutor.execute(sql);
                System.out.println(result.formatAsTable());
            } catch (Exception e) {
                System.out.println("Execution error: " + e.getMessage());
            }
        }
    }
    
    private static void testIndexOptimization(QueryExecutor queryExecutor) {
        System.out.println("\n--- Index-Optimized Queries ---");
        
        String[] indexQueries = {
            "SELECT * FROM employees WHERE name = 'Bob Smith'",        // Hash index optimization
            "SELECT * FROM employees WHERE department = 'Sales'",      // Hash index optimization
            "SELECT * FROM employees WHERE id = 5"                     // Primary key optimization
        };
        
        for (String sql : indexQueries) {
            System.out.println("\n" + sql);
            long startTime = System.currentTimeMillis();
            try {
                QueryResult result = queryExecutor.execute(sql);
                long executionTime = System.currentTimeMillis() - startTime;
                System.out.println("Execution time: " + executionTime + "ms");
                System.out.println("Results: " + result.getRowCount() + " rows");
                
                if (!result.isEmpty()) {
                    System.out.println(result.formatAsTable());
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }
    
    private static void demonstrateComplexQueries(QueryExecutor queryExecutor) {
        String[] complexQueries = {
            "SELECT name, age FROM employees WHERE age > 30 AND department = 'Engineering'",
            "SELECT DISTINCT department FROM employees",
            "SELECT * FROM employees WHERE active = true AND salary > 70000"
        };
        
        for (String sql : complexQueries) {
            System.out.println("\n--- Complex Query: " + sql + " ---");
            try {
                long startTime = System.currentTimeMillis();
                QueryResult result = queryExecutor.execute(sql);
                long executionTime = System.currentTimeMillis() - startTime;
                
                System.out.println("Execution time: " + executionTime + "ms");
                System.out.println(result.formatAsTable());
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
