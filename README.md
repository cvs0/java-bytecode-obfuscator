# Java Bytecode Obfuscator

A powerful and flexible Java bytecode obfuscator built with ASM that provides comprehensive obfuscation capabilities while maintaining fine-grained control over what gets obfuscated.

## ✨ Features

### 🔧 Core Obfuscation
- **Class Renaming** - Rename classes to obscure names while preserving functionality
- **Method Renaming** - Obfuscate method names with intelligent handling of constructors, synthetic methods, and inheritance
- **Field Renaming** - Rename fields while maintaining proper access relationships
- **Local Variable Renaming** - Obfuscate local variable names for additional protection
- **Reference Updating** - Automatically updates all references to renamed elements throughout the codebase
- **Inheritance-Aware Renaming** - Properly handles interface implementations and method overrides

### 🎯 Advanced Configuration
- **Keep Rules System** - Sophisticated rules for preserving specific classes, methods, and fields
- **Pattern Matching** - Use regex patterns to define keep rules for multiple elements at once
- **Granular Control** - Specify exactly what to keep at the class, method, and field level
- **Configuration Files** - JSON-based configuration with presets for common scenarios
- **Command Line Interface** - Full-featured CLI with extensive options

### 🔄 Extensible Architecture
- **Transformer Pipeline** - Modular transformer system with priority-based execution
- **Abstract Base Classes** - Easy to extend with custom obfuscation techniques
- **Plugin System** - Add new transformers without modifying core code
- **Context Sharing** - Transformers can share data and coordinate operations

### 📊 Enhanced Logging & Analysis
- **Rich Console Output** - Beautiful logging with emojis and color coding
- **Timestamped Logs** - All operations are timestamped for debugging
- **Progress Tracking** - Visual progress indicators and statistics
- **Mapping Generation** - Generate detailed mappings of original to obfuscated names
- **Verbose Mode** - Detailed logging of all transformations performed
- **Validation System** - Comprehensive validation with warnings and error detection

### 🛠️ Smart Handling
- **Method References** - Properly handles Java 8+ method references (::) in lambda expressions
- **Synthetic Methods** - Intelligently handles compiler-generated synthetic methods
- **Bridge Methods** - Correct handling of bridge methods in inheritance hierarchies
- **Entry Point Protection** - Automatic preservation of main methods and constructors
- **JAR Structure** - Preserves non-class files (resources, manifests, etc.)

## 🚀 Quick Start

### Installation

Download the latest release or build from source:

```bash
git clone https://github.com/cvs0/java-bytecode-obfuscator.git
cd java-bytecode-obfuscator
./gradlew build
```

### Basic Usage

```bash
# Simple obfuscation with all features enabled
java -jar java-bytecode-obfuscator-1.0-SNAPSHOT.jar input.jar output.jar \
  --rename-classes --rename-methods --rename-fields --rename-local-variables

# With main class protection
java -jar java-bytecode-obfuscator-1.0-SNAPSHOT.jar input.jar output.jar \
  -m com.example.Main --keep-main-class --keep-entry-points \
  --rename-classes --rename-methods --rename-fields --rename-local-variables

# Generate mappings for debugging
java -jar java-bytecode-obfuscator-1.0-SNAPSHOT.jar input.jar output.jar \
  --mappings mappings.txt --verbose \
  --rename-classes --rename-methods --rename-fields --rename-local-variables
```

### Configuration File

Create a JSON configuration file for complex scenarios:

```json
{
  "mainClass": "com/example/Main",
  "renameClasses": true,
  "renameFields": true,
  "renameMethods": true,
  "verbose": true,
  "keepRules": {
    "keepMainClass": true,
    "keepStandardEntryPoints": true,
    "keepClasses": ["com/example/api/PublicAPI"],
    "keepClassPatterns": [".*Controller"],
    "keepMethods": {
      "com/example/Service": ["publicMethod"]
    },
    "keepMethodPatterns": {
      ".*Controller": ["handle.*"]
    }
  }
}
```

### Programmatic Usage

```java
import net.cvs0.Obfuscator;
import net.cvs0.config.ObfuscationConfig;

ObfuscationConfig config = new ObfuscationConfig.Builder()
    .mainClass("com/example/Application")
    .renameClasses(true)
    .renameFields(true)
    .renameMethods(true)
    .verbose(true)
    
    // Keep specific classes
    .keepClass("com/example/api/PublicAPI")
    .keepClassPattern("com/example/config/.*")
    
    // Keep specific methods
    .keepClassMethod("com/example/Service", "publicMethod")
    .keepClassMethodPattern(".*Controller", "handle.*")
    .keepAllMethodsForClass("com/example/PublicInterface")
    
    // Keep specific fields
    .keepClassField("com/example/Constants", "VERSION")
    .keepClassFieldPattern(".*Entity", "id")
    .keepAllFieldsForClass("com/example/Config")
    
    // Convenience methods
    .keepMainClass()
    .keepStandardEntryPoints()
    .build();

Obfuscator obfuscator = new Obfuscator();
obfuscator.obfuscate(inputJar, outputJar, config, mappingsFile);
```

## CLI Reference

### Command Line Options

```
Usage: obfuscator [OPTIONS] <input-jar> <output-jar>

Arguments:
  <input-jar>                 Input JAR file to obfuscate
  <output-jar>                Output JAR file

Options:
  -c, --config <file>         Configuration file (JSON)
  -m, --main-class <class>    Main class name
      --rename-classes        Enable class renaming
      --rename-fields         Enable field renaming  
      --rename-methods        Enable method renaming
      --mappings <file>       Output mappings file
  -v, --verbose               Enable verbose output
      --keep-class <class>    Keep specific class (repeatable)
      --keep-class-pattern <pattern>  Keep classes matching pattern (repeatable)
      --keep-main-class       Keep the main class
      --keep-entry-points     Keep standard entry points
  -h, --help                  Show help
      --version               Show version
```

### CLI Examples

```bash
# Basic obfuscation
java -jar obfuscator.jar input.jar output.jar

# With configuration file
java -jar obfuscator.jar -c config.json input.jar output.jar

# Command line configuration
java -jar obfuscator.jar input.jar output.jar \
  --main-class com/example/Main \
  --keep-main-class \
  --keep-entry-points \
  --verbose

# Keep specific classes
java -jar obfuscator.jar input.jar output.jar \
  --keep-class "com/example/api/PublicAPI" \
  --keep-class-pattern ".*Controller" \
  --keep-class-pattern ".*Service"

# Generate mappings
java -jar obfuscator.jar input.jar output.jar --mappings mappings.txt

# Override config file settings
java -jar obfuscator.jar -c config.json input.jar output.jar \
  --rename-classes false \
  --verbose
```

## Configuration File Examples

### Basic Configuration (`config-examples/basic.json`)
```json
{
  "mainClass": "com/example/Main",
  "renameClasses": true,
  "renameFields": true,
  "renameMethods": true,
  "verbose": true,
  "keepRules": {
    "keepMainClass": true,
    "keepStandardEntryPoints": true
  }
}
```

### Spring Boot Application (`config-examples/spring-boot.json`)
```json
{
  "mainClass": "com/example/Application",
  "renameClasses": true,
  "renameFields": true,
  "renameMethods": true,
  "verbose": false,
  "keepRules": {
    "keepMainClass": true,
    "keepStandardEntryPoints": true,
    "keepClassPatterns": [
      ".*Application", ".*Controller", ".*Configuration", ".*Config.*"
    ],
    "keepMethodPatterns": {
      ".*Controller": [".*"],
      ".*Service": [".*public.*"],
      ".*Repository": [".*"]
    },
    "keepFieldPatterns": {
      ".*Entity": [".*"],
      ".*Configuration": [".*"],
      ".*Properties": [".*"]
    }
  }
}
```

### Library Obfuscation (`config-examples/library.json`)
```json
{
  "renameClasses": false,
  "renameFields": true,
  "renameMethods": false,
  "verbose": false,
  "keepRules": {
    "keepStandardEntryPoints": true,
    "keepClassPatterns": ["com/mylib/api/.*", "com/mylib/public/.*"],
    "keepMethodPatterns": {
      ".*": [".*public.*"]
    },
    "keepAllMethods": ["com/mylib/PublicInterface"],
    "keepFieldPatterns": {
      "com/mylib/api/.*": [".*"],
      "com/mylib/constants/.*": [".*"]
    }
  }
}
```

### Advanced Configuration (`config-examples/advanced.json`)
```json
{
  "mainClass": "com/example/MyApp",
  "renameClasses": true,
  "renameFields": true,
  "renameMethods": true,
  "verbose": true,
  "keepRules": {
    "keepMainClass": true,
    "keepStandardEntryPoints": true,
    "keepClasses": ["com/example/api/PublicAPI"],
    "keepClassPatterns": [".*Controller", "com/example/dto/.*"],
    "keepMethods": {
      "com/example/Service": [
        "publicMethod",
        {"name": "specificMethod", "descriptor": "(Ljava/lang/String;)V"}
      ]
    },
    "keepMethodPatterns": {
      ".*Controller": ["handle.*", "process.*"],
      ".*Service": ["get.*", "set.*"]
    },
    "keepAllMethods": ["com/example/PublicInterface"],
    "keepFields": {
      "com/example/Constants": ["VERSION", "BUILD_DATE"]
    },
    "keepFieldPatterns": {
      ".*Entity": ["id", ".*Date"],
      ".*Model": [".*"]
    },
    "keepAllFields": ["com/example/GlobalConstants"]
  }
}
```

## Keep Rules Reference

### Class-Level Rules

| Method | Description | Example |
|--------|-------------|---------|
| `keepClass(String)` | Keep specific class | `.keepClass("com/example/API")` |
| `keepClassPattern(String)` | Keep classes matching pattern | `.keepClassPattern(".*Controller")` |

### Method-Level Rules

| Method | Description | Example |
|--------|-------------|---------|
| `keepClassMethod(String, String)` | Keep specific method | `.keepClassMethod("MyClass", "publicMethod")` |
| `keepClassMethodWithDescriptor(String, String, String)` | Keep method with descriptor | `.keepClassMethodWithDescriptor("MyClass", "method", "(I)V")` |
| `keepClassMethodPattern(String, String)` | Keep methods matching pattern | `.keepClassMethodPattern(".*Service", "get.*")` |
| `keepAllMethodsForClass(String)` | Keep all methods in class | `.keepAllMethodsForClass("PublicAPI")` |

### Field-Level Rules

| Method | Description | Example |
|--------|-------------|---------|
| `keepClassField(String, String)` | Keep specific field | `.keepClassField("Config", "VERSION")` |
| `keepClassFieldPattern(String, String)` | Keep fields matching pattern | `.keepClassFieldPattern(".*Entity", "id")` |
| `keepAllFieldsForClass(String)` | Keep all fields in class | `.keepAllFieldsForClass("Constants")` |

### Convenience Methods

| Method | Description |
|--------|-------------|
| `keepMainClass()` | Keep the main class (requires `mainClass()` to be set) |
| `keepStandardEntryPoints()` | Keep main methods, constructors, and static initializers |

## Advanced Usage

### Custom Transformers

```java
public class CustomTransformer extends AbstractTransformer {
    public CustomTransformer() {
        super("CustomTransformer", 400); // Name and priority
    }
    
    @Override
    public void transform(ClassReader reader, ClassWriter writer, ObfuscationContext context) {
        // Your custom transformation logic
        logTransformation("Applying custom transformation", context);
        
        // Apply transformation
        ClassVisitor visitor = new CustomVisitor(writer, context);
        reader.accept(visitor, 0);
    }
    
    @Override
    public boolean isEnabled(ObfuscationContext context) {
        return context.getConfig().getProperty("enableCustom", Boolean.class, false);
    }
}

// Register custom transformer
Obfuscator obfuscator = new Obfuscator();
obfuscator.getEngine().registerTransformer(new CustomTransformer());
```

### Configuration Validation

```java
ObfuscationConfig config = // ... your config

ConfigValidator.ValidationResult result = ConfigValidator.validate(config);

if (result.hasErrors()) {
    System.err.println("Configuration errors:");
    result.getErrors().forEach(error -> System.err.println("  - " + error));
    return;
}

if (result.hasWarnings()) {
    System.out.println("Configuration warnings:");
    result.getWarnings().forEach(warning -> System.out.println("  - " + warning));
}
```

### Mapping File Format

The generated mapping file contains the transformation mappings:

```
# Class mappings
com/example/MyClass -> a
com/example/Service -> b

# Field mappings
com/example/MyClass.fieldName -> a.a
com/example/Service.config -> b.b

# Method mappings
com/example/MyClass.methodName()V -> a.a()V
com/example/Service.processData(Ljava/lang/String;)I -> b.a(Ljava/lang/String;)I
```

## Configuration Examples

### Minimal Obfuscation
```java
ObfuscationConfig config = new ObfuscationConfig.Builder()
    .renameClasses(false)
    .renameFields(true)      // Only obfuscate fields
    .renameMethods(false)
    .keepStandardEntryPoints()
    .build();
```

### Aggressive Obfuscation
```java
ObfuscationConfig config = ConfigPresets.createAggressiveObfuscation()
    .build(); // Minimal keep rules for maximum obfuscation
```

### Framework-Specific Configurations

#### Spring Framework
```java
ObfuscationConfig config = new ObfuscationConfig.Builder()
    .renameClasses(true)
    .renameFields(true)
    .renameMethods(true)
    .keepClassPattern(".*Configuration")
    .keepClassPattern(".*Controller")
    .keepClassMethodPattern(".*Component", ".*")
    .keepClassFieldPattern(".*Entity", ".*")
    .keepStandardEntryPoints()
    .build();
```

#### Android Applications
```java
ObfuscationConfig config = new ObfuscationConfig.Builder()
    .renameClasses(true)
    .renameFields(true)
    .renameMethods(true)
    .keepClassPattern(".*Activity")
    .keepClassPattern(".*Service")
    .keepClassPattern(".*BroadcastReceiver")
    .keepClassMethodPattern(".*Activity", "onCreate.*")
    .keepClassMethodPattern(".*Activity", "onResume.*")
    .keepStandardEntryPoints()
    .build();
```

## Architecture

### Core Components

- **ObfuscationEngine** - Main processing engine that orchestrates the obfuscation
- **TransformerPipeline** - Manages and executes transformers in priority order
- **ObfuscationContext** - Shared context containing configuration and mappings
- **AbstractTransformer** - Base class for all transformers with common functionality

### Built-in Transformers

1. **ClassRenameTransformer** (Priority: 100)
   - Renames classes and updates all references
   - Handles inheritance and interface implementations

2. **FieldRenameTransformer** (Priority: 200)
   - Renames fields and updates field access instructions
   - Preserves field access relationships

3. **MethodRenameTransformer** (Priority: 300)
   - Renames methods and updates method invocations
   - Skips constructors and static initializers

### Validation System

- **Input Validation** - Validates JAR files, output paths, and file permissions
- **Configuration Validation** - Validates keep rules, patterns, and configuration consistency
- **Pattern Validation** - Validates regex patterns and class name formats

## Best Practices

### 1. Always Keep Entry Points
```java
.keepStandardEntryPoints()  // Keep main methods, constructors, etc.
```

### 2. Use Patterns for Similar Classes
```java
.keepClassPattern(".*Controller")      // Instead of individual controllers
.keepClassMethodPattern(".*Entity", "get.*")  // Keep all getters in entities
```

### 3. Test with Verbose Logging
```java
.verbose(true)  // Enable detailed logging during development
```

### 4. Validate Configuration
```java
ConfigValidator.ValidationResult result = ConfigValidator.validate(config);
// Check result before obfuscating
```

### 5. Keep Reflection-Used Elements
```java
.keepClass("com/example/ReflectionUsedClass")
.keepAllMethodsForClass("com/example/ReflectionUsedClass")
```

## Troubleshooting

### Common Issues

**ClassNotFoundException after obfuscation**
- Ensure main class is kept: `.keepMainClass()`
- Check if reflection is used: keep reflected classes
- Verify entry points are preserved: `.keepStandardEntryPoints()`

**NoSuchMethodError after obfuscation**
- Keep public API methods: `.keepClassMethodPattern(".*API", ".*")`
- Preserve interface implementations
- Check for dynamic method calls

**Compilation errors with obfuscated JAR**
- Validate keep rules with `ConfigValidator`
- Use verbose logging to see what's being renamed
- Test with minimal obfuscation first

### Debug Mode
```java
ObfuscationConfig config = ConfigPresets.createDebugObfuscation()
    .verbose(true)
    .build();
```

## Contributing

### Adding New Transformers

1. Extend `AbstractTransformer`
2. Implement required methods
3. Set appropriate priority
4. Register with `ObfuscationEngine`

### Configuration Extensions

1. Add new properties to `ObfuscationConfig`
2. Update `Builder` class
3. Add validation in `ConfigValidator`
4. Create preset if needed

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Changelog

### Version 1.0.0
- Initial release with class, method, and field renaming
- Advanced keep rules system
- Configuration presets
- Comprehensive validation
- Mapping generation
- Extensible transformer architecture