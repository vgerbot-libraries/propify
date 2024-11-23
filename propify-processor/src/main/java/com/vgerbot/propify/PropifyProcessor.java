package com.vgerbot.propify;

import com.vgerbot.propify.generator.JavaPoetCodeGenerator;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.HashSet;
import java.util.Set;

/**
 * Annotation processor for the @Propify annotation.
 * Generates type-safe property access classes from property files.
 */
@SupportedAnnotationTypes("com.vgerbot.propify.Propify")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class PropifyProcessor extends AbstractProcessor {
    private Messager messager;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.messager = processingEnv.getMessager();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> set = new HashSet<>();
        set.add(Propify.class.getCanonicalName());
        return set;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (TypeElement annotation : annotations) {
            Set<? extends Element> annotatedElements = roundEnv.getElementsAnnotatedWith(annotation);
            
            for (Element element : annotatedElements) {
                try {
                    processElement(element);
                } catch (Exception e) {
                    // Report error with element and stack trace
                    messager.printMessage(
                        Diagnostic.Kind.ERROR,
                        "Failed to process @Propify annotation: " + e.getMessage() + "\n" + 
                        getStackTrace(e),
                        element
                    );
                }
            }
        }
        return true;
    }

    private void processElement(Element element) throws IOException {
        if (!(element instanceof TypeElement)) {
            return;
        }

        TypeElement typeElement = (TypeElement) element;
        Propify propifyAnnotation = typeElement.getAnnotation(Propify.class);

        if (propifyAnnotation == null) {
            return;
        }

        // Create context and validate configuration
        PropifyContext context = new PropifyContext(propifyAnnotation, processingEnv);
        PropifyConfigParser parser = context.getParser();
        
        if (parser == null) {
            messager.printMessage(
                Diagnostic.Kind.ERROR,
                "No parser found for media type: " + propifyAnnotation.mediaType(),
                element
            );
            return;
        }

        // Load and parse properties
        PropifyProperties properties;
        try (InputStream stream = context.loadResource()) {
            if (stream == null) {
                messager.printMessage(
                    Diagnostic.Kind.ERROR,
                    "Could not find resource: " + propifyAnnotation.location(),
                    element
                );
                return;
            }
            properties = parser.parse(stream);
        }

        // Generate code
        String packageName = processingEnv.getElementUtils()
            .getPackageOf(typeElement)
            .getQualifiedName()
            .toString();
        String generatedClassName = typeElement.getSimpleName() + "Propify";

        String code = JavaPoetCodeGenerator.getInstance()
            .generateCode(packageName, generatedClassName, properties);

        // Write generated file
        JavaFileObject file = processingEnv.getFiler()
            .createSourceFile(packageName + "." + generatedClassName);
            
        try (Writer writer = file.openWriter()) {
            writer.write(code);
        }

        messager.printMessage(
            Diagnostic.Kind.NOTE,
            "Successfully generated " + generatedClassName + " for " + propifyAnnotation.location(),
            element
        );
    }

    private String getStackTrace(Exception e) {
        StringBuilder sb = new StringBuilder();
        for (StackTraceElement element : e.getStackTrace()) {
            sb.append("\tat ").append(element.toString()).append("\n");
        }
        return sb.toString();
    }
}
