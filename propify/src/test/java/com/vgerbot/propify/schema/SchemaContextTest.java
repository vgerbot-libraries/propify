package com.vgerbot.propify.schema;

import com.vgerbot.propify.core.ResourceLoader;
import com.vgerbot.propify.core.ResourceLoaderProvider;
import com.vgerbot.propify.logger.Logger;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class SchemaContextTest {

    @Mock
    private ResourceLoaderProvider resourceLoaderProvider;

    @Mock
    private ResourceLoader resourceLoader;

    @Mock
    private Logger logger;

    private SchemaContext context;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        context = new SchemaContext(
                "schemas/user.json",
                SchemaType.JSON_SCHEMA,
                "#/definitions/User",
                "$$Dto",
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
        assertNotNull("Context should not be null", context);
    }

    @Test
    public void testGetLocation() {
        assertEquals("Location should match", "schemas/user.json", context.getLocation());
    }

    @Test
    public void testGetType() {
        assertEquals("Type should match", SchemaType.JSON_SCHEMA, context.getType());
    }

    @Test
    public void testGetSchemaRef() {
        assertEquals("SchemaRef should match", "#/definitions/User", context.getSchemaRef());
    }

    @Test
    public void testGetGeneratedClassName() {
        assertEquals("GeneratedClassName should match", "$$Dto", context.getGeneratedClassName());
    }

    @Test
    public void testIsBuilder() {
        assertTrue("isBuilder should return true", context.isBuilder());
    }

    @Test
    public void testIsJacksonAnnotations() {
        assertTrue("isJacksonAnnotations should return true", context.isJacksonAnnotations());
    }

    @Test
    public void testIsJaxbAnnotations() {
        assertFalse("isJaxbAnnotations should return false", context.isJaxbAnnotations());
    }

    @Test
    public void testIsValidationAnnotations() {
        assertTrue("isValidationAnnotations should return true", context.isValidationAnnotations());
    }

    @Test
    public void testIsSerializable() {
        assertTrue("isSerializable should return true", context.isSerializable());
    }

    @Test
    public void testIsGenerateHelperMethods() {
        assertTrue("isGenerateHelperMethods should return true", context.isGenerateHelperMethods());
    }

    @Test
    public void testGetLogger() {
        assertEquals("Logger should match", logger, context.getLogger());
    }

    @Test
    public void testLoadResource() throws IOException {
        String testContent = "test content";
        InputStream testStream = new ByteArrayInputStream(testContent.getBytes());

        when(resourceLoaderProvider.getLoader("schemas/user.json")).thenReturn(resourceLoader);
        when(resourceLoader.load("schemas/user.json")).thenReturn(testStream);

        InputStream result = context.loadResource();

        assertNotNull("Result should not be null", result);
        assertEquals("Stream should match", testStream, result);

        verify(resourceLoaderProvider).getLoader("schemas/user.json");
        verify(resourceLoader).load("schemas/user.json");
    }

    @Test(expected = IOException.class)
    public void testLoadResourceWithIOException() throws IOException {
        when(resourceLoaderProvider.getLoader("schemas/user.json")).thenReturn(resourceLoader);
        when(resourceLoader.load("schemas/user.json")).thenThrow(new IOException("File not found"));

        context.loadResource();
    }

    @Test
    public void testContextWithAllFalseFlags() {
        SchemaContext falseContext = new SchemaContext(
                "test.json",
                SchemaType.AUTO,
                "",
                "$$",
                false,
                false,
                false,
                false,
                false,
                false,
                resourceLoaderProvider,
                logger
        );

        assertFalse("isBuilder should be false", falseContext.isBuilder());
        assertFalse("isJacksonAnnotations should be false", falseContext.isJacksonAnnotations());
        assertFalse("isJaxbAnnotations should be false", falseContext.isJaxbAnnotations());
        assertFalse("isValidationAnnotations should be false", falseContext.isValidationAnnotations());
        assertFalse("isSerializable should be false", falseContext.isSerializable());
        assertFalse("isGenerateHelperMethods should be false", falseContext.isGenerateHelperMethods());
    }

    @Test
    public void testContextWithAllTrueFlags() {
        SchemaContext trueContext = new SchemaContext(
                "test.json",
                SchemaType.OPENAPI,
                "#/components/schemas/Pet",
                "Generated$$",
                true,
                true,
                true,
                true,
                true,
                true,
                resourceLoaderProvider,
                logger
        );

        assertTrue("isBuilder should be true", trueContext.isBuilder());
        assertTrue("isJacksonAnnotations should be true", trueContext.isJacksonAnnotations());
        assertTrue("isJaxbAnnotations should be true", trueContext.isJaxbAnnotations());
        assertTrue("isValidationAnnotations should be true", trueContext.isValidationAnnotations());
        assertTrue("isSerializable should be true", trueContext.isSerializable());
        assertTrue("isGenerateHelperMethods should be true", trueContext.isGenerateHelperMethods());
    }

    @Test
    public void testContextWithDifferentSchemaTypes() {
        SchemaContext autoContext = new SchemaContext(
                "test.json", SchemaType.AUTO, "", "$$",
                false, false, false, false, false, false,
                resourceLoaderProvider, logger
        );
        assertEquals("Type should be AUTO", SchemaType.AUTO, autoContext.getType());

        SchemaContext jsonSchemaContext = new SchemaContext(
                "test.json", SchemaType.JSON_SCHEMA, "", "$$",
                false, false, false, false, false, false,
                resourceLoaderProvider, logger
        );
        assertEquals("Type should be JSON_SCHEMA", SchemaType.JSON_SCHEMA, jsonSchemaContext.getType());

        SchemaContext openApiContext = new SchemaContext(
                "test.json", SchemaType.OPENAPI, "", "$$",
                false, false, false, false, false, false,
                resourceLoaderProvider, logger
        );
        assertEquals("Type should be OPENAPI", SchemaType.OPENAPI, openApiContext.getType());

        SchemaContext xmlSchemaContext = new SchemaContext(
                "test.json", SchemaType.XML_SCHEMA, "", "$$",
                false, false, false, false, false, false,
                resourceLoaderProvider, logger
        );
        assertEquals("Type should be XML_SCHEMA", SchemaType.XML_SCHEMA, xmlSchemaContext.getType());
    }

    @Test
    public void testContextWithEmptyStrings() {
        SchemaContext emptyContext = new SchemaContext(
                "", SchemaType.AUTO, "", "",
                false, false, false, false, false, false,
                resourceLoaderProvider, logger
        );

        assertEquals("Location should be empty", "", emptyContext.getLocation());
        assertEquals("SchemaRef should be empty", "", emptyContext.getSchemaRef());
        assertEquals("GeneratedClassName should be empty", "", emptyContext.getGeneratedClassName());
    }

    @Test
    public void testContextWithNullSchemaRef() {
        SchemaContext nullRefContext = new SchemaContext(
                "test.json", SchemaType.JSON_SCHEMA, null, "$$",
                false, false, false, false, false, false,
                resourceLoaderProvider, logger
        );

        assertNull("SchemaRef should be null", nullRefContext.getSchemaRef());
    }
}
