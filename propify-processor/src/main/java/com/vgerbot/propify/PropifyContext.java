package com.vgerbot.propify;

import javax.annotation.processing.ProcessingEnvironment;
import java.io.IOException;
import java.io.InputStream;
import java.util.ServiceLoader;

public class PropifyContext {
    static ServiceLoader<PropifyConfigParser> configParserServiceLoader = ServiceLoader.load(PropifyConfigParser.class, PropifyProcessor.class.getClassLoader());
    static ServiceLoader<PropifyConfigResource> resourceServiceLoader = ServiceLoader.load(PropifyConfigResource.class, PropifyProcessor.class.getClassLoader());

    private static InputStream loadResource(ProcessingEnvironment processingEnvironment, String location) throws IOException {
        for (PropifyConfigResource configLoader : resourceServiceLoader) {
            Boolean accept = configLoader.accept(location);
            if(accept) {
                return configLoader.load(processingEnvironment, location);
            }
        }
        throw new IllegalStateException("No suitable configuration loader found for the specified location: " + location);
    }
    private static PropifyConfigParser getParser(String mediaType) {
        for (PropifyConfigParser parser : configParserServiceLoader) {
            if (parser.accept(mediaType)) {
                return parser;
            }
        }
        throw new IllegalStateException("No suitable configuration parser found for the specified media type: '" + mediaType + "'");
    }

    private String location;
    private String mediaType;
    private ProcessingEnvironment processingEnvironment;
    public PropifyContext(Propify propifyAnnotation, ProcessingEnvironment processingEnvironment) {
        this.processingEnvironment = processingEnvironment;
        String location = propifyAnnotation.location();
        String mediaType = propifyAnnotation.mediaType();
        if ("".equals(mediaType)) {
            mediaType = Utils.inferMediaType(location);
        }
        this.location = location;
        this.mediaType = mediaType;
    }

    public ProcessingEnvironment getProcessingEnvironment() {
        return processingEnvironment;
    }
    public InputStream loadResource() throws IOException {
        return loadResource(this.processingEnvironment, this.location);
    }
    public PropifyConfigParser getParser() {
        return getParser(this.mediaType);
    }
}
