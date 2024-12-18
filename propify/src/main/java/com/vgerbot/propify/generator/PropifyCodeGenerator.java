package com.vgerbot.propify.generator;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import com.vgerbot.propify.*;
import com.vgerbot.propify.logger.RuntimeLogger;
import com.vgerbot.propify.runtime.RuntimeResourceLoaderProvider;

import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.List;

public final class PropifyCodeGenerator {
    private static final PropifyCodeGenerator INSTANCE = new PropifyCodeGenerator();
    public static PropifyCodeGenerator getInstance() {
        return INSTANCE;
    }
    private PropifyCodeGenerator() {
    }

    public String generateCode(String packageName, String className, PropifyContext context, PropifyProperties properties) {
        ClassName className1 = ClassName.get(packageName, className);
        TypeSpec.Builder builder = generateType(
                className1,
                properties
        );
        builder.addModifiers(Modifier.PUBLIC);
        builder.addMethod(
                MethodSpec.methodBuilder("getInstance")
                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                        .returns(className1)
                        .addStatement("$T context = new $T($S, $S, $L, $S, $T.getInstance(), new $T())",
                                PropifyContext.class, PropifyContext.class,
                                context.getLocation(),
                                context.getMediaType(),
                                context.isAutoTypeConversion(),
                                context.getGeneratedClassName(),
                                RuntimeResourceLoaderProvider.class,
                                RuntimeLogger.class
                                )
                        .beginControlFlow("try")
                        .addStatement("$T stream = context.loadResource()", InputStream.class)
                        .addStatement("$T parserProvider = $T.getInstance()", PropifyConfigParserProvider.class, PropifyConfigParserProvider.class)
                        .addStatement("$T parser = parserProvider.getParser(context)", PropifyConfigParser.class)
                        .addStatement("$T properties = parser.parse(context, stream)", PropifyProperties.class)
                        .addStatement("return new $T(properties)", className1)
                        .nextControlFlow("catch ($T e)", IOException.class)
                        .addStatement("throw new RuntimeException(e)")
                        .endControlFlow()
                        .build()
        );
        TypeSpec typeSpec = builder.build();
        JavaFile javaFile = JavaFile.builder(packageName, typeSpec)
                .addFileComment("Generated code - do not modify")
                .build();

        StringBuilder sb = new StringBuilder();
        try {
            javaFile.writeTo(sb);
            return sb.toString();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to generate code", e);
        }
    }

    private TypeSpec.Builder generateType(ClassName className, PropifyProperties properties) {
        TypeSpec.Builder builder = TypeSpec.classBuilder(
                        className
                )
                .addJavadoc("Generated property class for accessing configuration values.\n")
                .addField(
                        PropifyProperties.class,
                        "properties",
                        Modifier.PRIVATE,
                        Modifier.FINAL
                )
                .addMethod(
                        MethodSpec.constructorBuilder()
                                .addModifiers(Modifier.PRIVATE)
                                .addParameter(PropifyProperties.class, "properties")
                                .addStatement("this.properties = properties")
                                .build()
                );
        properties.forEach((key, value) -> {
            if (value instanceof PropifyProperties) {
                ClassName innerClassName = className.nestedClass(Utils.convertToClassName(key));
                TypeSpec.Builder innerTypeBuilder = generateType(
                        innerClassName,
                        (PropifyProperties) value
                );
                innerTypeBuilder.addModifiers(Modifier.PUBLIC, Modifier.STATIC);
                TypeSpec innerType = innerTypeBuilder.build();

                builder.addType(innerType);
                builder.addMethod(
                        MethodSpec.methodBuilder(
                                Utils.convertToGetterName(key, null)
                        )
                                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                                .returns(innerClassName)
                                .addStatement("return new $T((PropifyProperties) properties.get(\"$L\"))", innerClassName, key)
                                .build()
                );
            } else {
                builder.addMethod(
                        generateGetterMethod(key, getType(value))
                );
            }
        });
        return builder;
    }
    private MethodSpec generateGetterMethod(String propertyName, Type type) {
        return MethodSpec.methodBuilder(
                    Utils.convertToGetterName(propertyName, type)
                )
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .returns(type)
                .addStatement("return ($T)properties.get($S)", type, propertyName)
                .build();
    }
    private Type getType(Object value) {
        if (value == null) {
            return Object.class;
        }
        if (value instanceof List) {
            return List.class;
        }
        return value.getClass();
    }

    public static void main(String[] args) {
        PropifyCodeGenerator generator = new PropifyCodeGenerator();
        PropifyProperties properties = new PropifyProperties();
        properties.put("name", "asdasd");
        PropifyProperties app = properties.createNested("app");
        PropifyProperties server = app.createNested("server");
        server.put("port", 123);
        server.put("path", "/asd/asd");

        String code = generator.generateCode("com.vgerbot.propify", "XXX", new PropifyContext(
                "classpath:config/application.properties",
                "application/properties",
                true,
                "$$PropertiesPropify",
                ',',
                RuntimeResourceLoaderProvider.getInstance(),
                new RuntimeLogger()
        ), properties);
        System.out.println(code);
    }
}
