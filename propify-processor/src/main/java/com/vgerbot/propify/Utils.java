package com.vgerbot.propify;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility functions for propify processor.
 *
 * <p>This class provides utility functions for propify processor, such as
 * converting strings to valid Java class names or field names.
 */
public class Utils {
    private static final Pattern NUMBER_PATTERN = Pattern.compile("([+-])?(\\d+)(\\.\\d+)?([BblLfFdD]?)");

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
     * and the first letter of each word is capitalized. If the resulting
     * field name starts with a digit, an underscore is prefixed to the
     * field name. Additionally, if the resulting field name is a reserved
     * keyword in Java, an underscore is appended to the field name.
     *
     * @param input the input string to be converted
     * @return the converted field name in valid Java field name format
     * @throws IllegalArgumentException if the input string is empty
     */
    public static String convertToFieldName(String input) {
        if (input.length() == 0) {
            throw new IllegalArgumentException("Field name cannot be empty");
        }
        char[] chars = input.toCharArray();
        StringBuilder fieldName = new StringBuilder();
        boolean shouldCapitalize = false;
        if (!(Character.isLetter(chars[0]) || chars[0] == '_')) {
            fieldName.append('_');
        }
        for (char c : chars) {
            if (Character.isLetterOrDigit(c)) {
                if (shouldCapitalize) {
                    fieldName.append(Character.toUpperCase(c));
                    shouldCapitalize = false;
                } else {
                    fieldName.append(c);
                }
            } else {
                fieldName.append('_');
                shouldCapitalize = true;
            }
        }
        switch (fieldName.toString()) {
            case "class":
            case "public":
            case "final":
            case "static":
            case "int":
            case "long":
            case "float":
            case "byte":
            case "double":
            case "void":
            case "boolean":
            case "synchronized":
            case "volatile":
                fieldName.append('_');
                break;
        }

        return fieldName.toString();
    }

    /**
     * Converts a given value to a Java literal string.
     * If the value is an instance of Long, Float, Double, or Byte,
     * it is converted to a string literal in the corresponding type.
     * If the value is an instance of String, it is enclosed in double quotes.
     * Otherwise, it is converted to a string using {@link String#valueOf(Object)}.
     *
     * @param value the value to be converted
     * @return the converted string literal
     */

    public static String toLiteralString(Object value) {
        if (value instanceof Long) {
            return value + "L";  // long 类型的字面量
        } else if (value instanceof Float) {
            return value + "F";  // float 类型的字面量
        } else if (value instanceof Double) {
            return value + "D";  // double 类型的字面量
        } else if (value instanceof Byte) {
            return value + "B";  // byte 类型的字面量
        } else if (value instanceof String) {
            return "\"" + value + "\"";
        }
        return String.valueOf(value);  // 其他类型直接转为字符串
    }

    /**
     * Infer media type from a given file name.
     *
     * @param fileName the file name to be inferred
     * @return the inferred media type, or {@code null} if the media type cannot be inferred
     */
    public static String inferMediaType(String fileName) {
        String ext = fileName.substring(fileName.lastIndexOf('.') + 1);
        switch (ext) {
            case "properties":
                return "text/x-java-properties";
            case "json":
                return "application/json";
            case "yml":
            case "yaml":
                return "application/x-yaml";
            default:
                return null;
        }
    }

    /**
     * Converts a given object to its corresponding Java type.
     * <p>
     * The function attempts to interpret the input object as a specific Java type.
     * If the object is a string representation of a boolean ("true" or "false"),
     * it is converted to a Boolean. If the object is a string representation of a
     * number with an optional suffix (such as 'L', 'B', 'F', 'D'), it is converted
     * to the corresponding numeric type (Long, Byte, Float, Double). If no suffix
     * is present and the number is within the range of an Integer, it is converted
     * to an Integer; otherwise, it is converted to a Long.
     * <p>
     * If the input object does not match any recognized pattern, it is returned as is.
     *
     * @param value the object to be converted
     * @return the converted object in its corresponding Java type, or the original
     * object if no conversion is applicable
     */

    public static Object parseValue(Object value) {
        final String strValue = value + "";
        if (value.equals("true") || value.equals("false")) {
            return Boolean.valueOf(strValue);
        }
        Matcher matcher = NUMBER_PATTERN.matcher(strValue);
        if (matcher.matches()) {
            int gid = 1;
            String signal = matcher.group(gid ++ );
            String intPart = matcher.group(gid ++ );
            String floatPart = matcher.group(gid ++ );
            String suffix = matcher.group(gid ++ );
            int sig = "-".equals(signal) ? -1 : 1;
            switch (suffix.toLowerCase()) {
                case "l":
                    return sig * Long.valueOf(intPart);
                case "b":
                    return Byte.valueOf(intPart);
                case "f":
                    return Float.valueOf(strValue);
                case "d":
                    return Double.valueOf(strValue);
                default:
                    if (hasValue(floatPart)) {
                        return Double.valueOf(strValue);
                    } else {
                        Long l = Long.valueOf(strValue);
                        if(l > Integer.MAX_VALUE) {
                            return l;
                        } else {
                            return Integer.valueOf(strValue);
                        }
                    }
            }
        }

        return value;
    }

    private static boolean hasValue(String value) {
        return value != null && !value.isEmpty() && !"null".equals(value);
    }

}
