package com.vgerbot.propify.schema;

/**
 * Enum representing the type of schema definition.
 * 
 * @since 2.1.0
 */
public enum SchemaType {
    /**
     * Auto-detect schema type from file extension
     */
    AUTO,
    
    /**
     * JSON Schema (draft-07, 2019-09, 2020-12)
     */
    JSON_SCHEMA,
    
    /**
     * OpenAPI 3.x specification
     */
    OPENAPI,
    
    /**
     * XML Schema Definition (XSD)
     */
    XML_SCHEMA
}
