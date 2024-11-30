package com.vgerbot.propify;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class PropifyPropertiesTest {

    private PropifyProperties propertiesWithConversion;
    private PropifyProperties propertiesWithoutConversion;

    @Before
    public void setUp() {
        propertiesWithConversion = new PropifyProperties(true);
        propertiesWithoutConversion = new PropifyProperties(false);
    }

    @Test
    public void testPut_StringValue_StoresAsString() {
        propertiesWithConversion.put("key", "value");
        assertEquals("value", propertiesWithConversion.get("key"));
    }

    @Test
    public void testPut_IntegerString_WithAutoConversion() {
        propertiesWithConversion.put("key", "123");
        assertEquals(123, propertiesWithConversion.get("key"));
    }

    @Test
    public void testPut_IntegerString_WithoutAutoConversion() {
        propertiesWithoutConversion.put("key", "123");
        assertEquals("123", propertiesWithoutConversion.get("key"));
    }

    @Test
    public void testPut_LongString_WithAutoConversion() {
        propertiesWithConversion.put("key", "123L");
        assertEquals(123L, propertiesWithConversion.get("key"));
    }

    @Test
    public void testPut_LongString_WithoutAutoConversion() {
        propertiesWithoutConversion.put("key", "123L");
        assertEquals("123L", propertiesWithoutConversion.get("key"));
    }

    @Test
    public void testPut_FloatString_WithAutoConversion() {
        propertiesWithConversion.put("key", "123.45f");
        assertEquals(123.45f, propertiesWithConversion.get("key"));
    }

    @Test
    public void testPut_FloatString_WithoutAutoConversion() {
        propertiesWithoutConversion.put("key", "123.45f");
        assertEquals("123.45f", propertiesWithoutConversion.get("key"));
    }

    @Test
    public void testPut_DoubleString_WithAutoConversion() {
        propertiesWithConversion.put("key", "123.45");
        assertEquals(123.45d, propertiesWithConversion.get("key"));
    }

    @Test
    public void testPut_DoubleString_WithoutAutoConversion() {
        propertiesWithoutConversion.put("key", "123.45");
        assertEquals("123.45", propertiesWithoutConversion.get("key"));
    }

    @Test
    public void testPut_BooleanString_WithAutoConversion() {
        propertiesWithConversion.put("key1", "true");
        propertiesWithConversion.put("key2", "false");
        assertEquals(true, propertiesWithConversion.get("key1"));
        assertEquals(false, propertiesWithConversion.get("key2"));
    }

    @Test
    public void testPut_BooleanString_WithoutAutoConversion() {
        propertiesWithoutConversion.put("key1", "true");
        propertiesWithoutConversion.put("key2", "false");
        assertEquals("true", propertiesWithoutConversion.get("key1"));
        assertEquals("false", propertiesWithoutConversion.get("key2"));
    }

    @Test
    public void testPut_ByteString_WithAutoConversion() {
        propertiesWithConversion.put("key", "123b");
        assertEquals((byte)123, propertiesWithConversion.get("key"));
    }

    @Test
    public void testPut_ByteString_WithoutAutoConversion() {
        propertiesWithoutConversion.put("key", "123b");
        assertEquals("123b", propertiesWithoutConversion.get("key"));
    }

    @Test
    public void testPut_NestedPropifyProperties_InheritsConversionSetting() {
        PropifyProperties nested = propertiesWithConversion.createNested();
        nested.put("number", "123");
        propertiesWithConversion.put("parent", nested);
        
        PropifyProperties retrieved = (PropifyProperties)propertiesWithConversion.get("parent");
        assertEquals(123, retrieved.get("number"));

        nested = propertiesWithoutConversion.createNested();
        nested.put("number", "123");
        propertiesWithoutConversion.put("parent", nested);
        
        retrieved = (PropifyProperties)propertiesWithoutConversion.get("parent");
        assertEquals("123", retrieved.get("number"));
    }

    @Test
    public void testPut_NullValue_StoresNull() {
        propertiesWithConversion.put("key", null);
        assertNull(propertiesWithConversion.get("key"));
        
        propertiesWithoutConversion.put("key", null);
        assertNull(propertiesWithoutConversion.get("key"));
    }

    @Test
    public void testPut_EmptyString_StoresEmptyString() {
        propertiesWithConversion.put("key", "");
        assertEquals("", propertiesWithConversion.get("key"));
        
        propertiesWithoutConversion.put("key", "");
        assertEquals("", propertiesWithoutConversion.get("key"));
    }


    @Test
    public void testPut_DeepNestedStructure_InheritsConversionSetting() {
        PropifyProperties level1 = propertiesWithConversion.createNested();
        PropifyProperties level2 = level1.createNested();
        level2.put("number", "42");
        level1.put("level2", level2);
        propertiesWithConversion.put("level1", level1);
        
        PropifyProperties retrieved1 = (PropifyProperties) propertiesWithConversion.get("level1");
        PropifyProperties retrieved2 = (PropifyProperties) retrieved1.get("level2");
        assertEquals(42, retrieved2.get("number"));

        level1 = propertiesWithoutConversion.createNested();
        level2 = level1.createNested();
        level2.put("number", "42");
        level1.put("level2", level2);
        propertiesWithoutConversion.put("level1", level1);
        
        retrieved1 = (PropifyProperties) propertiesWithoutConversion.get("level1");
        retrieved2 = (PropifyProperties) retrieved1.get("level2");
        assertEquals("42", retrieved2.get("number"));
    }
}
