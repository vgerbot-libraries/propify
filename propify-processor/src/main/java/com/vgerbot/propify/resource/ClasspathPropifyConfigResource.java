package com.vgerbot.propify.resource;

import com.vgerbot.propify.PropifyConfigResource;

import javax.annotation.processing.ProcessingEnvironment;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.InputStream;

public class ClasspathPropifyConfigResource implements PropifyConfigResource {
    @Override
    public Boolean accept(String location) {
        if (location.startsWith("classpath:")) {
            return true;
        }
        return false;
    }

    @Override
    public InputStream load(ProcessingEnvironment processingEnvironment, String location) throws IOException {
        String filePath = location.substring("classpath:".length()).trim();
        processingEnvironment.getMessager().printMessage(Diagnostic.Kind.NOTE, "loading classpath source: " + filePath);
        FileObject fileObject = processingEnvironment.getFiler().getResource(
                StandardLocation.CLASS_PATH,
                "",
                filePath
        );
        return fileObject.openInputStream();
    }
}
