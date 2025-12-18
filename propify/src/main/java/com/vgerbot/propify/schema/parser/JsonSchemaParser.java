package com.vgerbot.propify.schema.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vgerbot.propify.common.Utils;
import com.vgerbot.propify.schema.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Parser for JSON Schema definitions.
 * Supports JSON Schema draft-07 and later.
 *
 * @since 2.1.0
 */
public class JsonSchemaParser implements SchemaParser {
    
    private final ObjectMapper objectMapper;

    public JsonSchemaParser() {
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public SchemaDefinition parse(SchemaContext context, InputStream inputStream) throws IOException {
        JsonNode rootNode = objectMapper.readTree(inputStream);
        
        SchemaDefinition schema = new SchemaDefinition();
        
        // Extract title
        if (rootNode.has("title")) {
            schema.setTitle(rootNode.get("title").asText());
            schema.setName(schema.getTitle());
        }
        
        // Extract description
        if (rootNode.has("description")) {
            schema.setDescription(rootNode.get("description").asText());
        }
        
        // Parse properties
        if (rootNode.has("properties")) {
            JsonNode propertiesNode = rootNode.get("properties");
            parseProperties(propertiesNode, schema);
        }
        
        // Parse required fields
        if (rootNode.has("required")) {
            JsonNode requiredNode = rootNode.get("required");
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

    private void parseProperties(JsonNode propertiesNode, SchemaDefinition schema) {
        Iterator<Map.Entry<String, JsonNode>> fields = propertiesNode.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> field = fields.next();
            String propertyName = field.getKey();
            JsonNode propertyNode = field.getValue();
            
            PropertyDefinition property = parseProperty(propertyName, propertyNode, schema);
            schema.addProperty(propertyName, property);
        }
    }

    private PropertyDefinition parseProperty(String name, JsonNode node, SchemaDefinition parentSchema) {
        PropertyDefinition property = new PropertyDefinition(name, "string");
        
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
            property.setExclusiveMinimum(node.get("exclusiveMinimum").asBoolean());
        }
        if (node.has("exclusiveMaximum")) {
            property.setExclusiveMaximum(node.get("exclusiveMaximum").asBoolean());
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
            PropertyDefinition items = parseProperty(name + "Item", itemsNode, parentSchema);
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
            
            parseProperties(node.get("properties"), nestedSchema);
            
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
        
        // $ref reference
        if (node.has("$ref")) {
            String ref = node.get("$ref").asText();
            property.setRefType(extractRefTypeName(ref));
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

    private String extractRefTypeName(String ref) {
        // Extract type name from reference like "#/definitions/User" -> "User"
        int lastSlash = ref.lastIndexOf('/');
        if (lastSlash >= 0 && lastSlash < ref.length() - 1) {
            return ref.substring(lastSlash + 1);
        }
        return ref;
    }

    @Override
    public boolean supports(SchemaType type) {
        return type == SchemaType.JSON_SCHEMA || type == SchemaType.AUTO;
    }
}

