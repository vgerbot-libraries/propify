package com.vgerbot.propify.parser;

import com.vgerbot.propify.PropifyContext;
import com.vgerbot.propify.PropifyConfigParser;
import com.vgerbot.propify.PropifyProperties;
import com.vgerbot.propify.Utils;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.convert.DefaultListDelimiterHandler;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.io.FileHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

public class PropertiesConfigParser implements PropifyConfigParser {
    @Override
    public PropifyProperties parse(PropifyContext context, InputStream stream) throws IOException {
        if (stream == null) {
            throw new IOException("Input stream cannot be null");
        }

        try {
            // Create and configure the Properties Configuration
            PropertiesConfiguration config = new PropertiesConfiguration();
            config.setListDelimiterHandler(new DefaultListDelimiterHandler(','));

            // Use FileHandler to load from InputStream
            FileHandler handler = new FileHandler(config);
            handler.setEncoding(StandardCharsets.UTF_8.name());
            handler.load(stream);

            PropifyProperties propifyProperties = new PropifyProperties();

            // Convert configuration to PropifyProperties
            Iterator<String> keys = config.getKeys();
            while (keys.hasNext()) {
                String key = keys.next();
                Object value = config.getProperty(key);
                
                // Handle value conversion if enabled
                if (context.isAutoTypeConversion() && value instanceof String) {
                    value = Utils.convertValue((String) value);
                }

                // Handle nested properties
                String[] keyPath = key.split("\\s*\\.\\s*");
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
                    current.put(keyPath[keyPath.length - 1].trim(), value);
                } else {
                    propifyProperties.put(key.trim(), value);
                }
            }

            return propifyProperties;
        } catch (ConfigurationException e) {
            throw new IOException("Failed to parse properties configuration: " + e.getMessage(), e);
        }
    }

    @Override
    public Boolean accept(PropifyContext context) {
        String mediaType = context.getMediaType();
        if (mediaType == null || mediaType.trim().isEmpty()) {
            String location = context.getLocation().trim();
            if(!location.isEmpty()) {
                return location.endsWith(".properties");
            }
            return false;
        }
        String type = mediaType.toLowerCase();
        return "application/java-properties".equals(type) ||
               "application/x-java-properties".equals(type) ||
               "text/java-properties".equals(type) ||
               "text/x-java-properties".equals(type);
    }
}
