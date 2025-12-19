package com.vgerbot.propify.schema;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class PropertyDefinitionTest {

    private PropertyDefinition property;

    @Before
    public void setUp() {
        property = new PropertyDefinition();
    }

    @Test
    public void testDefaultConstructor() {
        PropertyDefinition prop = new PropertyDefinition();

        assertNotNull("Property should not be null", prop);
        assertNull("Name should be null by default", prop.getName());
        assertNull("Type should be null by default", prop.getType());
    }

    @Test
    public void testParameterizedConstructor() {
        PropertyDefinition prop = new PropertyDefinition("testName", "string");

        assertEquals("Name should match", "testName", prop.getName());
        assertEquals("Type should match", "string", prop.getType());
    }

    @Test
    public void testNameGetterSetter() {
        property.setName("propertyName");

        assertEquals("Name should match", "propertyName", property.getName());
    }

    @Test
    public void testTypeGetterSetter() {
        property.setType("integer");

        assertEquals("Type should match", "integer", property.getType());
    }

    @Test
    public void testFormatGetterSetter() {
        property.setFormat("date-time");

        assertEquals("Format should match", "date-time", property.getFormat());
    }

    @Test
    public void testDescriptionGetterSetter() {
        property.setDescription("Test description");

        assertEquals("Description should match", "Test description", property.getDescription());
    }

    @Test
    public void testRequiredGetterSetter() {
        property.setRequired(true);

        assertTrue("Required should be true", property.isRequired());

        property.setRequired(false);
        assertFalse("Required should be false", property.isRequired());
    }

    @Test
    public void testDefaultValueGetterSetter() {
        property.setDefaultValue("default");

        assertEquals("Default value should match", "default", property.getDefaultValue());
    }

    @Test
    public void testPatternGetterSetter() {
        property.setPattern("^[A-Z].*");

        assertEquals("Pattern should match", "^[A-Z].*", property.getPattern());
    }

    @Test
    public void testMinLengthGetterSetter() {
        property.setMinLength(5);

        assertEquals("MinLength should match", Integer.valueOf(5), property.getMinLength());
    }

    @Test
    public void testMaxLengthGetterSetter() {
        property.setMaxLength(100);

        assertEquals("MaxLength should match", Integer.valueOf(100), property.getMaxLength());
    }

    @Test
    public void testMinimumGetterSetter() {
        property.setMinimum(0);

        assertEquals("Minimum should match", Integer.valueOf(0), property.getMinimum());
    }

    @Test
    public void testMaximumGetterSetter() {
        property.setMaximum(1000);

        assertEquals("Maximum should match", Integer.valueOf(1000), property.getMaximum());
    }

    @Test
    public void testExclusiveMinimumGetterSetter() {
        property.setExclusiveMinimum(true);

        assertTrue("ExclusiveMinimum should be true", property.getExclusiveMinimum());
    }

    @Test
    public void testExclusiveMaximumGetterSetter() {
        property.setExclusiveMaximum(true);

        assertTrue("ExclusiveMaximum should be true", property.getExclusiveMaximum());
    }

    @Test
    public void testItemsGetterSetter() {
        PropertyDefinition items = new PropertyDefinition("item", "string");
        property.setItems(items);

        assertEquals("Items should match", items, property.getItems());
    }

    @Test
    public void testMinItemsGetterSetter() {
        property.setMinItems(1);

        assertEquals("MinItems should match", Integer.valueOf(1), property.getMinItems());
    }

    @Test
    public void testMaxItemsGetterSetter() {
        property.setMaxItems(10);

        assertEquals("MaxItems should match", Integer.valueOf(10), property.getMaxItems());
    }

    @Test
    public void testNestedSchemaGetterSetter() {
        SchemaDefinition nestedSchema = new SchemaDefinition("Nested");
        property.setNestedSchema(nestedSchema);

        assertEquals("NestedSchema should match", nestedSchema, property.getNestedSchema());
    }

    @Test
    public void testRefTypeGetterSetter() {
        property.setRefType("User");

        assertEquals("RefType should match", "User", property.getRefType());
    }

    @Test
    public void testEnumValuesGetterSetter() {
        List<Object> enumValues = Arrays.asList("RED", "GREEN", "BLUE");
        property.setEnumValues(enumValues);

        assertEquals("EnumValues should match", enumValues, property.getEnumValues());
    }

    @Test
    public void testHasPatternWithPattern() {
        property.setPattern("^test.*");

        assertTrue("hasPattern should return true", property.hasPattern());
    }

    @Test
    public void testHasPatternWithoutPattern() {
        assertFalse("hasPattern should return false", property.hasPattern());
    }

    @Test
    public void testHasPatternWithEmptyPattern() {
        property.setPattern("");

        assertFalse("hasPattern should return false for empty pattern", property.hasPattern());
    }

    @Test
    public void testHasEnumValuesWithValues() {
        property.setEnumValues(Arrays.asList("A", "B", "C"));

        assertTrue("hasEnumValues should return true", property.hasEnumValues());
    }

    @Test
    public void testHasEnumValuesWithoutValues() {
        assertFalse("hasEnumValues should return false", property.hasEnumValues());
    }

    @Test
    public void testHasEnumValuesWithEmptyList() {
        property.setEnumValues(Arrays.asList());

        assertFalse("hasEnumValues should return false for empty list", property.hasEnumValues());
    }

    @Test
    public void testIsArray() {
        property.setType("array");

        assertTrue("isArray should return true", property.isArray());
    }

    @Test
    public void testIsArrayWithDifferentType() {
        property.setType("string");

        assertFalse("isArray should return false", property.isArray());
    }

    @Test
    public void testIsObject() {
        property.setType("object");

        assertTrue("isObject should return true", property.isObject());
    }

    @Test
    public void testIsObjectWithDifferentType() {
        property.setType("string");

        assertFalse("isObject should return false", property.isObject());
    }

    @Test
    public void testIsString() {
        property.setType("string");

        assertTrue("isString should return true", property.isString());
    }

    @Test
    public void testIsStringWithDifferentType() {
        property.setType("integer");

        assertFalse("isString should return false", property.isString());
    }

    @Test
    public void testIsInteger() {
        property.setType("integer");

        assertTrue("isInteger should return true", property.isInteger());
    }

    @Test
    public void testIsIntegerWithDifferentType() {
        property.setType("string");

        assertFalse("isInteger should return false", property.isInteger());
    }

    @Test
    public void testIsNumber() {
        property.setType("number");

        assertTrue("isNumber should return true", property.isNumber());
    }

    @Test
    public void testIsNumberWithDifferentType() {
        property.setType("string");

        assertFalse("isNumber should return false", property.isNumber());
    }

    @Test
    public void testIsBoolean() {
        property.setType("boolean");

        assertTrue("isBoolean should return true", property.isBoolean());
    }

    @Test
    public void testIsBooleanWithDifferentType() {
        property.setType("string");

        assertFalse("isBoolean should return false", property.isBoolean());
    }

    @Test
    public void testNullType() {
        property.setType(null);

        assertFalse("isString should return false with null type", property.isString());
        assertFalse("isInteger should return false with null type", property.isInteger());
        assertFalse("isNumber should return false with null type", property.isNumber());
        assertFalse("isBoolean should return false with null type", property.isBoolean());
        assertFalse("isArray should return false with null type", property.isArray());
        assertFalse("isObject should return false with null type", property.isObject());
    }
}
