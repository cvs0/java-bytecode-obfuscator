package net.cvs0;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import net.cvs0.config.ObfuscationConfig;
import net.cvs0.config.ConfigLoader;
import net.cvs0.config.NamingMode;
import net.cvs0.mappings.export.MappingExporter;
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

    @Option(names = {"--rename-local-variables"}, description = "Enable local variable renaming")
    private Boolean renameLocalVariables;

    @Option(names = {"--obfuscate-conditions"}, description = "Enable condition obfuscation (transforms true/false into complex expressions)")
    private Boolean obfuscateConditions;

    @Option(names = {"--mappings", "--output-mappings"}, description = "Output mappings file")
    private File mappingsFile;

    @Option(names = {"--mapping-format"}, 
            description = "Mapping output format (default: auto-detect from file extension):%n" +
                         "  PROGUARD - ProGuard mapping format%n" +
                         "  SRG - SRG (Mod Coder Pack) format%n" +
                         "  TINY - Tiny mapping format%n" +
                         "  JSON - JSON format%n" +
                         "  CSV - CSV format%n" +
                         "  HUMAN_READABLE - Human readable format%n" +
                         "  RETRACE - Retrace format%n" +
                         "  ALL - Export all formats")
    private String mappingFormat;

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

    public static void main(String[] args)
    {
        int exitCode = new CommandLine(new Main()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() throws Exception
    {
        try {
            if (!inputJar.exists()) {
                System.err.println("Error: Input JAR file does not exist: " + inputJar);
                return 1;
            }

            ObfuscationConfig config = buildConfiguration();
            
            if (verbose) {
                System.out.println("Java Bytecode Obfuscator v1.0.0");
                System.out.println("Input JAR: " + inputJar.getAbsolutePath());
                System.out.println("Output JAR: " + outputJar.getAbsolutePath());
                if (mappingsFile != null) {
                    System.out.println("Mappings file: " + mappingsFile.getAbsolutePath());
                }
                System.out.println("Naming mode: " + config.getNamingMode().name() + " - " + config.getNamingMode().getDescription());
                System.out.println();
            }

            MappingExporter.MappingFormat format = parseMappingFormat();
            
            Obfuscator obfuscator = new Obfuscator();
            obfuscator.obfuscate(inputJar, outputJar, config, mappingsFile, format);

            return 0;
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            if (verbose) {
                e.printStackTrace();
            }
            return 1;
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
        
        if (renameLocalVariables != null) {
            builder.renameLocalVariables(renameLocalVariables);
        }
        
        if (obfuscateConditions != null) {
            builder.obfuscateConditions(obfuscateConditions);
        }
        
        if (verbose) {
            builder.verbose(true);
        }
        
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
            builder.keepMainClass();
        }
        
        if (keepStandardEntryPoints) {
            builder.keepStandardEntryPoints();
        }
        
        if (namingMode != null) {
            builder.namingMode(namingMode);
        }
        
        if (configFile == null && renameClasses == null && renameFields == null && renameMethods == null && renameLocalVariables == null) {
            builder.renameClasses(true)
                   .renameFields(true)  
                   .renameMethods(true)
                   .renameLocalVariables(true);
        }
        
        return builder.build();
    }
    
    private MappingExporter.MappingFormat parseMappingFormat()
    {
        if (mappingFormat == null || mappingFormat.equalsIgnoreCase("AUTO")) {
            return null;
        }
        
        try {
            return MappingExporter.MappingFormat.valueOf(mappingFormat.toUpperCase());
        } catch (IllegalArgumentException e) {
            System.err.println("Warning: Unknown mapping format '" + mappingFormat + "', using auto-detection");
            return null;
        }
    }
}