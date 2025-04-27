package com.vgerbot.propify.generator;

import com.ibm.icu.text.MessageFormat;
import com.ibm.icu.text.MeasureFormat;
import com.ibm.icu.text.MessagePattern;
import com.squareup.javapoet.*;
import com.vgerbot.propify.common.MessageFormatParser;
import com.vgerbot.propify.common.Utils;
import com.vgerbot.propify.i18n.Message;
import com.vgerbot.propify.i18n.PropifyI18nResourceBundle;
import com.vgerbot.propify.i18n.ICUMessageTemplateExtension;

import javax.lang.model.element.Modifier;
import java.lang.reflect.Field;
import java.text.Format;
import java.util.*;
import java.util.stream.Collectors;
import java.util.Date;

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
            String methodName = Utils.convertToFieldName(key);
            Object value = bundle.getObject(key);
            List<ParameterSpec> parameterSpecs = new ArrayList<>();;
            Set<String> formatArgumentNames = new HashSet<>();
            if (value instanceof CharSequence) {
                List<MessageFormatParser.PlaceholderInfo> placeholders = MessageFormatParser.parsePlaceholders(value.toString());

                placeholders.forEach(placeholder -> {
                    Class<?> type = getArgType(placeholder.getFormatType());
                    String argName = placeholder.getName();
                    formatArgumentNames.add(argName);
                    parameterSpecs.add(
                            ParameterSpec.builder(type, Utils.convertToFieldName(argName)).build()
                    );
                });
            }

            String[] parameterNames = parameterSpecs.stream().map(it -> it.name).toArray(String[]::new);
            String format = "{" + String.join(",", Arrays.stream(parameterNames).map(v -> "$S").toArray(String[]::new)) + "}";
            AnnotationSpec annotation = AnnotationSpec.builder(Message.class)
                    .addMember("key", CodeBlock.of("$S", key))
                    .addMember("arguments", CodeBlock.of(format, formatArgumentNames.stream().toArray()))
                    .build();
            return MethodSpec.methodBuilder(methodName)
                    .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                    .returns(String.class)
                    .addParameters(parameterSpecs)
                    .addAnnotation(annotation)
                    .build();
        }).collect(Collectors.toList());
    }

    private List<ParameterSpec> generateMethodParameters(String pattern) {
        MessageFormat format = new MessageFormat(pattern);
        Set<String> argumentNames = format.getArgumentNames();
        return argumentNames.stream().map(argName -> ParameterSpec.builder(String.class, Utils.convertToFieldName(argName)).build())
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
                .addStatement(
                        defaultLocale.trim().isEmpty() ?
                                CodeBlock.of("return get($T.getDefault())", Locale.class)
                                : CodeBlock.of("return get($T.forLanguageTag($S))", Locale.class, defaultLocale))
                .build();
    }

    /**
     * Determines the appropriate Java type based on the format type
     * @param format The ICU Format object
     * @return The appropriate Java class type for the parameter
     */
    private Class<?> getArgTypeFromFormat(Format format) {
        if (format == null) {
            return String.class;
        }
        
        // ICU NumberFormat and its subclasses
        if (format instanceof com.ibm.icu.text.NumberFormat) {
            return Number.class;
        }
        
        // ICU DateFormat and its subclasses
        if (format instanceof com.ibm.icu.text.DateFormat) {
            return Date.class;
        }

        // MeasureFormat uses Measure objects with Number values
        if (format instanceof com.ibm.icu.text.MeasureFormat) {
            return Number.class;
        }
        
        // Plural format typically takes numbers
        if (format instanceof com.ibm.icu.text.PluralFormat) {
            return Number.class;
        }
        
        // Select format takes strings
        if (format instanceof com.ibm.icu.text.SelectFormat) {
            return String.class;
        }
        
        // For any other format type
        return Object.class;
    }
    private Class<?> getArgType(String type) {
        if(type == null) {
            return String.class;
        }
        switch (type) {
            case "date":
                return Date.class;
            case "number":
                return Number.class;
            case "select":
                return String.class;
        }
        return Object.class;
    }
}
