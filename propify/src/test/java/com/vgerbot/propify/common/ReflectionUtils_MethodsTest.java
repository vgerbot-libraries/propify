package com.vgerbot.propify.common;

import org.junit.Test;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.junit.Assert.*;

/**
 * Tests for the utility methods in ReflectionUtils class.
 */
public class ReflectionUtils_MethodsTest {

    // Test class with various members for testing reflection
    private static class TestClass {
        private String privateField;
        public String publicField;
        
        private TestClass() {}
        
        public TestClass(String value) {
            this.privateField = value;
        }
        
        private void privateMethod() {}
        
        public void publicMethod() {}
        
        private String privateMethodWithParams(String param1, int param2) {
            return param1 + param2;
        }
    }

    /**
     * Test for makeAccessible method with an already accessible object.
     */
    @Test
    public void testMakeAccessible_AlreadyAccessible() {
        // Create an accessible object
        AccessibleObject object = TestClass.class.getFields()[0]; // publicField
        object.setAccessible(true);
        
        // Call makeAccessible
        ReflectionUtils.makeAccessible(object);
        
        // Verify it's still accessible
        assertTrue(object.isAccessible());
    }

    /**
     * Test for makeAccessible method with an inaccessible object.
     */
    @Test
    public void testMakeAccessible_Inaccessible() {
        // Create an inaccessible object
        AccessibleObject object = null;
        try {
            object = TestClass.class.getDeclaredField("privateField");
            object.setAccessible(false);
        } catch (NoSuchFieldException e) {
            fail("Test setup failed: " + e.getMessage());
        }
        
        // Call makeAccessible
        ReflectionUtils.makeAccessible(object);
        
        // Verify it's now accessible
        assertTrue(object.isAccessible());
    }

    /**
     * Test for getDeclaredConstructor method with a public constructor.
     */
    @Test
    public void testGetDeclaredConstructor_PublicConstructor() {
        // Get public constructor
        Constructor<TestClass> constructor = ReflectionUtils.getDeclaredConstructor(TestClass.class, String.class);
        
        // Verify it's the correct constructor
        assertNotNull(constructor);
        assertEquals(1, constructor.getParameterCount());
        assertEquals(String.class, constructor.getParameterTypes()[0]);
        assertTrue(constructor.isAccessible());
    }

    /**
     * Test for getDeclaredConstructor method with a private constructor.
     */
    @Test
    public void testGetDeclaredConstructor_PrivateConstructor() {
        // Get private constructor
        Constructor<TestClass> constructor = ReflectionUtils.getDeclaredConstructor(TestClass.class);
        
        // Verify it's the correct constructor
        assertNotNull(constructor);
        assertEquals(0, constructor.getParameterCount());
        assertTrue(constructor.isAccessible());
    }

    /**
     * Test for getDeclaredConstructor method when constructor doesn't exist.
     */
    @Test
    public void testGetDeclaredConstructor_NonExistentConstructor() {
        try {
            // Try to get a constructor that doesn't exist
            ReflectionUtils.getDeclaredConstructor(TestClass.class, Integer.class, Boolean.class);
            fail("Expected PropifyException was not thrown");
        } catch (PropifyException e) {
            // Verify the exception message
            assertTrue(e.getMessage().contains("Failed to get constructor"));
            assertTrue(e.getCause() instanceof NoSuchMethodException);
        }
    }

    /**
     * Test for getDeclaredField method with a public field.
     */
    @Test
    public void testGetDeclaredField_PublicField() {
        // Get public field
        Field field = ReflectionUtils.getDeclaredField(TestClass.class, "publicField");
        
        // Verify it's the correct field
        assertNotNull(field);
        assertEquals("publicField", field.getName());
        assertTrue(field.isAccessible());
    }

    /**
     * Test for getDeclaredField method with a private field.
     */
    @Test
    public void testGetDeclaredField_PrivateField() {
        // Get private field
        Field field = ReflectionUtils.getDeclaredField(TestClass.class, "privateField");
        
        // Verify it's the correct field
        assertNotNull(field);
        assertEquals("privateField", field.getName());
        assertTrue(field.isAccessible());
    }

    /**
     * Test for getDeclaredField method when field doesn't exist.
     */
    @Test
    public void testGetDeclaredField_NonExistentField() {
        try {
            // Try to get a field that doesn't exist
            ReflectionUtils.getDeclaredField(TestClass.class, "nonExistentField");
            fail("Expected PropifyException was not thrown");
        } catch (PropifyException e) {
            // Verify the exception message
            assertTrue(e.getMessage().contains("Failed to get field"));
            assertTrue(e.getCause() instanceof NoSuchFieldException);
        }
    }

    /**
     * Test for getDeclaredMethod method with a public method.
     */
    @Test
    public void testGetDeclaredMethod_PublicMethod() {
        // Get public method
        Method method = ReflectionUtils.getDeclaredMethod(TestClass.class, "publicMethod");
        
        // Verify it's the correct method
        assertNotNull(method);
        assertEquals("publicMethod", method.getName());
        assertEquals(0, method.getParameterCount());
        assertTrue(method.isAccessible());
    }

    /**
     * Test for getDeclaredMethod method with a private method.
     */
    @Test
    public void testGetDeclaredMethod_PrivateMethod() {
        // Get private method
        Method method = ReflectionUtils.getDeclaredMethod(TestClass.class, "privateMethod");
        
        // Verify it's the correct method
        assertNotNull(method);
        assertEquals("privateMethod", method.getName());
        assertEquals(0, method.getParameterCount());
        assertTrue(method.isAccessible());
    }

    /**
     * Test for getDeclaredMethod method with a method that has parameters.
     */
    @Test
    public void testGetDeclaredMethod_MethodWithParameters() {
        // Get method with parameters
        Method method = ReflectionUtils.getDeclaredMethod(TestClass.class, "privateMethodWithParams", 
                String.class, int.class);
        
        // Verify it's the correct method
        assertNotNull(method);
        assertEquals("privateMethodWithParams", method.getName());
        assertEquals(2, method.getParameterCount());
        assertEquals(String.class, method.getParameterTypes()[0]);
        assertEquals(int.class, method.getParameterTypes()[1]);
        assertTrue(method.isAccessible());
    }

    /**
     * Test for getDeclaredMethod method when method doesn't exist.
     */
    @Test
    public void testGetDeclaredMethod_NonExistentMethod() {
        try {
            // Try to get a method that doesn't exist
            ReflectionUtils.getDeclaredMethod(TestClass.class, "nonExistentMethod");
            fail("Expected PropifyException was not thrown");
        } catch (PropifyException e) {
            // Verify the exception message
            assertTrue(e.getMessage().contains("Failed to get method"));
            assertTrue(e.getCause() instanceof NoSuchMethodException);
        }
    }
}
