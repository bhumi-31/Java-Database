package com.database.aggregation;

import com.database.core.Row;
import com.database.types.TypedValue;
import com.database.types.IntegerValue;
import com.database.types.DoubleValue;
import com.database.types.DataType;

import java.util.*;

/**
 * Handles aggregation functions like COUNT, SUM, AVG, MIN, MAX.
 */
public class AggregationOperator {
    
    public enum AggregateFunction {
        COUNT,
        SUM,
        AVG,
        MIN,
        MAX
    }
    
    /**
     * Performs aggregation on a list of rows.
     */
    public TypedValue performAggregation(List<Row> rows, String columnName, 
                                       AggregateFunction function) {
        switch (function) {
            case COUNT:
                return count(rows);
            case SUM:
                return sum(rows, columnName);
            case AVG:
                return average(rows, columnName);
            case MIN:
                return minimum(rows, columnName);
            case MAX:
                return maximum(rows, columnName);
            default:
                throw new IllegalArgumentException("Unsupported aggregation function: " + function);
        }
    }
    
    /**
     * Performs GROUP BY aggregation.
     */
    public Map<TypedValue, List<Row>> groupBy(List<Row> rows, String groupByColumn) {
        Map<TypedValue, List<Row>> groups = new HashMap<>();
        
        for (Row row : rows) {
            TypedValue groupKey = row.getValue(groupByColumn);
            if (groupKey == null) {
                groupKey = new NullValue(); // Handle null values
            }
            
            groups.computeIfAbsent(groupKey, k -> new ArrayList<>()).add(row);
        }
        
        return groups;
    }
    
    /**
     * Performs aggregation with GROUP BY.
     */
    public Map<TypedValue, TypedValue> performGroupByAggregation(List<Row> rows, 
                                                               String groupByColumn,
                                                               String aggregateColumn,
                                                               AggregateFunction function) {
        Map<TypedValue, List<Row>> groups = groupBy(rows, groupByColumn);
        Map<TypedValue, TypedValue> results = new HashMap<>();
        
        for (Map.Entry<TypedValue, List<Row>> entry : groups.entrySet()) {
            TypedValue groupKey = entry.getKey();
            List<Row> groupRows = entry.getValue();
            TypedValue aggregateResult = performAggregation(groupRows, aggregateColumn, function);
            results.put(groupKey, aggregateResult);
        }
        
        return results;
    }
    
    /**
     * COUNT aggregation function.
     */
    private TypedValue count(List<Row> rows) {
        return new IntegerValue(rows.size());
    }
    
    /**
     * SUM aggregation function.
     */
    private TypedValue sum(List<Row> rows, String columnName) {
        double sum = 0.0;
        int count = 0;
        
        for (Row row : rows) {
            TypedValue value = row.getValue(columnName);
            if (value != null) {
                if (value instanceof IntegerValue) {
                    sum += ((IntegerValue) value).intValue();
                    count++;
                } else if (value instanceof DoubleValue) {
                    sum += ((DoubleValue) value).doubleValue();
                    count++;
                }
            }
        }
        
        return count > 0 ? new DoubleValue(sum) : null;
    }
    
    /**
     * AVERAGE aggregation function.
     */
    private TypedValue average(List<Row> rows, String columnName) {
        double sum = 0.0;
        int count = 0;
        
        for (Row row : rows) {
            TypedValue value = row.getValue(columnName);
            if (value != null) {
                if (value instanceof IntegerValue) {
                    sum += ((IntegerValue) value).intValue();
                    count++;
                } else if (value instanceof DoubleValue) {
                    sum += ((DoubleValue) value).doubleValue();
                    count++;
                }
            }
        }
        
        return count > 0 ? new DoubleValue(sum / count) : null;
    }
    
    /**
     * MINIMUM aggregation function.
     */
    private TypedValue minimum(List<Row> rows, String columnName) {
        TypedValue min = null;
        
        for (Row row : rows) {
            TypedValue value = row.getValue(columnName);
            if (value != null) {
                if (min == null || compareValues(value, min) < 0) {
                    min = value;
                }
            }
        }
        
        return min;
    }
    
    /**
     * MAXIMUM aggregation function.
     */
    private TypedValue maximum(List<Row> rows, String columnName) {
        TypedValue max = null;
        
        for (Row row : rows) {
            TypedValue value = row.getValue(columnName);
            if (value != null) {
                if (max == null || compareValues(value, max) > 0) {
                    max = value;
                }
            }
        }
        
        return max;
    }
    
    /**
     * Compares two TypedValues for MIN/MAX operations.
     */
    private int compareValues(TypedValue v1, TypedValue v2) {
        if (v1 instanceof IntegerValue && v2 instanceof IntegerValue) {
            return Integer.compare(((IntegerValue) v1).intValue(), ((IntegerValue) v2).intValue());
        } else if (v1 instanceof DoubleValue && v2 instanceof DoubleValue) {
            return Double.compare(((DoubleValue) v1).doubleValue(), ((DoubleValue) v2).doubleValue());
        } else if (v1 instanceof IntegerValue && v2 instanceof DoubleValue) {
            return Double.compare(((IntegerValue) v1).intValue(), ((DoubleValue) v2).doubleValue());
        } else if (v1 instanceof DoubleValue && v2 instanceof IntegerValue) {
            return Double.compare(((DoubleValue) v1).doubleValue(), ((IntegerValue) v2).intValue());
        } else {
            // For string comparison or other types
            return v1.toString().compareTo(v2.toString());
        }
    }
    
    /**
     * Represents a null value for grouping purposes.
     */
    private static class NullValue extends TypedValue {
        public NullValue() {
            super(DataType.VARCHAR, null);
        }
        
        @Override
        public Object getValue() {
            return null;
        }
        
        @Override
        public boolean isValid() {
            return true;
        }
        
        @Override
        protected int compareValues(TypedValue other) {
            return 0; // All nulls are equal
        }
        
        @Override
        public String toString() {
            return "NULL";
        }
        
        @Override
        public boolean equals(Object obj) {
            return obj instanceof NullValue;
        }
        
        @Override
        public int hashCode() {
            return 0; // All null values have the same hash code
        }
    }
}
