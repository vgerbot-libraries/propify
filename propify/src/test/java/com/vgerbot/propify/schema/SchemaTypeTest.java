package com.vgerbot.propify.schema;

import org.junit.Test;

import static org.junit.Assert.*;

public class SchemaTypeTest {

    @Test
    public void testEnumValues() {
        SchemaType[] values = SchemaType.values();

        assertNotNull("Enum values should not be null", values);
        assertEquals("Should have 4 enum constants", 4, values.length);
    }

    @Test
    public void testAutoValue() {
        SchemaType auto = SchemaType.AUTO;

        assertNotNull("AUTO should not be null", auto);
        assertEquals("AUTO name should match", "AUTO", auto.name());
    }

    @Test
    public void testJsonSchemaValue() {
        SchemaType jsonSchema = SchemaType.JSON_SCHEMA;

        assertNotNull("JSON_SCHEMA should not be null", jsonSchema);
        assertEquals("JSON_SCHEMA name should match", "JSON_SCHEMA", jsonSchema.name());
    }

    @Test
    public void testOpenApiValue() {
        SchemaType openApi = SchemaType.OPENAPI;

        assertNotNull("OPENAPI should not be null", openApi);
        assertEquals("OPENAPI name should match", "OPENAPI", openApi.name());
    }

    @Test
    public void testXmlSchemaValue() {
        SchemaType xmlSchema = SchemaType.XML_SCHEMA;

        assertNotNull("XML_SCHEMA should not be null", xmlSchema);
        assertEquals("XML_SCHEMA name should match", "XML_SCHEMA", xmlSchema.name());
    }

    @Test
    public void testValueOf() {
        assertEquals("valueOf AUTO should work", SchemaType.AUTO, SchemaType.valueOf("AUTO"));
        assertEquals("valueOf JSON_SCHEMA should work", SchemaType.JSON_SCHEMA, SchemaType.valueOf("JSON_SCHEMA"));
        assertEquals("valueOf OPENAPI should work", SchemaType.OPENAPI, SchemaType.valueOf("OPENAPI"));
        assertEquals("valueOf XML_SCHEMA should work", SchemaType.XML_SCHEMA, SchemaType.valueOf("XML_SCHEMA"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValueOfInvalid() {
        SchemaType.valueOf("INVALID");
    }

    @Test
    public void testEnumComparison() {
        assertSame("AUTO should be singleton", SchemaType.AUTO, SchemaType.valueOf("AUTO"));
        assertNotSame("AUTO and JSON_SCHEMA should be different", SchemaType.AUTO, SchemaType.JSON_SCHEMA);
    }
}
