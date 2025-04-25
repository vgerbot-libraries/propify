package com.vgerbot.propify.i18n;

import org.apache.commons.configuration2.interpol.Lookup;

public class ICULookup implements Lookup  {
    @Override
    public Object lookup(String variable) {
        return "{" + variable + "}";
    }
}
