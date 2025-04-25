package com.vgerbot.propify.parser;

import com.vgerbot.propify.core.PropifyConfigParser;
import com.vgerbot.propify.core.PropifyContext;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.INIConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.io.FileHandler;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class IniConfigParser implements PropifyConfigParser {
    @Override
    public Configuration parse(PropifyContext context, InputStream stream) throws IOException {
        FileBasedConfiguration configuration = new INIConfiguration();

        FileHandler handler = new FileHandler(configuration);
        handler.setEncoding(StandardCharsets.UTF_8.name());
        try {
            handler.load(stream);
        } catch (ConfigurationException e) {
            throw new IOException(e);
        }
        return configuration;
    }

    @Override
    public Boolean accept(PropifyContext context) {
        String mediaType = context.getMediaType();
        String location = context.getLocation().trim();
        if (!location.isEmpty()) {
            return location.endsWith(".ini");
        }
        if (mediaType == null) {
            return false;
        }
        String type = mediaType.toLowerCase();
        return "text/ini".equals(type) ||
                "text/plain".equals(type);
    }
}
