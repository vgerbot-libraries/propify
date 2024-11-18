package com.vgerbot.propify;

import com.palantir.javapoet.*;

import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.lang.reflect.Type;

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
        TypeSpec typeSpec = generateType(packageName, className, properties);
        JavaFile javaFile = JavaFile.builder(packageName, typeSpec).build();
        StringBuilder sb = new StringBuilder();
        try {
            javaFile.writeTo(sb);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return sb.toString();
    }

    private TypeSpec generateType(String packageName, String className, PropifyProperties properties) {
        TypeSpec.Builder builder = TypeSpec.classBuilder(className);
        properties.forEach((k, v) -> {
            if (v instanceof PropifyProperties) {
                String innerClassName = k;
                TypeSpec typeSpec = generateType(packageName, innerClassName, (PropifyProperties) v);
                typeSpec.modifiers().add(Modifier.PRIVATE);
                typeSpec.modifiers().add(Modifier.STATIC);
                builder.addType(
                        typeSpec
                );
                Tuple2<FieldSpec, MethodSpec> tuple = generateProperty(k, v, ClassName.get(packageName, innerClassName));
                builder.addField(tuple._1);
                builder.addMethod(tuple._2);
            } else {
                Tuple2<FieldSpec, MethodSpec> tuple = generateProperty(k, v, null);
                builder.addField(tuple._1);
                builder.addMethod(tuple._2);
            }
        });
        return builder.build();
    }

    private Tuple2<FieldSpec, MethodSpec> generateProperty(String name, Object value, TypeName typeName) {
        if (typeName == null) {
            Type type = getType(value);
            typeName = TypeName.get(type);
        }
        FieldSpec field = FieldSpec
                .builder(
                        typeName,
                        name,
                        Modifier.FINAL,
                        Modifier.PRIVATE
                )
                .initializer("$$", value)
                .build();
        MethodSpec method = MethodSpec.methodBuilder(getterName(name, typeName))
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
