package com.database.core;

import com.database.storage.StorageEngine;
import com.database.types.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Main database engine class that coordinates all database operations.
 */
public class Database {
    private final StorageEngine storageEngine;

    public Database() {
        this.storageEngine = new StorageEngine();
    }

    public Database(long maxMemory) {
        this.storageEngine = new StorageEngine(maxMemory);
    }

    /**
     * Executes a SQL-like command. Currently supports basic table operations.
     * In later phases, this will be replaced with a full SQL parser.
     */
    public String execute(String command) {
        if (command == null || command.trim().isEmpty()) {
            return "Error: Empty command";
        }

        command = command.trim();
        
        try {
            // Very basic command parsing for Phase 1
            if (command.toUpperCase().startsWith("CREATE TABLE")) {
                return executeCreateTable(command);
            } else if (command.toUpperCase().startsWith("INSERT INTO")) {
                return executeInsert(command);
            } else if (command.toUpperCase().startsWith("SELECT")) {
                return executeSelect(command);
            } else if (command.toUpperCase().startsWith("DROP TABLE")) {
                return executeDropTable(command);
            } else {
                return "Error: Unsupported command: " + command;
            }
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    /**
     * Creates a table programmatically.
     */
    public Table createTable(String name, Schema schema) {
        return storageEngine.createTable(name, schema);
    }

    /**
     * Gets a table by name.
     */
    public Table getTable(String name) {
        return storageEngine.getTable(name);
    }

    /**
     * Drops a table by name.
     */
    public boolean dropTable(String name) {
        return storageEngine.dropTable(name);
    }

    /**
     * Returns storage engine for advanced operations.
     */
    public StorageEngine getStorageEngine() {
        return storageEngine;
    }

    // Basic SQL command implementations for Phase 1

    private String executeCreateTable(String command) {
        // Very basic parsing: CREATE TABLE users (id INTEGER PRIMARY KEY, name VARCHAR(50), age INTEGER)
        // This is a placeholder - will be replaced with proper SQL parser in Phase 3
        
        String[] parts = command.split("\\s+", 3);
        if (parts.length < 3) {
            return "Error: Invalid CREATE TABLE syntax";
        }
        
        String tableName = parts[2].split("\\s*\\(")[0].trim();
        
        // For now, create a simple test table
        List<Column> columns = new ArrayList<>();
        columns.add(new Column("id", DataType.INTEGER, false, true));
        columns.add(new Column("name", DataType.VARCHAR, true, false, 50));
        columns.add(new Column("age", DataType.INTEGER, true, false));
        
        Schema schema = new Schema(columns);
        storageEngine.createTable(tableName, schema);
        
        return "Table '" + tableName + "' created successfully";
    }

    private String executeInsert(String command) {
        // Very basic parsing: INSERT INTO users (id, name, age) VALUES (1, 'Alice', 25)
        // This is a placeholder - will be replaced with proper SQL parser in Phase 3
        
        try {
            // Extract table name
            String[] parts = command.split("\\s+");
            String tableName = parts[2];
            
            Table table = storageEngine.getTable(tableName);
            if (table == null) {
                return "Error: Table '" + tableName + "' does not exist";
            }
            
            // For now, create a simple test row
            Row row = new Row();
            row.setValue("id", new IntegerValue(1));
            row.setValue("name", new VarcharValue("TestUser", 50));
            row.setValue("age", new IntegerValue(25));
            
            table.insert(row);
            return "1 row inserted into '" + tableName + "'";
            
        } catch (Exception e) {
            return "Error executing insert: " + e.getMessage();
        }
    }

    private String executeSelect(String command) {
        // Very basic parsing: SELECT * FROM users
        // This is a placeholder - will be replaced with proper SQL parser in Phase 3
        
        try {
            String[] parts = command.split("\\s+");
            if (parts.length < 4 || !parts[2].equalsIgnoreCase("FROM")) {
                return "Error: Invalid SELECT syntax";
            }
            
            String tableName = parts[3];
            Table table = storageEngine.getTable(tableName);
            if (table == null) {
                return "Error: Table '" + tableName + "' does not exist";
            }
            
            List<Row> rows = table.selectAll();
            StringBuilder result = new StringBuilder();
            result.append("Results from '").append(tableName).append("':\n");
            
            if (rows.isEmpty()) {
                result.append("No rows found.");
            } else {
                for (Row row : rows) {
                    result.append(row.toString()).append("\n");
                }
            }
            
            return result.toString();
            
        } catch (Exception e) {
            return "Error executing select: " + e.getMessage();
        }
    }

    private String executeDropTable(String command) {
        String[] parts = command.split("\\s+");
        if (parts.length < 3) {
            return "Error: Invalid DROP TABLE syntax";
        }
        
        String tableName = parts[2];
        boolean success = storageEngine.dropTable(tableName);
        
        if (success) {
            return "Table '" + tableName + "' dropped successfully";
        } else {
            return "Error: Table '" + tableName + "' does not exist";
        }
    }

    /**
     * Returns database statistics.
     */
    public String getStats() {
        return storageEngine.getMemoryStats();
    }

    @Override
    public String toString() {
        return "Database{" +
                "tables=" + storageEngine.getTableCount() +
                ", " + storageEngine.getMemoryManager().toString() +
                '}';
    }
}
