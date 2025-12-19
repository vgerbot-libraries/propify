package com.vgerbot.propify.schema;

import java.io.IOException;
import java.io.InputStream;

/**
 * Interface for parsing schema definitions from various formats.
 *
 * @since 2.1.0
 */
public interface SchemaParser {
    
    /**
     * Parse a schema from an input stream.
     *
     * @param context the schema context
     * @param inputStream the input stream containing the schema
     * @return the parsed schema definition
     * @throws IOException if an error occurs while reading the stream
     */
    SchemaDefinition parse(SchemaContext context, InputStream inputStream) throws IOException;
    
    /**
     * Check if this parser can handle the given schema type.
     *
     * @param type the schema type
     * @return true if this parser supports the type, false otherwise
     */
    boolean supports(SchemaType type);
}

