package com.vgerbot.propify;

import java.io.IOException;
import java.io.InputStream;

/**
 * Core interface for parsing configuration resources in the Propify framework.
 *
 * &lt;p&gt;This interface defines the contract for parsers that can read configuration data
 * from various formats (such as properties files, YAML, JSON, etc.) and convert them
 * into a standardized {@link PropifyProperties} representation. Implementations of this
 * interface handle the specifics of parsing different configuration formats while
 * maintaining a consistent API for the rest of the framework.
 *
 * &lt;p&gt;Key responsibilities of implementations:
 * &lt;ul&gt;
 *   &lt;li&gt;Parse configuration data from an input stream
 *   &lt;li&gt;Convert configuration values to appropriate Java types
 *   &lt;li&gt;Handle nested properties and complex data structures
 *   &lt;li&gt;Maintain consistent error handling and reporting
 * &lt;/ul&gt;
 *
 * &lt;p&gt;Parsers are discovered through Java's ServiceLoader mechanism. To register a new
 * parser implementation, include its fully qualified class name in:
 * {@code META-INF/services/com.vgerbot.propify.PropifyConfigParser}
 *
 * &lt;p&gt;Example implementation for a custom format:
 * &lt;pre&gt;
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
 * &lt;/pre&gt;
 *
 * @see PropifyProperties The standardized properties container
 * @see PropifyProcessor The annotation processor that uses these parsers
 * @since 1.0.0
 */
public interface PropifyConfigParser {

    /**
     * Parses configuration data from an input stream into a {@link PropifyProperties} object.
     *
     * &lt;p&gt;This method is responsible for:
     * &lt;ul&gt;
     *   &lt;li&gt;Reading and parsing the configuration data from the input stream
     *   &lt;li&gt;Converting string values to appropriate Java types (numbers, booleans, etc.)
     *   &lt;li&gt;Handling nested properties using dot notation (e.g., "server.port")
     *   &lt;li&gt;Maintaining the hierarchical structure of the configuration
     * &lt;/ul&gt;
     *
     * &lt;p&gt;Implementation guidelines:
     * &lt;ul&gt;
     *   &lt;li&gt;Always close any resources created during parsing
     *   &lt;li&gt;Handle character encoding appropriately (UTF-8 recommended)
     *   &lt;li&gt;Provide clear error messages for parsing failures
     *   &lt;li&gt;Validate data types and formats where appropriate
     *   &lt;li&gt;Respect the autoTypeConversion setting from the context
     * &lt;/ul&gt;
     *
     * @param context the PropifyContext containing configuration settings
     * @param stream the input stream containing configuration data
     * @return a {@link PropifyProperties} object containing the parsed configuration
     * @throws IOException if an error occurs while reading or parsing the stream
     * @see PropifyProperties For the structure of the returned object
     * @since 1.0.0
     */
    PropifyProperties parse(PropifyContext context, InputStream stream) throws IOException;

    /**
     * Determines if this parser can handle configuration data of the specified media type.
     *
     * &lt;p&gt;The media type string follows the MIME type format as specified in
     * &lt;a href="https://tools.ietf.org/html/rfc2046"&gt;RFC 2046&lt;/a&gt;.
     * Common media types include:
     * &lt;ul&gt;
     *   &lt;li&gt;{@code text/x-java-properties} - Java Properties files
     *   &lt;li&gt;{@code application/yaml} - YAML files
     *   &lt;li&gt;{@code application/json} - JSON files
     * &lt;/ul&gt;
     *
     * &lt;p&gt;Implementation guidelines:
     * &lt;ul&gt;
     *   &lt;li&gt;Media type comparison should be case-insensitive
     *   &lt;li&gt;Consider supporting multiple media types for the same format
     *   &lt;li&gt;Handle both primary and alternative MIME types where appropriate
     * &lt;/ul&gt;
     *
     * @param mediaType the media type to check, following MIME type format
     * @return true if this parser can handle the specified media type, false otherwise
     * @since 1.0.0
     */
    Boolean accept(String mediaType);
}
