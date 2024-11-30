package com.vgerbot.propify;

import com.vgerbot.propify.service.ServiceLoaderWrapper;

import javax.annotation.processing.ProcessingEnvironment;
import java.io.IOException;
import java.io.InputStream;

public final class PropifyContext {
    private final ServiceLoaderWrapper<PropifyConfigParser> configParserServiceLoader;
    private final ServiceLoaderWrapper<PropifyConfigResource> resourceServiceLoader;
    private final String location;
    private final String mediaType;
    private final String generatedClassName;
    private final ProcessingEnvironment processingEnvironment;
    private final boolean autoTypeConversion;

    public PropifyContext(Propify propifyAnnotation, ProcessingEnvironment processingEnvironment) {
        this(propifyAnnotation, processingEnvironment,
             ServiceLoaderWrapper.forClass(PropifyConfigParser.class, PropifyProcessor.class.getClassLoader()),
             ServiceLoaderWrapper.forClass(PropifyConfigResource.class, PropifyProcessor.class.getClassLoader()));
    }

    // Constructor for testing
    PropifyContext(Propify propifyAnnotation, ProcessingEnvironment processingEnvironment,
                  ServiceLoaderWrapper<PropifyConfigParser> configParserServiceLoader,
                  ServiceLoaderWrapper<PropifyConfigResource> resourceServiceLoader) {
        if (propifyAnnotation == null) {
            throw new IllegalArgumentException("Propify annotation cannot be null");
        }
        if (processingEnvironment == null) {
            throw new IllegalArgumentException("ProcessingEnvironment cannot be null");
        }
        
        this.processingEnvironment = processingEnvironment;
        this.configParserServiceLoader = configParserServiceLoader;
        this.resourceServiceLoader = resourceServiceLoader;
        this.autoTypeConversion = propifyAnnotation.autoTypeConversion();

        String location = propifyAnnotation.location();
        if (location == null || location.trim().isEmpty()) {
            throw new IllegalArgumentException("Location cannot be null or empty");
        }
        this.location = location.trim();

        String mediaType = propifyAnnotation.mediaType();
        if (mediaType == null || mediaType.trim().isEmpty()) {
            mediaType = Utils.inferMediaType(this.location);
        }
        this.mediaType = mediaType.trim();

        String generatedClassName = propifyAnnotation.generatedClassName();
        if(generatedClassName == null || generatedClassName.trim().isEmpty()) {
            generatedClassName = "$$Propify";
        }
        this.generatedClassName = generatedClassName.trim();
    }

    public ProcessingEnvironment getProcessingEnvironment() {
        return processingEnvironment;
    }

    public boolean isAutoTypeConversion() {
        return autoTypeConversion;
    }

    public InputStream loadResource() throws IOException {
        for (PropifyConfigResource configLoader : resourceServiceLoader) {
            if (configLoader.accept(location)) {
                return configLoader.load(processingEnvironment, location);
            }
        }
        throw new IOException("Could not find resource: " + location);
    }

    public PropifyConfigParser getParser() throws IOException {
        for (PropifyConfigParser parser : configParserServiceLoader) {
            if (parser.accept(mediaType)) {
                return parser;
            }
        }
        throw new IOException("No parser found for media type: " + mediaType);
    }

    public String getClassName(String originClassName) {
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
