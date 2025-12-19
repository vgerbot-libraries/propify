## 🚀 New Features

### Schema-Based POJO Generation (`@SchemaGen`)

The headline feature of 3.0.0 is the addition of **schema-based code generation** through the new `@SchemaGen` annotation. This allows you to automatically generate Java model classes (POJOs/DTOs) from schema definitions.

**Key Capabilities:**

- **Multiple Schema Formats**: Support for JSON Schema (draft-07+) and OpenAPI 3.x specifications
- **Mutable POJOs**: Generates classes with getters and setters for runtime data binding
- **Builder Pattern**: Fluent object construction out of the box
- **Jackson Integration**: Optional Jackson annotations for JSON serialization/deserialization
- **Bean Validation**: Optional validation annotations (`@NotNull`, `@Email`, `@Size`, `@Min`, `@Max`, etc.)
- **Enum Support**: Full support for enum types with proper code generation
- **Nested Objects**: Complex nested types are automatically generated as nested classes
- **Compile-Time Safety**: Invalid schemas fail the build early

**Quick Example:**

```java
// 1. Define schema
@SchemaGen(
    location = "classpath:schemas/user.schema.json",
    type = SchemaType.JSON_SCHEMA
)
public interface UserSchema {}

// 2. Use generated POJO
User user = User.builder()
    .username("johndoe")
    .email("john@example.com")
    .age(30)
    .build();

// 3. JSON serialization with Jackson
ObjectMapper mapper = new ObjectMapper();
String json = mapper.writeValueAsString(user);
```

**Distinction from `@Propify`:**

| Feature | @Propify | @SchemaGen |
|---------|----------|------------|
| **Input** | Config files with data | Schema files (structure only) |
| **Output** | Read-only classes + data | Mutable POJOs |
| **Fields** | `private final` | `private` (mutable) |
| **Methods** | Getters only | Getters + Setters |
| **Constructor** | Private (singleton) | Public + Builder |
| **Use Case** | Application configuration | API data models |
| **Data Source** | Compile-time (fixed) | Runtime (dynamic) |

**Configuration Options:**

```java
@SchemaGen(
    location = "schemas/api.yaml",           // Schema file location
    type = SchemaType.AUTO,                  // AUTO, JSON_SCHEMA, OPENAPI
    schemaRef = "Pet",                       // For OpenAPI: which schema
    builder = true,                          // Generate builder (default: true)
    jacksonAnnotations = true,               // Add Jackson annotations (default: true)
    validationAnnotations = true,            // Add validation annotations (default: true)
    serializable = true,                     // Implement Serializable (default: true)
    generateHelperMethods = true             // equals/hashCode/toString (default: true)
)
```

**Use Cases:**

- REST API DTOs from OpenAPI specifications
- Microservices communication with shared schemas
- Database entity generation
- Contract-first API development
- Schema registry integration

📖 **[Complete SchemaGen Documentation](docs/SCHEMAGEN.md)**

---

## 🐛 Bug Fixes

### Java 17 Compatibility

- **Fixed**: Reflection access to javac internal `List` in Java 17
  - Resolved compatibility issues with newer Java versions
  - Ensures smooth operation on Java 8-17+

---

## 📦 Examples & Demos

### New Schema Examples

Added comprehensive examples demonstrating schema-based generation:

- **`EnumCodeGeneratorDemo.java`** - Enum type generation from schemas
- **`EnumUsageExample.java`** - Using generated enum types
- **`EnumExample.java`** - Complete enum support demonstration
- **`ExampleFilesDemo.java`** - Working with schema files
- **`SchemaGenExample.java`** - End-to-end schema generation example

Example schemas included:
- `schemas/user.schema.json` - JSON Schema example
- `schemas/petstore.yaml` - OpenAPI 3.0 example

---

## 🔄 Migration Guide

### Upgrading from 2.x to 3.0.0

**Maven:**

```xml
<dependency>
    <groupId>com.vgerbot</groupId>
    <artifactId>propify</artifactId>
    <version>3.0.0</version>
</dependency>
```

**Gradle:**

```groovy
dependencies {
    implementation 'com.vgerbot:propify:3.0.0'
    annotationProcessor 'com.vgerbot:propify:3.0.0'
}
```

**Breaking Changes:**

None. Version 3.0.0 is fully backward compatible with 2.x. All existing `@Propify` and `@I18n` code will continue to work without modifications.

The new `@SchemaGen` feature is purely additive.


**Full Changelog**: https://github.com/vgerbot-libraries/propify/compare/v2.0.0...v3.0.0
