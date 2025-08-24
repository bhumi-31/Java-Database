package com.database.engine;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class BenchmarkManager {
    private Database database;
    private QueryProcessor queryProcessor;
    private AggregationEngine aggregationEngine;
    private List<QueryBenchmark> benchmarkHistory;
    
    public BenchmarkManager(Database database, QueryProcessor queryProcessor, AggregationEngine aggregationEngine) {
        this.database = database;
        this.queryProcessor = queryProcessor;
        this.aggregationEngine = aggregationEngine;
        this.benchmarkHistory = new ArrayList<>();
    }
    
    /**
     * Execute query with performance measurement
     */
    public BenchmarkResult executeWithBenchmark(String query) {
        long startTime = System.nanoTime();
        long startMemory = getUsedMemory();
        
        Object result = null;
        Exception error = null;
        int resultCount = 0;
        String queryType = detectQueryType(query);
        
        try {
            // Execute query based on type
            switch (queryType) {
                case "AGGREGATION":
                    List<Map<String, Object>> aggResults = aggregationEngine.processAggregation(query);
                    result = aggResults;
                    resultCount = aggResults.size();
                    break;
                    
                case "JOIN":
                    List<Record> joinResults = queryProcessor.processJoin(query);
                    result = joinResults;
                    resultCount = joinResults.size();
                    break;
                    
                case "SELECT":
                    List<Record> selectResults = executeSelect(query);
                    result = selectResults;
                    resultCount = selectResults.size();
                    break;
                    
                case "INSERT":
                    result = executeInsert(query);
                    resultCount = 1;
                    break;
                    
                case "CREATE":
                    result = executeCreate(query);
                    resultCount = 1;
                    break;
                    
                default:
                    throw new RuntimeException("Unsupported query type: " + queryType);
            }
            
        } catch (Exception e) {
            error = e;
        }
        
        long endTime = System.nanoTime();
        long endMemory = getUsedMemory();
        
        long executionTimeNanos = endTime - startTime;
        long memoryUsed = endMemory - startMemory;
        
        BenchmarkResult benchmarkResult = new BenchmarkResult(
            query,
            queryType,
            executionTimeNanos,
            memoryUsed,
            resultCount,
            result,
            error
        );
        
        // Add to history
        QueryBenchmark benchmark = new QueryBenchmark(query, queryType, executionTimeNanos, resultCount);
        benchmarkHistory.add(benchmark);
        
        return benchmarkResult;
    }
    
    /**
     * Run comprehensive benchmarks
     */
    public void runComprehensiveBenchmarks() {
        System.out.println("\n=== Running Comprehensive Benchmarks ===");
        
        // Setup test data
        setupBenchmarkData();
        
        List<String> testQueries = Arrays.asList(
            // Basic operations
            "CREATE TABLE benchmark_test",
            "INSERT INTO benchmark_test name=TestUser age=25 score=85",
            "SELECT * FROM benchmark_test",
            "SELECT * FROM benchmark_test WHERE age>20",
            
            // Aggregation benchmarks
            "SELECT COUNT(*) FROM benchmark_test",
            "SELECT AVG(score) FROM benchmark_test",
            "SELECT score, COUNT(*) FROM benchmark_test GROUP BY score",
            
            // Complex queries
            "SELECT * FROM benchmark_test WHERE age>20 AND score>80",
            "SELECT * FROM benchmark_test ORDER BY score DESC"
        );
        
        List<BenchmarkResult> results = new ArrayList<>();
        
        for (String query : testQueries) {
            System.out.println("\nBenchmarking: " + query);
            try {
                BenchmarkResult result = executeWithBenchmark(query);
                results.add(result);
                
                System.out.printf("  ✓ Executed in: %.3f ms\n", result.getExecutionTimeMs());
                System.out.printf("  ✓ Records: %d\n", result.getResultCount());
                System.out.printf("  ✓ Memory: %s\n", formatMemory(result.getMemoryUsed()));
                
            } catch (Exception e) {
                System.out.println("  ❌ Failed: " + e.getMessage());
            }
        }
        
        // Display summary
        displayBenchmarkSummary(results);
    }
    
    /**
     * Setup benchmark test data
     */
    private void setupBenchmarkData() {
        try {
            // Create benchmark table
            database.createTable("benchmark_test");
            Table table = database.getTable("benchmark_test");
            
            // Insert test data
            for (int i = 1; i <= 1000; i++) {
                Map<String, Object> data = new HashMap<>();
                data.put("name", "User" + i);
                data.put("age", 18 + (i % 50));  // Age between 18-67
                data.put("score", 50 + (i % 50)); // Score between 50-99
                data.put("department", "Dept" + ((i % 5) + 1)); // 5 departments
                table.insert(data);
            }
            
            System.out.println("✓ Benchmark data setup complete (1000 records)");
            
        } catch (Exception e) {
            System.out.println("⚠️ Benchmark data setup failed: " + e.getMessage());
        }
    }
    
    /**
     * Display benchmark summary
     */
    private void displayBenchmarkSummary(List<BenchmarkResult> results) {
        System.out.println("\n=== Benchmark Summary ===");
        
        double totalTime = 0;
        int successCount = 0;
        int failCount = 0;
        
        System.out.println("Query Type           | Avg Time (ms) | Success Rate | Avg Records");
        System.out.println("---------------------|---------------|--------------|------------");
        
        Map<String, List<BenchmarkResult>> groupedResults = new HashMap<>();
        
        for (BenchmarkResult result : results) {
            groupedResults.computeIfAbsent(result.getQueryType(), k -> new ArrayList<>()).add(result);
            totalTime += result.getExecutionTimeMs();
            
            if (result.getError() == null) {
                successCount++;
            } else {
                failCount++;
            }
        }
        
        for (Map.Entry<String, List<BenchmarkResult>> entry : groupedResults.entrySet()) {
            String queryType = entry.getKey();
            List<BenchmarkResult> typeResults = entry.getValue();
            
            double avgTime = typeResults.stream()
                .mapToDouble(BenchmarkResult::getExecutionTimeMs)
                .average().orElse(0.0);
                
            double avgRecords = typeResults.stream()
                .filter(r -> r.getError() == null)
                .mapToInt(BenchmarkResult::getResultCount)
                .average().orElse(0.0);
                
            long successRate = typeResults.stream()
                .filter(r -> r.getError() == null)
                .count() * 100 / typeResults.size();
            
            System.out.printf("%-20s | %10.3f | %9d%% | %10.1f\n", 
                queryType, avgTime, successRate, avgRecords);
        }
        
        System.out.println("---------------------|---------------|--------------|------------");
        System.out.printf("Total Queries: %d | Total Time: %.3f ms | Success: %d | Failed: %d\n",
            results.size(), totalTime, successCount, failCount);
        
        // Performance insights
        displayPerformanceInsights(results);
    }
    
    /**
     * Display performance insights
     */
    private void displayPerformanceInsights(List<BenchmarkResult> results) {
        System.out.println("\n=== Performance Insights ===");
        
        // Find slowest query
        BenchmarkResult slowest = results.stream()
            .filter(r -> r.getError() == null)
            .max(Comparator.comparingDouble(BenchmarkResult::getExecutionTimeMs))
            .orElse(null);
            
        if (slowest != null) {
            System.out.printf("🐌 Slowest Query: %s (%.3f ms)\n", 
                slowest.getQuery(), slowest.getExecutionTimeMs());
        }
        
        // Find fastest query
        BenchmarkResult fastest = results.stream()
            .filter(r -> r.getError() == null)
            .min(Comparator.comparingDouble(BenchmarkResult::getExecutionTimeMs))
            .orElse(null);
            
        if (fastest != null) {
            System.out.printf("🚀 Fastest Query: %s (%.3f ms)\n", 
                fastest.getQuery(), fastest.getExecutionTimeMs());
        }
        
        // Memory usage insights
        long totalMemory = results.stream()
            .mapToLong(BenchmarkResult::getMemoryUsed)
            .sum();
            
        System.out.printf("💾 Total Memory Used: %s\n", formatMemory(totalMemory));
        
        // Performance recommendations
        System.out.println("\n📊 Recommendations:");
        
        double avgAggregationTime = results.stream()
            .filter(r -> "AGGREGATION".equals(r.getQueryType()) && r.getError() == null)
            .mapToDouble(BenchmarkResult::getExecutionTimeMs)
            .average().orElse(0.0);
            
        double avgSelectTime = results.stream()
            .filter(r -> "SELECT".equals(r.getQueryType()) && r.getError() == null)
            .mapToDouble(BenchmarkResult::getExecutionTimeMs)
            .average().orElse(0.0);
        
        if (avgAggregationTime > avgSelectTime * 2) {
            System.out.println("- Consider adding indexes for aggregation queries");
        }
        
        if (slowest != null && slowest.getExecutionTimeMs() > 10.0) {
            System.out.println("- Optimize slow queries by adding WHERE clauses");
        }
        
        System.out.println("- Use LIMIT clause for large result sets");
        System.out.println("- Consider partitioning large tables");
    }
    
    /**
     * Get query execution history
     */
    public List<QueryBenchmark> getBenchmarkHistory() {
        return new ArrayList<>(benchmarkHistory);
    }
    
    /**
     * Clear benchmark history
     */
    public void clearHistory() {
        benchmarkHistory.clear();
    }
    
    /**
     * Get performance statistics
     */
    public Map<String, Object> getPerformanceStats() {
        Map<String, Object> stats = new HashMap<>();
        
        if (benchmarkHistory.isEmpty()) {
            return stats;
        }
        
        double avgExecutionTime = benchmarkHistory.stream()
            .mapToLong(QueryBenchmark::getExecutionTimeNanos)
            .average().orElse(0.0) / 1_000_000.0; // Convert to ms
            
        long totalQueries = benchmarkHistory.size();
        
        Map<String, Long> queryTypeCount = new HashMap<>();
        for (QueryBenchmark benchmark : benchmarkHistory) {
            queryTypeCount.merge(benchmark.getQueryType(), 1L, Long::sum);
        }
        
        stats.put("totalQueries", totalQueries);
        stats.put("averageExecutionTimeMs", avgExecutionTime);
        stats.put("queryTypeDistribution", queryTypeCount);
        
        return stats;
    }
    
    // Helper methods
    
    private String detectQueryType(String query) {
        String lowerQuery = query.toLowerCase().trim();
        
        if (AggregationEngine.isAggregationQuery(query)) {
            return "AGGREGATION";
        } else if (lowerQuery.contains(" join ")) {
            return "JOIN";
        } else if (lowerQuery.startsWith("select")) {
            return "SELECT";
        } else if (lowerQuery.startsWith("insert")) {
            return "INSERT";
        } else if (lowerQuery.startsWith("create")) {
            return "CREATE";
        } else if (lowerQuery.startsWith("update")) {
            return "UPDATE";
        } else if (lowerQuery.startsWith("delete")) {
            return "DELETE";
        }
        
        return "UNKNOWN";
    }
    
    private List<Record> executeSelect(String query) {
        // This is a simplified version - in real implementation,
        // you'd integrate with your existing SELECT logic
        String[] parts = query.split("\\s+");
        if (parts.length >= 4) {
            String tableName = parts[3];
            Table table = database.getTable(tableName);
            return table.selectAll();
        }
        return new ArrayList<>();
    }
    
    private String executeInsert(String query) {
        // Simplified INSERT execution
        return "INSERT_SUCCESS";
    }
    
    private String executeCreate(String query) {
        // Simplified CREATE execution
        return "CREATE_SUCCESS";
    }
    
    private long getUsedMemory() {
        Runtime runtime = Runtime.getRuntime();
        return runtime.totalMemory() - runtime.freeMemory();
    }
    
    private String formatMemory(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
    }
    
    // Inner classes for benchmark data
    
    public static class BenchmarkResult {
        private String query;
        private String queryType;
        private long executionTimeNanos;
        private long memoryUsed;
        private int resultCount;
        private Object result;
        private Exception error;
        
        public BenchmarkResult(String query, String queryType, long executionTimeNanos, 
                             long memoryUsed, int resultCount, Object result, Exception error) {
            this.query = query;
            this.queryType = queryType;
            this.executionTimeNanos = executionTimeNanos;
            this.memoryUsed = memoryUsed;
            this.resultCount = resultCount;
            this.result = result;
            this.error = error;
        }
        
        public double getExecutionTimeMs() {
            return executionTimeNanos / 1_000_000.0;
        }
        
        // Getters
        public String getQuery() { return query; }
        public String getQueryType() { return queryType; }
        public long getExecutionTimeNanos() { return executionTimeNanos; }
        public long getMemoryUsed() { return memoryUsed; }
        public int getResultCount() { return resultCount; }
        public Object getResult() { return result; }
        public Exception getError() { return error; }
    }
    
    public static class QueryBenchmark {
        private String query;
        private String queryType;
        private long executionTimeNanos;
        private int resultCount;
        private long timestamp;
        
        public QueryBenchmark(String query, String queryType, long executionTimeNanos, int resultCount) {
            this.query = query;
            this.queryType = queryType;
            this.executionTimeNanos = executionTimeNanos;
            this.resultCount = resultCount;
            this.timestamp = System.currentTimeMillis();
        }
        
        // Getters
        public String getQuery() { return query; }
        public String getQueryType() { return queryType; }
        public long getExecutionTimeNanos() { return executionTimeNanos; }
        public int getResultCount() { return resultCount; }
        public long getTimestamp() { return timestamp; }
    }
}