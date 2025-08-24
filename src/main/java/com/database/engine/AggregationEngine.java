package com.database.engine;

import java.util.*;
import java.util.stream.Collectors;

public class AggregationEngine {
    private Database database;
    
    public AggregationEngine(Database database) {
        this.database = database;
    }
    
    /**
     * Process aggregation queries like COUNT, SUM, AVG, MIN, MAX
     * Examples:
     * - SELECT COUNT(*) FROM students
     * - SELECT AVG(age) FROM students
     * - SELECT COUNT(*) FROM students GROUP BY course
     */
    public List<Map<String, Object>> processAggregation(String query) {
        try {
            query = query.toLowerCase().trim();
            String[] parts = query.split("\\s+");
            
            // Find the aggregation function
            String selectPart = extractSelectPart(query);
            String tableName = extractTableName(query);
            String groupByField = extractGroupByField(query);
            String whereClause = extractWhereClause(query);
            
            Table table = database.getTable(tableName);
            List<Record> records = table.getAllRecords();
            
            // Apply WHERE filter if present
            if (whereClause != null && !whereClause.isEmpty()) {
                QueryProcessor queryProcessor = new QueryProcessor(database);
                records = queryProcessor.processComplexWhere(table, whereClause);
            }
            
            // Process aggregation
            if (groupByField != null) {
                return processGroupByAggregation(records, selectPart, groupByField);
            } else {
                return processSingleAggregation(records, selectPart);
            }
            
        } catch (Exception e) {
            throw new RuntimeException("Error processing aggregation: " + e.getMessage());
        }
    }
    
    /**
     * Extract SELECT part from query
     */
    private String extractSelectPart(String query) {
        int selectIndex = query.indexOf("select");
        int fromIndex = query.indexOf("from");
        
        if (selectIndex != -1 && fromIndex != -1) {
            return query.substring(selectIndex + 6, fromIndex).trim();
        }
        
        throw new RuntimeException("Invalid SELECT syntax");
    }
    
    /**
     * Extract table name from query
     */
    private String extractTableName(String query) {
        String[] parts = query.split("\\s+");
        int fromIndex = findIndex(parts, "from");
        
        if (fromIndex != -1 && fromIndex + 1 < parts.length) {
            return parts[fromIndex + 1];
        }
        
        throw new RuntimeException("Table name not found");
    }
    
    /**
     * Extract GROUP BY field
     */
    private String extractGroupByField(String query) {
        if (query.contains("group by")) {
            int groupByIndex = query.indexOf("group by");
            String afterGroupBy = query.substring(groupByIndex + 8).trim();
            String[] parts = afterGroupBy.split("\\s+");
            return parts[0];
        }
        return null;
    }
    
    /**
     * Extract WHERE clause
     */
    private String extractWhereClause(String query) {
        if (query.contains("where")) {
            int whereIndex = query.indexOf("where");
            int groupByIndex = query.indexOf("group by");
            
            if (groupByIndex != -1) {
                return query.substring(whereIndex + 5, groupByIndex).trim();
            } else {
                return query.substring(whereIndex + 5).trim();
            }
        }
        return null;
    }
    
    /**
     * Process single aggregation (no GROUP BY)
     */
    private List<Map<String, Object>> processSingleAggregation(List<Record> records, String selectPart) {
        List<Map<String, Object>> result = new ArrayList<>();
        Map<String, Object> resultRow = new HashMap<>();
        
        if (selectPart.equals("count(*)")) {
            resultRow.put("COUNT(*)", records.size());
        } else if (selectPart.startsWith("count(")) {
            String field = extractFieldFromFunction(selectPart);
            long count = records.stream()
                .filter(r -> r.getField(field) != null)
                .count();
            resultRow.put("COUNT(" + field + ")", count);
        } else if (selectPart.startsWith("sum(")) {
            String field = extractFieldFromFunction(selectPart);
            double sum = records.stream()
                .mapToDouble(r -> {
                    Object value = r.getField(field);
                    if (value instanceof Integer) {
                        return ((Integer) value).doubleValue();
                    } else if (value instanceof Double) {
                        return (Double) value;
                    }
                    return 0.0;
                })
                .sum();
            resultRow.put("SUM(" + field + ")", sum);
        } else if (selectPart.startsWith("avg(")) {
            String field = extractFieldFromFunction(selectPart);
            OptionalDouble avg = records.stream()
                .filter(r -> r.getField(field) instanceof Number)
                .mapToDouble(r -> {
                    Object value = r.getField(field);
                    if (value instanceof Integer) {
                        return ((Integer) value).doubleValue();
                    } else if (value instanceof Double) {
                        return (Double) value;
                    }
                    return 0.0;
                })
                .average();
            resultRow.put("AVG(" + field + ")", avg.orElse(0.0));
        } else if (selectPart.startsWith("min(")) {
            String field = extractFieldFromFunction(selectPart);
            Optional<Object> min = records.stream()
                .map(r -> r.getField(field))
                .filter(Objects::nonNull)
                .min((a, b) -> {
                    if (a instanceof Integer && b instanceof Integer) {
                        return Integer.compare((Integer) a, (Integer) b);
                    }
                    return a.toString().compareTo(b.toString());
                });
            resultRow.put("MIN(" + field + ")", min.orElse(null));
        } else if (selectPart.startsWith("max(")) {
            String field = extractFieldFromFunction(selectPart);
            Optional<Object> max = records.stream()
                .map(r -> r.getField(field))
                .filter(Objects::nonNull)
                .max((a, b) -> {
                    if (a instanceof Integer && b instanceof Integer) {
                        return Integer.compare((Integer) a, (Integer) b);
                    }
                    return a.toString().compareTo(b.toString());
                });
            resultRow.put("MAX(" + field + ")", max.orElse(null));
        }
        
        result.add(resultRow);
        return result;
    }
    
    /**
     * Process GROUP BY aggregation
     */
    private List<Map<String, Object>> processGroupByAggregation(List<Record> records, String selectPart, String groupByField) {
        List<Map<String, Object>> result = new ArrayList<>();
        
        // Group records by field value
        Map<Object, List<Record>> groupedRecords = records.stream()
            .filter(r -> r.getField(groupByField) != null)
            .collect(Collectors.groupingBy(r -> r.getField(groupByField)));
        
        // Process each group
        for (Map.Entry<Object, List<Record>> group : groupedRecords.entrySet()) {
            Map<String, Object> resultRow = new HashMap<>();
            Object groupValue = group.getKey();
            List<Record> groupRecords = group.getValue();
            
            // Add group by field value
            resultRow.put(groupByField, groupValue);
            
            // Calculate aggregation for this group
            if (selectPart.equals("count(*)")) {
                resultRow.put("COUNT(*)", groupRecords.size());
            } else if (selectPart.startsWith("count(")) {
                String field = extractFieldFromFunction(selectPart);
                long count = groupRecords.stream()
                    .filter(r -> r.getField(field) != null)
                    .count();
                resultRow.put("COUNT(" + field + ")", count);
            } else if (selectPart.startsWith("sum(")) {
                String field = extractFieldFromFunction(selectPart);
                double sum = groupRecords.stream()
                    .mapToDouble(r -> {
                        Object value = r.getField(field);
                        if (value instanceof Integer) {
                            return ((Integer) value).doubleValue();
                        } else if (value instanceof Double) {
                            return (Double) value;
                        }
                        return 0.0;
                    })
                    .sum();
                resultRow.put("SUM(" + field + ")", sum);
            } else if (selectPart.startsWith("avg(")) {
                String field = extractFieldFromFunction(selectPart);
                OptionalDouble avg = groupRecords.stream()
                    .filter(r -> r.getField(field) instanceof Number)
                    .mapToDouble(r -> {
                        Object value = r.getField(field);
                        if (value instanceof Integer) {
                            return ((Integer) value).doubleValue();
                        } else if (value instanceof Double) {
                            return (Double) value;
                        }
                        return 0.0;
                    })
                    .average();
                resultRow.put("AVG(" + field + ")", avg.orElse(0.0));
            } else if (selectPart.startsWith("min(")) {
                String field = extractFieldFromFunction(selectPart);
                Optional<Object> min = groupRecords.stream()
                    .map(r -> r.getField(field))
                    .filter(Objects::nonNull)
                    .min((a, b) -> {
                        if (a instanceof Integer && b instanceof Integer) {
                            return Integer.compare((Integer) a, (Integer) b);
                        }
                        return a.toString().compareTo(b.toString());
                    });
                resultRow.put("MIN(" + field + ")", min.orElse(null));
            } else if (selectPart.startsWith("max(")) {
                String field = extractFieldFromFunction(selectPart);
                Optional<Object> max = groupRecords.stream()
                    .map(r -> r.getField(field))
                    .filter(Objects::nonNull)
                    .max((a, b) -> {
                        if (a instanceof Integer && b instanceof Integer) {
                            return Integer.compare((Integer) a, (Integer) b);
                        }
                        return a.toString().compareTo(b.toString());
                    });
                resultRow.put("MAX(" + field + ")", max.orElse(null));
            }
            
            result.add(resultRow);
        }
        
        return result;
    }
    
    /**
     * Extract field name from aggregation function
     * Example: "count(age)" -> "age"
     */
    private String extractFieldFromFunction(String functionCall) {
        int openParen = functionCall.indexOf('(');
        int closeParen = functionCall.indexOf(')');
        
        if (openParen != -1 && closeParen != -1) {
            return functionCall.substring(openParen + 1, closeParen).trim();
        }
        
        throw new RuntimeException("Invalid function syntax: " + functionCall);
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
     * Check if query contains aggregation functions
     */
    public static boolean isAggregationQuery(String query) {
        String lowerQuery = query.toLowerCase();
        return lowerQuery.contains("count(") || 
               lowerQuery.contains("sum(") || 
               lowerQuery.contains("avg(") || 
               lowerQuery.contains("min(") || 
               lowerQuery.contains("max(") ||
               lowerQuery.contains("group by");
    }
    
    /**
     * Display aggregation results in a formatted way
     */
    public void displayAggregationResults(List<Map<String, Object>> results) {
        if (results.isEmpty()) {
            System.out.println("No results found.");
            return;
        }
        
        System.out.println("\n=== Aggregation Results ===");
        
        // Print header
        Set<String> allKeys = new LinkedHashSet<>();
        for (Map<String, Object> row : results) {
            allKeys.addAll(row.keySet());
        }
        
        for (String key : allKeys) {
            System.out.printf("%-15s", key);
        }
        System.out.println();
        System.out.println("-".repeat(allKeys.size() * 15));
        
        // Print data
        for (Map<String, Object> row : results) {
            for (String key : allKeys) {
                Object value = row.get(key);
                if (value instanceof Double) {
                    System.out.printf("%-15.2f", (Double) value);
                } else {
                    System.out.printf("%-15s", value != null ? value.toString() : "NULL");
                }
            }
            System.out.println();
        }
        
        System.out.println("\nTotal rows: " + results.size());
        System.out.println("===============================\n");
    }
}