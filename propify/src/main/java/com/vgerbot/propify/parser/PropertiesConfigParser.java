package com.vgerbot.propify.parser;

import com.vgerbot.propify.PropifyContext;
import com.vgerbot.propify.PropifyConfigParser;
import com.vgerbot.propify.PropifyProperties;
import com.vgerbot.propify.Utils;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.convert.DefaultListDelimiterHandler;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.interpol.ConfigurationInterpolator;
import org.apache.commons.configuration2.interpol.DefaultLookups;
import org.apache.commons.configuration2.io.FileHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

public class PropertiesConfigParser implements PropifyConfigParser {
    @Override
    public Configuration parse(PropifyContext context, InputStream stream) throws IOException {
        if (stream == null) {
            throw new IOException("Input stream cannot be null");
        }

        try {
            // Create and configure the Properties Configuration
            PropertiesConfiguration config = new PropertiesConfiguration();
            config.setListDelimiterHandler(new DefaultListDelimiterHandler(context.getListDelimiter()));

            // Use FileHandler to load from InputStream
            FileHandler handler = new FileHandler(config);
            handler.setEncoding(StandardCharsets.UTF_8.name());
            handler.load(stream);

            return config;
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
