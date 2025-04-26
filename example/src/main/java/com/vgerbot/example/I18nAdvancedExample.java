package com.vgerbot.example;

import java.util.Locale;

import com.vgerbot.propify.i18n.I18n;

@I18n(baseName = "messages", generatedClassName = "I18nMessages")
public class I18nAdvancedExample {
    public static void main(String[] args) {
        I18nMessages.LocaleMessages messages = I18nMessages.get(Locale.forLanguageTag("en-US"));
        System.out.println(messages.productPrice(13));
        System.out.println(messages.itemCount(12));
    }
//    public static void main(String[] args) {
//        I18nMessages messages = I18nMessages.getInstance();
//
//        System.out.println("============== Advanced I18n Examples ==============\n");
//
//        // Basic message with parameters
//        String username = "John";
//        System.out.println("--- Basic Parameter Substitution ---");
//        System.out.println("English: " + messages.getWelcomeMessage(username));
//        System.out.println("Chinese: " + messages.withLocale(Locale.CHINESE).getWelcomeMessage(username));
//
//        // Number formatting
//        double price = 1234.56;
//        System.out.println("\n--- Number Formatting ---");
//        System.out.println("US Format: " + messages.getProductPrice(price));
//        System.out.println("Chinese Format: " + messages.withLocale(Locale.CHINESE).getProductPrice(price));
//
//        // Date formatting
//        Date now = new Date();
//        System.out.println("\n--- Date Formatting ---");
//        System.out.println("US Format: " + messages.getCurrentTime(now));
//        System.out.println("Chinese Format: " + messages.withLocale(Locale.CHINESE).getCurrentTime(now));
//
//        // Plural forms
//        System.out.println("\n--- Plural Forms ---");
//        for (int count : new int[]{0, 1, 2, 5}) {
//            System.out.println("Items (" + count + "): " + messages.getItemCount(count));
//        }
//
//        // Choice format
//        System.out.println("\n--- Choice Format ---");
//        for (int temperature : new int[]{-5, 15, 25, 35}) {
//            System.out.println("Temperature (" + temperature + "°C): " +
//                messages.getTemperatureDescription(temperature));
//        }
//
//        // Complex message with multiple parameters
//        System.out.println("\n--- Complex Message ---");
//        String user = "Alice";
//        int unreadMessages = 3;
//        Date lastLogin = new Date(System.currentTimeMillis() - 3600000); // 1 hour ago
//        System.out.println(messages.getUserStatus(user, unreadMessages, lastLogin));
//
//        // Error messages with parameters
//        System.out.println("\n--- Error Messages ---");
//        System.out.println(messages.getValidationError("email", "invalid format"));
//        System.out.println(messages.getValidationError("password", "too short"));
//    }
}
