package com.vgerbot.propify.parser;

import com.vgerbot.propify.core.PropifyContext;
import org.apache.commons.configuration2.Configuration;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@Ignore
public class PropertiesConfigParserTest {

    private PropertiesConfigParser parser;

    @Mock
    private PropifyContext context;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        parser = new PropertiesConfigParser();
        when(context.getListDelimiter()).thenReturn(',');
    }

    @Test
    public void parse_ValidPropertiesContent_ShouldParseSuccessfully() throws IOException {
        // Given
        String content =
                new StringBuilder()
                        .append("database.host=localhost\n")
                        .append("database.port=5432\n")
                        .append("app.name=TestApp\n")
                        .append("app.debug=true\n")
                        .toString();
        InputStream stream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));

        // When
        Configuration config = parser.parse(context, stream);

        // Then
        assertNotNull(config);
        assertEquals("localhost", config.getString("database.host"));
        assertEquals(5432, config.getInt("database.port"));
        assertEquals("TestApp", config.getString("app.name"));
        assertTrue(config.getBoolean("app.debug"));
    }

    @Test
    public void parse_ListProperties_ShouldParseSuccessfully() throws IOException {
        // Given
        String content = "app.tags=tag1,tag2,tag3\napp.numbers=1,2,3";
        InputStream stream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));

        // When
        Configuration config = parser.parse(context, stream);

        // Then
        assertNotNull(config);
        List<String> tags = config.getList(String.class, "app.tags");
        List<Integer> numbers = config.getList(Integer.class, "app.numbers");
        
        assertEquals(Arrays.asList("tag1", "tag2", "tag3"), tags);
        assertEquals(Arrays.asList(1, 2, 3), numbers);
    }

    @Test
    public void parse_CustomListDelimiter_ShouldParseSuccessfully() throws IOException {
        // Given
        when(context.getListDelimiter()).thenReturn(';');
        String content = "app.tags=tag1;tag2;tag3";
        InputStream stream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));

        // When
        Configuration config = parser.parse(context, stream);

        // Then
        assertNotNull(config);
        List<String> tags = config.getList(String.class, "app.tags");
        assertEquals(Arrays.asList("tag1", "tag2", "tag3"), tags);
    }

    @Test
    public void parse_EmptyProperty_ShouldParseSuccessfully() throws IOException {
        // Given
        String content = "empty.property=";
        InputStream stream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));

        // When
        Configuration config = parser.parse(context, stream);

        // Then
        assertNotNull(config);
        assertTrue(config.containsKey("empty.property"));
        assertEquals("", config.getString("empty.property"));
    }

    @Test
    public void parse_NullInputStream_ShouldThrowIOException() {
        // When/Then
        assertThrows(IOException.class, () -> parser.parse(context, null));
    }

    @Test
    public void accept_PropertiesFileExtension_ShouldReturnTrue() {
        // Given
        when(context.getLocation()).thenReturn("config.properties");
        when(context.getMediaType()).thenReturn(null);

        // When
        boolean result = parser.accept(context);

        // Then
        assertTrue(result);
    }

    @Test
    public void accept_JavaPropertiesMediaType_ShouldReturnTrue() {
        // Given
        when(context.getLocation()).thenReturn("");
        when(context.getMediaType()).thenReturn("application/java-properties");

        // When
        boolean result = parser.accept(context);

        // Then
        assertTrue(result);
    }

    @Test
    public void accept_XJavaPropertiesMediaType_ShouldReturnTrue() {
        // Given
        when(context.getLocation()).thenReturn("");
        when(context.getMediaType()).thenReturn("application/x-java-properties");

        // When
        boolean result = parser.accept(context);

        // Then
        assertTrue(result);
    }

    @Test
    public void accept_TextJavaPropertiesMediaType_ShouldReturnTrue() {
        // Given
        when(context.getLocation()).thenReturn("");
        when(context.getMediaType()).thenReturn("text/java-properties");

        // When
        boolean result = parser.accept(context);

        // Then
        assertTrue(result);
    }

    @Test
    public void accept_NonPropertiesFileExtension_ShouldReturnFalse() {
        // Given
        when(context.getLocation()).thenReturn("config.yaml");
        when(context.getMediaType()).thenReturn(null);

        // When
        boolean result = parser.accept(context);

        // Then
        assertFalse(result);
    }

    @Test
    public void accept_NonPropertiesMediaType_ShouldReturnFalse() {
        // Given
        when(context.getLocation()).thenReturn("");
        when(context.getMediaType()).thenReturn("application/json");

        // When
        boolean result = parser.accept(context);

        // Then
        assertFalse(result);
    }

    @Test
    public void accept_NullMediaTypeAndEmptyLocation_ShouldReturnFalse() {
        // Given
        when(context.getLocation()).thenReturn("");
        when(context.getMediaType()).thenReturn(null);

        // When
        boolean result = parser.accept(context);

        // Then
        assertFalse(result);
    }
}
