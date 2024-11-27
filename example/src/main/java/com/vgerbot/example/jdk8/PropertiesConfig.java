package com.vgerbot.example.jdk8;

import com.vgerbot.propify.Propify;

@Propify(
        location = "classpath: application.properties",
        generatedClassName = "ApplicationPropertiesPropify"
)
public interface PropertiesConfig {

}
