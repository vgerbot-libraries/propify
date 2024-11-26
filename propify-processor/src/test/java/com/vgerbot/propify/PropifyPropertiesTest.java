package com.vgerbot.propify;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class PropifyPropertiesTest {

    private PropifyProperties properties;

    @Before
    public void setUp() {
        properties = new PropifyProperties();
    }

    @Test
    public void testPut_StringValue_StoresAsString() {
        properties.put("key", "value");
        assertEquals("value", properties.get("key"));
    }

    @Test
    public void testPut_IntegerString_ParsesToInteger() {
        properties.put("key", "123");
        assertEquals(123, properties.get("key"));
    }

    @Test
    public void testPut_LongString_ParsesToLong() {
        properties.put("key", "123L");
        assertEquals(123L, properties.get("key"));
    }

    @Test
    public void testPut_FloatString_ParsesToFloat() {
        properties.put("key", "123.45f");
        assertEquals(123.45f, properties.get("key"));
    }

    @Test
    public void testPut_DoubleString_ParsesToDouble() {
        properties.put("key", "123.45");
        assertEquals(123.45d, properties.get("key"));
    }

    @Test
    public void testPut_BooleanString_ParsesToBoolean() {
        properties.put("key1", "true");
        properties.put("key2", "false");
        assertEquals(true, properties.get("key1"));
        assertEquals(false, properties.get("key2"));
    }

    @Test
    public void testPut_ByteString_ParsesToByte() {
        properties.put("key", "123b");
        assertEquals((byte)123, properties.get("key"));
    }

    @Test
    public void testPut_NestedPropifyProperties_StoresDirectly() {
        PropifyProperties nested = new PropifyProperties();
        nested.put("nestedKey", "nestedValue");
        
        properties.put("parent", nested);
        
        assertSame(nested, properties.get("parent"));
        assertEquals("nestedValue", ((PropifyProperties)properties.get("parent")).get("nestedKey"));
    }

    @Test
    public void testPut_NullValue_StoresNull() {
        properties.put("key", null);
        assertNull(properties.get("key"));
    }

    @Test
    public void testPut_EmptyString_StoresEmptyString() {
        properties.put("key", "");
        assertEquals("", properties.get("key"));
    }

    @Test
    public void testPut_NonStringObject_UsesParseValue() {
        Object customObject = new Object() {
            @Override
            public String toString() {
                return "123";
            }
        };
        
        properties.put("key", customObject);
        assertEquals(123, properties.get("key"));
    }

    @Test
    public void testPut_MultipleValues_MaintainsAllEntries() {
        properties.put("string", "value");
        properties.put("integer", "123");
        properties.put("boolean", "true");
        
        assertEquals("value", properties.get("string"));
        assertEquals(123, properties.get("integer"));
        assertEquals(true, properties.get("boolean"));
    }

    @Test
    public void testPut_OverwriteExisting_UpdatesValue() {
        properties.put("key", "oldValue");
        properties.put("key", "newValue");
        
        assertEquals("newValue", properties.get("key"));
    }

    @Test
    public void testPut_DeepNestedStructure_MaintainsHierarchy() {
        PropifyProperties level1 = new PropifyProperties();
        PropifyProperties level2 = new PropifyProperties();
        PropifyProperties level3 = new PropifyProperties();
        
        level3.put("value", "deep");
        level2.put("level3", level3);
        level1.put("level2", level2);
        properties.put("level1", level1);
        
        PropifyProperties retrieved1 = (PropifyProperties) properties.get("level1");
        PropifyProperties retrieved2 = (PropifyProperties) retrieved1.get("level2");
        PropifyProperties retrieved3 = (PropifyProperties) retrieved2.get("level3");
        
        assertEquals("deep", retrieved3.get("value"));
    }

    @Test
    public void testPut_MixedTypes_HandlesCorrectly() {
        PropifyProperties nested = new PropifyProperties();
        nested.put("number", "42");
        nested.put("text", "hello");
        
        properties.put("nested", nested);
        properties.put("topLevel", "123");
        
        assertEquals(42, ((PropifyProperties)properties.get("nested")).get("number"));
        assertEquals("hello", ((PropifyProperties)properties.get("nested")).get("text"));
        assertEquals(123, properties.get("topLevel"));
    }

    @Test
    public void testInheritance_HashMapMethods_WorkCorrectly() {
        properties.put("key", "value");
        
        assertTrue(properties.containsKey("key"));
        assertFalse(properties.isEmpty());
        assertEquals(1, properties.size());
        
        properties.remove("key");
        assertTrue(properties.isEmpty());
        
        properties.clear();
        assertEquals(0, properties.size());
    }
}
