package com.vgerbot.propify.parser;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.vgerbot.propify.common.FlatDottedMapConfiguration;
import com.vgerbot.propify.core.PropifyConfigParser;
import com.vgerbot.propify.core.PropifyContext;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.MapConfiguration;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;

public class YamlConfigParser implements PropifyConfigParser {
    @Override
    public Configuration parse(PropifyContext context, InputStream stream) throws IOException {
        if (stream == null) {
            throw new IllegalArgumentException("Input stream cannot be null");
        }
        if (context == null) {
            throw new IllegalArgumentException("PropifyContext cannot be null");
        }
        if (stream.available() == 0) {
            return new MapConfiguration(Collections.emptyMap());
        }
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        Map map = mapper.readValue(stream, Map.class);
        MapConfiguration configuration = new FlatDottedMapConfiguration(map);
        return configuration;
    }


    @Override
    public Boolean accept(PropifyContext context) {
        String mediaType = context.getMediaType();
        if (mediaType == null || mediaType.trim().isEmpty()) {

            String location = context.getLocation();
            location = location == null ? null : location.trim();
            if(location != null && !location.isEmpty()) {
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
