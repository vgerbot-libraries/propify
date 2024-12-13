package com.vgerbot.propify;

import java.io.IOException;
import java.io.InputStream;

public final class PropifyContext {

    private final String location;
    private final String mediaType;
    private final boolean autoTypeConversion;
    private final String generatedClassName;
    private final ResourceLoaderProvider resourceLoaderProvider;

    public PropifyContext(
            String location,
            String mediaType,
            boolean autoTypeConversion,
            String generatedClassName,
            ResourceLoaderProvider resourceLoaderProvider
    ) {
        this.location = location;
        this.mediaType = mediaType;
        this.autoTypeConversion = autoTypeConversion;
        this.generatedClassName = generatedClassName;
        this.resourceLoaderProvider = resourceLoaderProvider;
    }
    public String getLocation() {
        return location;
    }
    public String getMediaType() {
        return mediaType;
    }
    public String getGeneratedClassName() {
        return generatedClassName;
    }
    public boolean isAutoTypeConversion() {
        return this.autoTypeConversion;
    }
    public ResourceLoader getResourceLoader() {
        return this.resourceLoaderProvider.getLoader(this.location);
    }

    public InputStream loadResource() throws IOException {
        return this.getResourceLoader().load(this.location);
    }

    public String generateClassName(String originClassName) {
        if (originClassName == null || originClassName.trim().isEmpty()) {
            throw new IllegalArgumentException("Origin class name cannot be null or empty");
        }

        if (this.generatedClassName.isEmpty()) {
            return originClassName + "Propify";
        } else {
            return this.generatedClassName.replaceAll("\\$\\$", originClassName);
        }
    }
}
