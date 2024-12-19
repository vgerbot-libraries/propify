package com.vgerbot.example.jdk8;

import org.apache.commons.configuration2.YAMLConfiguration;
import org.apache.commons.configuration2.interpol.ConfigurationInterpolator;
import org.apache.commons.configuration2.interpol.DefaultLookups;
import org.apache.commons.configuration2.io.FileHandler;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class Main {
    public static void main(String[] args) throws Exception {
        System.out.println(System.getenv().get("HOME"));
        YAMLConfiguration yamlConfiguration = new YAMLConfiguration();
        InputStream stream = Main.class.getClassLoader().getResourceAsStream("advanced-config.yml");

        ConfigurationInterpolator interpolator = yamlConfiguration.getInterpolator();
        interpolator.registerLookup("env", DefaultLookups.ENVIRONMENT.getLookup());
//        yamlConfiguration.read(stream);
//        stream.close();
        FileHandler handler = new FileHandler(yamlConfiguration);
        handler.setEncoding(StandardCharsets.UTF_8.name());
        handler.load(stream);

        System.out.println(yamlConfiguration.getString("redis.password"));

        yamlConfiguration.getKeys().forEachRemaining(key -> {
            Object value = yamlConfiguration.getProperty(key);
            System.out.println(key + " = " + value + ", " + value.getClass());
            System.out.println("");
        });

//        yamlConfiguration.read(stream);

//        yamlConfiguration.ad
    }
}
