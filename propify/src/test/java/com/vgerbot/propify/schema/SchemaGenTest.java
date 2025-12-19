package com.vgerbot.propify.schema;

import org.junit.Test;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Method;

import static org.junit.Assert.*;

public class SchemaGenTest {

    @Test
    public void testAnnotationExists() {
        assertNotNull("SchemaGen annotation should exist", SchemaGen.class);
    }

    @Test
    public void testAnnotationRetention() {
        assertNotNull("Should have retention policy", SchemaGen.class.getAnnotation(java.lang.annotation.Retention.class));

        java.lang.annotation.Retention retention = SchemaGen.class.getAnnotation(java.lang.annotation.Retention.class);
        assertEquals("Should have SOURCE retention", RetentionPolicy.SOURCE, retention.value());
    }

    @Test
    public void testAnnotationTarget() {
        assertNotNull("Should have target", SchemaGen.class.getAnnotation(java.lang.annotation.Target.class));

        java.lang.annotation.Target target = SchemaGen.class.getAnnotation(java.lang.annotation.Target.class);
        ElementType[] types = target.value();

        assertEquals("Should have one target type", 1, types.length);
        assertEquals("Should target TYPE", ElementType.TYPE, types[0]);
    }

    @Test
    public void testLocationMethod() throws NoSuchMethodException {
        Method locationMethod = SchemaGen.class.getDeclaredMethod("location");

        assertNotNull("location method should exist", locationMethod);
        assertEquals("location should return String", String.class, locationMethod.getReturnType());
    }

    @Test
    public void testTypeMethod() throws NoSuchMethodException {
        Method typeMethod = SchemaGen.class.getDeclaredMethod("type");

        assertNotNull("type method should exist", typeMethod);
        assertEquals("type should return SchemaType", SchemaType.class, typeMethod.getReturnType());
        assertEquals("type default should be AUTO", SchemaType.AUTO, typeMethod.getDefaultValue());
    }

    @Test
    public void testSchemaRefMethod() throws NoSuchMethodException {
        Method schemaRefMethod = SchemaGen.class.getDeclaredMethod("schemaRef");

        assertNotNull("schemaRef method should exist", schemaRefMethod);
        assertEquals("schemaRef should return String", String.class, schemaRefMethod.getReturnType());
        assertEquals("schemaRef default should be empty", "", schemaRefMethod.getDefaultValue());
    }

    @Test
    public void testGeneratedClassNameMethod() throws NoSuchMethodException {
        Method generatedClassNameMethod = SchemaGen.class.getDeclaredMethod("generatedClassName");

        assertNotNull("generatedClassName method should exist", generatedClassNameMethod);
        assertEquals("generatedClassName should return String", String.class, generatedClassNameMethod.getReturnType());
        assertEquals("generatedClassName default should be $$", "$$", generatedClassNameMethod.getDefaultValue());
    }

    @Test
    public void testBuilderMethod() throws NoSuchMethodException {
        Method builderMethod = SchemaGen.class.getDeclaredMethod("builder");

        assertNotNull("builder method should exist", builderMethod);
        assertEquals("builder should return boolean", boolean.class, builderMethod.getReturnType());
        assertEquals("builder default should be true", true, builderMethod.getDefaultValue());
    }

    @Test
    public void testJacksonAnnotationsMethod() throws NoSuchMethodException {
        Method jacksonAnnotationsMethod = SchemaGen.class.getDeclaredMethod("jacksonAnnotations");

        assertNotNull("jacksonAnnotations method should exist", jacksonAnnotationsMethod);
        assertEquals("jacksonAnnotations should return boolean", boolean.class, jacksonAnnotationsMethod.getReturnType());
        assertEquals("jacksonAnnotations default should be true", true, jacksonAnnotationsMethod.getDefaultValue());
    }

    @Test
    public void testJaxbAnnotationsMethod() throws NoSuchMethodException {
        Method jaxbAnnotationsMethod = SchemaGen.class.getDeclaredMethod("jaxbAnnotations");

        assertNotNull("jaxbAnnotations method should exist", jaxbAnnotationsMethod);
        assertEquals("jaxbAnnotations should return boolean", boolean.class, jaxbAnnotationsMethod.getReturnType());
        assertEquals("jaxbAnnotations default should be false", false, jaxbAnnotationsMethod.getDefaultValue());
    }

    @Test
    public void testValidationAnnotationsMethod() throws NoSuchMethodException {
        Method validationAnnotationsMethod = SchemaGen.class.getDeclaredMethod("validationAnnotations");

        assertNotNull("validationAnnotations method should exist", validationAnnotationsMethod);
        assertEquals("validationAnnotations should return boolean", boolean.class, validationAnnotationsMethod.getReturnType());
        assertEquals("validationAnnotations default should be true", true, validationAnnotationsMethod.getDefaultValue());
    }

    @Test
    public void testSerializableMethod() throws NoSuchMethodException {
        Method serializableMethod = SchemaGen.class.getDeclaredMethod("serializable");

        assertNotNull("serializable method should exist", serializableMethod);
        assertEquals("serializable should return boolean", boolean.class, serializableMethod.getReturnType());
        assertEquals("serializable default should be true", true, serializableMethod.getDefaultValue());
    }

    @Test
    public void testGenerateHelperMethodsMethod() throws NoSuchMethodException {
        Method generateHelperMethodsMethod = SchemaGen.class.getDeclaredMethod("generateHelperMethods");

        assertNotNull("generateHelperMethods method should exist", generateHelperMethodsMethod);
        assertEquals("generateHelperMethods should return boolean", boolean.class, generateHelperMethodsMethod.getReturnType());
        assertEquals("generateHelperMethods default should be true", true, generateHelperMethodsMethod.getDefaultValue());
    }

    @Test
    public void testIsAnnotation() {
        assertTrue("SchemaGen should be an annotation", SchemaGen.class.isAnnotation());
    }

    @Test
    public void testAnnotationMethods() {
        Method[] methods = SchemaGen.class.getDeclaredMethods();

        assertTrue("Should have methods", methods.length > 0);
        assertEquals("Should have 10 methods", 10, methods.length);
    }

    @Test
    public void testAllDefaultValues() throws NoSuchMethodException {
        assertEquals("type default", SchemaType.AUTO,
                SchemaGen.class.getDeclaredMethod("type").getDefaultValue());
        assertEquals("schemaRef default", "",
                SchemaGen.class.getDeclaredMethod("schemaRef").getDefaultValue());
        assertEquals("generatedClassName default", "$$",
                SchemaGen.class.getDeclaredMethod("generatedClassName").getDefaultValue());
        assertEquals("builder default", true,
                SchemaGen.class.getDeclaredMethod("builder").getDefaultValue());
        assertEquals("jacksonAnnotations default", true,
                SchemaGen.class.getDeclaredMethod("jacksonAnnotations").getDefaultValue());
        assertEquals("jaxbAnnotations default", false,
                SchemaGen.class.getDeclaredMethod("jaxbAnnotations").getDefaultValue());
        assertEquals("validationAnnotations default", true,
                SchemaGen.class.getDeclaredMethod("validationAnnotations").getDefaultValue());
        assertEquals("serializable default", true,
                SchemaGen.class.getDeclaredMethod("serializable").getDefaultValue());
        assertEquals("generateHelperMethods default", true,
                SchemaGen.class.getDeclaredMethod("generateHelperMethods").getDefaultValue());
    }

    @Test
    public void testLocationHasNoDefault() throws NoSuchMethodException {
        Method locationMethod = SchemaGen.class.getDeclaredMethod("location");

        assertNull("location should have no default value", locationMethod.getDefaultValue());
    }

    @Test
    public void testCanCreateProxy() {
        // Test that we can create a proxy instance of the annotation
        SchemaGen schemaGen = new SchemaGen() {
            @Override
            public String location() {
                return "test.json";
            }

            @Override
            public SchemaType type() {
                return SchemaType.JSON_SCHEMA;
            }

            @Override
            public String schemaRef() {
                return "#/definitions/User";
            }

            @Override
            public String generatedClassName() {
                return "$$Dto";
            }

            @Override
            public boolean builder() {
                return true;
            }

            @Override
            public boolean jacksonAnnotations() {
                return true;
            }

            @Override
            public boolean jaxbAnnotations() {
                return false;
            }

            @Override
            public boolean validationAnnotations() {
                return true;
            }

            @Override
            public boolean serializable() {
                return true;
            }

            @Override
            public boolean generateHelperMethods() {
                return true;
            }

            @Override
            public Class<? extends Annotation> annotationType() {
                return SchemaGen.class;
            }
        };

        assertEquals("Location should match", "test.json", schemaGen.location());
        assertEquals("Type should match", SchemaType.JSON_SCHEMA, schemaGen.type());
        assertEquals("SchemaRef should match", "#/definitions/User", schemaGen.schemaRef());
        assertEquals("GeneratedClassName should match", "$$Dto", schemaGen.generatedClassName());
        assertTrue("Builder should be true", schemaGen.builder());
        assertTrue("JacksonAnnotations should be true", schemaGen.jacksonAnnotations());
        assertFalse("JaxbAnnotations should be false", schemaGen.jaxbAnnotations());
        assertTrue("ValidationAnnotations should be true", schemaGen.validationAnnotations());
        assertTrue("Serializable should be true", schemaGen.serializable());
        assertTrue("GenerateHelperMethods should be true", schemaGen.generateHelperMethods());
    }
}
