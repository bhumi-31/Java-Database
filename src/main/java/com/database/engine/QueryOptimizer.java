package com.database.engine;

import java.util.*;

public class QueryOptimizer {
    private Database database;
    
    public QueryOptimizer(Database database) {
        this.database = database;
    }
    
    // Optimize WHERE conditions - push down filters early
    public List<Record> optimizedSelect(String tableName, Map<String, Object> conditions) {
        Table table = database.getTable(tableName);
        List<Record> records = table.getAllRecords();
        
        // Sort conditions by selectivity (smaller results first)
        List<Map.Entry<String, Object>> sortedConditions = new ArrayList<>(conditions.entrySet());
        sortedConditions.sort((a, b) -> {
            int countA = countMatches(records, a.getKey(), a.getValue());
            int countB = countMatches(records, b.getKey(), b.getValue());
            return Integer.compare(countA, countB);
        });
        
        // Apply conditions in optimized order
        List<Record> result = new ArrayList<>(records);
        for (Map.Entry<String, Object> condition : sortedConditions) {
            result = filterRecords(result, condition.getKey(), condition.getValue());
        }
        
        return result;
    }
    
    // Count matches for selectivity estimation
    private int countMatches(List<Record> records, String field, Object value) {
        int count = 0;
        for (Record record : records) {
            if (Objects.equals(record.getField(field), value)) {
                count++;
            }
        }
        return count;
    }
    
    // Filter records with given condition
    private List<Record> filterRecords(List<Record> records, String field, Object value) {
        List<Record> filtered = new ArrayList<>();
        for (Record record : records) {
            if (Objects.equals(record.getField(field), value)) {
                filtered.add(record);
            }
        }
        return filtered;
    }
    
    // Optimize ORDER BY - suggest index creation for frequently sorted fields
    public List<Record> optimizedOrderBy(List<Record> records, String field, boolean ascending) {
        List<Record> sortedRecords = new ArrayList<>(records);
        
        sortedRecords.sort((r1, r2) -> {
            Object v1 = r1.getField(field);
            Object v2 = r2.getField(field);
            
            if (v1 == null && v2 == null) return 0;
            if (v1 == null) return ascending ? -1 : 1;
            if (v2 == null) return ascending ? 1 : -1;
            
            int comparison;
            if (v1 instanceof Number && v2 instanceof Number) {
                comparison = Double.compare(((Number) v1).doubleValue(), ((Number) v2).doubleValue());
            } else {
                comparison = v1.toString().compareTo(v2.toString());
            }
            
            return ascending ? comparison : -comparison;
        });
        
        return sortedRecords;
    }
    
    // Suggest optimal JOIN order based on table sizes
    public String[] optimizeJoinOrder(String[] tables) {
        Map<String, Integer> tableSizes = new HashMap<>();
        
        for (String tableName : tables) {
            Table table = database.getTable(tableName);
            tableSizes.put(tableName, table.getRecordCount());
        }
        
        // Sort tables by size (smaller first for better performance)
        List<String> sortedTables = Arrays.asList(tables.clone());
        sortedTables.sort((a, b) -> Integer.compare(tableSizes.get(a), tableSizes.get(b)));
        
        return sortedTables.toArray(new String[0]);
    }
    
    // Analyze query complexity
    public QueryStats analyzeQuery(String query) {
        QueryStats stats = new QueryStats();
        
        query = query.toLowerCase();
        
        // Count operations
        if (query.contains("join")) stats.joinCount++;
        if (query.contains("where")) stats.whereConditions = countOccurrences(query, "and") + 1;
        if (query.contains("order by")) stats.hasOrderBy = true;
        if (query.contains("group by")) stats.hasGroupBy = true;
        
        // Estimate complexity
        stats.complexity = calculateComplexity(stats);
        
        return stats;
    }
    
    private int countOccurrences(String str, String substring) {
        int count = 0;
        int index = 0;
        while ((index = str.indexOf(substring, index)) != -1) {
            count++;
            index += substring.length();
        }
        return count;
    }
    
    private int calculateComplexity(QueryStats stats) {
        int complexity = 1;
        complexity += stats.joinCount * 3;
        complexity += stats.whereConditions;
        if (stats.hasOrderBy) complexity += 2;
        if (stats.hasGroupBy) complexity += 2;
        return complexity;
    }
    
    // Query statistics class
    public static class QueryStats {
        public int joinCount = 0;
        public int whereConditions = 0;
        public boolean hasOrderBy = false;
        public boolean hasGroupBy = false;
        public int complexity = 1;
        
        @Override
        public String toString() {
            return String.format(
                "Query Stats: Joins=%d, WHERE conditions=%d, ORDER BY=%b, GROUP BY=%b, Complexity=%d",
                joinCount, whereConditions, hasOrderBy, hasGroupBy, complexity
            );
        }
    }
    
    // Get optimization suggestions
    public List<String> getOptimizationSuggestions(String query, List<String> tableNames) {
        List<String> suggestions = new ArrayList<>();
        
        query = query.toLowerCase();
        
        // Check for full table scans
        if (query.contains("select * from") && !query.contains("where")) {
            suggestions.add("Consider adding WHERE clause to avoid full table scan");
        }
        
        // Check for multiple JOINs
        int joinCount = countOccurrences(query, "join");
        if (joinCount > 2) {
            suggestions.add("Multiple JOINs detected - consider breaking into smaller queries");
        }
        
        // Check for ORDER BY without LIMIT
        if (query.contains("order by") && !query.contains("limit")) {
            suggestions.add("Consider adding LIMIT clause with ORDER BY for better performance");
        }
        
        // Check table sizes for JOIN optimization
        if (joinCount > 0 && tableNames.size() > 1) {
            String[] optimizedOrder = optimizeJoinOrder(tableNames.toArray(new String[0]));
            suggestions.add("Optimal JOIN order: " + String.join(" -> ", optimizedOrder));
        }
        
        return suggestions;
    }

    
}