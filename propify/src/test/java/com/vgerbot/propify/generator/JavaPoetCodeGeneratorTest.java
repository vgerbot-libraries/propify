package com.vgerbot.propify.generator;

import com.vgerbot.propify.PropifyProperties;
import org.junit.Before;
import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.ExpectedException;

import javax.tools.*;
import java.io.File;
import java.io.IOException;
import java.util.*;

import static org.junit.Assert.*;

public class JavaPoetCodeGeneratorTest {
    private JavaPoetCodeGenerator generator;
    private PropifyProperties properties;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() {
        generator = JavaPoetCodeGenerator.getInstance();
        properties = new PropifyProperties();
    }

    @Test
    public void testBasicPropertyGeneration() {
        properties.put("stringProp", "test value");
        properties.put("intProp", 42);
        properties.put("boolProp", true);

        String generatedCode = generator.generateCode("com.test", "TestConfig", properties);

        assertNotNull("Generated code should not be null", generatedCode);
        assertTrue("Should contain string property", generatedCode.contains("private final String stringProp"));
        assertTrue("Should contain int property", generatedCode.contains("private final Integer intProp"));
        assertTrue("Should contain boolean property", generatedCode.contains("private final Boolean boolProp"));
        assertTrue("Should contain string getter", generatedCode.contains("public final String getStringProp()"));
        assertTrue("Should contain int getter", generatedCode.contains("public final Integer getIntProp()"));
        assertTrue("Should contain boolean getter", generatedCode.contains("public final Boolean isBoolProp()"));
    }

    @Test
    public void testNestedPropertyGeneration() {
        PropifyProperties nestedProps = new PropifyProperties();
        nestedProps.put("nestedString", "nested value");
        nestedProps.put("nestedInt", 123);
        properties.put("nested", nestedProps);

        String generatedCode = generator.generateCode("com.test", "TestConfig", properties);

        assertNotNull("Generated code should not be null", generatedCode);
        assertTrue("Should contain nested class", generatedCode.contains("public static final class Nested"));
        assertTrue("Should contain nested string property", generatedCode.contains("private final String nestedString"));
        assertTrue("Should contain nested int property", generatedCode.contains("private final Integer nestedInt"));
        assertTrue("Should contain nested getter", generatedCode.contains("public final Nested getNested()"));
    }

    @Test
    public void testArrayPropertyGeneration() {
        properties.put("stringArray", new String[]{"one", "two", "three"});
        properties.put("intArray", new int[]{1, 2, 3});

        String generatedCode = generator.generateCode("com.test", "TestConfig", properties);

        assertNotNull("Generated code should not be null", generatedCode);
        assertTrue("Should contain string array initialization", 
            generatedCode.contains("private final String[] stringArray = new String[] {\"one\", \"two\", \"three\"}"));
        assertTrue("Should contain int array initialization", 
            generatedCode.contains("private final int[] intArray = new int[] {1, 2, 3}"));
    }

    @Test
    public void testListPropertyGeneration() {
        properties.put("stringList", Arrays.asList("one", "two", "three"));
        properties.put("intList", Arrays.asList(1, 2, 3));

        String generatedCode = generator.generateCode("com.test", "TestConfig", properties);

        assertNotNull("Generated code should not be null", generatedCode);
        assertTrue("Should contain string list initialization", 
            generatedCode.contains("Arrays.asList(\"one\", \"two\", \"three\")"));
        assertTrue("Should contain int list initialization", 
            generatedCode.contains("Arrays.asList(1, 2, 3)"));
    }

    @Test
    public void testNullPropertyHandling() {
        properties.put("nullProp", null);

        String generatedCode = generator.generateCode("com.test", "TestConfig", properties);

        assertNotNull("Generated code should not be null", generatedCode);
        assertTrue("Should contain null property", generatedCode.contains("private final Object nullProp = null"));
        assertTrue("Should contain null property getter", generatedCode.contains("public final Object getNullProp()"));
    }

    @Test
    public void testComplexNestedStructure() {
        PropifyProperties level2 = new PropifyProperties();
        level2.put("deep", "value");
        level2.put("numbers", Arrays.asList(1, 2, 3));

        PropifyProperties level1 = new PropifyProperties();
        level1.put("nested", level2);
        level1.put("flag", true);

        properties.put("root", level1);

        String generatedCode = generator.generateCode("com.test", "TestConfig", properties);

        assertNotNull("Generated code should not be null", generatedCode);
        assertTrue("Should contain root class", generatedCode.contains("public static final class Root"));
        assertTrue("Should contain nested class", generatedCode.contains("public static final class Nested"));
        assertTrue("Should contain deep property", generatedCode.contains("private final String deep"));
        assertTrue("Should contain numbers list", generatedCode.contains("private final List numbers"));
        assertTrue("Should contain flag property", generatedCode.contains("private final Boolean flag"));
    }

    @Test
    public void testEmptyProperties() {
        String generatedCode = generator.generateCode("com.test", "TestConfig", properties);

        assertNotNull("Generated code should not be null", generatedCode);
        assertTrue("Should contain class declaration", generatedCode.contains("public final class TestConfig"));
        assertFalse("Should not contain any private fields", generatedCode.contains("private final"));
    }

    @Test
    public void testSpecialCharactersInPropertyNames() {
        properties.put("special-prop", "value");
        properties.put("another.prop", "value");
        properties.put("weird@prop", "value");

        String generatedCode = generator.generateCode("com.test", "TestConfig", properties);

        assertNotNull("Generated code should not be null", generatedCode);
        assertTrue("Should contain normalized special prop", generatedCode.contains("specialProp"));
        assertTrue("Should contain normalized another prop", generatedCode.contains("anotherProp"));
        assertTrue("Should contain normalized weird prop", generatedCode.contains("weirdProp"));
    }

    @Test
    public void testGeneratedCodeCompilation() throws IOException {
        properties.put("stringProp", "test");
        properties.put("intProp", 42);
        properties.put("listProp", Arrays.asList(1, 2, 3));

        String generatedCode = generator.generateCode("com.test", "TestConfig", properties);

        // Create a temporary file
        File tmp = File.createTempFile("TestConfig", ".java");

        File sourceFile = new File(tmp.getParent() + "/TestConfig.java");
        sourceFile.deleteOnExit();

        // Write the generated code to the file
        java.nio.file.Files.write(sourceFile.toPath(), generatedCode.getBytes());

        // Get the Java compiler
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null);

        // Compile the generated code
        Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromFiles(Arrays.asList(sourceFile));
        JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, diagnostics, null, null, compilationUnits);

        boolean success = task.call();
        fileManager.close();

        diagnostics.getDiagnostics().forEach(d -> {
            System.err.println(d.toString());
        });

        assertTrue("Generated code should compile without errors", success);
        assertEquals("There should be no compilation diagnostics", 0, diagnostics.getDiagnostics().size());
    }
}
