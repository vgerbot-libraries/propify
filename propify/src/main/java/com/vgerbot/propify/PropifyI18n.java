package com.vgerbot.propify;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface PropifyI18n {

    String baseName();
    /**
     * Specifies the default locale for i18n property files.
     *
     * <p>The default locale is used when:
     * <ul>
     *   <li>No locale-specific property file exists for the requested locale
     *   <li>A property is missing in the locale-specific file
     * </ul>
     *
     * <p>The locale should be specified in the format:
     * <ul>
     *   <li>language code: "en"
     *   <li>language and country codes: "en_US"
     * </ul>
     *
     * @return the default locale for i18n properties
     * @since 1.2.0
     */
    String defaultLocale() default "";

    String generatedClassName() default "MessageResource";
}
