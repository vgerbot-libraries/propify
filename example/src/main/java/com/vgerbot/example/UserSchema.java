package com.vgerbot.example;

import com.vgerbot.propify.schema.SchemaGen;
import com.vgerbot.propify.schema.SchemaType;

/**
 * Example using @SchemaGen with JSON Schema.
 * This will generate a User POJO class with getters, setters, and builder pattern.
 */
@SchemaGen(
    location = "classpath:schemas/user.schema.json",
    type = SchemaType.JSON_SCHEMA
)
public interface UserSchema {
}

