package com.vgerbot.propify.common;

import org.junit.Test;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Test for the Utils constructor to ensure it cannot be instantiated.
 */
public class Utils_ConstructorTest {

    /**
     * Test that the Utils constructor throws a PropifyException when attempting to instantiate it.
     * This verifies that the utility class is properly designed to prevent instantiation.
     */
    @Test
    public void testConstructor_ThrowsPropifyException() {
        try {
            // Get the private constructor
            Constructor<Utils> constructor = Utils.class.getDeclaredConstructor();
            // Make it accessible
            constructor.setAccessible(true);
            // Try to create a new instance
            constructor.newInstance();
            // If we get here, the test should fail
            fail("Expected PropifyException was not thrown");
        } catch (InvocationTargetException e) {
            // The constructor should throw a PropifyException
            Throwable cause = e.getCause();
            assertTrue("Expected PropifyException but got: " + cause.getClass().getName(), 
                      cause instanceof PropifyException);
            assertEquals("Cannot instantiate Utils class", cause.getMessage());
        } catch (NoSuchMethodException | IllegalAccessException | InstantiationException e) {
            fail("Unexpected exception: " + e.getMessage());
        }
    }
}
