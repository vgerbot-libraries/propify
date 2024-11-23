package com.vgerbot.propify.parser;

import com.vgerbot.propify.PropifyConfigParser;
import com.vgerbot.propify.PropifyProperties;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Properties;

public class PropertiesParser implements PropifyConfigParser  {
    @Override
    public PropifyProperties parse(InputStream stream) throws IOException {
        Properties properties = new Properties();
        properties.load(stream);
        PropifyProperties propifyProperties = new PropifyProperties();
        properties.forEach((key, value) -> {
            String[] keyPath = key.toString().split("\\s*\\.\\s*");
            System.out.println(Arrays.toString(keyPath));
            if (keyPath.length > 1) {
                PropifyProperties subProperties = propifyProperties;
                for (int i = 0; i < keyPath.length - 1; i++) {
                    PropifyProperties newSubProperties = (PropifyProperties) subProperties.computeIfAbsent(keyPath[i], k -> new PropifyProperties());
                    subProperties = newSubProperties;
                }
                subProperties.put(keyPath[keyPath.length - 1], value);
            } else {
                propifyProperties.put(key.toString(), value);
            }
        });
        return propifyProperties;
    }

    @Override
    public Boolean accept(String mediaType) {
        return "text/x-java-properties".equals(mediaType.toLowerCase());
    }

}
