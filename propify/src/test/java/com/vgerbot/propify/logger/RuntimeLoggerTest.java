package com.vgerbot.propify.logger;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;

/**
 * Basic tests for the RuntimeLogger class.
 * These tests focus on the behavior that can be tested without mocking static methods.
 */
public class RuntimeLoggerTest {

    @Test
    public void testLoggerCreation() {
        RuntimeLogger logger = new RuntimeLogger();
        assertNotNull(logger);
    }

    @Test
    public void testInfoMethod() {
        RuntimeLogger logger = new RuntimeLogger();
        // Just verify it doesn't throw an exception
        logger.info("Test info message");
    }

    @Test
    public void testWarnMethod() {
        RuntimeLogger logger = new RuntimeLogger();
        // Just verify it doesn't throw an exception
        logger.warn("Test warning message");
    }

    @Test
    public void testErrorMethod() {
        RuntimeLogger logger = new RuntimeLogger();
        // Just verify it doesn't throw an exception
        logger.error("Test error message");
    }
}
