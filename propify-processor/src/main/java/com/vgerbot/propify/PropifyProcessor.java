package com.vgerbot.propify;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;
import java.io.IOException;
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
                PropifyProperties properties = new PropifyProperties();
                properties.put("a", 1);
                properties.put("b", 2L);
                properties.put("c", true);
                PropifyProperties sub = new PropifyProperties();
                sub.put("a", "string");
                properties.put("sub", sub);
                String packageName = "com.vgerbot.propify";
                String generatedClassName = "PropsGen";
                String code = PropifyCodeGenerator.getInstance().generateCode(packageName, generatedClassName, properties);
                System.out.println(code);

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
