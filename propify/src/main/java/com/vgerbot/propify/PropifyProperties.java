package com.vgerbot.propify;

import java.util.HashMap;

/**
 * A specialized HashMap implementation for managing hierarchical configuration properties.
 * This class extends HashMap to store property key-value pairs where values can be of any type (Object).
 * It provides functionality to create and manage nested property structures, allowing for
 * hierarchical configuration data representation.
 *
 * <p>Example usage:
 * <pre>
 * PropifyProperties props = new PropifyProperties();
 * props.put("simple.key", "value");
 * PropifyProperties nested = props.createNested("parent");
 * nested.put("child", "value");
 * </pre>
 */
public class PropifyProperties extends HashMap<String, Object> {

    /**
     * Creates a new nested PropifyProperties instance and associates it with the specified key
     * in the current properties map.
     *
     * @param key The key under which to store the nested properties
     * @return A new PropifyProperties instance that has been added to the current map
     *         under the specified key
     */
    public PropifyProperties createNested(String key) {
        PropifyProperties nested = new PropifyProperties();
        this.put(key, nested);
        return nested;
    }
}
