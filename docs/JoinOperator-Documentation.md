# JoinOperator Documentation

## Overview

The `JoinOperator` class provides comprehensive SQL JOIN operations for the custom in-memory database engine. It supports all standard SQL join types and handles complex table relationships with proper null value management and schema merging.

## Class Structure

```java
package com.database.join;

import com.database.core.Row;
import com.database.core.Schema;
import com.database.core.Table;
import com.database.core.Column;
import com.database.types.TypedValue;
```

## Join Types

The `JoinOperator` supports four standard SQL join types through the `JoinType` enumeration:

### 1. INNER JOIN

- **Purpose**: Returns only rows that have matching values in both tables
- **Use Case**: Finding records that exist in both tables with matching join keys
- **Null Handling**: Excludes rows with null values in the join columns

### 2. LEFT JOIN (LEFT OUTER JOIN)

- **Purpose**: Returns all rows from the left table, plus matching rows from the right table
- **Use Case**: Preserving all records from the primary table while adding optional related data
- **Null Handling**: Fills right table columns with null values when no match exists

### 3. RIGHT JOIN (RIGHT OUTER JOIN)

- **Purpose**: Returns all rows from the right table, plus matching rows from the left table
- **Use Case**: Preserving all records from the secondary table while adding optional related data
- **Null Handling**: Fills left table columns with null values when no match exists

### 4. FULL OUTER JOIN

- **Purpose**: Returns all rows from both tables, matching where possible
- **Use Case**: Complete data analysis requiring all records from both tables
- **Null Handling**: Fills missing columns with null values for unmatched rows

## Core Methods

### performJoin()

**Signature:**

```java
public List<Row> performJoin(Table leftTable, Table rightTable,
                            String leftColumn, String rightColumn,
                            JoinType joinType)
```

**Parameters:**

- `leftTable`: The primary table in the join operation
- `rightTable`: The secondary table in the join operation
- `leftColumn`: Column name in the left table to join on
- `rightColumn`: Column name in the right table to join on
- `joinType`: Type of join operation to perform

**Returns:** List of joined rows containing combined data from both tables

**Example Usage:**

```java
JoinOperator joinOp = new JoinOperator();
List<Row> results = joinOp.performJoin(
    employeesTable,
    departmentsTable,
    "department_id",
    "id",
    JoinType.INNER
);
```

### createJoinedSchema()

**Signature:**

```java
public Schema createJoinedSchema(Schema leftSchema, Schema rightSchema)
```

**Purpose:** Creates a new schema that combines columns from both input schemas for the joined result set.

**Schema Merging Rules:**

- All columns from left table are included first
- All columns from right table are included second
- Primary key constraints are removed (set to false) in the result schema
- Column nullability is preserved from original schemas
- Column names are preserved (no prefixing in current implementation)

## Implementation Details

### Join Algorithm

The join operations use a nested loop algorithm:

1. **Outer Loop**: Iterates through rows of the primary table
2. **Inner Loop**: For each primary row, searches through the secondary table
3. **Matching Logic**: Compares join column values using `TypedValue.equals()`
4. **Result Construction**: Creates new rows combining data from matched pairs

### Performance Characteristics

- **Time Complexity**: O(n × m) where n and m are table sizes
- **Space Complexity**: O(result_size) for storing joined rows
- **Memory Usage**: Creates new Row objects for each result

### Null Value Handling

The implementation follows SQL standard null handling:

- **Comparison**: `null == null` is false (SQL three-valued logic)
- **Join Behavior**: Null values in join columns never match anything
- **Result Nulls**: Missing data is represented as Java `null` values

### Row Construction Methods

#### createJoinedRow()

```java
private Row createJoinedRow(Row leftRow, Row rightRow, Schema leftSchema, Schema rightSchema)
```

- Combines actual data from both matched rows
- Preserves all column values from both tables
- Used for successful join matches

#### createJoinedRowWithNulls()

```java
private Row createJoinedRowWithNulls(Row sourceRow, Schema leftSchema, Schema rightSchema, boolean isLeft)
```

- Creates rows for unmatched records in outer joins
- Fills missing table columns with null values
- `isLeft` parameter determines which table provided the source data

## Usage Examples

### Basic Inner Join

```java
// Join employees with their departments
JoinOperator joinOp = new JoinOperator();
List<Row> employeeDepts = joinOp.performJoin(
    employeesTable,
    departmentsTable,
    "dept_id",
    "id",
    JoinType.INNER
);

// Result contains only employees with valid departments
for (Row row : employeeDepts) {
    String empName = row.getValue("name").toString();
    String deptName = row.getValue("department_name").toString();
    System.out.println(empName + " works in " + deptName);
}
```

### Left Join for Optional Data

```java
// Include all employees, even those without department assignments
List<Row> allEmployees = joinOp.performJoin(
    employeesTable,
    departmentsTable,
    "dept_id",
    "id",
    JoinType.LEFT
);

for (Row row : allEmployees) {
    String empName = row.getValue("name").toString();
    TypedValue deptValue = row.getValue("department_name");
    String deptName = (deptValue != null) ? deptValue.toString() : "Unassigned";
    System.out.println(empName + " - " + deptName);
}
```

### Full Outer Join for Complete Analysis

```java
// Analyze all employees and all departments
List<Row> completeView = joinOp.performJoin(
    employeesTable,
    departmentsTable,
    "dept_id",
    "id",
    JoinType.FULL
);

// Results include:
// - Employees with departments
// - Employees without departments (dept columns = null)
// - Departments without employees (emp columns = null)
```

## Error Handling

### Common Exceptions

1. **IllegalArgumentException**:

   - Thrown for unsupported join types
   - Thrown for invalid column names

2. **NullPointerException**:
   - Can occur if table parameters are null
   - Can occur if column names don't exist in tables

### Best Practices

```java
// Validate inputs before joining
if (leftTable == null || rightTable == null) {
    throw new IllegalArgumentException("Tables cannot be null");
}

if (!leftTable.getSchema().hasColumn(leftColumn)) {
    throw new IllegalArgumentException("Left column not found: " + leftColumn);
}

if (!rightTable.getSchema().hasColumn(rightColumn)) {
    throw new IllegalArgumentException("Right column not found: " + rightColumn);
}
```

## Performance Optimization Tips

### Index Usage

- Ensure join columns are indexed for better performance
- Primary key joins are automatically optimized
- Hash indexes work best for equality joins

### Memory Management

```java
// For large result sets, consider streaming processing
List<Row> results = joinOp.performJoin(table1, table2, "id", "ref_id", JoinType.INNER);
// Process results in batches to manage memory
```

### Join Order Optimization

```java
// Place smaller table as the left table when possible
// This reduces the number of iterations in the outer loop
if (table1.getRowCount() > table2.getRowCount()) {
    // Swap tables and adjust join type if needed
    results = joinOp.performJoin(table2, table1, rightCol, leftCol, joinType);
}
```

## Integration with Query Engine

The `JoinOperator` integrates seamlessly with the SQL query processing engine:

```java
// Example integration in query executor
public class QueryExecutor {
    private JoinOperator joinOperator = new JoinOperator();

    public List<Row> executeJoinQuery(JoinStatement joinStmt) {
        Table leftTable = getTable(joinStmt.getLeftTable());
        Table rightTable = getTable(joinStmt.getRightTable());

        return joinOperator.performJoin(
            leftTable,
            rightTable,
            joinStmt.getLeftColumn(),
            joinStmt.getRightColumn(),
            joinStmt.getJoinType()
        );
    }
}
```

## Future Enhancements

### Planned Improvements

1. **Multi-Column Joins**: Support for joining on multiple columns
2. **Index-Optimized Joins**: Use indexes to improve join performance
3. **Streaming Joins**: Process large datasets without loading all into memory
4. **Join Caching**: Cache frequently used join results
5. **Statistical Optimization**: Choose optimal join algorithms based on table statistics

### Advanced Join Types

Future versions may include:

- **CROSS JOIN**: Cartesian product of two tables
- **NATURAL JOIN**: Automatic joining on columns with same names
- **SEMI JOIN**: Return rows from left table that have matches in right table
- **ANTI JOIN**: Return rows from left table that don't have matches in right table

## Testing and Validation

### Unit Test Examples

```java
@Test
public void testInnerJoin() {
    JoinOperator joinOp = new JoinOperator();

    // Create test tables with sample data
    Table employees = createEmployeesTable();
    Table departments = createDepartmentsTable();

    // Perform inner join
    List<Row> results = joinOp.performJoin(
        employees, departments, "dept_id", "id", JoinType.INNER
    );

    // Validate results
    assertEquals(3, results.size()); // Expected number of matches

    for (Row row : results) {
        assertNotNull(row.getValue("employee_name"));
        assertNotNull(row.getValue("department_name"));
    }
}
```

### Performance Testing

```java
@Test
public void testJoinPerformance() {
    // Create large test datasets
    Table largeTable1 = createLargeTable(10000);
    Table largeTable2 = createLargeTable(5000);

    long startTime = System.currentTimeMillis();

    List<Row> results = joinOperator.performJoin(
        largeTable1, largeTable2, "id", "ref_id", JoinType.INNER
    );

    long endTime = System.currentTimeMillis();
    long duration = endTime - startTime;

    // Performance should be reasonable for dataset size
    assertTrue("Join took too long: " + duration + "ms", duration < 1000);
}
```

## Conclusion

The `JoinOperator` class provides a robust, SQL-compliant implementation of table join operations. It handles all standard join types with proper null value semantics and schema management. The implementation is designed for clarity and correctness, with performance optimizations planned for future releases.

For integration into larger database systems, the class provides clean interfaces and follows established database design patterns. The comprehensive error handling and validation make it suitable for production use in database engines.
