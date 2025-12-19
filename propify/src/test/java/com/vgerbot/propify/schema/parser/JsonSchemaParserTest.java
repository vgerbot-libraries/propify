package com.vgerbot.propify.schema.parser;

import com.vgerbot.propify.core.ResourceLoaderProvider;
import com.vgerbot.propify.logger.Logger;
import com.vgerbot.propify.schema.PropertyDefinition;
import com.vgerbot.propify.schema.SchemaContext;
import com.vgerbot.propify.schema.SchemaDefinition;
import com.vgerbot.propify.schema.SchemaType;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class JsonSchemaParserTest {

    private JsonSchemaParser parser;

    @Mock
    private ResourceLoaderProvider resourceLoaderProvider;

    @Mock
    private Logger logger;

    private SchemaContext context;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        parser = new JsonSchemaParser();

        context = new SchemaContext(
                "test.json",
                SchemaType.JSON_SCHEMA,
                "",
                "$$",
                true,
                true,
                false,
                true,
                true,
                true,
                resourceLoaderProvider,
                logger
        );
    }

    @Test
    public void testConstructor() {
        assertNotNull("Parser should not be null", parser);
    }

    @Test
    public void testSupportsJsonSchema() {
        assertTrue("Should support JSON_SCHEMA", parser.supports(SchemaType.JSON_SCHEMA));
    }

    @Test
    public void testSupportsAuto() {
        assertTrue("Should support AUTO", parser.supports(SchemaType.AUTO));
    }

    @Test
    public void testDoesNotSupportOpenApi() {
        assertFalse("Should not support OPENAPI", parser.supports(SchemaType.OPENAPI));
    }

    @Test
    public void testDoesNotSupportXmlSchema() {
        assertFalse("Should not support XML_SCHEMA", parser.supports(SchemaType.XML_SCHEMA));
    }

    @Test
    public void testParseSimpleSchema() throws IOException {
        String jsonSchema = "{\n" +
                "  \"title\": \"User\",\n" +
                "  \"description\": \"User schema\",\n" +
                "  \"type\": \"object\",\n" +
                "  \"properties\": {\n" +
                "    \"username\": {\n" +
                "      \"type\": \"string\",\n" +
                "      \"description\": \"Username\"\n" +
                "    },\n" +
                "    \"age\": {\n" +
                "      \"type\": \"integer\",\n" +
                "      \"description\": \"User age\"\n" +
                "    }\n" +
                "  },\n" +
                "  \"required\": [\"username\"]\n" +
                "}";

        InputStream inputStream = new ByteArrayInputStream(jsonSchema.getBytes());
        SchemaDefinition schema = parser.parse(context, inputStream);

        assertNotNull("Schema should not be null", schema);
        assertEquals("Title should match", "User", schema.getTitle());
        assertEquals("Name should match title", "User", schema.getName());
        assertEquals("Description should match", "User schema", schema.getDescription());
        assertEquals("Should have 2 properties", 2, schema.getProperties().size());
        assertTrue("Should have username property", schema.getProperties().containsKey("username"));
        assertTrue("Should have age property", schema.getProperties().containsKey("age"));
        assertTrue("username should be required", schema.isRequired("username"));
        assertFalse("age should not be required", schema.isRequired("age"));
    }

    @Test
    public void testParsePropertyWithFormat() throws IOException {
        String jsonSchema = "{\n" +
                "  \"type\": \"object\",\n" +
                "  \"properties\": {\n" +
                "    \"email\": {\n" +
                "      \"type\": \"string\",\n" +
                "      \"format\": \"email\"\n" +
                "    }\n" +
                "  }\n" +
                "}";

        InputStream inputStream = new ByteArrayInputStream(jsonSchema.getBytes());
        SchemaDefinition schema = parser.parse(context, inputStream);

        PropertyDefinition email = schema.getProperties().get("email");
        assertNotNull("Email property should exist", email);
        assertEquals("Format should be email", "email", email.getFormat());
    }

    @Test
    public void testParsePropertyWithDefaultValue() throws IOException {
        String jsonSchema = "{\n" +
                "  \"type\": \"object\",\n" +
                "  \"properties\": {\n" +
                "    \"active\": {\n" +
                "      \"type\": \"boolean\",\n" +
                "      \"default\": true\n" +
                "    }\n" +
                "  }\n" +
                "}";

        InputStream inputStream = new ByteArrayInputStream(jsonSchema.getBytes());
        SchemaDefinition schema = parser.parse(context, inputStream);

        PropertyDefinition active = schema.getProperties().get("active");
        assertNotNull("Active property should exist", active);
        assertEquals("Default value should be true", true, active.getDefaultValue());
    }

    @Test
    public void testParseStringConstraints() throws IOException {
        String jsonSchema = "{\n" +
                "  \"type\": \"object\",\n" +
                "  \"properties\": {\n" +
                "    \"password\": {\n" +
                "      \"type\": \"string\",\n" +
                "      \"pattern\": \"^[A-Za-z0-9]+$\",\n" +
                "      \"minLength\": 8,\n" +
                "      \"maxLength\": 20\n" +
                "    }\n" +
                "  }\n" +
                "}";

        InputStream inputStream = new ByteArrayInputStream(jsonSchema.getBytes());
        SchemaDefinition schema = parser.parse(context, inputStream);

        PropertyDefinition password = schema.getProperties().get("password");
        assertNotNull("Password property should exist", password);
        assertEquals("Pattern should match", "^[A-Za-z0-9]+$", password.getPattern());
        assertEquals("MinLength should be 8", Integer.valueOf(8), password.getMinLength());
        assertEquals("MaxLength should be 20", Integer.valueOf(20), password.getMaxLength());
    }

    @Test
    public void testParseNumericConstraints() throws IOException {
        String jsonSchema = "{\n" +
                "  \"type\": \"object\",\n" +
                "  \"properties\": {\n" +
                "    \"score\": {\n" +
                "      \"type\": \"number\",\n" +
                "      \"minimum\": 0,\n" +
                "      \"maximum\": 100,\n" +
                "      \"exclusiveMinimum\": false,\n" +
                "      \"exclusiveMaximum\": true\n" +
                "    }\n" +
                "  }\n" +
                "}";

        InputStream inputStream = new ByteArrayInputStream(jsonSchema.getBytes());
        SchemaDefinition schema = parser.parse(context, inputStream);

        PropertyDefinition score = schema.getProperties().get("score");
        assertNotNull("Score property should exist", score);
        assertEquals("Minimum should be 0", Integer.valueOf(0), score.getMinimum());
        assertEquals("Maximum should be 100", Integer.valueOf(100), score.getMaximum());
        assertEquals("ExclusiveMinimum should be false", false, score.getExclusiveMinimum());
        assertEquals("ExclusiveMaximum should be true", true, score.getExclusiveMaximum());
    }

    @Test
    public void testParseEnumValues() throws IOException {
        String jsonSchema = "{\n" +
                "  \"type\": \"object\",\n" +
                "  \"properties\": {\n" +
                "    \"color\": {\n" +
                "      \"type\": \"string\",\n" +
                "      \"enum\": [\"RED\", \"GREEN\", \"BLUE\"]\n" +
                "    }\n" +
                "  }\n" +
                "}";

        InputStream inputStream = new ByteArrayInputStream(jsonSchema.getBytes());
        SchemaDefinition schema = parser.parse(context, inputStream);

        PropertyDefinition color = schema.getProperties().get("color");
        assertNotNull("Color property should exist", color);
        assertTrue("Should have enum values", color.hasEnumValues());
        assertEquals("Should have 3 enum values", 3, color.getEnumValues().size());
        assertTrue("Should contain RED", color.getEnumValues().contains("RED"));
        assertTrue("Should contain GREEN", color.getEnumValues().contains("GREEN"));
        assertTrue("Should contain BLUE", color.getEnumValues().contains("BLUE"));
    }

    @Test
    public void testParseArrayProperty() throws IOException {
        String jsonSchema = "{\n" +
                "  \"type\": \"object\",\n" +
                "  \"properties\": {\n" +
                "    \"tags\": {\n" +
                "      \"type\": \"array\",\n" +
                "      \"items\": {\n" +
                "        \"type\": \"string\"\n" +
                "      },\n" +
                "      \"minItems\": 1,\n" +
                "      \"maxItems\": 10\n" +
                "    }\n" +
                "  }\n" +
                "}";

        InputStream inputStream = new ByteArrayInputStream(jsonSchema.getBytes());
        SchemaDefinition schema = parser.parse(context, inputStream);

        PropertyDefinition tags = schema.getProperties().get("tags");
        assertNotNull("Tags property should exist", tags);
        assertTrue("Should be array type", tags.isArray());
        assertNotNull("Items should not be null", tags.getItems());
        assertEquals("Item type should be string", "string", tags.getItems().getType());
        assertEquals("MinItems should be 1", Integer.valueOf(1), tags.getMinItems());
        assertEquals("MaxItems should be 10", Integer.valueOf(10), tags.getMaxItems());
    }

    @Test
    public void testParseNestedObject() throws IOException {
        String jsonSchema = "{\n" +
                "  \"type\": \"object\",\n" +
                "  \"properties\": {\n" +
                "    \"address\": {\n" +
                "      \"type\": \"object\",\n" +
                "      \"description\": \"User address\",\n" +
                "      \"properties\": {\n" +
                "        \"street\": {\n" +
                "          \"type\": \"string\"\n" +
                "        },\n" +
                "        \"city\": {\n" +
                "          \"type\": \"string\"\n" +
                "        }\n" +
                "      },\n" +
                "      \"required\": [\"city\"]\n" +
                "    }\n" +
                "  }\n" +
                "}";

        InputStream inputStream = new ByteArrayInputStream(jsonSchema.getBytes());
        SchemaDefinition schema = parser.parse(context, inputStream);

        PropertyDefinition address = schema.getProperties().get("address");
        assertNotNull("Address property should exist", address);
        assertTrue("Should be object type", address.isObject());
        assertNotNull("Nested schema should exist", address.getNestedSchema());
        assertEquals("Nested schema name should be Address", "Address", address.getNestedSchema().getName());
        assertEquals("Nested schema should have 2 properties", 2, address.getNestedSchema().getProperties().size());
        assertTrue("city should be required in nested schema", address.getNestedSchema().isRequired("city"));
        assertFalse("street should not be required in nested schema", address.getNestedSchema().isRequired("street"));
    }

    @Test
    public void testParsePropertyWithRef() throws IOException {
        String jsonSchema = "{\n" +
                "  \"type\": \"object\",\n" +
                "  \"properties\": {\n" +
                "    \"author\": {\n" +
                "      \"$ref\": \"#/definitions/User\"\n" +
                "    }\n" +
                "  }\n" +
                "}";

        InputStream inputStream = new ByteArrayInputStream(jsonSchema.getBytes());
        SchemaDefinition schema = parser.parse(context, inputStream);

        PropertyDefinition author = schema.getProperties().get("author");
        assertNotNull("Author property should exist", author);
        assertEquals("RefType should be User", "User", author.getRefType());
    }

    @Test
    public void testParseSchemaWithoutTitle() throws IOException {
        String jsonSchema = "{\n" +
                "  \"type\": \"object\",\n" +
                "  \"properties\": {\n" +
                "    \"name\": {\n" +
                "      \"type\": \"string\"\n" +
                "    }\n" +
                "  }\n" +
                "}";

        InputStream inputStream = new ByteArrayInputStream(jsonSchema.getBytes());
        SchemaDefinition schema = parser.parse(context, inputStream);

        assertNotNull("Schema should not be null", schema);
        assertNull("Title should be null", schema.getTitle());
        assertNull("Name should be null", schema.getName());
    }

    @Test
    public void testParseSchemaWithoutProperties() throws IOException {
        String jsonSchema = "{\n" +
                "  \"title\": \"Empty\",\n" +
                "  \"type\": \"object\"\n" +
                "}";

        InputStream inputStream = new ByteArrayInputStream(jsonSchema.getBytes());
        SchemaDefinition schema = parser.parse(context, inputStream);

        assertNotNull("Schema should not be null", schema);
        assertTrue("Properties should be empty", schema.getProperties().isEmpty());
    }

    @Test
    public void testParseSchemaWithMultipleTypes() throws IOException {
        String jsonSchema = "{\n" +
                "  \"type\": \"object\",\n" +
                "  \"properties\": {\n" +
                "    \"stringProp\": {\"type\": \"string\"},\n" +
                "    \"intProp\": {\"type\": \"integer\"},\n" +
                "    \"numberProp\": {\"type\": \"number\"},\n" +
                "    \"boolProp\": {\"type\": \"boolean\"},\n" +
                "    \"arrayProp\": {\"type\": \"array\"},\n" +
                "    \"objectProp\": {\"type\": \"object\"}\n" +
                "  }\n" +
                "}";

        InputStream inputStream = new ByteArrayInputStream(jsonSchema.getBytes());
        SchemaDefinition schema = parser.parse(context, inputStream);

        assertEquals("Should have 6 properties", 6, schema.getProperties().size());
        assertTrue("stringProp should be string", schema.getProperties().get("stringProp").isString());
        assertTrue("intProp should be integer", schema.getProperties().get("intProp").isInteger());
        assertTrue("numberProp should be number", schema.getProperties().get("numberProp").isNumber());
        assertTrue("boolProp should be boolean", schema.getProperties().get("boolProp").isBoolean());
        assertTrue("arrayProp should be array", schema.getProperties().get("arrayProp").isArray());
        assertTrue("objectProp should be object", schema.getProperties().get("objectProp").isObject());
    }

    @Test(expected = IOException.class)
    public void testParseInvalidJson() throws IOException {
        String invalidJson = "{ invalid json }";
        InputStream inputStream = new ByteArrayInputStream(invalidJson.getBytes());
        parser.parse(context, inputStream);
    }
}
