package com.vgerbot.propify.i18n;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.List;
import java.util.Date;

public class ICUTemplateArgumentsParserTest {

    @Test
    public void testSimpleStringArgument() {
        String pattern = "Hello {name}";
        List<ICUTemplateArgumentsParser.Argument> arguments = ICUTemplateArgumentsParser.parseTemplate(pattern);
        
        assertEquals(1, arguments.size());
        assertEquals("name", arguments.get(0).getName());
        assertEquals(String.class, arguments.get(0).getType());
    }

    @Test
    public void testMultipleArguments() {
        String pattern = "User {username} has {count, number} messages";
        List<ICUTemplateArgumentsParser.Argument> arguments = ICUTemplateArgumentsParser.parseTemplate(pattern);
        
        assertEquals(2, arguments.size());
        assertEquals("username", arguments.get(0).getName());
        assertEquals(String.class, arguments.get(0).getType());
        assertEquals("count", arguments.get(1).getName());
        assertEquals(Number.class, arguments.get(1).getType());
    }

    @Test
    public void testPluralArgument() {
        String pattern = "You have {count, plural, =1{one message} other{# messages}}";
        List<ICUTemplateArgumentsParser.Argument> arguments = ICUTemplateArgumentsParser.parseTemplate(pattern);
        
        assertEquals(1, arguments.size());
        assertEquals("count", arguments.get(0).getName());
        assertEquals(Number.class, arguments.get(0).getType());
    }

    @Test
    public void testDateAndTimeArguments() {
        String pattern = "Last login: {date, date, short} {time, time, short}";
        List<ICUTemplateArgumentsParser.Argument> arguments = ICUTemplateArgumentsParser.parseTemplate(pattern);
        
        assertEquals(2, arguments.size());
        assertEquals("date", arguments.get(0).getName());
        assertEquals(Date.class, arguments.get(0).getType());
        assertEquals("time", arguments.get(1).getName());
        assertEquals(Date.class, arguments.get(1).getType());
    }

    @Test
    public void testSelectArgument() {
        String pattern = "{gender, select, male{He} female{She} other{They}} likes this";
        List<ICUTemplateArgumentsParser.Argument> arguments = ICUTemplateArgumentsParser.parseTemplate(pattern);
        
        assertEquals(1, arguments.size());
        assertEquals("gender", arguments.get(0).getName());
        assertEquals(String.class, arguments.get(0).getType());
    }

    @Test
    public void testNestedArguments() {
        String pattern = "{count, plural, =1{You have one message from {sender}} other{You have # messages from {sender}}}";
        List<ICUTemplateArgumentsParser.Argument> arguments = ICUTemplateArgumentsParser.parseTemplate(pattern);
        
        assertEquals(2, arguments.size());
        assertEquals("count", arguments.get(0).getName());
        assertEquals(Number.class, arguments.get(0).getType());
        assertEquals("sender", arguments.get(1).getName());
        assertEquals(String.class, arguments.get(1).getType());
    }

    @Test
    public void testEmptyPattern() {
        String pattern = "";
        List<ICUTemplateArgumentsParser.Argument> arguments = ICUTemplateArgumentsParser.parseTemplate(pattern);
        
        assertTrue(arguments.isEmpty());
    }

    @Test
    public void testPatternWithNoArguments() {
        String pattern = "Hello World";
        List<ICUTemplateArgumentsParser.Argument> arguments = ICUTemplateArgumentsParser.parseTemplate(pattern);
        
        assertTrue(arguments.isEmpty());
    }

    @Test
    public void testComplexPatternWithMultipleTypes() {
        String pattern = "User {username} has {count, number} messages. Last login: {date, date, short}. Status: {status, select, active{Active} inactive{Inactive} other{Unknown}}";
        List<ICUTemplateArgumentsParser.Argument> arguments = ICUTemplateArgumentsParser.parseTemplate(pattern);

        assertEquals(4, arguments.size());
        assertEquals("username", arguments.get(0).getName());
        assertEquals(String.class, arguments.get(0).getType());
        assertEquals("count", arguments.get(1).getName());
        assertEquals(Number.class, arguments.get(1).getType());
        assertEquals("date", arguments.get(2).getName());
        assertEquals(Date.class, arguments.get(2).getType());
        assertEquals("status", arguments.get(3).getName());
        assertEquals(String.class, arguments.get(3).getType());
    }

    @Test
    public void testChoiceFormat() {
        String pattern = "{temperature,choice,-50<It's freezing|0<It's cold|20<It's warm|30<It's hot|40<It's extremely hot}";
        List<ICUTemplateArgumentsParser.Argument> arguments = ICUTemplateArgumentsParser.parseTemplate(pattern);
        
        assertEquals(1, arguments.size());
        assertEquals("temperature", arguments.get(0).getName());
        assertEquals(Number.class, arguments.get(0).getType());
    }

    @Test
    public void testCurrencyFormat() {
        String pattern = "Price: {amount, number, currency}";
        List<ICUTemplateArgumentsParser.Argument> arguments = ICUTemplateArgumentsParser.parseTemplate(pattern);
        
        assertEquals(1, arguments.size());
        assertEquals("amount", arguments.get(0).getName());
        assertEquals(Number.class, arguments.get(0).getType());
    }

    @Test
    public void testPercentFormat() {
        String pattern = "Discount: {rate, number, percent}";
        List<ICUTemplateArgumentsParser.Argument> arguments = ICUTemplateArgumentsParser.parseTemplate(pattern);
        
        assertEquals(1, arguments.size());
        assertEquals("rate", arguments.get(0).getName());
        assertEquals(Number.class, arguments.get(0).getType());
    }

    @Test
    public void testIntegerFormat() {
        String pattern = "Quantity: {count, number, integer}";
        List<ICUTemplateArgumentsParser.Argument> arguments = ICUTemplateArgumentsParser.parseTemplate(pattern);
        
        assertEquals(1, arguments.size());
        assertEquals("count", arguments.get(0).getName());
        assertEquals(Number.class, arguments.get(0).getType());
    }

    @Test
    public void testComplexNestedFormat() {
        String pattern = "{count, plural, =0{No items} =1{One item} other{# items}} in {category, select, cart{shopping cart} wishlist{wishlist} other{list}}";
        List<ICUTemplateArgumentsParser.Argument> arguments = ICUTemplateArgumentsParser.parseTemplate(pattern);
        
        assertEquals(2, arguments.size());
        assertEquals("count", arguments.get(0).getName());
        assertEquals(Number.class, arguments.get(0).getType());
        assertEquals("category", arguments.get(1).getName());
        assertEquals(String.class, arguments.get(1).getType());
    }

    @Test
    public void testEscapedCharacters() {
        String pattern = "Literal braces: '{' and '}' with {param} placeholder";
        List<ICUTemplateArgumentsParser.Argument> arguments = ICUTemplateArgumentsParser.parseTemplate(pattern);
        
        assertEquals(1, arguments.size());
        assertEquals("param", arguments.get(0).getName());
        assertEquals(String.class, arguments.get(0).getType());
    }

    @Test
    public void testDuplicateArguments() {
        String pattern = "{name} is {age} years old. Hello {name}!";
        List<ICUTemplateArgumentsParser.Argument> arguments = ICUTemplateArgumentsParser.parseTemplate(pattern);
        
        assertEquals(2, arguments.size());
        assertEquals("name", arguments.get(0).getName());
        assertEquals(String.class, arguments.get(0).getType());
        assertEquals("age", arguments.get(1).getName());
        assertEquals(String.class, arguments.get(1).getType());
    }

    @Test
    public void testComplexDateTimeFormat() {
        String pattern = "At {time, time, ::jmm} on {date, date, ::dMMMM}, there was {event} on planet {planet, number, integer}.";
        List<ICUTemplateArgumentsParser.Argument> arguments = ICUTemplateArgumentsParser.parseTemplate(pattern);
        
        assertEquals(4, arguments.size());
        assertEquals("time", arguments.get(0).getName());
        assertEquals(Date.class, arguments.get(0).getType());
        assertEquals("date", arguments.get(1).getName());
        assertEquals(Date.class, arguments.get(1).getType());
        assertEquals("event", arguments.get(2).getName());
        assertEquals(String.class, arguments.get(2).getType());
        assertEquals("planet", arguments.get(3).getName());
        assertEquals(Number.class, arguments.get(3).getType());
    }

    @Test
    public void testNumericIndexedArguments() {
        String pattern = "User {0} has {1, number, integer} unread {1, plural, =1{message} other{messages}}. Last login: {2, date, short} {2, time, short}";
        List<ICUTemplateArgumentsParser.Argument> arguments = ICUTemplateArgumentsParser.parseTemplate(pattern);
        
        assertEquals(3, arguments.size());
        
        // First argument (index 0)
        assertEquals("0", arguments.get(0).getName());
        assertEquals(String.class, arguments.get(0).getType());
        
        // Second argument (index 1) - used twice
        assertEquals("1", arguments.get(1).getName());
        assertEquals(Number.class, arguments.get(1).getType());
        
        // Third argument (index 2) - used twice
        assertEquals("2", arguments.get(2).getName());
        assertEquals(Date.class, arguments.get(2).getType());
    }

    @Test
    public void testMixedNumericAndNamedArguments() {
        String pattern = "User {0} ({username}) has {1, number, integer} unread {1, plural, =1{message} other{messages}}. Last login: {2, date, short} {2, time, short}";
        List<ICUTemplateArgumentsParser.Argument> arguments = ICUTemplateArgumentsParser.parseTemplate(pattern);
        
        assertEquals(4, arguments.size());
        
        // First argument (index 0)
        assertEquals("0", arguments.get(0).getName());
        assertEquals(String.class, arguments.get(0).getType());
        
        // Named argument
        assertEquals("username", arguments.get(1).getName());
        assertEquals(String.class, arguments.get(1).getType());
        
        // Second argument (index 1) - used twice
        assertEquals("1", arguments.get(2).getName());
        assertEquals(Number.class, arguments.get(2).getType());
        
        // Third argument (index 2) - used twice
        assertEquals("2", arguments.get(3).getName());
        assertEquals(Date.class, arguments.get(3).getType());
    }

    @Test
    public void testReusedNumericArguments() {
        String pattern = "{0} has {1} unread {1, plural, =1{message} other{messages}}. {0} last logged in at {2, time, short}";
        List<ICUTemplateArgumentsParser.Argument> arguments = ICUTemplateArgumentsParser.parseTemplate(pattern);
        
        assertEquals(3, arguments.size());
        
        // First argument (index 0) - used twice
        assertEquals("0", arguments.get(0).getName());
        assertEquals(String.class, arguments.get(0).getType());
        
        // Second argument (index 1) - used twice
        assertEquals("1", arguments.get(1).getName());
        assertEquals(Number.class, arguments.get(1).getType());
        
        // Third argument (index 2)
        assertEquals("2", arguments.get(2).getName());
        assertEquals(Date.class, arguments.get(2).getType());
    }
} 