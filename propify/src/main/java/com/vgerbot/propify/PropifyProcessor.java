package com.vgerbot.propify;

import com.vgerbot.propify.common.Utils;
import com.vgerbot.propify.core.*;
import com.vgerbot.propify.generator.I18nJavaPoetCodeGenerator;
import com.vgerbot.propify.generator.PropifyCodeGenerator;
import com.vgerbot.propify.i18n.CompileTimePropifyResourceBundleControl;
import com.vgerbot.propify.i18n.I18n;
import com.vgerbot.propify.loader.CompileTimeResourceLoaderProvider;
import com.vgerbot.propify.logger.CompileTimeLogger;
import org.apache.commons.configuration2.Configuration;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.HashSet;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

/**
 * Annotation processor that handles {@link Propify} and {@link I18n} annotations.
 *
 * <p>This processor generates strongly-typed configuration classes from external configuration
 * files and internationalization resource bundles during the compilation phase. It supports:
 * <ul>
 *   <li>Configuration property class generation from various formats (properties, YAML)</li>
 *   <li>Internationalization (i18n) support through resource bundles</li>
 *   <li>Automatic type conversion for configuration values</li>
 *   <li>Compile-time validation of configuration resources</li>
 * </ul>
 *
 * <p>The processor is registered through the Java Service Provider Interface (SPI) mechanism
 * and is automatically picked up by the Java compiler when the propify library is on the
 * annotation processor path.
 *
 * @see Propify The annotation for configuration property generation
 * @see I18n The annotation for internationalization support
 * @since 1.0.0
 */
@SupportedAnnotationTypes({"com.vgerbot.propify.core.Propify", "com.vgerbot.propify.PropifyI18n"})
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class PropifyProcessor extends AbstractProcessor {
    public static ProcessingEnvironment processingEnvironment;
    private Messager messager;

    /**
     *
     *
     * Initializes the annotation processor with the processing environment.
     *
     * <p>This method is called by the Java compiler before any processing begins.
     * It sets up the messager for compile-time logging and diagnostics.
     *
     * @param processingEnv provides access to processing tools and environment
     */
    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        PropifyProcessor.processingEnvironment = processingEnv;
        this.messager = processingEnv.getMessager();
    }

    /**
     * Returns the set of annotation types supported by this processor.
     *
     * @return a set containing the fully qualified names of the supported annotations
     */
    @Override
    public Set<String> getSupportedAnnotationTypes() {
        final Set<String> set = new HashSet<>();
        set.add(Propify.class.getCanonicalName());
        set.add(I18n.class.getCanonicalName());
        return set;
    }

    /**
     * Returns the latest supported source version.
     *
     * @return the latest supported source version
     */
    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    /**
     * Processes annotations found in the source code.
     *
     * <p>This method is called by the compiler for each round of annotation processing.
     * It handles both {@link Propify} and {@link I18n} annotations by:
     * <ul>
     *   <li>Loading and parsing configuration files</li>
     *   <li>Generating type-safe configuration classes</li>
     *   <li>Creating i18n resource bundle wrappers</li>
     *   <li>Reporting any errors encountered during processing</li>
     * </ul>
     *
     * @param annotations the annotation types requested to be processed
     * @param roundEnv    environment for information about the current and prior round
     * @return true if the annotations are claimed by this processor
     */
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
                    if (i18nAnnotation != null) {
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

    /**
     * Processes an {@link I18n} annotation to generate internationalization support code.
     *
     * <p>This method:
     * <ul>
     *   <li>Loads the base resource bundle</li>
     *   <li>Generates a type-safe wrapper class for accessing messages</li>
     *   <li>Creates utility methods for locale handling</li>
     * </ul>
     *
     * @param i18nAnnotation the I18n annotation to process
     * @param element        the annotated type element
     * @throws IOException if there are errors reading resources or writing generated code
     */
    private void processI18nAnnotation(final I18n i18nAnnotation, final TypeElement element) throws IOException {
        // Get package name
        final String packageName = processingEnv.getElementUtils()
                .getPackageOf(element)
                .getQualifiedName()
                .toString();

        String generatedClassName = i18nAnnotation.generatedClassName().replace("$$", element.getSimpleName().toString());

        ResourceBundle resourceBundle = ResourceBundle.getBundle(i18nAnnotation.baseName(), new CompileTimePropifyResourceBundleControl(processingEnvironment));

        // Generate code using JavaPoet
        final String code = I18nJavaPoetCodeGenerator.getInstance()
                .generateCode(packageName, generatedClassName, i18nAnnotation.baseName(), i18nAnnotation.defaultLocale(), resourceBundle);
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

    /**
     * Processes a {@link Propify} annotation to generate configuration classes.
     *
     * <p>This method:
     * <ul>
     *   <li>Creates a processing context</li>
     *   <li>Loads and parses the configuration file</li>
     *   <li>Generates a type-safe configuration class</li>
     *   <li>Handles automatic type conversion if enabled</li>
     * </ul>
     *
     * @param propifyAnnotation the Propify annotation to process
     * @param element           the annotated type element
     * @throws IOException if there are errors reading resources or writing generated code
     */
    private void processPropifyAnnotation(final Propify propifyAnnotation, final TypeElement element) throws IOException {

        AnnotationMirror annotationMirror = getAnnotationMirror(element, Propify.class);

        AnnotationValue annotationLookupsValue = getAnnotationValue(annotationMirror, "lookups");
        String[] lookups = Utils.getClassesFromAnnotationValue(annotationLookupsValue);

        // Create context and load configuration
        final PropifyContext context = new PropifyContext(
                propifyAnnotation.location(),
                propifyAnnotation.mediaType(),
                propifyAnnotation.generatedClassName(),
                propifyAnnotation.listDelimiter(),
                lookups,
                new CompileTimeResourceLoaderProvider(processingEnv),
                new CompileTimeLogger(processingEnv)
        );
        PropifyConfigParserProvider provider = PropifyConfigParserProvider.getInstance();
        // Load and parse properties
        PropifyProperties properties;
        PropifyPropertiesBuilder propifyPropertiesBuilder = new PropifyPropertiesBuilder();
        try (InputStream stream = context.loadResource()) {
            PropifyConfigParser parser = provider.getParser(context);
            Configuration configuration = parser.parse(context, stream);

            propifyPropertiesBuilder.config(configuration);
        }
        properties = propifyPropertiesBuilder.build();

        // Generate code
        final String packageName = processingEnv.getElementUtils()
                .getPackageOf(element)
                .getQualifiedName()
                .toString();

        final String generatedClassName = context.generateClassName(element.getSimpleName().toString());

        final String code = PropifyCodeGenerator.getInstance()
                .generateCode(packageName, generatedClassName, context, properties);

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

    private static AnnotationMirror getAnnotationMirror(TypeElement typeElement, Class<?> clazz) {
        String clazzName = clazz.getName();
        for(AnnotationMirror m : typeElement.getAnnotationMirrors()) {
            if(m.getAnnotationType().toString().equals(clazzName)) {
                return m;
            }
        }
        return null;
    }

    private static AnnotationValue getAnnotationValue(AnnotationMirror annotationMirror, String key) {
        if (annotationMirror == null) {
            return null;
        }
        for(Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : annotationMirror.getElementValues().entrySet() ) {
            if(entry.getKey().getSimpleName().toString().equals(key)) {
                return entry.getValue();
            }
        }
        return null;
    }
}
