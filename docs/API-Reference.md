# Database Engine API Reference

## Overview

This document provides comprehensive API documentation for the Custom In-Memory Database Engine. The API is designed to be intuitive, type-safe, and performant.

## Core API Classes

### DatabaseEngine

The main entry point for database operations.

#### Constructor

```java
public DatabaseEngine()
public DatabaseEngine(MemoryManager memoryManager)
```

#### Methods

##### createTable()

```java
public Table createTable(String tableName, Schema schema)
```

**Description**: Creates a new table with the specified schema.

**Parameters**:

- `tableName`: Unique name for the table
- `schema`: Schema definition with columns and constraints

**Returns**: `Table` instance for data operations

**Throws**: `IllegalArgumentException` if table name exists or schema is invalid

**Example**:

```java
List<Column> columns = Arrays.asList(
    new Column("id", DataType.INTEGER, false, true),
    new Column("name", DataType.VARCHAR, false, false)
);
Schema schema = new Schema(columns);
Table users = engine.createTable("users", schema);
```

##### getTable()

```java
public Table getTable(String tableName)
```

**Description**: Retrieves an existing table by name.

**Parameters**:

- `tableName`: Name of the table to retrieve

**Returns**: `Table` instance or `null` if not found

##### dropTable()

```java
public boolean dropTable(String tableName)
```

**Description**: Removes a table and all its data.

**Parameters**:

- `tableName`: Name of the table to drop

**Returns**: `true` if table was dropped, `false` if not found

---

### Table

Represents a database table with rows, schema, and indexes.

#### Core Operations

##### insert()

```java
public void insert(Row row)
public void insertBatch(List<Row> rows)
```

**Description**: Inserts one or more rows into the table.

**Parameters**:

- `row`: Row to insert (must conform to table schema)
- `rows`: List of rows for batch insertion

**Throws**:

- `SchemaValidationException`: If row doesn't match schema
- `PrimaryKeyViolationException`: If primary key constraint violated

**Example**:

```java
Row user = new Row();
user.setValue("id", new IntegerValue(1));
user.setValue("name", new VarcharValue("Alice"));
table.insert(user);
```

##### update()

```java
public boolean update(TypedValue primaryKey, Row newRow)
public int updateWhere(Predicate<Row> condition, Function<Row, Row> updater)
```

**Description**: Updates existing rows in the table.

**Parameters**:

- `primaryKey`: Primary key of row to update
- `newRow`: New row data (must conform to schema)
- `condition`: Predicate for conditional updates
- `updater`: Function to transform matching rows

**Returns**: `true` if row was updated, or count of updated rows

##### delete()

```java
public boolean delete(TypedValue primaryKey)
public int deleteWhere(Predicate<Row> condition)
```

**Description**: Deletes rows from the table.

**Parameters**:

- `primaryKey`: Primary key of row to delete
- `condition`: Predicate for conditional deletion

**Returns**: `true` if row was deleted, or count of deleted rows

##### select()

```java
public List<Row> selectAll()
public Row selectByPrimaryKey(TypedValue key)
public List<Row> selectWhere(Predicate<Row> condition)
public List<Row> selectColumns(List<String> columns)
```

**Description**: Queries data from the table.

**Parameters**:

- `key`: Primary key value for single row lookup
- `condition`: Predicate for filtering rows
- `columns`: List of column names to retrieve

**Returns**: List of matching rows or single row

#### Index Management

##### createIndex()

```java
public void createIndex(String columnName, IndexType type)
```

**Description**: Creates an index on the specified column.

**Parameters**:

- `columnName`: Column to index
- `type`: `IndexType.BTREE` for range queries, `IndexType.HASH` for equality

##### dropIndex()

```java
public boolean dropIndex(String columnName)
```

**Description**: Removes an index from the column.

##### getIndexStatistics()

```java
public List<IndexStats> getIndexStatistics()
```

**Description**: Returns statistics for all indexes on the table.

---

### Schema

Defines the structure and constraints of a table.

#### Constructor

```java
public Schema(List<Column> columns)
```

#### Methods

##### validateRow()

```java
public boolean validateRow(Row row)
```

**Description**: Validates that a row conforms to this schema.

##### getColumn()

```java
public Column getColumn(String name)
```

**Description**: Gets column definition by name (case-insensitive).

##### hasColumn()

```java
public boolean hasColumn(String name)
```

**Description**: Checks if a column exists in the schema.

##### getColumns()

```java
public List<Column> getColumns()
```

**Description**: Returns all columns in the schema.

##### getPrimaryKeyColumn()

```java
public String getPrimaryKeyColumn()
```

**Description**: Returns the primary key column name, or null if none.

---

### Column

Defines a single column in a table schema.

#### Constructors

```java
public Column(String name, DataType type)
public Column(String name, DataType type, boolean nullable, boolean primaryKey)
public Column(String name, DataType type, boolean nullable, boolean primaryKey, int maxLength)
```

#### Methods

##### getName()

```java
public String getName()
```

##### getType()

```java
public DataType getType()
```

##### isNullable()

```java
public boolean isNullable()
```

##### isPrimaryKey()

```java
public boolean isPrimaryKey()
```

##### getMaxLength()

```java
public int getMaxLength()
```

---

### Row

Represents a single record in a table.

#### Constructors

```java
public Row()
public Row(int rowId)
```

#### Methods

##### setValue()

```java
public void setValue(String columnName, TypedValue value)
```

**Description**: Sets the value for a column.

##### getValue()

```java
public TypedValue getValue(String columnName)
```

**Description**: Gets the value for a column.

##### hasValue()

```java
public boolean hasValue(String columnName)
```

**Description**: Checks if a column has a value (not null).

##### copy()

```java
public Row copy()
public Row deepCopy()
```

**Description**: Creates a shallow or deep copy of the row.

---

## Data Types

### DataType Enumeration

```java
public enum DataType {
    INTEGER,    // 32-bit signed integers
    DOUBLE,     // 64-bit floating point
    VARCHAR,    // Variable length strings
    BOOLEAN     // True/false values
}
```

### TypedValue Hierarchy

Abstract base class for all data values.

#### IntegerValue

```java
public IntegerValue(Integer value)
public Integer getValue()
```

#### DoubleValue

```java
public DoubleValue(Double value)
public Double getValue()
```

#### VarcharValue

```java
public VarcharValue(String value)
public VarcharValue(String value, int maxLength)
public String getValue()
public int getMaxLength()
```

#### BooleanValue

```java
public BooleanValue(Boolean value)
public Boolean getValue()
```

## Advanced Features

### Transaction Management

#### TransactionManager

```java
public class TransactionManager {
    public Transaction beginTransaction()
    public void commit(Transaction transaction)
    public void rollback(Transaction transaction)
    public TransactionStatus getStatus(Transaction transaction)
}
```

#### Transaction

```java
public class Transaction {
    public long getId()
    public TransactionStatus getStatus()
    public List<TransactionOperation> getOperations()
    public void addOperation(TransactionOperation operation)
}
```

#### Usage Example

```java
TransactionManager txManager = new TransactionManager();
Transaction tx = txManager.beginTransaction();

try {
    // Perform database operations
    table.insert(newRow);
    table.update(key, updatedRow);

    // Commit if all operations succeed
    txManager.commit(tx);
} catch (Exception e) {
    // Rollback on any error
    txManager.rollback(tx);
    throw e;
}
```

### Join Operations

#### JoinOperator

```java
public class JoinOperator {
    public List<Row> performJoin(Table leftTable, Table rightTable,
                                String leftColumn, String rightColumn,
                                JoinType joinType)

    public Schema createJoinedSchema(Schema leftSchema, Schema rightSchema)
}
```

#### JoinType Enumeration

```java
public enum JoinType {
    INNER,      // Only matching rows
    LEFT,       // All left rows + matching right rows
    RIGHT,      // All right rows + matching left rows
    FULL        // All rows from both tables
}
```

#### Usage Example

```java
JoinOperator joinOp = new JoinOperator();

// Join employees with departments
List<Row> results = joinOp.performJoin(
    employeesTable,
    departmentsTable,
    "dept_id",        // employees.dept_id
    "id",             // departments.id
    JoinType.INNER
);

// Create schema for joined results
Schema joinedSchema = joinOp.createJoinedSchema(
    employeesTable.getSchema(),
    departmentsTable.getSchema()
);
```

### Aggregation Functions

#### AggregationOperator

```java
public class AggregationOperator {
    // Simple aggregations
    public TypedValue count(List<Row> rows)
    public TypedValue average(List<Row> rows, String columnName)
    public TypedValue minimum(List<Row> rows, String columnName)
    public TypedValue maximum(List<Row> rows, String columnName)

    // Group by operations
    public Map<TypedValue, List<Row>> groupBy(List<Row> rows, String columnName)
    public Map<TypedValue, TypedValue> groupByWithAggregation(
        List<Row> rows, String groupColumn, String aggregateColumn, AggregationType type)
}
```

#### AggregationType Enumeration

```java
public enum AggregationType {
    COUNT,
    AVERAGE,
    MINIMUM,
    MAXIMUM
}
```

#### Usage Example

```java
AggregationOperator aggOp = new AggregationOperator();
List<Row> employees = table.selectAll();

// Simple aggregations
TypedValue totalCount = aggOp.count(employees);
TypedValue avgSalary = aggOp.average(employees, "salary");
TypedValue minAge = aggOp.minimum(employees, "age");
TypedValue maxAge = aggOp.maximum(employees, "age");

// Group by department
Map<TypedValue, List<Row>> byDept = aggOp.groupBy(employees, "department");

// Average salary by department
Map<TypedValue, TypedValue> avgSalaryByDept = aggOp.groupByWithAggregation(
    employees, "department", "salary", AggregationType.AVERAGE
);
```

## Index Management

### IndexManager

```java
public class IndexManager {
    public void createIndex(String column, IndexType type, boolean isPrimaryKey)
    public boolean dropIndex(String column)
    public boolean hasIndex(String column)
    public IndexType getIndexType(String column)
    public List<String> getIndexedColumns()
    public List<IndexStats> getStatistics()
}
```

### IndexType Enumeration

```java
public enum IndexType {
    BTREE,      // Balanced tree for range queries
    HASH        // Hash table for equality lookups
}
```

### IndexStats

```java
public class IndexStats {
    public String getColumn()
    public IndexType getType()
    public int getSize()
    public boolean isPrimaryKey()
    public long getHitCount()
    public long getMissCount()
}
```

## Memory Management

### MemoryManager

```java
public class MemoryManager {
    public void setMaxMemory(long maxBytes)
    public long getMaxMemory()
    public long getUsedMemory()
    public double getUsagePercentage()
    public void forceGarbageCollection()
    public MemoryStats getStatistics()
}
```

### Usage Example

```java
MemoryManager memMgr = engine.getMemoryManager();

// Set 1GB memory limit
memMgr.setMaxMemory(1024 * 1024 * 1024);

// Monitor usage
if (memMgr.getUsagePercentage() > 0.8) {
    System.out.println("Memory usage high: " + memMgr.getUsagePercentage() + "%");
    memMgr.forceGarbageCollection();
}
```

## Error Handling

### Exception Hierarchy

```java
// Base exception for all database errors
public class DatabaseException extends RuntimeException

// Schema and validation errors
public class SchemaValidationException extends DatabaseException
public class PrimaryKeyViolationException extends DatabaseException
public class ColumnNotFoundException extends DatabaseException

// Memory and resource errors
public class OutOfMemoryException extends DatabaseException
public class TableNotFoundException extends DatabaseException

// Transaction errors
public class TransactionException extends DatabaseException
public class TransactionConflictException extends TransactionException
```

### Usage Example

```java
try {
    Row row = new Row();
    row.setValue("invalid_column", new IntegerValue(123));
    table.insert(row);
} catch (SchemaValidationException e) {
    System.err.println("Schema validation failed: " + e.getMessage());
} catch (ColumnNotFoundException e) {
    System.err.println("Column not found: " + e.getMessage());
} catch (DatabaseException e) {
    System.err.println("Database error: " + e.getMessage());
}
```

## Performance Guidelines

### Best Practices

1. **Index Creation**

   ```java
   // Create indexes before bulk data loading
   table.createIndex("frequently_queried_column", IndexType.HASH);
   table.createIndex("range_query_column", IndexType.BTREE);
   ```

2. **Batch Operations**

   ```java
   // Use batch inserts for better performance
   List<Row> batch = new ArrayList<>();
   for (Data data : largeDataset) {
       batch.add(createRow(data));
       if (batch.size() >= 1000) {
           table.insertBatch(batch);
           batch.clear();
       }
   }
   ```

3. **Memory Monitoring**

   ```java
   // Monitor memory usage in long-running applications
   MemoryManager memMgr = engine.getMemoryManager();
   if (memMgr.getUsagePercentage() > 0.9) {
       // Take action: clear caches, optimize queries, etc.
   }
   ```

4. **Query Optimization**

   ```java
   // Use primary key lookups when possible
   Row user = table.selectByPrimaryKey(new IntegerValue(userId));

   // Prefer indexed columns in WHERE conditions
   List<Row> results = table.selectWhere(row ->
       row.getValue("indexed_column").equals(searchValue)
   );
   ```

### Performance Characteristics

| Operation                | Time Complexity     | Space Complexity |
| ------------------------ | ------------------- | ---------------- |
| Insert                   | O(log n) with index | O(1)             |
| Update by PK             | O(log n)            | O(1)             |
| Delete by PK             | O(log n)            | O(1)             |
| Select by PK             | O(log n)            | O(1)             |
| Select with Hash Index   | O(1) average        | O(1)             |
| Select with B-Tree Index | O(log n)            | O(1)             |
| Range Query (B-Tree)     | O(log n + k)        | O(k)             |
| Join (nested loop)       | O(n × m)            | O(result)        |
| Group By                 | O(n log n)          | O(n)             |

## Integration Examples

### Spring Boot Integration

```java
@Configuration
public class DatabaseConfig {

    @Bean
    public DatabaseEngine databaseEngine() {
        MemoryManager memMgr = new MemoryManager();
        memMgr.setMaxMemory(512 * 1024 * 1024); // 512MB
        return new DatabaseEngine(memMgr);
    }

    @Bean
    public UserRepository userRepository(DatabaseEngine engine) {
        return new UserRepository(engine);
    }
}

@Repository
public class UserRepository {
    private final Table usersTable;

    public UserRepository(DatabaseEngine engine) {
        Schema schema = createUserSchema();
        this.usersTable = engine.createTable("users", schema);

        // Create indexes for common queries
        usersTable.createIndex("email", IndexType.HASH);
        usersTable.createIndex("created_date", IndexType.BTREE);
    }

    public void save(User user) {
        Row row = convertToRow(user);
        usersTable.insert(row);
    }

    public User findByEmail(String email) {
        List<Row> results = usersTable.selectWhere(row ->
            row.getValue("email").equals(new VarcharValue(email))
        );
        return results.isEmpty() ? null : convertToUser(results.get(0));
    }
}
```

### Web Service Integration

```java
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        try {
            userRepository.save(user);
            return ResponseEntity.ok(user);
        } catch (SchemaValidationException e) {
            return ResponseEntity.badRequest().build();
        } catch (PrimaryKeyViolationException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUser(@PathVariable Long id) {
        User user = userRepository.findById(id);
        return user != null ? ResponseEntity.ok(user) : ResponseEntity.notFound().build();
    }
}
```

This comprehensive API reference provides detailed information for integrating and using the Custom In-Memory Database Engine in various applications and frameworks.
