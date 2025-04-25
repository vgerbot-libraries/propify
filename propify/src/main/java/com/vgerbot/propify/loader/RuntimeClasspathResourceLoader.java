package com.vgerbot.propify.loader;

import com.vgerbot.propify.core.ResourceLoader;

import java.io.IOException;
import java.io.InputStream;

public class RuntimeClasspathResourceLoader implements ResourceLoader  {
    private static final String CLASSPATH_PREFIX = "classpath:";
    @Override
    public boolean accept(String location) {
        if (location == null) {
            return false;
        }
        return location.startsWith(CLASSPATH_PREFIX);
    }

    @Override
    public InputStream load(String location) throws IOException {
        if (location == null) {
            throw new IllegalArgumentException("Location cannot be null");
        }
        if (!location.startsWith(CLASSPATH_PREFIX)) {
            throw new IllegalArgumentException("Location must start with 'classpath:': " + location);
        }
        String filePath = location.substring(CLASSPATH_PREFIX.length()).trim();
        if (filePath.isEmpty()) {
            throw new IllegalArgumentException("Classpath resource path cannot be empty");
        }

        return Thread.currentThread().getContextClassLoader().getResourceAsStream(filePath);
    }
}
