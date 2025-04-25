package com.vgerbot.propify.common;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.configuration2.MapConfiguration;
import org.apache.commons.configuration2.tree.DefaultExpressionEngine;
import org.apache.commons.configuration2.tree.DefaultExpressionEngineSymbols;

/**
 * A configuration class that extends MapConfiguration to provide support for accessing
 * nested properties using dot notation and array/list indexing.
 * <p>
 * This implementation allows for traversing complex hierarchical data structures such as
 * nested maps, lists, and arrays in a convenient, intuitive manner.
 * </p>
 * 
 * <h3>Examples:</h3>
 * 
 * <h4>Accessing nested maps:</h4>
 * <pre>
 * // For a map structure: {"config": {"server": {"port": 8080}}}
 * config.getInt("config.server.port") // returns 8080
 * </pre>
 * 
 * <h4>Accessing arrays and lists:</h4>
 * <pre>
 * // For a map structure: {"foo": {"bar": [1, 2, 3]}}
 * config.getInt("foo.bar[0]") // returns 1
 * config.getInt("foo.bar[1]") // returns 2
 * </pre>
 * 
 * <h4>Accessing nested objects within collections:</h4>
 * <pre>
 * // For a map structure: {"items": [{"id": 1, "name": "first"}, {"id": 2, "name": "second"}]}
 * config.getInt("items[0].id")     // returns 1
 * config.getString("items[1].name") // returns "second"
 * </pre>
 * 
 * <p>
 * The implementation uses Apache Commons Configuration2's DefaultExpressionEngine internally
 * with custom symbols to properly interpret the dotted notation and array indices.
 * </p>
 * 
 * @see org.apache.commons.configuration2.MapConfiguration
 * @see org.apache.commons.configuration2.tree.DefaultExpressionEngine
 */
public class FlatDottedMapConfiguration extends MapConfiguration {
    
    private final DefaultExpressionEngine expressionEngine;
    
    /**
     * Creates a new FlatDottedMapConfiguration with the specified map.
     * 
     * @param map The map containing configuration properties
     */
    public FlatDottedMapConfiguration(Map<String, ?> map) {
        super(map);
        this.expressionEngine = createExpressionEngine();
    }

    /**
     * Creates a new FlatDottedMapConfiguration with the specified Properties.
     * 
     * @param props The Properties containing configuration properties
     */
    private FlatDottedMapConfiguration(Properties props) {
        super(props);
        this.expressionEngine = createExpressionEngine();
    }
    
    /**
     * Creates a custom DefaultExpressionEngine with symbols that support
     * the dotted notation and array indices.
     * 
     * @return The configured expression engine
     */
    private DefaultExpressionEngine createExpressionEngine() {
        DefaultExpressionEngineSymbols symbols = new DefaultExpressionEngineSymbols.Builder(
                DefaultExpressionEngineSymbols.DEFAULT_SYMBOLS)
                .setPropertyDelimiter(".")
                .setIndexStart("[")
                .setIndexEnd("]")
                .create();
        return new DefaultExpressionEngine(symbols);
    }

    @Override
    protected Object getPropertyInternal(String key) {
        // Try direct access first (for simple non-nested properties)
        if (super.containsKeyInternal(key)) {
            return super.getPropertyInternal(key);
        }

        try {
            // Parse the key into parts
            String[] parts = parseKey(key);
            if (parts.length == 0) {
                return null;
            }
            
            // Start with the root object
            Object currentValue = super.getPropertyInternal(parts[0]);
            
            // Navigate through the object hierarchy
            for (int i = 1; i < parts.length && currentValue != null; i++) {
                currentValue = getNestedProperty(currentValue, parts[i]);
            }
            
            return currentValue;
        } catch (Exception e) {
            // If any error occurs during navigation, return null
            return null;
        }
    }
    
    /**
     * Parses a property key into its component parts.
     * 
     * @param key The property key to parse
     * @return Array of key parts
     */
    private String[] parseKey(String key) {
        if (key == null || key.isEmpty()) {
            return new String[0];
        }
        
        List<String> parts = new ArrayList<>();
        StringBuilder currentPart = new StringBuilder();
        
        final int length = key.length();

        for (int i = 0; i < length; i++) {
            char c = key.charAt(i);
            
            if (c == '.') {
                // Add the current part if not empty and reset
                if (currentPart.length() > 0) {
                    parts.add(currentPart.toString());
                    currentPart = new StringBuilder();
                }
            } else if (c == '[') {
                // Add the current part if not empty
                if (currentPart.length() > 0) {
                    parts.add(currentPart.toString());
                    currentPart = new StringBuilder();
                }
                
                // Start collecting the index part
                currentPart.append('[');
            } else if (c == ']') {
                // Complete the index part
                currentPart.append(']');
                parts.add(currentPart.toString());
                currentPart = new StringBuilder();
            } else {
                currentPart.append(c);
            }
        }
        
        // Add any remaining part
        if (currentPart.length() > 0) {
            parts.add(currentPart.toString());
        }
        
        return parts.toArray(new String[0]);
    }
    /**
     * Gets a nested property from an object.
     * 
     * @param container The container object (Map, List, or Array)
     * @param propertyKey The property key to access
     * @return The value of the property, or null if not found
     */
    private Object getNestedProperty(Object container, String propertyKey) {
        if (container == null || propertyKey == null || propertyKey.isEmpty()) {
            return null;
        }
        
        // Handle array/list index notation
        if (propertyKey.startsWith("[") && propertyKey.endsWith("]")) {
            final int openBracketIndex = propertyKey.indexOf('[');
            final int closeBracketIndex = propertyKey.indexOf(']');
            if (closeBracketIndex <= 0 || openBracketIndex < 0) {
                return null;
            }
            
            try {
                final int index = Integer.parseInt(propertyKey.substring(openBracketIndex + 1, closeBracketIndex));
                
                if (container instanceof List) {
                    List<?> list = (List<?>) container;
                    if (index >= 0 && index < list.size()) {
                        return list.get(index);
                    }
                } else if (container.getClass().isArray()) {
                    int length = Array.getLength(container);
                    if (index >= 0 && index < length) {
                        return Array.get(container, index);
                    }
                }
            } catch (NumberFormatException e) {
                // Invalid index format
                return null;
            }
        } else if (container instanceof Map) {
            // Regular map property access
            return ((Map<?, ?>) container).get(propertyKey);
        } else {
            try {
                Field field = container.getClass().getDeclaredField(propertyKey);
                field.setAccessible(true);
                return field.get(container);
            } catch (Exception e) {
                return null;
            }
        }
        
        return null;
    }
}
