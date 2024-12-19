package com.vgerbot.propify.logger;

import com.vgerbot.propify.core.PropifyContext;

/**
 * Core logging interface for the Propify framework.
 * 
 * <p>This interface defines the standard logging operations used throughout the framework
 * for reporting various events, warnings, and errors during annotation processing and
 * code generation. It provides:
 * <ul>
 *   <li>Different severity levels (info, warn, error)</li>
 *   <li>Consistent logging API across different environments</li>
 *   <li>Integration with various logging implementations</li>
 * </ul>
 *
 * <p>The framework includes two main implementations:
 * <ul>
 *   <li>{@code CompileTimeLogger} - Uses the annotation processor's Messager</li>
 *   <li>{@code RuntimeLogger} - Uses standard output streams</li>
 * </ul>
 *
 * @see PropifyContext Uses this logger for processing events
 * @since 1.1.0
 */
public interface Logger {

    /**
     * Logs an informational message.
     * 
     * <p>This method should be used for general information about the processing
     * progress, such as:
     * <ul>
     *   <li>Resource loading events</li>
     *   <li>Configuration parsing progress</li>
     *   <li>Code generation status</li>
     * </ul>
     *
     * @param message the information message to log
     */
    void info(String message);

    /**
     * Logs a warning message.
     * 
     * <p>This method should be used for non-critical issues that don't prevent
     * processing but might indicate problems, such as:
     * <ul>
     *   <li>Deprecated configuration usage</li>
     *   <li>Missing optional resources</li>
     *   <li>Potential configuration conflicts</li>
     * </ul>
     *
     * @param message the warning message to log
     */
    void warn(String message);

    /**
     * Logs an error message.
     * 
     * <p>This method should be used for critical issues that prevent proper
     * processing or indicate serious problems, such as:
     * <ul>
     *   <li>Resource loading failures</li>
     *   <li>Invalid configuration syntax</li>
     *   <li>Code generation errors</li>
     * </ul>
     *
     * @param message the error message to log
     */
    void error(String message);
}
