package com.vgerbot.propify.core;

/**
 * Provider interface for obtaining resource loaders based on location strings.
 * 
 * <p>This interface defines the contract for providers that supply appropriate
 * {@link ResourceLoader} instances based on resource locations. The framework
 * includes two main implementations:
 * <ul>
 *   <li>CompileTimeResourceLoaderProvider - For annotation processing</li>
 *   <li>RuntimeResourceLoaderProvider - For runtime resource loading</li>
 * </ul>
 *
 * <p>The provider examines the location string (typically a URI) to determine
 * which loader is appropriate. For example:
 * <ul>
 *   <li>classpath: → ClasspathResourceLoader</li>
 *   <li>file: → FileResourceLoader</li>
 *   <li>http:, https: → URLResourceLoader</li>
 * </ul>
 *
 * @see ResourceLoader The interface implemented by resource loaders
 * @see PropifyContext Uses providers to obtain appropriate loaders
 * @since 1.1.0
 */
public interface ResourceLoaderProvider {

    /**
     * Gets an appropriate resource loader for the given location.
     * 
     * <p>This method analyzes the location string to determine which type of
     * loader should be used. The decision is typically based on:
     * <ul>
     *   <li>The URI scheme or protocol (e.g., classpath:, file:, http:, https:)</li>
     *   <li>The path format and structure</li>
     *   <li>The current execution context (compile-time vs runtime)</li>
     * </ul>
     *
     * @param location the location string identifying the resource
     * @return a ResourceLoader capable of loading resources from the given location
     * @throws IllegalArgumentException if the location format is invalid
     * @throws UnsupportedOperationException if no loader is available for the location
     */
    ResourceLoader getLoader(String location);
}
