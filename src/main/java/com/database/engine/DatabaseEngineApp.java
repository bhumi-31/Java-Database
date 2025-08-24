package com.database.engine;

import java.util.*;

public class DatabaseEngineApp {
    private Database database;
    private Scanner scanner;
    private QueryProcessor queryProcessor;
    private AggregationEngine aggregationEngine;
    private BenchmarkManager benchmarkManager;
    
    public DatabaseEngineApp() {
        this.database = new Database("MyDB");
        this.scanner = new Scanner(System.in);
        this.queryProcessor = new QueryProcessor(database);
        this.aggregationEngine = new AggregationEngine(database);
        this.benchmarkManager = new BenchmarkManager(database, queryProcessor, aggregationEngine);
    }
    
    public void start() {
        System.out.println("=== Custom Database Engine ===");
        System.out.println("Type 'help' for commands or 'exit' to quit");
        
        while (true) {
            System.out.print("db> ");
            String input = scanner.nextLine().trim();
            
            if (input.equalsIgnoreCase("exit")) {
                break;
            }
            
            processCommand(input);
        }
        
        scanner.close();
        System.out.println("Database engine stopped.");
    }
    
    private void processCommand(String input) {
        String[] parts = input.split("\\s+");
        if (parts.length == 0) return;
        
        String command = parts[0].toUpperCase();
        
        try {
            switch (command) {
                case "HELP":
                    showHelp();
                    break;
                case "CREATE":
                    handleCreate(parts);
                    break;
                case "INSERT":
                    handleInsert(parts);
                    break;
                case "SELECT":
                    handleSelect(input);
                    break;
                case "UPDATE":
                    handleUpdate(input);
                    break;
                case "DELETE":
                    handleDelete(input);
                    break;
                case "JOIN":
                    handleJoin(input);
                    break;
                case "SHOW":
                    handleShow(parts);
                    break;
                case "TEST":
                    runTests();
                    break;
                case "BENCHMARK":
                    handleBenchmark(input);
                    break;
                case "STATS":
                    showPerformanceStats();
                    break;
                case "HISTORY":
                    showBenchmarkHistory();
                    break;
                default:
                    System.out.println("Unknown command: " + command);
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
    
    private void showHelp() {
        System.out.println("\nAvailable Commands:");
        System.out.println("CREATE TABLE tablename");
        System.out.println("INSERT INTO tablename field1=value1 field2=value2");
        System.out.println("SELECT * FROM tablename");
        System.out.println("SELECT * FROM tablename WHERE field=value");
        System.out.println("SELECT COUNT(*) FROM tablename");
        System.out.println("SELECT AVG(field) FROM tablename");
        System.out.println("UPDATE tablename SET field=value WHERE field=value");
        System.out.println("DELETE FROM tablename WHERE field=value");
        System.out.println("JOIN table1 table2 ON field1=field2");
        System.out.println("SHOW TABLES");
        System.out.println("TEST - Run sample tests");
        System.out.println("BENCHMARK - Run benchmarks");
        System.out.println("STATS - Show performance stats");
        System.out.println("EXIT\n");
    }
    
    private void handleCreate(String[] parts) {
        if (parts.length >= 3 && parts[1].equalsIgnoreCase("TABLE")) {
            String tableName = parts[2];
            database.createTable(tableName);
            System.out.println("Table '" + tableName + "' created successfully.");
        } else {
            System.out.println("Usage: CREATE TABLE tablename");
        }
    }
    
    private void handleInsert(String[] parts) {
        if (parts.length >= 4 && parts[1].equalsIgnoreCase("INTO")) {
            String tableName = parts[2];
            Table table = database.getTable(tableName);
            
            Map<String, Object> data = new HashMap<>();
            
            String fieldsString = String.join(" ", Arrays.copyOfRange(parts, 3, parts.length));
            String[] fieldPairs = fieldsString.split("\\s+(?=\\w+=)");
            
            for (String pair : fieldPairs) {
                if (pair.contains("=")) {
                    String[] fieldValue = pair.split("=", 2);
                    if (fieldValue.length == 2) {
                        String field = fieldValue[0].trim();
                        String value = fieldValue[1].trim().replaceAll("\"", "");
                        
                        try {
                            data.put(field, Integer.parseInt(value));
                        } catch (NumberFormatException e) {
                            data.put(field, value);
                        }
                    }
                }
            }
            
            int id = table.insert(data);
            System.out.println("Record inserted with ID: " + id);
            System.out.println("Data inserted: " + data);
        } else {
            System.out.println("Usage: INSERT INTO tablename field1=value1 field2=value2");
        }
    }
    
    private void handleSelect(String input) {
        try {
            // Handle aggregation queries first
            if (AggregationEngine.isAggregationQuery(input)) {
                List<Map<String, Object>> results = aggregationEngine.processAggregation(input);
                aggregationEngine.displayAggregationResults(results);
                return;
            }
            
            String[] parts = input.split("\\s+");
            if (parts.length >= 4 && parts[1].equals("*") && parts[2].equalsIgnoreCase("FROM")) {
                String tableName = parts[3];
                Table table = database.getTable(tableName);
                
                List<Record> records;
                
                if (parts.length >= 6 && parts[4].equalsIgnoreCase("WHERE")) {
                    String[] condition = parts[5].split("=");
                    if (condition.length == 2) {
                        String field = condition[0];
                        String value = condition[1];
                        
                        try {
                            records = table.select(field, Integer.parseInt(value));
                        } catch (NumberFormatException e) {
                            records = table.select(field, value);
                        }
                    } else {
                        records = table.selectAll();
                    }
                } else {
                    records = table.selectAll();
                }
                
                displayRecords(records);
            } else {
                System.out.println("Usage: SELECT * FROM tablename [WHERE field=value]");
            }
        } catch (Exception e) {
            System.out.println("Error in SELECT: " + e.getMessage());
        }
    }
    
    // ========== NEW UPDATE METHOD ==========
    private void handleUpdate(String input) {
        try {
            // UPDATE tablename SET field=value WHERE condition
            String[] parts = input.split("\\s+");
            if (parts.length < 4 || !parts[2].equalsIgnoreCase("SET")) {
                System.out.println("Usage: UPDATE tablename SET field=value WHERE field=value");
                return;
            }
            
            String tableName = parts[1];
            Table table = database.getTable(tableName);
            if (table == null) {
                System.out.println("Table '" + tableName + "' does not exist.");
                return;
            }
            
            // Find SET clause
            int setIndex = -1;
            int whereIndex = -1;
            for (int i = 0; i < parts.length; i++) {
                if (parts[i].equalsIgnoreCase("SET")) {
                    setIndex = i;
                }
                if (parts[i].equalsIgnoreCase("WHERE")) {
                    whereIndex = i;
                    break;
                }
            }
            
            if (setIndex == -1 || setIndex + 1 >= parts.length) {
                System.out.println("Invalid UPDATE syntax. Missing SET clause.");
                return;
            }
            
            // Parse SET clause
            String setClause = parts[setIndex + 1];
            String[] setParts = setClause.split("=");
            if (setParts.length != 2) {
                System.out.println("Invalid SET clause. Use: field=value");
                return;
            }
            
            String fieldName = setParts[0].trim();
            String newValue = setParts[1].trim().replace("\"", "").replace("'", "");
            
            // Parse WHERE clause
            String whereCondition = "";
            if (whereIndex != -1 && whereIndex + 1 < parts.length) {
                String[] whereClauseParts = Arrays.copyOfRange(parts, whereIndex + 1, parts.length);
                whereCondition = String.join(" ", whereClauseParts);
            }
            
            long startTime = System.currentTimeMillis();
            int updatedCount = table.updateRecords(fieldName, newValue, whereCondition);
            long endTime = System.currentTimeMillis();
            
            System.out.println("Updated " + updatedCount + " record(s).");
            System.out.println("Execution time: " + (endTime - startTime) + "ms");
            
        } catch (Exception e) {
            System.out.println("Error executing UPDATE: " + e.getMessage());
        }
    }
    
    // ========== NEW DELETE METHOD ==========
    private void handleDelete(String input) {
        try {
            // DELETE FROM tablename WHERE condition
            String[] parts = input.split("\\s+");
            if (parts.length < 3 || !parts[1].equalsIgnoreCase("FROM")) {
                System.out.println("Usage: DELETE FROM tablename WHERE field=value");
                return;
            }
            
            String tableName = parts[2];
            Table table = database.getTable(tableName);
            if (table == null) {
                System.out.println("Table '" + tableName + "' does not exist.");
                return;
            }
            
            // Find WHERE clause
            int whereIndex = -1;
            for (int i = 0; i < parts.length; i++) {
                if (parts[i].equalsIgnoreCase("WHERE")) {
                    whereIndex = i;
                    break;
                }
            }
            
            String whereCondition = "";
            if (whereIndex != -1 && whereIndex + 1 < parts.length) {
                String[] whereClauseParts = Arrays.copyOfRange(parts, whereIndex + 1, parts.length);
                whereCondition = String.join(" ", whereClauseParts);
            }
            
            // Safety check for DELETE without WHERE
            if (whereCondition.isEmpty()) {
                System.out.print("WARNING: This will delete ALL records from table '" + tableName + "'. Continue? (y/n): ");
                String confirmation = scanner.nextLine().trim().toLowerCase();
                if (!confirmation.equals("y") && !confirmation.equals("yes")) {
                    System.out.println("Delete operation cancelled.");
                    return;
                }
            }
            
            long startTime = System.currentTimeMillis();
            int deletedCount = table.deleteRecords(whereCondition);
            long endTime = System.currentTimeMillis();
            
            System.out.println("Deleted " + deletedCount + " record(s).");
            System.out.println("Execution time: " + (endTime - startTime) + "ms");
            
        } catch (Exception e) {
            System.out.println("Error executing DELETE: " + e.getMessage());
        }
    }
    
    private void handleJoin(String input) {
        try {
            // JOIN table1 table2 ON field1=field2
            String[] parts = input.split("\\s+");
            if (parts.length >= 5 && parts[3].equalsIgnoreCase("ON")) {
                String table1 = parts[1];
                String table2 = parts[2];
                String condition = parts[4]; // This is "course=code"
                
                // Parse the condition to get separate field names
                String[] conditionParts = condition.split("=");
                if (conditionParts.length != 2) {
                    System.out.println("Invalid JOIN condition. Use format: field1=field2");
                    return;
                }
                
                String field1 = conditionParts[0].trim(); // "course"
                String field2 = conditionParts[1].trim(); // "code"
                
                long startTime = System.currentTimeMillis();
                
                // Use the performInnerJoin method directly with different field names
                Table t1 = database.getTable(table1);
                Table t2 = database.getTable(table2);
                List<Record> results = queryProcessor.performInnerJoin(t1, t2, field1, field2);
                
                long endTime = System.currentTimeMillis();
                
                System.out.println("\nJOIN Results:");
                for (Record record : results) {
                    System.out.println(record);
                }
                System.out.println("Total joined records: " + results.size());
                System.out.println("Query execution time: " + (endTime - startTime) + "ms\n");
                
            } else {
                System.out.println("Usage: JOIN table1 table2 ON field1=field2");
            }
        } catch (Exception e) {
            System.out.println("Error executing JOIN: " + e.getMessage());
            e.printStackTrace(); // Add this for debugging
        }
    }
    
    private void handleShow(String[] parts) {
        if (parts.length >= 2 && parts[1].equalsIgnoreCase("TABLES")) {
            String[] tables = database.listTables();
            System.out.println("Tables in database:");
            if (tables.length == 0) {
                System.out.println("No tables found.");
            } else {
                for (String table : tables) {
                    System.out.println("- " + table);
                }
            }
        }
    }
    
    private void displayRecords(List<Record> records) {
        if (records.isEmpty()) {
            System.out.println("No records found.");
            return;
        }
        
        System.out.println("\nResults:");
        for (Record record : records) {
            System.out.println(record);
        }
        System.out.println("Total records: " + records.size() + "\n");
    }
    
    private void handleBenchmark(String input) {
        try {
            System.out.println("\n=== Running Benchmarks ===");
            benchmarkManager.runComprehensiveBenchmarks();
        } catch (Exception e) {
            System.out.println("Benchmark error: " + e.getMessage());
        }
    }
    
    private void showPerformanceStats() {
        try {
            System.out.println("\n=== Performance Statistics ===");
            Map<String, Object> stats = benchmarkManager.getPerformanceStats();
            for (Map.Entry<String, Object> entry : stats.entrySet()) {
                System.out.println(entry.getKey() + ": " + entry.getValue());
            }
            System.out.println();
        } catch (Exception e) {
            System.out.println("Stats error: " + e.getMessage());
        }
    }
    
    private void showBenchmarkHistory() {
        try {
            System.out.println("\n=== Benchmark History ===");
            List<BenchmarkManager.QueryBenchmark> history = benchmarkManager.getBenchmarkHistory();
            if (history.isEmpty()) {
                System.out.println("No benchmarks run yet.");
            } else {
                for (int i = 0; i < Math.min(10, history.size()); i++) {
                    BenchmarkManager.QueryBenchmark benchmark = history.get(i);
                    System.out.println((i + 1) + ". " + benchmark.toString());
                }
                System.out.println("Total benchmarks: " + history.size());
            }
            System.out.println();
        } catch (Exception e) {
            System.out.println("History error: " + e.getMessage());
        }
    }
    
    private void runTests() {
        System.out.println("\n=== Running Tests ===");
        
        try {
            // Test 1: Basic operations
            System.out.println("Test 1: Basic CRUD Operations");
            database.createTable("test_students");
            Table testTable = database.getTable("test_students");
            
            Map<String, Object> student1 = new HashMap<>();
            student1.put("name", "TestStudent");
            student1.put("age", 22);
            student1.put("course", "CS");
            testTable.insert(student1);
            
            List<Record> allStudents = testTable.selectAll();
            System.out.println("✓ Basic functionality works - " + allStudents.size() + " records");
            
            // Test 2: UPDATE operation
            System.out.println("\nTest 2: UPDATE Operation");
            try {
                int updated = testTable.updateRecords("name", "UpdatedStudent", "age=22");
                System.out.println("✓ UPDATE function works - " + updated + " records updated");
            } catch (Exception e) {
                System.out.println("❌ UPDATE function failed: " + e.getMessage());
            }
            
            // Test 3: DELETE operation
            System.out.println("\nTest 3: DELETE Operation");
            try {
                // Insert another record first
                Map<String, Object> student2 = new HashMap<>();
                student2.put("name", "DeleteMe");
                student2.put("age", 25);
                student2.put("course", "Math");
                testTable.insert(student2);
                
                int deleted = testTable.deleteRecords("course=Math");
                System.out.println("✓ DELETE function works - " + deleted + " records deleted");
            } catch (Exception e) {
                System.out.println("❌ DELETE function failed: " + e.getMessage());
            }
            
            // Test 4: Aggregation
            System.out.println("\nTest 4: Aggregation Functions");
            try {
                List<Map<String, Object>> countResult = aggregationEngine.processAggregation("SELECT COUNT(*) FROM test_students");
                System.out.println("✓ COUNT function works");
            } catch (Exception e) {
                System.out.println("❌ COUNT function failed: " + e.getMessage());
            }
            
            // Test 5: JOIN with different field names
            System.out.println("\nTest 5: JOIN Operations");
            try {
                database.createTable("test_courses");
                Table coursesTable = database.getTable("test_courses");
                Map<String, Object> course1 = new HashMap<>();
                course1.put("code", "CS");
                course1.put("name", "Computer Science");
                coursesTable.insert(course1);
                
                // Test JOIN with different field names
                Table t1 = database.getTable("test_students");
                Table t2 = database.getTable("test_courses");
                List<Record> joinResults = queryProcessor.performInnerJoin(t1, t2, "course", "code");
                System.out.println("✓ JOIN function works - " + joinResults.size() + " joined records");
            } catch (Exception e) {
                System.out.println("❌ JOIN function failed: " + e.getMessage());
                e.printStackTrace();
            }
            
            System.out.println("\n=== Tests Completed ===\n");
            
        } catch (Exception e) {
            System.out.println("❌ Test failed: " + e.getMessage());
        }
    }
    
    public static void main(String[] args) {
        DatabaseEngineApp app = new DatabaseEngineApp();
        app.start();
    }
}