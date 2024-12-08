package com.vgerbot.example.jdk8;

import com.vgerbot.propify.I18n;

/**
 * Example demonstrating i18n support in Propify.
 * This class will have its properties generated from messages.properties files
 * with support for different locales (en, zh).
 */
@I18n(
        baseName = "messages",
        defaultLocale = "zh"
)
public class I18nExample {
    public static void main(String[] args) {
        System.out.println("==================== JAVA PROPERTIES ============================");
        String x = MessageResource.getDefault().greeting("Mario");
        System.out.println(x);
    }
}

