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
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        System.out.println("Hello world!");
        
        for (TypeElement annotation : annotations) {
            Set<? extends Element> annotatedElements = roundEnv.getElementsAnnotatedWith(annotation);
            
            for (Element element : annotatedElements) {
                if (element instanceof TypeElement) {
                    TypeElement typeElement = (TypeElement) element;
                    Propify propifyAnnotation = typeElement.getAnnotation(Propify.class);
                    
                    if (propifyAnnotation != null) {
                        String propertiesPath = propifyAnnotation.value();
                        String className = typeElement.getSimpleName().toString();
                        String packageName = processingEnv.getElementUtils()
                            .getPackageOf(typeElement).getQualifiedName().toString();
                        String generatedClassName = className + "Generated";
                        
                        try {
                            JavaFileObject file = processingEnv.getFiler()
                                .createSourceFile(packageName + "." + generatedClassName);
                                
                            try (Writer writer = file.openWriter()) {
                                writer.write("package " + packageName + ";\n\n");
                                writer.write("import java.util.Properties;\n");
                                writer.write("import java.io.InputStream;\n\n");
                                writer.write("public class " + generatedClassName + " {\n");
                                writer.write("    private final Properties properties = new Properties();\n\n");
                                writer.write("    public " + generatedClassName + "() {\n");
                                writer.write("        try {\n");
                                writer.write("            String path = \"" + propertiesPath.replace("classpath:", "") + "\";\n");
                                writer.write("            InputStream input = getClass().getClassLoader().getResourceAsStream(path);\n");
                                writer.write("            if (input != null) {\n");
                                writer.write("                properties.load(input);\n");
                                writer.write("                input.close();\n");
                                writer.write("            } else {\n");
                                writer.write("                throw new RuntimeException(\"Could not find properties file: \" + path);\n");
                                writer.write("            }\n");
                                writer.write("        } catch (Exception e) {\n");
                                writer.write("            throw new RuntimeException(\"Failed to load properties file\", e);\n");
                                writer.write("        }\n");
                                writer.write("    }\n\n");
                                writer.write("    public String getProperty(String key) {\n");
                                writer.write("        return properties.getProperty(key);\n");
                                writer.write("    }\n");
                                writer.write("}\n");
                            }
                        } catch (IOException e) {
                            throw new RuntimeException("Failed to create source file", e);
                        }
                    }
                }
            }
        }
        return true;
    }
}
