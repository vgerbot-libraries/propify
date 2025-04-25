package com.vgerbot.propify.logger;

import java.util.logging.Level;

public class RuntimeLogger implements Logger {
    private final java.util.logging.Logger logger = java.util.logging.Logger.getLogger("Propify");
    @Override
    public void info(String message) {
        logger.info(message);
    }

    @Override
    public void warn(String message) {

        logger.warning(message);
    }

    @Override
    public void error(String message) {
        logger.log(Level.OFF, message);
    }
}
