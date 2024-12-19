package com.vgerbot.propify.lookup;

import org.apache.commons.configuration2.interpol.Lookup;

public class PropifyLookupAdaptor implements Lookup {
    private final PropifyLookup lookup;

    public PropifyLookupAdaptor(PropifyLookup lookup) {
        this.lookup = lookup;
    }
    @Override
    public Object lookup(String variable) {
        return lookup.lookup(variable);
    }
}
