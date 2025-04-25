package com.vgerbot.propify.i18n;

import javax.annotation.processing.ProcessingEnvironment;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.InputStream;

public class CompileTimePropifyResourceBundleControl extends AbstractPropifyResourceBundleControl {
    private final ProcessingEnvironment processingEnvironment;

    public CompileTimePropifyResourceBundleControl(ProcessingEnvironment processingEnvironment) {
        this.processingEnvironment = processingEnvironment;
    }
    @Override
    protected InputStream loadResource(String resourceName, ClassLoader loader, boolean reloadFlag) throws IOException {
        FileObject fileObject = processingEnvironment.getFiler().getResource(StandardLocation.CLASS_PATH, "", resourceName);
        return fileObject.openInputStream();
    }
}
