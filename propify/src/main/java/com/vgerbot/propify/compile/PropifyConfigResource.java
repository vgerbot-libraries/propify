package com.vgerbot.propify.compile;

import com.vgerbot.propify.PropifyConfigParser;
import com.vgerbot.propify.PropifyProcessor;

import javax.annotation.processing.ProcessingEnvironment;
import java.io.IOException;
import java.io.InputStream;

/**
 * Core interface for loading configuration resources in the Propify framework.
 *
 * <p>This interface defines the contract for resource loaders that can access
 * configuration files from various locations such as the classpath, file system,
 * or remote URLs. Implementations handle the specifics of resource access while
 * providing a consistent API for the framework to load configuration data.
 *
 * <p>Key responsibilities of implementations:
 * <ul>
 *   <li>Determine if they can handle a given resource location
 *   <li>Load resources from supported locations
 *   <li>Handle resource access errors appropriately
 *   <li>Manage resource lifecycle (opening/closing streams)
 * </ul>
 *
 * <p>Resource loaders are discovered through Java's ServiceLoader mechanism.
 * To register a new loader implementation, include its fully qualified class name in:
 * {@code META-INF/services/com.vgerbot.propify.compile.PropifyConfigResource}
 *
 * <p>Example implementation for a custom resource type:
 * <pre>
 * public class CustomResourceLoader implements PropifyConfigResource {
 *     {@literal @}Override
 *     public Boolean accept(String location) {
 *         return location.startsWith("custom://");
 *     }
 *
 *     {@literal @}Override
 *     public InputStream load(ProcessingEnvironment env, String location) 
 *             throws IOException {
 *         // Load resource from custom location
 *         return new CustomResourceInputStream(location);
 *     }
 * }
 * </pre>
 *
 * @see PropifyProcessor The annotation processor that uses these resource loaders
 * @see PropifyConfigParser Interface for parsing the loaded resources
 * @since 1.0.0
 */
public interface PropifyConfigResource {

    /**
     * Determines if this resource loader can handle the specified location.
     *
     * <p>The location string typically follows a URI-like format with a scheme
     * indicating the resource type. Common schemes include:
     * <ul>
     *   <li>{@code classpath:} - Resources in the Java classpath
     *   <li>{@code file:} - Local file system resources
     *   <li>{@code http:} or {@code https:} - Remote resources via HTTP
     * </ul>
     *
     * <p>Implementation guidelines:
     * <ul>
     *   <li>Check for supported schemes/protocols
     *   <li>Validate basic location format
     *   <li>Consider case sensitivity based on the resource type
     * </ul>
     *
     * @param location the resource location to check
     * @return true if this loader can handle the specified location, false otherwise
     * @since 1.0.0
     */
    Boolean accept(String location);

    /**
     * Loads a configuration resource from the specified location.
     *
     * <p>This method is responsible for:
     * <ul>
     *   <li>Opening a connection to the resource
     *   <li>Providing access to the resource content as an InputStream
     *   <li>Handling resource access errors appropriately
     *   <li>Managing resource lifecycle
     * </ul>
     *
     * <p>Implementation guidelines:
     * <ul>
     *   <li>Use appropriate character encoding (UTF-8 recommended)
     *   <li>Handle resource not found scenarios
     *   <li>Provide clear error messages for access failures
     *   <li>Consider resource caching if appropriate
     *   <li>Ensure proper resource cleanup
     * </ul>
     *
     * <p>The returned InputStream should be closed by the caller when no longer needed.
     *
     * @param processingEnvironment the annotation processing environment
     * @param location the location of the resource to load
     * @return an InputStream providing access to the resource content
     * @throws IOException if the resource cannot be loaded
     * @since 1.0.0
     */
    InputStream load(ProcessingEnvironment processingEnvironment, String location) throws IOException;
}
