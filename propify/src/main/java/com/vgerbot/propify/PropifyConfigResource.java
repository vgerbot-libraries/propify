package com.vgerbot.propify;

import javax.annotation.processing.ProcessingEnvironment;
import java.io.IOException;
import java.io.InputStream;

/**
 * Core interface for loading configuration resources in the Propify framework.
 *
 * &lt;p&gt;This interface defines the contract for resource loaders that can access
 * configuration files from various locations such as the classpath, file system,
 * or remote URLs. Implementations handle the specifics of resource access while
 * providing a consistent API for the framework to load configuration data.
 *
 * &lt;p&gt;Key responsibilities of implementations:
 * &lt;ul&gt;
 *   &lt;li&gt;Determine if they can handle a given resource location
 *   &lt;li&gt;Load resources from supported locations
 *   &lt;li&gt;Handle resource access errors appropriately
 *   &lt;li&gt;Manage resource lifecycle (opening/closing streams)
 * &lt;/ul&gt;
 *
 * &lt;p&gt;Resource loaders are discovered through Java's ServiceLoader mechanism.
 * To register a new loader implementation, include its fully qualified class name in:
 * {@code META-INF/services/com.vgerbot.propify.PropifyConfigResource}
 *
 * &lt;p&gt;Example implementation for a custom resource type:
 * &lt;pre&gt;
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
 * &lt;/pre&gt;
 *
 * @see PropifyProcessor The annotation processor that uses these resource loaders
 * @see PropifyConfigParser Interface for parsing the loaded resources
 * @since 1.0.0
 */
public interface PropifyConfigResource {

    /**
     * Determines if this resource loader can handle the specified location.
     *
     * &lt;p&gt;The location string typically follows a URI-like format with a scheme
     * indicating the resource type. Common schemes include:
     * &lt;ul&gt;
     *   &lt;li&gt;{@code classpath:} - Resources in the Java classpath
     *   &lt;li&gt;{@code file:} - Local file system resources
     *   &lt;li&gt;{@code http:} or {@code https:} - Remote resources via HTTP
     * &lt;/ul&gt;
     *
     * &lt;p&gt;Implementation guidelines:
     * &lt;ul&gt;
     *   &lt;li&gt;Check for supported schemes/protocols
     *   &lt;li&gt;Validate basic location format
     *   &lt;li&gt;Consider case sensitivity based on the resource type
     * &lt;/ul&gt;
     *
     * @param location the resource location to check
     * @return true if this loader can handle the specified location, false otherwise
     * @since 1.0.0
     */
    Boolean accept(String location);

    /**
     * Loads a configuration resource from the specified location.
     *
     * &lt;p&gt;This method is responsible for:
     * &lt;ul&gt;
     *   &lt;li&gt;Opening a connection to the resource
     *   &lt;li&gt;Providing access to the resource content as an InputStream
     *   &lt;li&gt;Handling resource access errors appropriately
     *   &lt;li&gt;Managing resource lifecycle
     * &lt;/ul&gt;
     *
     * &lt;p&gt;Implementation guidelines:
     * &lt;ul&gt;
     *   &lt;li&gt;Use appropriate character encoding (UTF-8 recommended)
     *   &lt;li&gt;Handle resource not found scenarios
     *   &lt;li&gt;Provide clear error messages for access failures
     *   &lt;li&gt;Consider resource caching if appropriate
     *   &lt;li&gt;Ensure proper resource cleanup
     * &lt;/ul&gt;
     *
     * &lt;p&gt;The returned InputStream should be closed by the caller when no longer needed.
     *
     * @param processingEnvironment the annotation processing environment
     * @param location the location of the resource to load
     * @return an InputStream providing access to the resource content
     * @throws IOException if the resource cannot be loaded
     * @since 1.0.0
     */
    InputStream load(ProcessingEnvironment processingEnvironment, String location) throws IOException;
}
