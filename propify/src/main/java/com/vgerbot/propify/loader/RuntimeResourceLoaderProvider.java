package com.vgerbot.propify.loader;

import com.vgerbot.propify.core.ResourceLoader;
import com.vgerbot.propify.core.ResourceLoaderProvider;
import com.vgerbot.propify.service.ServiceLoaderWrapper;

import java.util.ArrayList;
import java.util.List;

/**
 * Provider for runtime resource loaders that handles different resource protocols.
 * 
 * <p>This provider uses Java's ServiceLoader mechanism to load ResourceLoader
 * implementations dynamically. It supports:
 * <ul>
 *   <li>Classpath resources (classpath: protocol)</li>
 *   <li>File system resources (file: protocol)</li>
 *   <li>HTTP/HTTPS resources (http:, https: protocols)</li>
 * </ul>
 */
public class RuntimeResourceLoaderProvider implements ResourceLoaderProvider {
    private static final RuntimeResourceLoaderProvider INSTANCE = new RuntimeResourceLoaderProvider();
    private final List<ResourceLoader> loaders;

    private RuntimeResourceLoaderProvider() {
        this.loaders = new ArrayList<>();
        ServiceLoaderWrapper<ResourceLoader> serviceLoader = ServiceLoaderWrapper.forClass(
            ResourceLoader.class,
            RuntimeResourceLoaderProvider.class.getClassLoader()
        );
        for (ResourceLoader loader : serviceLoader) {
            loaders.add(loader);
        }
    }

    public static RuntimeResourceLoaderProvider getInstance() {
        return INSTANCE;
    }

    @Override
    public ResourceLoader getLoader(String location) {
        if (location == null) {
            throw new IllegalArgumentException("Location cannot be null");
        }

        for (ResourceLoader loader : loaders) {
            if (loader.accept(location)) {
                return loader;
            }
        }

        // Default to first available loader (typically ClasspathResourceLoader)
        // for backward compatibility
        if (!loaders.isEmpty()) {
            return loaders.get(0);
        }

        throw new IllegalStateException("No ResourceLoader implementations found");
    }
}
