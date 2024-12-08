package com.vgerbot.propify;

import com.vgerbot.propify.generator.JavaPoetCodeGenerator;
import com.vgerbot.propify.generator.I18nJavaPoetCodeGenerator;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.*;

@SupportedAnnotationTypes({"com.vgerbot.propify.Propify", "com.vgerbot.propify.PropifyI18n"})
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
        set.add(I18n.class.getCanonicalName());
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
                    Propify propifyAnnotation = element.getAnnotation(Propify.class);
                    if (propifyAnnotation != null) {
                        processPropifyAnnotation(propifyAnnotation, (TypeElement) element);
                    }
                    I18n i18nAnnotation = element.getAnnotation(I18n.class);
                    if(i18nAnnotation != null) {
                        processI18nAnnotation(i18nAnnotation, (TypeElement) element);
                    }
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
                                "Failed to process annotation: " + message, element);
                        }
                    } else {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        return true;
    }

    private void processI18nAnnotation(final I18n i18nAnnotation, final TypeElement element) throws IOException {
        // Get package name
        final String packageName = processingEnv.getElementUtils()
            .getPackageOf(element)
            .getQualifiedName()
            .toString();

        String generatedClassName = i18nAnnotation.generatedClassName().replace("$$", element.getSimpleName().toString());
        FileObject resource = processingEnv.getFiler().getResource(StandardLocation.CLASS_PATH, "", i18nAnnotation.baseName() + ".properties");
        ResourceBundle resourceBundle = new PropertyResourceBundle(resource.openInputStream());
        // Generate code using JavaPoet
        final String code = I18nJavaPoetCodeGenerator.getInstance()
            .generateCode(packageName, generatedClassName, i18nAnnotation.baseName(), i18nAnnotation.defaultLocale(), resourceBundle);
        System.err.println(code);
        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, code);
        // Write generated file
        final JavaFileObject file = processingEnv.getFiler()
            .createSourceFile(packageName + "." + generatedClassName);
            
        try (Writer writer = file.openWriter()) {
            writer.write(code);
        }

        messager.printMessage(
            Diagnostic.Kind.NOTE,
            "Generated MessageResource for i18n support",
            element
        );
    }

    private void processPropifyAnnotation(final Propify propifyAnnotation, final TypeElement element) throws IOException {
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
