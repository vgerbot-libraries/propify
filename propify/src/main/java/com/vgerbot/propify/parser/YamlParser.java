package com.vgerbot.propify.parser;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.vgerbot.propify.PropifyConfigParser;
import com.vgerbot.propify.PropifyContext;
import com.vgerbot.propify.PropifyProperties;
import com.vgerbot.propify.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public class YamlParser implements PropifyConfigParser {
    private final ObjectMapper yamlMapper;

    public YamlParser() {
        this.yamlMapper = new ObjectMapper(new YAMLFactory());
    }

    @Override
    public PropifyProperties parse(PropifyContext context, InputStream stream) throws IOException {
        if (stream == null) {
            throw new IOException("Input stream cannot be null");
        }

        Map<String, Object> yamlMap;
        try {
            yamlMap = yamlMapper.readValue(stream, Map.class);
            if (yamlMap == null) {
                return new PropifyProperties();
            }
        } catch (JsonProcessingException e) {
            if (e.getMessage() != null && e.getMessage().contains("No content")) {
                return new PropifyProperties();
            }
            throw new IOException("Invalid YAML format: " + e.getMessage(), e);
        }

        PropifyProperties properties = new PropifyProperties();
        convertMapToProperties(context, yamlMap, properties);
        return properties;
    }

    @Override
    public Boolean accept(String mediaType) {
        if (mediaType == null) {
            return false;
        }
        String type = mediaType.toLowerCase();
        return "application/yaml".equals(type) || 
               "application/x-yaml".equals(type) ||
               "text/yaml".equals(type) ||
               "text/x-yaml".equals(type);
    }

    @SuppressWarnings("unchecked")
    private void convertMapToProperties(PropifyContext context,Map<String, Object> map, PropifyProperties properties) {
        if (map == null) {
            return;
        }

        map.forEach((key, value) -> {
            if (key == null) {
                return;
            }

            if (value instanceof Map) {
                PropifyProperties nestedProps = properties.createNested(key);
                convertMapToProperties(context, (Map<String, Object>) value, nestedProps);
            } else if (value instanceof String && context.isAutoTypeConversion()) {
                // Preserve lists as-is
                properties.put(key, Utils.convertValue(value));
            } else {
                properties.put(key, value);
            }
        });
    }
}
