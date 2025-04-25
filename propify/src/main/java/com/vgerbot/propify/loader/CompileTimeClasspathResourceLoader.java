package com.vgerbot.propify.loader;

import com.vgerbot.propify.core.ResourceLoader;

import javax.annotation.processing.ProcessingEnvironment;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.InputStream;

public class CompileTimeClasspathResourceLoader implements ResourceLoader {
    private static final String CLASSPATH_PREFIX = "classpath:";
    private final ProcessingEnvironment processingEnvironment;
    public CompileTimeClasspathResourceLoader(ProcessingEnvironment processingEnvironment) {
        if (processingEnvironment == null) {
            throw new IllegalArgumentException("ProcessingEnvironment cannot be null");
        }
        this.processingEnvironment = processingEnvironment;
    }
    @Override
    public boolean accept(String location) {
        if (location == null) {
            return false;
        }
        return location.startsWith(CLASSPATH_PREFIX);
    }

    @Override
    public InputStream load(String location) throws IOException {
        if (location == null) {
            throw new IllegalArgumentException("Location cannot be null");
        }
        if (!location.startsWith(CLASSPATH_PREFIX)) {
            throw new IllegalArgumentException("Location must start with 'classpath:': " + location);
        }

        String filePath = location.substring(CLASSPATH_PREFIX.length()).trim();
        if (filePath.isEmpty()) {
            throw new IllegalArgumentException("Classpath resource path cannot be empty");
        }

        try {
            processingEnvironment.getMessager().printMessage(
                    Diagnostic.Kind.NOTE,
                    "loading classpath source: " + filePath
            );

            FileObject fileObject = processingEnvironment.getFiler().getResource(
                    StandardLocation.CLASS_PATH,
                    "",
                    filePath
            );

            if (fileObject == null) {
                throw new IOException("Could not find resource: " + filePath);
            }

            return fileObject.openInputStream();
        } catch (IOException e) {
            throw new IOException("Could not find resource: " + filePath, e);
        } catch (Exception e) {
            throw new IOException("Failed to load resource: " + filePath, e);
        }
    }
}
