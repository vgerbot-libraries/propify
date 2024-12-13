package com.vgerbot.propify;

import com.vgerbot.propify.service.ServiceLoaderWrapper;

public class PropifyConfigParserProvider {
    private static final PropifyConfigParserProvider INSTANCE = new PropifyConfigParserProvider();
    public static PropifyConfigParserProvider getInstance() {
        return INSTANCE;
    }
    public PropifyConfigParser getParser(String mediaType) {
        ClassLoader classLoader = PropifyProcessor.class.getClassLoader();
        for(PropifyConfigParser parser: ServiceLoaderWrapper.forClass(PropifyConfigParser.class, classLoader)) {
            System.out.println("parser: " + parser + ", " + mediaType);
            if (parser.accept(mediaType)) {
                return parser;
            }
        }
        return null;
    }

}
