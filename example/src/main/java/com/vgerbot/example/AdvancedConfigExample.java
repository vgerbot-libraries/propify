package com.vgerbot.example;

import com.vgerbot.propify.core.Propify;
import com.vgerbot.propify.lookup.PropifyLookup;

class CustomLookup implements PropifyLookup {

    @Override
    public String getPrefix() {
        return "custom";
    }

    @Override
    public Object lookup(String variable) {
        return "Custom";
    }
}

@Propify(location = "classpath:advanced-config.yml", generatedClassName = "AdvancedConfig", lookups = { CustomLookup.class })
public class AdvancedConfigExample {
    public static void main(String[] args) {
//        AdvancedConfig config = AdvancedConfig.getInstance();
//        System.out.println(config.getAws().getAccessKeyId());
    }
}
