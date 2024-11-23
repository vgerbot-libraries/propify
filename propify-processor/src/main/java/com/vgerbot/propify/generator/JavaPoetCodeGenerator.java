package com.vgerbot.propify.generator;

import com.squareup.javapoet.*;
import com.vgerbot.propify.PropifyProperties;
import com.vgerbot.propify.Utils;

import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

/**
 * Implementation of CodeGenerator using JavaPoet library for Java source code generation.
 */
public class JavaPoetCodeGenerator implements CodeGenerator {
    private static class SingletonHolder {
        private static final JavaPoetCodeGenerator INSTANCE = new JavaPoetCodeGenerator();
    }

    public static JavaPoetCodeGenerator getInstance() {
        return SingletonHolder.INSTANCE;
    }

    @Override
    public String generateCode(String packageName, String className, PropifyProperties properties) {
        TypeSpec typeSpec = generateType(
            Arrays.asList(Modifier.PUBLIC, Modifier.FINAL), 
            ClassName.get(packageName, className), 
            properties
        );
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

    private static class PropertySpec {
        final FieldSpec field;
        final MethodSpec getter;

        PropertySpec(FieldSpec field, MethodSpec getter) {
            this.field = field;
            this.getter = getter;
        }
    }

    private TypeSpec generateType(List<Modifier> modifiers, ClassName className, PropifyProperties properties) {
        TypeSpec.Builder builder = TypeSpec.classBuilder(className)
            .addModifiers(modifiers.toArray(new Modifier[0]))
            .addJavadoc("Generated property class for accessing configuration values.\n");

        properties.forEach((key, value) -> {
            String fieldName = Utils.convertToFieldName(key);
            if (value instanceof PropifyProperties) {
                // Generate nested class for nested properties
                ClassName innerClassName = className.nestedClass(Utils.convertToClassName(key));
                TypeSpec innerType = generateType(
                    Arrays.asList(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL),
                    innerClassName,
                    (PropifyProperties) value
                );
                
                builder.addType(innerType);
                PropertySpec propertySpec = generateProperty(
                    fieldName,
                    CodeBlock.builder().addStatement("new $T()", innerClassName).build(),
                    innerClassName
                );
                builder.addField(propertySpec.field);
                builder.addMethod(propertySpec.getter);
            } else {
                PropertySpec propertySpec = generateProperty(
                    fieldName,
                    value instanceof String ? CodeBlock.of("$S", value) :
                    CodeBlock.of("$L", Utils.toLiteralString(value)),
                    TypeName.get(getType(value))
                );
                builder.addField(propertySpec.field);
                builder.addMethod(propertySpec.getter);
            }
        });

        return builder.build();
    }

    private PropertySpec generateProperty(String name, CodeBlock initializer, TypeName typeName) {
        FieldSpec field = FieldSpec.builder(typeName, name)
            .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
            .initializer(initializer)
            .build();

        String getterPrefix = TypeName.get(Boolean.class).equals(typeName) ? "is" : "get";
        String methodName = getterPrefix + name.substring(0, 1).toUpperCase() + name.substring(1);

        MethodSpec getter = MethodSpec.methodBuilder(methodName)
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
            .returns(typeName)
            .addStatement("return $N", field)
            .addJavadoc("Returns the value of property '$L'\n@return the property value\n", name)
            .build();

        return new PropertySpec(field, getter);
    }

    private Type getType(Object value) {
        if (value == null) {
            return Object.class;
        }
        return value.getClass();
    }
}
