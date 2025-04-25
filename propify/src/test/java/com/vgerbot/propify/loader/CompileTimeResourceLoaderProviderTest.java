package com.vgerbot.propify.loader;

import com.vgerbot.propify.core.ResourceLoader;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.annotation.processing.ProcessingEnvironment;

import static org.junit.Assert.*;

public class CompileTimeResourceLoaderProviderTest {

    @Mock
    private ProcessingEnvironment processingEnvironment;

    private CompileTimeResourceLoaderProvider provider;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        provider = new CompileTimeResourceLoaderProvider(processingEnvironment);
    }

    @Test
    public void testGetLoaderForClasspathLocation() {
        ResourceLoader loader = provider.getLoader("classpath:test/resources/config.properties");
        assertNotNull("Loader should not be null", loader);
        assertTrue("Loader should be CompileTimeClasspathResourceLoader", 
                loader instanceof CompileTimeClasspathResourceLoader);
    }

    @Test
    public void testGetLoaderForFileLocation() {
        ResourceLoader loader = provider.getLoader("file:/path/to/config.properties");
        assertNotNull("Loader should not be null", loader);
        assertTrue("Loader should be FileResourceLoader", loader instanceof FileResourceLoader);
    }

    @Test
    public void testGetLoaderForHttpLocation() {
        ResourceLoader loader = provider.getLoader("http://example.com/config.properties");
        assertNotNull("Loader should not be null", loader);
        assertTrue("Loader should be HTTPResourceLoader", loader instanceof HTTPResourceLoader);
    }

    @Test
    public void testGetLoaderForHttpsLocation() {
        ResourceLoader loader = provider.getLoader("https://example.com/config.properties");
        assertNotNull("Loader should not be null", loader);
        assertTrue("Loader should be HTTPResourceLoader", loader instanceof HTTPResourceLoader);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetLoaderForUnsupportedLocation() {
        provider.getLoader("ftp://example.com/config.properties");
    }

    @Test(expected = IllegalArgumentException.class) 
    public void testGetLoaderForInvalidLocation() {
        provider.getLoader("invalid-location");
    }
} 