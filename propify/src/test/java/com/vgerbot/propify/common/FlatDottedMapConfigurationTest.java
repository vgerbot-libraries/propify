package com.vgerbot.propify.common;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link FlatDottedMapConfiguration} class.
 * 
 * These tests verify the ability to access properties in nested data structures
 * using dot notation and array/list indexing.
 */
public class FlatDottedMapConfigurationTest {

    private FlatDottedMapConfiguration configuration;
    private Map<String, Object> testData;
    
    @Before
    public void setUp() {
        testData = new HashMap<>();
        configuration = new FlatDottedMapConfiguration(testData);
    }
    
    /**
     * Tests accessing simple top-level properties.
     */
    @Test
    public void testSimpleProperties() {
        // Setup
        testData.put("stringProp", "value");
        testData.put("intProp", 42);
        testData.put("boolProp", true);
        
        // Verify
        assertEquals("value", configuration.getString("stringProp"));
        assertEquals(42, configuration.getInt("intProp"));
        assertTrue(configuration.getBoolean("boolProp"));
        
        // Non-existent property
        assertNull(configuration.getString("nonExistent"));
    }
    
    /**
     * Tests accessing properties in nested maps.
     */
    @Test
    public void testNestedMapProperties() {
        // Setup
        Map<String, Object> serverMap = new HashMap<>();
        serverMap.put("port", 8080);
        serverMap.put("host", "localhost");
        
        Map<String, Object> dbMap = new HashMap<>();
        dbMap.put("url", "jdbc:mysql://localhost:3306/test");
        dbMap.put("username", "admin");
        
        Map<String, Object> configMap = new HashMap<>();
        configMap.put("server", serverMap);
        configMap.put("database", dbMap);
        
        testData.put("config", configMap);
        
        // Verify
        assertEquals(8080, configuration.getInt("config.server.port"));
        assertEquals("localhost", configuration.getString("config.server.host"));
        assertEquals("jdbc:mysql://localhost:3306/test", configuration.getString("config.database.url"));
        assertEquals("admin", configuration.getString("config.database.username"));
        
        // Non-existent nested property
        assertNull(configuration.getString("config.server.nonExistent"));
        assertNull(configuration.getString("config.nonExistent.property"));
    }
    
    /**
     * Tests accessing elements in arrays and lists.
     */
    @Test
    public void testArrayAndListProperties() {
        // Setup - Array
        Integer[] numberArray = {1, 2, 3, 4, 5};
        testData.put("numbers", numberArray);
        
        // Setup - List
        List<String> stringList = Arrays.asList("one", "two", "three");
        testData.put("strings", stringList);
        
        // Verify array access
        assertEquals(1, configuration.getInt("numbers[0]"));
        assertEquals(3, configuration.getInt("numbers[2]"));
        assertEquals(5, configuration.getInt("numbers[4]"));
        
        // Verify list access
        assertEquals("one", configuration.getString("strings[0]"));
        assertEquals("two", configuration.getString("strings[1]"));
        assertEquals("three", configuration.getString("strings[2]"));
        
        // Test out of bounds
        assertNull(configuration.getString("numbers[10]"));
        assertNull(configuration.getString("strings[5]"));
    }
    
    /**
     * Tests accessing properties in nested maps within arrays/lists.
     */
    @Test
    public void testNestedObjectsInCollections() {
        // Setup - List of maps
        List<Map<String, Object>> items = new ArrayList<>();
        
        Map<String, Object> item1 = new HashMap<>();
        item1.put("id", 1);
        item1.put("name", "first");
        items.add(item1);
        
        Map<String, Object> item2 = new HashMap<>();
        item2.put("id", 2);
        item2.put("name", "second");
        items.add(item2);
        
        testData.put("items", items);
        
        // Verify access to maps in list
        assertEquals(1, configuration.getInt("items[0].id"));
        assertEquals("first", configuration.getString("items[0].name"));
        assertEquals(2, configuration.getInt("items[1].id"));
        assertEquals("second", configuration.getString("items[1].name"));
        
        // Non-existent property in map within list
        assertNull(configuration.getString("items[0].nonExistent"));
        
        // Out of bounds
        assertNull(configuration.getString("items[5].id"));
    }
    
    /**
     * Tests accessing deeply nested properties.
     */
    @Test
    public void testDeeplyNestedProperties() {
        // Setup - Deep nesting
        Map<String, Object> level3 = new HashMap<>();
        level3.put("value", "deepValue");
        
        Map<String, Object> level2 = new HashMap<>();
        level2.put("level3", level3);
        
        Map<String, Object> level1 = new HashMap<>();
        level1.put("level2", level2);
        
        testData.put("level1", level1);
        
        // Setup - Deep nesting with arrays
        List<Object> deepList = new ArrayList<>();
        Map<String, Object> deepMap = new HashMap<>();
        deepMap.put("key", "deepListValue");
        deepList.add(deepMap);
        
        Map<String, Object> nestedMap = new HashMap<>();
        nestedMap.put("list", deepList);
        
        testData.put("nested", nestedMap);
        
        // Verify deeply nested access
        assertEquals("deepValue", configuration.getString("level1.level2.level3.value"));
        
        // Verify deeply nested access with list
        assertEquals("deepListValue", configuration.getString("nested.list[0].key"));
        
        // Non-existent deep property
        assertNull(configuration.getString("level1.level2.level3.nonExistent"));
        assertNull(configuration.getString("level1.level2.nonExistent.value"));
    }
    
    /**
     * Tests edge cases like null values and invalid access patterns.
     */
    @Test
    public void testEdgeCases() {
        // Setup
        Map<String, Object> nullMap = new HashMap<>();
        nullMap.put("nullValue", null);
        
        Map<String, Object> emptyMap = new HashMap<>();
        
        List<Object> emptyList = new ArrayList<>();
        
        testData.put("withNull", nullMap);
        testData.put("empty", emptyMap);
        testData.put("emptyList", emptyList);
        testData.put("nullDirect", null);
        
        // Test with null values
        assertNull(configuration.getString("withNull.nullValue"));
        
        // Test with empty containers
        assertNull(configuration.getString("empty.something"));
        assertNull(configuration.getString("emptyList[0]"));
        
        // Test null direct value
        assertNull(configuration.getString("nullDirect"));
        
        // Test trying to traverse a non-container
        testData.put("string", "not a container");
        assertNull(configuration.getString("string.property"));
        assertNull(configuration.getString("string[0]"));
        
        // Test incorrect syntax
        assertNull(configuration.getString("level1..level2"));
        assertNull(configuration.getString("emptyList[abc]")); // Not a number
    }
    
    /**
     * Tests complex nesting with mixed data structures.
     */
    @Test
    public void testComplexNestedStructures() {
        // Setup a complex nested structure
        Map<String, Object> user = new HashMap<>();
        user.put("name", "John Doe");
        
        List<Map<String, Object>> addresses = new ArrayList<>();
        
        Map<String, Object> address1 = new HashMap<>();
        address1.put("type", "home");
        address1.put("street", "123 Main St");
        
        Map<String, Object> address2 = new HashMap<>();
        address2.put("type", "work");
        address2.put("street", "456 Market St");
        
        addresses.add(address1);
        addresses.add(address2);
        
        user.put("addresses", addresses);
        
        Map<String, Object> skills = new HashMap<>();
        skills.put("programming", Arrays.asList("Java", "Python", "JavaScript"));
        skills.put("languages", Arrays.asList("English", "Spanish"));
        
        user.put("skills", skills);
        
        testData.put("user", user);
        
        // Verify complex nested access
        assertEquals("John Doe", configuration.getString("user.name"));
        assertEquals("home", configuration.getString("user.addresses[0].type"));
        assertEquals("123 Main St", configuration.getString("user.addresses[0].street"));
        assertEquals("work", configuration.getString("user.addresses[1].type"));
        assertEquals("Java", configuration.getString("user.skills.programming[0]"));
        assertEquals("Spanish", configuration.getString("user.skills.languages[1]"));
    }
    
    /**
     * Tests handling of invalid input for property keys.
     */
    @Test
    public void testInvalidKeys() {
        // Invalid keys should return null, not throw exceptions
        assertNull(configuration.getString(null));
        assertNull(configuration.getString(""));
        
        // Malformed array indices
        assertNull(configuration.getString("property["));
        assertNull(configuration.getString("property]"));
        assertNull(configuration.getString("property[]"));
    }
} 