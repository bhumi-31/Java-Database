package com.database.engine;

import java.util.*;

public class QueryProcessor {
    private Database database;
    
    public QueryProcessor(Database database) {
        this.database = database;
    }
    
    /**
     * Process JOIN queries
     * Syntax: SELECT * FROM table1 JOIN table2 ON table1.field=table2.field
     */
    public List<Record> processJoin(String query) {
        try {
            // Parse JOIN query
            String[] parts = query.toLowerCase().split("\\s+");
            
            int fromIndex = findIndex(parts, "from");
            int joinIndex = findIndex(parts, "join");
            int onIndex = findIndex(parts, "on");
            
            if (fromIndex == -1 || joinIndex == -1 || onIndex == -1) {
                throw new RuntimeException("Invalid JOIN syntax");
            }
            
            String table1Name = parts[fromIndex + 1];
            String table2Name = parts[joinIndex + 1];
            String joinCondition = parts[onIndex + 1];
            
            // Parse join condition (table1.field=table2.field)
            String[] conditionParts = joinCondition.split("=");
            if (conditionParts.length != 2) {
                throw new RuntimeException("Invalid JOIN condition");
            }
            
            String[] leftField = conditionParts[0].split("\\.");
            String[] rightField = conditionParts[1].split("\\.");
            
            String table1Field = leftField[1];
            String table2Field = rightField[1];
            
            // Get tables
            Table table1 = database.getTable(table1Name);
            Table table2 = database.getTable(table2Name);
            
            // Perform INNER JOIN
            return performInnerJoin(table1, table2, table1Field, table2Field);
            
        } catch (Exception e) {
            throw new RuntimeException("Error processing JOIN: " + e.getMessage());
        }
    }
    
    /**
     * Perform INNER JOIN between two tables
     */
    public List<Record> performInnerJoin(Table table1, Table table2, String field1, String field2) {
        List<Record> result = new ArrayList<>();
        List<Record> records1 = table1.getAllRecords();
        List<Record> records2 = table2.getAllRecords();
        
        int joinedId = 1;
        
        for (Record r1 : records1) {
            for (Record r2 : records2) {
                Object value1 = r1.getField(field1);
                Object value2 = r2.getField(field2);
                
                if (value1 != null && value2 != null && value1.equals(value2)) {
                    // Create joined record
                    Record joinedRecord = new Record(joinedId++);
                    
                    // Add fields from first table with table prefix
                    for (Map.Entry<String, Object> entry : r1.getAllFields().entrySet()) {
                        joinedRecord.setField(table1.getTableName() + "." + entry.getKey(), entry.getValue());
                    }
                    
                    // Add fields from second table with table prefix
                    for (Map.Entry<String, Object> entry : r2.getAllFields().entrySet()) {
                        joinedRecord.setField(table2.getTableName() + "." + entry.getKey(), entry.getValue());
                    }
                    
                    result.add(joinedRecord);
                }
            }
        }
        
        return result;
    }
    
    /**
 * Simple INNER JOIN method for direct table names and single field
 * Used by DatabaseEngineApp for JOIN table1 table2 ON field syntax
 */
public List<Record> innerJoin(String table1Name, String table2Name, String joinField) {
    try {
        Table table1 = database.getTable(table1Name);
        Table table2 = database.getTable(table2Name);
        
        return performInnerJoin(table1, table2, joinField, joinField);
        
    } catch (Exception e) {
        throw new RuntimeException("Error in INNER JOIN: " + e.getMessage());
    }
}
    /**
     * Perform LEFT JOIN between two tables
     */
    public List<Record> performLeftJoin(Table table1, Table table2, String field1, String field2) {
        List<Record> result = new ArrayList<>();
        List<Record> records1 = table1.getAllRecords();
        List<Record> records2 = table2.getAllRecords();
        
        int joinedId = 1;
        
        for (Record r1 : records1) {
            boolean hasMatch = false;
            
            for (Record r2 : records2) {
                Object value1 = r1.getField(field1);
                Object value2 = r2.getField(field2);
                
                if (value1 != null && value2 != null && value1.equals(value2)) {
                    hasMatch = true;
                    
                    // Create joined record
                    Record joinedRecord = new Record(joinedId++);
                    
                    // Add fields from first table
                    for (Map.Entry<String, Object> entry : r1.getAllFields().entrySet()) {
                        joinedRecord.setField(table1.getTableName() + "." + entry.getKey(), entry.getValue());
                    }
                    
                    // Add fields from second table
                    for (Map.Entry<String, Object> entry : r2.getAllFields().entrySet()) {
                        joinedRecord.setField(table2.getTableName() + "." + entry.getKey(), entry.getValue());
                    }
                    
                    result.add(joinedRecord);
                }
            }
            
            // If no match found, add record with null values for right table
            if (!hasMatch) {
                Record joinedRecord = new Record(joinedId++);
                
                // Add fields from first table
                for (Map.Entry<String, Object> entry : r1.getAllFields().entrySet()) {
                    joinedRecord.setField(table1.getTableName() + "." + entry.getKey(), entry.getValue());
                }
                
                // Add null fields for second table
                // Note: In real implementation, you'd get schema info from table2
                joinedRecord.setField(table2.getTableName() + ".null_field", null);
                
                result.add(joinedRecord);
            }
        }
        
        return result;
    }
    
    /**
     * Process complex WHERE conditions with AND/OR
     */
    public List<Record> processComplexWhere(Table table, String whereClause) {
        List<Record> allRecords = table.getAllRecords();
        List<Record> result = new ArrayList<>();
        
        // Simple implementation for AND conditions
        if (whereClause.toLowerCase().contains(" and ")) {
            String[] conditions = whereClause.split("(?i)\\s+and\\s+");
            
            for (Record record : allRecords) {
                boolean matchesAll = true;
                
                for (String condition : conditions) {
                    if (!evaluateCondition(record, condition.trim())) {
                        matchesAll = false;
                        break;
                    }
                }
                
                if (matchesAll) {
                    result.add(record);
                }
            }
        }
        // Simple implementation for OR conditions
        else if (whereClause.toLowerCase().contains(" or ")) {
            String[] conditions = whereClause.split("(?i)\\s+or\\s+");
            
            for (Record record : allRecords) {
                for (String condition : conditions) {
                    if (evaluateCondition(record, condition.trim())) {
                        result.add(record);
                        break; // Found match, no need to check other conditions
                    }
                }
            }
        }
        // Single condition
        else {
            for (Record record : allRecords) {
                if (evaluateCondition(record, whereClause)) {
                    result.add(record);
                }
            }
        }
        
        return result;
    }
    
    /**
     * Evaluate single condition (field=value, field>value, etc.)
     */
    private boolean evaluateCondition(Record record, String condition) {
        try {
            // Handle different operators
            if (condition.contains(">=")) {
                String[] parts = condition.split(">=");
                return compareValues(record.getField(parts[0].trim()), parts[1].trim(), ">=");
            } else if (condition.contains("<=")) {
                String[] parts = condition.split("<=");
                return compareValues(record.getField(parts[0].trim()), parts[1].trim(), "<=");
            } else if (condition.contains(">")) {
                String[] parts = condition.split(">");
                return compareValues(record.getField(parts[0].trim()), parts[1].trim(), ">");
            } else if (condition.contains("<")) {
                String[] parts = condition.split("<");
                return compareValues(record.getField(parts[0].trim()), parts[1].trim(), "<");
            } else if (condition.contains("=")) {
                String[] parts = condition.split("=");
                Object fieldValue = record.getField(parts[0].trim());
                String targetValue = parts[1].trim();
                
                // Try to match as number first, then as string
                try {
                    return Objects.equals(fieldValue, Integer.parseInt(targetValue));
                } catch (NumberFormatException e) {
                    return Objects.equals(fieldValue, targetValue);
                }
            }
        } catch (Exception e) {
            return false;
        }
        
        return false;
    }
    
    /**
     * Compare values with different operators
     */
    private boolean compareValues(Object fieldValue, String targetValue, String operator) {
        if (fieldValue == null) return false;
        
        try {
            // Try numeric comparison
            if (fieldValue instanceof Integer) {
                int fieldInt = (Integer) fieldValue;
                int targetInt = Integer.parseInt(targetValue);
                
                switch (operator) {
                    case ">": return fieldInt > targetInt;
                    case "<": return fieldInt < targetInt;
                    case ">=": return fieldInt >= targetInt;
                    case "<=": return fieldInt <= targetInt;
                }
            }
            // String comparison
            else if (fieldValue instanceof String) {
                String fieldStr = (String) fieldValue;
                int comparison = fieldStr.compareTo(targetValue);
                
                switch (operator) {
                    case ">": return comparison > 0;
                    case "<": return comparison < 0;
                    case ">=": return comparison >= 0;
                    case "<=": return comparison <= 0;
                }
            }
        } catch (NumberFormatException e) {
            // If parsing fails, treat as string comparison
        }
        
        return false;
    }
    
    /**
     * Helper method to find index of a word in array
     */
    private int findIndex(String[] array, String word) {
        for (int i = 0; i < array.length; i++) {
            if (array[i].equalsIgnoreCase(word)) {
                return i;
            }
        }
        return -1;
    }
    
    /**
     * Process ORDER BY clause
     */
    public List<Record> processOrderBy(List<Record> records, String orderByClause) {
        String[] parts = orderByClause.split("\\s+");
        String fieldName = parts[0];
        boolean ascending = true;
        
        if (parts.length > 1 && parts[1].equalsIgnoreCase("DESC")) {
            ascending = false;
        }
        
        final boolean isAscending = ascending;
        
        records.sort((r1, r2) -> {
            Object val1 = r1.getField(fieldName);
            Object val2 = r2.getField(fieldName);
            
            if (val1 == null && val2 == null) return 0;
            if (val1 == null) return isAscending ? -1 : 1;
            if (val2 == null) return isAscending ? 1 : -1;
            
            int comparison = 0;
            if (val1 instanceof Integer && val2 instanceof Integer) {
                comparison = Integer.compare((Integer) val1, (Integer) val2);
            } else {
                comparison = val1.toString().compareTo(val2.toString());
            }
            
            return isAscending ? comparison : -comparison;
        });
        
        return records;
    }
    
    /**
     * Apply LIMIT to results
     */
    public List<Record> applyLimit(List<Record> records, int limit) {
        if (limit <= 0 || limit >= records.size()) {
            return records;
        }
        
        return records.subList(0, limit);
    }
}