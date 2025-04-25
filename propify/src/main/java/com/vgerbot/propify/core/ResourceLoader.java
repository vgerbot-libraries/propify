package com.vgerbot.propify.core;

import java.io.IOException;
import java.io.InputStream;

/**
 * Core interface for loading configuration resources in the Propify framework.
 * 
 * <p>This interface defines the contract for resource loaders that can access
 * configuration files from various locations. It supports:
 * <ul>
 *   <li>Multiple resource protocols (classpath, file, http)</li>
 *   <li>Location-based resource loading</li>
 *   <li>Stream-based resource access</li>
 * </ul>
 *
 * <p>The framework includes several implementations for different resource types:
 * <ul>
 *   <li>Classpath resources (classpath: protocol)</li>
 *   <li>File system resources (file: protocol)</li>
 *   <li>URL-based resources (http:, https: protocols)</li>
 * </ul>
 *
 * <p>Resource locations are specified using URI-style strings:
 * <pre>
 * classpath:config/application.properties
 * file:/path/to/config.yaml
 * http://config-server/app-config.json
 * </pre>
 *
 * @see PropifyContext Uses resource loaders to access configuration files
 * @see ResourceLoaderProvider Provides appropriate loaders for different locations
 * @since 1.1.0
 */
public interface ResourceLoader {

    /**
     * Determines if this loader can handle resources at the specified location.
     * 
     * <p>This method examines the location string to determine if this loader
     * can handle resources at that location. The decision is typically based on:
     * <ul>
     *   <li>The protocol prefix (e.g., "classpath:", "file:")</li>
     *   <li>The path format and structure</li>
     *   <li>The resource type or extension</li>
     * </ul>
     *
     * @param location the location string to check
     * @return true if this loader can handle resources at the specified location,
     *         false otherwise
     */
    boolean accept(String location);

    /**
     * Loads a resource from the specified location as an input stream.
     * 
     * <p>This method:
     * <ul>
     *   <li>Locates the resource using the provided location string</li>
     *   <li>Opens a stream to read the resource content</li>
     *   <li>Handles any access or I/O errors appropriately</li>
     * </ul>
     *
     * <p>The caller is responsible for closing the returned input stream.
     * It's recommended to use try-with-resources:
     * <pre>
     * try (InputStream stream = loader.load(location)) {
     *     // Use the stream
     * }
     * </pre>
     *
     * @param location the location string identifying the resource to load
     * @return an input stream for reading the resource content
     * @throws IOException if the resource cannot be loaded or read
     * @throws IllegalArgumentException if the location string is invalid
     */
    InputStream load(String location) throws IOException;
}
