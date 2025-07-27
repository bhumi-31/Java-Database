package com.database.demo;

import com.database.core.*;
import com.database.storage.StorageEngine;
import com.database.types.*;
import com.database.index.*;

import java.util.*;

/**
 * Enhanced demo showcasing the indexing capabilities.
 */
public class IndexingDemo {
    
    public static void main(String[] args) {
        System.out.println("=== Custom Database Engine - Indexing Demo ===");
        
        try {
            demonstrateIndexing();
        } catch (Exception e) {
            System.err.println("Error during demo: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void demonstrateIndexing() {
        System.out.println("\n1. Creating storage engine and tables...");
        
        StorageEngine storage = new StorageEngine();
        
        // Create employees table with schema
        List<Column> employeeColumns = Arrays.asList(
            new Column("id", DataType.INTEGER, false, true),
            new Column("name", DataType.VARCHAR, false, false),
            new Column("age", DataType.INTEGER, false, false),
            new Column("department", DataType.VARCHAR, false, false),
            new Column("salary", DataType.DOUBLE, false, false)
        );
        
        Schema employeeSchema = new Schema(employeeColumns);
        Table employees = storage.createTable("employees", employeeSchema);
        
        System.out.println("Created 'employees' table with schema: " + employeeSchema);
        
        // Create index manager and set up indexes
        IndexManager indexManager = new IndexManager();
        indexManager.createPrimaryKeyIndex("id");
        indexManager.createIndex("name", IndexManager.IndexType.HASH);
        indexManager.createIndex("age", IndexManager.IndexType.BTREE);
        indexManager.createIndex("department", IndexManager.IndexType.HASH);
        
        System.out.println("\n2. Created indexes:");
        Map<String, IndexManager.IndexStats> stats = indexManager.getIndexStatistics();
        for (IndexManager.IndexStats stat : stats.values()) {
            System.out.println("  " + stat);
        }
        
        System.out.println("\n3. Inserting employee data...");
        
        // Insert employee data and maintain indexes
        insertEmployee(employees, indexManager, 1, "Alice Johnson", 28, "Engineering", 75000.0);
        insertEmployee(employees, indexManager, 2, "Bob Smith", 35, "Sales", 65000.0);
        insertEmployee(employees, indexManager, 3, "Carol Davis", 42, "Engineering", 85000.0);
        insertEmployee(employees, indexManager, 4, "David Wilson", 29, "Marketing", 60000.0);
        insertEmployee(employees, indexManager, 5, "Eve Brown", 31, "Engineering", 78000.0);
        insertEmployee(employees, indexManager, 6, "Frank Miller", 38, "Sales", 70000.0);
        insertEmployee(employees, indexManager, 7, "Grace Lee", 26, "Marketing", 58000.0);
        insertEmployee(employees, indexManager, 8, "Henry Chen", 45, "Engineering", 92000.0);
        
        System.out.println("Inserted " + employees.getRowCount() + " employees");
        
        System.out.println("\n4. Testing index-based lookups...");
        
        // Test primary key lookup
        System.out.println("\n--- Primary Key Lookup ---");
        Integer rowId = indexManager.search("id", new IntegerValue(5));
        if (rowId != null) {
            List<Row> allRows = employees.selectAll();
            if (rowId < allRows.size()) {
                Row employee = allRows.get(rowId);
                System.out.println("Employee with ID 5: " + formatEmployee(employee));
            }
        }
        
        // Test name lookup (hash index)
        System.out.println("\n--- Name Lookup (Hash Index) ---");
        rowId = indexManager.search("name", new VarcharValue("Carol Davis"));
        if (rowId != null) {
            List<Row> allRows = employees.selectAll();
            if (rowId < allRows.size()) {
                Row employee = allRows.get(rowId);
                System.out.println("Employee named 'Carol Davis': " + formatEmployee(employee));
            }
        }
        
        // Test department lookup (hash index)
        System.out.println("\n--- Department Lookup (Hash Index) ---");
        rowId = indexManager.search("department", new VarcharValue("Engineering"));
        if (rowId != null) {
            List<Row> allRows = employees.selectAll();
            if (rowId < allRows.size()) {
                Row employee = allRows.get(rowId);
                System.out.println("First Engineering employee found: " + formatEmployee(employee));
            }
        }
        
        // Test age range query (B-Tree index)
        System.out.println("\n--- Age Range Query (B-Tree Index) ---");
        List<Integer> ageRangeResults = indexManager.rangeQuery("age", new IntegerValue(30), new IntegerValue(40));
        System.out.println("Employees aged 30-40:");
        List<Row> allRows = employees.selectAll();
        for (Integer id : ageRangeResults) {
            if (id < allRows.size()) {
                Row employee = allRows.get(id);
                System.out.println("  " + formatEmployee(employee));
            }
        }
        
        System.out.println("\n5. Testing B-Tree and Hash indexes separately...");
        
        // Test B-Tree index directly
        testBTreeIndex();
        
        // Test Hash index directly
        testHashIndex();
        
        System.out.println("\n6. Index performance comparison...");
        compareIndexPerformance();
        
        System.out.println("\n=== Indexing Demo Complete ===");
    }
    
    private static void insertEmployee(Table table, IndexManager indexManager, int id, String name, int age, String department, double salary) {
        Map<String, TypedValue> values = new HashMap<>();
        values.put("id", new IntegerValue(id));
        values.put("name", new VarcharValue(name));
        values.put("age", new IntegerValue(age));
        values.put("department", new VarcharValue(department));
        values.put("salary", new DoubleValue(salary));
        
        Row row = new Row();
        for (Map.Entry<String, TypedValue> entry : values.entrySet()) {
            row.setValue(entry.getKey(), entry.getValue());
        }
        
        table.insert(row);
        
        // Update indexes
        indexManager.insert(values, table.getRowCount() - 1);
    }
    
    private static String formatEmployee(Row employee) {
        return String.format("ID: %s, Name: %s, Age: %s, Dept: %s, Salary: $%.0f",
            employee.getValue("id"),
            employee.getValue("name"),
            employee.getValue("age"),
            employee.getValue("department"),
            ((DoubleValue)employee.getValue("salary")).getValue());
    }
    
    private static void testBTreeIndex() {
        System.out.println("\n--- B-Tree Index Test ---");
        
        BTreeIndex<TypedValue, String> btree = new BTreeIndex<>(4);
        
        // Insert data
        btree.insert(new IntegerValue(10), "Ten");
        btree.insert(new IntegerValue(5), "Five");
        btree.insert(new IntegerValue(15), "Fifteen");
        btree.insert(new IntegerValue(3), "Three");
        btree.insert(new IntegerValue(7), "Seven");
        btree.insert(new IntegerValue(12), "Twelve");
        btree.insert(new IntegerValue(18), "Eighteen");
        btree.insert(new IntegerValue(1), "One");
        btree.insert(new IntegerValue(20), "Twenty");
        
        System.out.println("B-Tree size: " + btree.size());
        
        // Test range query
        List<String> rangeResults = btree.rangeQuery(new IntegerValue(5), new IntegerValue(15));
        System.out.println("Range query [5-15]: " + rangeResults);
        
        // Test individual searches
        System.out.println("Search 7: " + btree.search(new IntegerValue(7)));
        System.out.println("Search 25 (not found): " + btree.search(new IntegerValue(25)));
    }
    
    private static void testHashIndex() {
        System.out.println("\n--- Hash Index Test ---");
        
        HashIndex<TypedValue, Integer> hash = new HashIndex<>();
        
        // Insert data
        hash.insert(new VarcharValue("apple"), 1);
        hash.insert(new VarcharValue("banana"), 2);
        hash.insert(new VarcharValue("cherry"), 3);
        hash.insert(new VarcharValue("date"), 4);
        hash.insert(new VarcharValue("elderberry"), 5);
        
        System.out.println("Hash index size: " + hash.size());
        
        // Test searches
        System.out.println("Search 'banana': " + hash.search(new VarcharValue("banana")));
        System.out.println("Search 'grape' (not found): " + hash.search(new VarcharValue("grape")));
        
        // Test contains
        System.out.println("Contains 'cherry': " + hash.containsKey(new VarcharValue("cherry")));
        System.out.println("Contains 'fig': " + hash.containsKey(new VarcharValue("fig")));
    }
    
    private static void compareIndexPerformance() {
        System.out.println("\n--- Index Performance Comparison ---");
        
        final int TEST_SIZE = 1000;
        
        // Setup indexes
        BTreeIndex<TypedValue, String> btree = new BTreeIndex<>();
        HashIndex<TypedValue, String> hash = new HashIndex<>();
        
        // Insert data and measure time
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < TEST_SIZE; i++) {
            btree.insert(new IntegerValue(i), "Value" + i);
        }
        long btreeInsertTime = System.currentTimeMillis() - startTime;
        
        startTime = System.currentTimeMillis();
        for (int i = 0; i < TEST_SIZE; i++) {
            hash.insert(new IntegerValue(i), "Value" + i);
        }
        long hashInsertTime = System.currentTimeMillis() - startTime;
        
        // Search performance
        startTime = System.currentTimeMillis();
        for (int i = 0; i < 100; i++) {
            btree.search(new IntegerValue(i * 10));
        }
        long btreeSearchTime = System.currentTimeMillis() - startTime;
        
        startTime = System.currentTimeMillis();
        for (int i = 0; i < 100; i++) {
            hash.search(new IntegerValue(i * 10));
        }
        long hashSearchTime = System.currentTimeMillis() - startTime;
        
        System.out.println("Performance results for " + TEST_SIZE + " insertions:");
        System.out.println("  B-Tree insert time: " + btreeInsertTime + "ms");
        System.out.println("  Hash insert time: " + hashInsertTime + "ms");
        System.out.println("  B-Tree search time (100 searches): " + btreeSearchTime + "ms");
        System.out.println("  Hash search time (100 searches): " + hashSearchTime + "ms");
        
        // Range query advantage of B-Tree
        startTime = System.currentTimeMillis();
        List<String> rangeResults = btree.rangeQuery(new IntegerValue(100), new IntegerValue(200));
        long rangeQueryTime = System.currentTimeMillis() - startTime;
        
        System.out.println("  B-Tree range query [100-200]: " + rangeResults.size() + " results in " + rangeQueryTime + "ms");
        System.out.println("  (Hash index cannot perform efficient range queries)");
    }
}
