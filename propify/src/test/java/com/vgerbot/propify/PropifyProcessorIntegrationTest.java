package com.vgerbot.propify;

import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import javax.tools.*;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Integration test for the PropifyProcessor.
 * This test compiles sample source files with the annotation processor
 * and verifies the output.
 */
@RunWith(JUnit4.class)
public class PropifyProcessorIntegrationTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Test
    public void testPropifyAnnotationProcessing() throws IOException {
        // Create source files
        File sourceDir = tempFolder.newFolder("source");
        File outputDir = tempFolder.newFolder("output");
        
        // Create a test configuration file
        File resourcesDir = tempFolder.newFolder("resources");
        File configFile = new File(resourcesDir, "test-config.properties");
        try (PrintWriter writer = new PrintWriter(configFile)) {
            writer.println("app.name=Test Application");
            writer.println("app.version=1.0.0");
            writer.println("database.url=jdbc:mysql://localhost:3306/testdb");
            writer.println("database.username=admin");
            writer.println("database.password=password");
        }
        
        // Create a test source file with the Propify annotation
        File packageDir = new File(sourceDir, "com/test");
        packageDir.mkdirs();
        File sourceFile = new File(packageDir, "AppConfig.java");
        try (PrintWriter writer = new PrintWriter(sourceFile)) {
            writer.println("package com.test;");
            writer.println();
            writer.println("import com.vgerbot.propify.core.Propify;");
            writer.println();
            writer.println("/**");
            writer.println(" * Test configuration class");
            writer.println(" */");
            writer.println("@Propify(");
            writer.println("    location = \"file:"+configFile.getAbsolutePath()+"\",");
            writer.println("    mediaType = \"text/x-java-properties\",");
            writer.println("    generatedClassName = \"$$Generated\"");
            writer.println(")");
            writer.println("public interface AppConfig {");
            writer.println("    String getAppName();");
            writer.println("    String getAppVersion();");
            writer.println("    String getDatabaseUrl();");
            writer.println("    String getDatabaseUsername();");
            writer.println("    String getDatabasePassword();");
            writer.println("}");
        }
        
        // Compile the test source file with the PropifyProcessor
        boolean success = compile(sourceDir, outputDir, resourcesDir);
        
        // Verify compilation was successful
        assertTrue("Compilation should succeed", success);
        
        // Verify the generated file exists
        File generatedFile = new File(outputDir, "com/test/AppConfigGenerated.class");
        assertTrue("Generated file should exist", generatedFile.exists());
    }
    
    @Test
    public void testI18nAnnotationProcessing() throws IOException {
        // Create source files
        File sourceDir = tempFolder.newFolder("i18n-source");
        File outputDir = tempFolder.newFolder("i18n-output");
        
        // Create i18n resource bundle files
        File resourcesDir = tempFolder.newFolder("i18n-resources");
        File messagesFile = new File(resourcesDir, "messages.properties");
        try (PrintWriter writer = new PrintWriter(messagesFile)) {
            writer.println("welcome=Welcome to our application");
            writer.println("error.notFound=Resource not found");
            writer.println("error.unauthorized=Access denied");
        }
        
        // Create a test source file with the I18n annotation
        File packageDir = new File(sourceDir, "com/test");
        packageDir.mkdirs();
        File sourceFile = new File(packageDir, "Messages.java");
        try (PrintWriter writer = new PrintWriter(sourceFile)) {
            writer.println("package com.test;");
            writer.println();
            writer.println("import com.vgerbot.propify.i18n.I18n;");
            writer.println("import java.util.Locale;");
            writer.println();
            writer.println("/**");
            writer.println(" * Test messages interface");
            writer.println(" */");
            writer.println("@I18n(");
            writer.println("    baseName = \"messages\",");
            writer.println("    defaultLocale = \"en\",");
            writer.println("    generatedClassName = \"$$Messages\"");
            writer.println(")");
            writer.println("public interface Messages {");
            writer.println("    String getWelcome();");
            writer.println("    String getErrorNotFound();");
            writer.println("    String getErrorUnauthorized();");
            writer.println("}");
        }
        
        // Compile the test source file with the PropifyProcessor
        boolean success = compile(sourceDir, outputDir, resourcesDir);
        
        // Verify compilation was successful
        assertTrue("Compilation should succeed", success);
        
        // Verify the generated file exists
        File generatedFile = new File(outputDir, "com/test/MessagesMessages.class");
        assertTrue("Generated I18n file should exist", generatedFile.exists());
    }
    
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