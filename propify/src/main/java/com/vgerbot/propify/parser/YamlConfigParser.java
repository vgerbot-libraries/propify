package com.vgerbot.propify.parser;

import com.vgerbot.propify.PropifyContext;
import com.vgerbot.propify.PropifyConfigParser;
import com.vgerbot.propify.PropifyProperties;
import com.vgerbot.propify.Utils;
import org.apache.commons.configuration2.YAMLConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.interpol.ConfigurationInterpolator;
import org.apache.commons.configuration2.interpol.DefaultLookups;
import org.apache.commons.configuration2.interpol.EnvironmentLookup;
import org.apache.commons.configuration2.interpol.SystemPropertiesLookup;
import org.apache.commons.configuration2.io.FileHandler;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class YamlConfigParser implements PropifyConfigParser {
    @Override
    public PropifyProperties parse(PropifyContext context, InputStream stream) throws IOException {
        if (stream == null) {
            throw new IOException("Input stream cannot be null");
        }

        try {
            // Create and configure YAML Configuration
            YAMLConfiguration config = new YAMLConfiguration();
            
            // Setup environment variable and system property interpolation
            ConfigurationInterpolator interpolator = config.getInterpolator();
            interpolator.registerLookup("env", DefaultLookups.ENVIRONMENT.getLookup());
            interpolator.registerLookup("sys", DefaultLookups.SYSTEM_PROPERTIES.getLookup());
            
            // Use FileHandler to load from InputStream
            FileHandler handler = new FileHandler(config);
            handler.setEncoding(StandardCharsets.UTF_8.name());
            handler.load(stream);

            PropifyProperties properties = new PropifyProperties();

            // Convert configuration to PropifyProperties
            Iterator<String> keys = config.getKeys();
            while (keys.hasNext()) {
                String key = keys.next();
                processKey(context, config, key, properties);
            }

            return properties;
        } catch (ConfigurationException e) {
            throw new IOException("Failed to parse YAML configuration: " + e.getMessage(), e);
        }
    }

    private void processKey(PropifyContext context, YAMLConfiguration config, String key, PropifyProperties properties) {
        Object value = config.getProperty(key);
        
        if (value == null) {
            return;
        }

        // Handle different value types
        if (value instanceof List) {
            processListValue(context, key, (List<?>) value, properties);
        } else if (value instanceof String && context.isAutoTypeConversion()) {
            // Handle string values with potential type conversion
            String strValue = (String) value;
            // Check if it's a duration string (e.g., "30m", "24h")
            if (strValue.matches("\\d+[smhd]")) {
                properties.put(key, strValue); // Keep duration strings as-is
            } else {
                properties.put(key, Utils.convertValue(strValue));
            }
        } else {
            properties.put(key, value);
        }
    }

    private void processListValue(PropifyContext context, String key, List<?> list, PropifyProperties properties) {
        List<Object> processedList = new ArrayList<>();
        
        for (Object item : list) {
            if (item instanceof String && context.isAutoTypeConversion()) {
                processedList.add(Utils.convertValue((String) item));
            } else {
                processedList.add(item);
            }
        }
        
        properties.put(key, processedList);
    }

    @Override
    public Boolean accept(PropifyContext context) {
        String mediaType = context.getMediaType();
        if (mediaType == null || mediaType.trim().isEmpty()) {
            String location = context.getLocation().trim();
            if(!location.isEmpty()) {
                return location.endsWith(".yml") || location.endsWith(".yaml");
            }
            return false;
        }
        String type = mediaType.toLowerCase();
        return "application/yaml".equals(type) || 
               "application/x-yaml".equals(type) ||
               "text/yaml".equals(type) ||
               "text/x-yaml".equals(type);
    }
}
