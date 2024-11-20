package com.vgerbot.propify;

import com.squareup.javapoet.*;

import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

class Tuple2<A, B> {
    public final A _1;
    public final B _2;

    public Tuple2(A _1, B _2) {
        this._1 = _1;
        this._2 = _2;
    }
}

public class PropifyCodeGenerator {
    private static class SingletonHolder {
        private static PropifyCodeGenerator INSTANCE = new PropifyCodeGenerator();
    }

    public static PropifyCodeGenerator getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public String generateCode(
            String packageName,
            String className,
            PropifyProperties properties
    ) {
        TypeSpec typeSpec = generateType(Arrays.asList(Modifier.PUBLIC, Modifier.FINAL), ClassName.get(packageName, className), properties);
        JavaFile javaFile = JavaFile.builder(packageName, typeSpec).build();
        StringBuilder sb = new StringBuilder();
        try {
            javaFile.writeTo(sb);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return sb.toString();
    }

    private TypeSpec generateType(List<Modifier> modifiers, ClassName className, PropifyProperties properties) {

        TypeSpec.Builder builder = TypeSpec.classBuilder(className);
        builder.modifiers.addAll(modifiers);
        properties.forEach((k, v) -> {
            String fieldName = Utils.convertToFieldName(k);
            if (v instanceof PropifyProperties) {
                ClassName innerClassName = className.nestedClass(Utils.convertToClassName(k));
                TypeSpec typeSpec = generateType(
                        Arrays.asList(
                                Modifier.PRIVATE,
                                Modifier.STATIC,
                                Modifier.FINAL
                        )
                        , innerClassName, (PropifyProperties) v);
                builder.addType(
                        typeSpec
                );

                Tuple2<FieldSpec, MethodSpec> tuple = generateProperty(fieldName, CodeBlock.builder()
                        .addStatement("new $T()", innerClassName)
                        .build(), innerClassName);
                builder.addField(tuple._1);
                builder.addMethod(tuple._2);
            } else {
                Tuple2<FieldSpec, MethodSpec> tuple = generateProperty(fieldName, CodeBlock.of("$L", Utils.toLiteralString(v)), TypeName.get(getType(v)));
                builder.addField(tuple._1);
                builder.addMethod(tuple._2);
            }
        });
        return builder.build();
    }

    private Tuple2<FieldSpec, MethodSpec> generateProperty(String name, CodeBlock initializer, TypeName typeName) {

        FieldSpec field = FieldSpec
                .builder(
                        typeName,
                        name,
                        Modifier.FINAL,
                        Modifier.PRIVATE
                )
                .initializer(initializer)
                .build();
        MethodSpec method = MethodSpec
                .methodBuilder(getterName(name, typeName))
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .returns(typeName)
                .addStatement(
                        CodeBlock.of("return $L", name)
                )
                .build();
        return new Tuple2<>(field, method);
    }

    private String getterName(String propertyName, TypeName typeName) {
        String suf = propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1);
        if (TypeName.get(Boolean.class).equals(typeName)) {
            return "is" + suf;
        }
        return "get" + suf;
    }


    private Type getType(Object value) {
        if (value == null) {
            return Object.class;
        }
        return value.getClass();
    }
}