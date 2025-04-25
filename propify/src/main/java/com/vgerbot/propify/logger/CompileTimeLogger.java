package com.vgerbot.propify.logger;

import javax.annotation.processing.ProcessingEnvironment;
import javax.tools.Diagnostic;

public class CompileTimeLogger implements Logger {

    private final ProcessingEnvironment environment;

    public CompileTimeLogger(ProcessingEnvironment environment) {
        this.environment = environment;
    }
    @Override
    public void info(String message) {
        environment.getMessager().printMessage(Diagnostic.Kind.NOTE, message);
    }

    @Override
    public void warn(String message) {
        environment.getMessager().printMessage(Diagnostic.Kind.WARNING, message);
    }

    @Override
    public void error(String message) {
        environment.getMessager().printMessage(Diagnostic.Kind.ERROR, message);
    }
}
