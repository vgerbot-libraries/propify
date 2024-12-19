package com.vgerbot.propify.generator;

import com.squareup.javapoet.*;
import com.vgerbot.propify.*;
import com.vgerbot.propify.common.Utils;
import com.vgerbot.propify.logger.RuntimeLogger;
import com.vgerbot.propify.runtime.RuntimeResourceLoaderProvider;
import org.apache.commons.configuration2.Configuration;

import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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
        String[] lookups = context.getLookups();

        CodeBlock.Builder constructContextCodeBuilder = CodeBlock.builder();
        constructContextCodeBuilder.add("$T context = new $T(", PropifyContext.class, PropifyContext.class);
        constructContextCodeBuilder.add("$S", context.getLocation());
        constructContextCodeBuilder.add(",$S", context.getMediaType());
        constructContextCodeBuilder.add(",$S", context.getGeneratedClassName());
        constructContextCodeBuilder.add(",'$L'", context.getListDelimiter());
        constructContextCodeBuilder.add(",new String[]{");
        constructContextCodeBuilder.add(Arrays.stream(lookups).map(it -> "$S").collect(Collectors.joining(",")), lookups);
        constructContextCodeBuilder.add("}");
        constructContextCodeBuilder.add(",$T.getInstance()", RuntimeResourceLoaderProvider.class);
        constructContextCodeBuilder.add(",new $T()", RuntimeLogger.class);
        constructContextCodeBuilder.add(")");


        builder.addMethod(
                MethodSpec.methodBuilder("getInstance")
                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                        .returns(className1)
                        .addStatement(constructContextCodeBuilder.build())
                        .beginControlFlow("try")
                        .addStatement("$T stream = context.loadResource()", InputStream.class)
                        .addStatement("$T parserProvider = $T.getInstance()", PropifyConfigParserProvider.class, PropifyConfigParserProvider.class)
                        .addStatement("$T parser = parserProvider.getParser(context)", PropifyConfigParser.class)
                        .addStatement("$T configuration = parser.parse(context, stream)", Configuration.class)
                        .addStatement("configuration.installInterpolator(context.getAllLookups(), $T.emptyList());", Collections.class)
                        .addStatement("$T propifyPropertiesBuilder = new $T();", PropifyPropertiesBuilder.class, PropifyPropertiesBuilder.class)
                        .addStatement("propifyPropertiesBuilder.config(configuration);")
                        .addStatement("return new $T(propifyPropertiesBuilder.build())", className1)
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
                "$$PropertiesPropify",
                ',',
                new String[]{},
                RuntimeResourceLoaderProvider.getInstance(),
                new RuntimeLogger()
        ), properties);
        System.out.println(code);
    }
}
