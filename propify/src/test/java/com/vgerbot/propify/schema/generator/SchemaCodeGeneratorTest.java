package com.vgerbot.propify.schema.generator;

import com.vgerbot.propify.core.ResourceLoaderProvider;
import com.vgerbot.propify.logger.Logger;
import com.vgerbot.propify.schema.PropertyDefinition;
import com.vgerbot.propify.schema.SchemaContext;
import com.vgerbot.propify.schema.SchemaDefinition;
import com.vgerbot.propify.schema.SchemaType;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class SchemaCodeGeneratorTest {

    private SchemaCodeGenerator generator;

    @Mock
    private ResourceLoaderProvider resourceLoaderProvider;

    @Mock
    private Logger logger;

    private SchemaContext context;
    private SchemaDefinition schema;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        generator = SchemaCodeGenerator.getInstance();

        context = new SchemaContext(
                "test.json",
                SchemaType.JSON_SCHEMA,
                "",
                "$$",
                true,
                true,
                false,
                true,
                true,
                true,
                resourceLoaderProvider,
                logger
        );

        schema = new SchemaDefinition("User");
        schema.setTitle("User Schema");
        schema.setDescription("Schema for user entity");
    }

    @Test
    public void testGetInstance() {
        SchemaCodeGenerator instance1 = SchemaCodeGenerator.getInstance();
        SchemaCodeGenerator instance2 = SchemaCodeGenerator.getInstance();

        assertNotNull("Instance should not be null", instance1);
        assertSame("Should return same instance", instance1, instance2);
    }

    @Test
    public void testGenerateCodeSimpleClass() {
        PropertyDefinition username = new PropertyDefinition("username", "string");
        username.setRequired(true);
        schema.addProperty("username", username);
        schema.addRequired("username");

        String code = generator.generateCode("com.example", "User", context, schema);

        assertNotNull("Generated code should not be null", code);
        assertTrue("Should contain package declaration", code.contains("package com.example;"));
        assertTrue("Should contain class declaration", code.contains("public class User"));
        assertTrue("Should contain username field", code.contains("username"));
        assertTrue("Should contain getter", code.contains("getUsername"));
        assertTrue("Should contain setter", code.contains("setUsername"));
    }

    @Test
    public void testGenerateCodeWithSerializable() {
        PropertyDefinition name = new PropertyDefinition("name", "string");
        schema.addProperty("name", name);

        String code = generator.generateCode("com.example", "User", context, schema);

        assertTrue("Should implement Serializable", code.contains("implements Serializable"));
        assertTrue("Should have serialVersionUID", code.contains("serialVersionUID"));
    }

    @Test
    public void testGenerateCodeWithoutSerializable() {
        SchemaContext noSerializableContext = new SchemaContext(
                "test.json", SchemaType.JSON_SCHEMA, "", "$$",
                true, true, false, true, false, true,
                resourceLoaderProvider, logger
        );

        PropertyDefinition name = new PropertyDefinition("name", "string");
        schema.addProperty("name", name);

        String code = generator.generateCode("com.example", "User", noSerializableContext, schema);

        assertFalse("Should not implement Serializable", code.contains("implements Serializable"));
        assertFalse("Should not have serialVersionUID", code.contains("serialVersionUID"));
    }

    @Test
    public void testGenerateCodeWithBuilder() {
        PropertyDefinition name = new PropertyDefinition("name", "string");
        schema.addProperty("name", name);

        String code = generator.generateCode("com.example", "User", context, schema);

        assertTrue("Should contain Builder class", code.contains("public static class Builder"));
        assertTrue("Should contain builder method", code.contains("public static User.Builder builder()"));
        assertTrue("Should contain build method", code.contains("public User build()"));
    }

    @Test
    public void testGenerateCodeWithoutBuilder() {
        SchemaContext noBuilderContext = new SchemaContext(
                "test.json", SchemaType.JSON_SCHEMA, "", "$$",
                false, true, false, true, true, true,
                resourceLoaderProvider, logger
        );

        PropertyDefinition name = new PropertyDefinition("name", "string");
        schema.addProperty("name", name);

        String code = generator.generateCode("com.example", "User", noBuilderContext, schema);

        assertFalse("Should not contain Builder class", code.contains("public static class Builder"));
        assertFalse("Should not contain builder method", code.contains("public static User.Builder builder()"));
    }

    @Test
    public void testGenerateCodeWithHelperMethods() {
        PropertyDefinition name = new PropertyDefinition("name", "string");
        schema.addProperty("name", name);

        String code = generator.generateCode("com.example", "User", context, schema);

        assertTrue("Should contain equals method", code.contains("public boolean equals("));
        assertTrue("Should contain hashCode method", code.contains("public int hashCode()"));
        assertTrue("Should contain toString method", code.contains("public String toString()"));
    }

    @Test
    public void testGenerateCodeWithoutHelperMethods() {
        SchemaContext noHelperContext = new SchemaContext(
                "test.json", SchemaType.JSON_SCHEMA, "", "$$",
                true, true, false, true, true, false,
                resourceLoaderProvider, logger
        );

        PropertyDefinition name = new PropertyDefinition("name", "string");
        schema.addProperty("name", name);

        String code = generator.generateCode("com.example", "User", noHelperContext, schema);

        assertFalse("Should not contain equals method", code.contains("public boolean equals("));
        assertFalse("Should not contain hashCode method", code.contains("public int hashCode()"));
        assertFalse("Should not contain toString method", code.contains("public String toString()"));
    }

    @Test
    public void testGenerateCodeWithJacksonAnnotations() {
        PropertyDefinition email = new PropertyDefinition("email", "string");
        email.setFormat("email");
        schema.addProperty("email", email);

        String code = generator.generateCode("com.example", "User", context, schema);

        assertTrue("Should contain JsonProperty annotation", code.contains("@JsonProperty"));
    }

    @Test
    public void testGenerateCodeWithValidationAnnotations() {
        PropertyDefinition username = new PropertyDefinition("username", "string");
        username.setRequired(true);
        username.setMinLength(3);
        username.setMaxLength(20);
        schema.addProperty("username", username);
        schema.addRequired("username");

        String code = generator.generateCode("com.example", "User", context, schema);

        assertTrue("Should contain NotNull annotation", code.contains("@NotNull"));
        assertTrue("Should contain Size annotation", code.contains("@Size"));
    }

    @Test
    public void testGenerateCodeWithEnumProperty() {
        PropertyDefinition status = new PropertyDefinition("status", "string");
        status.setEnumValues(Arrays.asList("ACTIVE", "INACTIVE", "PENDING"));
        schema.addProperty("status", status);

        String code = generator.generateCode("com.example", "User", context, schema);

        assertTrue("Should contain enum Status", code.contains("enum Status"));
        assertTrue("Should contain enum constants", code.contains("ACTIVE") && code.contains("INACTIVE") && code.contains("PENDING"));
    }

    @Test
    public void testGenerateCodeWithArrayProperty() {
        PropertyDefinition tags = new PropertyDefinition("tags", "array");
        PropertyDefinition tagItem = new PropertyDefinition("tagItem", "string");
        tags.setItems(tagItem);
        schema.addProperty("tags", tags);

        String code = generator.generateCode("com.example", "User", context, schema);

        assertTrue("Should contain List type", code.contains("List<String>"));
        assertTrue("Should have tags field", code.contains("tags"));
    }

    @Test
    public void testGenerateCodeWithNestedObject() {
        PropertyDefinition address = new PropertyDefinition("address", "object");
        SchemaDefinition addressSchema = new SchemaDefinition("Address");
        PropertyDefinition street = new PropertyDefinition("street", "string");
        addressSchema.addProperty("street", street);
        address.setNestedSchema(addressSchema);
        schema.addProperty("address", address);
        schema.addNestedSchema("Address", addressSchema);

        String code = generator.generateCode("com.example", "User", context, schema);

        assertTrue("Should contain nested Address class", code.contains("public static class Address"));
        assertTrue("Should contain street field in nested class", code.contains("street"));
    }

    @Test
    public void testGenerateCodeWithIntegerProperty() {
        PropertyDefinition age = new PropertyDefinition("age", "integer");
        schema.addProperty("age", age);

        String code = generator.generateCode("com.example", "User", context, schema);

        assertTrue("Should contain Integer type", code.contains("Integer"));
        assertTrue("Should have age field", code.contains("age"));
    }

    @Test
    public void testGenerateCodeWithNumberProperty() {
        PropertyDefinition score = new PropertyDefinition("score", "number");
        schema.addProperty("score", score);

        String code = generator.generateCode("com.example", "User", context, schema);

        assertTrue("Should contain Double type", code.contains("Double"));
        assertTrue("Should have score field", code.contains("score"));
    }

    @Test
    public void testGenerateCodeWithBooleanProperty() {
        PropertyDefinition active = new PropertyDefinition("active", "boolean");
        schema.addProperty("active", active);

        String code = generator.generateCode("com.example", "User", context, schema);

        assertTrue("Should contain Boolean type", code.contains("Boolean"));
        assertTrue("Should have active field", code.contains("active"));
        assertTrue("Should have isActive getter", code.contains("isActive"));
    }

    @Test
    public void testGenerateCodeWithDateTimeProperty() {
        PropertyDefinition createdAt = new PropertyDefinition("createdAt", "string");
        createdAt.setFormat("date-time");
        schema.addProperty("createdAt", createdAt);

        String code = generator.generateCode("com.example", "User", context, schema);

        assertTrue("Should contain LocalDateTime type", code.contains("LocalDateTime"));
    }

    @Test
    public void testGenerateCodeWithDateProperty() {
        PropertyDefinition birthDate = new PropertyDefinition("birthDate", "string");
        birthDate.setFormat("date");
        schema.addProperty("birthDate", birthDate);

        String code = generator.generateCode("com.example", "User", context, schema);

        assertTrue("Should contain LocalDate type", code.contains("LocalDate"));
    }

    @Test
    public void testGenerateCodeWithTimeProperty() {
        PropertyDefinition startTime = new PropertyDefinition("startTime", "string");
        startTime.setFormat("time");
        schema.addProperty("startTime", startTime);

        String code = generator.generateCode("com.example", "User", context, schema);

        assertTrue("Should contain LocalTime type", code.contains("LocalTime"));
    }

    @Test
    public void testGenerateCodeWithLongFormat() {
        PropertyDefinition id = new PropertyDefinition("id", "integer");
        id.setFormat("int64");
        schema.addProperty("id", id);

        String code = generator.generateCode("com.example", "User", context, schema);

        assertTrue("Should contain Long type", code.contains("Long"));
    }

    @Test
    public void testGenerateCodeWithFloatFormat() {
        PropertyDefinition price = new PropertyDefinition("price", "number");
        price.setFormat("float");
        schema.addProperty("price", price);

        String code = generator.generateCode("com.example", "User", context, schema);

        assertTrue("Should contain Float type", code.contains("Float"));
    }

    @Test
    public void testGenerateCodeWithRefType() {
        PropertyDefinition author = new PropertyDefinition("author", "object");
        author.setRefType("Author");
        schema.addProperty("author", author);

        String code = generator.generateCode("com.example", "User", context, schema);

        assertTrue("Should contain Author type", code.contains("Author"));
        assertTrue("Should have author field", code.contains("author"));
    }

    @Test
    public void testGenerateCodeWithDescription() {
        PropertyDefinition name = new PropertyDefinition("name", "string");
        name.setDescription("User's full name");
        schema.addProperty("name", name);

        String code = generator.generateCode("com.example", "User", context, schema);

        assertTrue("Should contain property description in javadoc", code.contains("User's full name"));
    }

    @Test
    public void testGenerateCodeWithSchemaDescription() {
        schema.setDescription("This is a user entity schema");
        PropertyDefinition name = new PropertyDefinition("name", "string");
        schema.addProperty("name", name);

        String code = generator.generateCode("com.example", "User", context, schema);

        assertTrue("Should contain schema description in javadoc", code.contains("This is a user entity schema"));
    }

    @Test
    public void testGenerateCodeWithEmptySchema() {
        String code = generator.generateCode("com.example", "Empty", context, schema);

        assertNotNull("Code should not be null", code);
        assertTrue("Should contain class declaration", code.contains("public class Empty"));
        assertTrue("Should contain default constructor", code.contains("public Empty()"));
    }

    @Test
    public void testGenerateCodeWithPatternValidation() {
        PropertyDefinition email = new PropertyDefinition("email", "string");
        email.setPattern("^[A-Za-z0-9+_.-]+@(.+)$");
        schema.addProperty("email", email);

        String code = generator.generateCode("com.example", "User", context, schema);

        assertTrue("Should contain Pattern annotation", code.contains("@Pattern"));
    }

    @Test
    public void testGenerateCodeWithMinMaxValidation() {
        PropertyDefinition age = new PropertyDefinition("age", "integer");
        age.setMinimum(0);
        age.setMaximum(150);
        schema.addProperty("age", age);

        String code = generator.generateCode("com.example", "User", context, schema);

        assertTrue("Should contain Min annotation", code.contains("@Min"));
        assertTrue("Should contain Max annotation", code.contains("@Max"));
    }

    @Test
    public void testGenerateCodeWithEmailFormat() {
        PropertyDefinition email = new PropertyDefinition("email", "string");
        email.setFormat("email");
        schema.addProperty("email", email);

        String code = generator.generateCode("com.example", "User", context, schema);

        assertTrue("Should contain Email annotation", code.contains("@Email"));
    }

    @Test
    public void testGenerateCodeMultipleProperties() {
        PropertyDefinition username = new PropertyDefinition("username", "string");
        PropertyDefinition age = new PropertyDefinition("age", "integer");
        PropertyDefinition active = new PropertyDefinition("active", "boolean");

        schema.addProperty("username", username);
        schema.addProperty("age", age);
        schema.addProperty("active", active);

        String code = generator.generateCode("com.example", "User", context, schema);

        assertTrue("Should contain username", code.contains("username"));
        assertTrue("Should contain age", code.contains("age"));
        assertTrue("Should contain active", code.contains("active"));
        assertTrue("Should contain getUsername", code.contains("getUsername"));
        assertTrue("Should contain getAge", code.contains("getAge"));
        assertTrue("Should contain isActive", code.contains("isActive"));
        assertTrue("Should contain setUsername", code.contains("setUsername"));
        assertTrue("Should contain setAge", code.contains("setAge"));
        assertTrue("Should contain setActive", code.contains("setActive"));
    }

    @Test
    public void testGenerateCodeWithComplexSchema() {
        // Create a complex schema with multiple property types
        PropertyDefinition id = new PropertyDefinition("id", "integer");
        id.setFormat("int64");
        id.setRequired(true);

        PropertyDefinition username = new PropertyDefinition("username", "string");
        username.setRequired(true);
        username.setMinLength(3);
        username.setMaxLength(20);

        PropertyDefinition email = new PropertyDefinition("email", "string");
        email.setFormat("email");
        email.setRequired(true);

        PropertyDefinition status = new PropertyDefinition("status", "string");
        status.setEnumValues(Arrays.asList("ACTIVE", "INACTIVE"));

        PropertyDefinition tags = new PropertyDefinition("tags", "array");
        PropertyDefinition tagItem = new PropertyDefinition("tagItem", "string");
        tags.setItems(tagItem);

        schema.addProperty("id", id);
        schema.addProperty("username", username);
        schema.addProperty("email", email);
        schema.addProperty("status", status);
        schema.addProperty("tags", tags);

        schema.addRequired("id");
        schema.addRequired("username");
        schema.addRequired("email");

        String code = generator.generateCode("com.example", "User", context, schema);

        assertNotNull("Code should not be null", code);
        assertTrue("Should contain all properties",
                code.contains("id") && code.contains("username") &&
                code.contains("email") && code.contains("status") && code.contains("tags"));
        assertTrue("Should contain enum", code.contains("enum Status"));
        assertTrue("Should contain validation annotations",
                code.contains("@NotNull") && code.contains("@Size") && code.contains("@Email"));
    }

    @Test
    public void testGenerateCodeWithFileComment() {
        PropertyDefinition name = new PropertyDefinition("name", "string");
        schema.addProperty("name", name);

        String code = generator.generateCode("com.example", "User", context, schema);

        assertTrue("Should contain file comment", code.contains("Generated from schema - do not modify"));
    }
}
