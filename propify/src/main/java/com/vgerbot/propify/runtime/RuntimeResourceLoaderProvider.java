package com.vgerbot.propify.runtime;

import com.vgerbot.propify.ResourceLoader;
import com.vgerbot.propify.ResourceLoaderProvider;

public class RuntimeResourceLoaderProvider implements ResourceLoaderProvider {
    private static final RuntimeResourceLoaderProvider INSTANCE = new RuntimeResourceLoaderProvider();

    public static RuntimeResourceLoaderProvider getInstance() {
        return INSTANCE;
    }

    @Override
    public ResourceLoader getLoader(String location) {
        return new ClasspathResourceLoader();
    }
}
