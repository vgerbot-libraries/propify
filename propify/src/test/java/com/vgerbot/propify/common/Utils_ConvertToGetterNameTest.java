package com.vgerbot.propify.common;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class Utils_ConvertToGetterNameTest {

    @Test
    public void testConvertToGetterName_SimpleInput_NonBoolean_ReturnsGetPrefix() {
        String input = "name";
        boolean isBoolean = false;
        String expected = "getName";
        assertEquals(expected, Utils.convertToGetterName(input, isBoolean));
    }

    @Test
    public void testConvertToGetterName_SimpleInput_Boolean_ReturnsIsPrefix() {
        String input = "active";
        boolean isBoolean = true;
        String expected = "isActive";
        assertEquals(expected, Utils.convertToGetterName(input, isBoolean));
    }

    @Test
    public void testConvertToGetterName_InputWithSpecialChars_NonBoolean_ReturnsGetPrefix() {
        String input = "user-name";
        boolean isBoolean = false;
        String expected = "getUserName";
        assertEquals(expected, Utils.convertToGetterName(input, isBoolean));
    }

    @Test
    public void testConvertToGetterName_InputWithSpecialChars_Boolean_ReturnsIsPrefix() {
        String input = "is-active";
        boolean isBoolean = true;
        String expected = "isIsActive";
        assertEquals(expected, Utils.convertToGetterName(input, isBoolean));
    }

    @Test
    public void testConvertToGetterName_InputStartingWithDigit_NonBoolean_ReturnsGetPrefix() {
        String input = "123name";
        boolean isBoolean = false;
        String expected = "get_123name";
        assertEquals(expected, Utils.convertToGetterName(input, isBoolean));
    }

    @Test
    public void testConvertToGetterName_InputStartingWithDigit_Boolean_ReturnsIsPrefix() {
        String input = "123active";
        boolean isBoolean = true;
        String expected = "is_123active";
        assertEquals(expected, Utils.convertToGetterName(input, isBoolean));
    }

    @Test
    public void testConvertToGetterName_JavaKeyword_NonBoolean_ReturnsGetPrefix() {
        String input = "class";
        boolean isBoolean = false;
        String expected = "getClass_";
        assertEquals(expected, Utils.convertToGetterName(input, isBoolean));
    }

    @Test
    public void testConvertToGetterName_JavaKeyword_Boolean_ReturnsIsPrefix() {
        String input = "class";
        boolean isBoolean = true;
        String expected = "isClass_";
        assertEquals(expected, Utils.convertToGetterName(input, isBoolean));
    }

    @Test
    public void testConvertToGetterName_AlreadyHasGetPrefix_NonBoolean_DoesNotDuplicate() {
        String input = "getName";
        boolean isBoolean = false;
        String expected = "getGetName";
        assertEquals(expected, Utils.convertToGetterName(input, isBoolean));
    }

    @Test
    public void testConvertToGetterName_AlreadyHasIsPrefix_Boolean_DoesNotDuplicate() {
        String input = "isActive";
        boolean isBoolean = true;
        String expected = "isIsActive";
        assertEquals(expected, Utils.convertToGetterName(input, isBoolean));
    }

    @Test
    public void testConvertToGetterName_SingleCharField_NonBoolean_CapitalizesCorrectly() {
        String input = "x";
        boolean isBoolean = false;
        String expected = "getX";
        assertEquals(expected, Utils.convertToGetterName(input, isBoolean));
    }

    @Test
    public void testConvertToGetterName_SingleCharField_Boolean_CapitalizesCorrectly() {
        String input = "b";
        boolean isBoolean = true;
        String expected = "isB";
        assertEquals(expected, Utils.convertToGetterName(input, isBoolean));
    }

    @Test
    public void testConvertToGetterName_EmptyInput_ThrowsIllegalArgumentException() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> Utils.convertToGetterName("", false));
        assertEquals("Field name cannot be null or empty", exception.getMessage());
    }

    @Test
    public void testConvertToGetterName_NullInput_ThrowsIllegalArgumentException() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> Utils.convertToGetterName(null, false));
        assertEquals("Field name cannot be null or empty", exception.getMessage());
    }
}
