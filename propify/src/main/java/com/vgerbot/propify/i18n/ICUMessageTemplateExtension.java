package com.vgerbot.propify.i18n;

import com.ibm.icu.text.MessageFormat;

import java.util.HashMap;
import java.util.Map;

public class ICUMessageTemplateExtension implements MessageTemplateExtension {
    private static final ThreadLocal<Map<String, MessageFormat>> formats = ThreadLocal.withInitial(HashMap::new);

    @Override
    public String format(String message, Map<String, Object> arguments) {
        MessageFormat format = formats.get().computeIfAbsent(message, k -> new MessageFormat(message));
        return format.format(arguments);
    }
}
