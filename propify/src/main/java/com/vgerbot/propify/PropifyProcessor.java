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
        final Set<String> set = new HashSet<>();
        set.add(Propify.class.getCanonicalName());
        return set;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
        for (final TypeElement annotation : annotations) {
            final Set<? extends Element> annotatedElements = roundEnv.getElementsAnnotatedWith(annotation);
            
            for (final Element element : annotatedElements) {
                if (!(element instanceof TypeElement)) {
                    continue;
                }
                
                try {
                    processElement((TypeElement) element);
                } catch (Exception e) {
                    String message = e.getMessage();
                    if (message != null) {
                        if (message.contains("No parser found for media type")) {
                            messager.printMessage(Diagnostic.Kind.ERROR, 
                                "No parser found for media type", element);
                        } else if (message.contains("Could not find resource")) {
                            messager.printMessage(Diagnostic.Kind.ERROR, 
                                "Could not find resource", element);
                        } else {
                            messager.printMessage(Diagnostic.Kind.ERROR, 
                                "Failed to process @Propify annotation: " + message, element);
                        }
                    } else {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        return true;
    }

    private void processElement(final TypeElement element) throws IOException {
        Propify propifyAnnotation = element.getAnnotation(Propify.class);
        if (propifyAnnotation == null) {
            return;
        }

        // Create context and load configuration
        final PropifyContext context = new PropifyContext(propifyAnnotation, processingEnv);
        
        // Load and parse properties
        PropifyProperties properties;
        try (InputStream stream = context.loadResource()) {
            PropifyConfigParser parser = context.getParser();
            properties = parser.parse(context, stream);
        }

        // Generate code
        final String packageName = processingEnv.getElementUtils()
            .getPackageOf(element)
            .getQualifiedName()
            .toString();
        final String generatedClassName = context.getClassName(element.getSimpleName().toString());

        final String code = JavaPoetCodeGenerator.getInstance()
            .generateCode(packageName, generatedClassName, properties);

        // Write generated file
        final JavaFileObject file = processingEnv.getFiler()
            .createSourceFile(packageName + "." + generatedClassName);
            
        try (Writer writer = file.openWriter()) {
            writer.write(code);
        }

        messager.printMessage(
            Diagnostic.Kind.NOTE,
            "Generated " + generatedClassName + " from " + propifyAnnotation.location(),
            element
        );
    }
}
