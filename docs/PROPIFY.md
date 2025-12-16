# @Propify - Type-Safe Configuration

## Overview

The **@Propify** annotation generates a typed API from configuration files (YAML, INI, or `.properties`). Instead of fetching values by string keys at runtime, you work with generated getters. If the config file is invalid or a key disappears, you find out during compilation.

## Key Differences from Other Approaches

| Feature | Traditional Approach | @Propify |
|---------|---------------------|----------|
| **Access Method** | `config.get("db.url")` | `config.getDb().getUrl()` |
| **Type Safety** | Runtime strings | Compile-time types |
| **Error Detection** | Runtime failures | Compile-time errors |
| **IDE Support** | No autocomplete | Full autocomplete |
| **Refactoring** | Manual search/replace | IDE-assisted |
| **Data Loading** | Runtime parsing | Compile-time embedding |

## Features

- **Generated getters instead of string keys**: `config.getDatabase().getUrl()` rather than `config.get("db.url")`.
- **Compile-time validation**: parse errors and incompatible shapes fail the build.
- **Multiple input formats**: YAML, INI, and Java `.properties`.
- **Nested structure**: dotted keys / nested sections map to nested types.
- **Custom lookups**: resolve values from env vars, system props, or custom sources.
- **Flexible locations**: classpath, filesystem, or HTTP/HTTPS.
- **Read-only by design**: generated config values are immutable.
- **No runtime parsing requirement**: configuration data is embedded at compile time.

## Quick Start

### 1. YAML Configuration

**Create** `src/main/resources/application.yml`:

```yaml
server:
  host: localhost
  port: 8080
database:
  url: jdbc:mysql://localhost:3306/mydb
  username: root
  password: secret
  pool:
    minSize: 5
    maxSize: 20
```

**Annotate** an interface:

```java
@Propify(location = "application.yml")
public interface AppConfig {}
```

**Use** the generated class:

```java
public class Main {
    public static void main(String[] args) {
        AppConfigPropify config = AppConfigPropify.getInstance();
        
        // Type-safe nested access
        System.out.println(config.getServer().getHost());     // "localhost"
        System.out.println(config.getServer().getPort());     // 8080
        System.out.println(config.getDatabase().getUrl());    // "jdbc:mysql://..."
        System.out.println(config.getDatabase().getPool().getMaxSize());  // 20
    }
}
```

### 2. Properties Configuration

**Create** `src/main/resources/application.properties`:

```properties
app.name=MyApplication
app.version=1.0.0
app.debug=true

server.host=localhost
server.port=8080

database.url=jdbc:postgresql://localhost:5432/mydb
database.username=admin
database.password=secret
```

**Annotate** an interface:

```java
@Propify(location = "application.properties")
public interface AppConfig {}
```

**Use** the generated class:

```java
AppConfigPropify config = AppConfigPropify.getInstance();
System.out.println(config.getApp().getName());        // "MyApplication"
System.out.println(config.getApp().getVersion());     // "1.0.0"
System.out.println(config.getApp().getDebug());       // true (boolean)
```

### 3. INI Configuration

**Create** `src/main/resources/config.ini`:

```ini
[application]
name = MyApp
version = 2.0.0

[server]
host = 0.0.0.0
port = 9090

[database]
driver = org.postgresql.Driver
url = jdbc:postgresql://localhost:5432/mydb
```

**Annotate** an interface:

```java
@Propify(
    location = "config.ini",
    mediaType = "text/plain"
)
public interface Config {}
```

**Use** the generated class:

```java
ConfigPropify config = ConfigPropify.getInstance();
System.out.println(config.getApplication().getName());  // "MyApp"
System.out.println(config.getServer().getPort());       // 9090
```

## Configuration Options

The `@Propify` annotation provides several configuration options:

```java
@Propify(
    location = "application.yml",              // Configuration file location
    generatedClassName = "$$Propify",          // Generated class name pattern
    mediaType = "",                            // Media type (auto-detected)
    lookups = {}                               // Custom lookup providers
)
public interface AppConfig {}
```

### Location

Specifies where to find the configuration file. Supports three types of locations:

#### Classpath Resources (Default)

```java
@Propify(location = "application.yml")
@Propify(location = "classpath:config/app.yml")
```

Files should be in `src/main/resources/` directory.

#### File System

```java
@Propify(location = "file:///etc/myapp/config.yml")
@Propify(location = "file:///C:/config/app.yml")  // Windows
```

Use absolute paths for file system access.

#### HTTP/HTTPS URLs

```java
@Propify(location = "https://config.example.com/app.yml")
@Propify(location = "http://localhost:8080/config")
```

Remote configurations are fetched at compile time and embedded in the generated class.

> ⚠️ **Important**: Configuration files must be accessible at build time. Ensure network resources are available during compilation.

### Generated Class Name

Customize the name of the generated implementation class:

```java
@Propify(
    location = "application.yml",
    generatedClassName = "ApplicationConfig"
)
public interface AppConfig {}

// Usage:
ApplicationConfig config = ApplicationConfig.getInstance();
```

**Default Pattern**: `$$Propify` (e.g., `AppConfig` → `AppConfigPropify`)

**Custom Patterns**:
- `$$` is replaced with the interface name
- Use any valid Java identifier

```java
// Interface: ServerConfig
@Propify(generatedClassName = "$$Impl")     // → ServerConfigImpl
@Propify(generatedClassName = "Generated$$") // → GeneratedServerConfig
@Propify(generatedClassName = "MyConfig")    // → MyConfig
```

### Media Type

Specifies the format of the configuration file. Usually auto-detected from file extension:

| Extension | Auto-Detected Type | Description |
|-----------|-------------------|-------------|
| `.yml`, `.yaml` | `application/x-yaml` | YAML format |
| `.properties` | `application/x-java-properties` | Java Properties |
| `.ini` | `text/plain` | INI format |

**Manual specification** (for non-standard extensions):

```java
@Propify(
    location = "config.txt",
    mediaType = "application/x-yaml"
)
public interface Config {}

@Propify(
    location = "app.config",
    mediaType = "application/x-java-properties"
)
public interface AppConfig {}
```

**Supported Media Types**:
- `application/x-yaml` - YAML
- `application/x-java-properties` - Java Properties
- `text/plain` - INI format

### Custom Lookups

Propify supports dynamic value interpolation at build time through custom lookup providers. This allows you to inject environment variables, system properties, or any custom values into your configuration.

#### Built-in Lookups

**Environment Variables**:

```yaml
# application.yml
database:
  url: "{env:DB_URL}"
  password: "{env:DB_PASSWORD}"
```

```java
@Propify(
    location = "application.yml",
    lookups = { EnvironmentLookup.class }
)
public interface AppConfig {}
```

#### Custom Lookup Implementation

Create a custom lookup provider by implementing `PropifyLookup`:

```java
public class VaultLookup implements PropifyLookup {
    
    @Override
    public String getPrefix() {
        return "vault";  // Used in config as {vault:...}
    }
    
    @Override
    public Object lookup(String variable) {
        // Your custom logic to fetch values
        // Example: fetch from HashiCorp Vault
        return vaultClient.read("secret/" + variable);
    }
}
```

**Configuration file**:

```yaml
app:
  apiKey: "{vault:api-secret}"
  dbPassword: "{vault:db-password}"
  tempDir: "{env:TEMP_DIR}"
```

**Usage**:

```java
@Propify(
    location = "application.yml",
    lookups = {
        EnvironmentLookup.class,
        VaultLookup.class
    }
)
public interface AppConfig {}

// Generated code resolves all placeholders at compile time
AppConfigPropify config = AppConfigPropify.getInstance();
String apiKey = config.getApp().getApiKey();  // Value from Vault
```

#### Common Lookup Examples

**System Properties Lookup**:

```java
public class SystemPropertiesLookup implements PropifyLookup {
    
    @Override
    public String getPrefix() {
        return "sys";
    }
    
    @Override
    public Object lookup(String variable) {
        return System.getProperty(variable);
    }
}
```

**Database Lookup**:

```java
public class DatabaseLookup implements PropifyLookup {
    
    @Override
    public String getPrefix() {
        return "db";
    }
    
    @Override
    public Object lookup(String variable) {
        // Fetch configuration from database
        return configRepository.findByKey(variable);
    }
}
```

## Generated Code Features

### Immutable Configuration

All generated classes produce immutable configuration objects:

```java
public class AppConfigPropify {
    private final Server server;
    private final Database database;
    
    // Private constructor
    private AppConfigPropify() {
        this.server = new Server("localhost", 8080);
        this.database = new Database("jdbc:...", "root");
    }
    
    // Singleton pattern
    private static final AppConfigPropify INSTANCE = new AppConfigPropify();
    
    public static AppConfigPropify getInstance() {
        return INSTANCE;
    }
    
    // Only getters, no setters
    public Server getServer() { return server; }
    public Database getDatabase() { return database; }
}
```

### Nested Classes

Hierarchical configuration generates nested classes:

```yaml
server:
  http:
    port: 8080
    host: localhost
  https:
    port: 8443
    keystore: /path/to/keystore
```

```java
AppConfigPropify config = AppConfigPropify.getInstance();
config.getServer().getHttp().getPort();      // 8080
config.getServer().getHttps().getKeystore(); // "/path/to/keystore"
```

### Type Conversion

Propify automatically converts configuration values to appropriate Java types:

| Config Value | Java Type | Example |
|--------------|-----------|---------|
| `true`, `false` | `boolean` | `enabled: true` → `getEnabled()` returns `boolean` |
| `123`, `-456` | `int` | `port: 8080` → `getPort()` returns `int` |
| `3.14`, `1.5e10` | `double` | `rate: 3.14` → `getRate()` returns `double` |
| `"text"` | `String` | `name: app` → `getName()` returns `String` |
| `[1, 2, 3]` | `List<Integer>` | `ports: [80, 443]` → `getPorts()` returns `List<Integer>` |
| `{key: value}` | Nested object | `db: {url: ...}` → `getDb()` returns nested class |

## Advanced Examples

### Complex Nested Configuration

```yaml
# advanced-config.yml
application:
  name: MyApp
  environment: production
  
security:
  jwt:
    secret: mySecret
    expiration: 3600
  cors:
    allowedOrigins:
      - http://localhost:3000
      - https://example.com
    allowedMethods:
      - GET
      - POST
      - PUT
    
database:
  primary:
    url: jdbc:postgresql://localhost:5432/maindb
    username: admin
    pool:
      minSize: 5
      maxSize: 50
  replica:
    url: jdbc:postgresql://replica:5432/maindb
    username: readonly
    pool:
      minSize: 2
      maxSize: 20
```

```java
@Propify(location = "advanced-config.yml")
public interface AdvancedConfig {}

// Usage
AdvancedConfigPropify config = AdvancedConfigPropify.getInstance();

// Deep nesting
String jwtSecret = config.getSecurity().getJwt().getSecret();
int jwtExpiration = config.getSecurity().getJwt().getExpiration();

// Lists
List<String> origins = config.getSecurity().getCors().getAllowedOrigins();
List<String> methods = config.getSecurity().getCors().getAllowedMethods();

// Multiple database configurations
int primaryMaxSize = config.getDatabase().getPrimary().getPool().getMaxSize();
int replicaMaxSize = config.getDatabase().getReplica().getPool().getMaxSize();
```

### Environment-Specific Configuration

```yaml
# config.yml
app:
  name: MyApplication
  version: 1.0.0
  
server:
  host: "{env:SERVER_HOST}"
  port: "{env:SERVER_PORT}"
  
database:
  url: "jdbc:mysql://{env:DB_HOST}:{env:DB_PORT}/{env:DB_NAME}"
  username: "{env:DB_USER}"
  password: "{env:DB_PASSWORD}"
  
external:
  apiKey: "{vault:external-api-key}"
  apiUrl: "{env:EXTERNAL_API_URL}"
```

```java
@Propify(
    location = "config.yml",
    lookups = {
        EnvironmentLookup.class,
        VaultLookup.class
    }
)
public interface AppConfig {}

// All environment variables and vault values are resolved at compile time
AppConfigPropify config = AppConfigPropify.getInstance();
String serverHost = config.getServer().getHost();  // from $SERVER_HOST
String dbUrl = config.getDatabase().getUrl();      // fully interpolated URL
```

### Mixed Format Configuration

```java
// YAML for complex hierarchical config
@Propify(location = "application.yml")
public interface AppConfig {}

// Properties for simple flat config
@Propify(location = "build.properties")
public interface BuildInfo {}

// INI for legacy systems
@Propify(
    location = "legacy.ini",
    mediaType = "text/plain"
)
public interface LegacyConfig {}

// Usage in application
public class Application {
    private static final AppConfigPropify appConfig = AppConfigPropify.getInstance();
    private static final BuildInfoPropify buildInfo = BuildInfoPropify.getInstance();
    private static final LegacyConfigPropify legacyConfig = LegacyConfigPropify.getInstance();
    
    public static void main(String[] args) {
        System.out.println("App: " + appConfig.getApp().getName());
        System.out.println("Version: " + buildInfo.getVersion());
        System.out.println("Legacy: " + legacyConfig.getSystem().getPath());
    }
}
```

## Type Mapping Reference

### Scalar Types

```yaml
# String
name: "MyApp"
description: "A sample application"

# Integer
port: 8080
maxConnections: 1000

# Float/Double
ratio: 0.75
price: 99.99

# Boolean
enabled: true
debug: false
```

### Collections

```yaml
# List of strings
tags:
  - java
  - spring
  - microservices

# List of integers
ports:
  - 8080
  - 8443
  - 9090

# List of objects
servers:
  - host: server1.example.com
    port: 8080
  - host: server2.example.com
    port: 8081
```

### Nested Objects

```yaml
database:
  connection:
    url: jdbc:mysql://localhost:3306/db
    timeout: 30
  pool:
    size: 10
    maxWait: 5000
```

Generated accessor methods:

```java
config.getDatabase().getConnection().getUrl()      // String
config.getDatabase().getConnection().getTimeout()  // int
config.getDatabase().getPool().getSize()           // int
config.getDatabase().getPool().getMaxWait()        // int
```

## Common Use Cases

### 1. Application Configuration

```yaml
# application.yml
app:
  name: "E-Commerce Platform"
  version: "2.0.0"
  baseUrl: "https://example.com"
  
server:
  port: 8080
  contextPath: "/api"
  
logging:
  level: INFO
  file: "/var/log/app.log"
```

### 2. Database Configuration

```yaml
# database.yml
datasource:
  primary:
    driver: org.postgresql.Driver
    url: jdbc:postgresql://localhost:5432/maindb
    username: admin
    password: secret
    
  cache:
    driver: org.h2.Driver
    url: jdbc:h2:mem:cache
    
  pool:
    initialSize: 10
    maxSize: 50
    minIdle: 5
    maxWait: 10000
```

### 3. Feature Flags

```yaml
# features.yml
features:
  darkMode: true
  newCheckout: false
  betaFeatures: true
  experimentalSearch: false
  
rollout:
  darkMode: 100
  newCheckout: 10
  betaFeatures: 50
```

### 4. Integration Configuration

```yaml
# integrations.yml
aws:
  region: us-west-2
  accessKeyId: "{env:AWS_ACCESS_KEY_ID}"
  secretAccessKey: "{env:AWS_SECRET_ACCESS_KEY}"
  s3:
    bucket: my-app-bucket
    
stripe:
  apiKey: "{vault:stripe-api-key}"
  webhookSecret: "{vault:stripe-webhook-secret}"
  
sendgrid:
  apiKey: "{vault:sendgrid-api-key}"
  fromEmail: "noreply@example.com"
```

## Best Practices

### 1. Organize Configuration Files

```
src/main/resources/
├── application.yml          # Main application config
├── database.yml             # Database settings
├── security.yml             # Security settings
├── integrations.yml         # External service configs
└── features.yml             # Feature flags
```

```java
@Propify(location = "application.yml")
public interface AppConfig {}

@Propify(location = "database.yml")
public interface DatabaseConfig {}

@Propify(location = "security.yml")
public interface SecurityConfig {}
```

### 2. Use Environment Variables for Secrets

```yaml
# Good: Use placeholders for sensitive data
database:
  password: "{env:DB_PASSWORD}"
  
aws:
  secretKey: "{env:AWS_SECRET_KEY}"

# Bad: Hardcode secrets
database:
  password: "my-secret-password"  # DON'T DO THIS
```

### 3. Provide Sensible Defaults

```yaml
server:
  host: localhost
  port: 8080
  
cache:
  enabled: true
  ttl: 3600
  maxSize: 1000
```

### 4. Use Descriptive Names

```yaml
# Good: Clear and descriptive
database:
  connectionTimeout: 5000
  maxPoolSize: 20
  
# Bad: Cryptic abbreviations
db:
  ct: 5000
  mps: 20
```

### 5. Group Related Settings

```yaml
# Good: Grouped by feature
email:
  smtp:
    host: smtp.example.com
    port: 587
    username: user@example.com
  templates:
    welcome: "templates/welcome.html"
    reset: "templates/reset.html"

# Bad: Flat structure
email.smtp.host: smtp.example.com
email.smtp.port: 587
email.smtp.username: user@example.com
email.templates.welcome: "templates/welcome.html"
```

## Troubleshooting

### Configuration File Not Found

**Problem**: `File not found: application.yml`

**Solutions**:
- Ensure file is in `src/main/resources/`
- Check file name spelling and extension
- For file system paths, use absolute paths: `file:///path/to/config.yml`
- For URLs, ensure the server is accessible at build time

### Type Conversion Errors

**Problem**: Cannot convert value to expected type

**Solutions**:
- Check that config values match expected types
- Use quotes for string values: `port: "8080"` vs `port: 8080`
- Verify boolean values are lowercase: `true`/`false`

### Lookup Resolution Fails

**Problem**: Placeholder not resolved: `{env:MY_VAR}`

**Solutions**:
- Ensure lookup class is included in `lookups` parameter
- Verify environment variable exists at build time
- Check lookup prefix matches placeholder prefix

### Generated Class Not Found

**Problem**: Cannot find generated class

**Solutions**:
- Rebuild the project to trigger annotation processing
- Check Maven/Gradle annotation processor configuration
- Verify `propify` is in `annotationProcessorPaths` or `annotationProcessor`

## Comparison with Alternatives

### vs. Spring @ConfigurationProperties

| Feature | @Propify | Spring @ConfigurationProperties |
|---------|----------|--------------------------------|
| **Framework Dependency** | None | Requires Spring |
| **Data Loading** | Compile-time | Runtime |
| **Performance** | Zero overhead | Runtime parsing |
| **Type Safety** | Full | Full |
| **Validation** | Compile-time | Runtime |
| **Use Case** | Any Java app | Spring applications |

### vs. Apache Commons Configuration

| Feature | @Propify | Commons Configuration |
|---------|----------|----------------------|
| **Type Safety** | Strong | Weak (string-based) |
| **Error Detection** | Compile-time | Runtime |
| **Code Generation** | Yes | No |
| **Ease of Use** | Annotation-based | API-based |
| **Dependencies** | Zero runtime | Runtime library |

### vs. Typesafe Config (Lightbend)

| Feature | @Propify | Typesafe Config |
|---------|----------|----------------|
| **Type Safety** | Native Java types | String-based with casting |
| **IDE Support** | Full autocomplete | Limited |
| **Compile-Time Validation** | Yes | No |
| **HOCON Support** | No | Yes |
| **Simplicity** | Very simple | More complex |

## Limitations

- **Read-Only**: Generated configuration classes are immutable. For mutable data classes, use `@SchemaGen` instead.
- **Build-Time Resolution**: All values (including lookups) are resolved at compile time. Runtime configuration changes require recompilation.
- **Format Support**: Currently supports YAML, Properties, and INI. Other formats may require manual `mediaType` specification or are not supported.
- **Complex Types**: Limited support for complex generic types beyond `List<T>` and nested objects.

## Migration Guide

### From String-Based Properties

**Before**:

```java
Properties props = new Properties();
props.load(new FileInputStream("config.properties"));

String host = props.getProperty("server.host");
int port = Integer.parseInt(props.getProperty("server.port"));
```

**After**:

```java
@Propify(location = "config.properties")
public interface Config {}

ConfigPropify config = ConfigPropify.getInstance();
String host = config.getServer().getHost();
int port = config.getServer().getPort();  // Already typed!
```

### From Spring @Value

**Before**:

```java
@Component
public class MyService {
    @Value("${server.host}")
    private String host;
    
    @Value("${server.port}")
    private int port;
}
```

**After**:

```java
@Propify(location = "application.yml")
public interface AppConfig {}

@Component
public class MyService {
    private final AppConfigPropify config = AppConfigPropify.getInstance();
    
    public void doSomething() {
        String host = config.getServer().getHost();
        int port = config.getServer().getPort();
    }
}
```

## Contributing

Found a bug or want to contribute? Please see the main [CONTRIBUTING.md](../CONTRIBUTING.md) for guidelines.

## License

MIT License - see [LICENSE](../LICENSE)
