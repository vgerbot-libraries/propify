package com.vgerbot.propify.generator;

import com.vgerbot.propify.core.PropifyContext;
import com.vgerbot.propify.core.PropifyProperties;
import com.vgerbot.propify.loader.RuntimeResourceLoaderProvider;
import com.vgerbot.propify.logger.RuntimeLogger;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class PropifyCodeGeneratorTest {

    @Test
    public void testGenerateCodeWithSimpleProperties() {
        // Setup
        PropifyContext context = createContext();
        PropifyProperties properties = new PropifyProperties();
        properties.put("stringProperty", "value");
        properties.put("intProperty", 42);
        properties.put("booleanProperty", true);

        // Execute
        String code = PropifyCodeGenerator.getInstance().generateCode(
                "com.example", "TestConfig", context, properties);

        // Verify
        assertNotNull(code);
        assertTrue(code.contains("package com.example;"));
        assertTrue(code.contains("public final class TestConfig"));
        assertTrue(code.contains("public final String getStringProperty()"));
        assertTrue(code.contains("public final Integer getIntProperty()"));
        assertTrue(code.contains("public final Boolean isBooleanProperty()"));
    }

    @Test
    public void testGenerateCodeWithNestedProperties() {
        // Setup
        PropifyContext context = createContext();
        PropifyProperties properties = new PropifyProperties();
        PropifyProperties nested = properties.createNested("database");
        nested.put("url", "jdbc:mysql://localhost:3306/mydb");
        nested.put("username", "admin");
        nested.put("password", "password");

        // Execute
        String code = PropifyCodeGenerator.getInstance().generateCode(
                "com.example", "TestConfig", context, properties);

        // Verify
        assertNotNull(code);
        assertTrue(code.contains("public static final class Database"));
        assertTrue(code.contains("public final Database getDatabase()"));
        assertTrue(code.contains("public final String getUrl()"));
        assertTrue(code.contains("public final String getUsername()"));
        assertTrue(code.contains("public final String getPassword()"));
        assertTrue(code.contains("instance = new Database((PropifyProperties) properties.get(\"database\"))"));
    }

    @Test
    public void testGenerateCodeWithListOfPrimitives() {
        // Setup
        PropifyContext context = createContext();
        PropifyProperties properties = new PropifyProperties();
        properties.put("numbers", Arrays.asList(1, 2, 3, 4, 5));
        properties.put("names", Arrays.asList("Alice", "Bob", "Charlie"));

        // Execute
        String code = PropifyCodeGenerator.getInstance().generateCode(
                "com.example", "TestConfig", context, properties);

        // Verify
        assertNotNull(code);
        assertTrue(code.contains("public final List<Integer> getNumbers()"));
        assertTrue(code.contains("public final List<String> getNames()"));
    }

    @Test
    public void testGenerateCodeWithListOfObjects() {
        // Setup
        PropifyContext context = createContext();
        PropifyProperties properties = new PropifyProperties();
        
        PropifyProperties user1 = new PropifyProperties();
        user1.put("id", 1);
        user1.put("name", "User1");
        
        PropifyProperties user2 = new PropifyProperties();
        user2.put("id", 2);
        user2.put("name", "User2");
        
        properties.put("users", Arrays.asList(user1, user2));

        // Execute
        String code = PropifyCodeGenerator.getInstance().generateCode(
                "com.example", "TestConfig", context, properties);

        // Verify
        assertNotNull(code);
        assertTrue(code.contains("public static final class UsersItem"));
        assertTrue(code.contains("public final List<UsersItem> getUsers()"));
        assertTrue(code.contains("public final Integer getId()"));
        assertTrue(code.contains("public final String getName()"));
    }

    @Test
    public void testGenerateCodeWithMapProperty() {
        // Setup
        PropifyContext context = createContext();
        PropifyProperties properties = new PropifyProperties();
        Map<String, Object> map = new HashMap<>();
        map.put("key1", "value1");
        map.put("key2", "value2");
        properties.put("settings", map);

        // Execute
        String code = PropifyCodeGenerator.getInstance().generateCode(
                "com.example", "TestConfig", context, properties);

        // Verify
        assertNotNull(code);
        assertTrue(code.contains("public final Map<String, String> getSettings()"));
    }

    @Test
    public void testGenerateMethodNameConvention() {
        // Setup
        PropifyContext context = createContext();
        PropifyProperties properties = new PropifyProperties();
        properties.put("camelCaseProperty", "value");
        properties.put("snake_case_property", "value");
        properties.put("kebab-case-property", "value");
        properties.put("enabled", true);

        // Execute
        String code = PropifyCodeGenerator.getInstance().generateCode(
                "com.example", "TestConfig", context, properties);

        // Verify
        assertNotNull(code);
        assertTrue(code.contains("public final String getCamelCaseProperty()"));
        assertTrue(code.contains("public final String getSnakeCaseProperty()"));
        assertTrue(code.contains("public final String getKebabCaseProperty()"));
        assertTrue(code.contains("public final Boolean isEnabled()"));
    }

    @Test
    public void testGeneratedStaticInstanceMethod() {
        // Setup
        PropifyContext context = createContext();
        PropifyProperties properties = new PropifyProperties();

        // Execute
        String code = PropifyCodeGenerator.getInstance().generateCode(
                "com.example", "TestConfig", context, properties);

        // Verify
        assertNotNull(code);
        assertTrue(code.contains("public static final TestConfig getInstance()"));
        assertTrue(code.contains("PropifyContext context = new PropifyContext("));
        assertTrue(code.contains("PropifyPropertiesBuilder propifyPropertiesBuilder = new PropifyPropertiesBuilder()"));
        assertTrue(code.contains("return new TestConfig(propifyPropertiesBuilder.build(context))"));
    }

    @Test
    public void testGeneratedFileComment() {
        // Setup
        PropifyContext context = createContext();
        PropifyProperties properties = new PropifyProperties();

        // Execute
        String code = PropifyCodeGenerator.getInstance().generateCode(
                "com.example", "TestConfig", context, properties);

        // Verify
        assertNotNull(code);
        assertTrue(code.contains("Generated code - do not modify"));
    }

    @Test
    public void testEmptyProperties() {
        // Setup
        PropifyContext context = createContext();
        PropifyProperties properties = new PropifyProperties();

        // Execute
        String code = PropifyCodeGenerator.getInstance().generateCode(
                "com.example", "TestConfig", context, properties);

        // Verify
        assertNotNull(code);
        assertTrue(code.contains("public final class TestConfig"));
        assertTrue(code.contains("private final PropifyProperties properties"));
    }

    private PropifyContext createContext() {
        return new PropifyContext(
                "classpath:application.properties",
                "properties",
                "TestConfig",
                ',',
                new String[]{},
                RuntimeResourceLoaderProvider.getInstance(),
                new RuntimeLogger()
        );
    }
} 