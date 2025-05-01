package com.vgerbot.propify.common;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Constructor;

public class ReflectionUtils {
    private ReflectionUtils() {
        throw new PropifyException("Cannot instantiate ReflectionUtils class");
    }

    public static void makeAccessible(AccessibleObject object) {
        if (!object.isAccessible()) {
            object.setAccessible(true);
        }
    }

    public static <T> Constructor<T> getDeclaredConstructor(Class<T> clazz, Class<?>... parameterTypes) {
        try {
            Constructor<T> constructor = clazz.getDeclaredConstructor(parameterTypes);
            makeAccessible(constructor);
            return constructor;
        } catch (NoSuchMethodException e) {
            throw new PropifyException("Failed to get constructor", e);
        }
    }

    public static Field getDeclaredField(Class<?> clazz, String fieldName) {
        try {
            Field field = clazz.getDeclaredField(fieldName);
            makeAccessible(field);
            return field;
        } catch (NoSuchFieldException e) {
            throw new PropifyException("Failed to get field", e);
        }
    }

    public static Method getDeclaredMethod(Class<?> clazz, String methodName, Class<?>... parameterTypes) {
        try {
            Method method = clazz.getDeclaredMethod(methodName, parameterTypes);
            makeAccessible(method);
            return method;
        } catch (NoSuchMethodException e) {
            throw new PropifyException("Failed to get method", e);
        }
    }
} 