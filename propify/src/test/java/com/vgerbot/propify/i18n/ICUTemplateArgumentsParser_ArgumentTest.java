package com.vgerbot.propify.i18n;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests for the Argument inner class of ICUTemplateArgumentsParser.
 * Specifically focuses on testing equals, hashCode, and toString methods.
 */
public class ICUTemplateArgumentsParser_ArgumentTest {

    /**
     * Test for equals method with identical arguments.
     */
    @Test
    public void testEquals_IdenticalArguments() {
        ICUTemplateArgumentsParser.Argument arg1 = new ICUTemplateArgumentsParser.Argument("name", String.class);
        ICUTemplateArgumentsParser.Argument arg2 = new ICUTemplateArgumentsParser.Argument("name", String.class);
        
        assertTrue(arg1.equals(arg2));
        assertTrue(arg2.equals(arg1));
    }

    /**
     * Test for equals method with different names.
     */
    @Test
    public void testEquals_DifferentNames() {
        ICUTemplateArgumentsParser.Argument arg1 = new ICUTemplateArgumentsParser.Argument("name1", String.class);
        ICUTemplateArgumentsParser.Argument arg2 = new ICUTemplateArgumentsParser.Argument("name2", String.class);
        
        assertFalse(arg1.equals(arg2));
        assertFalse(arg2.equals(arg1));
    }

    /**
     * Test for equals method with different types.
     */
    @Test
    public void testEquals_DifferentTypes() {
        ICUTemplateArgumentsParser.Argument arg1 = new ICUTemplateArgumentsParser.Argument("name", String.class);
        ICUTemplateArgumentsParser.Argument arg2 = new ICUTemplateArgumentsParser.Argument("name", Number.class);
        
        assertFalse(arg1.equals(arg2));
        assertFalse(arg2.equals(arg1));
    }

    /**
     * Test for equals method with null.
     */
    @Test
    public void testEquals_Null() {
        ICUTemplateArgumentsParser.Argument arg = new ICUTemplateArgumentsParser.Argument("name", String.class);
        
        assertFalse(arg.equals(null));
    }

    /**
     * Test for equals method with different object type.
     */
    @Test
    public void testEquals_DifferentObjectType() {
        ICUTemplateArgumentsParser.Argument arg = new ICUTemplateArgumentsParser.Argument("name", String.class);
        
        assertFalse(arg.equals("not an Argument object"));
    }

    /**
     * Test for equals method with same object reference.
     */
    @Test
    public void testEquals_SameReference() {
        ICUTemplateArgumentsParser.Argument arg = new ICUTemplateArgumentsParser.Argument("name", String.class);
        
        assertTrue(arg.equals(arg));
    }

    /**
     * Test for hashCode method with identical arguments.
     */
    @Test
    public void testHashCode_IdenticalArguments() {
        ICUTemplateArgumentsParser.Argument arg1 = new ICUTemplateArgumentsParser.Argument("name", String.class);
        ICUTemplateArgumentsParser.Argument arg2 = new ICUTemplateArgumentsParser.Argument("name", String.class);
        
        assertEquals(arg1.hashCode(), arg2.hashCode());
    }

    /**
     * Test for hashCode method with different arguments.
     */
    @Test
    public void testHashCode_DifferentArguments() {
        ICUTemplateArgumentsParser.Argument arg1 = new ICUTemplateArgumentsParser.Argument("name1", String.class);
        ICUTemplateArgumentsParser.Argument arg2 = new ICUTemplateArgumentsParser.Argument("name2", Number.class);
        
        // Different arguments should (most likely) have different hash codes
        // Note: This is not guaranteed by the hashCode contract, but is a good practice
        assertNotEquals(arg1.hashCode(), arg2.hashCode());
    }

    /**
     * Test for toString method.
     */
    @Test
    public void testToString() {
        ICUTemplateArgumentsParser.Argument arg = new ICUTemplateArgumentsParser.Argument("name", String.class);
        String toString = arg.toString();
        
        // Check that toString contains the name and type
        assertTrue(toString.contains("name='name'"));
        assertTrue(toString.contains("type=class java.lang.String"));
    }

    /**
     * Test for toString method with different values.
     */
    @Test
    public void testToString_DifferentValues() {
        ICUTemplateArgumentsParser.Argument arg1 = new ICUTemplateArgumentsParser.Argument("count", Number.class);
        ICUTemplateArgumentsParser.Argument arg2 = new ICUTemplateArgumentsParser.Argument("date", java.util.Date.class);
        
        String toString1 = arg1.toString();
        String toString2 = arg2.toString();
        
        // Check that toString contains the correct name and type for each argument
        assertTrue(toString1.contains("name='count'"));
        assertTrue(toString1.contains("type=class java.lang.Number"));
        
        assertTrue(toString2.contains("name='date'"));
        assertTrue(toString2.contains("type=class java.util.Date"));
        
        // Different arguments should have different toString representations
        assertNotEquals(toString1, toString2);
    }

    /**
     * Test for constructor with None.class type.
     */
    @Test
    public void testConstructor_NoneClass() {
        // The Argument constructor should convert None.class to String.class
        ICUTemplateArgumentsParser.Argument arg = new ICUTemplateArgumentsParser.Argument("name", None.class);
        
        assertEquals("name", arg.getName());
        assertEquals(String.class, arg.getType());
    }

    /**
     * Test for getName method.
     */
    @Test
    public void testGetName() {
        ICUTemplateArgumentsParser.Argument arg = new ICUTemplateArgumentsParser.Argument("name", String.class);
        
        assertEquals("name", arg.getName());
    }

    /**
     * Test for getType method.
     */
    @Test
    public void testGetType() {
        ICUTemplateArgumentsParser.Argument arg = new ICUTemplateArgumentsParser.Argument("name", Number.class);
        
        assertEquals(Number.class, arg.getType());
    }
}
