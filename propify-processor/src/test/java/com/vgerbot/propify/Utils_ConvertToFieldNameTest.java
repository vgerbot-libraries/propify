package com.vgerbot.propify;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class Utils_ConvertToFieldNameTest {

    @Test
    public void testConvertToFieldName_EmptyInput_ThrowsIllegalArgumentException() {
        try {
            com.vgerbot.propify.Utils.convertToFieldName("");
            assert false;
        } catch (IllegalArgumentException e) {
            assertEquals("Field name cannot be empty", e.getMessage());
        }
    }

    @Test
    public void testConvertToFieldName_SimpleInput_ReturnsExpectedFieldName() {
        String input = "helloWorld";
        String expected = "helloWorld";
        assertEquals(expected, com.vgerbot.propify.Utils.convertToFieldName(input));
    }

    @Test
    public void testConvertToFieldName_InputWithNonLetterOrDigit_ReturnsExpectedFieldName() {
        String input = "hello-World";
        String expected = "hello_World";
        assertEquals(expected, com.vgerbot.propify.Utils.convertToFieldName(input));
    }

    @Test
    public void testConvertToFieldName_InputStartingWithDigit_ReturnsExpectedFieldName() {
        String input = "123hello";
        String expected = "_123hello";
        assertEquals(expected, com.vgerbot.propify.Utils.convertToFieldName(input));
    }

    @Test
    public void testConvertToFieldName_InputWithReservedKeyword_ReturnsExpectedFieldName() {
        String input = "class";
        String expected = "class_";
        assertEquals(expected, com.vgerbot.propify.Utils.convertToFieldName(input));
    }

    @Test
    public void testConvertToFieldName_InputWithMultipleWords_ReturnsExpectedFieldName() {
        String input = "hello world";
        String expected = "hello_World";
        assertEquals(expected, com.vgerbot.propify.Utils.convertToFieldName(input));
    }
}