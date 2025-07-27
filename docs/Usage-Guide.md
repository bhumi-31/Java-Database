# Database Engine Usage Guide

## Quick Start Tutorial

This guide will walk you through using the Custom In-Memory Database Engine, from basic setup to advanced features.

## Table of Contents

1. [Installation and Setup](#installation-and-setup)
2. [Basic Operations](#basic-operations)
3. [Schema Design](#schema-design)
4. [Indexing Strategy](#indexing-strategy)
5. [Advanced Features](#advanced-features)
6. [Performance Optimization](#performance-optimization)
7. [Best Practices](#best-practices)
8. [Troubleshooting](#troubleshooting)

## Installation and Setup

### Build from Source

```bash
# Clone the repository
git clone <repository-url>
cd java_database

# Build the project
.\build.bat  # Windows
# or
./build.sh   # Linux/macOS

# Run demo to verify installation
java -cp "target\classes" com.database.demo.DatabaseDemo
```

### Maven Integration

```xml
<dependency>
    <groupId>com.database</groupId>
    <artifactId>in-memory-database</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Basic Setup

```java
import com.database.core.*;
import com.database.types.*;

// Create database engine
DatabaseEngine engine = new DatabaseEngine();

// Configure memory (optional)
MemoryManager memMgr = engine.getMemoryManager();
memMgr.setMaxMemory(512 * 1024 * 1024); // 512MB limit
```

## Basic Operations

### Creating Tables

```java
// Define columns
List<Column> columns = Arrays.asList(
    new Column("id", DataType.INTEGER, false, true),      // Primary key
    new Column("name", DataType.VARCHAR, false, false),   // Required field
    new Column("age", DataType.INTEGER, true, false),     // Optional field
    new Column("email", DataType.VARCHAR, false, false),  // Required field
    new Column("salary", DataType.DOUBLE, true, false)    // Optional field
);

// Create schema
Schema userSchema = new Schema(columns);

// Create table
Table usersTable = engine.createTable("users", userSchema);
```

### Inserting Data

```java
// Create a new row
Row user1 = new Row();
user1.setValue("id", new IntegerValue(1));
user1.setValue("name", new VarcharValue("Alice Johnson"));
user1.setValue("age", new IntegerValue(28));
user1.setValue("email", new VarcharValue("alice@example.com"));
user1.setValue("salary", new DoubleValue(75000.0));

// Insert the row
usersTable.insert(user1);

// Batch insert for better performance
List<Row> batch = new ArrayList<>();
for (int i = 2; i <= 100; i++) {
    Row user = new Row();
    user.setValue("id", new IntegerValue(i));
    user.setValue("name", new VarcharValue("User " + i));
    user.setValue("age", new IntegerValue(20 + (i % 40)));
    user.setValue("email", new VarcharValue("user" + i + "@example.com"));
    batch.add(user);
}
usersTable.insertBatch(batch);
```

### Querying Data

```java
// Select all rows
List<Row> allUsers = usersTable.selectAll();
System.out.println("Total users: " + allUsers.size());

// Select by primary key (fastest)
Row specificUser = usersTable.selectByPrimaryKey(new IntegerValue(1));
if (specificUser != null) {
    String name = specificUser.getValue("name").toString();
    System.out.println("User 1: " + name);
}

// Select with conditions
List<Row> youngUsers = usersTable.selectWhere(row -> {
    TypedValue ageValue = row.getValue("age");
    return ageValue != null && ((IntegerValue) ageValue).getValue() < 30;
});

// Select specific columns only
List<Row> nameAndEmail = usersTable.selectColumns(Arrays.asList("name", "email"));
```

### Updating Data

```java
// Update by primary key
Row updatedUser = new Row();
updatedUser.setValue("id", new IntegerValue(1));
updatedUser.setValue("name", new VarcharValue("Alice Smith"));
updatedUser.setValue("age", new IntegerValue(29));
updatedUser.setValue("email", new VarcharValue("alice.smith@example.com"));
updatedUser.setValue("salary", new DoubleValue(80000.0));

boolean updated = usersTable.update(new IntegerValue(1), updatedUser);
System.out.println("Update successful: " + updated);

// Conditional updates
int updatedCount = usersTable.updateWhere(
    row -> {
        TypedValue salary = row.getValue("salary");
        return salary != null && ((DoubleValue) salary).getValue() < 50000.0;
    },
    row -> {
        // Give 10% raise to underpaid employees
        TypedValue currentSalary = row.getValue("salary");
        if (currentSalary != null) {
            double newSalary = ((DoubleValue) currentSalary).getValue() * 1.10;
            row.setValue("salary", new DoubleValue(newSalary));
        }
        return row;
    }
);
```

### Deleting Data

```java
// Delete by primary key
boolean deleted = usersTable.delete(new IntegerValue(1));
System.out.println("Deletion successful: " + deleted);

// Conditional deletion
int deletedCount = usersTable.deleteWhere(row -> {
    TypedValue age = row.getValue("age");
    return age != null && ((IntegerValue) age).getValue() > 65;
});
System.out.println("Deleted " + deletedCount + " retired users");
```

## Schema Design

### Data Types Guide

```java
// INTEGER: 32-bit signed integers (-2^31 to 2^31-1)
Column idColumn = new Column("id", DataType.INTEGER, false, true);

// DOUBLE: 64-bit floating point numbers
Column salaryColumn = new Column("salary", DataType.DOUBLE, true, false);

// VARCHAR: Variable length strings with optional max length
Column nameColumn = new Column("name", DataType.VARCHAR, false, false, 100);
Column descColumn = new Column("description", DataType.VARCHAR, true, false); // Default 255

// BOOLEAN: True/false values
Column activeColumn = new Column("active", DataType.BOOLEAN, false, false);
```

### Constraints and Validation

```java
// Primary Key Constraints
Column pkColumn = new Column("id", DataType.INTEGER, false, true); // NOT NULL, PRIMARY KEY

// NOT NULL Constraints
Column requiredColumn = new Column("email", DataType.VARCHAR, false, false); // NOT NULL

// Nullable Columns
Column optionalColumn = new Column("phone", DataType.VARCHAR, true, false); // NULLABLE

// Custom Validation (implement in application layer)
public boolean validateUser(Row user) {
    // Email format validation
    TypedValue email = user.getValue("email");
    if (email != null && !email.toString().contains("@")) {
        return false;
    }

    // Age range validation
    TypedValue age = user.getValue("age");
    if (age != null) {
        int ageValue = ((IntegerValue) age).getValue();
        if (ageValue < 0 || ageValue > 150) {
            return false;
        }
    }

    return true;
}
```

### Schema Evolution

```java
// Creating new table with additional columns
List<Column> enhancedColumns = new ArrayList<>(originalColumns);
enhancedColumns.add(new Column("created_date", DataType.VARCHAR, false, false));
enhancedColumns.add(new Column("last_login", DataType.VARCHAR, true, false));

Schema enhancedSchema = new Schema(enhancedColumns);
Table newUsersTable = engine.createTable("users_v2", enhancedSchema);

// Migrate data from old table to new table
List<Row> existingUsers = oldUsersTable.selectAll();
for (Row oldUser : existingUsers) {
    Row newUser = oldUser.copy();
    newUser.setValue("created_date", new VarcharValue("2024-01-01"));
    newUser.setValue("last_login", null); // Nullable field
    newUsersTable.insert(newUser);
}
```

## Indexing Strategy

### When to Use Indexes

```java
// Create indexes BEFORE loading large amounts of data
Table table = engine.createTable("employees", schema);

// Hash Index: For equality queries (=)
table.createIndex("email", IndexType.HASH);        // Fast lookups
table.createIndex("department", IndexType.HASH);   // Fast filtering

// B-Tree Index: For range queries (<, >, BETWEEN, ORDER BY)
table.createIndex("salary", IndexType.BTREE);      // Salary ranges
table.createIndex("hire_date", IndexType.BTREE);   // Date ranges
table.createIndex("age", IndexType.BTREE);         // Age ranges

// Primary key is automatically indexed (B-Tree)
```

### Index Performance Examples

```java
// Optimized queries (using indexes)
// Hash index lookup: O(1) average time
List<Row> engineeringDept = table.selectWhere(row ->
    row.getValue("department").equals(new VarcharValue("Engineering"))
);

// B-Tree range query: O(log n + k) time
List<Row> highEarners = table.selectWhere(row -> {
    TypedValue salary = row.getValue("salary");
    return salary != null && ((DoubleValue) salary).getValue() > 80000.0;
});

// Primary key lookup: O(log n) time
Row employee = table.selectByPrimaryKey(new IntegerValue(12345));
```

### Index Maintenance

```java
// Monitor index performance
List<IndexStats> stats = table.getIndexStatistics();
for (IndexStats stat : stats) {
    System.out.printf("Index: %s, Type: %s, Size: %d, Hit Rate: %.2f%%\n",
        stat.getColumn(), stat.getType(), stat.getSize(),
        (double) stat.getHitCount() / (stat.getHitCount() + stat.getMissCount()) * 100
    );
}

// Drop unused indexes to save memory
table.dropIndex("rarely_used_column");

// Recreate indexes for better performance
table.dropIndex("frequently_updated_column");
table.createIndex("frequently_updated_column", IndexType.HASH);
```

## Advanced Features

### Transaction Management

```java
import com.database.transaction.*;

TransactionManager txManager = new TransactionManager();

// Basic transaction
Transaction tx = txManager.beginTransaction();
try {
    // Perform multiple operations
    table.insert(newUser);
    table.update(existingUserId, updatedUser);
    table.delete(inactiveUserId);

    // Commit if all operations succeed
    txManager.commit(tx);
    System.out.println("Transaction committed successfully");
} catch (Exception e) {
    // Rollback on any error
    txManager.rollback(tx);
    System.err.println("Transaction rolled back: " + e.getMessage());
}

// Check transaction status
TransactionStatus status = txManager.getStatus(tx);
System.out.println("Transaction status: " + status);
```

### Join Operations

```java
import com.database.join.*;

// Create related tables
Table employeesTable = engine.createTable("employees", employeeSchema);
Table departmentsTable = engine.createTable("departments", departmentSchema);

// Populate with sample data
// ... insert employees and departments ...

// Perform joins
JoinOperator joinOp = new JoinOperator();

// INNER JOIN: Only employees with valid departments
List<Row> employeesWithDepts = joinOp.performJoin(
    employeesTable,
    departmentsTable,
    "department_id",    // employees.department_id
    "id",               // departments.id
    JoinType.INNER
);

// LEFT JOIN: All employees, even those without departments
List<Row> allEmployees = joinOp.performJoin(
    employeesTable,
    departmentsTable,
    "department_id",
    "id",
    JoinType.LEFT
);

// Process join results
for (Row row : employeesWithDepts) {
    String empName = row.getValue("name").toString();
    String deptName = row.getValue("department_name").toString();
    System.out.println(empName + " works in " + deptName);
}
```

### Aggregation Functions

```java
import com.database.aggregation.*;

AggregationOperator aggOp = new AggregationOperator();
List<Row> employees = employeesTable.selectAll();

// Simple aggregations
TypedValue totalEmployees = aggOp.count(employees);
TypedValue avgSalary = aggOp.average(employees, "salary");
TypedValue minAge = aggOp.minimum(employees, "age");
TypedValue maxAge = aggOp.maximum(employees, "age");

System.out.println("Total Employees: " + totalEmployees);
System.out.println("Average Salary: $" + avgSalary);
System.out.println("Age Range: " + minAge + " to " + maxAge);

// Group by operations
Map<TypedValue, List<Row>> byDepartment = aggOp.groupBy(employees, "department_id");
for (Map.Entry<TypedValue, List<Row>> entry : byDepartment.entrySet()) {
    String deptId = entry.getKey().toString();
    int count = entry.getValue().size();
    System.out.println("Department " + deptId + ": " + count + " employees");
}

// Group by with aggregation
Map<TypedValue, TypedValue> avgSalaryByDept = aggOp.groupByWithAggregation(
    employees, "department_id", "salary", AggregationType.AVERAGE
);

for (Map.Entry<TypedValue, TypedValue> entry : avgSalaryByDept.entrySet()) {
    String deptId = entry.getKey().toString();
    String avgSal = entry.getValue().toString();
    System.out.println("Department " + deptId + " avg salary: $" + avgSal);
}
```

## Performance Optimization

### Memory Management

```java
// Configure memory limits
MemoryManager memMgr = engine.getMemoryManager();
memMgr.setMaxMemory(1024 * 1024 * 1024); // 1GB limit

// Monitor memory usage
long used = memMgr.getUsedMemory();
long max = memMgr.getMaxMemory();
double percentage = memMgr.getUsagePercentage();

System.out.printf("Memory: %d MB / %d MB (%.1f%%)\n",
    used / 1024 / 1024, max / 1024 / 1024, percentage * 100);

// React to memory pressure
if (percentage > 0.8) {
    System.out.println("High memory usage detected!");

    // Clear caches, drop unused indexes, etc.
    table.dropIndex("rarely_used_column");

    // Force garbage collection
    memMgr.forceGarbageCollection();
}
```

### Query Optimization

```java
// Efficient: Use indexed columns in WHERE clauses
List<Row> results = table.selectWhere(row -> {
    // This will use the hash index on 'department'
    return row.getValue("department").equals(targetDepartment);
});

// Less efficient: Non-indexed column filtering
List<Row> results = table.selectWhere(row -> {
    // This requires full table scan
    return row.getValue("description").toString().contains("keyword");
});

// Optimize by creating appropriate indexes
table.createIndex("description", IndexType.HASH); // If using equality
// or
table.createIndex("description", IndexType.BTREE); // If using ranges/patterns
```

### Batch Operations

```java
// Efficient batch insertion
List<Row> largeDataset = loadDataFromFile();
int batchSize = 1000;

for (int i = 0; i < largeDataset.size(); i += batchSize) {
    int endIndex = Math.min(i + batchSize, largeDataset.size());
    List<Row> batch = largeDataset.subList(i, endIndex);
    table.insertBatch(batch);

    // Optional: Print progress
    System.out.printf("Inserted %d/%d records\n", endIndex, largeDataset.size());
}
```

## Best Practices

### Schema Design

1. **Choose Appropriate Data Types**

```java
// Good: Use specific types
Column ageColumn = new Column("age", DataType.INTEGER, true, false);
Column salaryColumn = new Column("salary", DataType.DOUBLE, true, false);

// Avoid: Everything as VARCHAR
Column badAgeColumn = new Column("age", DataType.VARCHAR, true, false); // Inefficient
```

2. **Primary Key Strategy**

```java
// Good: Auto-incrementing integer primary key
Column idColumn = new Column("id", DataType.INTEGER, false, true);

// Consider: Natural primary keys when appropriate
Column emailColumn = new Column("email", DataType.VARCHAR, false, true);
```

3. **Nullable vs Non-Nullable**

```java
// Required business data should be non-nullable
Column nameColumn = new Column("name", DataType.VARCHAR, false, false);
Column emailColumn = new Column("email", DataType.VARCHAR, false, false);

// Optional data can be nullable
Column phoneColumn = new Column("phone", DataType.VARCHAR, true, false);
Column middleNameColumn = new Column("middle_name", DataType.VARCHAR, true, false);
```

### Indexing Strategy

1. **Index Frequently Queried Columns**

```java
// If you often query by email
table.createIndex("email", IndexType.HASH);

// If you often query salary ranges
table.createIndex("salary", IndexType.BTREE);
```

2. **Don't Over-Index**

```java
// Good: Index columns used in WHERE clauses
table.createIndex("department", IndexType.HASH);
table.createIndex("hire_date", IndexType.BTREE);

// Avoid: Indexing every column (wastes memory)
```

3. **Choose Correct Index Type**

```java
// Hash for equality (=) queries
table.createIndex("status", IndexType.HASH);

// B-Tree for range (<, >, BETWEEN) and ordering
table.createIndex("created_date", IndexType.BTREE);
```

### Error Handling

```java
public void safeInsertUser(Row user) {
    try {
        // Validate before inserting
        if (!userSchema.validateRow(user)) {
            throw new IllegalArgumentException("Invalid user data");
        }

        table.insert(user);
        System.out.println("User inserted successfully");

    } catch (SchemaValidationException e) {
        System.err.println("Schema validation failed: " + e.getMessage());
    } catch (PrimaryKeyViolationException e) {
        System.err.println("User ID already exists: " + e.getMessage());
    } catch (OutOfMemoryException e) {
        System.err.println("Insufficient memory: " + e.getMessage());
        // Maybe clear caches or request more memory
    } catch (DatabaseException e) {
        System.err.println("Database error: " + e.getMessage());
    }
}
```

### Thread Safety

```java
// The engine is thread-safe, but consider synchronization for complex operations
private final Object tableLock = new Object();

public void atomicUserUpdate(int userId, String newEmail, double newSalary) {
    synchronized (tableLock) {
        Row user = table.selectByPrimaryKey(new IntegerValue(userId));
        if (user != null) {
            user.setValue("email", new VarcharValue(newEmail));
            user.setValue("salary", new DoubleValue(newSalary));
            table.update(new IntegerValue(userId), user);
        }
    }
}
```

## Troubleshooting

### Common Issues and Solutions

#### 1. OutOfMemoryException

**Problem**: Database runs out of memory

```java
// Symptoms
Exception in thread "main" com.database.exception.OutOfMemoryException:
    Memory limit exceeded: 512MB
```

**Solutions**:

```java
// Increase memory limit
MemoryManager memMgr = engine.getMemoryManager();
memMgr.setMaxMemory(1024 * 1024 * 1024); // 1GB

// Or optimize memory usage
table.dropIndex("unused_column");
memMgr.forceGarbageCollection();

// Or process data in smaller batches
int batchSize = 500; // Reduce from 1000
```

#### 2. Poor Query Performance

**Problem**: Queries are slow

```java
// Slow query example (takes 50ms for 10,000 records)
List<Row> results = table.selectWhere(row ->
    row.getValue("department").equals(searchValue)
);
```

**Solutions**:

```java
// Create appropriate index
table.createIndex("department", IndexType.HASH);
// Now same query takes 0.1ms

// Check index usage
List<IndexStats> stats = table.getIndexStatistics();
for (IndexStats stat : stats) {
    double hitRate = (double) stat.getHitCount() /
        (stat.getHitCount() + stat.getMissCount());
    if (hitRate < 0.5) {
        System.out.println("Low index hit rate for: " + stat.getColumn());
    }
}
```

#### 3. SchemaValidationException

**Problem**: Row doesn't match table schema

```java
// Error example
Row user = new Row();
user.setValue("id", new IntegerValue(1));
user.setValue("invalid_column", new VarcharValue("test")); // Column doesn't exist
table.insert(user); // Throws SchemaValidationException
```

**Solution**:

```java
// Validate before inserting
if (schema.validateRow(user)) {
    table.insert(user);
} else {
    System.err.println("Row validation failed");

    // Check for missing required columns
    for (Column column : schema.getColumns()) {
        if (!column.isNullable() && !user.hasValue(column.getName())) {
            System.err.println("Missing required column: " + column.getName());
        }
    }
}
```

#### 4. PrimaryKeyViolationException

**Problem**: Duplicate primary key values

```java
// Error: Trying to insert duplicate ID
Row user1 = createUser(1, "Alice");
Row user2 = createUser(1, "Bob"); // Same ID!

table.insert(user1); // Success
table.insert(user2); // Throws PrimaryKeyViolationException
```

**Solutions**:

```java
// Check if key exists before inserting
TypedValue userId = new IntegerValue(1);
if (table.selectByPrimaryKey(userId) == null) {
    table.insert(user);
} else {
    // Update existing user instead
    table.update(userId, user);
}

// Or use auto-incrementing IDs
private int nextUserId = 1;
public Row createUserWithAutoId(String name) {
    Row user = new Row();
    user.setValue("id", new IntegerValue(nextUserId++));
    user.setValue("name", new VarcharValue(name));
    return user;
}
```

### Performance Monitoring

```java
public void monitorDatabasePerformance() {
    // Memory monitoring
    MemoryManager memMgr = engine.getMemoryManager();
    System.out.printf("Memory Usage: %.1f%%\n",
        memMgr.getUsagePercentage() * 100);

    // Index monitoring
    List<IndexStats> stats = table.getIndexStatistics();
    for (IndexStats stat : stats) {
        long totalQueries = stat.getHitCount() + stat.getMissCount();
        if (totalQueries > 0) {
            double hitRate = (double) stat.getHitCount() / totalQueries;
            System.out.printf("Index %s hit rate: %.1f%%\n",
                stat.getColumn(), hitRate * 100);
        }
    }

    // Table statistics
    int rowCount = table.selectAll().size();
    System.out.println("Total rows: " + rowCount);
}
```

### Debugging Tips

1. **Enable Verbose Logging**

```java
// Add logging to track operations
public void insertWithLogging(Row row) {
    System.out.println("Inserting row: " + row);
    long startTime = System.currentTimeMillis();

    table.insert(row);

    long endTime = System.currentTimeMillis();
    System.out.println("Insert took: " + (endTime - startTime) + "ms");
}
```

2. **Validate Data Integrity**

```java
public void validateTableIntegrity() {
    List<Row> allRows = table.selectAll();
    Schema schema = table.getSchema();

    for (Row row : allRows) {
        if (!schema.validateRow(row)) {
            System.err.println("Invalid row found: " + row);
        }
    }

    // Check for primary key uniqueness
    Set<TypedValue> seenKeys = new HashSet<>();
    String pkColumn = schema.getPrimaryKeyColumn();

    for (Row row : allRows) {
        TypedValue pk = row.getValue(pkColumn);
        if (seenKeys.contains(pk)) {
            System.err.println("Duplicate primary key: " + pk);
        }
        seenKeys.add(pk);
    }
}
```

This comprehensive usage guide covers all aspects of working with the Custom In-Memory Database Engine, from basic operations to advanced troubleshooting techniques.
