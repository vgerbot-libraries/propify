package com.vgerbot.propify.loader;

import com.vgerbot.propify.core.ResourceLoader;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

/**
 * Tests for the RuntimeClasspathResourceLoader class.
 * These tests focus on the behavior that can be tested without mocking static methods.
 */
public class RuntimeClasspathResourceLoaderTest {

    private RuntimeClasspathResourceLoader loader;

    @Before
    public void setUp() {
        loader = new RuntimeClasspathResourceLoader();
    }

    @Test
    public void testImplementsResourceLoader() {
        assertTrue(loader instanceof ResourceLoader);
    }

    @Test
    public void testAcceptWithClasspathPrefix() {
        assertTrue(loader.accept("classpath:test/resources/config.properties"));
    }

    @Test
    public void testAcceptWithoutClasspathPrefix() {
        assertFalse(loader.accept("file:test/resources/config.properties"));
        assertFalse(loader.accept("http://example.com/config.properties"));
        assertFalse(loader.accept("resources/config.properties"));
    }

    @Test
    public void testAcceptWithNullLocation() {
        assertFalse(loader.accept(null));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testLoadWithNullLocation() throws IOException {
        loader.load(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testLoadWithNonClasspathLocation() throws IOException {
        loader.load("file:test/resources/config.properties");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testLoadWithEmptyPath() throws IOException {
        loader.load("classpath:");
    }
    
    /**
     * Test loading an existing resource from the classpath.
     * This test uses a resource that should be available in the test classpath.
     */
    @Test
    public void testLoadExistingResource() throws IOException {
        // This test assumes test.yml exists in the test resources
        try {
            loader.load("classpath:test.yml");
            // If we get here without exception, the test passes
            assertTrue(true);
        } catch (IOException e) {
            fail("Should be able to load test.yml from classpath: " + e.getMessage());
        }
    }
}
