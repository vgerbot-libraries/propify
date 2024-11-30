package com.vgerbot.propify;

import java.util.HashMap;

/**
 * Properties map that parses values to native Java types.
 *
 * <p>This class is a thin wrapper around {@link java.util.HashMap} that overrides
 * the {@link java.util.Map#put(Object, Object)} method to parse values to native
 * Java types using {@link Utils#parseValue(Object)} when autoTypeConversion is enabled.
 */
public class PropifyProperties extends HashMap<String, Object> {
    private final boolean autoTypeConversion;

    public PropifyProperties(boolean autoTypeConversion) {
        this.autoTypeConversion = autoTypeConversion;
    }

    public PropifyProperties() {
        this(true); // Default to true for backward compatibility
    }

    @Override
    public Object put(String key, Object value) {
        if (value instanceof PropifyProperties) {
            return super.put(key, value);
        }
        return super.put(key, autoTypeConversion ? Utils.parseValue(value) : value);
    }

    /**
     * Creates a new nested PropifyProperties instance with the same autoTypeConversion setting.
     *
     * @return a new PropifyProperties instance
     */
    public PropifyProperties createNested() {
        return new PropifyProperties(this.autoTypeConversion);
    }
}
