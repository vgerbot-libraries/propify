package com.vgerbot.propify;

import org.junit.Test;
import static org.junit.Assert.*;

public class Utils_ToLiteralStringTest {
    
    @Test
    public void testLongLiteral() {
        assertEquals("123L", Utils.toLiteralString(123L));
        assertEquals("-456L", Utils.toLiteralString(-456L));
    }

    @Test
    public void testFloatLiteral() {
        assertEquals("123.45F", Utils.toLiteralString(123.45F));
        assertEquals("-456.78F", Utils.toLiteralString(-456.78F));
    }

    @Test
    public void testDoubleLiteral() {
        assertEquals("123.45D", Utils.toLiteralString(123.45D));
        assertEquals("-456.78D", Utils.toLiteralString(-456.78D));
    }

    @Test
    public void testByteLiteral() {
        assertEquals("123B", Utils.toLiteralString((byte)123));
        assertEquals("-12B", Utils.toLiteralString((byte)-12));
    }

    @Test
    public void testShortLiteral() {
        assertEquals("123S", Utils.toLiteralString((short)123));
        assertEquals("-456S", Utils.toLiteralString((short)-456));
    }

    @Test
    public void testCharLiteral() {
        assertEquals("'a'", Utils.toLiteralString('a'));
        assertEquals("'\\n'", Utils.toLiteralString('\n'));
        assertEquals("'\\t'", Utils.toLiteralString('\t'));
        assertEquals("'\\''", Utils.toLiteralString('\''));
        assertEquals("'\\\\'", Utils.toLiteralString('\\'));
        assertEquals("'\\u0000'", Utils.toLiteralString('\0'));
    }

    @Test
    public void testStringLiteral() {
        assertEquals("\"hello\"", Utils.toLiteralString("hello"));
        assertEquals("\"\"", Utils.toLiteralString(""));
        assertEquals("\"null\"", Utils.toLiteralString("null"));
        assertEquals("\"Hello\\nWorld\"", Utils.toLiteralString("Hello\nWorld"));
        assertEquals("\"Tab:\\tHere\"", Utils.toLiteralString("Tab:\tHere"));
        assertEquals("\"Quote\\\"Inside\\\"Here\"", Utils.toLiteralString("Quote\"Inside\"Here"));
        assertEquals("\"Path: C:\\\\Program Files\\\\\"", Utils.toLiteralString("Path: C:\\Program Files\\"));
    }

    @Test
    public void testArrayLiteral() {
        assertEquals("{1, 2, 3}", Utils.toLiteralString(new int[]{1, 2, 3}));
        assertEquals("{\"a\", \"b\", \"c\"}", Utils.toLiteralString(new String[]{"a", "b", "c"}));
        assertEquals("{true, false}", Utils.toLiteralString(new boolean[]{true, false}));
        assertEquals("{1L, 2L}", Utils.toLiteralString(new long[]{1L, 2L}));
        assertEquals("{}", Utils.toLiteralString(new Object[0]));
        assertEquals("{'a', 'b'}", Utils.toLiteralString(new char[]{'a', 'b'}));
    }

    @Test
    public void testOtherTypes() {
        assertEquals("123", Utils.toLiteralString(123)); // Integer
        assertEquals("true", Utils.toLiteralString(true)); // Boolean
        assertEquals("null", Utils.toLiteralString(null)); // null value
    }
}
