package com.vgerbot.propify.common;

import javax.lang.model.element.AnnotationValue;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.IntStream;

/**
 * Utility functions for propify processor.
 *
 * <p>This class provides utility functions for propify processor, such as
 * converting strings to valid Java class names or field names.
 */
public class Utils {

    private static final Set<String> JAVA_KEYWORDS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            "abstract", "assert", "boolean", "break", "byte", "case", "catch", "char",
            "class", "const", "continue", "default", "do", "double", "else", "enum",
            "extends", "false", "final", "finally", "float", "for", "goto", "if",
            "implements", "import", "instanceof", "int", "interface", "long", "native",
            "new", "null", "package", "private", "protected", "public", "return",
            "short", "static", "strictfp", "super", "switch", "synchronized", "this",
            "throw", "throws", "transient", "true", "try", "void", "volatile", "while"
    )));

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
        if (input == null || input.isEmpty()) {
            throw new IllegalArgumentException("Class name cannot be null or empty");
        }
        String sanitized = input.trim().replaceAll("[^\\p{L}\\p{N}$]+", "-");

        if (!Character.isLetter(sanitized.charAt(0))) {
            sanitized = "_" + sanitized;
        }

        String[] parts = sanitized.split("-");
        StringBuilder camelCaseName = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            if (!parts[i].isEmpty()) {
                camelCaseName.append(Character.toUpperCase(parts[i].charAt(0)))
                             .append(parts[i].substring(1));
            }
        }

        String result = camelCaseName.toString();
        if (JAVA_KEYWORDS.contains(result)) {
            result += "_";
        }

        return result;
    }

    /**
     * Converts a given input string to a valid Java field name format.
     * Special characters are handled according to these rules:
     * 1. Letters, digits, and dollar signs ($) are preserved
     * 2. Consecutive special characters are collapsed into a single underscore
     * 3. Leading digits are prefixed with an underscore
     * 4. Unicode letters are preserved
     * 5. Java keywords are suffixed with an underscore
     * 6. Trailing underscores from special characters are removed
     *
     * @param input the input string to be converted
     * @return the converted field name in valid Java field name format
     * @throws IllegalArgumentException if the input string is empty
     */
    public static String convertToFieldName(String input) {
        if (input == null || input.isEmpty()) {
            throw new IllegalArgumentException("Field name cannot be null or empty");
        }
        String sanitized = input.trim().replaceAll("[^\\p{L}\\p{N}$]+", "-");

        if (!Character.isLetter(sanitized.charAt(0))) {
            sanitized = "_" + sanitized;
        }

        String[] parts = sanitized.split("-");
        StringBuilder camelCaseName = new StringBuilder(parts[0]);
        for (int i = 1; i < parts.length; i++) {
            if (!parts[i].isEmpty()) {
                camelCaseName.append(Character.toUpperCase(parts[i].charAt(0)))
                             .append(parts[i].substring(1));
            }
        }

        String result = camelCaseName.toString();
        if (JAVA_KEYWORDS.contains(result) || Character.isDigit(result.charAt(0))) {
            result += "_";
        }

        return result;
    }
    public static String convertToGetterName(String input, boolean isBoolean) {
        String fieldName = convertToFieldName(input);
        String getterPrefix = isBoolean ? "is" : "get";
        return getterPrefix + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
    }

    /**
     * Converts a given value to a Java literal string representation.
     * Handles various Java types with appropriate literal suffixes and formatting:
     * - Numeric types (Long, Float, Double, Byte, Short) with type suffixes
     * - Characters with single quotes and proper escaping
     * - Strings with double quotes and proper escaping
     * - Arrays with proper formatting
     * - Other types converted using String.valueOf()
     *
     * @param value the value to be converted
     * @return the converted string literal
     */
    public static String toLiteralString(Object value) {
        if (value == null) {
            return "null";
        }
        
        if (value instanceof Long) {
            return value + "L";
        } else if (value instanceof Float) {
            return value + "F";
        } else if (value instanceof Double) {
            return value + "D";
        } else if (value instanceof Byte) {
            return value + "B";
        } else if (value instanceof Short) {
            return value + "S";
        } else if (value instanceof Character) {
            return "'" + escapeChar((Character) value) + "'";
        } else if (value instanceof String) {
            return "\"" + escapeString((String) value) + "\"";
        } else if (value.getClass().isArray()) {
            return arrayToLiteralString(value);
        }
        
        return String.valueOf(value);
    }

    private static String escapeChar(char c) {
        switch (c) {
            case '\b': return "\\b";
            case '\t': return "\\t";
            case '\n': return "\\n";
            case '\f': return "\\f";
            case '\r': return "\\r";
            case '\"': return "\\\"";
            case '\'': return "\\'";
            case '\\': return "\\\\";
            default:
                if (c < 32 || c > 126) {
                    return String.format("\\u%04x", (int) c);
                }
                return String.valueOf(c);
        }
    }

    private static String escapeString(String str) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            sb.append(escapeChar(str.charAt(i)));
        }
        return sb.toString();
    }

    private static String arrayToLiteralString(Object array) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        
        int length = java.lang.reflect.Array.getLength(array);
        for (int i = 0; i < length; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            Object element = java.lang.reflect.Array.get(array, i);
            sb.append(toLiteralString(element));
        }
        
        sb.append("}");
        return sb.toString();
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
                return "application/yaml";
            default:
                return null;
        }
    }

    public static String[] getClassesFromAnnotationValue(AnnotationValue annotationValue) {
        if(annotationValue == null) {
            return new String[0];
        }
        Object value = annotationValue.getValue();
        Class<?> cls = value.getClass();
        if ("com.sun.tools.javac.util.List".equals(cls.getName())) {
            try {
                Method getMethod = cls.getDeclaredMethod("get", int.class);
                Method sizeMethod = cls.getDeclaredMethod("size");
                Integer size = (Integer)sizeMethod.invoke(value);
                if(size >= 0) {
                    return IntStream.range(0, size).mapToObj(index -> {
                        try {
                            Object classInfo = getMethod.invoke(value, index);
                            Method getValueMethod = classInfo.getClass().getMethod("getValue");
                            Object type = getValueMethod.invoke(classInfo);
                            return type.toString();
                        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                            throw new RuntimeException(e);
                        }
                    }).toArray(String[]::new);
                }
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
        return new String[]{};
    }
}
