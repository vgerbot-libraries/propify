package com.vgerbot.propify;

import com.squareup.javapoet.ClassName;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.HashSet;
import java.util.Set;


@SupportedAnnotationTypes("com.vgerbot.propify.Propify")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class PropifyProcessor extends AbstractProcessor {

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
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        
        for (TypeElement annotation : annotations) {
            Set<? extends Element> annotatedElements = roundEnv.getElementsAnnotatedWith(annotation);
            
            for (Element element : annotatedElements) {
                if (!(element instanceof TypeElement)) {
                    continue;
                }
                TypeElement typeElement = (TypeElement) element;
                Propify propifyAnnotation = typeElement.getAnnotation(Propify.class);

                if (propifyAnnotation == null) {
                    continue;
                }
                PropifyContext context = new PropifyContext(propifyAnnotation, processingEnv);
                PropifyConfigParser parser = context.getParser();
                PropifyProperties properties;
                try(InputStream stream = context.loadResource()) {
                    properties = parser.parse(stream);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                String packageName = processingEnv.getElementUtils().getPackageOf(typeElement).getQualifiedName().toString();
                String generatedClassName = typeElement.getSimpleName() + "Propify";

                String code = PropifyCodeGenerator.getInstance().generateCode(packageName, generatedClassName, properties);

                try {
                    JavaFileObject file = processingEnv.getFiler()
                        .createSourceFile(packageName + "." + generatedClassName);
                    try (Writer writer = file.openWriter()) {
                        writer.write(code);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return true;
    }

}
