package com.vgerbot.propify;

import com.vgerbot.propify.service.ServiceLoaderWrapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.annotation.processing.ProcessingEnvironment;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class PropifyContextTest {

    @Mock private ProcessingEnvironment processingEnv;
    @Mock private Propify propifyAnnotation;
    @Mock private ServiceLoaderWrapper<PropifyConfigParser> mockParserLoader;
    @Mock private ServiceLoaderWrapper<PropifyConfigResource> mockResourceLoader;
    @Mock private PropifyConfigParser yamlParser;
    @Mock private PropifyConfigParser propertiesParser;
    @Mock private PropifyConfigResource classpathResource;

    @Before
    public void setUp() throws IOException {
        // Setup mock parsers
        when(yamlParser.accept("application/yaml")).thenReturn(true);
        when(propertiesParser.accept("text/x-java-properties")).thenReturn(true);
        
        // Setup mock iterators
        when(mockParserLoader.iterator()).thenReturn(
            Arrays.asList(yamlParser, propertiesParser).iterator()
        );

        // Setup mock resource loader
        when(classpathResource.accept(any())).thenReturn(true);
        when(mockResourceLoader.iterator()).thenReturn(
            Collections.singletonList(classpathResource).iterator()
        );
    }

    @Test
    public void testConstructor_WithValidAnnotation_InitializesCorrectly() throws IOException {
        when(propifyAnnotation.location()).thenReturn("test.yaml");
        when(propifyAnnotation.mediaType()).thenReturn("application/yaml");
        when(propifyAnnotation.generatedClassName()).thenReturn("TestClass");

        PropifyContext context = new PropifyContext(propifyAnnotation, processingEnv, mockParserLoader, mockResourceLoader);

        assertEquals(processingEnv, context.getProcessingEnvironment());
        assertNotNull(context.getParser());
    }

    @Test
    public void testGetParser_WithYamlMediaType_ReturnsYamlParser() throws IOException {
        when(propifyAnnotation.location()).thenReturn("test.yaml");
        when(propifyAnnotation.mediaType()).thenReturn("application/yaml");

        PropifyContext context = new PropifyContext(propifyAnnotation, processingEnv, mockParserLoader, mockResourceLoader);
        PropifyConfigParser parser = context.getParser();

        assertSame(yamlParser, parser);
    }

    @Test
    public void testGetParser_WithPropertiesMediaType_ReturnsPropertiesParser() throws IOException {
        when(propifyAnnotation.location()).thenReturn("test.properties");
        when(propifyAnnotation.mediaType()).thenReturn("text/x-java-properties");

        PropifyContext context = new PropifyContext(propifyAnnotation, processingEnv, mockParserLoader, mockResourceLoader);
        PropifyConfigParser parser = context.getParser();

        assertSame(propertiesParser, parser);
    }

    @Test(expected = IOException.class)
    public void testGetParser_WithUnsupportedMediaType_ThrowsException() throws IOException {
        when(propifyAnnotation.location()).thenReturn("test.unknown");
        when(propifyAnnotation.mediaType()).thenReturn("application/unknown");

        PropifyContext context = new PropifyContext(propifyAnnotation, processingEnv, mockParserLoader, mockResourceLoader);
        context.getParser();
    }

    @Test
    public void testLoadResource_WithValidLocation_LoadsSuccessfully() throws IOException {
        when(propifyAnnotation.location()).thenReturn("test.yaml");
        when(propifyAnnotation.mediaType()).thenReturn("application/yaml");
        
        InputStream mockStream = new ByteArrayInputStream("test".getBytes());
        when(classpathResource.load(eq(processingEnv), any())).thenReturn(mockStream);

        PropifyContext context = new PropifyContext(propifyAnnotation, processingEnv, mockParserLoader, mockResourceLoader);
        InputStream result = context.loadResource();

        assertNotNull(result);
        verify(classpathResource).load(eq(processingEnv), eq("test.yaml"));
    }

    @Test(expected = IOException.class)
    public void testLoadResource_WithNoSuitableLoader_ThrowsException() throws IOException {
        when(propifyAnnotation.location()).thenReturn("test.yaml");
        when(propifyAnnotation.mediaType()).thenReturn("application/yaml");
        when(classpathResource.accept(any())).thenReturn(false);

        PropifyContext context = new PropifyContext(propifyAnnotation, processingEnv, mockParserLoader, mockResourceLoader);
        context.loadResource();
    }

    @Test
    public void testGetClassName_WithEmptyGeneratedClassName_AppendsDefaultSuffix() {
        when(propifyAnnotation.location()).thenReturn("test.yaml");
        when(propifyAnnotation.mediaType()).thenReturn("application/yaml");
        when(propifyAnnotation.generatedClassName()).thenReturn("");

        PropifyContext context = new PropifyContext(propifyAnnotation, processingEnv, mockParserLoader, mockResourceLoader);
        String result = context.getClassName("TestClass");

        assertEquals("TestClassPropify", result);
    }

    @Test
    public void testGetClassName_WithCustomGeneratedClassName_ReplacesPlaceholder() {
        when(propifyAnnotation.location()).thenReturn("test.yaml");
        when(propifyAnnotation.mediaType()).thenReturn("application/yaml");
        when(propifyAnnotation.generatedClassName()).thenReturn("Custom$$Config");

        PropifyContext context = new PropifyContext(propifyAnnotation, processingEnv, mockParserLoader, mockResourceLoader);
        String result = context.getClassName("TestClass");

        assertEquals("CustomTestClassConfig", result);
    }

    @Test
    public void testMediaTypeInference_WithEmptyMediaType_InfersFromLocation() throws IOException {
        when(propifyAnnotation.location()).thenReturn("test.yaml");
        when(propifyAnnotation.mediaType()).thenReturn("");

        PropifyContext context = new PropifyContext(propifyAnnotation, processingEnv, mockParserLoader, mockResourceLoader);
        PropifyConfigParser parser = context.getParser();

        assertSame(yamlParser, parser);
        verify(yamlParser).accept("application/yaml");
    }

    @Test
    public void testMediaTypeInference_WithPropertiesFile_InfersCorrectly() throws IOException {
        when(propifyAnnotation.location()).thenReturn("test.properties");
        when(propifyAnnotation.mediaType()).thenReturn("");

        PropifyContext context = new PropifyContext(propifyAnnotation, processingEnv, mockParserLoader, mockResourceLoader);
        PropifyConfigParser parser = context.getParser();

        assertSame(propertiesParser, parser);
        verify(propertiesParser).accept("text/x-java-properties");
    }

    @Test
    public void testGetClassName_WithComplexPlaceholderPattern_HandlesCorrectly() {
        when(propifyAnnotation.location()).thenReturn("test.yaml");
        when(propifyAnnotation.mediaType()).thenReturn("application/yaml");
        when(propifyAnnotation.generatedClassName()).thenReturn("Prefix$$Suffix");

        PropifyContext context = new PropifyContext(propifyAnnotation, processingEnv, mockParserLoader, mockResourceLoader);
        String result = context.getClassName("TestClass");

        assertEquals("PrefixTestClassSuffix", result);
    }
}
