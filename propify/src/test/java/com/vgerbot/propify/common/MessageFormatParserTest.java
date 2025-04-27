package com.vgerbot.propify.common;

import org.junit.Test;
import org.junit.Assert;
import java.util.List;

public class MessageFormatParserTest {

    @Test
    public void testSimplePlaceholder() {
        String pattern = "Hello, {name}!";
        List<MessageFormatParser.PlaceholderInfo> placeholders = MessageFormatParser.parsePlaceholders(pattern);
        
        Assert.assertEquals(1, placeholders.size());
        Assert.assertEquals("name", placeholders.get(0).getName());
        Assert.assertNull(placeholders.get(0).getFormatType());
    }
    
    @Test
    public void testMultiplePlaceholders() {
        String pattern = "Hello, {firstName} {lastName}! Your age is {age}.";
        List<MessageFormatParser.PlaceholderInfo> placeholders = MessageFormatParser.parsePlaceholders(pattern);
        
        Assert.assertEquals(3, placeholders.size());
        Assert.assertEquals("firstName", placeholders.get(0).getName());
        Assert.assertEquals("lastName", placeholders.get(1).getName());
        Assert.assertEquals("age", placeholders.get(2).getName());
    }
    
    @Test
    public void testPlaceholderWithFormat() {
        String pattern = "The date is {date,date,yyyy-MM-dd}";
        List<MessageFormatParser.PlaceholderInfo> placeholders = MessageFormatParser.parsePlaceholders(pattern);
        
        Assert.assertEquals(1, placeholders.size());
        Assert.assertEquals("date", placeholders.get(0).getName());
        Assert.assertEquals("date", placeholders.get(0).getFormatType());
    }
    
    @Test
    public void testNumberPlaceholder() {
        String pattern = "The price is {0,number,currency}";
        List<MessageFormatParser.PlaceholderInfo> placeholders = MessageFormatParser.parsePlaceholders(pattern);
        
        Assert.assertEquals(1, placeholders.size());
        Assert.assertEquals("0", placeholders.get(0).getName());
        Assert.assertEquals("number", placeholders.get(0).getFormatType());
    }
    
    @Test
    public void testChoicePlaceholder() {
        String pattern = "{count,choice,0#No items|1#One item|1<{count} items}";
        List<MessageFormatParser.PlaceholderInfo> placeholders = MessageFormatParser.parsePlaceholders(pattern);
        
        Assert.assertEquals(2, placeholders.size());
        Assert.assertEquals("count", placeholders.get(0).getName());
        Assert.assertEquals("number", placeholders.get(0).getFormatType());
    }
    
    @Test
    public void testMixedPlaceholders() {
        String pattern = "On {date,date,short}, {name} bought {count,number} items for {price,number,currency}.";
        List<MessageFormatParser.PlaceholderInfo> placeholders = MessageFormatParser.parsePlaceholders(pattern);
        
        Assert.assertEquals(4, placeholders.size());
        
        Assert.assertEquals("date", placeholders.get(0).getName());
        Assert.assertEquals("date", placeholders.get(0).getFormatType());
        
        Assert.assertEquals("name", placeholders.get(1).getName());
        Assert.assertNull(placeholders.get(1).getFormatType());
        
        Assert.assertEquals("count", placeholders.get(2).getName());
        Assert.assertEquals("number", placeholders.get(2).getFormatType());
        
        Assert.assertEquals("price", placeholders.get(3).getName());
        Assert.assertEquals("number", placeholders.get(3).getFormatType());
    }
    
    @Test
    public void testEmptyPattern() {
        String pattern = "";
        List<MessageFormatParser.PlaceholderInfo> placeholders = MessageFormatParser.parsePlaceholders(pattern);
        
        Assert.assertTrue(placeholders.isEmpty());
    }
    
    @Test
    public void testPatternWithoutPlaceholders() {
        String pattern = "Hello, world!";
        List<MessageFormatParser.PlaceholderInfo> placeholders = MessageFormatParser.parsePlaceholders(pattern);
        
        Assert.assertTrue(placeholders.isEmpty());
    }
    
    @Test
    public void testNestedPlaceholders() {
        String pattern = "The time is {time,choice,0#morning {hour,number}:{minute,number} AM|1#afternoon {hour,number}:{minute,number} PM}";
        List<MessageFormatParser.PlaceholderInfo> placeholders = MessageFormatParser.parsePlaceholders(pattern);
        
        // The ICU MessageFormat library should identify the following placeholders:
        // 1. time (with type choice)
        // 2. hour (with type number)
        // 3. minute (with type number)
        // 4. hour (with type number) - from the second choice option
        // 5. minute (with type number) - from the second choice option
        
        Assert.assertEquals(5, placeholders.size());
        Assert.assertEquals("time", placeholders.get(0).getName());
        Assert.assertEquals("number", placeholders.get(0).getFormatType());
    }
    
    @Test
    public void testEscapedCurlyBraces() {
        String pattern = "Literal braces: '{' and '}' with {param} placeholder";
        List<MessageFormatParser.PlaceholderInfo> placeholders = MessageFormatParser.parsePlaceholders(pattern);
        
        Assert.assertEquals(1, placeholders.size());
        Assert.assertEquals("param", placeholders.get(0).getName());
    }
    
    @Test
    public void testDuplicatePlaceholderNames() {
        String pattern = "{name} is {age} years old. Hello {name}!";
        List<MessageFormatParser.PlaceholderInfo> placeholders = MessageFormatParser.parsePlaceholders(pattern);
        
        Assert.assertEquals(3, placeholders.size());
        Assert.assertEquals("name", placeholders.get(0).getName());
        Assert.assertEquals("age", placeholders.get(1).getName());
        Assert.assertEquals("name", placeholders.get(2).getName());
    }
    
    @Test
    public void testPluralFormat() {
        String pattern = "{count,plural,one{# item}other{# items}}";
        List<MessageFormatParser.PlaceholderInfo> placeholders = MessageFormatParser.parsePlaceholders(pattern);
        
        Assert.assertEquals(1, placeholders.size());
        Assert.assertEquals("count", placeholders.get(0).getName());
        Assert.assertEquals("plural", placeholders.get(0).getFormatType());
    }
    
    @Test
    public void testSelectFormat() {
        String pattern = "{gender,select,male{He}female{She}other{They}}";
        List<MessageFormatParser.PlaceholderInfo> placeholders = MessageFormatParser.parsePlaceholders(pattern);
        
        Assert.assertEquals(1, placeholders.size());
        Assert.assertEquals("gender", placeholders.get(0).getName());
        Assert.assertEquals("select", placeholders.get(0).getFormatType());
    }
    
    @Test
    public void testComplexNested() {
        String pattern = "There {count,plural,one{is # item}other{are # items}} in your {category,select,cart{shopping cart}wishlist{wishlist}other{list}}.";
        List<MessageFormatParser.PlaceholderInfo> placeholders = MessageFormatParser.parsePlaceholders(pattern);
        
        Assert.assertEquals(2, placeholders.size());
        Assert.assertEquals("count", placeholders.get(0).getName());
        Assert.assertEquals("plural", placeholders.get(0).getFormatType());
        Assert.assertEquals("category", placeholders.get(1).getName());
        Assert.assertEquals("select", placeholders.get(1).getFormatType());
    }
    
    @Test
    public void testNullPattern() {
        try {
            MessageFormatParser.parsePlaceholders(null);
            Assert.fail("Expected NullPointerException was not thrown");
        } catch (NullPointerException e) {
            // Expected behavior - ICU MessagePattern constructor should throw NPE for null input
        }
    }
    
    @Test
    public void testMalformedPattern() {
        try {
            // Unclosed brace
            String pattern = "Hello, {name!";
            List<MessageFormatParser.PlaceholderInfo> placeholders = MessageFormatParser.parsePlaceholders(pattern);
            
            // The ICU MessagePattern library should either throw an exception or 
            // return no placeholders for malformed patterns
            Assert.assertTrue(placeholders.isEmpty());
        } catch (IllegalArgumentException e) {
            // This is also acceptable - ICU might throw an exception for malformed patterns
        }
    }
    
    @Test
    public void testInvalidFormatType() {
        try {
            // Invalid format type
            String pattern = "{name,invalidtype}";
            List<MessageFormatParser.PlaceholderInfo> placeholders = MessageFormatParser.parsePlaceholders(pattern);
            
            // The class should still extract the name but the format type might be treated literally
            Assert.assertEquals(1, placeholders.size());
            Assert.assertEquals("name", placeholders.get(0).getName());
            Assert.assertEquals("invalidtype", placeholders.get(0).getFormatType());
        } catch (IllegalArgumentException e) {
            // Also acceptable if ICU throws exception for invalid format types
        }
    }
} 