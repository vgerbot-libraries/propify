package com.vgerbot.propify.compile;

import com.vgerbot.propify.ResourceLoader;
import com.vgerbot.propify.ResourceLoaderProvider;

import javax.annotation.processing.ProcessingEnvironment;

public class CompileTimeResourceLoaderProvider implements ResourceLoaderProvider  {
    private final ProcessingEnvironment processingEnvironment;

    public CompileTimeResourceLoaderProvider(ProcessingEnvironment processingEnvironment) {
        this.processingEnvironment = processingEnvironment;
    }
    @Override
    public ResourceLoader getLoader(String location) {
        return new ClasspathResourceLoader(processingEnvironment);
    }
}
