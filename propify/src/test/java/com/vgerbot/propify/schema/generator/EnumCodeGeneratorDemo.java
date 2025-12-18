package com.vgerbot.propify.schema.generator;

import com.vgerbot.propify.schema.PropertyDefinition;
import com.vgerbot.propify.schema.SchemaContext;
import com.vgerbot.propify.schema.SchemaDefinition;
import com.vgerbot.propify.schema.SchemaType;
import com.vgerbot.propify.schema.parser.JsonSchemaParser;
import com.vgerbot.propify.schema.parser.OpenApiSchemaParser;
import org.apache.commons.lang3.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Demo showing enum support in code generation.
 */
public class EnumCodeGeneratorDemo {

    public static void main(String[] args) {
        System.out.println("=== Enum Code Generation Demo ===\n");
        
        try {
            demonstrateJsonSchemaEnum();
            System.out.println("\n" + StringUtils.repeat("=", 80) + "\n");
            demonstrateOpenApiSchemaEnum();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void demonstrateJsonSchemaEnum() throws Exception {
        System.out.println("1. JSON Schema with Enum - Code Generation");
        System.out.println(StringUtils.repeat("-", 80));
        
        String jsonSchema = "{\n" +
            "  \"title\": \"User\",\n" +
            "  \"type\": \"object\",\n" +
            "  \"description\": \"User entity with status and role\",\n" +
            "  \"properties\": {\n" +
            "    \"id\": {\n" +
            "      \"type\": \"integer\",\n" +
            "      \"description\": \"User ID\"\n" +
            "    },\n" +
            "    \"name\": {\n" +
            "      \"type\": \"string\",\n" +
            "      \"description\": \"User name\"\n" +
            "    },\n" +
            "    \"status\": {\n" +
            "      \"type\": \"string\",\n" +
            "      \"enum\": [\"active\", \"inactive\", \"suspended\", \"deleted\"],\n" +
            "      \"description\": \"User account status\"\n" +
            "    },\n" +
            "    \"role\": {\n" +
            "      \"type\": \"string\",\n" +
            "      \"enum\": [\"admin\", \"user\", \"guest\"],\n" +
            "      \"description\": \"User role\"\n" +
            "    },\n" +
            "    \"priority\": {\n" +
            "      \"type\": \"integer\",\n" +
            "      \"enum\": [1, 2, 3, 4, 5],\n" +
            "      \"description\": \"User priority level\"\n" +
            "    }\n" +
            "  },\n" +
            "  \"required\": [\"id\", \"name\", \"status\"]\n" +
            "}";

        JsonSchemaParser parser = new JsonSchemaParser();
        InputStream inputStream = new ByteArrayInputStream(jsonSchema.getBytes(StandardCharsets.UTF_8));
        SchemaDefinition schema = parser.parse(null, inputStream);

        // Create a minimal context for code generation
        SchemaContext context = new SchemaContext(
            "test.json",
            SchemaType.JSON_SCHEMA,
            null,
            "User",
            false,  // builder
            true,   // jacksonAnnotations
            false,  // jaxbAnnotations
            true,   // validationAnnotations
            true,   // serializable
            true,   // generateHelperMethods
            null,
            null
        );

        // Generate code
        SchemaCodeGenerator generator = SchemaCodeGenerator.getInstance();
        String generatedCode = generator.generateCode("com.example.model", "User", context, schema);

        System.out.println("\nGenerated Java Code:");
        System.out.println(StringUtils.repeat("-", 80));
        System.out.println(generatedCode);
    }

    private static void demonstrateOpenApiSchemaEnum() throws Exception {
        System.out.println("2. OpenAPI Schema with Enum - Code Generation");
        System.out.println(StringUtils.repeat("-", 80));
        
        String openApiSchema = "openapi: 3.0.0\n" +
            "info:\n" +
            "  title: Pet Store API\n" +
            "  version: 1.0.0\n" +
            "components:\n" +
            "  schemas:\n" +
            "    Pet:\n" +
            "      type: object\n" +
            "      description: A pet in the store\n" +
            "      properties:\n" +
            "        id:\n" +
            "          type: integer\n" +
            "          format: int64\n" +
            "          description: Pet ID\n" +
            "        name:\n" +
            "          type: string\n" +
            "          description: Pet name\n" +
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
            "            - reptile\n" +
            "          default: dog\n" +
            "          description: Pet category\n" +
            "      required:\n" +
            "        - id\n" +
            "        - name\n" +
            "        - status\n";

        OpenApiSchemaParser parser = new OpenApiSchemaParser();
        InputStream inputStream = new ByteArrayInputStream(openApiSchema.getBytes(StandardCharsets.UTF_8));
        
        SchemaContext context = new SchemaContext(
            "test.yaml",
            SchemaType.OPENAPI,
            "Pet",
            "Pet",
            true,   // builder
            true,   // jacksonAnnotations
            false,  // jaxbAnnotations
            true,   // validationAnnotations
            false,  // serializable
            true,   // generateHelperMethods
            null,
            null
        );

        SchemaDefinition schema = parser.parse(context, inputStream);

        // Generate code
        SchemaCodeGenerator generator = SchemaCodeGenerator.getInstance();
        String generatedCode = generator.generateCode("com.example.petstore", "Pet", context, schema);

        System.out.println("\nGenerated Java Code:");
        System.out.println(StringUtils.repeat("-", 80));
        System.out.println(generatedCode);
    }
}
