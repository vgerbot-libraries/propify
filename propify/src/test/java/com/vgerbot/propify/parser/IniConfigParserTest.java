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

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

public class IniConfigParserTest {

    private IniConfigParser parser;

    @Mock
    private PropifyContext context;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        parser = new IniConfigParser();
    }

    @Test
    public void parse_ValidIniContent_ShouldParseSuccessfully() throws IOException {
        // Given
        String iniContent =
                new StringBuilder()
                        .append("[database]\n")
                        .append("host=localhost\n")
                        .append("port=5432\n")
                        .append("\n")
                        .append("[application]\n")
                        .append("name=TestApp\n")
                        .append("\n")
                        .toString();
        InputStream stream = new ByteArrayInputStream(iniContent.getBytes(StandardCharsets.UTF_8));

        // When
        Configuration config = parser.parse(context, stream);

        // Then
        assertNotNull(config);
        assertEquals("localhost", config.getString("database.host"));
        assertEquals(5432, config.getInt("database.port"));
        assertEquals("TestApp", config.getString("application.name"));
    }

    @Test
    public void parse_EmptyContent_ShouldReturnEmptyConfiguration() throws IOException {
        // Given
        String emptyContent = "";
        InputStream stream = new ByteArrayInputStream(emptyContent.getBytes(StandardCharsets.UTF_8));

        // When
        Configuration config = parser.parse(context, stream);

        // Then
        assertNotNull(config);
        assertTrue(config.isEmpty());
    }

    @Test
    public void accept_IniFileExtension_ShouldReturnTrue() {
        // Given
        when(context.getLocation()).thenReturn("config.ini");
        when(context.getMediaType()).thenReturn(null);

        // When
        boolean result = parser.accept(context);

        // Then
        assertTrue(result);
    }

    @Test
    public void accept_IniMediaType_ShouldReturnTrue() {
        // Given
        when(context.getLocation()).thenReturn("");
        when(context.getMediaType()).thenReturn("text/ini");

        // When
        boolean result = parser.accept(context);

        // Then
        assertTrue(result);
    }

    @Test
    public void accept_PlainTextMediaType_ShouldReturnTrue() {
        // Given
        when(context.getLocation()).thenReturn("");
        when(context.getMediaType()).thenReturn("text/plain");

        // When
        boolean result = parser.accept(context);

        // Then
        assertTrue(result);
    }

    @Test
    public void accept_NonIniFileExtension_ShouldReturnFalse() {
        // Given
        when(context.getLocation()).thenReturn("config.yaml");
        when(context.getMediaType()).thenReturn(null);

        // When
        boolean result = parser.accept(context);

        // Then
        assertFalse(result);
    }

    @Test
    public void accept_NonIniMediaType_ShouldReturnFalse() {
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
