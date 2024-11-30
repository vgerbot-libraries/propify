package com.vgerbot.propify.parser;

import com.vgerbot.propify.PropifyConfigParser;
import com.vgerbot.propify.PropifyContext;
import com.vgerbot.propify.PropifyProperties;

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

        PropifyProperties propifyProperties = new PropifyProperties(context.isAutoTypeConversion());
        properties.forEach((key, value) -> {
            String[] keyPath = key.toString().split("\\s*\\.\\s*");
            String strValue = value.toString().trim();

            // Handle nested properties
            if (keyPath.length > 1) {
                PropifyProperties current = propifyProperties;
                for (int i = 0; i < keyPath.length - 1; i++) {
                    String pathKey = keyPath[i].trim();
                    Object existing = current.get(pathKey);
                    if (existing instanceof PropifyProperties) {
                        current = (PropifyProperties) existing;
                    } else {
                        PropifyProperties newProps = current.createNested();
                        current.put(pathKey, newProps);
                        current = newProps;
                    }
                }
                current.put(keyPath[keyPath.length - 1].trim(), strValue);
            } else {
                propifyProperties.put(key.toString().trim(), strValue);
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
