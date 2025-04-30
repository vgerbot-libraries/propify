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
        assertTrue(generatedCode.contains("String greeting(String name)"));
        assertTrue(generatedCode.contains("String welcome(String city, String name)"));
        assertTrue(generatedCode.contains("@Message(\n" +
                "        key = \"greeting\",\n" +
                "        arguments = {\"name\"}\n" +
                "    )"));
        assertTrue(generatedCode.contains("@Message(\n" +
                "        key = \"welcome\",\n" +
                "        arguments = {\"city\",\"name\"}\n" +
                "    )"));
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

    @Test
    public void testGenerateCodeWithPluralForms() {
        ResourceBundle bundle = new ListResourceBundle() {
            @Override
            protected Object[][] getContents() {
                return new Object[][] {
                    {"items", "You have {count, plural, =0{no items} =1{one item} other{# items}}"},
                    {"apples", "There {count, plural, =0{are no apples} =1{is one apple} other{are # apples}}"}
                };
            }
        };

        String generatedCode = I18nJavaPoetCodeGenerator.getInstance()
            .generateCode("com.example", "Messages", "messages", "", bundle);

        assertNotNull(generatedCode);

        assertTrue(generatedCode.contains("String items(Number count)"));
        assertTrue(generatedCode.contains("String apples(Number count)"));
        assertTrue(generatedCode.contains("@Message(\n" +
                "        key = \"items\",\n" +
                "        arguments = {\"count\"}\n" +
                "    )"));
    }

    @Test
    public void testGenerateCodeWithSelectForms() {
        ResourceBundle bundle = new ListResourceBundle() {
            @Override
            protected Object[][] getContents() {
                return new Object[][] {
                    {"gender", "{gender, select, male{He} female{She} other{They}} likes this."},
                    {"animal", "The {animal, select, cat{cat} dog{dog} other{animal}} is cute."}
                };
            }
        };

        String generatedCode = I18nJavaPoetCodeGenerator.getInstance()
            .generateCode("com.example", "Messages", "messages", "", bundle);

        assertNotNull(generatedCode);
        assertTrue(generatedCode.contains("String gender(String gender)"));
        assertTrue(generatedCode.contains("String animal(String animal)"));
        assertTrue(generatedCode.contains("@Message(\n" +
                "        key = \"gender\",\n" +
                "        arguments = {\"gender\"}\n" +
                "    )"));
    }

    @Test
    public void testGenerateCodeWithDateFormats() {
        ResourceBundle bundle = new ListResourceBundle() {
            @Override
            protected Object[][] getContents() {
                return new Object[][] {
                    {"meeting", "The meeting is scheduled for {date, date, full} at {time, time, short}"},
                    {"deadline", "The deadline is {date, date, medium}"}
                };
            }
        };

        String generatedCode = I18nJavaPoetCodeGenerator.getInstance()
            .generateCode("com.example", "Messages", "messages", "", bundle);

        assertNotNull(generatedCode);
        assertTrue(generatedCode.contains("String meeting(Date date, Date time)"));
        assertTrue(generatedCode.contains("String deadline(Date date)"));
        assertTrue(generatedCode.contains("@Message(\n" +
                "        key = \"meeting\",\n" +
                "        arguments = {\"date\",\"time\"}\n" +
                "    )"));
    }

    @Test
    public void testGenerateCodeWithNumberFormats() {
        ResourceBundle bundle = new ListResourceBundle() {
            @Override
            protected Object[][] getContents() {
                return new Object[][] {
                    {"price", "The price is {amount, number, currency}"},
                    {"percentage", "The completion rate is {rate, number, percent}"}
                };
            }
        };

        String generatedCode = I18nJavaPoetCodeGenerator.getInstance()
            .generateCode("com.example", "Messages", "messages", "", bundle);

        assertNotNull(generatedCode);
        assertTrue(generatedCode.contains("String price(Number amount)"));
        assertTrue(generatedCode.contains("String percentage(Number rate)"));
        assertTrue(generatedCode.contains("@Message(\n" +
                "        key = \"price\",\n" +
                "        arguments = {\"amount\"}\n" +
                "    )"));
    }

    @Test
    public void testGenerateCodeWithComplexCombination() {
        ResourceBundle bundle = new ListResourceBundle() {
            @Override
            protected Object[][] getContents() {
                return new Object[][] {
                    {"order", "{count, plural, =0{No orders} =1{One order} other{# orders}} placed by {gender, select, male{him} female{her} other{them}} on {date, date, medium} for {amount, number, currency}"},
                    {"notification", "{type, select, email{Email} sms{SMS} other{Message}} sent to {recipients, plural, =0{no one} =1{one recipient} other{# recipients}} at {time, time, short}"}
                };
            }
        };

        String generatedCode = I18nJavaPoetCodeGenerator.getInstance()
            .generateCode("com.example", "Messages", "messages", "", bundle);

        assertNotNull(generatedCode);
        assertTrue(generatedCode.contains("String order(Number count, String gender, Date date, Number amount)"));
        assertTrue(generatedCode.contains("String notification(String type, Number recipients, Date time)"));
        assertTrue(generatedCode.contains("@Message(\n" +
                "        key = \"order\",\n" +
                "        arguments = {\"count\",\"gender\",\"date\",\"amount\"}\n" +
                "    )"));
    }
}
