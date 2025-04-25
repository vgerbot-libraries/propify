package com.vgerbot.propify.i18n;

import org.apache.commons.configuration2.*;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.interpol.ConfigurationInterpolator;
import org.apache.commons.configuration2.io.FileHandler;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public abstract class AbstractPropifyResourceBundleControl extends ResourceBundle.Control {
    @Override
    public List<String> getFormats(String s) {
        return Arrays.asList("java.properties", "java.class", "xml", "yaml", "json", "ini", "xml.properties");
    }

    @Override
    public ResourceBundle newBundle(String baseName, Locale locale, String format, ClassLoader loader, boolean reload) throws IllegalAccessException, InstantiationException, IOException {
        if ("java.class".equals(format)) {
            return super.newBundle(baseName, locale, format, loader, reload);
        }
        String bundleName = this.toBundleName(baseName, locale);
        FileBasedConfiguration configuration = null;
        String fileName = null;
        switch(format) {
            case "java.properties": {
                fileName = bundleName + ".properties";
                configuration = new PropertiesConfiguration();
            }
            break;
            case "xml": {
                fileName = bundleName + ".xml";
                configuration = new XMLConfiguration();
            }
            break;
            case "yaml": {
                fileName = bundleName + ".yaml";
                configuration = new YAMLConfiguration();
            }
            break;
            case "json": {
                fileName = bundleName + ".json";
                configuration = new JSONConfiguration();
            }
            break;
            case "ini": {
                fileName = bundleName + ".ini";
                configuration = new INIConfiguration();
            }
            break;
            case "xml.properties": {
                fileName = bundleName + ".properties.xml";
                configuration = new XMLPropertiesConfiguration();
            }
            break;
            default:
                throw new IllegalArgumentException("unknown format: " + format);
        }
        ICULookup icu = new ICULookup();
        ConfigurationInterpolator interpolator = configuration.getInterpolator();
        interpolator.registerLookup("icu", icu);
        interpolator.registerLookup("args", icu);
        try {
            InputStream stream = this.loadResource(fileName, loader, reload);
            if (stream == null) {
                return null;
            }
            FileHandler handler = new FileHandler(configuration);
            handler.setEncoding(StandardCharsets.UTF_8.name());
            handler.load(stream);
            return new PropifyResourceBundle(configuration);
        } catch(ConfigurationException e) {
            return null;
        }
    }
    protected abstract InputStream loadResource(String resourceName, ClassLoader loader, boolean reloadFlag) throws IOException;
}
