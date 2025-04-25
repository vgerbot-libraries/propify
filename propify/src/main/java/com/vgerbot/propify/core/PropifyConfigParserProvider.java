package com.vgerbot.propify.core;

import com.vgerbot.propify.PropifyProcessor;
import com.vgerbot.propify.service.ServiceLoaderWrapper;

/**
 * Provider class for obtaining configuration parsers through Java's ServiceLoader mechanism.
 * 
 * <p>This class uses the ServiceLoader pattern to discover and provide appropriate
 * configuration parsers based on the media type of configuration resources. It ensures
 * that only one instance of the provider exists through the singleton pattern.
 */
public class PropifyConfigParserProvider {
    private static final PropifyConfigParserProvider INSTANCE = new PropifyConfigParserProvider();
    private PropifyConfigParserProvider() {}
    /**
     * Gets the singleton instance of the provider.
     *
     * @return the singleton instance
     */
    public static PropifyConfigParserProvider getInstance() {
        return INSTANCE;
    }

    /**
     * Gets an appropriate parser for the given context.
     * 
     * <p>This method:
     * <ul>
     *   <li>Uses ServiceLoader to discover available parsers</li>
     *   <li>Finds the first parser that accepts the context's media type</li>
     *   <li>Returns null if no suitable parser is found</li>
     * </ul>
     *
     * @param context the context containing media type information
     * @return a parser that can handle the context's media type, or null if none found
     * @see PropifyConfigParser#accept(PropifyContext) For parser acceptance criteria
     */
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
