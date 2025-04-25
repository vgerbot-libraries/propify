package com.vgerbot.propify.core;

import com.vgerbot.propify.PropifyProcessor;
import com.vgerbot.propify.logger.Logger;
import com.vgerbot.propify.lookup.PropifyLookup;
import com.vgerbot.propify.lookup.PropifyLookupAdaptor;
import org.apache.commons.configuration2.interpol.Lookup;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

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
public class PropifyContext {

    private final String location;
    private final String mediaType;
    private final String generatedClassName;
    private final ResourceLoaderProvider resourceLoaderProvider;
    private final char listDelimiter;
    private final Logger logger;
    private final String[] lookups;

    /**
     * Creates a new PropifyContext with the specified configuration.
     *
     * @param location the location of the configuration resource
     * @param mediaType the media type of the configuration resource
     * @param generatedClassName pattern for generating the configuration class name
     * @param listDelimiter the list delimiter character
     * @param lookups the list of lookup classes
     * @param resourceLoaderProvider provider for resource loading capabilities
     * @param logger logger for processing messages and diagnostics
     */
    public PropifyContext(
            String location,
            String mediaType,
            String generatedClassName,
            char listDelimiter,
            String[] lookups,
            ResourceLoaderProvider resourceLoaderProvider,
            Logger logger
    ) {
        this.location = location;
        this.mediaType = mediaType;
        this.generatedClassName = generatedClassName == null ? "" : generatedClassName;
        this.listDelimiter = listDelimiter;
        this.lookups = lookups;
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

    public char getListDelimiter() {
        return listDelimiter;
    }

    public String[] getLookups() {
        return lookups;
    }

    public Map<String, Lookup> getAllLookups() {
        return Arrays.stream(this.lookups).map(it -> {
            try {
                Class<PropifyLookup> cls = (Class<PropifyLookup>)Class.forName(it);
                Constructor<PropifyLookup> constructor = cls.getDeclaredConstructor();
                constructor.setAccessible(true);
                return constructor.newInstance();
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | NoSuchMethodException |
                     InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }).collect(Collectors.toMap(PropifyLookup::getPrefix, PropifyLookupAdaptor::new));
    }
}
