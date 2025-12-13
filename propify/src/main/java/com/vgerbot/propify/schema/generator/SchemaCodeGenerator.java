package com.vgerbot.propify.schema.generator;

import com.squareup.javapoet.*;
import com.vgerbot.propify.common.Utils;
import com.vgerbot.propify.schema.*;

import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;

/**
 * Generates Java POJO classes from schema definitions.
 *
 * @since 2.1.0
 */
public class SchemaCodeGenerator {
    
    private static final SchemaCodeGenerator INSTANCE = new SchemaCodeGenerator();
    
    public static SchemaCodeGenerator getInstance() {
        return INSTANCE;
    }
    
    private SchemaCodeGenerator() {
    }

    /**
     * Generate Java source code from a schema definition.
     *
     * @param packageName the package name for the generated class
     * @param className the class name
     * @param context the schema context
     * @param schema the schema definition
     * @return the generated Java source code as a string
     */
    public String generateCode(String packageName, String className, SchemaContext context, SchemaDefinition schema) {
        TypeSpec typeSpec = generateClass(className, context, schema);
        
        JavaFile javaFile = JavaFile.builder(packageName, typeSpec)
                .addFileComment("Generated from schema - do not modify")
                .indent("    ")
                .build();
        
        StringBuilder sb = new StringBuilder();
        try {
            javaFile.writeTo(sb);
            return sb.toString();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to generate code", e);
        }
    }

    private TypeSpec generateClass(String className, SchemaContext context, SchemaDefinition schema) {
        TypeSpec.Builder classBuilder = TypeSpec.classBuilder(className)
                .addModifiers(Modifier.PUBLIC);
        
        // Add javadoc
        if (schema.getDescription() != null && !schema.getDescription().isEmpty()) {
            classBuilder.addJavadoc(schema.getDescription() + "\n\n");
        }
        classBuilder.addJavadoc("Generated from schema\n");
        if (schema.getTitle() != null) {
            classBuilder.addJavadoc("Title: $L\n", schema.getTitle());
        }
        
        // Implement Serializable if requested
        if (context.isSerializable()) {
            classBuilder.addSuperinterface(Serializable.class);
            classBuilder.addField(
                    FieldSpec.builder(long.class, "serialVersionUID", Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                            .initializer("1L")
                            .build()
            );
        }
        
        // Generate nested classes first
        for (Map.Entry<String, SchemaDefinition> nested : schema.getNestedSchemas().entrySet()) {
            TypeSpec nestedClass = generateClass(nested.getKey(), context, nested.getValue());
            classBuilder.addType(nestedClass.toBuilder().addModifiers(Modifier.STATIC).build());
        }
        
        // Generate fields
        for (Map.Entry<String, PropertyDefinition> entry : schema.getProperties().entrySet()) {
            FieldSpec field = generateField(entry.getKey(), entry.getValue(), context);
            classBuilder.addField(field);
        }
        
        // Generate default constructor
        classBuilder.addMethod(
                MethodSpec.constructorBuilder()
                        .addModifiers(Modifier.PUBLIC)
                        .build()
        );
        
        // Generate getters and setters
        for (Map.Entry<String, PropertyDefinition> entry : schema.getProperties().entrySet()) {
            String propertyName = entry.getKey();
            PropertyDefinition property = entry.getValue();
            
            classBuilder.addMethod(generateGetter(propertyName, property, context));
            classBuilder.addMethod(generateSetter(propertyName, property, context, className));
        }
        
        // Generate builder if requested
        if (context.isBuilder()) {
            TypeSpec builderClass = generateBuilder(className, schema, context);
            classBuilder.addType(builderClass);
            classBuilder.addMethod(generateBuilderMethod(className));
        }
        
        // Generate equals, hashCode, toString if requested
        if (context.isGenerateHelperMethods()) {
            classBuilder.addMethod(generateEquals(className, schema));
            classBuilder.addMethod(generateHashCode(schema));
            classBuilder.addMethod(generateToString(className, schema));
        }
        
        return classBuilder.build();
    }

    private FieldSpec generateField(String name, PropertyDefinition property, SchemaContext context) {
        TypeName fieldType = getJavaType(property);
        FieldSpec.Builder fieldBuilder = FieldSpec.builder(fieldType, name, Modifier.PRIVATE);
        
        // Add Jackson annotations
        if (context.isJacksonAnnotations()) {
            addJacksonAnnotations(fieldBuilder, name, property);
        }
        
        // Add Bean Validation annotations
        if (context.isValidationAnnotations()) {
            addValidationAnnotations(fieldBuilder, property);
        }
        
        return fieldBuilder.build();
    }

    private void addJacksonAnnotations(FieldSpec.Builder fieldBuilder, String name, PropertyDefinition property) {
        try {
            Class<?> jsonPropertyClass = Class.forName("com.fasterxml.jackson.annotation.JsonProperty");
            fieldBuilder.addAnnotation(
                    AnnotationSpec.builder(ClassName.get(jsonPropertyClass))
                            .addMember("value", "$S", name)
                            .build()
            );
            
            // Add JsonFormat for date-time
            if ("date-time".equals(property.getFormat()) || "date".equals(property.getFormat())) {
                Class<?> jsonFormatClass = Class.forName("com.fasterxml.jackson.annotation.JsonFormat");
                AnnotationSpec.Builder formatBuilder = AnnotationSpec.builder(ClassName.get(jsonFormatClass));
                if ("date-time".equals(property.getFormat())) {
                    formatBuilder.addMember("shape", "$T.STRING", ClassName.get(jsonFormatClass.getPackage().getName(), "JsonFormat", "Shape"));
                    formatBuilder.addMember("pattern", "$S", "yyyy-MM-dd'T'HH:mm:ss");
                }
                fieldBuilder.addAnnotation(formatBuilder.build());
            }
        } catch (ClassNotFoundException e) {
            // Jackson not available, skip annotations
        }
    }

    private void addValidationAnnotations(FieldSpec.Builder fieldBuilder, PropertyDefinition property) {
        try {
            // @NotNull for required fields
            if (property.isRequired()) {
                Class<?> notNullClass = Class.forName("javax.validation.constraints.NotNull");
                fieldBuilder.addAnnotation(ClassName.get(notNullClass));
            }
            
            // @Size for string length
            if (property.isString() && (property.getMinLength() != null || property.getMaxLength() != null)) {
                Class<?> sizeClass = Class.forName("javax.validation.constraints.Size");
                AnnotationSpec.Builder sizeBuilder = AnnotationSpec.builder(ClassName.get(sizeClass));
                if (property.getMinLength() != null) {
                    sizeBuilder.addMember("min", "$L", property.getMinLength());
                }
                if (property.getMaxLength() != null) {
                    sizeBuilder.addMember("max", "$L", property.getMaxLength());
                }
                fieldBuilder.addAnnotation(sizeBuilder.build());
            }
            
            // @Min/@Max for numbers
            if ((property.isInteger() || property.isNumber()) && property.getMinimum() != null) {
                Class<?> minClass = Class.forName("javax.validation.constraints.Min");
                fieldBuilder.addAnnotation(
                        AnnotationSpec.builder(ClassName.get(minClass))
                                .addMember("value", "$L", property.getMinimum().longValue())
                                .build()
                );
            }
            if ((property.isInteger() || property.isNumber()) && property.getMaximum() != null) {
                Class<?> maxClass = Class.forName("javax.validation.constraints.Max");
                fieldBuilder.addAnnotation(
                        AnnotationSpec.builder(ClassName.get(maxClass))
                                .addMember("value", "$L", property.getMaximum().longValue())
                                .build()
                );
            }
            
            // @Pattern for regex
            if (property.hasPattern()) {
                Class<?> patternClass = Class.forName("javax.validation.constraints.Pattern");
                fieldBuilder.addAnnotation(
                        AnnotationSpec.builder(ClassName.get(patternClass))
                                .addMember("regexp", "$S", property.getPattern())
                                .build()
                );
            }
            
            // @Email for email format
            if ("email".equals(property.getFormat())) {
                Class<?> emailClass = Class.forName("javax.validation.constraints.Email");
                fieldBuilder.addAnnotation(ClassName.get(emailClass));
            }
        } catch (ClassNotFoundException e) {
            // Validation API not available, skip annotations
        }
    }

    private MethodSpec generateGetter(String propertyName, PropertyDefinition property, SchemaContext context) {
        TypeName returnType = getJavaType(property);
        String methodName = Utils.convertToGetterName(propertyName, property.isBoolean());
        
        MethodSpec.Builder getterBuilder = MethodSpec.methodBuilder(methodName)
                .addModifiers(Modifier.PUBLIC)
                .returns(returnType)
                .addStatement("return $L", propertyName);
        
        if (property.getDescription() != null && !property.getDescription().isEmpty()) {
            getterBuilder.addJavadoc(property.getDescription() + "\n");
        }
        
        return getterBuilder.build();
    }

    private MethodSpec generateSetter(String propertyName, PropertyDefinition property, SchemaContext context, String className) {
        TypeName paramType = getJavaType(property);
        String methodName = "set" + Utils.convertToClassName(propertyName);
        
        MethodSpec.Builder setterBuilder = MethodSpec.methodBuilder(methodName)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(paramType, propertyName)
                .addStatement("this.$L = $L", propertyName, propertyName);
        
        if (property.getDescription() != null && !property.getDescription().isEmpty()) {
            setterBuilder.addJavadoc(property.getDescription() + "\n");
        }
        
        return setterBuilder.build();
    }

    private TypeSpec generateBuilder(String className, SchemaDefinition schema, SchemaContext context) {
        ClassName outerClass = ClassName.bestGuess(className);
        
        TypeSpec.Builder builderBuilder = TypeSpec.classBuilder("Builder")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addField(outerClass, "instance", Modifier.PRIVATE)
                .addMethod(
                        MethodSpec.constructorBuilder()
                                .addModifiers(Modifier.PRIVATE)
                                .addStatement("this.instance = new $T()", outerClass)
                                .build()
                );
        
        // Generate builder methods for each property
        for (Map.Entry<String, PropertyDefinition> entry : schema.getProperties().entrySet()) {
            String propertyName = entry.getKey();
            PropertyDefinition property = entry.getValue();
            TypeName propertyType = getJavaType(property);
            
            builderBuilder.addMethod(
                    MethodSpec.methodBuilder(propertyName)
                            .addModifiers(Modifier.PUBLIC)
                            .addParameter(propertyType, propertyName)
                            .addStatement("instance.$L = $L", propertyName, propertyName)
                            .addStatement("return this")
                            .returns(ClassName.bestGuess("Builder"))
                            .build()
            );
        }
        
        // Generate build() method
        builderBuilder.addMethod(
                MethodSpec.methodBuilder("build")
                        .addModifiers(Modifier.PUBLIC)
                        .returns(outerClass)
                        .addStatement("return instance")
                        .build()
        );
        
        return builderBuilder.build();
    }

    private MethodSpec generateBuilderMethod(String className) {
        return MethodSpec.methodBuilder("builder")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(ClassName.bestGuess(className + ".Builder"))
                .addStatement("return new Builder()")
                .build();
    }

    private MethodSpec generateEquals(String className, SchemaDefinition schema) {
        MethodSpec.Builder equalsBuilder = MethodSpec.methodBuilder("equals")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(boolean.class)
                .addParameter(Object.class, "o")
                .addStatement("if (this == o) return true")
                .addStatement("if (!(o instanceof $L)) return false", className)
                .addStatement("$L that = ($L) o", className, className);
        
        if (schema.getProperties().isEmpty()) {
            equalsBuilder.addStatement("return true");
        } else {
            CodeBlock.Builder codeBuilder = CodeBlock.builder();
            codeBuilder.add("return ");
            
            Iterator<String> iter = schema.getProperties().keySet().iterator();
            while (iter.hasNext()) {
                String propName = iter.next();
                PropertyDefinition prop = schema.getProperties().get(propName);
                
                if (prop.isInteger() || prop.isNumber() || prop.isBoolean()) {
                    codeBuilder.add("$L == that.$L", propName, propName);
                } else {
                    codeBuilder.add("$T.equals($L, that.$L)", Objects.class, propName, propName);
                }
                
                if (iter.hasNext()) {
                    codeBuilder.add(" &&\n");
                }
            }
            codeBuilder.add(";");
            equalsBuilder.addCode(codeBuilder.build());
        }
        
        return equalsBuilder.build();
    }

    private MethodSpec generateHashCode(SchemaDefinition schema) {
        MethodSpec.Builder hashCodeBuilder = MethodSpec.methodBuilder("hashCode")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(int.class);
        
        if (schema.getProperties().isEmpty()) {
            hashCodeBuilder.addStatement("return 0");
        } else {
            CodeBlock.Builder codeBuilder = CodeBlock.builder();
            codeBuilder.add("return $T.hash(", Objects.class);
            
            Iterator<String> iter = schema.getProperties().keySet().iterator();
            while (iter.hasNext()) {
                codeBuilder.add("$L", iter.next());
                if (iter.hasNext()) {
                    codeBuilder.add(", ");
                }
            }
            codeBuilder.add(")");
            hashCodeBuilder.addStatement(codeBuilder.build());
        }
        
        return hashCodeBuilder.build();
    }

    private MethodSpec generateToString(String className, SchemaDefinition schema) {
        MethodSpec.Builder toStringBuilder = MethodSpec.methodBuilder("toString")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(String.class);
        
        CodeBlock.Builder codeBuilder = CodeBlock.builder();
        codeBuilder.add("return \"$L{\" +\n", className);
        
        Iterator<Map.Entry<String, PropertyDefinition>> iter = schema.getProperties().entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String, PropertyDefinition> entry = iter.next();
            String propName = entry.getKey();
            PropertyDefinition prop = entry.getValue();
            
            if (prop.isString()) {
                codeBuilder.add("\"$L='\" + $L + '\\'' +\n", propName, propName);
            } else {
                codeBuilder.add("\"$L=\" + $L +\n", propName, propName);
            }
            
            if (iter.hasNext()) {
                codeBuilder.add("\", \" +\n");
            }
        }
        
        codeBuilder.add("\"}\"");
        toStringBuilder.addStatement(codeBuilder.build());
        
        return toStringBuilder.build();
    }

    private TypeName getJavaType(PropertyDefinition property) {
        // Handle reference types
        if (property.getRefType() != null) {
            return ClassName.bestGuess(property.getRefType());
        }
        
        // Handle nested objects
        if (property.getNestedSchema() != null) {
            return ClassName.bestGuess(property.getNestedSchema().getName());
        }
        
        // Handle arrays
        if (property.isArray()) {
            if (property.getItems() != null) {
                TypeName itemType = getJavaType(property.getItems());
                return ParameterizedTypeName.get(ClassName.get(List.class), itemType);
            }
            return ParameterizedTypeName.get(ClassName.get(List.class), ClassName.get(Object.class));
        }
        
        // Handle primitives and basic types
        String type = property.getType();
        String format = property.getFormat();
        
        if ("string".equals(type)) {
            if ("date-time".equals(format)) {
                return ClassName.get("java.time", "LocalDateTime");
            } else if ("date".equals(format)) {
                return ClassName.get("java.time", "LocalDate");
            } else if ("time".equals(format)) {
                return ClassName.get("java.time", "LocalTime");
            }
            return ClassName.get(String.class);
        } else if ("integer".equals(type)) {
            if ("int64".equals(format)) {
                return ClassName.get(Long.class);
            }
            return ClassName.get(Integer.class);
        } else if ("number".equals(type)) {
            if ("float".equals(format)) {
                return ClassName.get(Float.class);
            }
            return ClassName.get(Double.class);
        } else if ("boolean".equals(type)) {
            return ClassName.get(Boolean.class);
        } else if ("object".equals(type)) {
            return ClassName.get(Map.class);
        }
        
        // Default to String
        return ClassName.get(String.class);
    }
}

