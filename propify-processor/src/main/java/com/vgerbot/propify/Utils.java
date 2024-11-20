package com.vgerbot.propify;

public class Utils {
    private Utils() {
        throw new RuntimeException("Cannot instantiate Utils class");
    }
    /**
     * Converts a given input string to a valid Java class name format.
     * Non-letter and non-digit characters are replaced with underscores,
     * and the first letter of each word is capitalized. If the resulting
     * class name starts with a digit, an underscore is prefixed to the
     * class name.
     *
     * @param input the input string to be converted
     * @return the converted class name in valid Java class name format
     */
    public static String convertToClassName(String input) {
        char[] chars = input.toCharArray();
        StringBuilder className = new StringBuilder();
        boolean shouldCapitalize = true;

        for (char c : chars) {
            if (Character.isLetterOrDigit(c)) {
                if (shouldCapitalize) {
                    className.append(Character.toUpperCase(c));
                    shouldCapitalize = false;
                } else {
                    className.append(c);
                }
            } else {
                className.append('_');
                shouldCapitalize = true;
            }
        }

        if (Character.isDigit(className.charAt(0))) {
            className.insert(0, '_');
        }

        return className.toString();
    }

    /**
     * Converts a given input string to a valid Java field name format.
     * Non-letter and non-digit characters are replaced with underscores,
     * and the first letter of each word is capitalized, except the first
     * letter of the field name is lowercased. If the resulting field name
     * starts with a digit, an underscore is prefixed to the field name.
     *
     * @param input the input string to be converted
     * @return the converted field name in valid Java field name format
     */
    public static  String convertToFieldName(String input) {
        if (input.length() == 0) {
            throw new IllegalArgumentException("Field name cannot be empty");
        }
        char[] chars = input.toCharArray();
        StringBuilder fieldName = new StringBuilder();
        boolean shouldCapitalize = false;
        if (Character.isDigit(chars[0])) {
            fieldName.append('_');
        }
        for (char c : chars) {
            if (Character.isLetterOrDigit(c)) {
                if (shouldCapitalize) {
                    fieldName.append(Character.toUpperCase(c));
                    shouldCapitalize = false;
                } else {
                    fieldName.append(Character.toLowerCase(c));
                }
            } else {
                fieldName.append('_');
                shouldCapitalize = true;
            }
        }

        return fieldName.toString();
    }
    public static String toLiteralString(Object value) {
        if (value instanceof Long) {
            return value + "L";  // long 类型的字面量
        } else if (value instanceof Float) {
            return value + "f";  // float 类型的字面量
        } else if (value instanceof Double) {
            return value + "d";  // double 类型的字面量
        } else if (value instanceof Byte) {
            return value + "b";  // byte 类型的字面量
        } else if(value instanceof  String) {
            return "\"" + value + "\"";
        }
        return String.valueOf(value);  // 其他类型直接转为字符串
    }
}
