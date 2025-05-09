package com.vgerbot.propify.generator;

import com.squareup.javapoet.*;
import com.vgerbot.propify.common.Utils;
import com.vgerbot.propify.i18n.ICUMessageTemplateExtension;
import com.vgerbot.propify.i18n.ICUTemplateArgumentsParser;
import com.vgerbot.propify.i18n.Message;
import com.vgerbot.propify.i18n.PropifyI18nResourceBundle;

import javax.lang.model.element.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
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
            String methodName = Utils.convertToFieldName(key);
            Object value = bundle.getObject(key);
            List<ICUTemplateArgumentsParser.Argument> arguments;

            if (value instanceof CharSequence) {
                arguments = ICUTemplateArgumentsParser.parseTemplate(value.toString());
            } else {
                arguments = new ArrayList<>(0);
            }

            String format = "{" + arguments.stream().map(v -> "$S").collect(Collectors.joining(",")) + "}";
            AnnotationSpec annotation = AnnotationSpec.builder(Message.class)
                    .addMember("key", CodeBlock.of("$S", key))
                    .addMember("arguments", CodeBlock.of(format, arguments.stream().map(ICUTemplateArgumentsParser.Argument::getName).toArray()))
                    .build();
            return MethodSpec.methodBuilder(methodName)
                    .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                    .returns(String.class)
                    .addParameters(arguments.stream().map(it -> ParameterSpec.builder(it.getType(), Utils.convertToFieldName(it.getName())).build()).collect(Collectors.toList()))
                    .addAnnotation(annotation)
                    .build();
        }).collect(Collectors.toList());
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
}
