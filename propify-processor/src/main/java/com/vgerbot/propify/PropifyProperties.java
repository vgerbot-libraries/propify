package com.vgerbot.propify;

import java.util.HashMap;

public class PropifyProperties extends HashMap<String, Object> {
    @Override
    public Object put(String s, Object o) {
        return super.put(s, Utils.parseValue(o));
    }
}
