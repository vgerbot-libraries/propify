package com.vgerbot.propify.logger;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.tools.Diagnostic;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

/**
 * Tests for the CompileTimeLogger class.
 */
public class CompileTimeLoggerTest {

    @Mock
    private ProcessingEnvironment mockEnvironment;
    
    @Mock
    private Messager mockMessager;
    
    private CompileTimeLogger logger;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        when(mockEnvironment.getMessager()).thenReturn(mockMessager);
        logger = new CompileTimeLogger(mockEnvironment);
    }

    @Test
    public void testLoggerCreation() {
        assertNotNull(logger);
    }

    @Test
    public void testInfoMethod() {
        String message = "Test info message";
        logger.info(message);
        
        verify(mockEnvironment).getMessager();
        verify(mockMessager).printMessage(Diagnostic.Kind.NOTE, message);
    }

    @Test
    public void testWarnMethod() {
        String message = "Test warning message";
        logger.warn(message);
        
        verify(mockEnvironment).getMessager();
        verify(mockMessager).printMessage(Diagnostic.Kind.WARNING, message);
    }

    @Test
    public void testErrorMethod() {
        String message = "Test error message";
        logger.error(message);
        
        verify(mockEnvironment).getMessager();
        verify(mockMessager).printMessage(Diagnostic.Kind.ERROR, message);
    }
}
