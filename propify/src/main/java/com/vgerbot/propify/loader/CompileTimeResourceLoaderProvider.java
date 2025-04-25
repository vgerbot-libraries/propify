package com.vgerbot.propify.loader;

import com.vgerbot.propify.core.ResourceLoader;
import com.vgerbot.propify.core.ResourceLoaderProvider;

import javax.annotation.processing.ProcessingEnvironment;

public class CompileTimeResourceLoaderProvider implements ResourceLoaderProvider {
    private final ProcessingEnvironment processingEnvironment;

    public CompileTimeResourceLoaderProvider(ProcessingEnvironment processingEnvironment) {
        this.processingEnvironment = processingEnvironment;
    }

    @Override
    public ResourceLoader getLoader(String location) {

        if (location.startsWith("classpath:")) {
            return new CompileTimeClasspathResourceLoader(processingEnvironment);
        }

        if (location.startsWith("file:")) {
            return new FileResourceLoader();
        }
        if (location.startsWith("http:") || location.startsWith("https:")) {
            return new HTTPResourceLoader();
        }
        throw new IllegalArgumentException("Unsupported location: " + location);
    }
}
