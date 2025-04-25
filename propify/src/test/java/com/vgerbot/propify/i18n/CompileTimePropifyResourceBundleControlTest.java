package com.vgerbot.propify.i18n;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class CompileTimePropifyResourceBundleControlTest {

    @Mock
    private ProcessingEnvironment processingEnvironment;
    
    @Mock
    private Filer filer;
    
    @Mock
    private FileObject fileObject;
    
    private CompileTimePropifyResourceBundleControl control;
    
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        when(processingEnvironment.getFiler()).thenReturn(filer);
        control = new CompileTimePropifyResourceBundleControl(processingEnvironment);
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
    public void testLoadResource() throws IOException {
        String resourceName = "messages.properties";
        byte[] content = "greeting=Hello!".getBytes();
        ByteArrayInputStream inputStream = new ByteArrayInputStream(content);
        
        when(filer.getResource(StandardLocation.CLASS_PATH, "", resourceName)).thenReturn(fileObject);
        when(fileObject.openInputStream()).thenReturn(inputStream);
        
        InputStream result = control.loadResource(resourceName, null, false);
        
        assertNotNull("Result should not be null", result);
        verify(filer).getResource(StandardLocation.CLASS_PATH, "", resourceName);
        verify(fileObject).openInputStream();
        
        // Read from the stream to verify it contains the expected content
        byte[] buffer = new byte[content.length];
        result.read(buffer);
        assertArrayEquals(content, buffer);
    }
    
    @Test(expected = IOException.class)
    public void testLoadResourceWithException() throws IOException {
        String resourceName = "nonexistent.properties";
        
        when(filer.getResource(StandardLocation.CLASS_PATH, "", resourceName)).thenReturn(fileObject);
        when(fileObject.openInputStream()).thenThrow(new IOException("Test exception"));
        
        control.loadResource(resourceName, null, false);
        // Should throw IOException
    }
    
    @Test
    public void testLoadResourceNullFileObject() throws IOException {
        String resourceName = "nonexistent.properties";
        
        when(filer.getResource(StandardLocation.CLASS_PATH, "", resourceName)).thenThrow(new IOException("Resource not found"));
        
        try {
            control.loadResource(resourceName, null, false);
            fail("Should throw IOException when resource not found");
        } catch (IOException e) {
            assertEquals("Resource not found", e.getMessage());
        }
    }
    
    @Test
    public void testIntegrationWithMockedNewBundle() throws IllegalAccessException, InstantiationException, IOException {
        // Setup for properties format
        String bundleName = "messages";
        String resourceName = "messages.properties";
        String propertiesContent = "greeting=Hello!";
        ByteArrayInputStream inputStream = new ByteArrayInputStream(propertiesContent.getBytes());
        
        when(filer.getResource(eq(StandardLocation.CLASS_PATH), eq(""), eq(resourceName))).thenReturn(fileObject);
        when(fileObject.openInputStream()).thenReturn(inputStream);
        
        // Test newBundle method with properties format
        ResourceBundle bundle = control.newBundle(bundleName, Locale.ROOT, "java.properties", getClass().getClassLoader(), false);
        
        assertNotNull("Bundle should not be null", bundle);
        assertEquals("Should load correct greeting", "Hello!", bundle.getString("greeting"));
    }
    
    @Test
    public void testConstructor() {
        assertNotNull("Control should be properly constructed", control);
        // Verify the processing environment is stored
        ProcessingEnvironment env = mock(ProcessingEnvironment.class);
        CompileTimePropifyResourceBundleControl newControl = new CompileTimePropifyResourceBundleControl(env);
        assertNotNull("Control should be properly constructed with a different environment", newControl);
    }
} 