package com.vgerbot.propify;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.annotation.processing.*;
import javax.lang.model.element.*;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class PropifyProcessorTest {
    
    private PropifyProcessor processor;

    @Mock private ProcessingEnvironment processingEnv;
    @Mock private Elements elementUtils;
    @Mock private Types typeUtils;
    @Mock private Filer filer;
    @Mock private Messager messager;
    @Mock private TypeElement typeElement;
    @Mock private PackageElement packageElement;
    @Mock private Name qualifiedName;
    @Mock private Name simpleName;
    @Mock private Propify propifyAnnotation;
    @Mock private JavaFileObject sourceFile;
    @Mock private RoundEnvironment roundEnv;
    @Mock private Writer writer;

    @Before
    public void setUp() {
        processor = new PropifyProcessor();
        
        // Setup processing environment
        when(processingEnv.getElementUtils()).thenReturn(elementUtils);
        when(processingEnv.getTypeUtils()).thenReturn(typeUtils);
        when(processingEnv.getFiler()).thenReturn(filer);
        when(processingEnv.getMessager()).thenReturn(messager);
        
        processor.init(processingEnv);

        // Setup common mocks
        when(elementUtils.getPackageOf(typeElement)).thenReturn(packageElement);
        when(packageElement.getQualifiedName()).thenReturn(qualifiedName);
        when(typeElement.getSimpleName()).thenReturn(simpleName);
        when(qualifiedName.toString()).thenReturn("com.example");
        when(simpleName.toString()).thenReturn("TestProps");
    }

    @Test
    public void testGetSupportedAnnotationTypes() {
        Set<String> types = processor.getSupportedAnnotationTypes();
        assertEquals(1, types.size());
        assertTrue(types.contains(Propify.class.getCanonicalName()));
    }

    @Test
    public void testGetSupportedSourceVersion() {
        assertNotNull(processor.getSupportedSourceVersion());
    }

    @Test
    public void testProcess_ValidProperties_GeneratesCode() throws IOException {
        // Setup
        String yamlContent = "name: John\nage: 30";
        when(typeElement.getAnnotation(Propify.class)).thenReturn(propifyAnnotation);
        when(propifyAnnotation.location()).thenReturn("classpath: test.yaml");
        when(propifyAnnotation.mediaType()).thenReturn("application/yaml");
        
        when(roundEnv.getElementsAnnotatedWith(any(TypeElement.class)))
            .thenReturn((Set) Collections.singleton(typeElement));
        
        // Mock resource loading
        ByteArrayInputStream resourceStream = new ByteArrayInputStream(yamlContent.getBytes());
        when(processingEnv.getFiler().getResource(any(), any(), any()))
            .thenReturn(new TestFileObject(resourceStream));
        
        // Mock file creation
        when(filer.createSourceFile(any())).thenReturn(sourceFile);
        when(sourceFile.openWriter()).thenReturn(new StringWriter());

        // Execute
        boolean result = processor.process(Collections.singleton(typeElement), roundEnv);

        // Verify
        assertTrue(result);
        verify(messager).printMessage(eq(Diagnostic.Kind.NOTE), any(), eq(typeElement));
    }

    @Test
    public void testProcess_ResourceNotFound_ReportsError() throws IOException {
        // Setup
        when(typeElement.getAnnotation(Propify.class)).thenReturn(propifyAnnotation);
        when(propifyAnnotation.location()).thenReturn("nonexistent.yaml");
        when(propifyAnnotation.mediaType()).thenReturn("application/yaml");
        
        when(roundEnv.getElementsAnnotatedWith(any(TypeElement.class)))
            .thenReturn((Set) Collections.singleton(typeElement));
        
        // Mock resource loading to return null (not found)
        when(processingEnv.getFiler().getResource(any(), any(), any()))
            .thenReturn(new TestFileObject(null));

        // Execute
        processor.process(Collections.singleton(typeElement), roundEnv);

        // Verify error was reported
        verify(messager).printMessage(
            eq(Diagnostic.Kind.ERROR),
            contains("Could not find resource"),
            eq(typeElement)
        );
    }

    @Test
    public void testProcess_InvalidMediaType_ReportsError() throws IOException {
        // Setup
        when(typeElement.getAnnotation(Propify.class)).thenReturn(propifyAnnotation);
        when(propifyAnnotation.mediaType()).thenReturn("invalid/type");
        when(propifyAnnotation.location()).thenReturn("classpath:test.invalid");

        when(processingEnv.getFiler().getResource(any(), any(), any()))
            .thenReturn(new TestFileObject(null));

        when(roundEnv.getElementsAnnotatedWith(any(TypeElement.class)))
            .thenReturn((Set) Collections.singleton(typeElement));

        // Execute
        processor.process(Collections.singleton(typeElement), roundEnv);

        // Verify error was reported
        verify(messager).printMessage(
            eq(Diagnostic.Kind.ERROR),
            contains("No parser found for media type"),
            eq(typeElement)
        );
    }

    @Test
    public void testProcess_IOException_ReportsError() throws IOException {
        // Setup
        when(typeElement.getAnnotation(Propify.class)).thenReturn(propifyAnnotation);
        when(propifyAnnotation.location()).thenReturn("classpath: test.yaml");
        when(propifyAnnotation.mediaType()).thenReturn("application/yaml");
        
        when(roundEnv.getElementsAnnotatedWith(any(TypeElement.class)))
            .thenReturn((Set) Collections.singleton(typeElement));
        
        // Mock resource loading to throw IOException
        when(processingEnv.getFiler().getResource(any(), any(), any()))
            .thenThrow(new IOException("Test error"));

        // Execute
        processor.process(Collections.singleton(typeElement), roundEnv);

        // Verify error was reported
        verify(messager).printMessage(
            eq(Diagnostic.Kind.ERROR),
            contains("Could not find resource"),
            eq(typeElement)
        );
    }

    @Test
    public void testProcess_NonTypeElement_Ignored() {
        // Setup
        Element nonTypeElement = mock(Element.class);
        when(roundEnv.getElementsAnnotatedWith(any(TypeElement.class)))
            .thenReturn((Set) Collections.singleton(nonTypeElement));

        // Execute
        boolean result = processor.process(Collections.singleton(typeElement), roundEnv);

        // Verify
        assertTrue(result);
        verify(messager, never()).printMessage(any(), any(), any());
    }

    // Helper class to mock FileObject for resource loading
    private static class TestFileObject extends javax.tools.SimpleJavaFileObject {
        private final InputStream content;

        TestFileObject(InputStream content) {
            super(java.net.URI.create("string:///test.yaml"), Kind.OTHER);
            this.content = content;
        }

        @Override
        public InputStream openInputStream() {
            return content;
        }
    }
}
