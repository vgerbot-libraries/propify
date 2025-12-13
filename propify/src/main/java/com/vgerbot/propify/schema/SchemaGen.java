package com.vgerbot.propify.schema;

import com.vgerbot.propify.PropifyProcessor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that enables automatic Java class generation from schema definitions.
 *
 * <p>The SchemaGen annotation processor generates mutable POJO/DTO classes from
 * schema definitions (JSON Schema, OpenAPI, etc.). Unlike {@code @Propify} which generates
 * read-only configuration classes with embedded data, {@code @SchemaGen} generates
 * empty, mutable data classes suitable for runtime data binding with JSON/XML.
 *
 * <p>Key features:
 * <ul>
 *   <li>Generates mutable POJOs with getters and setters</li>
 *   <li>Support for Builder pattern</li>
 *   <li>Automatic Jackson annotations for JSON serialization</li>
 *   <li>Bean Validation annotations from schema constraints</li>
 *   <li>Multiple schema formats (JSON Schema, OpenAPI)</li>
 * </ul>
 *
 * <p>Basic usage example:
 * <pre>
 * {@literal @}SchemaGen(location = "schemas/user.schema.json")
 * public interface User {
 * }
 * </pre>
 *
 * <p>OpenAPI example:
 * <pre>
 * {@literal @}SchemaGen(
 *     location = "openapi/petstore.yaml",
 *     type = SchemaType.OPENAPI,
 *     schemaRef = "Pet"
 * )
 * public interface Pet {
 * }
 * </pre>
 *
 * @see PropifyProcessor The annotation processor that handles this annotation
 * @see SchemaType Supported schema types
 * @since 2.1.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface SchemaGen {

    /**
     * Specifies the location of the schema file.
     *
     * <p>The location can be specified using various protocols:
     * <ul>
     *   <li>Classpath resources: {@code "classpath:schemas/user.schema.json"}
     *   <li>File system paths: {@code "file:/path/to/schema.json"}
     *   <li>URLs: {@code "https://example.com/api/schema.json"}
     * </ul>
     *
     * @return the location URI of the schema resource
     * @since 2.1.0
     */
    String location();

    /**
     * Specifies the type of schema definition.
     *
     * <p>If set to {@link SchemaType#AUTO}, the processor will attempt to detect
     * the schema type from the file extension:
     * <ul>
     *   <li>.json with "$schema" → JSON Schema</li>
     *   <li>.yaml/.yml with "openapi" → OpenAPI</li>
     *   <li>.xsd → XML Schema</li>
     * </ul>
     *
     * @return the schema type
     * @since 2.1.0
     */
    SchemaType type() default SchemaType.AUTO;

    /**
     * For OpenAPI: specifies which schema definition to generate.
     *
     * <p>Can be:
     * <ul>
     *   <li>Simple name: {@code "User"} → looks up in components/schemas/User</li>
     *   <li>JSON reference: {@code "#/components/schemas/Pet"}</li>
     *   <li>Empty string: generates all schemas in the file</li>
     * </ul>
     *
     * @return the schema reference path
     * @since 2.1.0
     */
    String schemaRef() default "";

    /**
     * Specifies the name pattern for the generated class.
     *
     * <p>The pattern can include the special placeholder {@code $$}, which will be
     * replaced with the name of the annotated class/interface.
     *
     * <p>Examples:
     * <ul>
     *   <li>{@code "$$"} → User → User (default)</li>
     *   <li>{@code "$$Dto"} → User → UserDto</li>
     *   <li>{@code "Generated$$"} → User → GeneratedUser</li>
     * </ul>
     *
     * @return the pattern for generating the class name
     * @since 2.1.0
     */
    String generatedClassName() default "$$";

    /**
     * Whether to generate a Builder pattern for the class.
     *
     * @return true to generate a builder, false otherwise
     * @since 2.1.0
     */
    boolean builder() default true;

    /**
     * Whether to add Jackson annotations for JSON serialization.
     *
     * <p>When enabled, adds annotations like:
     * <ul>
     *   <li>{@code @JsonProperty}</li>
     *   <li>{@code @JsonFormat}</li>
     *   <li>{@code @JsonIgnore}</li>
     * </ul>
     *
     * @return true to add Jackson annotations, false otherwise
     * @since 2.1.0
     */
    boolean jacksonAnnotations() default true;

    /**
     * Whether to add JAXB annotations for XML serialization.
     *
     * <p>When enabled, adds annotations like:
     * <ul>
     *   <li>{@code @XmlRootElement}</li>
     *   <li>{@code @XmlElement}</li>
     *   <li>{@code @XmlAttribute}</li>
     * </ul>
     *
     * @return true to add JAXB annotations, false otherwise
     * @since 2.1.0
     */
    boolean jaxbAnnotations() default false;

    /**
     * Whether to add Bean Validation annotations.
     *
     * <p>When enabled, generates validation annotations based on schema constraints:
     * <ul>
     *   <li>{@code @NotNull} for required fields</li>
     *   <li>{@code @Size} for string length constraints</li>
     *   <li>{@code @Min/@Max} for numeric ranges</li>
     *   <li>{@code @Pattern} for regex patterns</li>
     *   <li>{@code @Email} for email format</li>
     * </ul>
     *
     * @return true to add validation annotations, false otherwise
     * @since 2.1.0
     */
    boolean validationAnnotations() default true;

    /**
     * Whether the generated class should implement {@link java.io.Serializable}.
     *
     * @return true to implement Serializable, false otherwise
     * @since 2.1.0
     */
    boolean serializable() default true;

    /**
     * Whether to generate {@code equals()}, {@code hashCode()}, and {@code toString()} methods.
     *
     * @return true to generate these methods, false otherwise
     * @since 2.1.0
     */
    boolean generateHelperMethods() default true;
}
