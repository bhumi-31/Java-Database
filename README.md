# Custom Database Engine

A lightweight, file-based database engine built in Java with SQL-like command interface.

## 🚀 Features

- **CRUD Operations**: Create, Read, Update, Delete records
- **File Persistence**: Data automatically saved to disk and loaded on restart
- **Interactive CLI**: SQL-like command interface
- **Table Management**: Create and manage multiple tables
- **Query System**: Basic WHERE clause support
- **Data Types**: Automatic handling of strings and integers

## 🏃‍♂️ How to Run

### Prerequisites
- Java 23+ installed
- Maven 3.6+ installed

### Compilation & Execution
```bash
# Compile the project
mvn clean compile

# Run the database engine
java -cp target/classes com.database.engine.DatabaseEngineApp


## 🎮 Detailed Usage Examples

### Example 1: Student Management System
```sql
-- Create students table
CREATE TABLE students

-- Add student records
INSERT INTO students name=Rahul age=20 course=CSE branch=AI
INSERT INTO students name=Priya age=19 course=ECE branch=VLSI
INSERT INTO students name=Arjun age=21 course=CSE branch=AI

-- View all students
SELECT * FROM students

-- Find all CSE students
SELECT * FROM students WHERE course=CSE

-- Find specific student
SELECT * FROM students WHERE name=Rahul


### Example 2: Employee Database
CREATE TABLE employees
INSERT INTO employees name=John salary=50000 dept=IT
INSERT INTO employees name=Sarah salary=60000 dept=HR

SELECT * FROM employees WHERE dept=IT


## Sample Output
=== Custom Database Engine ===
Type 'help' for commands or 'exit' to quit

db> CREATE TABLE students
Table 'students' created successfully.

db> INSERT INTO students name=Bhumika age=21 course=CS
Record inserted with ID: 1
Data inserted: {name=Bhumika, course=CS, age=21}

db> SELECT * FROM students
Results:
Record{id=1, data={name=Bhumika, course=CS, age=21}}
Total records: 1


##  Project Structure

CustomDatabaseEngine/
├── src/main/java/com/database/engine/
│   ├── DatabaseEngineApp.java    # Main CLI application
│   ├── Database.java             # Database management
│   ├── Table.java               # Table operations & persistence
│   └── Record.java              # Individual record representation
├── data/                        # Database files (auto-created)
├── pom.xml                      # Maven configuration
└── README.md                    # Documentation

## Technical Implementation

Core Components

Record Class: HashMap-based data storage with unique IDs
Table Class: CRUD operations + Java serialization for persistence
Database Class: Multi-table management and directory handling
CLI App: Interactive SQL-like command parser

Technologies Used

Java 23: Modern Java features and performance
Maven: Build automation and dependency management
Object Serialization: Binary file storage
Collections Framework: HashMap, ArrayList for efficiency

💡 Key Features

Automatic Persistence: Data saved after each operation
Smart Data Types: Auto-detection of integers vs strings
Memory Efficient: Tables loaded on-demand
Error Handling: Robust exception management
File Organization: Structured data directory

🚀 Future Enhancements

B-Tree indexing for faster queries
Advanced SQL parsing (JOIN, GROUP BY, ORDER BY)
Concurrent access with thread safety
Transaction management with rollback
REST API interface
Query optimization engine

📋 Command Reference

| Command Syntax Example | Command Syntax | Example |
|------------------------|----------------|---------|
| **Create Table**       | `CREATE TABLE tablename` | `CREATE TABLE users` |
| **Insert Data**        | `INSERT INTO table field=value` | `INSERT INTO users name=John age=25` |
| **Select All**         | `SELECT * FROM tablename` | `SELECT * FROM users` |
| **Select Filtered**    | `SELECT * FROM table WHERE field=value` | `SELECT * FROM users WHERE age=25` |
| **Show Tables**        | `SHOW TABLES` | `SHOW TABLES` |
| **Exit**               | `EXIT` | `EXIT` |


👨‍💻 Author
Bhumika Narula