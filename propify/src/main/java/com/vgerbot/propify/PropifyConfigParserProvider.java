package com.vgerbot.propify;

import com.vgerbot.propify.service.ServiceLoaderWrapper;

public class PropifyConfigParserProvider {
    private static final PropifyConfigParserProvider INSTANCE = new PropifyConfigParserProvider();
    public static PropifyConfigParserProvider getInstance() {
        return INSTANCE;
    }
    public PropifyConfigParser getParser(PropifyContext context) {
        ClassLoader classLoader = PropifyProcessor.class.getClassLoader();
        for(PropifyConfigParser parser: ServiceLoaderWrapper.forClass(PropifyConfigParser.class, classLoader)) {
            if (parser.accept(context)) {
                return parser;
            }
        }
        return null;
    }

}
