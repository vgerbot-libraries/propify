package com.vgerbot.propify.i18n;

import java.lang.reflect.Proxy;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class PropifyI18nResourceBundle {
    private final String baseName;
    private final MessageTemplateExtension extension;
    private final Map<CacheKey, Object> proxyCache = new ConcurrentHashMap<>();

    public PropifyI18nResourceBundle(String baseName, MessageTemplateExtension extension) {
        this.baseName = baseName;
        this.extension = extension;
    }

    @SuppressWarnings("unchecked")
    public <T> T getMessageBundle(Class<T> type, Locale locale) {
        CacheKey cacheKey = new CacheKey(type, locale);
        return (T) proxyCache.computeIfAbsent(cacheKey, key -> {
            final ResourceBundle bundle = ResourceBundle.getBundle(this.baseName, locale);
            return Proxy.newProxyInstance(type.getClassLoader(), new Class[] { type }, (proxy, method, args) -> {
                Message annotation = method.getAnnotation(Message.class);
                if (annotation == null) {
                    throw new RuntimeException(method.toString() + " is not annotated with @PropifyI18nMessage");
                }
                String keyName = annotation.key();
                String[] arguments = annotation.arguments();

                if(arguments.length == 0) {
                    return bundle.getObject(keyName);
                } else {
                    Map<String, Object> paramsMap = IntStream.range(0, arguments.length)
                            .boxed()
                            .collect(Collectors.toMap(i -> arguments[i], i -> args[i]));
                    Object value = bundle.getObject(keyName);
                    if(value instanceof CharSequence) {
                        return extension.format(value.toString(), paramsMap);
                    }
                    return value;
                }
            });
        });
    }

    private static class CacheKey {
        private final Class<?> type;
        private final Locale locale;

        public CacheKey(Class<?> type, Locale locale) {
            this.type = type;
            this.locale = locale;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            CacheKey cacheKey = (CacheKey) o;
            return Objects.equals(type, cacheKey.type) && Objects.equals(locale, cacheKey.locale);
        }

        @Override
        public int hashCode() {
            return Objects.hash(type, locale);
        }
    }
}
