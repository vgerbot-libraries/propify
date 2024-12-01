# Propify ![Build Status](https://github.com/vgerbot-libraries/propify/actions/workflows/build.yml/badge.svg) [![Codacy Badge](https://app.codacy.com/project/badge/Grade/9d3df77c87d243a9bb68b8687a87bfeb)](https://app.codacy.com/gh/vgerbot-libraries/propify/dashboard?utm_source=gh&utm_medium=referral&utm_content=&utm_campaign=Badge_grade) [![Codacy Badge](https://app.codacy.com/project/badge/Coverage/9d3df77c87d243a9bb68b8687a87bfeb)](https://app.codacy.com/gh/vgerbot-libraries/propify/dashboard?utm_source=gh&utm_medium=referral&utm_content=&utm_campaign=Badge_coverage)

Propify is a lightweight Java annotation processor that generates type-safe Java code from configuration files (YAML/Properties). It helps eliminate the boilerplate of manually parsing configuration files and provides compile-time safety for accessing configuration properties.

## Features

- 🔒 **Type-safe Configuration**: Access your configuration properties with compile-time type checking
- 📝 **Multiple Format Support**: Works with both YAML and Properties files
- 🛠 **Compile-time Code Generation**: No runtime overhead, all code is generated during compilation
- ⚡ **Zero Runtime Dependencies**: Generated code has no dependencies on Propify itself
- 🎯 **Simple Integration**: Just add the annotation processor to your build and start using it

## Requirements

- Java 8 or higher
- Maven or Gradle build system

## Installation

### Maven

```xml
<dependency>
    <groupId>com.vgerbot</groupId>
    <artifactId>propify-processor</artifactId>
    <version>1.0-SNAPSHOT</version>
    <scope>provided</scope>
</dependency>
```

### Gradle

```groovy
annotationProcessor 'com.vgerbot:propify-processor:1.0-SNAPSHOT'
```

## Quick Start

1. Create your configuration file (e.g., `application.yml`):

```yaml
server:
  host: localhost
  port: 8080
database:
  url: jdbc:mysql://localhost:3306/mydb
  username: root
  password: secret
```

2. Create a configuration interface with the `@Propify` annotation:

```java
@Propify(location = "application.yml")
public interface AppConfig {
}
```

3. Use the generated configuration class:

```java
public class Application {
    public static void main(String[] args) {
        AppConfigPropify config = new AppConfigPropify();
        
        // Type-safe access to configuration
        String dbUrl = config.getDatabase().getUrl();
        int serverPort = config.getServer().getPort();
    }
}
```

## Advanced Usage

### Custom Generated Class Name

```java
@Propify(
    location = "application.yml",
    generatedClassName = "MyCustomConfigImpl"
)
public interface AppConfig {
    // ...
}
```

### Specifying Media Type

```java
@Propify(
    location = "application.properties",
    mediaType = "application/x-java-properties"
)
public interface AppConfig {
    // ...
}
```

## How It Works

1. During compilation, Propify processes classes annotated with `@Propify`
2. It reads and parses the specified configuration file
3. Generates a Java implementation class with type-safe getters
4. The generated code is compiled along with your source code

## Contributing

Contributions are welcome! Here's how you can help:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

Please make sure to update tests as appropriate and adhere to the existing coding style.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Support

- Create an issue on GitHub for bug reports or feature requests
- For usage questions, please refer to the examples above or create a discussion on GitHub

## Acknowledgments

- [JavaPoet](https://github.com/square/javapoet) for Java code generation
- [Jackson](https://github.com/FasterXML/jackson) for YAML parsing
