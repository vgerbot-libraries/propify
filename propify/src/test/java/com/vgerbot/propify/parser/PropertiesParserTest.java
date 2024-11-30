package com.vgerbot.propify.parser;

import com.vgerbot.propify.Propify;
import com.vgerbot.propify.PropifyContext;
import com.vgerbot.propify.PropifyProperties;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PropertiesParserTest {

    private PropertiesParser parser;

    private PropifyContext context;
    @Mock
    private Propify propifyAnnotation;
    @Mock private ProcessingEnvironment processingEnv;
    @Mock private Filer filer;
    @Mock private Messager messager;
    @Before
    public void setUp() {
        parser = new PropertiesParser();
//        when(processingEnv.getFiler()).thenReturn(filer);
//        when(processingEnv.getMessager()).thenReturn(messager);
        when(propifyAnnotation.location()).thenReturn("classpath: test.yml");
        when(propifyAnnotation.autoTypeConversion()).thenReturn(true);
        context = new PropifyContext(
                propifyAnnotation,
                processingEnv
        );
    }

    @Test
    public void testAccept_ValidMediaTypes_ReturnsTrue() {
        assertTrue(parser.accept("application/java-properties"));
        assertTrue(parser.accept("application/x-java-properties"));
        assertTrue(parser.accept("text/java-properties"));
        assertTrue(parser.accept("text/x-java-properties"));
        // Test case insensitivity
        assertTrue(parser.accept("APPLICATION/JAVA-PROPERTIES"));
    }

    @Test
    public void testAccept_InvalidMediaTypes_ReturnsFalse() {
        assertFalse(parser.accept("application/json"));
        assertFalse(parser.accept("text/plain"));
        assertFalse(parser.accept(""));
    }

    @Test
    public void testParse_SimpleProperties_ReturnsCorrectProperties() throws IOException {
        String props = "name=John\nage=30\nactive=true";
        PropifyProperties properties = parseProperties(props);

        assertEquals("John", properties.get("name"));
        assertEquals(30, properties.get("age"));
        assertEquals(true, properties.get("active"));
    }

    @Test
    public void testParse_NestedProperties_ReturnsNestedStructure() throws IOException {
        String props = 
            "person.name=John\n" +
            "person.address.street=Main St\n" +
            "person.address.city=Boston";

        PropifyProperties properties = parseProperties(props);
        PropifyProperties person = (PropifyProperties) properties.get("person");
        PropifyProperties address = (PropifyProperties) person.get("address");

        assertEquals("John", person.get("name"));
        assertEquals("Main St", address.get("street"));
        assertEquals("Boston", address.get("city"));
    }


    @Test
    public void testParse_EmptyProperties_ReturnsEmptyProperties() throws IOException {
        String props = "";
        PropifyProperties properties = parseProperties(props);
        assertTrue(properties.isEmpty());
    }

    @Test
    public void testParse_MixedNestedAndFlatProperties_HandlesCorrectly() throws IOException {
        String props = 
            "name=John\n" +
            "address.street=Main St\n" +
            "age=30\n" +
            "address.city=Boston";

        PropifyProperties properties = parseProperties(props);
        PropifyProperties address = (PropifyProperties) properties.get("address");

        assertEquals("John", properties.get("name"));
        assertEquals(30, properties.get("age"));
        assertEquals("Main St", address.get("street"));
        assertEquals("Boston", address.get("city"));
    }

    @Test
    public void testParse_DeepNestedStructure_HandlesCorrectly() throws IOException {
        String props = 
            "level1.level2.level3.level4.value=deep\n" +
            "level1.level2.value=medium\n" +
            "level1.value=shallow";

        PropifyProperties properties = parseProperties(props);
        PropifyProperties level1 = (PropifyProperties) properties.get("level1");
        PropifyProperties level2 = (PropifyProperties) level1.get("level2");
        PropifyProperties level3 = (PropifyProperties) level2.get("level3");
        PropifyProperties level4 = (PropifyProperties) level3.get("level4");

        assertEquals("deep", level4.get("value"));
        assertEquals("medium", level2.get("value"));
        assertEquals("shallow", level1.get("value"));
    }

    @Test
    public void testParse_SpecialCharactersInValues_PreservesCharacters() throws IOException {
        String props = 
            "special=!@#$%^&*()\n" +
            "unicode=こんにちは\n" +
            "url=https://example.com?param=value\n" +
            "path=C:\\\\Program Files\\\\App";

        PropifyProperties properties = parseProperties(props);

        assertEquals("!@#$%^&*()", properties.get("special"));
        assertEquals("こんにちは", properties.get("unicode"));
        assertEquals("https://example.com?param=value", properties.get("url"));
        assertEquals("C:\\Program Files\\App", properties.get("path"));
    }

    @Test
    public void testParse_CommentsAndEmptyLines_IgnoresCorrectly() throws IOException {
        String props = 
            "# This is a comment\n" +
            "\n" +
            "name=John\n" +
            "! Another comment\n" +
            "\n" +
            "age=30";

        PropifyProperties properties = parseProperties(props);

        assertEquals("John", properties.get("name"));
        assertEquals(30, properties.get("age"));
        assertEquals(2, properties.size());
    }


    private PropifyProperties parseProperties(String props) throws IOException {
        try (InputStream stream = new ByteArrayInputStream(props.getBytes(StandardCharsets.UTF_8))) {
            return parser.parse(context, stream);
        }
    }
}
