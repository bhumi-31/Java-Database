package com.database.persistence;

import com.database.core.Database;
import com.database.core.Table;
import java.io.*;

/**
 * Basic serialization manager for persisting database objects.
 * This is a simple implementation for Phase 1 - will be enhanced in Phase 5.
 */
public class BasicSerializer {

    /**
     * Saves a table to a file using Java serialization.
     */
    public void saveTable(Table table, String filename) throws IOException {
        if (table == null) {
            throw new IllegalArgumentException("Table cannot be null");
        }
        if (filename == null || filename.trim().isEmpty()) {
            throw new IllegalArgumentException("Filename cannot be null or empty");
        }

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename))) {
            oos.writeObject(table);
        }
    }

    /**
     * Loads a table from a file using Java serialization.
     */
    public Table loadTable(String filename) throws IOException, ClassNotFoundException {
        if (filename == null || filename.trim().isEmpty()) {
            throw new IllegalArgumentException("Filename cannot be null or empty");
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filename))) {
            return (Table) ois.readObject();
        }
    }

    /**
     * Saves a database to a file using Java serialization.
     * Note: This saves the storage engine state, not the Database object itself.
     */
    public void saveDatabase(Database db, String filename) throws IOException {
        if (db == null) {
            throw new IllegalArgumentException("Database cannot be null");
        }
        if (filename == null || filename.trim().isEmpty()) {
            throw new IllegalArgumentException("Filename cannot be null or empty");
        }

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename))) {
            oos.writeObject(db.getStorageEngine());
        }
    }

    /**
     * Loads a database from a file using Java serialization.
     * Note: This creates a new Database with the loaded storage engine.
     */
    public Database loadDatabase(String filename) throws IOException, ClassNotFoundException {
        if (filename == null || filename.trim().isEmpty()) {
            throw new IllegalArgumentException("Filename cannot be null or empty");
        }

        // For Phase 1, we'll implement a basic approach
        // In Phase 5, this will be replaced with proper WAL-based persistence
        throw new UnsupportedOperationException("Database loading will be implemented in Phase 5");
    }

    /**
     * Checks if a file exists and is readable.
     */
    public boolean fileExists(String filename) {
        if (filename == null) {
            return false;
        }
        File file = new File(filename);
        return file.exists() && file.isFile() && file.canRead();
    }

    /**
     * Deletes a file if it exists.
     */
    public boolean deleteFile(String filename) {
        if (filename == null) {
            return false;
        }
        File file = new File(filename);
        return file.exists() && file.delete();
    }

    /**
     * Gets the size of a file in bytes.
     */
    public long getFileSize(String filename) {
        if (filename == null) {
            return -1;
        }
        File file = new File(filename);
        return file.exists() ? file.length() : -1;
    }
}
