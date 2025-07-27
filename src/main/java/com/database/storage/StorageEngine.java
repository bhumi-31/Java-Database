package com.database.storage;

import com.database.core.Schema;
import com.database.core.Table;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Main storage engine for managing tables and memory.
 */
public class StorageEngine {
    private final Map<String, Table> tables;
    private final MemoryManager memoryManager;

    public StorageEngine() {
        this.tables = new ConcurrentHashMap<>();
        this.memoryManager = new MemoryManager();
    }

    public StorageEngine(long maxMemory) {
        this.tables = new ConcurrentHashMap<>();
        this.memoryManager = new MemoryManager(maxMemory);
    }

    /**
     * Creates a new table with the specified name and schema.
     * @param name the table name
     * @param schema the table schema
     * @return the created table
     * @throws IllegalArgumentException if table already exists or parameters are invalid
     */
    public Table createTable(String name, Schema schema) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Table name cannot be null or empty");
        }
        if (schema == null) {
            throw new IllegalArgumentException("Schema cannot be null");
        }

        String tableName = name.trim().toLowerCase();
        
        if (tables.containsKey(tableName)) {
            throw new IllegalArgumentException("Table already exists: " + name);
        }

        // Estimate memory needed for table metadata
        int estimatedSize = estimateTableMetadataSize(schema);
        memoryManager.allocate(estimatedSize);

        Table table = new Table(name, schema);
        tables.put(tableName, table);
        
        return table;
    }

    /**
     * Retrieves a table by name.
     * @param name the table name (case-insensitive)
     * @return the table, or null if not found
     */
    public Table getTable(String name) {
        if (name == null) {
            return null;
        }
        return tables.get(name.trim().toLowerCase());
    }

    /**
     * Drops (deletes) a table.
     * @param name the table name
     * @return true if table was dropped, false if not found
     */
    public boolean dropTable(String name) {
        if (name == null) {
            return false;
        }

        String tableName = name.trim().toLowerCase();
        Table table = tables.remove(tableName);
        
        if (table != null) {
            // Estimate and deallocate memory
            int estimatedSize = estimateTableSize(table);
            memoryManager.deallocate(estimatedSize);
            return true;
        }
        
        return false;
    }

    /**
     * Checks if a table exists.
     * @param name the table name
     * @return true if table exists, false otherwise
     */
    public boolean tableExists(String name) {
        if (name == null) {
            return false;
        }
        return tables.containsKey(name.trim().toLowerCase());
    }

    /**
     * Returns the names of all tables.
     */
    public Set<String> getTableNames() {
        return tables.keySet();
    }

    /**
     * Returns the number of tables.
     */
    public int getTableCount() {
        return tables.size();
    }

    /**
     * Returns the memory manager.
     */
    public MemoryManager getMemoryManager() {
        return memoryManager;
    }

    /**
     * Estimates the memory size needed for table metadata.
     */
    private int estimateTableMetadataSize(Schema schema) {
        // Rough estimation: base size + columns
        int baseSize = 1024; // Base table overhead
        int columnSize = schema.getColumnCount() * 256; // Estimated per column
        return baseSize + columnSize;
    }

    /**
     * Estimates the total memory size of a table including data.
     */
    private int estimateTableSize(Table table) {
        int metadataSize = estimateTableMetadataSize(table.getSchema());
        int dataSize = table.getRowCount() * 512; // Estimated per row
        return metadataSize + dataSize;
    }

    /**
     * Returns memory statistics for all tables.
     */
    public String getMemoryStats() {
        StringBuilder stats = new StringBuilder();
        stats.append("Storage Engine Memory Statistics:\n");
        stats.append(memoryManager.toString()).append("\n");
        stats.append("Tables: ").append(tables.size()).append("\n");
        
        for (Map.Entry<String, Table> entry : tables.entrySet()) {
            Table table = entry.getValue();
            stats.append("  ").append(entry.getKey())
                 .append(": ").append(table.getRowCount()).append(" rows\n");
        }
        
        return stats.toString();
    }

    @Override
    public String toString() {
        return "StorageEngine{" +
                "tables=" + tables.size() +
                ", memoryUsage=" + String.format("%.2f%%", memoryManager.getMemoryUsage() * 100) +
                '}';
    }
}
