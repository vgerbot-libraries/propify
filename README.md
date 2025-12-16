# Propify

![Build Status](https://github.com/vgerbot-libraries/propify/actions/workflows/build.yml/badge.svg) [![Codacy Badge](https://app.codacy.com/project/badge/Grade/9d3df77c87d243a9bb68b8687a87bfeb)](https://app.codacy.com/gh/vgerbot-libraries/propify/dashboard?utm_source=gh&utm_medium=referral&utm_content=&utm_campaign=Badge_grade) [![Codacy Badge](https://app.codacy.com/project/badge/Coverage/9d3df77c87d243a9bb68b8687a87bfeb)](https://app.codacy.com/gh/vgerbot-libraries/propify/dashboard?utm_source=gh&utm_medium=referral&utm_content=&utm_campaign=Badge_coverage)

----

**Propify** is a Java annotation processor that generates **typed** APIs from configuration files (YAML, INI, `.properties`), i18n bundles, and schema definitions (JSON Schema, OpenAPI).

If a config key, message key, or schema shape changes, the generated API changes with it and mismatches show up during compilation rather than as runtime lookups.

It supports nested properties, custom lookup providers (env/system/custom), ICU4J message formatting, and schema-based POJO generation. Generated code is regular Java; you call it from your application code.

----

## Table of Contents

1. [Why Propify?](#why-propify)
2. [Features](#features)
3. [Requirements](#requirements)
4. [Installation](#installation)
5. [Quick Start](#quick-start)
6. [Internationalization (i18n)](#internationalization-i18n)
7. [Schema-Based Generation](#schema-based-generation)
8. [How It Works](#how-it-works)
9. [Examples](#examples)
10. Documentation
    - 📖 [**@Propify Guide**](docs/PROPIFY.md) - Complete guide to type-safe configuration
    - 📖 [**@I18n Guide**](docs/I18N.md) - Comprehensive internationalization guide
    - 📖 [**@SchemaGen Guide**](docs/SCHEMAGEN.md) - Schema-based POJO generation guide
11. [Getting Help](#getting-help)
12. [Contributing](#contributing)
13. [License](#license)
14. [Acknowledgments](#acknowledgments)

----

## Why Propify?

- **Typed access**: use generated getters/methods instead of `config.get("a.b.c")`-style lookups.
- **Earlier failures**: missing keys and invalid formats fail the build, not production.
- **Less glue code**: no hand-written parsing/mapping for common cases.
- **Extensible resolution**: plug in custom lookup providers for env vars, system properties, or your own sources.

----

## Features

- **Type-safe config**: generate APIs from YAML, INI, or `.properties`.
- **Type-safe i18n**: generate resource bundle accessors; ICU4J MessageFormat support.
- **Schema-based POJOs**: generate mutable DTOs from JSON Schema or OpenAPI specs.
- **Compile-time validation**: syntax and schema checks happen during the build.
- **Nested keys**: hierarchical configs map to nested types.
- **Custom lookups**: resolve values from env/system/custom providers.
- **Compile-time generation**: no runtime parsing step required for the generated accessors.

----

## Requirements

- **Java**: 8 or higher
- **Build**: Maven or Gradle

----

## Installation

### Maven

Add dependency and annotation processor:

```xml
<dependencies>
  <dependency>
    <groupId>com.vgerbot</groupId>
    <artifactId>propify</artifactId>
    <version>3.0.0</version>
  </dependency>
</dependencies>

<build>
  <plugins>
    <plugin>
      <artifactId>maven-compiler-plugin</artifactId>
      <version>3.8.1</version>
      <configuration>
        <source>1.8</source>
        <target>1.8</target>
        <annotationProcessorPaths>
          <path>
            <groupId>com.vgerbot</groupId>
            <artifactId>propify</artifactId>
            <version>3.0.0</version>
          </path>
        </annotationProcessorPaths>
      </configuration>
    </plugin>
  </plugins>
</build>
```

### Gradle (≥4.6)

```groovy
dependencies {
  implementation 'com.vgerbot:propify:3.0.0'
  annotationProcessor 'com.vgerbot:propify:3.0.0'
}
```

### Gradle (<4.6)

```groovy
plugins {
  id 'net.ltgt.apt' version '0.21'
}

dependencies {
  compile 'com.vgerbot:propify:3.0.0'
  apt     'com.vgerbot:propify:3.0.0'
}
```

> For other tools, configure your build to include `propify` as an annotation processor.

----

## Quick Start

1. **Create** `src/main/resources/application.yml`:

   ```yaml
   server:
     host: localhost
     port: 8080
   database:
     url: jdbc:mysql://localhost:3306/mydb
     username: root
     password: secret
   ```

2. **Annotate** an interface:

   ```java
   @Propify(location = "application.yml")
   public interface AppConfig {}
   ```

3. **Use** the generated API:

   ```java
   public class Main {
     public static void main(String[] args) {
       AppConfigPropify cfg = AppConfigPropify.getInstance();
       System.out.println(cfg.getServer().getHost());
       System.out.println(cfg.getDatabase().getUrl());
     }
   }
   ```

> 📖 **[Full @Propify Documentation](docs/PROPIFY.md)** - Learn about custom lookups, media types, advanced configuration options, and more.

----

## Internationalization (i18n)

Generate type-safe resource bundles using ICU4J:

1. **Create** message files in `resources/`:

   ```properties
   # messages.properties (default)
   welcome=Welcome
   greeting=Hello, {0}!

   # messages_zh_CN.properties
   welcome=欢迎
   greeting=你好, {0}！
   ```

2. **Annotate** a class:

   ```java
   @I18n(baseName = "messages", defaultLocale = "en")
   public class Messages {}
   ```

3. **Access** messages:

   ```java
   String hi = MessageResource.getDefault().greeting("Alice");
   String hiZh = MessageResource.get(Locale.CHINESE).greeting("张三");
   ```

> 📖 **[Full @I18n Documentation](docs/I18N.md)** - Explore ICU MessageFormat, pluralization, date/time formatting, and multi-locale support.

----

## Schema-Based Generation

Generate mutable POJO/DTO classes from schema definitions for REST APIs and data modeling:

### Quick Example

```java
// 1. Create a JSON Schema or OpenAPI spec
@SchemaGen(location = "schemas/user.schema.json")
public interface UserSchema {}

// 2. Use the generated POJO
User user = User.builder()
    .username("johndoe")
    .email("john@example.com")
    .build();

// 3. Serialize/deserialize with Jackson
ObjectMapper mapper = new ObjectMapper();
String json = mapper.writeValueAsString(user);
User user = mapper.readValue(json, User.class);
```

Key points:

- Mutable POJOs with getters/setters
- Builder pattern for fluent construction
- Optional Jackson annotations for JSON serialization
- Optional Bean Validation annotations (`@NotNull`, `@Email`, `@Size`, etc.)
- JSON Schema and OpenAPI 3.x inputs

> 📖 **[Full @SchemaGen Documentation](docs/SCHEMAGEN.md)** - Learn about OpenAPI support, type mapping, validation annotations, and more.

----

## How It Works

1. **Scan** for `@Propify`, `@I18n`, and `@SchemaGen` annotations
2. **Parse** configuration files, message bundles, and schema definitions
3. **Generate** Java implementation classes and POJOs
4. **Compile** everything together—fail-fast on errors

----

## Examples

Check the [`example/`](example/) directory for complete working examples:

- **[Basic Configuration](example/src/main/java/com/vgerbot/example/AppConfig.java)** - Simple YAML/Properties configuration
- **[Advanced Configuration](example/src/main/java/com/vgerbot/example/AdvancedConfigExample.java)** - Custom lookups and nested structures
- **[INI Configuration](example/src/main/java/com/vgerbot/example/IniExample.java)** - INI format support
- **[Internationalization](example/src/main/java/com/vgerbot/example/I18nAdvancedExample.java)** - Multi-locale messages with ICU formatting
- **[Schema Generation](example/src/main/java/com/vgerbot/example/SchemaGenExample.java)** - JSON Schema and OpenAPI POJO generation

----

## Getting Help

If you encounter any issues or have questions about using Propify:

- **GitHub Issues**: Submit a [new issue](https://github.com/vgerbot-libraries/propify/issues) on our GitHub repository
- **Documentation**: Check the [Wiki](https://github.com/vgerbot-libraries/propify/wiki) for detailed documentation
- **Examples**: Browse the [examples directory](https://github.com/vgerbot-libraries/propify/tree/main/examples) for sample projects

----

## Contributing

1. Fork the repo
2. Create a feature branch (`git checkout -b feature/xyz`)
3. Implement and test your changes
4. Submit a Pull Request

Please follow the existing coding style and update tests.

----

## License

[MIT](LICENSE) © 2024 vgerbot-libraries

----

## Acknowledgments

- [JavaPoet](https://github.com/square/javapoet) - Java source file generation
- [Jackson YAML](https://github.com/FasterXML/jackson-dataformats-text) - YAML parsing
- [Apache Commons Configuration](https://commons.apache.org/proper/commons-configuration/) - Configuration management
- [ICU4J](https://unicode-org.github.io/icu/userguide/icu4j/) - Internationalization support
