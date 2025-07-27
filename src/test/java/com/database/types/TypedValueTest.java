package com.database.types;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for TypedValue implementations.
 */
public class TypedValueTest {

    @Test
    public void testIntegerValue() {
        IntegerValue value1 = new IntegerValue(42);
        IntegerValue value2 = new IntegerValue(42);
        IntegerValue value3 = new IntegerValue(100);

        assertEquals(DataType.INTEGER, value1.getType());
        assertEquals(42, value1.intValue());
        assertTrue(value1.isValid());
        
        assertEquals(0, value1.compareTo(value2));
        assertTrue(value1.compareTo(value3) < 0);
        assertTrue(value3.compareTo(value1) > 0);
        
        assertEquals(value1, value2);
        assertNotEquals(value1, value3);
    }

    @Test
    public void testVarcharValue() {
        VarcharValue value1 = new VarcharValue("hello", 10);
        VarcharValue value2 = new VarcharValue("hello", 10);
        VarcharValue value3 = new VarcharValue("world", 10);

        assertEquals(DataType.VARCHAR, value1.getType());
        assertEquals("hello", value1.stringValue());
        assertTrue(value1.isValid());
        
        assertEquals(0, value1.compareTo(value2));
        assertTrue(value1.compareTo(value3) < 0);
        
        assertEquals(value1, value2);
        assertNotEquals(value1, value3);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testVarcharValueTooLong() {
        new VarcharValue("this string is too long", 5);
    }

    @Test
    public void testBooleanValue() {
        BooleanValue value1 = new BooleanValue(true);
        BooleanValue value2 = new BooleanValue(true);
        BooleanValue value3 = new BooleanValue(false);

        assertEquals(DataType.BOOLEAN, value1.getType());
        assertTrue(value1.booleanValue());
        assertTrue(value1.isValid());
        
        assertEquals(0, value1.compareTo(value2));
        assertTrue(value1.compareTo(value3) > 0);
        
        assertEquals(value1, value2);
        assertNotEquals(value1, value3);
    }

    @Test
    public void testDoubleValue() {
        DoubleValue value1 = new DoubleValue(3.14);
        DoubleValue value2 = new DoubleValue(3.14);
        DoubleValue value3 = new DoubleValue(2.71);

        assertEquals(DataType.DOUBLE, value1.getType());
        assertEquals(3.14, value1.doubleValue(), 0.001);
        assertTrue(value1.isValid());
        
        assertEquals(0, value1.compareTo(value2));
        assertTrue(value1.compareTo(value3) > 0);
        
        assertEquals(value1, value2);
        assertNotEquals(value1, value3);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCompareDifferentTypes() {
        IntegerValue intValue = new IntegerValue(42);
        VarcharValue strValue = new VarcharValue("hello");
        intValue.compareTo(strValue);
    }
}
