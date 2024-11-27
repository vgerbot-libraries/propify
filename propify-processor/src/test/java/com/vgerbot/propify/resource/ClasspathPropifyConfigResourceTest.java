package com.vgerbot.propify.resource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ClasspathPropifyConfigResourceTest {

    private ClasspathPropifyConfigResource resource;

    @Mock private ProcessingEnvironment processingEnv;
    @Mock private Filer filer;
    @Mock private Messager messager;
    @Mock private FileObject fileObject;

    @Before
    public void setUp() {
        resource = new ClasspathPropifyConfigResource();
        when(processingEnv.getFiler()).thenReturn(filer);
        when(processingEnv.getMessager()).thenReturn(messager);
    }

    @Test
    public void testAccept_WithClasspathPrefix_ReturnsTrue() {
        assertTrue(resource.accept("classpath:config/test.yaml"));
    }

    @Test
    public void testAccept_WithoutClasspathPrefix_ReturnsFalse() {
        assertFalse(resource.accept("config/test.yaml"));
    }

    @Test
    public void testAccept_WithEmptyString_ReturnsFalse() {
        assertFalse(resource.accept(""));
    }

    @Test
    public void testAccept_WithNullLocation_ReturnsFalse() {
        assertFalse(resource.accept(null));
    }

    @Test
    public void testLoad_ValidResource_LoadsSuccessfully() throws IOException {
        // Setup
        String location = "classpath:config/test.yaml";
        String expectedContent = "test content";
        ByteArrayInputStream expectedStream = new ByteArrayInputStream(expectedContent.getBytes());
        
        when(filer.getResource(eq(StandardLocation.CLASS_PATH), eq(""), eq("config/test.yaml")))
            .thenReturn(fileObject);
        when(fileObject.openInputStream()).thenReturn(expectedStream);

        // Execute
        InputStream result = resource.load(processingEnv, location);

        // Verify
        assertNotNull(result);
        verify(messager).printMessage(eq(Diagnostic.Kind.NOTE), contains("loading classpath source"));
        
        // Verify content
        byte[] buffer = new byte[expectedContent.length()];
        result.read(buffer);
        assertEquals(expectedContent, new String(buffer));
    }

    @Test(expected = IOException.class)
    public void testLoad_NonexistentResource_ThrowsIOException() throws IOException {
        // Setup
        String location = "classpath:nonexistent.yaml";
        when(filer.getResource(any(), any(), any())).thenThrow(new IOException("Resource not found"));

        // Execute
        resource.load(processingEnv, location);
    }

    @Test
    public void testLoad_TrimsWhitespace_InFilePath() throws IOException {
        // Setup
        String location = "classpath:  config/test.yaml  ";
        when(filer.getResource(eq(StandardLocation.CLASS_PATH), eq(""), eq("config/test.yaml")))
            .thenReturn(fileObject);
        when(fileObject.openInputStream())
            .thenReturn(new ByteArrayInputStream("content".getBytes()));

        // Execute
        resource.load(processingEnv, location);

        // Verify correct path was used
        verify(filer).getResource(StandardLocation.CLASS_PATH, "", "config/test.yaml");
    }

    @Test
    public void testLoad_HandlesEmptyFile_ReturnsEmptyStream() throws IOException {
        // Setup
        String location = "classpath:empty.yaml";
        when(filer.getResource(any(), any(), any())).thenReturn(fileObject);
        when(fileObject.openInputStream()).thenReturn(new ByteArrayInputStream(new byte[0]));

        // Execute
        InputStream result = resource.load(processingEnv, location);

        // Verify
        assertEquals(0, result.available());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testLoad_WithoutClasspathPrefix_ThrowsException() throws IOException {
        resource.load(processingEnv, "config/test.yaml");
    }

    @Test
    public void testLoad_WithNestedPath_HandlesCorrectly() throws IOException {
        // Setup
        String location = "classpath:com/example/config/test.yaml";
        when(filer.getResource(eq(StandardLocation.CLASS_PATH), eq(""), eq("com/example/config/test.yaml")))
            .thenReturn(fileObject);
        when(fileObject.openInputStream())
            .thenReturn(new ByteArrayInputStream("content".getBytes()));

        // Execute
        InputStream result = resource.load(processingEnv, location);

        // Verify
        assertNotNull(result);
        verify(filer).getResource(StandardLocation.CLASS_PATH, "", "com/example/config/test.yaml");
    }

    @Test
    public void testLoad_LogsCorrectMessage() throws IOException {
        // Setup
        String location = "classpath:test.yaml";
        when(filer.getResource(any(), any(), any())).thenReturn(fileObject);
        when(fileObject.openInputStream())
            .thenReturn(new ByteArrayInputStream("content".getBytes()));

        // Execute
        resource.load(processingEnv, location);

        // Verify correct message was logged
        verify(messager).printMessage(
            eq(Diagnostic.Kind.NOTE),
            eq("loading classpath source: test.yaml")
        );
    }
}
