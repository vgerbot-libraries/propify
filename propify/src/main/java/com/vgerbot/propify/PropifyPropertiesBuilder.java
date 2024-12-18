package com.vgerbot.propify;

import org.apache.commons.configuration2.Configuration;

import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PropifyPropertiesBuilder {

    private Configuration config;

    public PropifyPropertiesBuilder config(Configuration config) {
        this.config = config;
        return this;
    }

    public PropifyProperties build() {
        if (this.config == null) {
            throw new IllegalStateException("Configuration ");
        }
        PropifyProperties properties = new PropifyProperties();
        Iterator<String> keys = config.getKeys();
        Pattern pattern = Pattern.compile("(?<key>.*?)(<(?<type>[^>]+)>)?");
        while (keys.hasNext()) {
            String key = keys.next();
            Matcher matcher = pattern.matcher(key);
            matcher.matches();
            String keyName = matcher.group("key");
            String typeName = matcher.group("type");

            Object value = null;
            if (typeName != null) {
                value = convertToType(typeName, config.getString(key));
            } else {
                value = config.getProperty(key);
                value = config.getInterpolator().interpolate(value);
            }
            this.storeValue(properties, keyName, value);
        }
        return properties;
    }

    private Object convertToType(String typeName, String value) {

        Class<? extends Object> cls = null;
        switch (typeName) {
            case "byte":
                cls = Byte.class;
                break;
            case "short":
                cls = Short.class;
                break;
            case "int":
                cls = Integer.class;
                break;
            case "long":
                cls = Long.class;
                break;
            case "float":
                cls = Float.class;
                break;
            case "double":
                cls = Double.class;
                break;
            case "boolean":
                cls = Boolean.class;
                break;
        }
        if (cls != null) {
            return config.get(cls, value);
        }

        try {
            cls = Class.forName(typeName);
            return config.get(cls, value);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
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

    public static void main(String[] args) {
        Pattern pattern = Pattern.compile("(?<key>.*?)(<(?<type>[^>]+)>)?");
        Matcher matcher = pattern.matcher("");
        System.out.println(matcher.matches());
        System.out.println(matcher.groupCount());
        System.out.println(matcher.group("key"));
        System.out.println(matcher.group("type"));
    }
}
