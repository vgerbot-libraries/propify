package com.vgerbot.propify;

import com.vgerbot.propify.core.Propify;
import com.vgerbot.propify.i18n.I18n;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class PropifyProcessorTest {

    private PropifyProcessor processor;

    @Mock
    private ProcessingEnvironment processingEnv;

    @Mock
    private Messager messager;

    @Mock
    private RoundEnvironment roundEnv;

    @Mock
    private TypeElement propifyAnnotation;
    
    @Mock
    private TypeElement i18nAnnotation;

    @Mock
    private TypeElement typeElement;

    @Mock
    private Name name;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        
        processor = new PropifyProcessor();
        
        when(processingEnv.getMessager()).thenReturn(messager);
        when(typeElement.getKind()).thenReturn(ElementKind.CLASS);
        when(typeElement.getSimpleName()).thenReturn(name);
        when(name.toString()).thenReturn("TestConfig");
        
        // Mock behavior for propifyAnnotation and i18nAnnotation
        when(propifyAnnotation.getQualifiedName()).thenReturn(mock(Name.class));
        when(propifyAnnotation.getQualifiedName().toString()).thenReturn(Propify.class.getCanonicalName());
        
        when(i18nAnnotation.getQualifiedName()).thenReturn(mock(Name.class));
        when(i18nAnnotation.getQualifiedName().toString()).thenReturn(I18n.class.getCanonicalName());
        
        processor.init(processingEnv);
    }

    @Test
    public void testInit() throws NoSuchFieldException, IllegalAccessException {
        // Verify that the processor was initialized correctly
        assertEquals(processingEnv, PropifyProcessor.processingEnvironment);
        Field field = processor.getClass().getDeclaredField("messager");
        field.setAccessible(true);

        assertNotNull("Messager should be initialized", field.get(processor));
    }

    @Test
    public void testGetSupportedAnnotationTypes() {
        Set<String> types = processor.getSupportedAnnotationTypes();
        
        assertTrue("Should support Propify annotation", 
                types.contains(Propify.class.getCanonicalName()));
        assertTrue("Should support I18n annotation", 
                types.contains(I18n.class.getCanonicalName()));
        assertEquals("Should support exactly 2 annotation types", 2, types.size());
    }

    @Test
    public void testGetSupportedSourceVersion() {
        assertNotNull("Supported source version should not be null", 
                processor.getSupportedSourceVersion());
    }

    @Test
    public void testProcessWithNoAnnotatedElements() {
        Set<TypeElement> annotations = new HashSet<>();
        annotations.add(propifyAnnotation);
        
        when(roundEnv.getElementsAnnotatedWith(propifyAnnotation))
                .thenReturn(Collections.emptySet());
        
        boolean result = processor.process(annotations, roundEnv);
        
        assertTrue("Process should return true", result);
        verify(roundEnv).getElementsAnnotatedWith(propifyAnnotation);
        verifyNoMoreInteractions(messager);
    }

    @Test
    public void testProcessWithNonTypeElement() {
        Set<TypeElement> annotations = new HashSet<>();
        annotations.add(propifyAnnotation);

        Element nonTypeElement = mock(Element.class);
        when(nonTypeElement.getKind()).thenReturn(ElementKind.FIELD);

        Set<Element> elements = new HashSet<>();
        elements.add(nonTypeElement);

        when((Set<Element>)roundEnv.getElementsAnnotatedWith(propifyAnnotation))
                .thenReturn(elements);

        boolean result = processor.process(annotations, roundEnv);

        assertTrue("Process should return true", result);
        verify(roundEnv).getElementsAnnotatedWith(propifyAnnotation);
        verifyNoMoreInteractions(messager);
    }

    @Test
    public void testProcessWithError() {
        Set<TypeElement> annotations = new HashSet<>();
        annotations.add(propifyAnnotation);

        Set<Element> elements = new HashSet<>();
        elements.add(typeElement);

        when((Set<Element>)roundEnv.getElementsAnnotatedWith(propifyAnnotation))
                .thenReturn(elements);

        // Mock a Propify annotation on the typeElement
        Propify mockPropify = mock(Propify.class);
        when(typeElement.getAnnotation(Propify.class)).thenReturn(mockPropify);

        // Simulate an error with specific message
        when(mockPropify.location()).thenThrow(new RuntimeException("Could not find resource"));

        boolean result = processor.process(annotations, roundEnv);

        assertTrue("Process should return true", result);
        verify(roundEnv).getElementsAnnotatedWith(propifyAnnotation);
        verify(messager).printMessage(eq(Diagnostic.Kind.ERROR),
                eq("Could not find resource"), eq(typeElement));
    }

    @Test
    public void testProcessWithUnknownMediaTypeError() {
        Set<TypeElement> annotations = new HashSet<>();
        annotations.add(propifyAnnotation);

        Set<Element> elements = new HashSet<>();
        elements.add(typeElement);

        when((Set<Element>)roundEnv.getElementsAnnotatedWith(propifyAnnotation))
                .thenReturn(elements);

        // Mock a Propify annotation on the typeElement
        Propify mockPropify = mock(Propify.class);
        when(typeElement.getAnnotation(Propify.class)).thenReturn(mockPropify);

        // Simulate a media type error
        when(mockPropify.location()).thenThrow(new RuntimeException("No parser found for media type"));

        boolean result = processor.process(annotations, roundEnv);

        assertTrue("Process should return true", result);
        verify(roundEnv).getElementsAnnotatedWith(propifyAnnotation);
        verify(messager).printMessage(eq(Diagnostic.Kind.ERROR),
                eq("No parser found for media type"), eq(typeElement));
    }

    @Test
    public void testProcessWithRuntimeException() {
        Set<TypeElement> annotations = new HashSet<>();
        annotations.add(propifyAnnotation);
        
        Set<Element> elements = new HashSet<>();
        elements.add(typeElement);
        
        when((Set<Element>)roundEnv.getElementsAnnotatedWith(propifyAnnotation))
                .thenReturn(elements);
        
        // Mock a Propify annotation on the typeElement
        Propify mockPropify = mock(Propify.class);
        when(typeElement.getAnnotation(Propify.class)).thenReturn(mockPropify);
        
        // Simulate an error with null message to trigger RuntimeException
        RuntimeException exception = mock(RuntimeException.class);
        when(exception.getMessage()).thenReturn(null);
        when(mockPropify.location()).thenThrow(exception);
        
        try {
            processor.process(annotations, roundEnv);
            fail("Should have thrown RuntimeException");
        } catch (RuntimeException e) {
            // Expected
        }
        
        verify(roundEnv).getElementsAnnotatedWith(propifyAnnotation);
    }
} 