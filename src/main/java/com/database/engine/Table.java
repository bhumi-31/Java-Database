package com.database.engine;

import java.io.*;
import java.util.*;

public class Table {
    private String tableName;
    private List<Record> records;
    private int nextId;
    private String filePath;
    
    public Table(String tableName, String dataDir) {
        this.tableName = tableName;
        this.records = new ArrayList<>();
        this.nextId = 1;
        this.filePath = dataDir + "/" + tableName + ".db";
        loadFromFile();
    }
    
    // Insert new record
    public int insert(Map<String, Object> data) {
        Record record = new Record(nextId++);
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            record.setField(entry.getKey(), entry.getValue());
        }
        records.add(record);
        saveToFile();
        return record.getId();
    }
    
    // Select all records
    public List<Record> selectAll() {
        return new ArrayList<>(records);
    }
    
    // Select with condition (simple)
    public List<Record> select(String fieldName, Object value) {
        List<Record> result = new ArrayList<>();
        for (Record record : records) {
            if (Objects.equals(record.getField(fieldName), value)) {
                result.add(record);
            }
        }
        return result;
    }
    
    // Update record by ID (existing method)
    public boolean update(int id, Map<String, Object> newData) {
        for (Record record : records) {
            if (record.getId() == id) {
                for (Map.Entry<String, Object> entry : newData.entrySet()) {
                    record.setField(entry.getKey(), entry.getValue());
                }
                saveToFile();
                return true;
            }
        }
        return false;
    }
    
    // Delete record by ID (existing method)
    public boolean delete(int id) {
        records.removeIf(record -> record.getId() == id);
        saveToFile();
        return true;
    }
    
    // ========== NEW METHODS FOR SQL-LIKE UPDATE/DELETE ==========
    
    // UPDATE with WHERE condition
    public int updateRecords(String fieldName, String newValue, String whereCondition) {
        int updatedCount = 0;
        
        for (Record record : records) {
            if (whereCondition.isEmpty() || matchesCondition(record, whereCondition)) {
                // Convert string value to appropriate type
                Object convertedValue = convertValue(newValue);
                record.setField(fieldName, convertedValue);
                updatedCount++;
            }
        }
        
        if (updatedCount > 0) {
            saveToFile();
        }
        return updatedCount;
    }
    
    // DELETE with WHERE condition  
    public int deleteRecords(String whereCondition) {
        int deletedCount = 0;
        List<Record> recordsToRemove = new ArrayList<>();
        
        for (Record record : records) {
            if (whereCondition.isEmpty() || matchesCondition(record, whereCondition)) {
                recordsToRemove.add(record);
                deletedCount++;
            }
        }
        
        // Remove records
        records.removeAll(recordsToRemove);
        
        if (deletedCount > 0) {
            saveToFile();
        }
        return deletedCount;
    }
    
    // SELECT with WHERE condition (enhanced)
    public List<Record> selectWithCondition(String whereCondition) {
        List<Record> result = new ArrayList<>();
        
        for (Record record : records) {
            if (whereCondition.isEmpty() || matchesCondition(record, whereCondition)) {
                result.add(record);
            }
        }
        return result;
    }
    
    // Helper method to check if record matches WHERE condition
    private boolean matchesCondition(Record record, String condition) {
        if (condition == null || condition.trim().isEmpty()) {
            return true;
        }
        
        try {
            // Handle simple conditions: field=value, field>value, field<value
            String[] operators = {">=", "<=", "!=", "=", ">", "<"};
            
            for (String op : operators) {
                if (condition.contains(op)) {
                    String[] parts = condition.split(op, 2);
                    if (parts.length == 2) {
                        String fieldName = parts[0].trim();
                        String value = parts[1].trim().replace("\"", "").replace("'", "");
                        
                        Object fieldValue = record.getField(fieldName);
                        if (fieldValue == null) return false;
                        
                        return compareValues(fieldValue, value, op);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error parsing condition: " + condition);
        }
        
        return false;
    }
    
    // Helper method to compare values based on operator
    private boolean compareValues(Object fieldValue, String compareValue, String operator) {
        try {
            // Try numeric comparison first
            if (fieldValue instanceof Number || isNumeric(fieldValue.toString())) {
                double fieldNum = Double.parseDouble(fieldValue.toString());
                double compareNum = Double.parseDouble(compareValue);
                
                switch (operator) {
                    case "=": return fieldNum == compareNum;
                    case "!=": return fieldNum != compareNum;
                    case ">": return fieldNum > compareNum;
                    case "<": return fieldNum < compareNum;
                    case ">=": return fieldNum >= compareNum;
                    case "<=": return fieldNum <= compareNum;
                }
            }
        } catch (NumberFormatException e) {
            // Fall back to string comparison
        }
        
        // String comparison
        String fieldStr = fieldValue.toString();
        switch (operator) {
            case "=": return fieldStr.equals(compareValue);
            case "!=": return !fieldStr.equals(compareValue);
            case ">": return fieldStr.compareTo(compareValue) > 0;
            case "<": return fieldStr.compareTo(compareValue) < 0;
            case ">=": return fieldStr.compareTo(compareValue) >= 0;
            case "<=": return fieldStr.compareTo(compareValue) <= 0;
            default: return false;
        }
    }
    
    // Helper method to check if string is numeric
    private boolean isNumeric(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    // Helper method to convert string values to appropriate types
    private Object convertValue(String value) {
        if (value == null) return null;
        
        // Remove quotes
        value = value.replace("\"", "").replace("'", "");
        
        // Try to convert to number if possible
        if (isNumeric(value)) {
            try {
                if (value.contains(".")) {
                    return Double.parseDouble(value);
                } else {
                    return Integer.parseInt(value);
                }
            } catch (NumberFormatException e) {
                // Fall back to string
            }
        }
        
        return value;
    }
    
    // ========== EXISTING METHODS ==========
    
    // Save to file
    private void saveToFile() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath))) {
            oos.writeObject(records);
            oos.writeInt(nextId);
        } catch (IOException e) {
            System.err.println("Error saving table: " + e.getMessage());
        }
    }
    
    // Load from file
    @SuppressWarnings("unchecked")
    private void loadFromFile() {
        File file = new File(filePath);
        if (file.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath))) {
                records = (List<Record>) ois.readObject();
                nextId = ois.readInt();
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("Error loading table: " + e.getMessage());
                records = new ArrayList<>();
                nextId = 1;
            }
        }
    }
    
    public List<Record> getAllRecords() {
        return new ArrayList<>(records);
    }
    
    public String getTableName() {
        return tableName;
    }
    
    public int getRecordCount() {
        return records.size();
    }
}