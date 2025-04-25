package com.vgerbot.propify.parser;

import com.vgerbot.propify.core.PropifyContext;
import com.vgerbot.propify.core.ResourceLoaderProvider;
import com.vgerbot.propify.logger.Logger;
import org.apache.commons.configuration2.Configuration;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.CoreMatchers.*;

public class YamlConfigParserTest {

    private YamlConfigParser parser;

    @Mock
    private PropifyContext context;

    @Mock
    private ResourceLoaderProvider resourceLoaderProvider;

    @Mock
    private Logger logger;

    private static final String YAML_CONTENT = 
            "app:\n" +
            "  name: Test Application\n" +
            "  version: 1.0.0\n" +
            "database:\n" +
            "  url: jdbc:mysql://localhost:3306/testdb\n" +
            "  credentials:\n" +
            "    username: admin\n" +
            "    password: password\n" +
            "features:\n" +
            "  - logging\n" +
            "  - security\n" +
            "  - monitoring\n" +
            "enabled: true\n" +
            "maxConnections: 100\n";

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        
        when(context.getMediaType()).thenReturn("application/yaml");
        when(context.getLogger()).thenReturn(logger);
        
        parser = new YamlConfigParser();
    }

    @Test
    public void testAccept() {
        PropifyContext context = mock(PropifyContext.class);
        // Supported media types
        when(context.getMediaType()).thenReturn("application/yaml");
        assertThat(parser.accept(context), is(true));

        when(context.getMediaType()).thenReturn("text/yaml");
        assertThat(parser.accept(context), is(true));

        when(context.getMediaType()).thenReturn("application/x-yaml");
        assertThat(parser.accept(context), is(true));

        when(context.getMediaType()).thenReturn("application/json");
        assertThat(parser.accept(context), is(false));

        when(context.getMediaType()).thenReturn("text/plain");
        assertThat(parser.accept(context), is(false));

        when(context.getMediaType()).thenReturn(null);
        assertThat(parser.accept(context), is(false));

        when(context.getLocation()).thenReturn("x.yaml");
        assertThat(parser.accept(context), is(true));

        when(context.getLocation()).thenReturn("x.yml");
        assertThat(parser.accept(context), is(true));

        when(context.getLocation()).thenReturn("x.xml");
        assertThat(parser.accept(context), is(false));
    }

    @Test
    public void testParse() throws Exception {
        InputStream inputStream = new ByteArrayInputStream(
                YAML_CONTENT.getBytes(StandardCharsets.UTF_8));

        Configuration configuration = parser.parse(context, inputStream);

        // Verify configuration was parsed correctly
        assertThat(configuration, notNullValue());

        // Test simple properties
        assertThat(configuration.getString("app.name"), is("Test Application"));
        assertThat(configuration.getString("app.version"), is("1.0.0"));
        assertThat(configuration.getString("database.url"), is("jdbc:mysql://localhost:3306/testdb"));
        assertThat(configuration.getString("database.credentials.username"), is("admin"));
        assertThat(configuration.getString("database.credentials.password"), is("password"));
        assertThat(configuration.getBoolean("enabled"), is(true));
        assertThat(configuration.getInt("maxConnections"), is(100));

        // Test array access
        assertThat(configuration.getList("features"), notNullValue());
        assertThat(configuration.getList("features").size(), is(3));
        assertThat(configuration.getList("features"), hasItems("logging", "security", "monitoring"));
    }

//
//    @Test
//    public void testParseSubset() throws Exception {
//        InputStream inputStream = new ByteArrayInputStream(
//                YAML_CONTENT.getBytes(StandardCharsets.UTF_8));
//
//        Configuration configuration = parser.parse(context, inputStream);
//
//        // Get a subset of the configuration
//        Configuration databaseConfig = configuration.subset("database");
//
//        assertNotNull(databaseConfig);
//        assertEquals("jdbc:mysql://localhost:3306/testdb", databaseConfig.getString("url"));
//
//        Configuration credentialsConfig = databaseConfig.subset("credentials");
//        assertNotNull(credentialsConfig);
//        assertEquals("admin", credentialsConfig.getString("username"));
//        assertEquals("password", credentialsConfig.getString("password"));
//    }
//
//    @Test
//    public void testIterator() throws Exception {
//        InputStream inputStream = new ByteArrayInputStream(
//                YAML_CONTENT.getBytes(StandardCharsets.UTF_8));
//
//        Configuration configuration = parser.parse(context, inputStream);
//
//        // Verify we can iterate over the keys
//        Iterator<String> keys = configuration.getKeys();
//        assertNotNull(keys);
//
//        boolean hasAppKey = false;
//        boolean hasDatabaseKey = false;
//        boolean hasFeaturesKey = false;
//        boolean hasEnabledKey = false;
//        boolean hasMaxConnectionsKey = false;
//
//        while (keys.hasNext()) {
//            String key = keys.next();
//            switch (key) {
//                case "app":
//                    hasAppKey = true;
//                    break;
//                case "database":
//                    hasDatabaseKey = true;
//                    break;
//                case "features":
//                    hasFeaturesKey = true;
//                    break;
//                case "enabled":
//                    hasEnabledKey = true;
//                    break;
//                case "maxConnections":
//                    hasMaxConnectionsKey = true;
//                    break;
//            }
//        }
//
//        assertTrue(hasAppKey);
//        assertTrue(hasDatabaseKey);
//        assertTrue(hasFeaturesKey);
//        assertTrue(hasEnabledKey);
//        assertTrue(hasMaxConnectionsKey);
//    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testParseWithNullContext() throws Exception {
        InputStream inputStream = new ByteArrayInputStream(
                YAML_CONTENT.getBytes(StandardCharsets.UTF_8));
        
        parser.parse(null, inputStream);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testParseWithNullInputStream() throws Exception {
        parser.parse(context, null);
    }

    @Test
    public void testParseValidYaml() throws IOException {
        String yaml = "server:\n" +
                "  port: 8080\n" +
                "  host: localhost\n" +
                "database:\n" +
                "  url: jdbc:mysql://localhost:3306/db\n" +
                "  credentials:\n" +
                "    username: user\n" +
                "    password: pass\n";

        InputStream input = new ByteArrayInputStream(yaml.getBytes(StandardCharsets.UTF_8));
        Configuration config = parser.parse(context, input);

        assertThat("Configuration should not be null", config, is(notNullValue()));

        assertThat(config.getProperty("server.port"), is(8080));
        assertThat(config.getProperty("server.host"), is("localhost"));

        assertThat(config.getProperty("database.credentials.username"), is("user"));
        assertThat(config.getProperty("database.credentials.password"), is("pass"));
    }

    @Test
    public void testParseEmptyYaml() throws IOException {
        String yaml = "";
        InputStream input = new ByteArrayInputStream(yaml.getBytes(StandardCharsets.UTF_8));
        Configuration config = parser.parse(context, input);
        assertThat("Configuration should not be null even for empty YAML", config, is(notNullValue()));
    }

    @Test
    public void testParseComplexYaml() throws IOException {
        String yaml = "# Complex YAML with various data types and structures\n" +
                "primitives:\n" +
                "  string: Hello World\n" +
                "  integer: 42\n" +
                "  float: 3.14159\n" +
                "  boolean: true\n" +
                "  nullValue: null\n" +
                "lists:\n" +
                "  simpleList:\n" +
                "    - item1\n" +
                "    - item2\n" +
                "    - item3\n" +
                "  mixedTypeList:\n" +
                "    - string value\n" +
                "    - 123\n" +
                "    - true\n" +
                "    - null\n" +
                "  emptyList: []\n" +
                "nested:\n" +
                "  level1:\n" +
                "    level2:\n" +
                "      level3:\n" +
                "        deepValue: deep nested value\n" +
                "  objects:\n" +
                "    - id: 1\n" +
                "      name: first object\n" +
                "      active: true\n" +
                "      tags:\n" +
                "        - important\n" +
                "        - primary\n" +
                "    - id: 2\n" +
                "      name: second object\n" +
                "      active: false\n" +
                "      tags:\n" +
                "        - secondary\n" +
                "complexMapping:\n" +
                "  key1:\n" +
                "    nestedKey: nestedValue\n" +
                "  'key with spaces': special value\n" +
                "  numeric-key-123: numeric key value\n";

        InputStream input = new ByteArrayInputStream(yaml.getBytes(StandardCharsets.UTF_8));
        Configuration config = parser.parse(context, input);

        // Verify the configuration is not null
        assertThat("Configuration should not be null", config, is(notNullValue()));

        assertThat(config.getProperty("primitives.string"), is("Hello World"));
        assertThat(config.getInt("primitives.integer"), is(42));
        assertThat(config.getDouble("primitives.float"), is(3.14159));
        assertThat(config.getBoolean("primitives.boolean"), is(true));
        assertThat(config.getProperty("primitives.nullValue"), is(nullValue()));

        // Test lists
        assertThat(config.getProperty("lists.simpleList"), instanceOf(java.util.List.class));
        java.util.List<?> simpleList = (java.util.List<?>) config.getProperty("lists.simpleList");
        assertThat(simpleList.size(), is(3));
        assertThat(simpleList.contains("item1"), is(true));
        assertThat(simpleList.contains("item2"), is(true));
        assertThat(simpleList.contains("item3"), is(true));

        // Test mixed type list
        assertThat(config.getProperty("lists.mixedTypeList"), instanceOf(java.util.List.class));
        java.util.List<?> mixedTypeList = (java.util.List<?>) config.getProperty("lists.mixedTypeList");
        assertThat(mixedTypeList.size(), is(4));
        assertThat(config.getString("lists.mixedTypeList[0]"), is("string value"));
        assertThat(config.getInt("lists.mixedTypeList[1]"), is(123));
        assertThat(config.getBoolean("lists.mixedTypeList[2]"), is(true));
        assertThat(config.getProperty("lists.mixedTypeList[3]"), is(nullValue()));

        // Test empty list
        assertThat(config.getProperty("lists.emptyList"), instanceOf(java.util.List.class));
        java.util.List<?> emptyList = (java.util.List<?>) config.getProperty("lists.emptyList");
        assertThat(emptyList.isEmpty(), is(true));

        // Test deeply nested values
        assertThat(config.getProperty("nested.level1.level2.level3.deepValue"), is("deep nested value"));

        // Test nested objects in array
        assertThat(config.getProperty("nested.objects"), instanceOf(java.util.List.class));
        java.util.List<?> objectsList = (java.util.List<?>) config.getProperty("nested.objects");
        assertThat(objectsList.size(), is(2));

        Object firstObject = objectsList.get(0);
        assertThat(firstObject, instanceOf(Map.class));
        Map firstObjectMap = (Map) firstObject;

        assertThat(firstObjectMap.get("id"), is(1));
        assertThat(firstObjectMap.get("name"), is("first object"));
        assertThat(firstObjectMap.get("active"), is(true));
        assertThat(firstObjectMap.get("tags"), instanceOf(java.util.List.class));
        java.util.List<?> firstObjectTags = (java.util.List<?>) firstObjectMap.get("tags");
        assertThat(firstObjectTags.contains("important"), is(true));
        assertThat(firstObjectTags.contains("primary"), is(true));

        Object secondObject = objectsList.get(1);
        assertThat(secondObject, instanceOf(Map.class));
        Map secondObjectMap = (Map) secondObject;

        assertThat(secondObjectMap.get("id"), is(2));
        assertThat(secondObjectMap.get("name"), is("second object"));
        assertThat(secondObjectMap.get("active"), is(false));
        assertThat(secondObjectMap.get("tags"), instanceOf(java.util.List.class));
        java.util.List<?> secondObjectTags = (java.util.List<?>) secondObjectMap.get("tags");
        assertThat(secondObjectTags.contains("secondary"), is(true));

        // Test complex mapping with special keys
        assertThat(config.getProperty("complexMapping.key1.nestedKey"), is("nestedValue"));
        assertThat(config.getProperty("complexMapping.key with spaces"), is("special value"));
        assertThat(config.getProperty("complexMapping.numeric-key-123"), is("numeric key value"));
    }

    @Test
    public void testAcceptWithYamlMediaType() {
        when(context.getMediaType()).thenReturn("application/yaml");
        assertThat(parser.accept(context), is(true));

        when(context.getMediaType()).thenReturn("text/yaml");
        assertThat(parser.accept(context), is(true));

        when(context.getMediaType()).thenReturn("application/x-yaml");
        assertThat(parser.accept(context), is(true));

        when(context.getMediaType()).thenReturn("text/x-yaml");
        assertThat(parser.accept(context), is(true));
    }

    @Test
    public void testAcceptWithInvalidMediaType() {
        when(context.getMediaType()).thenReturn("application/json");
        assertThat(parser.accept(context), is(false));
    }

    @Test
    public void testAcceptWithYamlFileExtension() {
        when(context.getMediaType()).thenReturn("");
        when(context.getLocation()).thenReturn("config.yml");
        assertThat(parser.accept(context), is(true));

        when(context.getLocation()).thenReturn("config.yaml");
        assertThat(parser.accept(context), is(true));
    }

    @Test
    public void testAcceptWithInvalidFileExtension() {
        when(context.getMediaType()).thenReturn("");
        when(context.getLocation()).thenReturn("config.json");
        assertThat(parser.accept(context), is(false));
    }

    @Test
    public void testAcceptWithNullMediaType() {
        when(context.getMediaType()).thenReturn(null);
        when(context.getLocation()).thenReturn("config.yml");
        assertThat(parser.accept(context), is(true));
    }

    @Test
    public void testAcceptWithEmptyLocation() {
        when(context.getMediaType()).thenReturn("");
        when(context.getLocation()).thenReturn("");
        assertThat(parser.accept(context), is(false));
    }

    @Test(expected = IOException.class)
    public void testParseInvalidYaml() throws IOException {
        String invalidYaml = "invalid:\n  - broken\n    yaml: content";
        InputStream input = new ByteArrayInputStream(invalidYaml.getBytes(StandardCharsets.UTF_8));
        parser.parse(context, input);
    }
}
