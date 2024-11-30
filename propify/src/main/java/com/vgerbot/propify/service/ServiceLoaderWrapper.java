package com.vgerbot.propify.service;

import java.util.Iterator;
import java.util.ServiceLoader;

public interface ServiceLoaderWrapper<T> extends Iterable<T> {
    @Override
    Iterator<T> iterator();

    static <T> ServiceLoaderWrapper<T> forClass(Class<T> serviceClass, ClassLoader classLoader) {
        ServiceLoader<T> serviceLoader = ServiceLoader.load(serviceClass, classLoader);
        return new ServiceLoaderWrapper<T>() {
            @Override
            public Iterator<T> iterator() {
                return serviceLoader.iterator();
            }
        };
    }
}
