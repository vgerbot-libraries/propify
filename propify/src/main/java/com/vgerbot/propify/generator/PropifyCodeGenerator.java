package com.vgerbot.propify.generator;

import com.squareup.javapoet.*;
import com.vgerbot.propify.common.Utils;
import com.vgerbot.propify.core.*;
import com.vgerbot.propify.logger.RuntimeLogger;
import com.vgerbot.propify.loader.RuntimeResourceLoaderProvider;

import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
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
        constructContextCodeBuilder.add(Arrays.stream(lookups).map(it -> "$S").collect(Collectors.joining(",")), (Object[]) lookups);
        constructContextCodeBuilder.add("}");
        constructContextCodeBuilder.add(",$T.getInstance()", RuntimeResourceLoaderProvider.class);
        constructContextCodeBuilder.add(",new $T()", RuntimeLogger.class);
        constructContextCodeBuilder.add(")");


        builder.addMethod(
                MethodSpec.methodBuilder("getInstance")
                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                        .returns(className1)
                        .addStatement(constructContextCodeBuilder.build())
                        .addStatement("$T propifyPropertiesBuilder = new $T()", PropifyPropertiesBuilder.class, PropifyPropertiesBuilder.class)
                        .addStatement("return new $T(propifyPropertiesBuilder.build(context))", className1)
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
                .addModifiers(
                        Modifier.FINAL
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
                this.generateFromNestedProperties(builder, className, key, (PropifyProperties) value);
            } else if(value instanceof List) {
                List<?> list = (List<?>) value;
                Set<Class<?>> elementClasses = list.stream().map(Object::getClass).collect(Collectors.toSet());

                if (elementClasses.size() == 1 && elementClasses.iterator().next().equals(PropifyProperties.class)) {
                    List<String> allKeys = list.stream()
                        .map(it -> ((PropifyProperties) it).keySet())
                        .flatMap(Collection::stream)
                        .collect(Collectors.toList());

                    Map<String, Class<?>> keyTypes = allKeys.stream()
                            .collect(Collectors.toMap(keyi -> keyi, keyi -> {
                        Set<Class<?>> types = list.stream().map(item -> {
                            Object valuei = ((Map<String, Object>) item).get(keyi);
                            if(valuei == null) {
                                return Object.class;
                            }
                            return valuei.getClass();
                        }).collect(Collectors.toSet());
                        return types.size() == 1 ? types.iterator().next() : Object.class;
                    }, (a, b) -> b));

                    ClassName elementClassName = className.nestedClass(Utils.convertToClassName(key+"Item"));
                    builder.addType(
                        generateNestClass(keyTypes, elementClassName)
                    );
                    TypeName fieldClassName = ParameterizedTypeName.get(ClassName.get(List.class), elementClassName);
                    String refFieldName = Utils.convertToFieldName(key + "Ref");
                    builder.addField(
                            FieldSpec.builder(ParameterizedTypeName.get(ClassName.get(AtomicReference.class), fieldClassName), refFieldName, Modifier.PRIVATE, Modifier.FINAL)
                                    .initializer("new $T<$T>()", AtomicReference.class, fieldClassName)
                                    .build()
                    );

                    builder.addMethod(
                            MethodSpec.methodBuilder(
                                            Utils.convertToGetterName(key, false)
                                    )
                                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                                    .returns(fieldClassName)
                                    .addStatement("$T instance = $L.get()", fieldClassName, refFieldName)
                                    .beginControlFlow("if ( instance == null )")
                                    .addStatement(
                                            "List<$T> list = (List<$T>)properties.get(\"$L\")",
                                            PropifyProperties.class,
                                            PropifyProperties.class,
                                            key
                                    )
                                    .addStatement(
                                            "instance = list.stream().map(it -> new $T(it)).collect($T.toList())",
                                            elementClassName,
                                            Collectors.class
                                    )
//                                    .addStatement("instance = new $T((PropifyProperties) properties.get(\"$L\"))", innerClassName, key)
                                    .beginControlFlow("if (!$L.compareAndSet(null, instance))", refFieldName)
                                    .addStatement("instance = $L.get()", refFieldName)
                                    .endControlFlow() // end if compare and set
                                    .endControlFlow() // end if instance == null
                                    .addStatement("return instance")
                                    .build()
                    );
                } else {
                    builder.addMethod(
                            generateGetterMethod(key, getType(value))
                    );
                }
            } else {
                builder.addMethod(
                        generateGetterMethod(key, getType(value))
                );
            }
        });
        return builder;
    }
    private TypeSpec generateNestClass(Map<String, Class<?>> keyTypes, ClassName classname) {
        TypeSpec.Builder builder = TypeSpec.classBuilder(
                classname
        )
                .addModifiers(
                        Modifier.PUBLIC,
                        Modifier.STATIC,
                        Modifier.FINAL
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
        keyTypes.forEach((key, type) -> {
            TypeName fieldClassName ;
            if(List.class.isAssignableFrom(type)) {
                fieldClassName = ParameterizedTypeName.get(List.class, Object.class);
            } else if(Map.class.isAssignableFrom(type)) {
                fieldClassName = ParameterizedTypeName.get(Map.class, String.class, Object.class);
            } else {
                fieldClassName = ClassName.get(type);
            }
            builder.addMethod(
                    MethodSpec.methodBuilder(
                            Utils.convertToGetterName(key, type.isPrimitive() && type.isAssignableFrom(Boolean.class))
                    )
                            .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                            .returns(fieldClassName)
                            .addStatement("return ($T)properties.get(\"$L\")", fieldClassName, key)
                    .build()
            );
        });
        return builder.build();
    }
    private void generateFromNestedProperties(TypeSpec.Builder builder, ClassName outerClass, String key, PropifyProperties properties) {
        ClassName innerClassName = outerClass.nestedClass(Utils.convertToClassName(key));
        TypeSpec.Builder innerTypeBuilder = generateType(
                innerClassName,
                properties
        );
        innerTypeBuilder.addModifiers(Modifier.PUBLIC, Modifier.STATIC);
        TypeSpec innerType = innerTypeBuilder.build();

        builder.addType(innerType);
        final String refFieldName = Utils.convertToFieldName(key) + "Ref";
        builder.addField(
                FieldSpec.builder(ParameterizedTypeName.get(ClassName.get(AtomicReference.class), innerClassName), refFieldName, Modifier.PRIVATE, Modifier.FINAL)
                        .initializer("new $T<$T>()", AtomicReference.class, innerClassName)
                        .build()
        );
        builder.addMethod(
                MethodSpec.methodBuilder(
                                Utils.convertToGetterName(key, false)
                        )
                        .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                        .returns(innerClassName)
                        .addStatement("$T instance = $L.get()", innerClassName, refFieldName)
                        .beginControlFlow("if ( instance == null )")
                        .addStatement("instance = new $T((PropifyProperties) properties.get(\"$L\"))", innerClassName, key)
                        .beginControlFlow("if (!$L.compareAndSet(null, instance))", refFieldName)
                        .addStatement("instance = $L.get()", refFieldName)
                        .endControlFlow() // end if compare and set
                        .endControlFlow() // end if instance == null
                        .addStatement("return instance")
                        .build()
        );
    }
    private MethodSpec generateGetterMethod(String propertyName, TypeName type) {
        boolean isBoolean = TypeName.BOOLEAN.equals(type) || TypeName.BOOLEAN.box().equals(type);
        return MethodSpec.methodBuilder(
                    Utils.convertToGetterName(propertyName, isBoolean)
                )
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .returns(type)
                .addStatement("return ($T)properties.get($S)", type, propertyName)
                .build();
    }
    private TypeName getType(Object value) {
        if (value == null) {
            return TypeName.get(Object.class);
        }
        if (value instanceof List) {
            Set<Class<?>> classes = ((List<?>) value).stream().map(Object::getClass).collect(Collectors.toSet());
            if (classes.size() == 1) {
                Class<?> type = classes.iterator().next();
                return ParameterizedTypeName.get(List.class, type);
            } else {
                return ParameterizedTypeName.get(List.class, Object.class);
            }
        }
        if (value instanceof Map) {
            Set<Class<?>> keyClasses = ((Map<?, ?>) value).keySet().stream().map(Object::getClass).collect(Collectors.toSet());
            Set<Class<?>> valueClasses = ((Map<?, ?>) value).values().stream().map(it -> it != null ? it.getClass() : Object.class).collect(Collectors.toSet());
            
            Class<?> keyType = keyClasses.size() == 1 ? keyClasses.iterator().next() : Object.class;
            Class<?> valueType = valueClasses.size() == 1 ? valueClasses.iterator().next() : Object.class;
            
            return ParameterizedTypeName.get(ClassName.get(Map.class), ClassName.get(keyType), ClassName.get(valueType));
        }
        return TypeName.get(value.getClass());
    }

}
