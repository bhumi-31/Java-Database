package com.database.core;

import com.database.types.*;
import org.junit.Test;
import static org.junit.Assert.*;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

/**
 * Unit tests for Table class.
 */
public class TableTest {

    private Schema createTestSchema() {
        List<Column> columns = new ArrayList<>();
        columns.add(new Column("id", DataType.INTEGER, false, true));
        columns.add(new Column("name", DataType.VARCHAR, true, false, 50));
        columns.add(new Column("age", DataType.INTEGER, true, false));
        return new Schema(columns);
    }

    private Row createTestRow(int id, String name, int age) {
        Row row = new Row();
        row.setValue("id", new IntegerValue(id));
        row.setValue("name", new VarcharValue(name, 50));
        row.setValue("age", new IntegerValue(age));
        return row;
    }

    @Test
    public void testCreateTable() {
        Schema schema = createTestSchema();
        Table table = new Table("users", schema);

        assertEquals("users", table.getName());
        assertEquals(schema, table.getSchema());
        assertEquals(0, table.getRowCount());
        assertTrue(table.isEmpty());
    }

    @Test
    public void testInsertRow() {
        Schema schema = createTestSchema();
        Table table = new Table("users", schema);

        Row row = createTestRow(1, "Alice", 25);
        table.insert(row);

        assertEquals(1, table.getRowCount());
        assertFalse(table.isEmpty());

        Row foundRow = table.findRowByPrimaryKey(1);
        assertNotNull(foundRow);
        assertEquals(new IntegerValue(1), foundRow.getValue("id"));
        assertEquals(new VarcharValue("Alice", 50), foundRow.getValue("name"));
    }

    @Test
    public void testSelectAll() {
        Schema schema = createTestSchema();
        Table table = new Table("users", schema);

        Row row1 = createTestRow(1, "Alice", 25);
        Row row2 = createTestRow(2, "Bob", 30);

        table.insert(row1);
        table.insert(row2);

        List<Row> allRows = table.selectAll();
        assertEquals(2, allRows.size());
    }

    @Test
    public void testUpdateRow() {
        Schema schema = createTestSchema();
        Table table = new Table("users", schema);

        Row row = createTestRow(1, "Alice", 25);
        table.insert(row);

        Map<String, TypedValue> updates = new HashMap<>();
        updates.put("name", new VarcharValue("Alice Smith", 50));
        updates.put("age", new IntegerValue(26));

        boolean success = table.update(row.getRowId(), updates);
        assertTrue(success);

        Row updatedRow = table.findRowByPrimaryKey(1);
        assertNotNull(updatedRow);
        assertEquals(new VarcharValue("Alice Smith", 50), updatedRow.getValue("name"));
        assertEquals(new IntegerValue(26), updatedRow.getValue("age"));
    }

    @Test
    public void testDeleteRow() {
        Schema schema = createTestSchema();
        Table table = new Table("users", schema);

        Row row = createTestRow(1, "Alice", 25);
        table.insert(row);

        assertEquals(1, table.getRowCount());

        boolean success = table.delete(row.getRowId());
        assertTrue(success);

        assertEquals(0, table.getRowCount());
        assertNull(table.findRowByPrimaryKey(1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInsertDuplicatePrimaryKey() {
        Schema schema = createTestSchema();
        Table table = new Table("users", schema);

        Row row1 = createTestRow(1, "Alice", 25);
        Row row2 = createTestRow(1, "Bob", 30); // Same primary key

        table.insert(row1);
        table.insert(row2); // Should throw exception
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInsertInvalidRow() {
        Schema schema = createTestSchema();
        Table table = new Table("users", schema);

        Row invalidRow = new Row();
        invalidRow.setValue("name", new VarcharValue("Alice", 50));
        // Missing required primary key

        table.insert(invalidRow);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdatePrimaryKey() {
        Schema schema = createTestSchema();
        Table table = new Table("users", schema);

        Row row = createTestRow(1, "Alice", 25);
        table.insert(row);

        Map<String, TypedValue> updates = new HashMap<>();
        updates.put("id", new IntegerValue(2)); // Trying to update primary key

        table.update(row.getRowId(), updates);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateTableWithNullName() {
        Schema schema = createTestSchema();
        new Table(null, schema);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateTableWithNullSchema() {
        new Table("users", null);
    }
}
