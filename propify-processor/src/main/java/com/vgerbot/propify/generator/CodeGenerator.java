package com.vgerbot.propify.generator;

import com.vgerbot.propify.PropifyProperties;

/**
 * Interface for code generation strategies in Propify.
 * Implementations can provide different ways of generating code from properties.
 */
public interface CodeGenerator {
    /**
     * Generate source code from the given properties configuration.
     *
     * @param packageName The target package name for the generated class
     * @param className The name of the class to generate
     * @param properties The properties configuration to use
     * @return The generated source code as a String
     */
    String generateCode(String packageName, String className, PropifyProperties properties);
}
