package com.database.engine;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Database {
    private String databaseName;
    private String dataDirectory;
    private Map<String, Table> tables;
    
    public Database(String databaseName) {
        this.databaseName = databaseName;
        this.dataDirectory = "data/" + databaseName;
        this.tables = new HashMap<>();
        
        // Create data directory if it doesn't exist
        File dir = new File(dataDirectory);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }
    
    // Create new table
    public Table createTable(String tableName) {
        if (tables.containsKey(tableName)) {
            System.out.println("Warning: Table " + tableName + " already exists, returning existing table");
            return tables.get(tableName);
        }
        
        Table table = new Table(tableName, dataDirectory);
        tables.put(tableName, table);
        return table;
    }
    
    // Get existing table
    public Table getTable(String tableName) {
        // First check if table is already loaded in memory
        if (tables.containsKey(tableName)) {
            return tables.get(tableName);
        }
        
        // Check if table file exists on disk
        File tableFile = new File(dataDirectory + "/" + tableName + ".db");
        if (tableFile.exists()) {
            // Load existing table from disk
            Table table = new Table(tableName, dataDirectory);
            tables.put(tableName, table);
            return table;
        }
        
        // Table doesn't exist
        throw new RuntimeException("Table '" + tableName + "' does not exist. Create it first using CREATE TABLE " + tableName);
    }
    
    // Check if table exists
    public boolean tableExists(String tableName) {
        if (tables.containsKey(tableName)) {
            return true;
        }
        
        File tableFile = new File(dataDirectory + "/" + tableName + ".db");
        return tableFile.exists();
    }
    
    // List all tables
    public String[] listTables() {
        Set<String> allTables = new HashSet<>(tables.keySet());
        
        // Also check disk files
        File dataDir = new File(dataDirectory);
        if (dataDir.exists()) {
            File[] files = dataDir.listFiles((dir, name) -> name.endsWith(".db"));
            if (files != null) {
                for (File file : files) {
                    String tableName = file.getName().replace(".db", "");
                    allTables.add(tableName);
                }
            }
        }
        
        return allTables.toArray(new String[0]);
    }
    
    // Drop table
    public boolean dropTable(String tableName) {
        if (tables.containsKey(tableName)) {
            tables.remove(tableName);
        }
        
        // Delete file
        File file = new File(dataDirectory + "/" + tableName + ".db");
        return file.delete();
    }
    
    public String getDatabaseName() {
        return databaseName;
    }
    
    public String getDataDirectory() {
        return dataDirectory;
    }
}