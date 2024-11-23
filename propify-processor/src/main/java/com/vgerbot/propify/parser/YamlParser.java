package com.vgerbot.propify.parser;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.vgerbot.propify.PropifyConfigParser;
import com.vgerbot.propify.PropifyProperties;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public class YamlParser implements PropifyConfigParser {
    private final ObjectMapper yamlMapper;

    public YamlParser() {
        this.yamlMapper = new ObjectMapper(new YAMLFactory());
    }

    @Override
    public PropifyProperties parse(InputStream stream) throws IOException {
        Map<String, Object> yamlMap = yamlMapper.readValue(stream, Map.class);
        PropifyProperties properties = new PropifyProperties();
        convertMapToProperties(yamlMap, properties);
        return properties;
    }

    @Override
    public Boolean accept(String mediaType) {
        return "application/yaml".equals(mediaType.toLowerCase()) || 
               "application/x-yaml".equals(mediaType.toLowerCase()) ||
               "text/yaml".equals(mediaType.toLowerCase()) ||
               "text/x-yaml".equals(mediaType.toLowerCase());
    }

    private void convertMapToProperties(Map<String, Object> map, PropifyProperties properties) {
        map.forEach((key, value) -> {
            if (value instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> nestedMap = (Map<String, Object>) value;
                PropifyProperties nestedProps = new PropifyProperties();
                convertMapToProperties(nestedMap, nestedProps);
                properties.put(key, nestedProps);
            } else {
                properties.put(key, value);
            }
        });
    }
}
