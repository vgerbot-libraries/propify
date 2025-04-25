package com.vgerbot.propify.core;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.MapConfiguration;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.Period;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class PropifyPropertiesBuilder {

    private Configuration config;

    public PropifyPropertiesBuilder config(Configuration config) {
        this.config = config;
        return this;
    }

    public PropifyProperties build(PropifyContext context) {
        try {
            InputStream stream = context.loadResource();
            PropifyConfigParserProvider parserProvider = PropifyConfigParserProvider.getInstance();
            PropifyConfigParser parser = parserProvider.getParser(context);
            Configuration configuration = parser.parse(context, stream);
            configuration.installInterpolator(context.getAllLookups(), Collections.emptyList());
            this.config(configuration);
            return this.build();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public PropifyProperties build() {
        if (this.config == null) {
            throw new IllegalStateException("Configuration ");
        }
        PropifyProperties properties = new PropifyProperties();
        Iterator<String> keys = config.getKeys();
        while (keys.hasNext()) {
            String key = keys.next();
            handleKeyValue(this.config, properties, key);
        }
        return properties;
    }

    private void handleKeyValue(Configuration config, PropifyProperties properties, String key) {
        Pattern pattern = Pattern.compile("(?<key>.*?)(\\((?<type>[^)]+)\\))?");
        Matcher matcher = pattern.matcher(key);
        //noinspection ResultOfMethodCallIgnored
        matcher.matches();
        String keyName = matcher.group("key");
        String typeName = matcher.group("type");

        Object value;
        if (typeName != null) {
            value = convertToType(config, typeName, key);
        } else {
            value = config.getProperty(key);
            if(value instanceof Map) {
                PropifyProperties subProperties = new PropifyProperties();
                Configuration subConfig = new MapConfiguration((Map)value);
                ((Map<String, Object>) value).forEach((subKey, _value) -> {
                    handleKeyValue(subConfig, subProperties, subKey);
                });
                value = subProperties;
            } else if(value instanceof List) {
                value = ((List<Object>) value).stream().map(it -> {
                    if(it instanceof Map) {
                        PropifyProperties subProperties = new PropifyProperties();
                        Configuration subConfig = new MapConfiguration((Map)it);
                        ((Map<String, ?>) it).forEach((subKey, _value) -> {
                            handleKeyValue(subConfig, subProperties, subKey);
                        });
                        return subProperties;
                    }
                    return it;
                }).collect(Collectors.toList());
            } else  if (value instanceof String) {
                value = config.getString(key);
            }
        }
        this.storeValue(properties, keyName, value);
    }


    private static final Pattern GENERIC_TYPE_PATTERN = Pattern.compile("([^<>]+)(?:<(.+)>)?");

    private Object convertToType(Configuration config, String typeName, String key) {
        try {
            TypeInfo typeInfo = parseType(typeName);
            return convertToTypeInternal(config, typeInfo, key);
        } catch (Exception e) {
            throw new PropifyTypeConversionException("Failed to convert property '" + key + "' to type '" + typeName + "'", e);
        }
    }

    private TypeInfo parseType(String typeName) {
        Matcher matcher = GENERIC_TYPE_PATTERN.matcher(typeName);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid type format: " + typeName);
        }

        String rawTypeString = matcher.group(1).trim();
        String genericPart = matcher.group(2);

        List<TypeInfo> typeParameters = new ArrayList<>();
        if (genericPart != null) {
            int depth = 0;
            StringBuilder current = new StringBuilder();

            for (char c : genericPart.toCharArray()) {
                if (c == '<') depth++;
                else if (c == '>') depth--;
                else if (c == ',' && depth == 0) {
                    typeParameters.add(parseType(current.toString().trim()));
                    current = new StringBuilder();
                    continue;
                }
                current.append(c);
            }
            if (current.length() > 0) {
                typeParameters.add(parseType(current.toString().trim()));
            }
        }
        Class<?> rawType;
        try {
            rawType = this.getClassForType(rawTypeString);
        } catch (ClassNotFoundException e) {
            rawType = String.class;
        }
        return new TypeInfo(rawType, typeParameters);
    }

    private Object convertToTypeInternal(Configuration config, TypeInfo typeInfo, String key) throws ClassNotFoundException {
        Class<?> rawType = typeInfo.getRawType();
        List<TypeInfo> typeParameters = typeInfo.getTypeParameters();

        if (rawType.isPrimitive() || this.isPrimitiveWrapper(rawType)) {
            return this.convertPrimitive(config, rawType, key);
        }
        if (rawType == String.class) {
            return config.getString(key);
        } else if (rawType == BigDecimal.class) {
            return config.getBigDecimal(key);
        } else if(rawType == BigInteger.class) {
            return config.getBigInteger(key);
        }
        if (isTemporal(rawType)) {
            return this.convertTemporal(rawType, config.getString(key));
        }

        if (rawType.isAssignableFrom(List.class)) {
            return convertToList(typeParameters.get(0), key);
        } else if (rawType.isAssignableFrom(Set.class)) {
            return new HashSet<>(convertToList(typeParameters.get(0), key));
        } else if (rawType.isAssignableFrom(Map.class)) {
            convertToMap(typeParameters.get(0), typeParameters.get(1), key);
        } else if (rawType.isArray()) {
            return convertToArray(new TypeInfo(rawType.getComponentType(), Collections.emptyList()), key);
        }

        return config.get(rawType, key);
    }

    private boolean isPrimitiveWrapper(Class<?> cls) {
        return cls == Boolean.class || cls == Byte.class || cls == Character.class ||
                cls == Short.class || cls == Integer.class || cls == Long.class ||
                cls == Float.class || cls == Double.class;
    }


    private Object convertPrimitive(Configuration config, Class<?> cls, String key) {

        if (cls == boolean.class || cls == Boolean.class) {
            return config.getBoolean(key);
        }
        if (cls == byte.class || cls == Byte.class) {
            return config.getByte(key);
        }
        if (cls == char.class || cls == Character.class) {
            return config.getString(key).charAt(0);
        }
        if (cls == short.class || cls == Short.class) {
            return config.getShort(key);
        }
        if (cls == int.class || cls == Integer.class) {
            return config.getInt(key);
        }
        if (cls == long.class || cls == Long.class) {
            return config.getLong(key);
        }

        if (cls == float.class || cls == Float.class) {
            return config.getFloat(key);
        }
        if (cls == double.class || cls == Double.class) {
            return config.getDouble(key);
        }

        throw new IllegalArgumentException("Unsupported primitive type: " + cls);
    }

    private boolean isTemporal(Class<?> cls) {
        return java.time.temporal.Temporal.class.isAssignableFrom(cls) ||
                java.util.Date.class.isAssignableFrom(cls) ||
                java.time.Instant.class.isAssignableFrom(cls) ||
                java.time.LocalDate.class.isAssignableFrom(cls) ||
                java.time.LocalTime.class.isAssignableFrom(cls) ||
                java.time.LocalDateTime.class.isAssignableFrom(cls) ||
                java.time.Duration.class.isAssignableFrom(cls) ||
                java.time.Period.class.isAssignableFrom(cls);
    }

    private Object convertTemporal(Class<?> cls, String value) {
        if (value == null) return null;
        try {
            if (cls == java.time.LocalDate.class) {
                return java.time.LocalDate.parse(value);
            }
            if (cls == java.time.LocalTime.class) {
                return java.time.LocalTime.parse(value);
            }
            if (cls == java.time.LocalDateTime.class) {
                return java.time.LocalDateTime.parse(value);
            }
            if (cls == java.time.Instant.class) {
                return java.time.Instant.parse(value);
            }
            if (cls == java.time.Duration.class) {
                return java.time.Duration.parse(value);
            }
            if (cls == java.time.Period.class) {
                return java.time.Period.parse(value);
            }
            if (cls == java.util.Date.class) {
                try {
                    return Date.from(Instant.parse(value));
                } catch (Exception e) {
                    return new java.util.Date(Long.parseLong(value));
                }
            }
        } catch (Exception e) {
            throw new PropifyTypeConversionException("Failed to parse temporal value: " + value, e);
        }
        throw new IllegalArgumentException("Unsupported temporal type: " + cls);
    }

    private List<?> convertToList(TypeInfo elementType, String key) throws ClassNotFoundException {
        List<Object> result = new ArrayList<>();
        List<?> values = config.getList(key);

        if (values != null) {
            for (Object value : values) {
                result.add(convertSingleValue(value, elementType));
            }
        }

        return result;
    }

    private Map<?, ?> convertToMap(TypeInfo keyType, TypeInfo valueType, String key) throws ClassNotFoundException {
        Map<Object, Object> result = new HashMap<>();
        Configuration subset = config.subset(key);

        for (Iterator<String> it = subset.getKeys(); it.hasNext(); ) {
            String mapKey = it.next();
            Object value = subset.getProperty(mapKey);

            Object convertedKey = convertSingleValue(mapKey, keyType);
            Object convertedValue = convertSingleValue(value, valueType);

            result.put(convertedKey, convertedValue);
        }

        return result;
    }

    private Object[] convertToArray(TypeInfo elementType, String key) throws ClassNotFoundException {
        List<?> list = convertToList(elementType, key);
        return list.toArray();
    }

    private Object convertSingleValue(Object value, TypeInfo typeInfo) throws ClassNotFoundException {
        if (value == null) {
            return null;
        }
        Class<?> targetClass = typeInfo.getRawType();

        if (targetClass.isAssignableFrom(Map.class)) {
            Map<Object, Object> convertedMap = new HashMap<>();
            ((Map<?, ?>)value).forEach((key2, value2) -> {
                Object convertedValue;
                try {
                    convertedValue = convertSingleValue(value2, typeInfo.getTypeParameters().get(1));
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
                convertedMap.put(key2, convertedValue);
            });
            return convertedMap;
        } else if(targetClass.isAssignableFrom(List.class)) {
            return ((List<?>)value).stream().map(value2 -> {
                try {
                    return convertSingleValue(value2, typeInfo.getTypeParameters().get(0));
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }).collect(Collectors.toList());
        } else if(targetClass.isAssignableFrom(Set.class)) {
            return ((Set<?>)value).stream().map(value2 -> {
                try {
                    return convertSingleValue(value2, typeInfo.getTypeParameters().get(0));
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }).collect(Collectors.toSet());
        }

        // If the value is already of the correct type, return it
        if (targetClass.isInstance(value)) {
            return value;
        }

        // Convert the value using a temporary key
        String tempKey = "temp_conversion_key";
        Configuration tempConfig = new org.apache.commons.configuration2.MapConfiguration(
                Collections.singletonMap(tempKey, value));

        PropifyPropertiesBuilder tempBuilder = new PropifyPropertiesBuilder().config(tempConfig);
        return tempBuilder.convertToTypeInternal(tempConfig, typeInfo, tempKey);
    }

    private Class<?> getClassForType(String typeName) throws ClassNotFoundException {
        // Handle primitive types
        Class<?> primitiveType = getPrimitiveType(typeName);
        if (primitiveType != null) {
            return primitiveType;
        }

        // Handle array types
        if (typeName.endsWith("[]")) {
            String componentTypeName = typeName.substring(0, typeName.length() - 2);
            Class<?> componentType = getClassForType(componentTypeName);
            return java.lang.reflect.Array.newInstance(componentType, 0).getClass();
        }

        // Handle fully qualified class names
        try {
            return Class.forName(typeName);
        } catch (ClassNotFoundException e) {
            // Try common packages if not found
            String[] commonPackages = {
                    "java.lang.",
                    "java.util.",
                    "java.math.",
                    "java.time."
            };

            for (String pkg : commonPackages) {
                try {
                    return Class.forName(pkg + typeName);
                } catch (ClassNotFoundException ignored) {
                    // Continue searching
                }
            }
            throw e;
        }
    }

    private Class<?> getPrimitiveType(String typeName) {
        switch (typeName.toLowerCase()) {
            case "byte":
                return Byte.class;
            case "short":
                return Short.class;
            case "int":
            case "integer":
                return Integer.class;
            case "long":
                return Long.class;
            case "float":
                return Float.class;
            case "double":
                return Double.class;
            case "boolean":
                return Boolean.class;
            case "char":
            case "character":
                return Character.class;
            case "string":
                return String.class;
            case "biginteger":
                return java.math.BigInteger.class;
            case "bigdecimal":
                return java.math.BigDecimal.class;
            case "date":
                return java.util.Date.class;
            case "localdate":
                return java.time.LocalDate.class;
            case "localtime":
                return java.time.LocalTime.class;
            case "localdatetime":
                return java.time.LocalDateTime.class;
            case "instant":
                return java.time.Instant.class;
            case "duration":
                return java.time.Duration.class;
            case "period":
                return java.time.Period.class;
            default:
                return null;
        }
    }

    private void storeValue(PropifyProperties properties, String key, Object value) {
        String[] keyPath = key.split("\\s*\\.\\s*");
        if (keyPath.length > 1) {
            PropifyProperties current = properties;
            for (int i = 0; i < keyPath.length - 1; i++) {
                String pathKey = keyPath[i].trim();
                Object existing = current.get(pathKey);
                if (existing instanceof PropifyProperties) {
                    current = (PropifyProperties) existing;
                } else {
                    current = current.createNested(pathKey);
                }
            }
            current.put(keyPath[keyPath.length - 1].trim(), value);
        } else {
            properties.put(key.trim(), value);
        }
    }

    private static class TypeInfo {
        private final Class<?> rawType;
        private final List<TypeInfo> typeParameters;

        public TypeInfo(Class<?> rawType, List<TypeInfo> typeParameters) {
            this.rawType = rawType;
            this.typeParameters = typeParameters;
        }

        public Class<?> getRawType() {
            return rawType;
        }

        public List<TypeInfo> getTypeParameters() {
            return typeParameters;
        }
    }

    public static class PropifyTypeConversionException extends RuntimeException {
        public PropifyTypeConversionException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
