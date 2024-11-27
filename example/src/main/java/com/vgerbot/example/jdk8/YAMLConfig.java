package com.vgerbot.example.jdk8;

import com.vgerbot.propify.Propify;

@Propify(
        location = "classpath: config.yml",
        generatedClassName = "ApplicationYAMLPropify"
)
public interface YAMLConfig {
}
