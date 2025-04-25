package com.vgerbot.propify.generator;

import org.junit.Test;

import java.util.ListResourceBundle;
import java.util.ResourceBundle;

import static org.junit.Assert.*;

public class I18nJavaPoetCodeGeneratorTest {
    
    @Test
    public void testGenerateCodeWithBasicMessages() {
        ResourceBundle bundle = new ListResourceBundle() {
            @Override
            protected Object[][] getContents() {
                return new Object[][] {
                    {"hello", "Hello"},
                    {"welcome", "Welcome"}
                };
            }
        };

        String generatedCode = I18nJavaPoetCodeGenerator.getInstance()
            .generateCode("com.example", "Messages", "messages", "", bundle);

        assertNotNull(generatedCode);
        final String cleanedCode = generatedCode.replaceAll("\\s+", " ").trim();
        assertTrue(cleanedCode.contains("public interface LocaleMessages"));
        assertTrue(cleanedCode.contains("String hello()"));
        assertTrue(cleanedCode.contains("String welcome()"));
        assertTrue(cleanedCode.contains("@Message( key = \"hello\", arguments = {} )"));
    }

    @Test
    public void testGenerateCodeWithParameterizedMessages() {
        ResourceBundle bundle = new ListResourceBundle() {
            @Override
            protected Object[][] getContents() {
                return new Object[][] {
                    {"greeting", "Hello {name}"},
                    {"welcome", "Welcome to {city}, {name}"}
                };
            }
        };

        String generatedCode = I18nJavaPoetCodeGenerator.getInstance()
            .generateCode("com.example", "Messages", "messages", "", bundle);

        assertNotNull(generatedCode);
        final String cleanedCode = generatedCode.replaceAll("\\s+", " ").trim();
        assertTrue(cleanedCode.contains("String greeting(String name)"));
        assertTrue(cleanedCode.contains("String welcome(String city, String name)"));
        assertTrue(cleanedCode.contains("@Message( key = \"greeting\", arguments = {\"name\"} )"));
        assertTrue(cleanedCode.contains("@Message( key = \"welcome\", arguments = {\"city\",\"name\"} )"));
    }

    @Test
    public void testGenerateCodeWithDefaultLocale() {
        ResourceBundle bundle = new ListResourceBundle() {
            @Override
            protected Object[][] getContents() {
                return new Object[][] {
                    {"hello", "Hello"}
                };
            }
        };

        String generatedCode = I18nJavaPoetCodeGenerator.getInstance()
            .generateCode("com.example", "Messages", "messages", "en-US", bundle);

        assertNotNull(generatedCode);
        assertTrue(generatedCode.contains("return get(Locale.forLanguageTag(\"en-US\"))"));
    }

    @Test
    public void testGenerateCodeWithEmptyDefaultLocale() {
        ResourceBundle bundle = new ListResourceBundle() {
            @Override
            protected Object[][] getContents() {
                return new Object[][] {
                    {"hello", "Hello"}
                };
            }
        };

        String generatedCode = I18nJavaPoetCodeGenerator.getInstance()
            .generateCode("com.example", "Messages", "messages", "", bundle);

        assertNotNull(generatedCode);
        assertTrue(generatedCode.contains("return get(Locale.getDefault())"));
    }

    @Test
    public void testResourceBundleFieldGeneration() {
        ResourceBundle bundle = new ListResourceBundle() {
            @Override
            protected Object[][] getContents() {
                return new Object[][] {
                    {"hello", "Hello"}
                };
            }
        };

        String generatedCode = I18nJavaPoetCodeGenerator.getInstance()
            .generateCode("com.example", "Messages", "custom.messages", "", bundle);

        assertNotNull(generatedCode);
        assertTrue(generatedCode.contains("private static final PropifyI18nResourceBundle resourceBundle"));
        assertTrue(generatedCode.contains("new PropifyI18nResourceBundle(\"custom.messages\""));
    }
}
