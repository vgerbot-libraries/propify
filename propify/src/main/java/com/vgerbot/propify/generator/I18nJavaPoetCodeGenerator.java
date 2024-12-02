package com.vgerbot.propify.generator;

import com.ibm.icu.text.MessageFormat;
import com.squareup.javapoet.*;
import com.vgerbot.propify.i18n.PropifyI18nResourceBundle;
import com.vgerbot.propify.i18n.ICUMessageTemplateExtension;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import java.util.*;
import java.util.stream.Collectors;

public class I18nJavaPoetCodeGenerator {
    private static class SingletonHolder {
        private static final I18nJavaPoetCodeGenerator INSTANCE = new I18nJavaPoetCodeGenerator();
    }

    public static I18nJavaPoetCodeGenerator getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public String generateCode(String packageName, String className, String baseName, String defaultLocale, ResourceBundle bundle) {
        // Generate the LocaleMessages interface
        TypeSpec localeMessagesInterface = TypeSpec.interfaceBuilder("LocaleMessages")
                .addModifiers(Modifier.PUBLIC)
                .addMethods(generateInterfaceMethods(bundle))
                .build();

        // Generate the MessageResource class
        TypeSpec messageResource = TypeSpec.classBuilder(className)
                .addModifiers(Modifier.PUBLIC)
                .addField(generateResourceBundleField(baseName))
                .addType(localeMessagesInterface)
                .addMethod(generateGetMethod())
                .addMethod(generateGetDefaultMethod(defaultLocale))
                .build();

        JavaFile javaFile = JavaFile.builder(packageName, messageResource)
                .addFileComment("Generated code - do not modify")
                .build();

        try {
            StringBuilder sb = new StringBuilder();
            javaFile.writeTo(sb);
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to generate code", e);
        }
    }

    private FieldSpec generateResourceBundleField(String baseName) {
        return FieldSpec.builder(PropifyI18nResourceBundle.class, "resourceBundle")
                .addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                .initializer("new $T($S, new $T())",
                        PropifyI18nResourceBundle.class,
                        baseName,
                        ICUMessageTemplateExtension.class)
                .build();
    }

    private Iterable<MethodSpec> generateInterfaceMethods(ResourceBundle bundle) {
        return bundle.keySet().stream().map(key -> {
            String methodName = key.replaceAll("\\.", "_");
            Object value = bundle.getObject(key);
            List<ParameterSpec> parameterSpecs;
            if (value instanceof CharSequence) {
                parameterSpecs = generateMethodParameters(value.toString());
            } else {
                parameterSpecs = Collections.emptyList();
            }
            return MethodSpec.methodBuilder(methodName)
                    .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                    .returns(String.class)
                    .addParameters(parameterSpecs)
                    .build();
        }).collect(Collectors.toList());
    }

    private List<ParameterSpec> generateMethodParameters(String pattern) {
        MessageFormat format = new MessageFormat(pattern);
        Set<String> argumentNames = format.getArgumentNames();
        return argumentNames.stream().map(argName -> ParameterSpec.builder(String.class, argName).build())
                .collect(Collectors.toList());
    }

    private MethodSpec generateGetMethod() {
        return MethodSpec.methodBuilder("get")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(ClassName.get("", "LocaleMessages"))
                .addParameter(Locale.class, "locale")
                .addStatement("return resourceBundle.getMessageBundle($T.class, locale)", ClassName.get("", "LocaleMessages"))
                .build();
    }

    private MethodSpec generateGetDefaultMethod(String defaultLocale) {
        return MethodSpec.methodBuilder("getDefault")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(ClassName.get("", "LocaleMessages"))
                .addStatement("return get($T.forLanguageTag($S))", Locale.class, defaultLocale)
                .build();
    }

}