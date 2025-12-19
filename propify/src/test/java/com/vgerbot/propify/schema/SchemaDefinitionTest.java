package com.vgerbot.propify.schema;

import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

public class SchemaDefinitionTest {

    private SchemaDefinition schema;

    @Before
    public void setUp() {
        schema = new SchemaDefinition();
    }

    @Test
    public void testDefaultConstructor() {
        SchemaDefinition def = new SchemaDefinition();

        assertNotNull("SchemaDefinition should not be null", def);
        assertNotNull("Properties should be initialized", def.getProperties());
        assertNotNull("Required should be initialized", def.getRequired());
        assertNotNull("NestedSchemas should be initialized", def.getNestedSchemas());
        assertTrue("Properties should be empty", def.getProperties().isEmpty());
        assertTrue("Required should be empty", def.getRequired().isEmpty());
        assertTrue("NestedSchemas should be empty", def.getNestedSchemas().isEmpty());
    }

    @Test
    public void testParameterizedConstructor() {
        SchemaDefinition def = new SchemaDefinition("User");

        assertEquals("Name should match", "User", def.getName());
        assertNotNull("Properties should be initialized", def.getProperties());
        assertNotNull("Required should be initialized", def.getRequired());
        assertNotNull("NestedSchemas should be initialized", def.getNestedSchemas());
    }

    @Test
    public void testNameGetterSetter() {
        schema.setName("TestSchema");

        assertEquals("Name should match", "TestSchema", schema.getName());
    }

    @Test
    public void testTitleGetterSetter() {
        schema.setTitle("Test Schema Title");

        assertEquals("Title should match", "Test Schema Title", schema.getTitle());
    }

    @Test
    public void testDescriptionGetterSetter() {
        schema.setDescription("Test schema description");

        assertEquals("Description should match", "Test schema description", schema.getDescription());
    }

    @Test
    public void testPropertiesGetterSetter() {
        Map<String, PropertyDefinition> properties = new HashMap<String, PropertyDefinition>();
        properties.put("prop1", new PropertyDefinition("prop1", "string"));
        properties.put("prop2", new PropertyDefinition("prop2", "integer"));

        schema.setProperties(properties);

        assertEquals("Properties should match", properties, schema.getProperties());
        assertEquals("Should have 2 properties", 2, schema.getProperties().size());
    }

    @Test
    public void testAddProperty() {
        PropertyDefinition property = new PropertyDefinition("username", "string");

        schema.addProperty("username", property);

        assertTrue("Properties should contain username", schema.getProperties().containsKey("username"));
        assertEquals("Property should match", property, schema.getProperties().get("username"));
    }

    @Test
    public void testAddMultipleProperties() {
        PropertyDefinition prop1 = new PropertyDefinition("name", "string");
        PropertyDefinition prop2 = new PropertyDefinition("age", "integer");
        PropertyDefinition prop3 = new PropertyDefinition("active", "boolean");

        schema.addProperty("name", prop1);
        schema.addProperty("age", prop2);
        schema.addProperty("active", prop3);

        assertEquals("Should have 3 properties", 3, schema.getProperties().size());
        assertTrue("Should contain name", schema.getProperties().containsKey("name"));
        assertTrue("Should contain age", schema.getProperties().containsKey("age"));
        assertTrue("Should contain active", schema.getProperties().containsKey("active"));
    }

    @Test
    public void testRequiredGetterSetter() {
        Set<String> required = new HashSet<String>();
        required.add("username");
        required.add("email");

        schema.setRequired(required);

        assertEquals("Required should match", required, schema.getRequired());
        assertEquals("Should have 2 required fields", 2, schema.getRequired().size());
    }

    @Test
    public void testAddRequired() {
        schema.addRequired("username");

        assertTrue("Required should contain username", schema.getRequired().contains("username"));
    }

    @Test
    public void testAddMultipleRequired() {
        schema.addRequired("username");
        schema.addRequired("email");
        schema.addRequired("password");

        assertEquals("Should have 3 required fields", 3, schema.getRequired().size());
        assertTrue("Should contain username", schema.getRequired().contains("username"));
        assertTrue("Should contain email", schema.getRequired().contains("email"));
        assertTrue("Should contain password", schema.getRequired().contains("password"));
    }

    @Test
    public void testIsRequiredTrue() {
        schema.addRequired("username");

        assertTrue("username should be required", schema.isRequired("username"));
    }

    @Test
    public void testIsRequiredFalse() {
        schema.addRequired("username");

        assertFalse("email should not be required", schema.isRequired("email"));
    }

    @Test
    public void testIsRequiredWithEmptySet() {
        assertFalse("Should return false for empty required set", schema.isRequired("anything"));
    }

    @Test
    public void testNestedSchemasGetter() {
        assertNotNull("NestedSchemas should not be null", schema.getNestedSchemas());
        assertTrue("NestedSchemas should be empty initially", schema.getNestedSchemas().isEmpty());
    }

    @Test
    public void testAddNestedSchema() {
        SchemaDefinition nestedSchema = new SchemaDefinition("Address");

        schema.addNestedSchema("Address", nestedSchema);

        assertTrue("NestedSchemas should contain Address", schema.getNestedSchemas().containsKey("Address"));
        assertEquals("Nested schema should match", nestedSchema, schema.getNestedSchemas().get("Address"));
    }

    @Test
    public void testAddMultipleNestedSchemas() {
        SchemaDefinition address = new SchemaDefinition("Address");
        SchemaDefinition contact = new SchemaDefinition("Contact");
        SchemaDefinition profile = new SchemaDefinition("Profile");

        schema.addNestedSchema("Address", address);
        schema.addNestedSchema("Contact", contact);
        schema.addNestedSchema("Profile", profile);

        assertEquals("Should have 3 nested schemas", 3, schema.getNestedSchemas().size());
        assertTrue("Should contain Address", schema.getNestedSchemas().containsKey("Address"));
        assertTrue("Should contain Contact", schema.getNestedSchemas().containsKey("Contact"));
        assertTrue("Should contain Profile", schema.getNestedSchemas().containsKey("Profile"));
    }

    @Test
    public void testCompleteSchemaSetup() {
        schema.setName("User");
        schema.setTitle("User Schema");
        schema.setDescription("Schema for user entity");

        PropertyDefinition username = new PropertyDefinition("username", "string");
        PropertyDefinition age = new PropertyDefinition("age", "integer");

        schema.addProperty("username", username);
        schema.addProperty("age", age);
        schema.addRequired("username");

        SchemaDefinition addressSchema = new SchemaDefinition("Address");
        schema.addNestedSchema("Address", addressSchema);

        assertEquals("Name should be User", "User", schema.getName());
        assertEquals("Should have 2 properties", 2, schema.getProperties().size());
        assertEquals("Should have 1 required field", 1, schema.getRequired().size());
        assertTrue("username should be required", schema.isRequired("username"));
        assertFalse("age should not be required", schema.isRequired("age"));
        assertEquals("Should have 1 nested schema", 1, schema.getNestedSchemas().size());
    }

    @Test
    public void testPropertiesOrderMaintained() {
        // LinkedHashMap should maintain insertion order
        schema.addProperty("firstName", new PropertyDefinition("firstName", "string"));
        schema.addProperty("lastName", new PropertyDefinition("lastName", "string"));
        schema.addProperty("age", new PropertyDefinition("age", "integer"));

        String[] keys = schema.getProperties().keySet().toArray(new String[0]);

        assertEquals("First key should be firstName", "firstName", keys[0]);
        assertEquals("Second key should be lastName", "lastName", keys[1]);
        assertEquals("Third key should be age", "age", keys[2]);
    }
}
