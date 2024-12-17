package com.vgerbot.propify;

import java.io.IOException;
import java.io.InputStream;

/**
 * Context class that holds configuration and processing information for the Propify annotation processor.
 * 
 * <p>This class encapsulates all the necessary information and utilities needed during
 * the processing of {@link Propify} annotations, including:
 * <ul>
 *   <li>Resource location and media type information</li>
 *   <li>Configuration options for type conversion and class generation</li>
 *   <li>Resource loading capabilities</li>
 *   <li>Logging facilities</li>
 * </ul>
 *
 * <p>The context is created during annotation processing and passed to various
 * components that need access to processing configuration and utilities.
 *
 * @see Propify The annotation that uses this context
 * @see PropifyProcessor The processor that creates this context
 * @since 1.0.0
 */
public final class PropifyContext {

    private final String location;
    private final String mediaType;
    private final boolean autoTypeConversion;
    private final String generatedClassName;
    private final ResourceLoaderProvider resourceLoaderProvider;
    private final Logger logger;

    /**
     * Creates a new PropifyContext with the specified configuration.
     *
     * @param location the location of the configuration resource
     * @param mediaType the media type of the configuration resource
     * @param autoTypeConversion whether automatic type conversion is enabled
     * @param generatedClassName pattern for generating the configuration class name
     * @param resourceLoaderProvider provider for resource loading capabilities
     * @param logger logger for processing messages and diagnostics
     */
    public PropifyContext(
            String location,
            String mediaType,
            boolean autoTypeConversion,
            String generatedClassName,
            ResourceLoaderProvider resourceLoaderProvider,
            Logger logger
    ) {
        this.location = location;
        this.mediaType = mediaType;
        this.autoTypeConversion = autoTypeConversion;
        this.generatedClassName = generatedClassName;
        this.resourceLoaderProvider = resourceLoaderProvider;
        this.logger = logger;
    }

    /**
     * Gets the location of the configuration resource.
     *
     * @return the resource location URI or path
     */
    public String getLocation() {
        return location;
    }

    /**
     * Gets the media type of the configuration resource.
     *
     * @return the media type string, or empty string if auto-detection is enabled
     */
    public String getMediaType() {
        return mediaType;
    }

    /**
     * Gets the pattern for generating the configuration class name.
     *
     * @return the class name generation pattern
     */
    public String getGeneratedClassName() {
        return generatedClassName;
    }

    /**
     * Checks if automatic type conversion is enabled.
     *
     * @return true if automatic type conversion is enabled, false otherwise
     */
    public boolean isAutoTypeConversion() {
        return this.autoTypeConversion;
    }

    /**
     * Gets the appropriate resource loader for the current location.
     * 
     * <p>The loader is determined based on the location protocol (e.g., classpath, file, http).
     *
     * @return a ResourceLoader instance suitable for the current location
     */
    public ResourceLoader getResourceLoader() {
        return this.resourceLoaderProvider.getLoader(this.location);
    }

    /**
     * Loads the configuration resource as an input stream.
     * 
     * <p>This method uses the appropriate resource loader to access the configuration
     * file specified by the location. The caller is responsible for closing the
     * returned stream.
     *
     * @return an input stream for reading the configuration resource
     * @throws IOException if the resource cannot be loaded
     */
    public InputStream loadResource() throws IOException {
        return this.getResourceLoader().load(this.location);
    }

    /**
     * Generates the configuration class name based on the original class name.
     * 
     * <p>This method applies the configured naming pattern to create the name for
     * the generated configuration class. The pattern can include the special
     * placeholder "$$" which will be replaced with the original class name.
     *
     * @param originClassName the name of the class being processed
     * @return the generated class name
     * @throws IllegalArgumentException if originClassName is null or empty
     */
    public String generateClassName(String originClassName) {
        if (originClassName == null || originClassName.trim().isEmpty()) {
            throw new IllegalArgumentException("Origin class name cannot be null or empty");
        }

        if (this.generatedClassName.isEmpty()) {
            return originClassName + "Propify";
        } else {
            return this.generatedClassName.replaceAll("\\$\\$", originClassName);
        }
    }

    /**
     * Gets the logger for processing messages and diagnostics.
     *
     * @return the logger instance
     */
    public Logger getLogger() {
        return logger;
    }
}
