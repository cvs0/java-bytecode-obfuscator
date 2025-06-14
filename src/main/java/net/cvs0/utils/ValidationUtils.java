package net.cvs0.utils;

import net.cvs0.config.ObfuscationConfig;

import java.io.File;
import java.util.Set;

public class ValidationUtils
{
    public static void validateInputFile(File inputFile)
    {
        if (inputFile == null) {
            throw new IllegalArgumentException("Input file cannot be null");
        }
        
        if (!inputFile.exists()) {
            throw new IllegalArgumentException("Input file does not exist: " + inputFile.getAbsolutePath());
        }
        
        if (!inputFile.isFile()) {
            throw new IllegalArgumentException("Input path is not a file: " + inputFile.getAbsolutePath());
        }
        
        if (!inputFile.getName().toLowerCase().endsWith(".jar")) {
            throw new IllegalArgumentException("Input file must be a JAR file: " + inputFile.getAbsolutePath());
        }
        
        if (!inputFile.canRead()) {
            throw new IllegalArgumentException("Cannot read input file: " + inputFile.getAbsolutePath());
        }
    }
    
    public static void validateOutputFile(File outputFile)
    {
        if (outputFile == null) {
            throw new IllegalArgumentException("Output file cannot be null");
        }
        
        if (outputFile.exists() && !outputFile.canWrite()) {
            throw new IllegalArgumentException("Cannot write to output file: " + outputFile.getAbsolutePath());
        }
        
        File parentDir = outputFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            if (!parentDir.mkdirs()) {
                throw new IllegalArgumentException("Cannot create output directory: " + parentDir.getAbsolutePath());
            }
        }
        
        if (!outputFile.getName().toLowerCase().endsWith(".jar")) {
            throw new IllegalArgumentException("Output file must be a JAR file: " + outputFile.getAbsolutePath());
        }
    }
    
    public static void validateConfig(ObfuscationConfig config)
    {
        if (config == null) {
            throw new IllegalArgumentException("Obfuscation config cannot be null");
        }
        
        if (config.getKeepRules() == null) {
            throw new IllegalArgumentException("Keep rules cannot be null");
        }
        
        net.cvs0.config.ConfigValidator.ValidationResult result = net.cvs0.config.ConfigValidator.validate(config);
        
        if (result.hasErrors()) {
            StringBuilder sb = new StringBuilder("Configuration validation failed:");
            for (String error : result.getErrors()) {
                sb.append("\n  - ").append(error);
            }
            throw new IllegalArgumentException(sb.toString());
        }
        
        if (result.hasWarnings()) {
            System.out.println("Configuration warnings:");
            for (String warning : result.getWarnings()) {
                System.out.println("  - " + warning);
            }
        }
    }
    
    public static void validateMappingsFile(File mappingsFile)
    {
        if (mappingsFile == null) {
            return;
        }
        
        File parentDir = mappingsFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            if (!parentDir.mkdirs()) {
                throw new IllegalArgumentException("Cannot create mappings directory: " + parentDir.getAbsolutePath());
            }
        }
        
        if (mappingsFile.exists() && !mappingsFile.canWrite()) {
            throw new IllegalArgumentException("Cannot write to mappings file: " + mappingsFile.getAbsolutePath());
        }
    }
    
    public static boolean isValidJavaIdentifier(String name)
    {
        if (name == null || name.isEmpty()) {
            return false;
        }
        
        if (!Character.isJavaIdentifierStart(name.charAt(0))) {
            return false;
        }
        
        for (int i = 1; i < name.length(); i++) {
            if (!Character.isJavaIdentifierPart(name.charAt(i))) {
                return false;
            }
        }
        
        return true;
    }
    
    public static boolean isReservedKeyword(String name)
    {
        Set<String> keywords = Set.of(
            "abstract", "assert", "boolean", "break", "byte", "case", "catch", "char", "class", "const",
            "continue", "default", "do", "double", "else", "enum", "extends", "final", "finally", "float",
            "for", "goto", "if", "implements", "import", "instanceof", "int", "interface", "long", "native",
            "new", "package", "private", "protected", "public", "return", "short", "static", "strictfp",
            "super", "switch", "synchronized", "this", "throw", "throws", "transient", "try", "void",
            "volatile", "while", "true", "false", "null"
        );
        
        return keywords.contains(name);
    }
}