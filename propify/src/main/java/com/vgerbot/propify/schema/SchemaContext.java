package com.vgerbot.propify.schema;

import com.vgerbot.propify.core.ResourceLoader;
import com.vgerbot.propify.core.ResourceLoaderProvider;
import com.vgerbot.propify.logger.Logger;

import java.io.IOException;
import java.io.InputStream;

/**
 * Context object that holds configuration for schema-based code generation.
 *
 * @since 2.1.0
 */
public class SchemaContext {
    private final String location;
    private final SchemaType type;
    private final String schemaRef;
    private final String generatedClassName;
    private final boolean builder;
    private final boolean jacksonAnnotations;
    private final boolean jaxbAnnotations;
    private final boolean validationAnnotations;
    private final boolean serializable;
    private final boolean generateHelperMethods;
    private final ResourceLoaderProvider resourceLoaderProvider;
    private final Logger logger;

    public SchemaContext(
            String location,
            SchemaType type,
            String schemaRef,
            String generatedClassName,
            boolean builder,
            boolean jacksonAnnotations,
            boolean jaxbAnnotations,
            boolean validationAnnotations,
            boolean serializable,
            boolean generateHelperMethods,
            ResourceLoaderProvider resourceLoaderProvider,
            Logger logger
    ) {
        this.location = location;
        this.type = type;
        this.schemaRef = schemaRef;
        this.generatedClassName = generatedClassName;
        this.builder = builder;
        this.jacksonAnnotations = jacksonAnnotations;
        this.jaxbAnnotations = jaxbAnnotations;
        this.validationAnnotations = validationAnnotations;
        this.serializable = serializable;
        this.generateHelperMethods = generateHelperMethods;
        this.resourceLoaderProvider = resourceLoaderProvider;
        this.logger = logger;
    }

    public InputStream loadResource() throws IOException {
        ResourceLoader loader = resourceLoaderProvider.getLoader(location);
        return loader.load(location);
    }

    public String getLocation() {
        return location;
    }

    public SchemaType getType() {
        return type;
    }

    public String getSchemaRef() {
        return schemaRef;
    }

    public String getGeneratedClassName() {
        return generatedClassName;
    }

    public boolean isBuilder() {
        return builder;
    }

    public boolean isJacksonAnnotations() {
        return jacksonAnnotations;
    }

    public boolean isJaxbAnnotations() {
        return jaxbAnnotations;
    }

    public boolean isValidationAnnotations() {
        return validationAnnotations;
    }

    public boolean isSerializable() {
        return serializable;
    }

    public boolean isGenerateHelperMethods() {
        return generateHelperMethods;
    }

    public Logger getLogger() {
        return logger;
    }
}

