package com.vgerbot.propify.i18n;

import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class ICUMessageTemplateExtensionTest {

    private ICUMessageTemplateExtension extension;

    @Before
    public void setUp() {
        extension = new ICUMessageTemplateExtension();
    }

    @Test
    public void testFormatSimpleMessage() {
        String template = "Hello, {name}!";
        Map<String, Object> params = new HashMap<>();
        params.put("name", "John");

        String result = extension.format(template, params);
        assertEquals("Hello, John!", result);
    }

    @Test
    public void testFormatWithMultipleParameters() {
        String template = "User {username} has {count} messages.";
        Map<String, Object> params = new HashMap<>();
        params.put("username", "alice");
        params.put("count", 42);

        String result = extension.format(template, params);
        assertEquals("User alice has 42 messages.", result);
    }

    @Test
    public void testFormatWithMissingParameter() {
        String template = "Hello, {name}!";
        Map<String, Object> params = new HashMap<>();
        // Intentionally not adding "name" parameter

        String result = extension.format(template, params);
        // The ICU extension should leave the placeholder as is when parameter is missing
        assertEquals("Hello, {name}!", result);
    }

    @Test
    public void testFormatWithSpecialCharacters() {
        String template = "Hello, {name}! Your code is {code}.";
        Map<String, Object> params = new HashMap<>();
        params.put("name", "O'Reilly");
        params.put("code", "<script>alert('xss')</script>");

        String result = extension.format(template, params);
        assertEquals("Hello, O'Reilly! Your code is <script>alert('xss')</script>.", result);
    }

    @Test
    public void testFormatWithNumberParameter() {
        String template = "Total: {amount, number, currency}";
        Map<String, Object> params = new HashMap<>();
        params.put("amount", 1234.56);

        String result = extension.format(template, params);
        // ICU MessageFormat should format as currency, but exact format depends on default locale
        assertNotNull(result);
        assertFalse(result.contains("{amount"));
    }

    @Test
    public void testFormatWithDateParameter() {
        String template = "Date: {date, date, short}";
        Map<String, Object> params = new HashMap<>();
        params.put("date", new java.util.Date());

        String result = extension.format(template, params);
        // ICU MessageFormat should format as date, but exact format depends on default locale
        assertNotNull(result);
        assertFalse(result.contains("{date"));
    }

    @Test
    public void testFormatWithPluralParameter() {
        String template = "{count, plural, one{One message} other{# messages}}";
        Map<String, Object> params = new HashMap<>();
        
        // Test with singular
        params.put("count", 1);
        String resultSingular = extension.format(template, params);
        assertEquals("One message", resultSingular);
        
        // Test with plural
        params.put("count", 5);
        String resultPlural = extension.format(template, params);
        assertEquals("5 messages", resultPlural);
    }

    @Test
    public void testFormatWithSelectParameter() {
        String template = "{gender, select, male{He} female{She} other{They}} likes to code.";
        Map<String, Object> params = new HashMap<>();
        
        params.put("gender", "male");
        assertEquals("He likes to code.", extension.format(template, params));
        
        params.put("gender", "female");
        assertEquals("She likes to code.", extension.format(template, params));
        
        params.put("gender", "other");
        assertEquals("They likes to code.", extension.format(template, params));
    }
    
    @Test
    public void testFormatWithComplexTemplate() {
        String template = "Welcome, {username}! You have {unread, plural, " +
                "=0{no unread messages} " +
                "=1{one unread message} " +
                "other{# unread messages}}.";
        
        Map<String, Object> params = new HashMap<>();
        params.put("username", "Alice");
        
        // Test with zero messages
        params.put("unread", 0);
        assertEquals("Welcome, Alice! You have no unread messages.", 
                extension.format(template, params));
        
        // Test with one message
        params.put("unread", 1);
        assertEquals("Welcome, Alice! You have one unread message.", 
                extension.format(template, params));
        
        // Test with multiple messages
        params.put("unread", 42);
        assertEquals("Welcome, Alice! You have 42 unread messages.", 
                extension.format(template, params));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testFormatWithInvalidTemplate() {
        String template = "This has {an unclosed parameter";
        Map<String, Object> params = new HashMap<>();
        
        extension.format(template, params);
    }
} 