# SchemaGen Feature

## Overview

The **SchemaGen** feature generates Java model classes (POJOs/DTOs) from schema definitions such as JSON Schema and OpenAPI. Use it when you want types that are populated at runtime (for example via Jackson).

This is intentionally different from `@Propify`: `@Propify` generates read-only configuration accessors with compile-time embedded data, while `@SchemaGen` generates mutable data classes designed for binding and serialization.

## Key Differences

| Feature | @Propify | @SchemaGen |
|---------|----------|------------|
| **Input** | Config files with data | Schema files (structure only) |
| **Output** | Read-only classes + data | Mutable POJOs |
| **Fields** | `private final` | `private` (mutable) |
| **Methods** | Getters only | Getters + Setters |
| **Constructor** | Private (singleton-style) | Public + Builder |
| **Use Case** | Application configuration | API data models |
| **Data Source** | Compile-time (fixed) | Runtime (dynamic via JSON/XML) |

## Features

- **Mutable POJOs**: getters and setters.
- **Builder support**: fluent construction for generated models.
- **Jackson annotations (optional)**: JSON serialization/deserialization support in generated code.
- **Bean Validation annotations (optional)**: constraints such as `@NotNull`, `@Email`, `@Size`.
- **Multiple schema inputs**: JSON Schema and OpenAPI 3.x.
- **Nested object support**: complex/nested types are generated as nested models.
- **Compile-time generation**: invalid inputs fail the build.
- **Standalone output**: generated code does not require SchemaGen at runtime.

## Quick Start

### 1. JSON Schema Example

**Create a JSON Schema** (`src/main/resources/schemas/user.schema.json`):

```json
{
  "$schema": "http://json-schema.org/draft-07/schema#",
  "title": "User",
  "type": "object",
  "properties": {
    "id": { "type": "integer" },
    "username": {
      "type": "string",
      "minLength": 3,
      "maxLength": 50
    },
    "email": {
      "type": "string",
      "format": "email"
    },
    "age": {
      "type": "integer",
      "minimum": 0,
      "maximum": 150
    }
  },
  "required": ["username", "email"]
}
```

**Annotate an interface**:

```java
@SchemaGen(
    location = "classpath:schemas/user.schema.json",
    type = SchemaType.JSON_SCHEMA
)
public interface UserSchema {}
```

**Use the generated class**:

```java
// Create using builder
User user = User.builder()
    .id(1)
    .username("johndoe")
    .email("john@example.com")
    .age(30)
    .build();

// Or using setters
User user = new User();
user.setUsername("johndoe");
user.setEmail("john@example.com");

// Jackson serialization
ObjectMapper mapper = new ObjectMapper();
String json = mapper.writeValueAsString(user);

// Deserialization
User user = mapper.readValue(json, User.class);
```

### 2. OpenAPI Example

**Create OpenAPI spec** (`src/main/resources/schemas/api.yaml`):

```yaml
openapi: 3.0.0
info:
  title: Pet Store API
  version: 1.0.0

components:
  schemas:
    Pet:
      type: object
      required:
        - id
        - name
      properties:
        id:
          type: integer
          format: int64
        name:
          type: string
          minLength: 1
          maxLength: 100
        status:
          type: string
          enum: [available, pending, sold]
        price:
          type: number
          format: double
          minimum: 0
```

**Annotate an interface**:

```java
@SchemaGen(
    location = "classpath:schemas/api.yaml",
    type = SchemaType.OPENAPI,
    schemaRef = "Pet"  // Specify which schema to generate
)
public interface PetSchema {}
```

**Use with REST APIs**:

```java
Pet pet = Pet.builder()
    .id(123L)
    .name("Fluffy")
    .status("available")
    .price(299.99)
    .build();

// Spring RestTemplate
RestTemplate restTemplate = new RestTemplate();
ResponseEntity<Pet> response = restTemplate.postForEntity(
    "https://api.example.com/pets",
    pet,
    Pet.class
);
```

## Configuration Options

The `@SchemaGen` annotation provides several configuration options:

```java
@SchemaGen(
    location = "schemas/user.schema.json",     // Schema file location
    type = SchemaType.AUTO,                    // AUTO, JSON_SCHEMA, OPENAPI, XML_SCHEMA
    schemaRef = "",                            // For OpenAPI: which schema to generate
    generatedClassName = "$$",                 // Class name pattern ($$ = interface name)
    builder = true,                            // Generate builder pattern
    jacksonAnnotations = true,                 // Add Jackson annotations
    jaxbAnnotations = false,                   // Add JAXB annotations
    validationAnnotations = true,              // Add Bean Validation annotations
    serializable = true,                       // Implement Serializable
    generateHelperMethods = true               // Generate equals/hashCode/toString
)
```

### Location Options

- **Classpath**: `"classpath:schemas/user.schema.json"`
- **File System**: `"file:///path/to/schema.json"`
- **HTTP/HTTPS**: `"https://example.com/api/schema.json"`

### Schema Types

- `SchemaType.AUTO` - Auto-detect from file extension/content
- `SchemaType.JSON_SCHEMA` - JSON Schema (draft-07+)
- `SchemaType.OPENAPI` - OpenAPI 3.x specification
- `SchemaType.XML_SCHEMA` - XML Schema (XSD) - *future support*

## Generated Code Features

### 1. Bean Validation Annotations

Based on schema constraints:

```java
@NotNull              // For required fields
@Email                // For email format
@Size(min=3, max=50)  // For string length
@Min(0) @Max(150)     // For numeric ranges
@Pattern(regexp="...")// For regex patterns
```

### 2. Jackson Annotations

For JSON serialization:

```java
@JsonProperty("fieldName")
@JsonFormat(shape = Shape.STRING, pattern = "yyyy-MM-dd")
```

### 3. Builder Pattern

Fluent object construction:

```java
User user = User.builder()
    .username("john")
    .email("john@example.com")
    .age(30)
    .build();
```

### 4. Helper Methods

Automatic generation of:

```java
@Override
public boolean equals(Object o) { ... }

@Override
public int hashCode() { ... }

@Override
public String toString() { ... }
```

## Type Mapping

Schema types are mapped to Java types:

| Schema Type | Format | Java Type |
|------------|--------|-----------|
| string | - | String |
| string | date-time | LocalDateTime |
| string | date | LocalDate |
| string | email | String (@Email) |
| integer | - | Integer |
| integer | int64 | Long |
| number | - | Double |
| number | float | Float |
| boolean | - | Boolean |
| array | - | List\<T> |
| object | - | Nested class |

## Example Project Structure

```
my-project/
├── src/main/
│   ├── java/com/example/
│   │   ├── UserSchema.java        # @SchemaGen annotation
│   │   └── MyService.java         # Uses generated User class
│   └── resources/schemas/
│       ├── user.schema.json       # JSON Schema
│       └── api.yaml               # OpenAPI spec
└── pom.xml
```

## Maven Configuration

Already included in Propify dependencies:

```xml
<dependency>
    <groupId>com.vgerbot</groupId>
    <artifactId>propify</artifactId>
    <version>3.0.0</version>
</dependency>
```

Runtime dependencies (if using features):

```xml
<!-- For Jackson JSON support -->
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
    <version>2.17.1</version>
</dependency>

<!-- For Bean Validation -->
<dependency>
    <groupId>javax.validation</groupId>
    <artifactId>validation-api</artifactId>
    <version>2.0.1.Final</version>
</dependency>
```

## Common Use Cases

### 1. REST API DTOs

Generate DTOs from OpenAPI specifications:

```java
@SchemaGen(
    location = "classpath:openapi/api.yaml",
    type = SchemaType.OPENAPI,
    schemaRef = "UserDTO"
)
public interface UserDtoSchema {}
```

### 2. Microservices Communication

Share schema definitions across services:

```java
@SchemaGen(
    location = "https://schema-registry.example.com/user.schema.json",
    type = SchemaType.JSON_SCHEMA
)
public interface UserSchema {}
```

### 3. Database Entities

Generate entity classes from JSON Schema:

```java
@SchemaGen(
    location = "classpath:schemas/product.schema.json",
    validationAnnotations = true,
    serializable = true
)
public interface ProductSchema {}
```

## Tips and Best Practices

1. **Keep Schemas in Version Control** - Track schema changes alongside code
2. **Use Schema Validation** - Validate JSON/XML against schemas at runtime
3. **Separate Schema Interfaces** - One schema file per interface for clarity
4. **Use Builder Pattern** - Enabled by default, provides cleaner construction
5. **Enable Validation** - Bean Validation catches constraint violations early
6. **Document Schemas** - Use description fields, they become Javadoc

## Troubleshooting

### Schema not found
- Check classpath resources are in `src/main/resources/`
- Verify the location path in `@SchemaGen`

### Jackson annotations not working
- Ensure `jacksonAnnotations = true` (default)
- Add Jackson dependencies to runtime classpath

### Validation not working
- Ensure `validationAnnotations = true` (default)
- Add validation-api to runtime classpath
- Use a validation implementation (e.g., Hibernate Validator)

## Comparison with Alternatives

### vs. jsonschema2pojo
- Integrated with the rest of Propify.
- Compile-time generation (no separate generator step in your build).
- Builder pattern support out of the box.
- Annotation-driven workflow consistent with `@Propify`.

### vs. OpenAPI Generator
- Annotation-based approach (no external generator CLI).
- Lightweight generated code intended to be edited/read in an IDE.
- Fits naturally into a standard Java compile/test cycle.

## Limitations

- XML Schema (XSD) support not yet implemented
- No support for allOf/oneOf/anyOf (planned)
- Enum types generated as Strings (planned improvement)

## Roadmap

- [ ] Full XML Schema (XSD) support
- [ ] Support for allOf/oneOf/anyOf
- [ ] Enum type generation
- [ ] Custom type mappers
- [ ] Lombok-style generation option
- [ ] Record class generation (Java 14+)

## Contributing

Contributions are welcome! Please see the main [CONTRIBUTING.md](../CONTRIBUTING.md) for guidelines.

## License

MIT License - see [LICENSE](../LICENSE)

