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
        
        if (inputFile.length() == 0) {
            throw new IllegalArgumentException("Input file is empty: " + inputFile.getAbsolutePath());
        }
        
        if (inputFile.length() > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Input file is too large (> 2GB): " + inputFile.getAbsolutePath());
        }
        
        try {
            if (!inputFile.getCanonicalPath().equals(inputFile.getAbsolutePath())) {
                Logger.warning("Input file path contains symbolic links: " + inputFile.getAbsolutePath());
            }
        } catch (java.io.IOException e) {
            Logger.warning("Could not resolve canonical path for input file: " + e.getMessage());
        }
        
        try {
            java.util.zip.ZipFile testZip = new java.util.zip.ZipFile(inputFile);
            testZip.close();
        } catch (java.util.zip.ZipException e) {
            throw new IllegalArgumentException("Input file is not a valid ZIP/JAR archive: " + inputFile.getAbsolutePath() + " - " + e.getMessage());
        } catch (java.io.IOException e) {
            throw new IllegalArgumentException("Cannot read input file as ZIP/JAR: " + inputFile.getAbsolutePath() + " - " + e.getMessage());
        }
        
        if (isFileOnNetworkDrive(inputFile)) {
            Logger.warning("Input file is on a network drive, processing may be slower: " + inputFile.getAbsolutePath());
        }
    }
    
    public static void validateOutputFile(File outputFile)
    {
        if (outputFile == null) {
            throw new IllegalArgumentException("Output file cannot be null");
        }
        
        if (outputFile.exists() && outputFile.isDirectory()) {
            throw new IllegalArgumentException("Output path is a directory, not a file: " + outputFile.getAbsolutePath());
        }
        
        if (outputFile.exists() && !outputFile.canWrite()) {
            throw new IllegalArgumentException("Cannot write to output file: " + outputFile.getAbsolutePath());
        }
        
        File parentDir = outputFile.getParentFile();
        if (parentDir != null) {
            if (!parentDir.exists()) {
                if (!parentDir.mkdirs()) {
                    throw new IllegalArgumentException("Cannot create output directory: " + parentDir.getAbsolutePath());
                }
            } else if (!parentDir.canWrite()) {
                throw new IllegalArgumentException("Cannot write to output directory: " + parentDir.getAbsolutePath());
            }
        }
        
        if (!outputFile.getName().toLowerCase().endsWith(".jar")) {
            throw new IllegalArgumentException("Output file must be a JAR file: " + outputFile.getAbsolutePath());
        }
        
        String outputName = outputFile.getName();
        if (outputName.contains("\\") || outputName.contains("/") || outputName.contains("..")) {
            throw new IllegalArgumentException("Output filename contains invalid characters: " + outputName);
        }
        
        if (outputName.length() > 255) {
            throw new IllegalArgumentException("Output filename is too long (> 255 characters): " + outputName);
        }
        
        if (containsControlCharacters(outputName)) {
            throw new IllegalArgumentException("Output filename contains control characters: " + outputName);
        }
        
        try {
            long freeSpace = outputFile.getParentFile() != null ? 
                outputFile.getParentFile().getFreeSpace() : 
                new File(".").getFreeSpace();
            if (freeSpace < 1024 * 1024) {
                Logger.warning("Low disk space available (< 1MB) at output location: " + outputFile.getParent());
            }
        } catch (SecurityException e) {
            Logger.warning("Cannot check disk space for output location: " + e.getMessage());
        }
        
        if (isFileOnNetworkDrive(outputFile)) {
            Logger.warning("Output file is on a network drive, writing may be slower: " + outputFile.getAbsolutePath());
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
    
    private static boolean isFileOnNetworkDrive(File file) {
        try {
            String path = file.getAbsolutePath();
            return path.startsWith("\\\\") || 
                   (path.length() > 1 && path.charAt(1) == ':' && 
                    java.nio.file.Files.getFileStore(file.toPath()).type().toLowerCase().contains("network"));
        } catch (Exception e) {
            return false;
        }
    }
    
    private static boolean containsControlCharacters(String str) {
        return str.chars().anyMatch(ch -> ch < 32 && ch != 9 && ch != 10 && ch != 13);
    }
    
    public static void validateFileSystemCapacity(File inputFile, File outputFile) {
        if (inputFile == null || outputFile == null) {
            return;
        }
        
        try {
            long inputSize = inputFile.length();
            long outputFreeSpace = outputFile.getParentFile() != null ? 
                outputFile.getParentFile().getFreeSpace() : 
                new File(".").getFreeSpace();
            
            long estimatedOutputSize = (long)(inputSize * 1.5);
            
            if (outputFreeSpace < estimatedOutputSize) {
                throw new IllegalArgumentException(
                    String.format("Insufficient disk space. Required: %d MB, Available: %d MB", 
                        estimatedOutputSize / (1024 * 1024), 
                        outputFreeSpace / (1024 * 1024)));
            }
        } catch (SecurityException e) {
            Logger.warning("Cannot check disk space: " + e.getMessage());
        }
    }
}