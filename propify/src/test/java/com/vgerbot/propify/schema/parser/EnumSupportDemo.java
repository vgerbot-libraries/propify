package com.vgerbot.propify.schema.parser;

import com.vgerbot.propify.schema.PropertyDefinition;
import com.vgerbot.propify.schema.SchemaContext;
import com.vgerbot.propify.schema.SchemaDefinition;
import com.vgerbot.propify.schema.SchemaType;
import org.apache.commons.lang3.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Demo to test enum support in schema parsers.
 * Run this class directly to see enum parsing in action.
 */
public class EnumSupportDemo {

    public static void main(String[] args) {
        System.out.println("=== Enum Support Demo ===\n");
        
        testJsonSchemaEnum();
        System.out.println();
        testOpenApiSchemaEnum();
    }

    private static void testJsonSchemaEnum() {
        System.out.println("1. Testing JSON Schema with Enum:");
        System.out.println(StringUtils.repeat('-', 50));
        
        String jsonSchema = "{\n" +
            "  \"title\": \"User\",\n" +
            "  \"type\": \"object\",\n" +
            "  \"properties\": {\n" +
            "    \"status\": {\n" +
            "      \"type\": \"string\",\n" +
            "      \"enum\": [\"active\", \"inactive\", \"suspended\", \"deleted\"],\n" +
            "      \"description\": \"User account status\"\n" +
            "    },\n" +
            "    \"accountType\": {\n" +
            "      \"type\": \"string\",\n" +
            "      \"enum\": [\"free\", \"premium\", \"enterprise\"],\n" +
            "      \"default\": \"free\",\n" +
            "      \"description\": \"User account type\"\n" +
            "    },\n" +
            "    \"priority\": {\n" +
            "      \"type\": \"integer\",\n" +
            "      \"enum\": [1, 2, 3, 4, 5],\n" +
            "      \"description\": \"Priority level\"\n" +
            "    }\n" +
            "  }\n" +
            "}";

        try {
            JsonSchemaParser parser = new JsonSchemaParser();
            InputStream inputStream = new ByteArrayInputStream(jsonSchema.getBytes(StandardCharsets.UTF_8));
            SchemaDefinition schema = parser.parse(null, inputStream);

            System.out.println("Schema: " + schema.getTitle());
            System.out.println();

            // Test status enum (string)
            PropertyDefinition statusProp = schema.getProperties().get("status");
            printEnumProperty(statusProp);

            // Test accountType enum (string with default)
            PropertyDefinition accountTypeProp = schema.getProperties().get("accountType");
            printEnumProperty(accountTypeProp);

            // Test priority enum (integer)
            PropertyDefinition priorityProp = schema.getProperties().get("priority");
            printEnumProperty(priorityProp);

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void testOpenApiSchemaEnum() {
        System.out.println("\n2. Testing OpenAPI Schema with Enum:");
        System.out.println(StringUtils.repeat('-', 50));
        
        String openApiSchema = "openapi: 3.0.0\n" +
            "info:\n" +
            "  title: Pet Store API\n" +
            "  version: 1.0.0\n" +
            "components:\n" +
            "  schemas:\n" +
            "    Pet:\n" +
            "      type: object\n" +
            "      properties:\n" +
            "        status:\n" +
            "          type: string\n" +
            "          enum:\n" +
            "            - available\n" +
            "            - pending\n" +
            "            - sold\n" +
            "          description: Pet status\n" +
            "        category:\n" +
            "          type: string\n" +
            "          enum:\n" +
            "            - dog\n" +
            "            - cat\n" +
            "            - bird\n" +
            "            - fish\n" +
            "          default: dog\n" +
            "          description: Pet category\n";

        try {
            OpenApiSchemaParser parser = new OpenApiSchemaParser();
            InputStream inputStream = new ByteArrayInputStream(openApiSchema.getBytes(StandardCharsets.UTF_8));
            
            // Create a minimal SchemaContext
            SchemaContext context = new SchemaContext(
                "test.yaml",
                SchemaType.OPENAPI,
                "Pet",
                null,
                false, false, false, false, false, false,
                null, null
            );
            
            SchemaDefinition schema = parser.parse(context, inputStream);

            System.out.println("Schema: " + schema.getName());
            System.out.println();

            // Test status enum
            PropertyDefinition statusProp = schema.getProperties().get("status");
            printEnumProperty(statusProp);

            // Test category enum with default
            PropertyDefinition categoryProp = schema.getProperties().get("category");
            printEnumProperty(categoryProp);

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void printEnumProperty(PropertyDefinition prop) {
        if (prop == null) {
            System.out.println("Property not found!");
            return;
        }

        System.out.println("Property: " + prop.getName());
        System.out.println("  Type: " + prop.getType());
        System.out.println("  Description: " + prop.getDescription());
        
        if (prop.hasEnumValues()) {
            List<Object> enumValues = prop.getEnumValues();
            System.out.println("  Enum Values: " + enumValues);
            System.out.println("  Enum Count: " + enumValues.size());
        } else {
            System.out.println("  Enum Values: None");
        }
        
        if (prop.getDefaultValue() != null) {
            System.out.println("  Default: " + prop.getDefaultValue());
        }
        
        System.out.println();
    }
}
