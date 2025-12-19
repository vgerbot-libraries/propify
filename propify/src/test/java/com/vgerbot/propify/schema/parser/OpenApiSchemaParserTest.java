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

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class OpenApiSchemaParserTest {

    private OpenApiSchemaParser parser;

    @Mock
    private ResourceLoaderProvider resourceLoaderProvider;

    @Mock
    private Logger logger;

    private SchemaContext yamlContext;
    private SchemaContext jsonContext;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        parser = new OpenApiSchemaParser();

        yamlContext = new SchemaContext(
                "test.yaml",
                SchemaType.OPENAPI,
                "Pet",
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

        jsonContext = new SchemaContext(
                "test.json",
                SchemaType.OPENAPI,
                "User",
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
    public void testSupportsOpenApi() {
        assertTrue("Should support OPENAPI", parser.supports(SchemaType.OPENAPI));
    }

    @Test
    public void testDoesNotSupportJsonSchema() {
        assertFalse("Should not support JSON_SCHEMA", parser.supports(SchemaType.JSON_SCHEMA));
    }

    @Test
    public void testDoesNotSupportAuto() {
        assertFalse("Should not support AUTO", parser.supports(SchemaType.AUTO));
    }

    @Test
    public void testDoesNotSupportXmlSchema() {
        assertFalse("Should not support XML_SCHEMA", parser.supports(SchemaType.XML_SCHEMA));
    }

    @Test
    public void testParseYamlOpenApiSpec() throws IOException {
        String yamlSpec = "openapi: 3.0.0\n" +
                "info:\n" +
                "  title: Pet Store API\n" +
                "  version: 1.0.0\n" +
                "components:\n" +
                "  schemas:\n" +
                "    Pet:\n" +
                "      type: object\n" +
                "      description: A pet\n" +
                "      properties:\n" +
                "        id:\n" +
                "          type: integer\n" +
                "          format: int64\n" +
                "        name:\n" +
                "          type: string\n" +
                "          description: Pet name\n" +
                "      required:\n" +
                "        - name\n";

        InputStream inputStream = new ByteArrayInputStream(yamlSpec.getBytes());
        SchemaDefinition schema = parser.parse(yamlContext, inputStream);

        assertNotNull("Schema should not be null", schema);
        assertEquals("Name should be Pet", "Pet", schema.getName());
        assertEquals("Title should be Pet", "Pet", schema.getTitle());
        assertEquals("Description should match", "A pet", schema.getDescription());
        assertEquals("Should have 2 properties", 2, schema.getProperties().size());
        assertTrue("Should have id property", schema.getProperties().containsKey("id"));
        assertTrue("Should have name property", schema.getProperties().containsKey("name"));
        assertTrue("name should be required", schema.isRequired("name"));
        assertFalse("id should not be required", schema.isRequired("id"));
    }

    @Test
    public void testParseJsonOpenApiSpec() throws IOException {
        String jsonSpec = "{\n" +
                "  \"openapi\": \"3.0.0\",\n" +
                "  \"info\": {\n" +
                "    \"title\": \"User API\",\n" +
                "    \"version\": \"1.0.0\"\n" +
                "  },\n" +
                "  \"components\": {\n" +
                "    \"schemas\": {\n" +
                "      \"User\": {\n" +
                "        \"type\": \"object\",\n" +
                "        \"title\": \"User Schema\",\n" +
                "        \"properties\": {\n" +
                "          \"username\": {\n" +
                "            \"type\": \"string\"\n" +
                "          },\n" +
                "          \"email\": {\n" +
                "            \"type\": \"string\",\n" +
                "            \"format\": \"email\"\n" +
                "          }\n" +
                "        }\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}";

        InputStream inputStream = new ByteArrayInputStream(jsonSpec.getBytes());
        SchemaDefinition schema = parser.parse(jsonContext, inputStream);

        assertNotNull("Schema should not be null", schema);
        assertEquals("Name should be User", "User", schema.getName());
        assertEquals("Title should match", "User Schema", schema.getTitle());
    }

    @Test
    public void testParseWithRefInSchemaRef() throws IOException {
        String yamlSpec = "openapi: 3.0.0\n" +
                "components:\n" +
                "  schemas:\n" +
                "    Pet:\n" +
                "      type: object\n" +
                "      properties:\n" +
                "        name:\n" +
                "          type: string\n";

        SchemaContext refContext = new SchemaContext(
                "test.yaml",
                SchemaType.OPENAPI,
                "#/components/schemas/Pet",
                "$$",
                true, true, false, true, true, true,
                resourceLoaderProvider,
                logger
        );

        InputStream inputStream = new ByteArrayInputStream(yamlSpec.getBytes());
        SchemaDefinition schema = parser.parse(refContext, inputStream);

        assertNotNull("Schema should not be null", schema);
        assertEquals("Name should be Pet", "Pet", schema.getName());
    }

    @Test
    public void testParsePropertyWithRef() throws IOException {
        String yamlSpec = "openapi: 3.0.0\n" +
                "components:\n" +
                "  schemas:\n" +
                "    Pet:\n" +
                "      type: object\n" +
                "      properties:\n" +
                "        owner:\n" +
                "          $ref: '#/components/schemas/User'\n";

        InputStream inputStream = new ByteArrayInputStream(yamlSpec.getBytes());
        SchemaDefinition schema = parser.parse(yamlContext, inputStream);

        PropertyDefinition owner = schema.getProperties().get("owner");
        assertNotNull("Owner property should exist", owner);
        assertEquals("RefType should be User", "User", owner.getRefType());
        assertTrue("Should be object type", owner.isObject());
    }

    @Test
    public void testParseNumericConstraints() throws IOException {
        String yamlSpec = "openapi: 3.0.0\n" +
                "components:\n" +
                "  schemas:\n" +
                "    Pet:\n" +
                "      type: object\n" +
                "      properties:\n" +
                "        age:\n" +
                "          type: integer\n" +
                "          minimum: 0\n" +
                "          maximum: 100\n" +
                "          exclusiveMinimum: false\n" +
                "          exclusiveMaximum: true\n";

        InputStream inputStream = new ByteArrayInputStream(yamlSpec.getBytes());
        SchemaDefinition schema = parser.parse(yamlContext, inputStream);

        PropertyDefinition age = schema.getProperties().get("age");
        assertNotNull("Age property should exist", age);
        assertEquals("Minimum should be 0", Integer.valueOf(0), age.getMinimum());
        assertEquals("Maximum should be 100", Integer.valueOf(100), age.getMaximum());
        assertEquals("ExclusiveMinimum should be false", false, age.getExclusiveMinimum());
        assertEquals("ExclusiveMaximum should be true", true, age.getExclusiveMaximum());
    }

    @Test
    public void testParseStringConstraints() throws IOException {
        String yamlSpec = "openapi: 3.0.0\n" +
                "components:\n" +
                "  schemas:\n" +
                "    Pet:\n" +
                "      type: object\n" +
                "      properties:\n" +
                "        name:\n" +
                "          type: string\n" +
                "          pattern: '^[A-Z].*'\n" +
                "          minLength: 3\n" +
                "          maxLength: 50\n";

        InputStream inputStream = new ByteArrayInputStream(yamlSpec.getBytes());
        SchemaDefinition schema = parser.parse(yamlContext, inputStream);

        PropertyDefinition name = schema.getProperties().get("name");
        assertNotNull("Name property should exist", name);
        assertEquals("Pattern should match", "^[A-Z].*", name.getPattern());
        assertEquals("MinLength should be 3", Integer.valueOf(3), name.getMinLength());
        assertEquals("MaxLength should be 50", Integer.valueOf(50), name.getMaxLength());
    }

    @Test
    public void testParseEnumValues() throws IOException {
        String yamlSpec = "openapi: 3.0.0\n" +
                "components:\n" +
                "  schemas:\n" +
                "    Pet:\n" +
                "      type: object\n" +
                "      properties:\n" +
                "        status:\n" +
                "          type: string\n" +
                "          enum:\n" +
                "            - available\n" +
                "            - pending\n" +
                "            - sold\n";

        InputStream inputStream = new ByteArrayInputStream(yamlSpec.getBytes());
        SchemaDefinition schema = parser.parse(yamlContext, inputStream);

        PropertyDefinition status = schema.getProperties().get("status");
        assertNotNull("Status property should exist", status);
        assertTrue("Should have enum values", status.hasEnumValues());
        assertEquals("Should have 3 enum values", 3, status.getEnumValues().size());
        assertTrue("Should contain available", status.getEnumValues().contains("available"));
        assertTrue("Should contain pending", status.getEnumValues().contains("pending"));
        assertTrue("Should contain sold", status.getEnumValues().contains("sold"));
    }

    @Test
    public void testParseArrayProperty() throws IOException {
        String yamlSpec = "openapi: 3.0.0\n" +
                "components:\n" +
                "  schemas:\n" +
                "    Pet:\n" +
                "      type: object\n" +
                "      properties:\n" +
                "        tags:\n" +
                "          type: array\n" +
                "          items:\n" +
                "            type: string\n" +
                "          minItems: 1\n" +
                "          maxItems: 10\n";

        InputStream inputStream = new ByteArrayInputStream(yamlSpec.getBytes());
        SchemaDefinition schema = parser.parse(yamlContext, inputStream);

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
        String yamlSpec = "openapi: 3.0.0\n" +
                "components:\n" +
                "  schemas:\n" +
                "    Pet:\n" +
                "      type: object\n" +
                "      properties:\n" +
                "        address:\n" +
                "          type: object\n" +
                "          description: Pet address\n" +
                "          properties:\n" +
                "            street:\n" +
                "              type: string\n" +
                "            city:\n" +
                "              type: string\n" +
                "          required:\n" +
                "            - city\n";

        InputStream inputStream = new ByteArrayInputStream(yamlSpec.getBytes());
        SchemaDefinition schema = parser.parse(yamlContext, inputStream);

        PropertyDefinition address = schema.getProperties().get("address");
        assertNotNull("Address property should exist", address);
        assertTrue("Should be object type", address.isObject());
        assertNotNull("Nested schema should exist", address.getNestedSchema());
        assertEquals("Nested schema should have 2 properties", 2, address.getNestedSchema().getProperties().size());
        assertTrue("city should be required", address.getNestedSchema().isRequired("city"));
        assertFalse("street should not be required", address.getNestedSchema().isRequired("street"));
    }

    @Test
    public void testParsePropertyWithDefaultValue() throws IOException {
        String yamlSpec = "openapi: 3.0.0\n" +
                "components:\n" +
                "  schemas:\n" +
                "    Pet:\n" +
                "      type: object\n" +
                "      properties:\n" +
                "        active:\n" +
                "          type: boolean\n" +
                "          default: true\n";

        InputStream inputStream = new ByteArrayInputStream(yamlSpec.getBytes());
        SchemaDefinition schema = parser.parse(yamlContext, inputStream);

        PropertyDefinition active = schema.getProperties().get("active");
        assertNotNull("Active property should exist", active);
        assertEquals("Default value should be true", true, active.getDefaultValue());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseWithoutOpenApiField() throws IOException {
        String invalidSpec = "{\n" +
                "  \"components\": {\n" +
                "    \"schemas\": {}\n" +
                "  }\n" +
                "}";

        InputStream inputStream = new ByteArrayInputStream(invalidSpec.getBytes());
        parser.parse(jsonContext, inputStream);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseWithoutSchemas() throws IOException {
        String invalidSpec = "openapi: 3.0.0\n" +
                "info:\n" +
                "  title: Test\n";

        InputStream inputStream = new ByteArrayInputStream(invalidSpec.getBytes());
        parser.parse(yamlContext, inputStream);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseWithNonExistentSchema() throws IOException {
        String yamlSpec = "openapi: 3.0.0\n" +
                "components:\n" +
                "  schemas:\n" +
                "    Dog:\n" +
                "      type: object\n";

        InputStream inputStream = new ByteArrayInputStream(yamlSpec.getBytes());
        parser.parse(yamlContext, inputStream); // Looking for Pet, but only Dog exists
    }

    @Test
    public void testParseWithEmptySchemaRef() throws IOException {
        String yamlSpec = "openapi: 3.0.0\n" +
                "components:\n" +
                "  schemas:\n" +
                "    Pet:\n" +
                "      type: object\n" +
                "      properties:\n" +
                "        name:\n" +
                "          type: string\n";

        SchemaContext emptyRefContext = new SchemaContext(
                "test.yaml",
                SchemaType.OPENAPI,
                "",
                "$$",
                true, true, false, true, true, true,
                resourceLoaderProvider,
                logger
        );

        InputStream inputStream = new ByteArrayInputStream(yamlSpec.getBytes());
        SchemaDefinition schema = parser.parse(emptyRefContext, inputStream);

        assertNotNull("Schema should not be null", schema);
        assertEquals("Should parse first schema (Pet)", "Pet", schema.getName());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseWithEmptySchemaRefAndNoSchemas() throws IOException {
        String yamlSpec = "openapi: 3.0.0\n" +
                "components:\n" +
                "  schemas: {}\n";

        SchemaContext emptyRefContext = new SchemaContext(
                "test.yaml",
                SchemaType.OPENAPI,
                "",
                "$$",
                true, true, false, true, true, true,
                resourceLoaderProvider,
                logger
        );

        InputStream inputStream = new ByteArrayInputStream(yamlSpec.getBytes());
        parser.parse(emptyRefContext, inputStream);
    }

    @Test
    public void testParseSchemaWithoutTitle() throws IOException {
        String yamlSpec = "openapi: 3.0.0\n" +
                "components:\n" +
                "  schemas:\n" +
                "    Pet:\n" +
                "      type: object\n" +
                "      properties:\n" +
                "        name:\n" +
                "          type: string\n";

        InputStream inputStream = new ByteArrayInputStream(yamlSpec.getBytes());
        SchemaDefinition schema = parser.parse(yamlContext, inputStream);

        assertEquals("Title should default to schema name", "Pet", schema.getTitle());
    }

    @Test
    public void testParsePropertyWithFormat() throws IOException {
        String yamlSpec = "openapi: 3.0.0\n" +
                "components:\n" +
                "  schemas:\n" +
                "    Pet:\n" +
                "      type: object\n" +
                "      properties:\n" +
                "        birthDate:\n" +
                "          type: string\n" +
                "          format: date-time\n";

        InputStream inputStream = new ByteArrayInputStream(yamlSpec.getBytes());
        SchemaDefinition schema = parser.parse(yamlContext, inputStream);

        PropertyDefinition birthDate = schema.getProperties().get("birthDate");
        assertNotNull("BirthDate property should exist", birthDate);
        assertEquals("Format should be date-time", "date-time", birthDate.getFormat());
    }
}
