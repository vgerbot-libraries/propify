package com.vgerbot.example.jdk8;

import com.vgerbot.propify.*;

import java.io.IOException;
import java.io.InputStream;

@Propify(
        location = "classpath: application.properties",
        generatedClassName = "ApplicationPropertiesPropify"
)
public class PropertiesConfig {
}

