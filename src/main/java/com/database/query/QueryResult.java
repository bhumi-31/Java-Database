package com.database.query;

import com.database.core.Row;
import com.database.core.Schema;
import com.database.core.Column;
import com.database.types.TypedValue;

import java.util.List;

/**
 * Represents the result of a SQL query execution.
 */
public class QueryResult {
    private final List<Row> rows;
    private final Schema schema;
    
    public QueryResult(List<Row> rows, Schema schema) {
        this.rows = rows;
        this.schema = schema;
    }
    
    public List<Row> getRows() {
        return rows;
    }
    
    public Schema getSchema() {
        return schema;
    }
    
    public int getRowCount() {
        return rows.size();
    }
    
    public boolean isEmpty() {
        return rows.isEmpty();
    }
    
    /**
     * Formats the query result as a table for display.
     */
    public String formatAsTable() {
        if (rows.isEmpty()) {
            return "No results found.";
        }
        
        StringBuilder result = new StringBuilder();
        
        // Calculate column widths
        int[] columnWidths = calculateColumnWidths();
        
        // Print header
        printSeparator(result, columnWidths);
        printRow(result, getColumnNames(), columnWidths);
        printSeparator(result, columnWidths);
        
        // Print data rows
        for (Row row : rows) {
            String[] values = new String[schema.getColumns().size()];
            for (int i = 0; i < schema.getColumns().size(); i++) {
                Column column = schema.getColumns().get(i);
                TypedValue value = row.getValue(column.getName());
                values[i] = value != null ? value.toString() : "NULL";
            }
            printRow(result, values, columnWidths);
        }
        
        printSeparator(result, columnWidths);
        result.append("Total rows: ").append(rows.size()).append("\n");
        
        return result.toString();
    }
    
    private String[] getColumnNames() {
        return schema.getColumns().stream()
                .map(Column::getName)
                .toArray(String[]::new);
    }
    
    private int[] calculateColumnWidths() {
        int[] widths = new int[schema.getColumns().size()];
        
        // Initialize with column name lengths
        for (int i = 0; i < schema.getColumns().size(); i++) {
            widths[i] = schema.getColumns().get(i).getName().length();
        }
        
        // Check data lengths
        for (Row row : rows) {
            for (int i = 0; i < schema.getColumns().size(); i++) {
                Column column = schema.getColumns().get(i);
                TypedValue value = row.getValue(column.getName());
                String valueStr = value != null ? value.toString() : "NULL";
                widths[i] = Math.max(widths[i], valueStr.length());
            }
        }
        
        // Minimum width of 3
        for (int i = 0; i < widths.length; i++) {
            widths[i] = Math.max(widths[i], 3);
        }
        
        return widths;
    }
    
    private void printSeparator(StringBuilder result, int[] columnWidths) {
        result.append("+");
        for (int width : columnWidths) {
            for (int i = 0; i < width + 2; i++) {
                result.append("-");
            }
            result.append("+");
        }
        result.append("\n");
    }
    
    private void printRow(StringBuilder result, String[] values, int[] columnWidths) {
        result.append("|");
        for (int i = 0; i < values.length; i++) {
            result.append(" ");
            result.append(String.format("%-" + columnWidths[i] + "s", values[i]));
            result.append(" |");
        }
        result.append("\n");
    }
    
    @Override
    public String toString() {
        return "QueryResult{" +
                "rowCount=" + rows.size() +
                ", columns=" + schema.getColumns().size() +
                '}';
    }
}
