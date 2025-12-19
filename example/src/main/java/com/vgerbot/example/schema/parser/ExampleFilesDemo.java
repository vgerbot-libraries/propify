package com.vgerbot.example.schema.parser;

import com.vgerbot.propify.schema.PropertyDefinition;
import com.vgerbot.propify.schema.SchemaContext;
import com.vgerbot.propify.schema.SchemaDefinition;
import com.vgerbot.propify.schema.SchemaType;
import com.vgerbot.propify.schema.parser.JsonSchemaParser;
import com.vgerbot.propify.schema.parser.OpenApiSchemaParser;

import org.apache.commons.lang3.StringUtils;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

/**
 * Demo to test enum support using actual example files.
 */
public class ExampleFilesDemo {

    public static void main(String[] args) {
        System.out.println("=== Example Files Enum Demo ===\n");

        String projectRoot = findProjectRoot();
        System.out.println("Project root: " + projectRoot + "\n");

        testUserSchema(projectRoot);
        System.out.println();
        testPetStoreSchema(projectRoot);
    }

    private static String findProjectRoot() {
        // Try to find the project root directory
        String currentDir = System.getProperty("user.dir");
        // If we're in propify/propify, go up one level
        if (currentDir.endsWith("/propify/propify")) {
            return currentDir.substring(0, currentDir.lastIndexOf("/propify"));
        }
        // If we're in propify, use it as is
        return currentDir;
    }

    private static void testUserSchema(String projectRoot) {
        System.out.println("1. Testing user.schema.json:");
        System.out.println(StringUtils.repeat('-', 60));

        String schemaPath = projectRoot + "/example/src/main/resources/schemas/user.schema.json";

        try {
            JsonSchemaParser parser = new JsonSchemaParser();
            InputStream inputStream = new FileInputStream(schemaPath);
            SchemaDefinition schema = parser.parse(null, inputStream);

            System.out.println("Schema: " + schema.getTitle());
            System.out.println("Properties with enum:");
            System.out.println();

            // Check all properties for enums
            for (Map.Entry<String, PropertyDefinition> entry : schema.getProperties().entrySet()) {
                PropertyDefinition prop = entry.getValue();
                if (prop.hasEnumValues()) {
                    printEnumProperty(prop);
                }
            }

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void testPetStoreSchema(String projectRoot) {
        System.out.println("\n2. Testing petstore.yaml:");
        System.out.println(StringUtils.repeat('-', 60));

        String schemaPath = projectRoot + "/example/src/main/resources/schemas/petstore.yaml";

        try {
            OpenApiSchemaParser parser = new OpenApiSchemaParser();
            InputStream inputStream = new FileInputStream(schemaPath);

            SchemaContext context = new SchemaContext(
                "petstore.yaml",
                SchemaType.OPENAPI,
                "Pet",
                null,
                false, false, false, false, false, false,
                null, null
            );

            SchemaDefinition schema = parser.parse(context, inputStream);

            System.out.println("Schema: " + schema.getName());
            System.out.println("Properties with enum:");
            System.out.println();

            // Check all properties for enums
            for (Map.Entry<String, PropertyDefinition> entry : schema.getProperties().entrySet()) {
                PropertyDefinition prop = entry.getValue();
                if (prop.hasEnumValues()) {
                    printEnumProperty(prop);
                }
            }

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void printEnumProperty(PropertyDefinition prop) {
        System.out.println("  Property: " + prop.getName());
        System.out.println("    Type: " + prop.getType());
        if (prop.getDescription() != null) {
            System.out.println("    Description: " + prop.getDescription());
        }

        if (prop.hasEnumValues()) {
            List<Object> enumValues = prop.getEnumValues();
            System.out.println("    Enum Values: " + enumValues);
            System.out.println("    Enum Count: " + enumValues.size());
        }

        if (prop.getDefaultValue() != null) {
            System.out.println("    Default: " + prop.getDefaultValue());
        }

        System.out.println();
    }
}
