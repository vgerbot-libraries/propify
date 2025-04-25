package com.vgerbot.propify.core;

import com.vgerbot.propify.logger.Logger;
import com.vgerbot.propify.lookup.PropifyLookup;
import org.apache.commons.configuration2.interpol.Lookup;
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

public class PropifyContextTest {

    @Mock
    private ResourceLoaderProvider resourceLoaderProvider;

    @Mock
    private ResourceLoader resourceLoader;

    @Mock
    private Logger logger;

    private PropifyContext context;
    private static final String TEST_LOCATION = "classpath:config.yml";
    private static final String TEST_MEDIA_TYPE = "application/yaml";
    private static final String TEST_CLASS_NAME = "Config$$Test";
    private static final char TEST_DELIMITER = ',';
    private static final String[] TEST_LOOKUPS = new String[] { TestLookup.class.getName() };

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        when(resourceLoaderProvider.getLoader(TEST_LOCATION)).thenReturn(resourceLoader);

        context = new PropifyContext(
                TEST_LOCATION,
                TEST_MEDIA_TYPE,
                TEST_CLASS_NAME,
                TEST_DELIMITER,
                TEST_LOOKUPS,
                resourceLoaderProvider,
                logger);
    }

    @Test
    public void testConstructorAndGetters() {
        assertEquals(TEST_LOCATION, context.getLocation());
        assertEquals(TEST_MEDIA_TYPE, context.getMediaType());
        assertEquals(TEST_CLASS_NAME, context.getGeneratedClassName());
        assertEquals(TEST_DELIMITER, context.getListDelimiter());
        assertArrayEquals(TEST_LOOKUPS, context.getLookups());
        assertEquals(logger, context.getLogger());
    }

    @Test
    public void testConstructorWithNullClassName() {
        PropifyContext nullClassNameContext = new PropifyContext(
                TEST_LOCATION,
                TEST_MEDIA_TYPE,
                null,
                TEST_DELIMITER,
                TEST_LOOKUPS,
                resourceLoaderProvider,
                logger);
        assertEquals("", nullClassNameContext.getGeneratedClassName());

        String result = nullClassNameContext.generateClassName("TestConfig");
        assertEquals("TestConfigPropify", result);
    }
    @Test
    public void testEmptyClassNamePattern() {
        PropifyContext customContext = new PropifyContext(
                "config.yml",
                "application/yaml",
                "",
                ',',
                new String[]{},
                resourceLoaderProvider,
                logger
        );

        String result = customContext.generateClassName("TestConfig");
        assertEquals("TestConfigPropify", result);
    }
    @Test
    public void testCustomClassNamePattern() {
        PropifyContext customContext = new PropifyContext(
                "config.yml",
                "application/yaml",
                "Custom$$",
                ',',
                new String[]{},
                resourceLoaderProvider,
                logger
        );

        String result = customContext.generateClassName("TestConfig");
        assertEquals("CustomTestConfig", result);
    }

    @Test
    public void testGetResourceLoader() {
        ResourceLoader loader = context.getResourceLoader();
        assertNotNull("Resource loader should not be null", loader);
        assertEquals(resourceLoader, loader);
        verify(resourceLoaderProvider).getLoader(TEST_LOCATION);
    }

    @Test
    public void testLoadResource() throws IOException {
        byte[] testData = "test data".getBytes();
        ByteArrayInputStream testStream = new ByteArrayInputStream(testData);
        when(resourceLoader.load(TEST_LOCATION)).thenReturn(testStream);

        InputStream result = context.loadResource();
        assertNotNull("Loaded resource should not be null", result);
        verify(resourceLoader).load(TEST_LOCATION);
    }

    @Test(expected = IOException.class)
    public void testLoadResourceWithError() throws IOException {
        when(resourceLoader.load(TEST_LOCATION)).thenThrow(new IOException("Resource not found"));
        context.loadResource();
    }

    @Test
    public void testGenerateClassNameWithPattern() {
        String originClassName = "TestConfig";
        String result = context.generateClassName(originClassName);
        assertEquals("ConfigTestConfigTest", result);
    }

    @Test
    public void testGenerateClassNameWithEmptyPattern() {
        PropifyContext emptyPatternContext = new PropifyContext(
                TEST_LOCATION,
                TEST_MEDIA_TYPE,
                "",
                TEST_DELIMITER,
                TEST_LOOKUPS,
                resourceLoaderProvider,
                logger);
        String result = emptyPatternContext.generateClassName("TestConfig");
        assertEquals("TestConfigPropify", result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGenerateClassNameWithNullOriginName() {
        context.generateClassName(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGenerateClassNameWithEmptyOriginName() {
        context.generateClassName("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGenerateClassNameWithBlankOriginName() {
        context.generateClassName("   ");
    }

    // Test class for lookup functionality
    public static class TestLookup implements PropifyLookup {
        @Override
        public String getPrefix() {
            return "test";
        }

        @Override
        public String lookup(String key) {
            return "test-" + key;
        }
    }

    @Test
    public void testGetAllLookups() {
        Map<String, Lookup> lookups = context.getAllLookups();
        assertNotNull("Lookups map should not be null", lookups);
        assertTrue("Should contain test lookup", lookups.containsKey("test"));

        Lookup lookup = lookups.get("test");
        assertNotNull("Lookup should not be null", lookup);
        assertEquals("test-value", lookup.lookup("value"));
    }

    @Test(expected = RuntimeException.class)
    public void testGetAllLookupsWithInvalidClass() {
        PropifyContext invalidContext = new PropifyContext(
                TEST_LOCATION,
                TEST_MEDIA_TYPE,
                TEST_CLASS_NAME,
                TEST_DELIMITER,
                new String[] { "com.invalid.Lookup" },
                resourceLoaderProvider,
                logger);
        invalidContext.getAllLookups();
    }
}