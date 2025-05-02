package com.vgerbot.propify.common;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class Utils_ConvertToFieldNameTest {

    @Test
    public void testConvertToFieldName_EmptyInput_ThrowsIllegalArgumentException() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> Utils.convertToFieldName(""));
        assertEquals("Field name cannot be null or empty", exception.getMessage());
    }

    @Test
    public void testConvertToFieldName_SimpleInput_ReturnsExpectedFieldName() {
        String input = "helloWorld";
        String expected = "helloWorld";
        assertEquals(expected, Utils.convertToFieldName(input));
    }

    @Test
    public void testConvertToFieldName_InputWithNonLetterOrDigit_ReturnsExpectedFieldName() {
        String input = "hello-World";
        String expected = "helloWorld";
        assertEquals(expected, Utils.convertToFieldName(input));
    }

    @Test
    public void testConvertToFieldName_InputStartingWithDigit_ReturnsExpectedFieldName() {
        String input = "123hello";
        String expected = "_123hello";
        assertEquals(expected, Utils.convertToFieldName(input));
    }

    @Test
    public void testConvertToFieldName_InputWithReservedKeyword_ReturnsExpectedFieldName() {
        String input = "class";
        String expected = "class_";
        assertEquals(expected, Utils.convertToFieldName(input));
    }

    @Test
    public void testConvertToFieldName_InputWithMultipleWords_ReturnsExpectedFieldName() {
        String input = "hello world";
        String expected = "helloWorld";
        assertEquals(expected, Utils.convertToFieldName(input));
    }

    @Test
    public void testConvertToFieldName_ConsecutiveSpecialChars_ReturnsSingleUnderscore() {
        String input = "hello---world";
        String expected = "helloWorld";
        assertEquals(expected, Utils.convertToFieldName(input));
    }

    @Test
    public void testConvertToFieldName_TrailingSpecialChars_RemovesTrailingUnderscore() {
        String input = "hello---";
        String expected = "hello";
        assertEquals(expected, Utils.convertToFieldName(input));
    }

    @Test
    public void testConvertToFieldName_DollarSign_PreservesDollarSign() {
        String input = "hello$world";
        String expected = "hello$world";
        assertEquals(expected, Utils.convertToFieldName(input));
    }

    @Test
    public void testConvertToFieldName_UnicodeLetters_PreservesUnicodeLetters() {
        String input = "helloワールド";
        String expected = "helloワールド";
        assertEquals(expected, Utils.convertToFieldName(input));
    }

    @Test
    public void testConvertToFieldName_AllSpecialChars_ReturnsUnderscore() {
        String input = "!@#$%^&*()";
        String expected = "_$";
        assertEquals(expected, Utils.convertToFieldName(input));
    }

    @Test
    public void testConvertToFieldName_MoreJavaKeywords_ReturnsWithUnderscore() {
        String[] keywords = { "if", "for", "while", "do", "break", "continue", "return",
                "void", "int", "boolean", "double", "float", "long" };
        for (String keyword : keywords) {
            assertEquals(keyword + "_", Utils.convertToFieldName(keyword));
        }
    }

    @Test
    public void testConvertToFieldName_MixedCase_PreservesCase() {
        String input = "helloWORLDtest";
        String expected = "helloWORLDtest";
        assertEquals(expected, Utils.convertToFieldName(input));
    }
}
