package com.vgerbot.propify.parser;

import com.vgerbot.propify.PropifyConfigParser;
import com.vgerbot.propify.PropifyContext;
import com.vgerbot.propify.PropifyProperties;
import com.vgerbot.propify.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class PropertiesParser implements PropifyConfigParser {
    @Override
    public PropifyProperties parse(PropifyContext context, InputStream stream) throws IOException {
        if (stream == null) {
            throw new IOException("Input stream cannot be null");
        }

        Properties properties = new Properties();
        try (InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            properties.load(reader);
        } catch (IllegalArgumentException e) {
            throw new IOException("Invalid properties format: " + e.getMessage(), e);
        }

        PropifyProperties propifyProperties = new PropifyProperties();
        properties.forEach((key, value) -> {
            String[] keyPath = key.toString().split("\\s*\\.\\s*");
            String strValue = value.toString().trim();
            Object convertedValue = context.isAutoTypeConversion() ? Utils.convertValue(strValue) : strValue;

            // Handle nested properties
            if (keyPath.length > 1) {
                PropifyProperties current = propifyProperties;
                for (int i = 0; i < keyPath.length - 1; i++) {
                    String pathKey = keyPath[i].trim();
                    Object existing = current.get(pathKey);
                    if (existing instanceof PropifyProperties) {
                        current = (PropifyProperties) existing;
                    } else {
                        current = current.createNested(pathKey);
                    }
                }
                current.put(keyPath[keyPath.length - 1].trim(), convertedValue);
            } else {
                propifyProperties.put(key.toString().trim(), convertedValue);
            }
        });

        return propifyProperties;
    }

    @Override
    public Boolean accept(String mediaType) {
        if (mediaType == null) {
            return false;
        }
        String type = mediaType.toLowerCase();
        return "application/java-properties".equals(type) ||
               "application/x-java-properties".equals(type) ||
               "text/java-properties".equals(type) ||
               "text/x-java-properties".equals(type);
    }
}
