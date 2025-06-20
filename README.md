# Java Bytecode Obfuscator

A powerful and flexible Java bytecode obfuscator built with ASM that provides comprehensive obfuscation capabilities while maintaining fine-grained control over what gets obfuscated.

## ✨ Features

### 🔧 Core Obfuscation
- **Class Renaming** - Rename classes to obscure names while preserving functionality
- **Method Renaming** - Obfuscate method names with intelligent handling of constructors, synthetic methods, and inheritance
- **Field Renaming** - Rename fields while maintaining proper access relationships
- **Local Variable Renaming** - Obfuscate local variable names for additional protection
- **Condition Obfuscation** - Transform simple boolean constants (true/false) into complex arithmetic expressions
- **String Compression** - Compress string literals using deflate/base64 encoding to reduce size and obfuscate content
- **String Compression** - Compress string literals using deflate/base64 encoding to reduce size and obfuscate content
- **Anti-Debugging Protection** - Runtime detection and response to debugging attempts
- **Reference Updating** - Automatically updates all references to renamed elements throughout the codebase
- **Inheritance-Aware Renaming** - Properly handles interface implementations and method overrides
- **Multiple Naming Modes** - Choose from sequential, alphabetic, random short/long, or single character naming schemes

### 🎯 Advanced Configuration
- **Obfuscation Levels** - Pre-defined security levels (MINIMAL, BASIC, AGGRESSIVE, EXTREME, CUSTOM)
- **Keep Rules System** - Sophisticated rules for preserving specific classes, methods, and fields
- **Pattern Matching** - Use regex patterns to define keep rules for multiple elements at once
- **Package Filtering** - Include/exclude specific packages with fine-grained control
- **Granular Control** - Specify exactly what to keep at the class, method, and field level
- **Configuration Files** - Enhanced JSON-based configuration with nested structure and presets
- **Configuration Validation** - Comprehensive validation with detailed warnings and error reporting
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
- **Obfuscation Resistance Scoring** - Comprehensive analysis of obfuscation strength with recommendations
- **Verbose Mode** - Detailed logging of all transformations performed
- **Validation System** - Comprehensive validation with warnings and error detection
- **Performance Monitoring** - Execution statistics and optimization recommendations

### 🛠️ Smart Handling
- **Method References** - Properly handles Java 8+ method references (::) in lambda expressions
- **Synthetic Methods** - Intelligently handles compiler-generated synthetic methods
- **Bridge Methods** - Correct handling of bridge methods in inheritance hierarchies
- **Entry Point Protection** - Automatic preservation of main methods and constructors
- **JAR Structure** - Preserves non-class files (resources, manifests, etc.)
- **Multi-threading** - Configurable parallel processing for improved performance
- **Backup System** - Automatic backup creation with configurable directory
- **String Compression** - Advanced string obfuscation and compression techniques
- **Code Optimization** - Built-in bytecode optimization during obfuscation

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
  --rename-classes --rename-methods --rename-fields --rename-local-variables \
  \
  --obfuscate-conditions --compress-strings --compress-strings

# Maximum security with anti-debugging and resistance scoring
java -jar java-bytecode-obfuscator-1.0-SNAPSHOT.jar input.jar output.jar \
  --rename-classes --rename-methods --rename-fields --rename-local-variables \
  --obfuscate-conditions --compress-strings --compress-strings --anti-debugging \
  \
  --debugger-action EXIT_WITH_ERROR --generate-score --naming-mode RANDOM_LONG

# With main class protection
java -jar java-bytecode-obfuscator-1.0-SNAPSHOT.jar input.jar output.jar \
  -m com.example.Main --keep-main-class --keep-entry-points \
  --rename-classes --rename-methods --rename-fields --rename-local-variables \
  \
  --obfuscate-conditions --compress-strings

# Generate mappings and resistance score
java -jar java-bytecode-obfuscator-1.0-SNAPSHOT.jar input.jar output.jar \
  --mappings mappings.txt --generate-score --verbose \
  --rename-classes --rename-methods --rename-fields --rename-local-variables \
  \
  --obfuscate-conditions --compress-strings --compress-strings

# Use different naming modes
java -jar java-bytecode-obfuscator-1.0-SNAPSHOT.jar input.jar output.jar \
  --naming-mode RANDOM_SHORT --rename-classes --rename-methods --rename-fields \
  \
  --obfuscate-conditions --compress-strings --compress-strings

java -jar java-bytecode-obfuscator-1.0-SNAPSHOT.jar input.jar output.jar \
  --naming-mode SEQUENTIAL_ALPHA --verbose \
  --rename-classes --rename-methods --rename-fields --rename-local-variables \
  \
  --obfuscate-conditions --compress-strings --compress-strings
```

### Enhanced Configuration File

Create a comprehensive JSON configuration file:

```json
{
  "obfuscationLevel": "AGGRESSIVE",
  "mainClass": "com/example/MyApp",
  
  "obfuscation": {
    "renameClasses": true,
    "renameFields": true,
    "renameMethods": true,
    "renameLocalVariables": true,
    "obfuscateConditions": true,
    "compressStrings": true,
    "shuffleMembers": true,
    "optimizeCode": true
  },
  
  "naming": {
    "namingMode": "RANDOM_SHORT"
  },
  
  "security": {
    "antiDebugging": true,
    "debuggerAction": "CORRUPT_EXECUTION"
  },
  
  "packages": {
    "includePackages": ["com/example/core", "com/example/business"],
    "excludePackages": ["com/example/test", "com/example/debug"]
  },
  
  "keepRules": {
    "keepMainClass": true,
    "keepStandardEntryPoints": true,
    "keepClasses": ["com/example/api/PublicAPI"],
    "keepClassPatterns": [".*Controller", ".*Exception"],
    "keepMethods": {
      "com/example/api/PublicAPI": ["publicMethod"]
    },
    "keepMethodPatterns": {
      ".*Controller": ["handle.*", "process.*"]
    }
  },
  
  "debugging": {
    "preserveLineNumbers": false,
    "preserveLocalVariableNames": false,
    "verbose": false,
    "generateScore": true
  },
  
  "performance": {
    "maxThreads": 4,
    "sequentialTransformers": false
  },
  
  "backup": {
    "enableBackup": true,
    "backupDir": "./backups"
  },
  
  "customSettings": {
    "stringEncryptionKey": "myCustomKey123",
    "customTransformerEnabled": true
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
    .renameLocalVariables(true)
    .obfuscateConditions(true)
    .compressStrings(true)
    .compressStrings(true)
    .namingMode(NamingMode.RANDOM_SHORT)
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

## 🎚️ Obfuscation Levels

The obfuscator provides pre-defined security levels that automatically configure optimal settings for different use cases:

### Available Levels

| Level | Description | Use Case | Settings |
|-------|-------------|----------|----------|
| **MINIMAL** | Basic protection with minimal impact | Development, debugging | Classes only, sequential naming |
| **BASIC** | Standard obfuscation for most applications | Production applications | Classes, fields, methods |
| **AGGRESSIVE** | Heavy obfuscation with advanced techniques | Sensitive applications | All features, anti-debugging, shuffling |
| **EXTREME** | Maximum security with performance trade-offs | High-security applications | All features, maximum settings |
| **CUSTOM** | User-defined configuration | Specific requirements | Manual configuration |

### Quick Level Selection

```bash
# Use predefined levels
java -jar obfuscator.jar input.jar output.jar --level AGGRESSIVE

# Override specific settings
java -jar obfuscator.jar input.jar output.jar --level BASIC --anti-debugging
```

### Programmatic Usage

```java
// Quick level selection
ObfuscationConfig config = ConfigPresets.createPresetForLevel(ObfuscationLevel.AGGRESSIVE)
    .mainClass("com/example/Main")
    .keepMainClass()
    .build();

// Pre-built presets
ObfuscationConfig prodConfig = ConfigPresets.createProductionObfuscation()
    .mainClass("com/example/Main")
    .build();

ObfuscationConfig devConfig = ConfigPresets.createDevelopmentObfuscation()
    .mainClass("com/example/Main")
    .build();

ObfuscationConfig secureConfig = ConfigPresets.createSecureObfuscation()
    .mainClass("com/example/Main")
    .build();
```

### Level Details

#### MINIMAL Level
- Class renaming only
- Sequential naming with prefix
- No anti-debugging
- Preserves debugging information
- Fast processing

#### BASIC Level (Default)
- Class, field, and method renaming
- Sequential alphabetic naming
- Basic keep rules
- Balanced security and performance

#### AGGRESSIVE Level
- All renaming features
- Local variable obfuscation
- Condition obfuscation
- Anti-debugging protection
- Member shuffling
- Random short naming

#### EXTREME Level
- Maximum obfuscation settings
- String compression
- Code optimization
- Advanced anti-debugging
- Random long naming
- All security features

## Naming Modes

The obfuscator supports multiple naming modes to generate obfuscated names:

### Available Modes

| Mode | Description | Example Output |
|------|-------------|----------------|
| `SEQUENTIAL_PREFIX` | Sequential with prefix (default) | `a1`, `a2`, `a3`, `m1`, `m2`, `f1`, `f2` |
| `SEQUENTIAL_ALPHA` | Sequential alphabetic | `a`, `b`, `c`, `aa`, `ab`, `ac` |
| `RANDOM_SHORT` | Random short names (4 characters) | `abcd`, `xyzk`, `mnop`, `qrst` |
| `RANDOM_LONG` | Random long names (8-16 characters) | `abcdefgh`, `xyzklmnopqrs` |
| `SINGLE_CHAR` | Single character names | `a`, `b`, `c`, then falls back to `a1`, `a2` |

### Usage Examples

```bash
# Use random short names for maximum obfuscation
java -jar obfuscator.jar input.jar output.jar --naming-mode RANDOM_SHORT

# Use single character names for minimal size
java -jar obfuscator.jar input.jar output.jar --naming-mode SINGLE_CHAR

# Use alphabetic sequence for readability in testing
java -jar obfuscator.jar input.jar output.jar --naming-mode SEQUENTIAL_ALPHA
```

### Configuration File

```json
{
  "namingMode": "RANDOM_SHORT",
  "renameClasses": true,
  "renameFields": true,
  "renameMethods": true
}
```

### Programmatic Usage

```java
import net.cvs0.config.NamingMode;

ObfuscationConfig config = new ObfuscationConfig.Builder()
    .namingMode(NamingMode.RANDOM_LONG)
    .renameClasses(true)
    .renameFields(true)
    .renameMethods(true)
    .build();
```

## Condition Obfuscation

Condition obfuscation transforms simple boolean constants (`true` and `false`) into mathematically equivalent complex expressions that make the code harder to understand and analyze.

### How It Works

The transformer replaces simple boolean constants with arithmetic expressions that evaluate to the same value:

**Original Code:**
```java
if (someFlag == true) {
    doSomething();
}
boolean result = false;
```

**Obfuscated Code:**
```java
// true becomes: 2 - 1
if (someFlag == (2 - 1)) {
    doSomething();
}
// false becomes: 1 - 1  
boolean result = (1 - 1);
```

### Features

- **Safe Transformation** - Only transforms constants that are likely to be boolean conditions (10% probability to avoid breaking non-boolean integer usage)
- **Multiple Strategies** - Uses various mathematical expressions to avoid patterns
- **Stackmap-Safe** - Generates bytecode that passes JVM verification
- **Conservative Approach** - Only targets simple constant loading to maintain program correctness

### Usage

```bash
# Enable condition obfuscation via CLI
java -jar obfuscator.jar input.jar output.jar --obfuscate-conditions

# Combined with other obfuscation techniques
java -jar obfuscator.jar input.jar output.jar \
  --rename-classes --rename-methods --rename-fields \
  --obfuscate-conditions --compress-strings --compress-strings --verbose
```

### Configuration

```json
{
  "renameClasses": true,
  "renameFields": true,
  "renameMethods": true,
  "obfuscateConditions": true,
  "compressStrings": true,
  "compressStrings": true,
  "verbose": true
}
```

### Programmatic Usage

```java
ObfuscationConfig config = new ObfuscationConfig.Builder()
    .renameClasses(true)
    .renameFields(true)
    .renameMethods(true)
    .obfuscateConditions(true)
    .compressStrings(true)
    .compressStrings(true)
    .build();
```

### Examples

**Before Obfuscation:**
```java
public boolean isEnabled() {
    return true;
}

public void process() {
    if (false) {
        handleError();
    }
}
```

**After Obfuscation:**
```java
public boolean a() {
    return 3 - 2;  // Evaluates to 1 (true)
}

public void b() {
    if (5 - 5) {   // Evaluates to 0 (false)
        c();
    }
}

## 🗜️ String Compression

String compression transforms string literals into compressed, base64-encoded data that is decompressed at runtime. This reduces JAR size and makes string analysis more difficult.

### How It Works

The transformer identifies string literals and compresses them using the Deflate algorithm, then encodes them with Base64. At runtime, the compressed strings are decompressed transparently.

**Original Code:**
```java
public class Example {
    public static void main(String[] args) {
        String message = "This is a very long string that contains a lot of repetitive text and should be compressed effectively using the deflate algorithm for better obfuscation."; (transforms boolean constants)
        System.out.println("Application started with message: " + message);
    }
}
```

**Obfuscated Code:**
```java
public class a1 {
    public static void main(String[] args) {
        String a = new String(new java.util.zip.Inflater().inflate(java.util.Base64.getDecoder().decode("eJwLycxNzStJzSvJTFHwSM3JzM+Lz8nPS1WwMjQwMDK1UjA0NDEwMLRSULA0NDQwsDK0MjSyMjKzMjKzMjKzMjK...")), 0, 147, java.nio.charset.StandardCharsets.UTF_8);
        System.out.println("Application started with message: " + a);
    }
}
```

### Features

- **Intelligent String Selection** - Only compresses strings that benefit from compression (length > 10 characters)
- **Special String Detection** - Avoids compressing URLs, configuration strings, class names, and other special patterns
- **Compression Efficiency** - Only applies compression if the result is actually smaller
- **Configurable Probability** - Randomly compresses strings to avoid predictable patterns (70% chance by default)
- **Runtime Transparent** - Decompression happens automatically at runtime
- **Keep Rules Aware** - Respects keep rules and doesn't compress strings in preserved classes

### Usage

```bash
# Enable string compression via CLI
java -jar obfuscator.jar input.jar output.jar --compress-strings

# Combined with other obfuscation techniques
java -jar obfuscator.jar input.jar output.jar \
  --rename-classes --rename-methods --rename-fields \
  --compress-strings --obfuscate-conditions --verbose
```

### Configuration File

```json
{
  "obfuscation": {
    "renameClasses": true,
    "renameFields": true,
    "renameMethods": true,
    "compressStrings": true
  },
  "debugging": {
    "verbose": true
  }
}
```

### Programmatic Usage

```java
ObfuscationConfig config = new ObfuscationConfig.Builder()
    .renameClasses(true)
    .renameFields(true)
    .renameMethods(true)
    .compressStrings(true)
    .verbose(true)
    .build();

Obfuscator obfuscator = new Obfuscator();
obfuscator.obfuscate(inputJar, outputJar, config, mappingsFile);
```

### String Selection Criteria

The transformer automatically determines which strings to compress based on:

| Criteria | Description | Example |
|----------|-------------|---------|
| **Minimum Length** | Strings must be at least 10 characters | `"Short"` → Not compressed |
| **Special Patterns** | Avoids URLs, config strings, file extensions | `"https://example.com"` → Not compressed |
| **Compression Efficiency** | Only compresses if result is smaller | `"aaaaaaaaaa"` → Compressed |
| **Class Keep Rules** | Respects keep rules for classes | Kept classes → Strings not compressed |
| **Random Selection** | 70% probability to avoid patterns | Random selection to prevent analysis |

### Benefits

- **Size Reduction** - Compresses repetitive or long strings effectively
- **Content Obfuscation** - Makes string analysis much more difficult
- **Runtime Performance** - Minimal impact on application startup
- **Reverse Engineering Protection** - Strings are not visible in decompiled code
- **Pattern Breaking** - Random compression prevents pattern-based analysis

### Performance Impact

- **Compression Time** - Adds minimal time during obfuscation
- **Runtime Overhead** - Small one-time decompression cost per string
- **Memory Usage** - Slightly higher memory usage during decompression
- **JAR Size** - Usually reduces overall JAR size for applications with many long strings

### Best Practices

1. **Combine with Other Features** - Use alongside renaming and condition obfuscation
2. **Test Performance** - Profile your application after string compression
3. **Verify Functionality** - Ensure compressed strings work correctly in your use case
4. **Monitor JAR Size** - Check if compression actually reduces your JAR size

### Examples

**Long Strings (Compressed):**
- Error messages and user notifications
- SQL queries and database scripts
- Configuration templates
- Help text and documentation strings

**Short/Special Strings (Not Compressed):**
- Class names and package names
- URLs and file paths
- Single words and short constants
- Format strings and patterns

## CLI Reference

### Command Line Options

```
Usage: obfuscator [OPTIONS] <input-jar> <output-jar>

Arguments:
  <input-jar>                 Input JAR file to obfuscate
  <output-jar>                Output JAR file

Core Options:
  -c, --config <file>         Configuration file (JSON)
  -m, --main-class <class>    Main class name
  -l, --level <level>         Obfuscation level (MINIMAL, BASIC, AGGRESSIVE, EXTREME, CUSTOM)
      --mappings <file>       Output mappings file
  -v, --verbose               Enable verbose output
  -h, --help                  Show help
      --version (deflate/base64 encoding)               Show version

Obfuscation Options:
      --rename-classes        Enable class renaming
      --rename-fields         Enable field renaming  
      --rename-methods        Enable method renaming
      --rename-local-variables Enable local variable renaming
      --obfuscate-conditions  Enable condition obfuscation (transforms boolean constants)
      --compress-strings      Enable string compression (deflate/base64 encoding)
      --shuffle-members       Enable member shuffling
      --optimize-code         Enable code optimization

Naming Options:
  -n, --naming-mode <mode>    Name generation mode (SEQUENTIAL_PREFIX, SEQUENTIAL_ALPHA, 
                              RANDOM_SHORT, RANDOM_LONG, SINGLE_CHAR)

Security Options:
      --anti-debugging        Enable anti-debugging protection
      --debugger-action <action> Debugger response (EXIT_SILENTLY, EXIT_WITH_ERROR, CORRUPT_EXECUTION)

Package Options:
      --include-package <pkg> Include specific package (repeatable)
      --exclude-package <pkg> Exclude specific package (repeatable)

Keep Rules:
      --keep-class <class>    Keep specific class (repeatable)
      --keep-class-pattern <pattern>  Keep classes matching pattern (repeatable)
      --keep-main-class       Keep the main class
      --keep-entry-points     Keep standard entry points

Debugging Options:
      --preserve-line-numbers Preserve line numbers for debugging
      --preserve-local-vars   Preserve local variable names
      --generate-score        Generate obfuscation resistance score

Performance Options:
      --max-threads <n>       Maximum number of threads
      --sequential            Use sequential transformer processing
      --enable-backup         Enable backup creation
      --backup-dir <dir>      Backup directory path
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
  --obfuscate-conditions \
  --verbose

# Keep specific classes
java -jar obfuscator.jar input.jar output.jar \
  --keep-class "com/example/api/PublicAPI" \
  --keep-class-pattern ".*Controller" \
  --keep-class-pattern ".*Service"

# Generate mappings
java -jar obfuscator.jar input.jar output.jar --mappings mappings.txt

# Use different naming modes
java -jar obfuscator.jar input.jar output.jar \
  --naming-mode RANDOM_LONG \
  --rename-classes --rename-methods --rename-fields --obfuscate-conditions

java -jar obfuscator.jar input.jar output.jar \
  --naming-mode SINGLE_CHAR \
  --obfuscate-conditions \
  --verbose

# Override config file settings
java -jar obfuscator.jar -c config.json input.jar output.jar \
  --rename-classes false \
  --naming-mode SEQUENTIAL_ALPHA \
  --obfuscate-conditions \
  --verbose
```

## 📦 Package Filtering

Control which packages are processed during obfuscation with fine-grained include/exclude rules:

### Include Packages
Only process classes in specified packages:

```bash
java -jar obfuscator.jar input.jar output.jar \
  --include-package com.example.core \
  --include-package com.example.business \
  --rename-classes --rename-methods --rename-fields
```

### Exclude Packages
Skip processing of specified packages:

```bash
java -jar obfuscator.jar input.jar output.jar \
  --exclude-package com.example.test \
  --exclude-package com.example.debug \
  --rename-classes --rename-methods --rename-fields
```

### Configuration File
```json
{
  "packages": {
    "includePackages": [
      "com/example/core",
      "com/example/business",
      "com/example/services"
    ],
    "excludePackages": [
      "com/example/test",
      "com/example/debug",
      "com/example/tools"
    ]
  }
}
```

### Programmatic Usage
```java
ObfuscationConfig config = new ObfuscationConfig.Builder()
    .includePackage("com/example/core")
    .includePackage("com/example/business")
    .excludePackage("com/example/test")
    .excludePackage("com/example/debug")
    .renameClasses(true)
    .renameFields(true)
    .renameMethods(true)
    .build();
```

### Behavior Rules
- **Exclude takes precedence** - If a package matches both include and exclude, it will be excluded
- **Hierarchical matching** - `com/example` will match `com/example/core/Service`
- **No rules = process all** - If no include/exclude rules are specified, all packages are processed
- **Validation** - Invalid package names will generate configuration errors

## Configuration File Examples

### Enhanced Configuration (`config-examples/enhanced.json`)
```json
{
  "obfuscationLevel": "AGGRESSIVE",
  "mainClass": "com/example/MyApp",
  
  "obfuscation": {
    "renameClasses": true,
    "renameFields": true,
    "renameMethods": true,
    "renameLocalVariables": true,
    "obfuscateConditions": true,
    "compressStrings": true,
    "shuffleMembers": true,
    "optimizeCode": true
  },
  
  "naming": {
    "namingMode": "RANDOM_SHORT"
  },
  
  "security": {
    "antiDebugging": true,
    "debuggerAction": "CORRUPT_EXECUTION"
  },
  
  "packages": {
    "includePackages": ["com/example/core", "com/example/business"],
    "excludePackages": ["com/example/test", "com/example/debug"]
  },
  
  "keepRules": {
    "keepMainClass": true,
    "keepStandardEntryPoints": true,
    "keepClasses": ["com/example/api/PublicAPI"],
    "keepClassPatterns": [".*Controller", ".*Exception"]
  },
  
  "debugging": {
    "preserveLineNumbers": false,
    "verbose": false,
    "generateScore": true
  },
  
  "performance": {
    "maxThreads": 4,
    "sequentialTransformers": false
  },
  
  "backup": {
    "enableBackup": true,
    "backupDir": "./backups"
  }
}
```

### Basic Configuration (`config-examples/basic.json`)
```json
{
  "obfuscationLevel": "BASIC",
  "mainClass": "com/example/Main",
  "keepRules": {
    "keepMainClass": true,
    "keepStandardEntryPoints": true
  },
  "debugging": {
    "verbose": true
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
  "renameLocalVariables": true,
  "obfuscateConditions": true,
  "namingMode": "RANDOM_SHORT",
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
  "namingMode": "SINGLE_CHAR",
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
  "renameLocalVariables": true,
  "obfuscateConditions": true,
  "namingMode": "RANDOM_LONG",
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

The enhanced validation system provides comprehensive checking with detailed feedback:

```java
// Validate complete configuration
ObfuscationConfig config = // ... your config
ConfigValidator.ValidationResult result = ConfigValidator.validate(config);

if (result.hasErrors()) {
    System.err.println("Configuration errors:");
    result.getErrors().forEach(error -> System.err.println("  ❌ " + error));
    return;
}

if (result.hasWarnings()) {
    System.out.println("Configuration warnings:");
    result.getWarnings().forEach(warning -> System.out.println("  ⚠️ " + warning));
}

// Validate builder before building
ObfuscationConfig.Builder builder = new ObfuscationConfig.Builder()
    .renameClasses(true)
    .maxThreads(0); // Invalid setting

if (!builder.isValid()) {
    ConfigValidator.ValidationResult validation = builder.validate();
    validation.getErrors().forEach(System.err::println);
}

ObfuscationConfig config = builder.build(); // Will throw if invalid
```

### Validation Features

- **Package name validation** - Ensures valid Java package names
- **Class name validation** - Checks for valid class names and patterns
- **Performance settings** - Validates thread counts and backup directories
- **Consistency checks** - Detects conflicting settings
- **Security warnings** - Identifies potential security issues
- **Best practice recommendations** - Suggests optimal configurations

### Common Validation Messages

| Type | Message | Solution |
|------|---------|----------|
| Error | "Max threads must be at least 1" | Set `maxThreads` to 1 or higher |
| Error | "Backup enabled but no directory specified" | Set `backupDir` when `enableBackup` is true |
| Warning | "Anti-debugging enabled but line numbers preserved" | Disable `preserveLineNumbers` for better security |
| Warning | "Include package is within excluded package" | Review package include/exclude rules |

## ⚡ Performance & Threading

The obfuscator supports multi-threaded processing and performance optimization:

### Threading Configuration

```java
// Configure thread count
ObfuscationConfig config = new ObfuscationConfig.Builder()
    .maxThreads(8)                    // Use 8 threads
    .sequentialTransformers(false)    // Enable parallel processing
    .build();

// Auto-detect optimal thread count
ObfuscationConfig config = new ObfuscationConfig.Builder()
    .maxThreads(Runtime.getRuntime().availableProcessors())
    .build();
```

### Sequential vs Parallel Processing

#### Parallel Processing (Default)
- Each transformer processes all classes simultaneously
- Better performance for large projects
- Uses multiple CPU cores efficiently

#### Sequential Processing
- Each transformer processes one class at a time
- More predictable memory usage
- Better for memory-constrained environments

```java
// Enable sequential processing
ObfuscationConfig config = new ObfuscationConfig.Builder()
    .sequentialTransformers(true)
    .maxThreads(1)  // Limited threading benefit with sequential
    .build();
```

### Performance Optimization

```java
ObfuscationConfig config = new ObfuscationConfig.Builder()
    .optimizeCode(true)           // Enable bytecode optimization
    .maxThreads(8)               // Utilize multiple cores
    .sequentialTransformers(false) // Parallel processing
    .enableBackup(false)         // Skip backup for speed (not recommended)
    .verbose(false)              // Reduce logging overhead
    .build();
```

### Backup System

Automatic backup creation with configurable options:

```java
ObfuscationConfig config = new ObfuscationConfig.Builder()
    .enableBackup(true)
    .backupDir("./backups")      // Custom backup directory
    .build();
```

### CLI Options
```bash
# Performance-optimized processing
java -jar obfuscator.jar input.jar output.jar \
  --max-threads 8 \
  --optimize-code \
  --enable-backup \
  --backup-dir ./backups

# Memory-efficient processing
java -jar obfuscator.jar input.jar output.jar \
  --max-threads 2 \
  --sequential \
  --no-backup
```

### Performance Tips

1. **Use appropriate thread count** - Usually CPU cores × 1-2
2. **Enable code optimization** - Improves output quality
3. **Disable backup for speed** - Only in development/testing
4. **Use sequential for large JARs** - Better memory management
5. **Monitor memory usage** - Adjust thread count if needed

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

### Using Obfuscation Levels
```java
// Quick preset selection
ObfuscationConfig minimal = ConfigPresets.createPresetForLevel(ObfuscationLevel.MINIMAL)
    .mainClass("com/example/Main")
    .keepMainClass()
    .build();

// Production-ready configuration
ObfuscationConfig production = ConfigPresets.createProductionObfuscation()
    .mainClass("com/example/Main")
    .keepMainClass()
    .build();

// Development configuration with debugging
ObfuscationConfig development = ConfigPresets.createDevelopmentObfuscation()
    .mainClass("com/example/Main")
    .keepMainClass()
    .build();

// Maximum security configuration
ObfuscationConfig secure = ConfigPresets.createSecureObfuscation()
    .mainClass("com/example/Main")
    .keepMainClass()
    .build();
```

### Custom Configurations

#### Minimal Obfuscation
```java
ObfuscationConfig config = new ObfuscationConfig.Builder()
    .obfuscationLevel(ObfuscationLevel.MINIMAL)
    .renameClasses(false)
    .renameFields(true)      // Only obfuscate fields
    .renameMethods(false)
    .namingMode(NamingMode.SINGLE_CHAR)
    .keepStandardEntryPoints()
    .build();
```

#### Aggressive Obfuscation
```java
ObfuscationConfig config = new ObfuscationConfig.Builder()
    .obfuscationLevel(ObfuscationLevel.AGGRESSIVE)
    .renameClasses(true)
    .renameFields(true)
    .renameMethods(true)
    .renameLocalVariables(true)
    .obfuscateConditions(true)
    .antiDebugging(true)
    .shuffleMembers(true)
    .namingMode(NamingMode.RANDOM_LONG)
    .keepStandardEntryPoints()
    .build();
```

#### Performance-Optimized
```java
ObfuscationConfig config = ConfigPresets.createPerformanceOptimizedObfuscation()
    .mainClass("com/example/Main")
    .maxThreads(Runtime.getRuntime().availableProcessors())
    .optimizeCode(true)
    .keepMainClass()
    .build();
```

### Different Naming Modes
```java
// Sequential with prefix (default)
ObfuscationConfig config1 = new ObfuscationConfig.Builder()
    .namingMode(NamingMode.SEQUENTIAL_PREFIX)
    .renameClasses(true)
    .build();

// Random short names for good obfuscation
ObfuscationConfig config2 = new ObfuscationConfig.Builder()
    .namingMode(NamingMode.RANDOM_SHORT)
    .renameClasses(true)
    .build();

// Single character for minimal size
ObfuscationConfig config3 = new ObfuscationConfig.Builder()
    .namingMode(NamingMode.SINGLE_CHAR)
    .renameClasses(true)
    .build();
```

### Framework-Specific Configurations

#### Spring Framework
```java
ObfuscationConfig config = new ObfuscationConfig.Builder()
    .renameClasses(true)
    .renameFields(true)
    .renameMethods(true)
    .namingMode(NamingMode.RANDOM_SHORT)
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
    .namingMode(NamingMode.SEQUENTIAL_ALPHA)
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

### Version 2.0.0 - Enhanced Configuration System
#### 🎚️ Obfuscation Levels
- Added pre-defined security levels (MINIMAL, BASIC, AGGRESSIVE, EXTREME, CUSTOM)
- Created preset configurations for common use cases
- Quick level selection via CLI and programmatic API

#### 📦 Package Filtering
- Include/exclude package filtering with hierarchical matching
- Fine-grained control over which packages to process
- Validation and conflict detection for package rules

#### ⚡ Performance & Threading
- Multi-threaded processing with configurable thread counts
- Sequential vs parallel transformer processing modes
- Performance optimization settings and recommendations
- Automatic backup system with configurable directories

#### 🔧 Enhanced Configuration
- Nested JSON configuration structure for better organization
- Custom settings support for extensibility
- String compression with deflate/base64 encoding
- Member shuffling and code optimization options
- Advanced debugging preservation options

#### ✅ Improved Validation
- Comprehensive configuration validation with detailed feedback
- Real-time validation during configuration building
- Best practice recommendations and security warnings
- Package name and class name validation

#### 🛠️ New Features
- String compression using deflate/base64 encoding
- Anti-debugging protection with configurable responses
- Line number and local variable preservation options
- Obfuscation resistance scoring improvements
- Enhanced CLI with categorized options
- Builder pattern validation before configuration creation

#### 📄 Documentation
- Comprehensive README with all new features
- Enhanced configuration examples
- Performance tuning guidelines
- Troubleshooting section improvements

### Version 1.1.0
- Added multiple naming modes (SEQUENTIAL_PREFIX, SEQUENTIAL_ALPHA, RANDOM_SHORT, RANDOM_LONG, SINGLE_CHAR)
- Enhanced CLI with naming mode selection
- Updated configuration file format to support naming modes
- Improved local variable renaming with configurable naming modes

### Version 1.0.0
- Initial release with class, method, and field renaming
- Advanced keep rules system
- Configuration presets
- Comprehensive validation
- Mapping generation
- Extensible transformer architecture