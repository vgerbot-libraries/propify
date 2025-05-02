package com.vgerbot.propify.common;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class Utils_ConvertToClassNameTest {

    @Test
    public void testConvertToClassName_EmptyInput_ThrowsIllegalArgumentException() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> Utils.convertToClassName(""));
        assertEquals("Class name cannot be null or empty", exception.getMessage());
    }

    @Test
    public void testConvertToClassName_NullInput_ThrowsIllegalArgumentException() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> Utils.convertToClassName(null));
        assertEquals("Class name cannot be null or empty", exception.getMessage());
    }

    @Test
    public void testConvertToClassName_SimpleInput_ReturnsExpectedClassName() {
        String input = "helloWorld";
        String expected = "HelloWorld";
        assertEquals(expected, Utils.convertToClassName(input));
    }

    @Test
    public void testConvertToClassName_InputWithNonLetterOrDigit_ReturnsExpectedClassName() {
        String input = "hello-World";
        String expected = "HelloWorld";
        assertEquals(expected, Utils.convertToClassName(input));
    }

    @Test
    public void testConvertToClassName_InputStartingWithDigit_ReturnsExpectedClassName() {
        String input = "123hello";
        String expected = "_123hello";
        assertEquals(expected, Utils.convertToClassName(input));
    }

    @Test
    public void testConvertToClassName_InputWithReservedKeyword_ReturnsExpectedClassName() {
        String input = "class";
        String expected = "Class";
        assertEquals(expected, Utils.convertToClassName(input));
    }

    @Test
    public void testConvertToClassName_InputWithMultipleWords_ReturnsExpectedClassName() {
        String input = "hello world";
        String expected = "HelloWorld";
        assertEquals(expected, Utils.convertToClassName(input));
    }

    @Test
    public void testConvertToClassName_ConsecutiveSpecialChars_ReturnsSingleUnderscore() {
        String input = "hello---world";
        String expected = "HelloWorld";
        assertEquals(expected, Utils.convertToClassName(input));
    }

    @Test
    public void testConvertToClassName_TrailingSpecialChars_RemovesTrailingUnderscore() {
        String input = "hello---";
        String expected = "Hello";
        assertEquals(expected, Utils.convertToClassName(input));
    }

    @Test
    public void testConvertToClassName_DollarSign_PreservesDollarSign() {
        String input = "hello$world";
        String expected = "Hello$world";
        assertEquals(expected, Utils.convertToClassName(input));
    }

    @Test
    public void testConvertToClassName_UnicodeLetters_PreservesUnicodeLetters() {
        String input = "helloワールド";
        String expected = "Helloワールド";
        assertEquals(expected, Utils.convertToClassName(input));
    }

    @Test
    public void testConvertToClassName_AllSpecialChars_ReturnsUnderscore() {
        String input = "!@#$%^&*()";
        String expected = "_$";
        assertEquals(expected, Utils.convertToClassName(input));
    }

    @Test
    public void testConvertToClassName_JavaKeywords_ReturnsWithUnderscore() {
        String[] keywords = { "if", "for", "while", "do", "break", "continue", "return",
                "void", "int", "boolean", "double", "float", "long" };
        for (String keyword : keywords) {
            String capitalized = Character.toUpperCase(keyword.charAt(0)) + keyword.substring(1);
            assertEquals(capitalized, Utils.convertToClassName(keyword));
        }
    }

    @Test
    public void testConvertToClassName_MixedCase_PreservesCase() {
        String input = "helloWORLDtest";
        String expected = "HelloWORLDtest";
        assertEquals(expected, Utils.convertToClassName(input));
    }

    @Test
    public void testConvertToClassName_WithWhitespace_TrimsWhitespace() {
        String input = "  hello  world  ";
        String expected = "HelloWorld";
        assertEquals(expected, Utils.convertToClassName(input));
    }
}
