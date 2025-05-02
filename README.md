# 🔧 Propify

![Build Status](https://github.com/vgerbot-libraries/propify/actions/workflows/build.yml/badge.svg) [![Codacy Badge](https://app.codacy.com/project/badge/Grade/9d3df77c87d243a9bb68b8687a87bfeb)](https://app.codacy.com/gh/vgerbot-libraries/propify/dashboard?utm_source=gh&utm_medium=referral&utm_content=&utm_campaign=Badge_grade) [![Codacy Badge](https://app.codacy.com/project/badge/Coverage/9d3df77c87d243a9bb68b8687a87bfeb)](https://app.codacy.com/gh/vgerbot-libraries/propify/dashboard?utm_source=gh&utm_medium=referral&utm_content=&utm_campaign=Badge_coverage)

----

**Propify** is a powerful, lightweight Java annotation processor that eliminates configuration errors by generating **type-safe** classes from your configuration files (YAML, INI, or `.properties`) and internationalization bundles.

> **Say goodbye to "stringly-typed" keys and runtime errors!** Access every configuration value and message through strongly-typed Java methods, catching invalid accesses at compile time instead of runtime.

Propify seamlessly supports nested properties, custom lookup providers, and full ICU4J formatting while adding zero runtime dependencies to your application.

----

## 📖 Table of Contents

1. [Why Propify?](#-why-propify)
2. [Features](#-features)
3. [Requirements](#-requirements)
4. [Installation](#-installation)
5. [Quick Start](#-quick-start)
6. [Advanced Usage](#-advanced-usage)
7. [Internationalization (i18n)](#-internationalization-i18n)
8. [How It Works](#️-how-it-works)
9. [Examples](#-examples)
10. [Getting Help](#-getting-help)
11. [Contributing](#-contributing)
12. [License](#-license)
13. [Acknowledgments](#-acknowledgments)

----

## 🤔 Why Propify?

- **Type-Safety**: Access configuration and messages via Java methods—no more stringly-typed keys.
- **Compile-Time Guarantees**: Prevent typos in code (incorrect keys) from compiling, so invalid property accesses are caught before runtime.
- **Productivity**: Skip manual parsing and error-prone lookups.
- **Extendable**: Plug in custom lookup providers for environment variables, system properties, or your own sources.

----

## ✨ Features

- 🔒 **Type-Safe Config**: Generates POJOs from YAML, INI, or `.properties` files
- 🌐 **Type-Safe i18n**: Strongly-typed resource bundles with ICU4J formatting
- 🛠 **Compile-Time Validation**: Syntax and schema checks during build
- 📚 **Nested Keys**: Dot-notation support for hierarchical configs
- 🔄 **Custom Lookups**: Inject dynamic values (env, system props, custom)
- ⚡️ **Zero Runtime Overhead**: All code generated at compile time

----

## 📋 Requirements

- **Java**: 8 or higher
- **Build**: Maven or Gradle

----

## 📥 Installation

### Maven

Add dependency and annotation processor:

```xml
<dependencies>
  <dependency>
    <groupId>com.vgerbot</groupId>
    <artifactId>propify</artifactId>
    <version>1.1.0</version>
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
            <version>1.1.0</version>
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
  implementation 'com.vgerbot:propify:1.1.0'
  annotationProcessor 'com.vgerbot:propify:1.1.0'
}
```

### Gradle (<4.6)

```groovy
plugins {
  id 'net.ltgt.apt' version '0.21'
}

dependencies {
  compile 'com.vgerbot:propify:1.1.0'
  apt     'com.vgerbot:propify:1.1.0'
}
```

> For other tools, configure your build to include `propify` as an annotation processor.

----

## 🚀 Quick Start

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
       AppConfigPropify cfg = new AppConfigPropify();
       System.out.println(cfg.getServer().getHost());
       System.out.println(cfg.getDatabase().getUrl());
     }
   }
   ```

Configuration locations can be:

- On the classpath (e.g., `application.yml` in `src/main/resources/`)
- Local file system (`file:///path/to/config.yml`)
- HTTP/HTTPS URL (`https://...`)

For example:

```java
@Propify(location = "https://example.com/config.yml")
public interface WebConfig {}
```

> ⚠️ Ensure your configuration files are reachable at build time—whether via classpath, file path, or network URL.

----

## 🔧 Advanced Usage

### Custom Class Name

```java
@Propify(
  location = "application.yml",
  generatedClassName = "CustomConfigImpl"
)
public interface AppConfig {}
```

### Media Types

By default, Propify infers file format from the file extension (`.yml`/`.yaml` for YAML, `.ini` for INI, `.properties` for Java properties). Manual `mediaType` specification is only required when the extension is non-standard or ambiguous.

```java
@Propify(
  location = "config.custom",              // non-standard extension
  mediaType = "application/x-java-properties"
)
public interface AppConfig {}
```

### Custom Lookups

Propify lets you interpolate dynamic values at build time via lookup providers. Out of the box you can use placeholders in your config:

- **Environment variables**: `{env:VAR_NAME}`
- **Custom lookups**: `{lookupName:variableName}` — resolved by the corresponding lookup class

**Example configuration** (`application.yml`):

```yaml
app:
  tempDir: "{env:TEMP_DIR}"
  secretKey: "{vault:db-secret}"
```

**Annotate your interface**:

```java
@Propify(
  location = "application.yml",
  lookups = {
    CustomEnvironmentLookup.class,  // resolves {env:...}
    VaultLookup.class              // resolves {vault:...}
  }
)
public interface AppConfig {}
```

**Usage in code**:

```java
AppConfig cfg = new AppConfigPropify();
String tempDir = cfg.getApp().getTempDir();    // from $TEMP_DIR
String secret = cfg.getApp().getSecretKey();   // from vault lookup
```

----

## 🌐 Internationalization (i18n)

Generate type-safe resource bundles using ICU4J:

1. **Create** message files in `resources/`:

   ```properties
   # messages.properties (default)
   welcome=Welcome
   greeting=Hello, {name}!

   # messages_zh_CN.properties
   welcome=欢迎
   greeting=你好, {name}！
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

Supports pluralization, dates, numbers, and custom ICU patterns—fully validated at compile time.

----

## ⚙️ How It Works

1. **Scan** for `@Propify` and `@I18n` annotations
2. **Parse** configuration and message files
3. **Generate** Java implementation classes
4. **Compile** everything together—fail-fast on errors

----

## 📝 Examples

Here are some practical examples of how to use Propify in common scenarios:

### Nested Configuration

```java
// application.yml
// server:
//   http:
//     port: 8080
//   https:
//     port: 8443
//     keystore: /path/to/keystore

@Propify(location = "application.yml")
public interface ServerConfig {}

// Usage
ServerConfigPropify config = new ServerConfigPropify();
int httpPort = config.getServer().getHttp().getPort();  // 8080
String keystore = config.getServer().getHttps().getKeystore();  // /path/to/keystore
```

### Environment-Specific Configuration

```java
// Using environment variables in your config
// app.yml
// database:
//   url: "jdbc:mysql://{env:DB_HOST}:{env:DB_PORT}/{env:DB_NAME}"

@Propify(
  location = "app.yml",
  lookups = { EnvironmentLookup.class }
)
public interface AppConfig {}
```

----

## 🙋 Getting Help

If you encounter any issues or have questions about using Propify:

- **GitHub Issues**: Submit a [new issue](https://github.com/vgerbot-libraries/propify/issues) on our GitHub repository
- **Documentation**: Check the [Wiki](https://github.com/vgerbot-libraries/propify/wiki) for detailed documentation
- **Examples**: Browse the [examples directory](https://github.com/vgerbot-libraries/propify/tree/main/examples) for sample projects

----

## 👥 Contributing

1. Fork the repo
2. Create a feature branch (`git checkout -b feature/xyz`)
3. Implement and test your changes
4. Submit a Pull Request

Please follow the existing coding style and update tests.

----

## 📄 License

[MIT](LICENSE) © 2024 vgerbot-libraries

----

## 🙏 Acknowledgments

- [JavaPoet](https://github.com/square/javapoet) - Java source file generation
- [Jackson YAML](https://github.com/FasterXML/jackson-dataformats-text) - YAML parsing
- [Apache Commons Configuration](https://commons.apache.org/proper/commons-configuration/) - Configuration management
- [ICU4J](https://unicode-org.github.io/icu/userguide/icu4j/) - Internationalization support
