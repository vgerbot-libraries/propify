package com.vgerbot.propify.i18n;

import com.vgerbot.propify.PropifyProcessor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that enables internationalization (i18n) support through resource bundles.
 * 
 * <p>This annotation generates type-safe wrappers for accessing internationalized messages
 * from resource bundles. It supports:
 * <ul>
 *   <li>Multiple locales through resource bundle variants</li>
 *   <li>Message formatting with parameters</li>
 *   <li>Fallback to default locale</li>
 *   <li>Compile-time validation of message keys</li>
 * </ul>
 *
 * <p>Basic usage example:
 * <pre>
 * {@literal @}I18n(baseName = "messages")
 * public class Messages {
 * }
 * </pre>
 *
 * <p>Example with custom configuration:
 * <pre>
 * {@literal @}I18n(
 *     baseName = "i18n/messages",
 *     defaultLocale = "en_US",
 *     generatedClassName = "I18nMessages"
 * )
 * public class Messages {
 * }
 * </pre>
 *
 * <p>The processor will generate a class that provides type-safe access to all messages
 * defined in the resource bundles. The generated class supports:
 * <ul>
 *   <li>Locale-specific message resolution</li>
 *   <li>Parameter interpolation in messages</li>
 *   <li>Fallback to default locale messages</li>
 * </ul>
 *
 * @see PropifyProcessor The annotation processor that handles this annotation
 * @since 1.1.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface I18n {

    /**
     * Specifies the base name of the resource bundle.
     * 
     * <p>The base name is used to locate the resource bundle files. It can include
     * path information relative to the classpath root. For example:
     * <ul>
     *   <li>"messages" - Looks for messages.properties, messages_en.properties, etc.</li>
     *   <li>"i18n/app" - Looks for i18n/app.properties, i18n/app_en.properties, etc.</li>
     * </ul>
     *
     * <p>The processor will look for resource bundle variants based on this base name,
     * following the standard ResourceBundle naming conventions:
     * <ul>
     *   <li>basename.properties - Default bundle</li>
     *   <li>basename_language.properties - Language-specific bundle</li>
     *   <li>basename_language_country.properties - Language and country specific bundle</li>
     * </ul>
     *
     * @return the base name for resource bundle lookup
     * @since 1.1.0
     */
    String baseName();

    /**
     * Specifies the default locale for i18n property files.
     *
     * <p>The default locale is used when:
     * <ul>
     *   <li>No locale-specific property file exists for the requested locale</li>
     *   <li>A property is missing in the locale-specific file</li>
     * </ul>
     *
     * <p>The locale should be specified in the format:
     * <ul>
     *   <li>language code: "en"</li>
     *   <li>language and country codes: "en_US"</li>
     * </ul>
     *
     * @return the default locale for i18n properties
     * @since 1.1.0
     */
    String defaultLocale() default "";

    /**
     * Specifies the name of the generated resource bundle wrapper class.
     * 
     * <p>This class will provide type-safe access to the internationalized messages
     * defined in the resource bundles. The generated class will include:
     * <ul>
     *   <li>Methods for each message key</li>
     *   <li>Parameter type checking for message formatting</li>
     *   <li>Locale management utilities</li>
     * </ul>
     *
     * <p>If not specified, defaults to "MessageResource". The generated class will be
     * created in the same package as the annotated class.
     *
     * @return the name for the generated resource bundle wrapper class
     * @since 1.1.0
     */
    String generatedClassName() default "MessageResource";
}
