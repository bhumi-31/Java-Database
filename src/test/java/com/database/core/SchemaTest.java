package com.database.core;

import com.database.types.*;
import org.junit.Test;
import static org.junit.Assert.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Unit tests for Schema class.
 */
public class SchemaTest {

    @Test
    public void testCreateSchema() {
        List<Column> columns = new ArrayList<>();
        columns.add(new Column("id", DataType.INTEGER, false, true));
        columns.add(new Column("name", DataType.VARCHAR, true, false, 50));
        columns.add(new Column("age", DataType.INTEGER, true, false));

        Schema schema = new Schema(columns);

        assertEquals(3, schema.getColumnCount());
        assertEquals("id", schema.getPrimaryKeyColumn());
        assertTrue(schema.hasColumn("id"));
        assertTrue(schema.hasColumn("name"));
        assertTrue(schema.hasColumn("age"));
        assertFalse(schema.hasColumn("nonexistent"));
    }

    @Test
    public void testGetColumn() {
        List<Column> columns = new ArrayList<>();
        columns.add(new Column("id", DataType.INTEGER, false, true));
        columns.add(new Column("name", DataType.VARCHAR, true, false, 50));

        Schema schema = new Schema(columns);

        Column idColumn = schema.getColumn("id");
        assertNotNull(idColumn);
        assertEquals("id", idColumn.getName());
        assertEquals(DataType.INTEGER, idColumn.getType());
        assertTrue(idColumn.isPrimaryKey());

        Column nameColumn = schema.getColumn("NAME"); // Case insensitive
        assertNotNull(nameColumn);
        assertEquals("name", nameColumn.getName());
        assertEquals(DataType.VARCHAR, nameColumn.getType());
        assertFalse(nameColumn.isPrimaryKey());

        assertNull(schema.getColumn("nonexistent"));
    }

    @Test
    public void testValidateRow() {
        List<Column> columns = new ArrayList<>();
        columns.add(new Column("id", DataType.INTEGER, false, true));
        columns.add(new Column("name", DataType.VARCHAR, true, false, 50));
        columns.add(new Column("age", DataType.INTEGER, true, false));

        Schema schema = new Schema(columns);

        // Valid row
        Row validRow = new Row();
        validRow.setValue("id", new IntegerValue(1));
        validRow.setValue("name", new VarcharValue("Alice", 50));
        validRow.setValue("age", new IntegerValue(25));

        assertTrue(schema.validateRow(validRow));

        // Row missing non-nullable column
        Row invalidRow1 = new Row();
        invalidRow1.setValue("name", new VarcharValue("Bob", 50));
        // Missing required 'id' column

        assertFalse(schema.validateRow(invalidRow1));

        // Row with wrong data type
        Row invalidRow2 = new Row();
        invalidRow2.setValue("id", new VarcharValue("not_an_integer", 10));

        assertFalse(schema.validateRow(invalidRow2));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDuplicateColumnNames() {
        List<Column> columns = new ArrayList<>();
        columns.add(new Column("id", DataType.INTEGER, false, true));
        columns.add(new Column("ID", DataType.VARCHAR, true, false)); // Duplicate name (case insensitive)

        new Schema(columns);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMultiplePrimaryKeys() {
        List<Column> columns = new ArrayList<>();
        columns.add(new Column("id1", DataType.INTEGER, false, true));
        columns.add(new Column("id2", DataType.INTEGER, false, true)); // Multiple primary keys

        new Schema(columns);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmptySchema() {
        new Schema(new ArrayList<>());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullSchema() {
        new Schema(null);
    }
}
