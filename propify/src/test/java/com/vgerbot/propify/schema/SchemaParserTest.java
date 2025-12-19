package com.vgerbot.propify.schema;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;

import static org.junit.Assert.*;

public class SchemaParserTest {

    @Test
    public void testInterfaceExists() {
        assertTrue("SchemaParser should be an interface", SchemaParser.class.isInterface());
    }

    @Test
    public void testParseMethods() throws NoSuchMethodException {
        Method parseMethod = SchemaParser.class.getDeclaredMethod("parse", SchemaContext.class, InputStream.class);

        assertNotNull("parse method should exist", parseMethod);
        assertEquals("parse should return SchemaDefinition", SchemaDefinition.class, parseMethod.getReturnType());

        Class<?>[] exceptionTypes = parseMethod.getExceptionTypes();
        assertEquals("parse should throw one exception type", 1, exceptionTypes.length);
        assertEquals("parse should throw IOException", IOException.class, exceptionTypes[0]);
    }

    @Test
    public void testSupportsMethod() throws NoSuchMethodException {
        Method supportsMethod = SchemaParser.class.getDeclaredMethod("supports", SchemaType.class);

        assertNotNull("supports method should exist", supportsMethod);
        assertEquals("supports should return boolean", boolean.class, supportsMethod.getReturnType());
    }

    @Test
    public void testInterfaceMethods() {
        Method[] methods = SchemaParser.class.getDeclaredMethods();

        assertEquals("Should have 2 methods", 2, methods.length);
    }

    @Test
    public void testCanImplementInterface() throws IOException {
        SchemaParser parser = new SchemaParser() {
            @Override
            public SchemaDefinition parse(SchemaContext context, InputStream inputStream) throws IOException {
                SchemaDefinition schema = new SchemaDefinition("Test");
                schema.setTitle("Test Schema");
                return schema;
            }

            @Override
            public boolean supports(SchemaType type) {
                return type == SchemaType.JSON_SCHEMA;
            }
        };

        assertNotNull("Parser should not be null", parser);
        assertTrue("Should support JSON_SCHEMA", parser.supports(SchemaType.JSON_SCHEMA));
        assertFalse("Should not support OPENAPI", parser.supports(SchemaType.OPENAPI));

        InputStream testStream = new ByteArrayInputStream("test".getBytes());
        SchemaDefinition schema = parser.parse(null, testStream);
        assertNotNull("Schema should not be null", schema);
        assertEquals("Schema name should match", "Test", schema.getName());
        assertEquals("Schema title should match", "Test Schema", schema.getTitle());
    }

    @Test
    public void testMultipleImplementations() {
        SchemaParser jsonParser = new SchemaParser() {
            @Override
            public SchemaDefinition parse(SchemaContext context, InputStream inputStream) throws IOException {
                return new SchemaDefinition("JSON");
            }

            @Override
            public boolean supports(SchemaType type) {
                return type == SchemaType.JSON_SCHEMA;
            }
        };

        SchemaParser openApiParser = new SchemaParser() {
            @Override
            public SchemaDefinition parse(SchemaContext context, InputStream inputStream) throws IOException {
                return new SchemaDefinition("OpenAPI");
            }

            @Override
            public boolean supports(SchemaType type) {
                return type == SchemaType.OPENAPI;
            }
        };

        assertTrue("JSON parser should support JSON_SCHEMA", jsonParser.supports(SchemaType.JSON_SCHEMA));
        assertFalse("JSON parser should not support OPENAPI", jsonParser.supports(SchemaType.OPENAPI));

        assertFalse("OpenAPI parser should not support JSON_SCHEMA", openApiParser.supports(SchemaType.JSON_SCHEMA));
        assertTrue("OpenAPI parser should support OPENAPI", openApiParser.supports(SchemaType.OPENAPI));
    }

    @Test
    public void testParserWithAllSchemaTypes() {
        SchemaParser universalParser = new SchemaParser() {
            @Override
            public SchemaDefinition parse(SchemaContext context, InputStream inputStream) throws IOException {
                return new SchemaDefinition();
            }

            @Override
            public boolean supports(SchemaType type) {
                return true;
            }
        };

        assertTrue("Should support AUTO", universalParser.supports(SchemaType.AUTO));
        assertTrue("Should support JSON_SCHEMA", universalParser.supports(SchemaType.JSON_SCHEMA));
        assertTrue("Should support OPENAPI", universalParser.supports(SchemaType.OPENAPI));
        assertTrue("Should support XML_SCHEMA", universalParser.supports(SchemaType.XML_SCHEMA));
    }

    @Test(expected = IOException.class)
    public void testParserCanThrowIOException() throws IOException {
        SchemaParser parser = new SchemaParser() {
            @Override
            public SchemaDefinition parse(SchemaContext context, InputStream inputStream) throws IOException {
                throw new IOException("Test exception");
            }

            @Override
            public boolean supports(SchemaType type) {
                return true;
            }
        };

        parser.parse(null, null);
    }

    @Test
    public void testParserWithNullSupport() {
        SchemaParser parser = new SchemaParser() {
            @Override
            public SchemaDefinition parse(SchemaContext context, InputStream inputStream) throws IOException {
                return null;
            }

            @Override
            public boolean supports(SchemaType type) {
                return false;
            }
        };

        assertFalse("Should not support AUTO", parser.supports(SchemaType.AUTO));
        assertFalse("Should not support JSON_SCHEMA", parser.supports(SchemaType.JSON_SCHEMA));
        assertFalse("Should not support OPENAPI", parser.supports(SchemaType.OPENAPI));
        assertFalse("Should not support XML_SCHEMA", parser.supports(SchemaType.XML_SCHEMA));
    }
}
