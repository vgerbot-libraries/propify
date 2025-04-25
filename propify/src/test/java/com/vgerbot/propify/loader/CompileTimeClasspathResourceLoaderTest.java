package com.vgerbot.propify.loader;

import com.vgerbot.propify.core.ResourceLoader;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class CompileTimeClasspathResourceLoaderTest {

    @Mock
    private ProcessingEnvironment processingEnvironment;

    @Mock
    private Filer filer;

    @Mock
    private FileObject fileObject;

    @Mock
    private Messager messager;

    private CompileTimeClasspathResourceLoader loader;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        when(processingEnvironment.getFiler()).thenReturn(filer);
        when(processingEnvironment.getMessager()).thenReturn(messager);
        loader = new CompileTimeClasspathResourceLoader(processingEnvironment);
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

    @Test
    public void testLoadResource() throws IOException {
        String location = "classpath:test/resources/config.properties";
        String filePath = "test/resources/config.properties";
        byte[] content = "key=value".getBytes();
        ByteArrayInputStream inputStream = new ByteArrayInputStream(content);

        when(filer.getResource(StandardLocation.CLASS_PATH, "", filePath)).thenReturn(fileObject);
        when(fileObject.openInputStream()).thenReturn(inputStream);

        InputStream result = loader.load(location);

        assertNotNull("Loaded resource should not be null", result);
        verify(processingEnvironment).getFiler();
        verify(filer).getResource(StandardLocation.CLASS_PATH, "", filePath);
        verify(fileObject).openInputStream();
        verify(processingEnvironment).getMessager();
        verify(messager).printMessage(eq(Diagnostic.Kind.NOTE), anyString());

        // Verify the content is correct
        byte[] buffer = new byte[content.length];
        result.read(buffer);
        assertArrayEquals(content, buffer);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithNullProcessingEnvironment() {
        new CompileTimeClasspathResourceLoader(null);
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

    @Test(expected = IOException.class)
    public void testLoadResourceWithIOException() throws IOException {
        String location = "classpath:test/resources/config.properties";
        String filePath = "test/resources/config.properties";

        when(filer.getResource(StandardLocation.CLASS_PATH, "", filePath)).thenReturn(fileObject);
        when(fileObject.openInputStream()).thenThrow(new IOException("Resource not found"));

        loader.load(location);
    }

    @Test(expected = IOException.class)
    public void testLoadResourceWithNullFileObject() throws IOException {
        String location = "classpath:test/resources/config.properties";
        String filePath = "test/resources/config.properties";

        when(filer.getResource(StandardLocation.CLASS_PATH, "", filePath)).thenReturn(null);

        loader.load(location);
    }

    @Test(expected = IOException.class)
    public void testLoadResourceWithGenericException() throws IOException {
        String location = "classpath:test/resources/config.properties";
        String filePath = "test/resources/config.properties";

        when(filer.getResource(StandardLocation.CLASS_PATH, "", filePath)).thenThrow(new RuntimeException("Unexpected error"));

        loader.load(location);
    }
} 