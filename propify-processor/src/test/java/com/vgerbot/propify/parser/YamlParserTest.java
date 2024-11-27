package com.vgerbot.propify.parser;

import com.vgerbot.propify.PropifyProperties;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.*;

public class YamlParserTest {
    private YamlParser parser;

    @Before
    public void setUp() {
        parser = new YamlParser();
    }

    @Test
    public void testAccept_ValidMediaTypes_ReturnsTrue() {
        assertTrue(parser.accept("application/yaml"));
        assertTrue(parser.accept("application/x-yaml"));
        assertTrue(parser.accept("text/yaml"));
        assertTrue(parser.accept("text/x-yaml"));
        // Test case insensitivity
        assertTrue(parser.accept("APPLICATION/YAML"));
    }

    @Test
    public void testAccept_InvalidMediaTypes_ReturnsFalse() {
        assertFalse(parser.accept("application/json"));
        assertFalse(parser.accept("text/plain"));
        assertFalse(parser.accept(""));
    }

    @Test
    public void testParse_SimpleYaml_ReturnsCorrectProperties() throws IOException {
        String yaml = "name: John\nage: 30\nactive: true";
        PropifyProperties props = parseYaml(yaml);

        assertEquals("John", props.get("name"));
        assertEquals(30, props.get("age"));
        assertEquals(true, props.get("active"));
    }

    @Test
    public void testParse_NestedYaml_ReturnsNestedProperties() throws IOException {
        String yaml = 
            "person:\n" +
            "  name: John\n" +
            "  address:\n" +
            "    street: Main St\n" +
            "    city: Boston\n";
        
        PropifyProperties props = parseYaml(yaml);
        PropifyProperties person = (PropifyProperties) props.get("person");
        PropifyProperties address = (PropifyProperties) person.get("address");

        assertEquals("John", person.get("name"));
        assertEquals("Main St", address.get("street"));
        assertEquals("Boston", address.get("city"));
    }

    @Test
    public void testParse_ComplexTypes_ParsesCorrectly() throws IOException {
        String yaml =
            "numbers:\n" +
            "  integer: 42\n" +
            "  double: 3.14\n" +
            "  scientific: 1.23e4\n" +
            "array: [1, 2, 3]\n" +
            "boolean: true\n" +
            "null_value: null\n";

        PropifyProperties props = parseYaml(yaml);
        PropifyProperties numbers = (PropifyProperties) props.get("numbers");

        assertEquals(42, numbers.get("integer"));
        assertEquals(3.14, numbers.get("double"));
        assertEquals(12300.0, numbers.get("scientific"));
        assertArrayEquals(new Integer[]{1, 2, 3}, ((java.util.List<?>) props.get("array")).toArray(new Integer[0]));
        assertEquals(true, props.get("boolean"));
        assertNull(props.get("null_value"));
    }

    @Test
    public void testParse_EmptyYaml_ReturnsEmptyProperties() throws IOException {
        String yaml = "";
        PropifyProperties props = parseYaml(yaml);
        assertTrue(props.isEmpty());
    }

    @Test(expected = IOException.class)
    public void testParse_InvalidYaml_ThrowsIOException() throws IOException {
        String yaml = "invalid:\n  \"this is not valid yaml";
        parseYaml(yaml);
    }

    @Test
    public void testParse_MultipleDocuments_ParsesFirstDocument() throws IOException {
        String yaml = 
            "first: document\n" +
            "---\n" +
            "second: document\n";
        
        PropifyProperties props = parseYaml(yaml);
        assertEquals("document", props.get("first"));
        assertNull(props.get("second"));
    }

    @Test
    public void testParse_DeepNestedStructure_HandlesCorrectly() throws IOException {
        String yaml =
            "level1:\n" +
            "  level2:\n" +
            "    level3:\n" +
            "      level4:\n" +
            "        value: deep\n";

        PropifyProperties props = parseYaml(yaml);
        PropifyProperties level1 = (PropifyProperties) props.get("level1");
        PropifyProperties level2 = (PropifyProperties) level1.get("level2");
        PropifyProperties level3 = (PropifyProperties) level2.get("level3");
        PropifyProperties level4 = (PropifyProperties) level3.get("level4");

        assertEquals("deep", level4.get("value"));
    }

    private PropifyProperties parseYaml(String yaml) throws IOException {
        try (InputStream stream = new ByteArrayInputStream(yaml.getBytes(StandardCharsets.UTF_8))) {
            return parser.parse(stream);
        }
    }
}
