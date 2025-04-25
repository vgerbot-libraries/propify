package com.vgerbot.propify.core;

import com.vgerbot.propify.PropifyProcessor;
import org.apache.commons.configuration2.Configuration;

import java.io.IOException;
import java.io.InputStream;

/**
 * Core interface for parsing configuration resources in the Propify framework.
 *
 * <p>This interface defines the contract for parsers that can read configuration data
 * from various formats (such as properties files, YAML, JSON, etc.) and convert them
 * into a standardized {@link PropifyProperties} representation. Implementations of this
 * interface handle the specifics of parsing different configuration formats while
 * maintaining a consistent API for the rest of the framework.
 *
 * <p>Key responsibilities of implementations:
 * <ul>
 *   <li>Parse configuration data from an input stream
 *   <li>Convert configuration values to appropriate Java types
 *   <li>Handle nested properties and complex data structures
 *   <li>Maintain consistent error handling and reporting
 * </ul>
 *
 * <p>Parsers are discovered through Java's ServiceLoader mechanism. To register a new
 * parser implementation, include its fully qualified class name in:
 * {@code META-INF/services/com.vgerbot.propify.core.PropifyConfigParser}
 *
 * <p>Example implementation for a custom format:
 * <pre>
 * public class CustomFormatParser implements PropifyConfigParser {
 *     {@literal @}Override
 *     public PropifyProperties parse(PropifyContext context, InputStream stream) throws IOException {
 *         PropifyProperties properties = new PropifyProperties(context.isAutoTypeConversion());
 *         // Parse stream and populate properties
 *         return properties;
 *     }
 *
 *     {@literal @}Override
 *     public Boolean accept(String mediaType) {
 *         return "application/x-custom-format".equalsIgnoreCase(mediaType);
 *     }
 * }
 * </pre>
 *
 * @see PropifyProperties The standardized properties container
 * @see PropifyProcessor The annotation processor that uses these parsers
 * @since 1.0.0
 */
public interface PropifyConfigParser {

    /**
     * Parses configuration data from an input stream into a {@link PropifyProperties} object.
     *
     * <p>This method is responsible for:
     * <ul>
     *   <li>Reading and parsing the configuration data from the input stream
     *   <li>Converting string values to appropriate Java types (numbers, booleans, etc.)
     *   <li>Handling nested properties using dot notation (e.g., "server.port")
     *   <li>Maintaining the hierarchical structure of the configuration
     * </ul>
     *
     * <p>Implementation guidelines:
     * <ul>
     *   <li>Always close any resources created during parsing
     *   <li>Handle character encoding appropriately (UTF-8 recommended)
     *   <li>Provide clear error messages for parsing failures
     *   <li>Validate data types and formats where appropriate
     *   <li>Respect the autoTypeConversion setting from the context
     * </ul>
     * @param context the context containing configuration information
     * @param stream the input stream containing configuration data
     * @return {@link PropifyProperties} object containing the parsed configuration
     * @throws IOException if an error occurs while reading or parsing the stream
     * @see PropifyProperties For the structure of the returned object
     * @since 1.0.0
     */
    Configuration parse(PropifyContext context, InputStream stream) throws IOException;

    /**
     * Determines if this parser can handle configuration data of the specified media type.
     *
     * <p>The media type string follows the MIME type format as specified in
     * <a href="https://tools.ietf.org/html/rfc2046">RFC 2046</a>.
     * Common media types include:
     * <ul>
     *   <li>{@code text/x-java-properties} - Java Properties files
     *   <li>{@code application/yaml} - YAML files
     *   <li>{@code application/json} - JSON files
     * </ul>
     *
     * <p>Implementation guidelines:
     * <ul>
     *   <li>Media type comparison should be case-insensitive
     *   <li>Consider supporting multiple media types for the same format
     *   <li>Handle both primary and alternative MIME types where appropriate
     * </ul>
     *
     * @param context the media type to check, following MIME type format
     * @return true if this parser can handle the specified media type, false otherwise
     * @since 1.0.0
     */
    Boolean accept(PropifyContext context);
}
