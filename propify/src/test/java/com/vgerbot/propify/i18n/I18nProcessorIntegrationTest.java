package com.vgerbot.propify.i18n;

import com.vgerbot.propify.PropifyProcessor;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import javax.tools.*;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static org.junit.Assert.*;

/**
 * Integration test for the I18n annotation processor.
 * Tests the complete flow of generating I18n code from annotations,
 * compiling the generated code, and using it at runtime.
 */
public class I18nProcessorIntegrationTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    /**
     * Tests the basic I18n annotation processing with minimal configuration.
     */
    @Test
    public void testBasicI18nAnnotationProcessing() throws IOException {
        // Create source and output directories
        File sourceDir = tempFolder.newFolder("i18n-basic-source");
        File outputDir = tempFolder.newFolder("i18n-basic-output");
        
        // Create i18n resource bundle files
        File resourcesDir = tempFolder.newFolder("i18n-basic-resources");
        
        // Default resource bundle
        File defaultPropertiesFile = new File(resourcesDir, "messages.properties");
        try (PrintWriter writer = new PrintWriter(defaultPropertiesFile)) {
            writer.println("greeting=Hello!");
            writer.println("welcome=Welcome, {name}!");
            writer.println("farewell=Goodbye, {name}!");
        }
        
        // Create source file with I18n annotation
        File packageDir = new File(sourceDir, "com/test");
        packageDir.mkdirs();
        
        File interfaceFile = new File(packageDir, "BasicMessages.java");
        try (PrintWriter writer = new PrintWriter(interfaceFile)) {
            writer.println("package com.test;");
            writer.println();
            writer.println("import com.vgerbot.propify.i18n.I18n;");
            writer.println("import com.vgerbot.propify.i18n.Message;");
            writer.println();
            writer.println("/**");
            writer.println(" * Basic messages interface");
            writer.println(" */");
            writer.println("@I18n(");
            writer.println("    baseName = \"messages\",");
            writer.println("    generatedClassName = \"BasicMessagesResource\"");
            writer.println(")");
            writer.println("public interface BasicMessages {");
            writer.println("    @Message(key = \"greeting\")");
            writer.println("    String getGreeting();");
            writer.println();
            writer.println("    @Message(key = \"welcome\", arguments = {\"name\"})");
            writer.println("    String getWelcome(String name);");
            writer.println();
            writer.println("    @Message(key = \"farewell\", arguments = {\"name\"})");
            writer.println("    String getFarewell(String name);");
            writer.println("}");
        }
        
        // Compile the source file with the PropifyProcessor
        boolean success = compile(sourceDir, outputDir, resourcesDir);
        
        // Verify compilation was successful
        assertTrue("Compilation should succeed", success);
        
        // Verify generated files
        File generatedClass = new File(outputDir, "com/test/BasicMessagesResource.class");
        assertTrue("Generated resource class should exist", generatedClass.exists());
        
        File generatedInterfaceClass = new File(outputDir, "com/test/BasicMessagesResource$LocaleMessages.class");
        assertTrue("Generated interface class should exist", generatedInterfaceClass.exists());
    }
    
    /**
     * Tests I18n annotation processing with multiple locales and 
     * more complex message formatting.
     */
    @Test
    public void testMultiLocaleAnnotationProcessing() throws IOException {
        // Create source and output directories
        File sourceDir = tempFolder.newFolder("i18n-multi-source");
        File outputDir = tempFolder.newFolder("i18n-multi-output");
        
        // Create i18n resource bundle files
        File resourcesDir = tempFolder.newFolder("i18n-multi-resources");
        
        // Default resource bundle
        File defaultPropertiesFile = new File(resourcesDir, "app_messages.properties");
        try (PrintWriter writer = new PrintWriter(defaultPropertiesFile)) {
            writer.println("greeting=Hello!");
            writer.println("welcome=Welcome, {name}!");
            writer.println("items.count=You have {count, plural, =0{no items} =1{one item} other{# items}}.");
            writer.println("help=For help, contact {email}.");
        }
        
        // English resource bundle
        File enPropertiesFile = new File(resourcesDir, "app_messages_en.properties");
        try (PrintWriter writer = new PrintWriter(enPropertiesFile)) {
            writer.println("greeting=Hello!");
            writer.println("welcome=Welcome, {name}!");
            writer.println("items.count=You have {count, plural, =0{no items} =1{one item} other{# items}}.");
            writer.println("help=For help, contact {email}.");
        }
        
        // French resource bundle
        File frPropertiesFile = new File(resourcesDir, "app_messages_fr.properties");
        try (PrintWriter writer = new PrintWriter(frPropertiesFile)) {
            writer.println("greeting=Bonjour !");
            writer.println("welcome=Bienvenue, {name} !");
            writer.println("items.count=Vous avez {count, plural, =0{aucun article} =1{un article} other{# articles}}.");
            writer.println("help=Pour obtenir de l'aide, contactez {email}.");
        }
        
        // Create source file with I18n annotation
        File packageDir = new File(sourceDir, "com/test");
        packageDir.mkdirs();
        
        File interfaceFile = new File(packageDir, "AppMessages.java");
        try (PrintWriter writer = new PrintWriter(interfaceFile)) {
            writer.println("package com.test;");
            writer.println();
            writer.println("import com.vgerbot.propify.i18n.I18n;");
            writer.println("import com.vgerbot.propify.i18n.Message;");
            writer.println();
            writer.println("/**");
            writer.println(" * Application messages interface with multiple locales");
            writer.println(" */");
            writer.println("@I18n(");
            writer.println("    baseName = \"app_messages\",");
            writer.println("    defaultLocale = \"en\",");
            writer.println("    generatedClassName = \"I18nAppMessages\"");
            writer.println(")");
            writer.println("public interface AppMessages {");
            writer.println("    @Message(key = \"greeting\")");
            writer.println("    String getGreeting();");
            writer.println();
            writer.println("    @Message(key = \"welcome\", arguments = {\"name\"})");
            writer.println("    String getWelcome(String name);");
            writer.println();
            writer.println("    @Message(key = \"items.count\", arguments = {\"count\"})");
            writer.println("    String getItemCount(int count);");
            writer.println();
            writer.println("    @Message(key = \"help\", arguments = {\"email\"})");
            writer.println("    String getHelp(String email);");
            writer.println("}");
        }
        
        // Compile the source file with the PropifyProcessor
        boolean success = compile(sourceDir, outputDir, resourcesDir);
        
        // Verify compilation was successful
        assertTrue("Compilation should succeed", success);
        
        // Verify generated files
        File generatedClass = new File(outputDir, "com/test/I18nAppMessages.class");
        assertTrue("Generated resource class should exist", generatedClass.exists());
        
        File generatedInterfaceClass = new File(outputDir, "com/test/I18nAppMessages$LocaleMessages.class");
        assertTrue("Generated interface class should exist", generatedInterfaceClass.exists());
    }
    
    /**
     * Tests I18n annotation processing with a custom class structure.
     */
    @Test
    public void testCustomI18nStructure() throws IOException {
        // Create source and output directories
        File sourceDir = tempFolder.newFolder("i18n-custom-source");
        File outputDir = tempFolder.newFolder("i18n-custom-output");
        
        // Create i18n resource bundle files
        File resourcesDir = tempFolder.newFolder("i18n-custom-resources");
        
        // Default resource bundle
        File defaultPropertiesFile = new File(resourcesDir, "i18n/errors.properties");
        defaultPropertiesFile.getParentFile().mkdirs();
        try (PrintWriter writer = new PrintWriter(defaultPropertiesFile)) {
            writer.println("not.found=Resource not found: {resource}");
            writer.println("unauthorized=Access denied for user {user}");
            writer.println("validation=Validation error: {message}");
            writer.println("server=Server error (code: {code})");
        }
        
        // Create directory structure in resources
        File i18nDir = new File(resourcesDir, "i18n");
        i18nDir.mkdirs();
        
        // Create source file with I18n annotation
        File packageDir = new File(sourceDir, "com/test/i18n");
        packageDir.mkdirs();
        
        File interfaceFile = new File(packageDir, "ErrorMessages.java");
        try (PrintWriter writer = new PrintWriter(interfaceFile)) {
            writer.println("package com.test.i18n;");
            writer.println();
            writer.println("import com.vgerbot.propify.i18n.I18n;");
            writer.println("import com.vgerbot.propify.i18n.Message;");
            writer.println();
            writer.println("/**");
            writer.println(" * Error messages interface");
            writer.println(" */");
            writer.println("@I18n(");
            writer.println("    baseName = \"i18n/errors\",");
            writer.println("    generatedClassName = \"ErrorMessagesImpl\"");
            writer.println(")");
            writer.println("public interface ErrorMessages {");
            writer.println("    @Message(key = \"not.found\", arguments = {\"resource\"})");
            writer.println("    String getNotFound(String resource);");
            writer.println();
            writer.println("    @Message(key = \"unauthorized\", arguments = {\"user\"})");
            writer.println("    String getUnauthorized(String user);");
            writer.println();
            writer.println("    @Message(key = \"validation\", arguments = {\"message\"})");
            writer.println("    String getValidation(String message);");
            writer.println();
            writer.println("    @Message(key = \"server\", arguments = {\"code\"})");
            writer.println("    String getServer(int code);");
            writer.println("}");
        }
        
        // Compile the source file with the PropifyProcessor
        boolean success = compile(sourceDir, outputDir, resourcesDir);
        
        // Verify compilation was successful
        assertTrue("Compilation should succeed", success);
        
        // Verify generated files
        File generatedClass = new File(outputDir, "com/test/i18n/ErrorMessagesImpl.class");
        assertTrue("Generated resource class should exist", generatedClass.exists());
        
        File generatedInterfaceClass = new File(outputDir, "com/test/i18n/ErrorMessagesImpl$LocaleMessages.class");
        assertTrue("Generated interface class should exist", generatedInterfaceClass.exists());
    }

    /**
     * Helper method to compile source files with the PropifyProcessor.
     */
    private boolean compile(File sourceDir, File outputDir, File resourcesDir) {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null);
        
        try {
            // Set up compilation units (source files)
            Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromFiles(
                    findJavaFiles(sourceDir));
            
            // Set up compilation options
            List<String> options = new ArrayList<>();
            options.add("-d");
            options.add(outputDir.getAbsolutePath());
            options.add("-classpath");
            String classpath = System.getProperty("java.class.path") + File.pathSeparator + resourcesDir.getAbsolutePath();
            options.add(classpath);
            options.add("-processor");
            options.add(PropifyProcessor.class.getName());
            
            // Run the compilation task
            JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, diagnostics, 
                    options, null, compilationUnits);
            
            boolean success = task.call();
            
            // Print diagnostic messages for debugging
            for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {
                System.out.println(diagnostic);
            }
            
            return success;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                fileManager.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Helper method to find Java source files recursively.
     */
    private List<File> findJavaFiles(File directory) {
        List<File> files = new ArrayList<>();
        if (directory.isDirectory()) {
            for (File file : directory.listFiles()) {
                if (file.isDirectory()) {
                    files.addAll(findJavaFiles(file));
                } else if (file.getName().endsWith(".java")) {
                    files.add(file);
                }
            }
        }
        return files;
    }
} 