package com.vgerbot.propify.core;


import com.vgerbot.propify.PropifyProcessor;
import com.vgerbot.propify.common.TemporarySupport;
import com.vgerbot.propify.lookup.PropifyLookup;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that enables automatic configuration property class generation.
 *
 * <p>The Propify annotation processor generates a strongly-typed configuration class from
 * external configuration files (properties, YAML, etc.). This provides type-safe access to
 * configuration properties and eliminates the need for manual property binding.
 *
 * <p>Key features:
 * <ul>
 *   <li>Automatic type conversion from string properties to Java types
 *   <li>Support for nested properties through dot notation
 *   <li>Flexible resource loading from classpath or URLs
 *   <li>Multiple configuration formats (properties, YAML) through media type detection
 * </ul>
 *
 * <p>Basic usage example:
 * <pre>
 * {@literal @}Propify("classpath:application.properties")
 * public class ApplicationConfig {
 * }
 * </pre>
 *
 * <p>Example with explicit media type and custom class name:
 * <pre>
 * {@literal @}Propify(
 *     value = "classpath:config.yaml",
 *     mediaType = "application/yaml",
 *     generatedClassName = "$$Properties"
 * )
 * public class ApplicationConfig {
 * }
 * </pre>
 *
 * <p>The processor will generate a class that provides type-safe access to all properties
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
     * <p>The location can be specified using various protocols:
     * <ul>
     *   <li>Classpath resources: {@code "classpath:config/application.properties"}
     *   <li>File system paths: {@code "file:/path/to/config.yaml"}
     *   <li>URLs: {@code "http://config-server/app.properties"}
     * </ul>
     *
     * <p>The resource must be accessible during the annotation processing phase.
     * For classpath resources, ensure they are included in the annotation processor's
     * classpath.
     *
     * @return the location URI of the configuration resource
     * @see ResourceLoader For details on resource loading
     * @since 1.0.0
     */
    String location();

    /**
     * Specifies the media type of the configuration resource.
     *
     * <p>The media type helps the processor determine how to parse the configuration file.
     * If not specified, the processor will attempt to infer the type from the file extension.
     *
     * <p>Common media types include:
     * <ul>
     *   <li>{@code "text/x-java-properties"} - Java Properties files
     *   <li>{@code "application/yaml"} or {@code "application/x-yaml"} - YAML files
     *   <li>{@code "application/json"} - JSON files
     * </ul>
     *
     * @return the media type of the configuration resource, or empty string for auto-detection
     * @see PropifyConfigParser#accept(PropifyContext) For media type handling
     * @since 1.0.0
     */
    String mediaType() default "";

    /**
     * Specifies the name pattern for the generated configuration class.
     *
     * <p>The pattern can include the special placeholder {@code $$}, which will be
     * replaced with the name of the annotated class. This allows for flexible naming
     * of the generated class while maintaining a clear relationship with the source class.
     *
     * <p>Examples:
     * <ul>
     *   <li>{@code "$$Propify"} → MyConfig → MyConfigPropify
     *   <li>{@code "$$Properties"} → AppConfig → AppConfigProperties
     *   <li>{@code "Generated$$"} → Config → GeneratedConfig
     * </ul>
     *
     * <p>The generated class will be created in the same package as the annotated class.
     *
     * @return the pattern for generating the configuration class name
     * @since 1.0.0
     */
    String generatedClassName() default "$$Propify";

    char listDelimiter() default ',';

    @TemporarySupport("This method is temporarily supported and may change or be removed in the future.")
    Class<? extends PropifyLookup>[] lookups() default {};

}
