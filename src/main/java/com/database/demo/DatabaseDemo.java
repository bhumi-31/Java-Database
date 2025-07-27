package com.database.demo;

import com.database.core.*;
import com.database.types.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Demo application showcasing the database engine functionality.
 */
public class DatabaseDemo {

    public static void main(String[] args) {
        System.out.println("=== Custom In-Memory Database Engine Demo ===\n");

        try {
            // Create database instance
            Database db = new Database();
            System.out.println("✓ Database created successfully");

            // Create table schema
            List<Column> columns = new ArrayList<>();
            columns.add(new Column("id", DataType.INTEGER, false, true));
            columns.add(new Column("name", DataType.VARCHAR, true, false, 50));
            columns.add(new Column("age", DataType.INTEGER, true, false));
            columns.add(new Column("salary", DataType.DOUBLE, true, false));
            columns.add(new Column("active", DataType.BOOLEAN, true, false));

            Schema schema = new Schema(columns);
            System.out.println("✓ Schema created with " + schema.getColumnCount() + " columns");

            // Create table
            Table employeeTable = db.createTable("employees", schema);
            System.out.println("✓ Table 'employees' created");

            // Insert sample data
            System.out.println("\n--- Inserting Sample Data ---");
            insertSampleData(employeeTable);

            // Display all data
            System.out.println("\n--- All Employees ---");
            displayAllRows(employeeTable);

            // Test primary key lookup
            System.out.println("\n--- Primary Key Lookup (ID=2) ---");
            Row employee = employeeTable.findRowByPrimaryKey(2);
            if (employee != null) {
                displayRow(employee);
            }

            // Test updates
            System.out.println("\n--- Updating Employee (ID=1) ---");
            testUpdate(employeeTable);

            // Test deletion
            System.out.println("\n--- Deleting Employee (ID=3) ---");
            testDelete(employeeTable);

            // Display final state
            System.out.println("\n--- Final State ---");
            displayAllRows(employeeTable);

            // Show statistics
            System.out.println("\n--- Database Statistics ---");
            System.out.println(db.getStats());

            // Test basic SQL-like commands
            System.out.println("\n--- Testing Basic SQL Commands ---");
            testBasicSQLCommands(db);

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void insertSampleData(Table table) {
        // Employee 1
        Row emp1 = new Row();
        emp1.setValue("id", new IntegerValue(1));
        emp1.setValue("name", new VarcharValue("Alice Johnson", 50));
        emp1.setValue("age", new IntegerValue(28));
        emp1.setValue("salary", new DoubleValue(65000.0));
        emp1.setValue("active", new BooleanValue(true));
        table.insert(emp1);
        System.out.println("  ✓ Inserted: Alice Johnson");

        // Employee 2
        Row emp2 = new Row();
        emp2.setValue("id", new IntegerValue(2));
        emp2.setValue("name", new VarcharValue("Bob Smith", 50));
        emp2.setValue("age", new IntegerValue(35));
        emp2.setValue("salary", new DoubleValue(75000.0));
        emp2.setValue("active", new BooleanValue(true));
        table.insert(emp2);
        System.out.println("  ✓ Inserted: Bob Smith");

        // Employee 3
        Row emp3 = new Row();
        emp3.setValue("id", new IntegerValue(3));
        emp3.setValue("name", new VarcharValue("Carol Davis", 50));
        emp3.setValue("age", new IntegerValue(42));
        emp3.setValue("salary", new DoubleValue(85000.0));
        emp3.setValue("active", new BooleanValue(false));
        table.insert(emp3);
        System.out.println("  ✓ Inserted: Carol Davis");

        // Employee 4
        Row emp4 = new Row();
        emp4.setValue("id", new IntegerValue(4));
        emp4.setValue("name", new VarcharValue("David Wilson", 50));
        emp4.setValue("age", new IntegerValue(31));
        emp4.setValue("salary", new DoubleValue(70000.0));
        emp4.setValue("active", new BooleanValue(true));
        table.insert(emp4);
        System.out.println("  ✓ Inserted: David Wilson");
    }

    private static void displayAllRows(Table table) {
        List<Row> rows = table.selectAll();
        System.out.println("Total rows: " + rows.size());
        System.out.println("ID | Name         | Age | Salary   | Active");
        System.out.println("---|--------------|-----|----------|-------");
        
        for (Row row : rows) {
            displayRow(row);
        }
    }

    private static void displayRow(Row row) {
        IntegerValue id = (IntegerValue) row.getValue("id");
        VarcharValue name = (VarcharValue) row.getValue("name");
        IntegerValue age = (IntegerValue) row.getValue("age");
        DoubleValue salary = (DoubleValue) row.getValue("salary");
        BooleanValue active = (BooleanValue) row.getValue("active");

        System.out.printf("%2d | %-12s | %3d | %8.0f | %s%n",
            id.intValue(),
            name.stringValue(),
            age.intValue(),
            salary.doubleValue(),
            active.booleanValue() ? "Yes" : "No");
    }

    private static void testUpdate(Table table) {
        Row employee = table.findRowByPrimaryKey(1);
        if (employee != null) {
            java.util.Map<String, TypedValue> updates = new java.util.HashMap<>();
            updates.put("salary", new DoubleValue(70000.0));
            updates.put("age", new IntegerValue(29));
            
            boolean success = table.update(employee.getRowId(), updates);
            if (success) {
                System.out.println("  ✓ Updated Alice's salary and age");
                displayRow(table.findRowByPrimaryKey(1));
            }
        }
    }

    private static void testDelete(Table table) {
        Row employee = table.findRowByPrimaryKey(3);
        if (employee != null) {
            boolean success = table.delete(employee.getRowId());
            if (success) {
                System.out.println("  ✓ Deleted Carol Davis (ID=3)");
            }
        }
    }

    private static void testBasicSQLCommands(Database db) {
        System.out.println("\nTesting CREATE TABLE:");
        String result = db.execute("CREATE TABLE test_table (id INTEGER PRIMARY KEY, name VARCHAR(50))");
        System.out.println("  " + result);

        System.out.println("\nTesting INSERT:");
        result = db.execute("INSERT INTO test_table (id, name) VALUES (1, 'Test User')");
        System.out.println("  " + result);

        System.out.println("\nTesting SELECT:");
        result = db.execute("SELECT * FROM test_table");
        System.out.println("  " + result);

        System.out.println("\nTesting DROP TABLE:");
        result = db.execute("DROP TABLE test_table");
        System.out.println("  " + result);
    }
}
