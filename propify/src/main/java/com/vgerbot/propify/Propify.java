package com.vgerbot.propify;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that enables automatic configuration property class generation.
 *
 * &lt;p&gt;The Propify annotation processor generates a strongly-typed configuration class from
 * external configuration files (properties, YAML, etc.). This provides type-safe access to
 * configuration properties and eliminates the need for manual property binding.
 *
 * &lt;p&gt;Key features:
 * &lt;ul&gt;
 *   &lt;li&gt;Automatic type conversion from string properties to Java types
 *   &lt;li&gt;Support for nested properties through dot notation
 *   &lt;li&gt;Flexible resource loading from classpath or URLs
 *   &lt;li&gt;Multiple configuration formats (properties, YAML) through media type detection
 * &lt;/ul&gt;
 *
 * &lt;p&gt;Basic usage example:
 * &lt;pre&gt;
 * {@literal @}Propify("classpath:application.properties")
 * public class ApplicationConfig {
 * }
 * &lt;/pre&gt;
 *
 * &lt;p&gt;Example with explicit media type and custom class name:
 * &lt;pre&gt;
 * {@literal @}Propify(
 *     value = "classpath:config.yaml",
 *     mediaType = "application/yaml",
 *     generatedClassName = "$$Properties"
 * )
 * public class ApplicationConfig {
 * }
 * &lt;/pre&gt;
 *
 * &lt;p&gt;The processor will generate a class that provides type-safe access to all properties
 * defined in the configuration file. The generated class name follows the pattern specified
 * in {@link #generatedClassName()}.
 *
 * @see PropifyProcessor The annotation processor that handles this annotation
 * @see PropifyConfigParser Interface for parsing different configuration formats
 * @since 1.0.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface Propify {

    /**
     * Specifies the location of the configuration resource file.
     *
     * &lt;p&gt;The location can be specified using various protocols:
     * &lt;ul&gt;
     *   &lt;li&gt;Classpath resources: {@code "classpath:config/application.properties"}
     *   &lt;li&gt;File system paths: {@code "file:/path/to/config.yaml"}
     *   &lt;li&gt;URLs: {@code "http://config-server/app.properties"}
     * &lt;/ul&gt;
     *
     * &lt;p&gt;The resource must be accessible during the annotation processing phase.
     * For classpath resources, ensure they are included in the annotation processor's
     * classpath.
     *
     * @return the location URI of the configuration resource
     * @see PropifyConfigResource For details on resource loading
     * @since 1.0.0
     */
    String location();

    /**
     * Specifies the media type of the configuration resource.
     *
     * &lt;p&gt;The media type helps the processor determine how to parse the configuration file.
     * If not specified, the processor will attempt to infer the type from the file extension.
     *
     * &lt;p&gt;Common media types include:
     * &lt;ul&gt;
     *   &lt;li&gt;{@code "text/x-java-properties"} - Java Properties files
     *   &lt;li&gt;{@code "application/yaml"} or {@code "application/x-yaml"} - YAML files
     *   &lt;li&gt;{@code "application/json"} - JSON files
     * &lt;/ul&gt;
     *
     * @return the media type of the configuration resource, or empty string for auto-detection
     * @see PropifyConfigParser#accept(String) For media type handling
     * @since 1.0.0
     */
    String mediaType() default "";

    /**
     * Specifies the name pattern for the generated configuration class.
     *
     * &lt;p&gt;The pattern can include the special placeholder {@code $$}, which will be
     * replaced with the name of the annotated class. This allows for flexible naming
     * of the generated class while maintaining a clear relationship with the source class.
     *
     * &lt;p&gt;Examples:
     * &lt;ul&gt;
     *   &lt;li&gt;{@code "$$Propify"} → MyConfig → MyConfigPropify
     *   &lt;li&gt;{@code "$$Properties"} → AppConfig → AppConfigProperties
     *   &lt;li&gt;{@code "Generated$$"} → Config → GeneratedConfig
     * &lt;/ul&gt;
     *
     * &lt;p&gt;The generated class will be created in the same package as the annotated class.
     *
     * @return the pattern for generating the configuration class name
     * @since 1.0.0
     */
    String generatedClassName() default "$$Propify";

    /**
     * Controls whether automatic type conversion should be performed for configuration values.
     *
     * &lt;p&gt;When enabled (default), the processor will automatically convert string values from
     * the configuration file to their appropriate Java types based on the property types in
     * the generated class. This includes:
     * &lt;ul&gt;
     *   &lt;li&gt; Primitive wrapper classes (Integer, Long, Boolean, etc.)
     *   &lt;li&gt;Collections and arrays of supported types
     * &lt;/ul&gt;
     *
     * &lt;p&gt;When disabled, values will be kept as strings and type conversion must be handled
     * manually in the application code.
     *
     * @return true to enable automatic type conversion (default), false to disable it
     * @since 1.1.0
     */
    boolean autoTypeConversion() default true;
}
