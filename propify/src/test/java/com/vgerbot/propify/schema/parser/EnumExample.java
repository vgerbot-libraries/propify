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
 * Example demonstrating enum support in schema parsers.
 */
public class EnumExample {

    public static void main(String[] args) {
        System.out.println("=== Enum Support Example ===\n");
        
        try {
            testJsonSchemaEnum();
            System.out.println();
            testOpenApiSchemaEnum();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void testJsonSchemaEnum() throws Exception {
        System.out.println("1. Testing JSON Schema with Enum:");
        System.out.println(StringUtils.repeat('-', 50));
        
        String jsonSchema = "{\n" +
            "  \"title\": \"User\",\n" +
            "  \"type\": \"object\",\n" +
            "  \"properties\": {\n" +
            "    \"status\": {\n" +
            "      \"type\": \"string\",\n" +
            "      \"enum\": [\"active\", \"inactive\", \"suspended\"],\n" +
            "      \"description\": \"User status\"\n" +
            "    },\n" +
            "    \"priority\": {\n" +
            "      \"type\": \"integer\",\n" +
            "      \"enum\": [1, 2, 3, 4, 5],\n" +
            "      \"description\": \"Priority level\"\n" +
            "    },\n" +
            "    \"name\": {\n" +
            "      \"type\": \"string\",\n" +
            "      \"description\": \"User name (no enum)\"\n" +
            "    }\n" +
            "  }\n" +
            "}";

        JsonSchemaParser parser = new JsonSchemaParser();
        InputStream inputStream = new ByteArrayInputStream(jsonSchema.getBytes(StandardCharsets.UTF_8));

        // Note: SchemaContext is not used in this simple example
        SchemaDefinition schema = parser.parse(null, inputStream);

        System.out.println("Schema Title: " + schema.getTitle());
        System.out.println("\nProperties:");
        
        // Print status property with enum
        PropertyDefinition statusProp = schema.getProperties().get("status");
        System.out.println("\n  - status:");
        System.out.println("    Type: " + statusProp.getType());
        System.out.println("    Description: " + statusProp.getDescription());
        System.out.println("    Has Enum: " + statusProp.hasEnumValues());
        if (statusProp.hasEnumValues()) {
            System.out.println("    Enum Values: " + statusProp.getEnumValues());
        }

        // Print priority property with enum
        PropertyDefinition priorityProp = schema.getProperties().get("priority");
        System.out.println("\n  - priority:");
        System.out.println("    Type: " + priorityProp.getType());
        System.out.println("    Description: " + priorityProp.getDescription());
        System.out.println("    Has Enum: " + priorityProp.hasEnumValues());
        if (priorityProp.hasEnumValues()) {
            System.out.println("    Enum Values: " + priorityProp.getEnumValues());
        }

        // Print name property without enum
        PropertyDefinition nameProp = schema.getProperties().get("name");
        System.out.println("\n  - name:");
        System.out.println("    Type: " + nameProp.getType());
        System.out.println("    Description: " + nameProp.getDescription());
        System.out.println("    Has Enum: " + nameProp.hasEnumValues());
    }

    private static void testOpenApiSchemaEnum() throws Exception {
        System.out.println("2. Testing OpenAPI Schema with Enum:");
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
            "          description: Pet status in the store\n" +
            "        category:\n" +
            "          type: string\n" +
            "          enum:\n" +
            "            - dog\n" +
            "            - cat\n" +
            "            - bird\n" +
            "            - fish\n" +
            "          default: dog\n" +
            "          description: Pet category\n" +
            "        name:\n" +
            "          type: string\n" +
            "          description: Pet name (no enum)\n";

        OpenApiSchemaParser parser = new OpenApiSchemaParser();
        InputStream inputStream = new ByteArrayInputStream(openApiSchema.getBytes(StandardCharsets.UTF_8));

        // Create a minimal context for OpenAPI parsing
        SchemaContext context = new SchemaContext(
            "test.yaml",  // location
            SchemaType.OPENAPI,  // type
            "Pet",  // schemaRef
            null,  // generatedClassName
            false,  // builder
            false,  // jacksonAnnotations
            false,  // jaxbAnnotations
            false,  // validationAnnotations
            false,  // serializable
            false,  // generateHelperMethods
            null,  // resourceLoaderProvider
            null   // logger
        );
        
        SchemaDefinition schema = parser.parse(context, inputStream);

        System.out.println("Schema Name: " + schema.getName());
        System.out.println("\nProperties:");
        
        // Print status property with enum
        PropertyDefinition statusProp = schema.getProperties().get("status");
        System.out.println("\n  - status:");
        System.out.println("    Type: " + statusProp.getType());
        System.out.println("    Description: " + statusProp.getDescription());
        System.out.println("    Has Enum: " + statusProp.hasEnumValues());
        if (statusProp.hasEnumValues()) {
            System.out.println("    Enum Values: " + statusProp.getEnumValues());
        }

        // Print category property with enum and default
        PropertyDefinition categoryProp = schema.getProperties().get("category");
        System.out.println("\n  - category:");
        System.out.println("    Type: " + categoryProp.getType());
        System.out.println("    Description: " + categoryProp.getDescription());
        System.out.println("    Has Enum: " + categoryProp.hasEnumValues());
        if (categoryProp.hasEnumValues()) {
            System.out.println("    Enum Values: " + categoryProp.getEnumValues());
        }
        System.out.println("    Default Value: " + categoryProp.getDefaultValue());

        // Print name property without enum
        PropertyDefinition nameProp = schema.getProperties().get("name");
        System.out.println("\n  - name:");
        System.out.println("    Type: " + nameProp.getType());
        System.out.println("    Description: " + nameProp.getDescription());
        System.out.println("    Has Enum: " + nameProp.hasEnumValues());
    }
}
