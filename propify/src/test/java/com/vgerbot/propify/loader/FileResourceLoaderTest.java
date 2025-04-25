package com.vgerbot.propify.loader;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import static org.junit.Assert.*;

/**
 * Tests for the FileResourceLoader class.
 */
public class FileResourceLoaderTest {

    private FileResourceLoader loader;
    private static final String FILE_PREFIX = "file:";

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();
    private File testFile;
    private String testContent = "test file content";

    @Before
    public void setUp() throws IOException {
        loader = new FileResourceLoader();
        
        // Create a temporary test file with content
        testFile = tempFolder.newFile("test-config.properties");
        try (FileWriter writer = new FileWriter(testFile)) {
            writer.write(testContent);
        }
    }

    @Test
    public void testAcceptWithFileUrl() {
        assertTrue(loader.accept(FILE_PREFIX + "/path/to/config.properties"));
    }

    @Test
    public void testAcceptWithHttpUrl() {
        assertFalse(loader.accept("http://example.com/config.properties"));
    }

    @Test
    public void testAcceptWithClasspathUrl() {
        assertFalse(loader.accept("classpath:config/config.properties"));
    }

    @Test
    public void testAcceptWithNullLocation() {
        assertFalse(loader.accept(null));
    }

    @Test
    public void testSuccessfulLoad() throws IOException {
        // Use the temporary file created in setUp
        String location = FILE_PREFIX + testFile.getAbsolutePath();
        
        try (InputStream input = loader.load(location)) {
            assertNotNull("Input stream should not be null", input);
            
            // Read the content from the stream
            String content = readStreamAsString(input);
            assertEquals("File content should match expected", testContent, content);
        }
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testLoadWithNullLocation() throws IOException {
        loader.load(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testLoadWithInvalidProtocol() throws IOException {
        loader.load("http://example.com/config.properties");
    }
    
    @Test(expected = IOException.class)
    public void testLoadNonExistentFile() throws IOException {
        // Create a path to a file that doesn't exist
        File nonExistentFile = new File(tempFolder.getRoot(), "non-existent.properties");
        String location = FILE_PREFIX + nonExistentFile.getAbsolutePath();
        
        loader.load(location);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testLoadWithEmptyPath() throws IOException {
        loader.load(FILE_PREFIX);
    }
    
    @Test(expected = IOException.class)
    public void testLoadDirectory() throws IOException {
        // Try to load a directory instead of a file
        String location = FILE_PREFIX + tempFolder.getRoot().getAbsolutePath();
        
        loader.load(location);
    }

    /**
     * Helper method to read an input stream into a string
     */
    private String readStreamAsString(InputStream input) throws IOException {
        try (Scanner scanner = new Scanner(input, StandardCharsets.UTF_8.name())) {
            return scanner.useDelimiter("\\A").next();
        }
    }
} 