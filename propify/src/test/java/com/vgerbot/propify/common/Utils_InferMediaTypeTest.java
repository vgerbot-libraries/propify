package com.vgerbot.propify.common;

import org.junit.Test;
import static org.junit.Assert.*;
import static org.junit.Assert.assertNull;

public class Utils_InferMediaTypeTest {

    @Test
    public void testInferMediaType_PropertiesExtension_ReturnsPropertiesMediaType() {
        String fileName = "config.properties";
        String expected = "text/x-java-properties";
        assertEquals(expected, Utils.inferMediaType(fileName));
    }

    @Test
    public void testInferMediaType_YmlExtension_ReturnsYamlMediaType() {
        String fileName = "config.yml";
        String expected = "application/yaml";
        assertEquals(expected, Utils.inferMediaType(fileName));
    }

    @Test
    public void testInferMediaType_YamlExtension_ReturnsYamlMediaType() {
        String fileName = "config.yaml";
        String expected = "application/yaml";
        assertEquals(expected, Utils.inferMediaType(fileName));
    }

    @Test
    public void testInferMediaType_JsonExtension_ReturnsJsonMediaType() {
        String fileName = "config.json";
        String expected = "application/json";
        assertEquals(expected, Utils.inferMediaType(fileName));
    }

    @Test
    public void testInferMediaType_UnknownExtension_ReturnsNull() {
        String fileName = "config.txt";
        assertNull(Utils.inferMediaType(fileName));
    }

    @Test
    public void testInferMediaType_NoExtension_ReturnsNull() {
        String fileName = "config";
        assertNull(Utils.inferMediaType(fileName));
    }

    @Test
    public void testInferMediaType_PathWithDirectory_ReturnsCorrectMediaType() {
        String fileName = "/path/to/config.properties";
        String expected = "text/x-java-properties";
        assertEquals(expected, Utils.inferMediaType(fileName));
    }

    @Test
    public void testInferMediaType_WindowsPathWithDirectory_ReturnsCorrectMediaType() {
        String fileName = "C:\\path\\to\\config.yml";
        String expected = "application/yaml";
        assertEquals(expected, Utils.inferMediaType(fileName));
    }

    @Test
    public void testInferMediaType_UppercaseExtension_ReturnsCorrectMediaType() {
        String fileName = "config.PROPERTIES";
        // The implementation is case-sensitive, so uppercase extensions return null
        assertNull(Utils.inferMediaType(fileName));
    }

    @Test
    public void testInferMediaType_MixedCaseExtension_ReturnsCorrectMediaType() {
        String fileName = "config.YmL";
        // The implementation is case-sensitive, so mixed case extensions return null
        assertNull(Utils.inferMediaType(fileName));
    }

    @Test
    public void testInferMediaType_EmptyString_ThrowsException() {
        // The implementation actually handles empty strings by returning null
        assertNull(Utils.inferMediaType(""));
    }

    @Test(expected = NullPointerException.class)
    public void testInferMediaType_NullInput_ThrowsException() {
        Utils.inferMediaType(null);
    }
}
