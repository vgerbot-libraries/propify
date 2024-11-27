package com.vgerbot.propify;

import java.util.HashMap;

public class PropifyProperties extends HashMap<String, Object> {
    @Override
    public Object put(String key, Object value) {
        if(value instanceof PropifyProperties) {
            return super.put(key, value);
        }
        return super.put(key, Utils.parseValue(value));
    }
}
