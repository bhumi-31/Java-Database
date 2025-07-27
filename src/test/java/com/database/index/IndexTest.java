package com.database.index;

import com.database.types.TypedValue;
import com.database.types.IntegerValue;
import com.database.types.VarcharValue;
import java.util.List;
import java.util.Map;

/**
 * Test class for indexing functionality.
 */
public class IndexTest {
    
    public static void main(String[] args) {
        System.out.println("=== Index System Test ===");
        
        testBTreeIndex();
        testHashIndex();
        testIndexManager();
        
        System.out.println("All index tests completed successfully!");
    }
    
    public static void testBTreeIndex() {
        System.out.println("\n--- Testing B-Tree Index ---");
        
        BTreeIndex<TypedValue, String> btree = new BTreeIndex<>();
        
        // Test insertions
        btree.insert(new IntegerValue(10), "Value10");
        btree.insert(new IntegerValue(5), "Value5");
        btree.insert(new IntegerValue(15), "Value15");
        btree.insert(new IntegerValue(3), "Value3");
        btree.insert(new IntegerValue(7), "Value7");
        btree.insert(new IntegerValue(12), "Value12");
        btree.insert(new IntegerValue(18), "Value18");
        
        System.out.println("Inserted 7 values into B-Tree");
        System.out.println("B-Tree size: " + btree.size());
        
        // Test searches
        String result = btree.search(new IntegerValue(7));
        System.out.println("Search for 7: " + result);
        
        result = btree.search(new IntegerValue(20));
        System.out.println("Search for 20 (not found): " + result);
        
        // Test range query
        List<String> rangeResults = btree.rangeQuery(new IntegerValue(5), new IntegerValue(15));
        System.out.println("Range query [5-15]: " + rangeResults);
        
        // Test deletion
        boolean deleted = btree.delete(new IntegerValue(7));
        System.out.println("Deleted 7: " + deleted);
        System.out.println("Size after deletion: " + btree.size());
    }
    
    public static void testHashIndex() {
        System.out.println("\n--- Testing Hash Index ---");
        
        HashIndex<TypedValue, Integer> hash = new HashIndex<>();
        
        // Test insertions
        hash.insert(new VarcharValue("apple"), 1);
        hash.insert(new VarcharValue("banana"), 2);
        hash.insert(new VarcharValue("cherry"), 3);
        hash.insert(new VarcharValue("date"), 4);
        
        System.out.println("Inserted 4 values into Hash Index");
        System.out.println("Hash size: " + hash.size());
        
        // Test searches
        Integer result = hash.search(new VarcharValue("banana"));
        System.out.println("Search for 'banana': " + result);
        
        result = hash.search(new VarcharValue("grape"));
        System.out.println("Search for 'grape' (not found): " + result);
        
        // Test contains
        boolean contains = hash.containsKey(new VarcharValue("apple"));
        System.out.println("Contains 'apple': " + contains);
        
        // Test deletion
        boolean deleted = hash.delete(new VarcharValue("cherry"));
        System.out.println("Deleted 'cherry': " + deleted);
        System.out.println("Size after deletion: " + hash.size());
    }
    
    public static void testIndexManager() {
        System.out.println("\n--- Testing Index Manager ---");
        
        IndexManager manager = new IndexManager();
        
        // Create indexes
        manager.createPrimaryKeyIndex("id");
        manager.createIndex("name", IndexManager.IndexType.HASH);
        manager.createIndex("age", IndexManager.IndexType.BTREE);
        
        System.out.println("Created indexes on: id (PK), name (HASH), age (BTREE)");
        
        // Test index statistics
        Map<String, IndexManager.IndexStats> stats = manager.getIndexStatistics();
        System.out.println("Index statistics:");
        for (IndexManager.IndexStats stat : stats.values()) {
            System.out.println("  " + stat);
        }
        
        // Test index existence
        System.out.println("Has index on 'name': " + manager.hasIndex("name"));
        System.out.println("Has index on 'email': " + manager.hasIndex("email"));
        
        // Test index types
        System.out.println("Index type for 'age': " + manager.getIndexType("age"));
        
        // Test indexed columns
        System.out.println("Indexed columns: " + manager.getIndexedColumns());
    }
}
