package com.vgerbot.propify;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class Utils_ParseValueTest {

    @Test
    public void testParseValue_BooleanTrue_ReturnsBooleanTrue() {
        Object input = "true";
        Object expected = Boolean.TRUE;
        assertEquals(expected, com.vgerbot.propify.Utils.parseValue(input));
    }

    @Test
    public void testParseValue_BooleanFalse_ReturnsBooleanFalse() {
        Object input = "false";
        Object expected = Boolean.FALSE;
        assertEquals(expected, com.vgerbot.propify.Utils.parseValue(input));
    }

    @Test
    public void testParseValue_LongSuffixL_ReturnsLong() {
        Object input = "123L";
        Object expected = 123L;
        assertEquals(expected, com.vgerbot.propify.Utils.parseValue(input));
    }

    @Test
    public void testParseValue_LongSuffixl_ReturnsLong() {
        Object input = "123l";
        Object expected = 123L;
        assertEquals(expected, com.vgerbot.propify.Utils.parseValue(input));
    }

    @Test
    public void testParseValue_ByteSuffixB_ReturnsByte() {
        Object input = "123B";
        Object expected = (byte) 123;
        assertEquals(expected, com.vgerbot.propify.Utils.parseValue(input));
    }

    @Test
    public void testParseValue_ByteSuffixb_ReturnsByte() {
        Object input = "123b";
        Object expected = (byte) 123;
        assertEquals(expected, com.vgerbot.propify.Utils.parseValue(input));
    }

    @Test
    public void testParseValue_FloatSuffixF_ReturnsFloat() {
        Object input = "123.45F";
        Object expected = 123.45f;
        assertEquals(expected, com.vgerbot.propify.Utils.parseValue(input));
    }

    @Test
    public void testParseValue_FloatSuffixf_ReturnsFloat() {
        Object input = "123.45f";
        Object expected = 123.45f;
        assertEquals(expected, com.vgerbot.propify.Utils.parseValue(input));
    }

    @Test
    public void testParseValue_DoubleSuffixD_ReturnsDouble() {
        Object input = "123.45D";
        Object expected = 123.45d;
        assertEquals(expected, com.vgerbot.propify.Utils.parseValue(input));
    }

    @Test
    public void testParseValue_DoubleSuffixd_ReturnsDouble() {
        Object input = "123.45d";
        Object expected = 123.45d;
        assertEquals(expected, com.vgerbot.propify.Utils.parseValue(input));
    }

    @Test
    public void testParseValue_NoSuffix_IntegerValue_ReturnsInteger() {
        Object input = "123";
        Object expected = 123;
        assertEquals(expected, com.vgerbot.propify.Utils.parseValue(input));
    }

    @Test
    public void testParseValue_NoSuffix_LongValue_ReturnsLong() {
        Object input = "1234567890123";
        Object expected = 1234567890123L;
        assertEquals(expected, com.vgerbot.propify.Utils.parseValue(input));
    }

    @Test
    public void testParseValue_NoSuffix_DoubleValue_ReturnsDouble() {
        Object input = "123.45";
        Object expected = 123.45d;
        assertEquals(expected, com.vgerbot.propify.Utils.parseValue(input));
    }

    @Test
    public void testParseValue_UnknownType_ReturnsOriginalValue() {
        Object input = "hello";
        Object expected = "hello";
        assertEquals(expected, com.vgerbot.propify.Utils.parseValue(input));
    }

}
