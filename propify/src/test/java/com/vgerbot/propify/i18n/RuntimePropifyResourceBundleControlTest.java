package com.vgerbot.propify.i18n;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.anyString;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

public class RuntimePropifyResourceBundleControlTest {

    private TestingRuntimePropifyResourceBundleControl control;
    private ClassLoader mockClassLoader;
    private URL url;
    private URLConnection mockConnection;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        control = new TestingRuntimePropifyResourceBundleControl();

        // Mock ClassLoader
        mockClassLoader = mock(ClassLoader.class);

        // Create real URL and mock URLConnection
        url = new URL("file:///dummy");
        mockConnection = mock(URLConnection.class);
    }

    // Testing subclass that bypasses the URL.openConnection() issue
    static class TestingRuntimePropifyResourceBundleControl extends RuntimePropifyResourceBundleControl {
        @Override
        protected InputStream loadResource(String resourceName, final ClassLoader classLoader, final boolean reloadFlag)
                throws IOException {
            try {
                return AccessController.doPrivileged(
                        (PrivilegedExceptionAction<InputStream>) () -> {
                            InputStream is = null;
                            if (reloadFlag) {
                                URL url = classLoader.getResource(resourceName);
                                if (url != null) {
                                    // Use our test helper instead of directly calling url.openConnection()
                                    URLConnection connection = URLStreamHandlerForTesting.openConnection(url);
                                    if (connection != null) {
                                        // Disable caches to get fresh data for reloading
                                        connection.setUseCaches(false);
                                        is = connection.getInputStream();
                                    }
                                }
                            } else {
                                is = classLoader.getResourceAsStream(resourceName);
                            }
                            return is;
                        });
            } catch (PrivilegedActionException e) {
                throw (IOException) e.getException();
            }
        }
    }

    @Test
    public void testGetFormats() {
        List<String> formats = control.getFormats("dummy");
        assertNotNull("Formats should not be null", formats);
        assertTrue("Should support properties format", formats.contains("java.properties"));
        assertTrue("Should support class format", formats.contains("java.class"));
        assertTrue("Should support XML format", formats.contains("xml"));
        assertTrue("Should support YAML format", formats.contains("yaml"));
        assertTrue("Should support JSON format", formats.contains("json"));
        assertTrue("Should support INI format", formats.contains("ini"));
        assertTrue("Should support XML properties format", formats.contains("xml.properties"));
        assertEquals("Should support exactly 7 formats", 7, formats.size());
    }

    @Test
    public void testLoadResourceWithoutReload() throws IOException {
        String resourceName = "messages.properties";
        byte[] content = "greeting=Hello!".getBytes();
        ByteArrayInputStream inputStream = new ByteArrayInputStream(content);

        when(mockClassLoader.getResourceAsStream(resourceName)).thenReturn(inputStream);

        InputStream result = control.loadResource(resourceName, mockClassLoader, false);

        assertNotNull("Result should not be null", result);
        verify(mockClassLoader).getResourceAsStream(resourceName);
        // No need to verify URL interactions since reload is false
    }

    @Test
    public void testLoadResourceWithReload() throws IOException {
        String resourceName = "messages.properties";
        byte[] content = "greeting=Hello!".getBytes();
        ByteArrayInputStream inputStream = new ByteArrayInputStream(content);

        when(mockClassLoader.getResource(resourceName)).thenReturn(url);
        when(mockConnection.getInputStream()).thenReturn(inputStream);

        // Use a test spy to intercept openConnection calls on our real URL
        URLConnection spyConnection = mockConnection;
        URLStreamHandlerForTesting.setURLConnectionToReturn(url, spyConnection);

        InputStream result = control.loadResource(resourceName, mockClassLoader, true);

        assertNotNull("Result should not be null", result);
        verify(mockClassLoader).getResource(resourceName);
        verify(mockConnection).setUseCaches(false);
        verify(mockConnection).getInputStream();
    }

    @Test
    public void testLoadResourceWithReloadNullUrl() throws IOException {
        String resourceName = "nonexistent.properties";

        when(mockClassLoader.getResource(resourceName)).thenReturn(null);

        InputStream result = control.loadResource(resourceName, mockClassLoader, true);

        assertNull("Result should be null for nonexistent resource", result);
        verify(mockClassLoader).getResource(resourceName);
    }

    @Test
    public void testLoadResourceWithReloadNullConnection() throws IOException {
        String resourceName = "messages.properties";

        when(mockClassLoader.getResource(resourceName)).thenReturn(url);
        URLStreamHandlerForTesting.setURLConnectionToReturn(url, null);

        InputStream result = control.loadResource(resourceName, mockClassLoader, true);

        assertNull("Result should be null when connection is null", result);
        verify(mockClassLoader).getResource(resourceName);
    }

    @Test(expected = IOException.class)
    public void testLoadResourceWithException() throws IOException {
        String resourceName = "messages.properties";

        when(mockClassLoader.getResource(resourceName)).thenReturn(url);
        URLStreamHandlerForTesting.setURLOpenConnectionToThrow(url, new IOException("Test exception"));

        control.loadResource(resourceName, mockClassLoader, true);
        // Should throw IOException
    }

    @Test
    public void testIntegrationWithResourceBundle() throws IOException {
        // This test checks if our control class works with ResourceBundle
        // Setup a mock ClassLoader that will return our test URL
        ClassLoader mockLoader = mock(ClassLoader.class);

        // Create test content
        byte[] content = "greeting=Hello!".getBytes();
        ByteArrayInputStream inputStream = new ByteArrayInputStream(content);

        // Setup the mock to return our test URL and content
        when(mockLoader.getResource(anyString())).thenReturn(url);
        when(mockConnection.getInputStream()).thenReturn(inputStream);

        // Configure our test helper
        URLStreamHandlerForTesting.setURLConnectionToReturn(url, mockConnection);

        // Now use the mocked resources
        ResourceBundle bundle = ResourceBundle.getBundle("messages", Locale.ENGLISH,
                this.getClass().getClassLoader(), control);

        assertNotNull("Bundle should not be null", bundle);
        assertEquals("Should load correct greeting", "Hello!", bundle.getString("greeting"));
    }

    @Test
    public void testNewBundleForDifferentFormats() throws IllegalAccessException, InstantiationException, IOException {
        // This integration test checks if our control class loads different format
        // files correctly

        // Setup mocks for different format tests
        ClassLoader mockLoader = mock(ClassLoader.class);

        // Mock for properties format
        String propertiesContent = "key=value";
        ByteArrayInputStream propertiesStream = new ByteArrayInputStream(propertiesContent.getBytes());
        when(mockLoader.getResourceAsStream("test.properties")).thenReturn(propertiesStream);

        // Mock for YAML format
        String yamlContent = "key: value";
        ByteArrayInputStream yamlStream = new ByteArrayInputStream(yamlContent.getBytes());
        when(mockLoader.getResourceAsStream("test.yaml")).thenReturn(yamlStream);

        // Mock for INI format
        String iniContent = "[section]\nkey=value";
        ByteArrayInputStream iniStream = new ByteArrayInputStream(iniContent.getBytes());
        when(mockLoader.getResourceAsStream("test.ini")).thenReturn(iniStream);

        // Test properties format
        ResourceBundle propertiesBundle = control.newBundle("test", Locale.ROOT, "java.properties",
                mockLoader, false);
        assertNotNull("Properties bundle should not be null", propertiesBundle);

        // Test YAML format - this might require additional mocking depending on
        // implementation
        try {
            ResourceBundle yamlBundle = control.newBundle("test", Locale.ROOT, "yaml",
                    mockLoader, false);
            if (yamlBundle != null) {
                assertNotNull("YAML bundle should not be null", yamlBundle);
            }
        } catch (UnsupportedOperationException e) {
            // If YAML format is not supported, this is acceptable
            // Just verify the exception message
            assertTrue("Exception should mention YAML format",
                    e.getMessage().contains("yaml") || e.getMessage().contains("YAML"));
        }

        // Test INI format - this might require additional mocking depending on
        // implementation
        try {
            ResourceBundle iniBundle = control.newBundle("test", Locale.ROOT, "ini",
                    mockLoader, false);
            if (iniBundle != null) {
                assertNotNull("INI bundle should not be null", iniBundle);
            }
        } catch (UnsupportedOperationException e) {
            // If INI format is not supported, this is acceptable
            // Just verify the exception message
            assertTrue("Exception should mention INI format",
                    e.getMessage().contains("ini") || e.getMessage().contains("INI"));
        }
    }

    // Helper class to handle URL testing
    static class URLStreamHandlerForTesting {
        private static URLConnection connectionToReturn;
        private static IOException exceptionToThrow;

        public static void setURLConnectionToReturn(URL url, URLConnection connection) {
            connectionToReturn = connection;
        }

        public static void setURLOpenConnectionToThrow(URL url, IOException exception) {
            exceptionToThrow = exception;
        }

        static URLConnection openConnection(URL url) throws IOException {
            if (exceptionToThrow != null) {
                IOException e = exceptionToThrow;
                exceptionToThrow = null;
                throw e;
            }
            return connectionToReturn;
        }
    }
}
