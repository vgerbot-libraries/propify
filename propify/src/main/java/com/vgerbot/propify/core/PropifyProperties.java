package com.vgerbot.propify.core;

import com.vgerbot.propify.PropifyProcessor;

import java.util.HashMap;

/**
 * A specialized HashMap implementation for managing hierarchical configuration properties.
 * 
 * <p>This class extends HashMap to provide a type-safe and hierarchical structure for
 * configuration properties. It supports:
 * <ul>
 *   <li>Nested property structures using dot notation</li>
 *   <li>Type-safe value storage and retrieval</li>
 *   <li>Hierarchical property organization</li>
 *   <li>Path tracking for nested properties</li>
 * </ul>
 *
 * <p>The class maintains an internal path tracking mechanism that helps in:
 * <ul>
 *   <li>Building fully qualified property names</li>
 *   <li>Managing nested property relationships</li>
 *   <li>Providing context for error reporting</li>
 * </ul>
 *
 * <p>Example usage:
 * <pre>
 * PropifyProperties props = new PropifyProperties();
 * 
 * // Simple key-value pairs
 * props.put("server.port", 8080);
 * props.put("app.name", "MyApp");
 * 
 * // Nested properties
 * PropifyProperties dbProps = props.createNested("database");
 * dbProps.put("url", "jdbc:mysql://localhost:3306/mydb");
 * dbProps.put("username", "admin");
 * 
 * // Accessing nested properties
 * // The above creates a structure equivalent to:
 * // database.url=jdbc:mysql://localhost:3306/mydb
 * // database.username=admin
 * </pre>
 *
 * @see PropifyConfigParser For classes that populate these properties
 * @see PropifyProcessor For how these properties are used in code generation
 * @since 1.0.0
 */
public class PropifyProperties extends HashMap<String, Object> {
    
    /**
     * The dot-notation path to this properties instance in the hierarchy.
     * Empty string for root properties, contains parent paths for nested instances.
     */
    private final String keyPath;

    /**
     * Creates a new root PropifyProperties instance.
     * 
     * <p>This constructor creates a properties instance with an empty key path,
     * indicating it is the root of a properties hierarchy.
     */
    public PropifyProperties() {
        this("");
    }

    /**
     * Creates a new PropifyProperties instance with the specified key path.
     * 
     * <p>This constructor is used internally to create nested property instances
     * while maintaining the full path hierarchy.
     *
     * @param keyPath the dot-notation path to this properties instance
     */
    private PropifyProperties(String keyPath) {
        this.keyPath = keyPath;
    }

    /**
     * Creates a new nested PropifyProperties instance under the specified key.
     * 
     * <p>This method:
     * <ul>
     *   <li>Creates a new PropifyProperties instance with an updated key path</li>
     *   <li>Associates the new instance with the specified key in this map</li>
     *   <li>Maintains the hierarchical relationship between properties</li>
     * </ul>
     *
     * <p>The new instance's key path will be this instance's key path plus the
     * specified key, joined with a dot. For example, if this instance's key path
     * is "config" and the specified key is "database", the new instance's key path
     * will be "config.database".
     *
     * @param key the key under which to create the nested properties
     * @return the newly created nested PropifyProperties instance
     * @throws IllegalArgumentException if the key is null or empty
     */
    public PropifyProperties createNested(String key) {
        PropifyProperties nested = new PropifyProperties(this.keyPath + "." + key);
        this.put(key, nested);
        return nested;
    }
}
