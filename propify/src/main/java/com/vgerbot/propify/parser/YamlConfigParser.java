package com.vgerbot.propify.parser;

import com.vgerbot.propify.PropifyConfigParser;
import com.vgerbot.propify.PropifyContext;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.YAMLConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.interpol.ConfigurationInterpolator;
import org.apache.commons.configuration2.interpol.DefaultLookups;
import org.apache.commons.configuration2.io.FileHandler;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class YamlConfigParser implements PropifyConfigParser {
    @Override
    public Configuration parse(PropifyContext context, InputStream stream) throws IOException {
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

            return config;
        } catch (ConfigurationException e) {
            throw new IOException("Failed to parse YAML configuration: " + e.getMessage(), e);
        }
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
