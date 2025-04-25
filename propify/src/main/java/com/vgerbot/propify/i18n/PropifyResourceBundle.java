package com.vgerbot.propify.i18n;

import org.apache.commons.configuration2.Configuration;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.ResourceBundle;

public class PropifyResourceBundle extends ResourceBundle {
    private final Configuration configuration;
    public PropifyResourceBundle(Configuration configuration) {
        super();
        this.configuration = configuration;
    }


    @Override
    protected Object handleGetObject(String key) {
        return configuration.getString(key);
    }

    @Override
    public Enumeration<String> getKeys() {
        Iterator<String> keys = configuration.getKeys();
        return new Enumeration<String>() {
            @Override
            public boolean hasMoreElements() {
                return keys.hasNext();
            }

            @Override
            public String nextElement() {
                return keys.next();
            }
        };
    }
}
