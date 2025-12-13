package com.vgerbot.propify.schema;

import java.util.*;

/**
 * Represents a parsed schema definition with properties and metadata.
 *
 * @since 2.1.0
 */
public class SchemaDefinition {
    private String name;
    private String title;
    private String description;
    private Map<String, PropertyDefinition> properties;
    private Set<String> required;
    private Map<String, SchemaDefinition> nestedSchemas;

    public SchemaDefinition() {
        this.properties = new LinkedHashMap<>();
        this.required = new HashSet<>();
        this.nestedSchemas = new LinkedHashMap<>();
    }

    public SchemaDefinition(String name) {
        this();
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Map<String, PropertyDefinition> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, PropertyDefinition> properties) {
        this.properties = properties;
    }

    public void addProperty(String name, PropertyDefinition property) {
        this.properties.put(name, property);
    }

    public Set<String> getRequired() {
        return required;
    }

    public void setRequired(Set<String> required) {
        this.required = required;
    }

    public void addRequired(String propertyName) {
        this.required.add(propertyName);
    }

    public boolean isRequired(String propertyName) {
        return required.contains(propertyName);
    }

    public Map<String, SchemaDefinition> getNestedSchemas() {
        return nestedSchemas;
    }

    public void addNestedSchema(String name, SchemaDefinition schema) {
        this.nestedSchemas.put(name, schema);
    }
}

