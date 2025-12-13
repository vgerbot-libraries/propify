package com.vgerbot.example;

import com.vgerbot.propify.schema.SchemaGen;
import com.vgerbot.propify.schema.SchemaType;

/**
 * Example using @SchemaGen with OpenAPI specification.
 * This will generate a Pet POJO class from the OpenAPI Pet schema.
 */
@SchemaGen(
    location = "classpath:schemas/petstore.yaml",
    type = SchemaType.OPENAPI,
    schemaRef = "Pet"
)
public interface PetSchema {
}

