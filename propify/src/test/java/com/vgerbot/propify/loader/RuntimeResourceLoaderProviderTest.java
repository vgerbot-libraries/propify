package com.vgerbot.propify.loader;

import com.vgerbot.propify.core.ResourceLoader;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests for the RuntimeResourceLoaderProvider class.
 * 
 * Note: Since RuntimeResourceLoaderProvider is a singleton that uses ServiceLoader,
 * we focus on testing its API and behavior with real instances. More comprehensive
 * tests would require dependency injection or a test-specific implementation.
 */
public class RuntimeResourceLoaderProviderTest {

    @Test
    public void testGetInstance() {
        RuntimeResourceLoaderProvider provider = RuntimeResourceLoaderProvider.getInstance();
        assertNotNull("Provider instance should not be null", provider);
        
        // Same instance should be returned on subsequent calls
        RuntimeResourceLoaderProvider secondProvider = RuntimeResourceLoaderProvider.getInstance();
        assertSame("getInstance should return the same instance", provider, secondProvider);
    }

    @Test
    public void testGetLoaderForFileLocation() {
        RuntimeResourceLoaderProvider provider = RuntimeResourceLoaderProvider.getInstance();
        ResourceLoader loader = provider.getLoader("file:/path/to/config.properties");
        assertNotNull("Loader should not be null", loader);
        assertTrue("Loader should accept file: URLs", loader.accept("file:/path/to/config.properties"));
    }

    @Test
    public void testGetLoaderForHttpLocation() {
        RuntimeResourceLoaderProvider provider = RuntimeResourceLoaderProvider.getInstance();
        ResourceLoader loader = provider.getLoader("http://example.com/config.properties");
        assertNotNull("Loader should not be null", loader);
        assertTrue("Loader should accept http: URLs", loader.accept("http://example.com/config.properties"));
    }

    @Test
    public void testGetLoaderForHttpsLocation() {
        RuntimeResourceLoaderProvider provider = RuntimeResourceLoaderProvider.getInstance();
        ResourceLoader loader = provider.getLoader("https://example.com/config.properties");
        assertNotNull("Loader should not be null", loader);
        assertTrue("Loader should accept https: URLs", loader.accept("https://example.com/config.properties"));
    }

    @Test
    public void testGetLoaderForClasspathLocation() {
        RuntimeResourceLoaderProvider provider = RuntimeResourceLoaderProvider.getInstance();
        ResourceLoader loader = provider.getLoader("classpath:config/config.properties");
        assertNotNull("Loader should not be null", loader);
        assertTrue("Loader should accept classpath: URLs", 
                 loader.accept("classpath:config/config.properties"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetLoaderWithNullLocation() {
        RuntimeResourceLoaderProvider provider = RuntimeResourceLoaderProvider.getInstance();
        provider.getLoader(null);
    }
} 