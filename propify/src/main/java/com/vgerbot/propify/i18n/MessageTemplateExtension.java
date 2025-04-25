package com.vgerbot.propify.i18n;

import java.util.Map;

public interface MessageTemplateExtension {
    String format(String message, Map<String, Object> arguments);
}
