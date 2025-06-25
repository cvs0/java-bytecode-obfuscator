package net.cvs0;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import net.cvs0.config.*;
import net.cvs0.core.Obfuscator;
import net.cvs0.utils.Logger;

import java.io.File;
import java.util.List;
import java.util.concurrent.Callable;

@Command(name = "obfuscator", mixinStandardHelpOptions = true, version = "1.0.0",
        description = "Java Bytecode Obfuscator - Obfuscate Java JAR files with advanced keep rules")
public class Main implements Callable<Integer>
{
    @Parameters(index = "0", description = "Input JAR file to obfuscate")
    private File inputJar;

    @Parameters(index = "1", description = "Output JAR file")
    private File outputJar;

    @Option(names = {"-c", "--config"}, description = "Configuration file (JSON)")
    private File configFile;

    @Option(names = {"-m", "--main-class"}, description = "Main class name")
    private String mainClass;

    @Option(names = {"--rename-classes"}, description = "Enable class renaming")
    private Boolean renameClasses;

    @Option(names = {"--rename-fields"}, description = "Enable field renaming")
    private Boolean renameFields;

    @Option(names = {"--rename-methods"}, description = "Enable method renaming")
    private Boolean renameMethods;



    @Option(names = {"--add-synthetic-members"}, description = "Add synthetic fields and methods")
    private Boolean addSyntheticMembers;

    @Option(names = {"-v", "--verbose"}, description = "Enable verbose output")
    private boolean verbose;

    @Option(names = {"--keep-class"}, description = "Keep specific class (can be used multiple times)")
    private List<String> keepClasses;

    @Option(names = {"--keep-class-pattern"}, description = "Keep classes matching pattern (can be used multiple times)")
    private List<String> keepClassPatterns;

    @Option(names = {"--keep-main-class"}, description = "Keep the main class")
    private boolean keepMainClass;

    @Option(names = {"--keep-entry-points"}, description = "Keep standard entry points (main methods, constructors)")
    private boolean keepStandardEntryPoints;

    @Option(names = {"--naming-mode", "-n"}, 
            description = "Name generation mode (default: SEQUENTIAL_PREFIX):%n" +
                         "  SEQUENTIAL_PREFIX - Sequential with prefix (a1, a2, a3...)%n" +
                         "  SEQUENTIAL_ALPHA - Sequential alphabetic (a, b, c... aa, ab, ac...)%n" +
                         "  RANDOM_SHORT - Random short names (abcd, xyzk, mnop...)%n" +
                         "  RANDOM_LONG - Random long names (highly obfuscated)%n" +
                         "  SINGLE_CHAR - Single character names (a, b, c...)")
    private NamingMode namingMode;

    @Option(names = {"--sequential-transformers"}, description = "Run transformers sequentially - each transformer processes all classes before the next starts (disabled by default)")
    private Boolean sequentialTransformers;

    @Option(names = {"--include-package"}, description = "Include specific package for obfuscation (can be used multiple times, e.g., com.example, org.myapp)")
    private List<String> includePackages;

    @Option(names = {"--stay-in-scope"}, description = "Only obfuscate classes within the same package scope as the main class (first two parts, e.g., net.cvs0.Main -> net.cvs0)")
    private boolean stayInScope;

    public static void main(String[] args)
    {
        try {
            setupShutdownHook();
            checkSystemRequirements();
            
            CommandLine commandLine = new CommandLine(new Main());
            commandLine.setExecutionExceptionHandler(new CustomExceptionHandler());
            
            int exitCode = commandLine.execute(args);
            System.exit(exitCode);
            
        } catch (OutOfMemoryError e) {
            System.err.println("FATAL: Out of memory. Please increase heap size with -Xmx parameter.");
            System.err.println("Current max heap: " + (Runtime.getRuntime().maxMemory() / 1024 / 1024) + "MB");
            System.exit(3);
        } catch (SecurityException e) {
            System.err.println("FATAL: Security restriction: " + e.getMessage());
            System.exit(4);
        } catch (Throwable e) {
            System.err.println("FATAL: Unexpected error: " + e.getMessage());
            e.printStackTrace();
            System.exit(2);
        }
    }
    
    private static void setupShutdownHook()
    {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            Logger.info("Obfuscator shutting down...");
        }));
    }
    
    private static void checkSystemRequirements()
    {
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory();
        
        if (maxMemory < 64 * 1024 * 1024) {
            System.err.println("WARNING: Very low heap memory (< 64MB). Consider increasing with -Xmx parameter.");
        }
        
        String javaVersion = System.getProperty("java.version");
        if (javaVersion.startsWith("1.7") || javaVersion.startsWith("1.6")) {
            System.err.println("WARNING: Java version " + javaVersion + " is very old. Java 8+ is recommended.");
        }
    }
    
    private static class CustomExceptionHandler implements CommandLine.IExecutionExceptionHandler
    {
        @Override
        public int handleExecutionException(Exception ex, CommandLine commandLine, CommandLine.ParseResult parseResult)
        {
            if (ex instanceof java.nio.file.NoSuchFileException) {
                System.err.println("Error: File not found: " + ex.getMessage());
                return 1;
            } else if (ex instanceof java.nio.file.AccessDeniedException) {
                System.err.println("Error: Access denied: " + ex.getMessage());
                return 1;
            } else if (ex instanceof java.io.IOException) {
                System.err.println("Error: I/O error: " + ex.getMessage());
                return 1;
            } else if (ex instanceof IllegalArgumentException) {
                System.err.println("Error: Invalid argument: " + ex.getMessage());
                return 1;
            } else {
                System.err.println("Error: " + ex.getMessage());
                
                Object verbose = parseResult.matchedOptionValue("-v", false);
                if (Boolean.TRUE.equals(verbose)) {
                    ex.printStackTrace();
                }
                return 1;
            }
        }
    }

    @Override
    public Integer call()
    {
        long startTime = System.currentTimeMillis();
        
        try {
            validateInputs();
            
            ObfuscationConfig config = buildConfiguration();
            validateConfiguration(config);
            
            if (verbose) {
                printHeader(config);
            }

            createBackupIfNeeded(config);
            
            Obfuscator obfuscator = new Obfuscator();
            obfuscator.obfuscate(inputJar, outputJar, config);
            
            long duration = System.currentTimeMillis() - startTime;
            Logger.success("Obfuscation completed successfully in " + duration + "ms");

            return 0;
            
        } catch (java.nio.file.NoSuchFileException e) {
            System.err.println("Error: File not found: " + e.getMessage());
            return 1;
        } catch (java.nio.file.AccessDeniedException e) {
            System.err.println("Error: Access denied: " + e.getMessage()); 
            return 1;
        } catch (java.io.IOException e) {
            System.err.println("Error: I/O error - " + e.getMessage());
            if (verbose) {
                e.printStackTrace();
            }
            return 1;
        } catch (IllegalArgumentException e) {
            System.err.println("Error: Invalid configuration - " + e.getMessage());
            return 1;
        } catch (OutOfMemoryError e) {
            System.err.println("Error: Out of memory. Current heap: " + 
                (Runtime.getRuntime().maxMemory() / 1024 / 1024) + "MB. " +
                "Try increasing with -Xmx parameter.");
            return 3;
        } catch (SecurityException e) {
            System.err.println("Error: Security restriction - " + e.getMessage());
            return 4;
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            if (verbose) {
                e.printStackTrace();
            }
            return 1;
        }
    }
    
    private void validateInputs() throws Exception
    {
        if (inputJar == null) {
            throw new IllegalArgumentException("Input JAR file must be specified");
        }
        
        if (outputJar == null) {
            throw new IllegalArgumentException("Output JAR file must be specified");
        }
        
        if (!inputJar.exists()) {
            throw new java.nio.file.NoSuchFileException("Input JAR file does not exist: " + inputJar.getAbsolutePath());
        }
        
        if (!inputJar.canRead()) {
            throw new java.nio.file.AccessDeniedException("Cannot read input JAR file: " + inputJar.getAbsolutePath());
        }
        
        if (inputJar.isDirectory()) {
            throw new IllegalArgumentException("Input path is a directory, not a file: " + inputJar.getAbsolutePath());
        }
        
        if (inputJar.equals(outputJar)) {
            throw new IllegalArgumentException("Input and output JAR files cannot be the same");
        }
        
        if (outputJar.exists() && !outputJar.canWrite()) {
            throw new java.nio.file.AccessDeniedException("Cannot write to output JAR file: " + outputJar.getAbsolutePath());
        }
        
        if (configFile != null && !configFile.exists()) {
            throw new java.nio.file.NoSuchFileException("Configuration file does not exist: " + configFile.getAbsolutePath());
        }
    }
    
    private void validateConfiguration(ObfuscationConfig config) throws Exception
    {
        if (config == null) {
            throw new IllegalArgumentException("Configuration cannot be null");
        }
        
        net.cvs0.config.ConfigValidator.ValidationResult result = 
            net.cvs0.config.ConfigValidator.validate(config);
        
        if (result.hasErrors()) {
            StringBuilder sb = new StringBuilder("Configuration validation failed:");
            for (String error : result.getErrors()) {
                sb.append("\n  - ").append(error);
            }
            throw new IllegalArgumentException(sb.toString());
        }
        
        if (result.hasWarnings() && verbose) {
            System.out.println("Configuration warnings:");
            for (String warning : result.getWarnings()) {
                System.out.println("  - " + warning);
            }
            System.out.println();
        }
    }
    
    private void printHeader(ObfuscationConfig config)
    {
        System.out.println("Java Bytecode Obfuscator v1.0.0");
        System.out.println("Input JAR: " + inputJar.getAbsolutePath() + " (" + (inputJar.length() / 1024) + " KB)");
        System.out.println("Output JAR: " + outputJar.getAbsolutePath());
        System.out.println("Naming mode: " + config.getNamingMode().name() + " - " + config.getNamingMode().getDescription());
        System.out.println("Obfuscation level: " + config.getObfuscationLevel().name());
        System.out.println("Max threads: " + config.getMaxThreads());
        
        Runtime runtime = Runtime.getRuntime();
        System.out.println("Available memory: " + (runtime.maxMemory() / 1024 / 1024) + "MB");
        System.out.println();
    }
    
    private void createBackupIfNeeded(ObfuscationConfig config) throws Exception
    {
        if (config.isEnableBackup() && config.getBackupDir() != null) {
            File backupDir = new File(config.getBackupDir());
            if (!backupDir.exists() && !backupDir.mkdirs()) {
                throw new java.io.IOException("Cannot create backup directory: " + backupDir.getAbsolutePath());
            }
            
            String timestamp = java.time.LocalDateTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            File backupFile = new File(backupDir, "backup_" + timestamp + "_" + inputJar.getName());
            
            java.nio.file.Files.copy(inputJar.toPath(), backupFile.toPath());
            Logger.info("Created backup: " + backupFile.getAbsolutePath());
        }
    }
    
    private ObfuscationConfig buildConfiguration() throws Exception
    {
        ObfuscationConfig.Builder builder;
        
        if (configFile != null) {
            if (!configFile.exists()) {
                throw new IllegalArgumentException("Configuration file does not exist: " + configFile.getAbsolutePath());
            }
            
            ConfigLoader loader = new ConfigLoader();
            builder = loader.loadConfig(configFile);
            
            if (verbose) {
                System.out.println("Loaded configuration from: " + configFile.getAbsolutePath());
            }
        } else {
            builder = new ObfuscationConfig.Builder();
        }
        
        if (mainClass != null) {
            String normalizedMainClass = mainClass.replace('.', '/');
            if (!normalizedMainClass.equals(mainClass)) {
                if (verbose) {
                    System.out.println("Normalized main class from '" + mainClass + "' to '" + normalizedMainClass + "'");
                }
            }
            builder.mainClass(normalizedMainClass);
        }
        
        if (renameClasses != null) {
            builder.renameClasses(renameClasses);
        }
        
        if (renameFields != null) {
            builder.renameFields(renameFields);
        }
        
        if (renameMethods != null) {
            builder.renameMethods(renameMethods);
        }
        

        
        if (addSyntheticMembers != null) {
            builder.addSyntheticMembers(addSyntheticMembers);
        }
        
        builder.verbose(verbose);
        
        if (keepClasses != null) {
            for (String className : keepClasses) {
                String normalizedClassName = className.replace('.', '/');
                builder.keepClass(normalizedClassName);
            }
        }
        
        if (keepClassPatterns != null) {
            for (String pattern : keepClassPatterns) {
                builder.keepClassPattern(pattern);
            }
        }
        
        if (keepMainClass) {
            builder.keepMainClass(keepMainClass);
        }
        
        if (keepStandardEntryPoints) {
            builder.keepStandardEntryPoints(keepStandardEntryPoints);
        }
        
        if (namingMode != null) {
            builder.namingMode(namingMode);
        }
        
        if (sequentialTransformers != null) {
            builder.sequentialTransformers(sequentialTransformers);
        }
        
        if (includePackages != null && !includePackages.isEmpty()) {
            for (String packageName : includePackages) {
                String normalizedPackage = packageName.replace('.', '/');
                builder.includePackage(normalizedPackage);
                if (verbose) {
                    System.out.println("Including package for obfuscation: " + packageName + " (" + normalizedPackage + ")");
                }
            }
        }
        
        builder.stayInScope(stayInScope);
        
        if (configFile == null && renameClasses == null && renameFields == null && renameMethods == null) {
            builder.renameClasses(true)
                   .renameFields(true)  
                   .renameMethods(true);
        }
        
        ObfuscationConfig config = builder.build();
        return config;
    }
}