package com.vgerbot.propify.schema.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.vgerbot.propify.common.Utils;
import com.vgerbot.propify.schema.PropertyDefinition;
import com.vgerbot.propify.schema.SchemaContext;
import com.vgerbot.propify.schema.SchemaDefinition;
import com.vgerbot.propify.schema.SchemaParser;
import com.vgerbot.propify.schema.SchemaType;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Parser for OpenAPI 3.x specifications.
 *
 * @since 2.1.0
 */
public class OpenApiSchemaParser implements SchemaParser {

    private final ObjectMapper jsonMapper;
    private final ObjectMapper yamlMapper;

    public OpenApiSchemaParser() {
        this.jsonMapper = new ObjectMapper();
        this.yamlMapper = new ObjectMapper(new YAMLFactory());
    }

    @Override
    public SchemaDefinition parse(SchemaContext context, InputStream inputStream) throws IOException {
        // Determine if it's YAML or JSON based on location
        ObjectMapper mapper = context.getLocation().endsWith(".yaml") || context.getLocation().endsWith(".yml")
                ? yamlMapper : jsonMapper;

        JsonNode rootNode = mapper.readTree(inputStream);

        // Validate it's an OpenAPI document
        if (!rootNode.has("openapi") && !rootNode.has("swagger")) {
            throw new IllegalArgumentException("Not a valid OpenAPI specification");
        }

        // Get schemas from components/schemas (OpenAPI 3.x)
        JsonNode schemasNode = null;
        if (rootNode.has("components") && rootNode.get("components").has("schemas")) {
            schemasNode = rootNode.get("components").get("schemas");
        }

        if (schemasNode == null) {
            throw new IllegalArgumentException("No schemas found in OpenAPI specification");
        }

        String schemaRef = context.getSchemaRef();

        if (schemaRef != null && !schemaRef.isEmpty()) {
            // Parse specific schema
            String schemaName = extractSchemaName(schemaRef);
            if (!schemasNode.has(schemaName)) {
                throw new IllegalArgumentException("Schema '" + schemaName + "' not found in OpenAPI specification");
            }
            return parseSchema(schemaName, schemasNode.get(schemaName), schemasNode);
        } else {
            // If no specific schema ref, parse the first one (or throw error)
            Iterator<Map.Entry<String, JsonNode>> fields = schemasNode.fields();
            if (fields.hasNext()) {
                Map.Entry<String, JsonNode> first = fields.next();
                return parseSchema(first.getKey(), first.getValue(), schemasNode);
            }
            throw new IllegalArgumentException("No schemas found in OpenAPI specification");
        }
    }

    private String extractSchemaName(String ref) {
        // Handle "#/components/schemas/Pet" or just "Pet"
        if (ref.contains("/")) {
            int lastSlash = ref.lastIndexOf('/');
            return ref.substring(lastSlash + 1);
        }
        return ref;
    }

    private SchemaDefinition parseSchema(String name, JsonNode schemaNode, JsonNode allSchemasNode) {
        SchemaDefinition schema = new SchemaDefinition(name);

        // Title
        if (schemaNode.has("title")) {
            schema.setTitle(schemaNode.get("title").asText());
        } else {
            schema.setTitle(name);
        }

        // Description
        if (schemaNode.has("description")) {
            schema.setDescription(schemaNode.get("description").asText());
        }

        // Properties
        if (schemaNode.has("properties")) {
            JsonNode propertiesNode = schemaNode.get("properties");
            parseProperties(propertiesNode, schema, allSchemasNode);
        }

        // Required fields
        if (schemaNode.has("required")) {
            JsonNode requiredNode = schemaNode.get("required");
            if (requiredNode.isArray()) {
                for (JsonNode reqField : requiredNode) {
                    schema.addRequired(reqField.asText());
                }
            }
        }

        // Update required flag on properties
        for (Map.Entry<String, PropertyDefinition> entry : schema.getProperties().entrySet()) {
            entry.getValue().setRequired(schema.isRequired(entry.getKey()));
        }

        return schema;
    }

    private void parseProperties(JsonNode propertiesNode, SchemaDefinition schema, JsonNode allSchemasNode) {
        Iterator<Map.Entry<String, JsonNode>> fields = propertiesNode.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> field = fields.next();
            String propertyName = field.getKey();
            JsonNode propertyNode = field.getValue();

            PropertyDefinition property = parseProperty(propertyName, propertyNode, schema, allSchemasNode);
            schema.addProperty(propertyName, property);
        }
    }

    private PropertyDefinition parseProperty(String name, JsonNode node, SchemaDefinition parentSchema, JsonNode allSchemasNode) {
        PropertyDefinition property = new PropertyDefinition(name, "string");

        // Handle $ref
        if (node.has("$ref")) {
            String ref = node.get("$ref").asText();
            property.setRefType(extractSchemaName(ref));
            property.setType("object");
            return property;
        }

        // Type
        if (node.has("type")) {
            property.setType(node.get("type").asText());
        }

        // Format
        if (node.has("format")) {
            property.setFormat(node.get("format").asText());
        }

        // Description
        if (node.has("description")) {
            property.setDescription(node.get("description").asText());
        }

        // Default value
        if (node.has("default")) {
            property.setDefaultValue(extractValue(node.get("default")));
        }

        // String constraints
        if (node.has("pattern")) {
            property.setPattern(node.get("pattern").asText());
        }
        if (node.has("minLength")) {
            property.setMinLength(node.get("minLength").asInt());
        }
        if (node.has("maxLength")) {
            property.setMaxLength(node.get("maxLength").asInt());
        }

        // Numeric constraints
        if (node.has("minimum")) {
            property.setMinimum(node.get("minimum").numberValue());
        }
        if (node.has("maximum")) {
            property.setMaximum(node.get("maximum").numberValue());
        }
        if (node.has("exclusiveMinimum")) {
            JsonNode exMin = node.get("exclusiveMinimum");
            if (exMin.isBoolean()) {
                property.setExclusiveMinimum(exMin.asBoolean());
            }
        }
        if (node.has("exclusiveMaximum")) {
            JsonNode exMax = node.get("exclusiveMaximum");
            if (exMax.isBoolean()) {
                property.setExclusiveMaximum(exMax.asBoolean());
            }
        }

        // Enum values
        if (node.has("enum")) {
            JsonNode enumNode = node.get("enum");
            if (enumNode.isArray()) {
                List<Object> enumValues = new ArrayList<>();
                for (JsonNode enumValue : enumNode) {
                    enumValues.add(extractValue(enumValue));
                }
                property.setEnumValues(enumValues);
            }
        }

        // Array items
        if (property.isArray() && node.has("items")) {
            JsonNode itemsNode = node.get("items");
            PropertyDefinition items = parseProperty(name + "Item", itemsNode, parentSchema, allSchemasNode);
            property.setItems(items);

            if (node.has("minItems")) {
                property.setMinItems(node.get("minItems").asInt());
            }
            if (node.has("maxItems")) {
                property.setMaxItems(node.get("maxItems").asInt());
            }
        }

        // Object (nested schema)
        if (property.isObject() && node.has("properties")) {
            String nestedName = Utils.convertToClassName(name);
            SchemaDefinition nestedSchema = new SchemaDefinition(nestedName);

            if (node.has("description")) {
                nestedSchema.setDescription(node.get("description").asText());
            }

            parseProperties(node.get("properties"), nestedSchema, allSchemasNode);

            if (node.has("required")) {
                JsonNode requiredNode = node.get("required");
                if (requiredNode.isArray()) {
                    for (JsonNode reqField : requiredNode) {
                        nestedSchema.addRequired(reqField.asText());
                    }
                }
            }

            // Update required flag on nested properties
            for (Map.Entry<String, PropertyDefinition> entry : nestedSchema.getProperties().entrySet()) {
                entry.getValue().setRequired(nestedSchema.isRequired(entry.getKey()));
            }

            property.setNestedSchema(nestedSchema);
            parentSchema.addNestedSchema(nestedName, nestedSchema);
        }

        return property;
    }

    private Object extractValue(JsonNode node) {
        if (node.isNull()) {
            return null;
        } else if (node.isBoolean()) {
            return node.asBoolean();
        } else if (node.isInt()) {
            return node.asInt();
        } else if (node.isLong()) {
            return node.asLong();
        } else if (node.isDouble()) {
            return node.asDouble();
        } else if (node.isTextual()) {
            return node.asText();
        }
        return node.toString();
    }

    @Override
    public boolean supports(SchemaType type) {
        return type == SchemaType.OPENAPI;
    }
}

