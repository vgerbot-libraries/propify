package com.vgerbot.propify.schema;

/**
 * Represents a property definition within a schema.
 *
 * @since 2.1.0
 */
public class PropertyDefinition {
    private String name;
    private String type;  // "string", "integer", "number", "boolean", "array", "object"
    private String format; // "date-time", "email", "uri", etc.
    private String description;
    private boolean required;
    private Object defaultValue;
    
    // Validation constraints
    private String pattern;
    private Integer minLength;
    private Integer maxLength;
    private Number minimum;
    private Number maximum;
    private Boolean exclusiveMinimum;
    private Boolean exclusiveMaximum;
    
    // For arrays
    private PropertyDefinition items;
    private Integer minItems;
    private Integer maxItems;
    
    // For objects (nested types)
    private SchemaDefinition nestedSchema;
    private String refType; // For $ref references

    public PropertyDefinition() {
    }

    public PropertyDefinition(String name, String type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(Object defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public Integer getMinLength() {
        return minLength;
    }

    public void setMinLength(Integer minLength) {
        this.minLength = minLength;
    }

    public Integer getMaxLength() {
        return maxLength;
    }

    public void setMaxLength(Integer maxLength) {
        this.maxLength = maxLength;
    }

    public Number getMinimum() {
        return minimum;
    }

    public void setMinimum(Number minimum) {
        this.minimum = minimum;
    }

    public Number getMaximum() {
        return maximum;
    }

    public void setMaximum(Number maximum) {
        this.maximum = maximum;
    }

    public Boolean getExclusiveMinimum() {
        return exclusiveMinimum;
    }

    public void setExclusiveMinimum(Boolean exclusiveMinimum) {
        this.exclusiveMinimum = exclusiveMinimum;
    }

    public Boolean getExclusiveMaximum() {
        return exclusiveMaximum;
    }

    public void setExclusiveMaximum(Boolean exclusiveMaximum) {
        this.exclusiveMaximum = exclusiveMaximum;
    }

    public PropertyDefinition getItems() {
        return items;
    }

    public void setItems(PropertyDefinition items) {
        this.items = items;
    }

    public Integer getMinItems() {
        return minItems;
    }

    public void setMinItems(Integer minItems) {
        this.minItems = minItems;
    }

    public Integer getMaxItems() {
        return maxItems;
    }

    public void setMaxItems(Integer maxItems) {
        this.maxItems = maxItems;
    }

    public SchemaDefinition getNestedSchema() {
        return nestedSchema;
    }

    public void setNestedSchema(SchemaDefinition nestedSchema) {
        this.nestedSchema = nestedSchema;
    }

    public String getRefType() {
        return refType;
    }

    public void setRefType(String refType) {
        this.refType = refType;
    }

    public boolean hasPattern() {
        return pattern != null && !pattern.isEmpty();
    }

    public boolean isArray() {
        return "array".equals(type);
    }

    public boolean isObject() {
        return "object".equals(type);
    }

    public boolean isString() {
        return "string".equals(type);
    }

    public boolean isInteger() {
        return "integer".equals(type);
    }

    public boolean isNumber() {
        return "number".equals(type);
    }

    public boolean isBoolean() {
        return "boolean".equals(type);
    }
}

