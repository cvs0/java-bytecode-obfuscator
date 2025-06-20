package net.cvs0.config;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class ConfigValidator
{
    public static class ValidationResult
    {
        private final boolean valid;
        private final List<String> warnings;
        private final List<String> errors;
        
        public ValidationResult(boolean valid, List<String> warnings, List<String> errors)
        {
            this.valid = valid;
            this.warnings = warnings;
            this.errors = errors;
        }
        
        public boolean isValid()
        {
            return valid;
        }
        
        public List<String> getWarnings()
        {
            return warnings;
        }
        
        public List<String> getErrors()
        {
            return errors;
        }
        
        public boolean hasWarnings()
        {
            return !warnings.isEmpty();
        }
        
        public boolean hasErrors()
        {
            return !errors.isEmpty();
        }
    }
    
    public static ValidationResult validate(ObfuscationConfig config)
    {
        List<String> warnings = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        
        validateBasicConfig(config, warnings, errors);
        validateKeepRules(config.getKeepRules(), warnings, errors);
        validateMainClass(config, warnings, errors);
        validateAdvancedSettings(config, warnings, errors);
        validatePackageScope(config, warnings, errors);
        validatePerformanceSettings(config, warnings, errors);
        
        return new ValidationResult(errors.isEmpty(), warnings, errors);
    }
    
    public static ValidationResult validateBuilder(ObfuscationConfig.Builder builder)
    {
        List<String> warnings = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        
        return new ValidationResult(errors.isEmpty(), warnings, errors);
    }
    
    private static void validateBasicConfig(ObfuscationConfig config, List<String> warnings, List<String> errors)
    {
        if (!config.isRenameClasses() && !config.isRenameFields() && !config.isRenameMethods()) {
            errors.add("At least one obfuscation option must be enabled");
        }
        
        if (config.isRenameClasses() && config.getKeepRules().getKeepClasses().isEmpty()) {
            warnings.add("Class renaming is enabled but no classes are kept - this might break reflection");
        }
    }
    
    private static void validateKeepRules(ObfuscationConfig.KeepRules keepRules, List<String> warnings, List<String> errors)
    {
        for (String className : keepRules.getKeepClasses()) {
            if (!isValidClassName(className)) {
                warnings.add("Potentially invalid class name in keep rules: " + className);
            }
        }
        
        for (String className : keepRules.getKeepClassMethods().keySet()) {
            if (!isValidClassName(className)) {
                warnings.add("Potentially invalid class name in keep method rules: " + className);
            }
        }
        
        for (String className : keepRules.getKeepClassFields().keySet()) {
            if (!isValidClassName(className)) {
                warnings.add("Potentially invalid class name in keep field rules: " + className);
            }
        }
    }
    
    private static void validateMainClass(ObfuscationConfig config, List<String> warnings, List<String> errors)
    {
        String mainClass = config.getMainClass();
        if (mainClass != null) {
            if (!isValidClassName(mainClass)) {
                warnings.add("Main class name appears to be invalid: " + mainClass);
            }
            
            if (config.isRenameClasses() && !config.shouldKeepClass(mainClass)) {
                warnings.add("Main class is not kept but class renaming is enabled - this will likely break execution");
            }
            
            if (config.isRenameMethods() && !config.shouldKeepMethod(mainClass, "main", "([Ljava/lang/String;)V")) {
                warnings.add("Main method is not kept but method renaming is enabled - this will likely break execution");
            }
        }
    }
    
    private static boolean isValidClassName(String className)
    {
        if (className == null || className.isEmpty()) {
            return false;
        }
        
        if (className.contains("*") || className.contains("?")) {
            try {
                Pattern.compile(className);
                return true;
            } catch (PatternSyntaxException e) {
                return false;
            }
        }
        
        String[] parts = className.split("/");
        for (String part : parts) {
            if (part.isEmpty()) {
                return false;
            }
            
            if (!Character.isJavaIdentifierStart(part.charAt(0))) {
                return false;
            }
            
            for (int i = 1; i < part.length(); i++) {
                if (!Character.isJavaIdentifierPart(part.charAt(i))) {
                    return false;
                }
            }
        }
        
        return true;
    }
    
    private static void validateAdvancedSettings(ObfuscationConfig config, List<String> warnings, List<String> errors)
    {
        if (config.isAntiDebugging() && config.isPreserveLineNumbers()) {
            warnings.add("Anti-debugging is enabled but line numbers are preserved - this may reduce security");
        }
        
        if (config.isCompressStrings() && !config.isRenameFields()) {
            warnings.add("String compression is enabled but field renaming is disabled - effectiveness may be limited");
        }
        
        if (config.getObfuscationLevel() == ObfuscationConfig.ObfuscationLevel.EXTREME && config.isPreserveLocalVariableNames()) {
            warnings.add("Extreme obfuscation level selected but local variable names are preserved");
        }
    }
    
    private static void validatePackageScope(ObfuscationConfig config, List<String> warnings, List<String> errors)
    {
        List<String> excludePackages = config.getExcludePackages();
        List<String> includePackages = config.getIncludePackages();
        
        if (!excludePackages.isEmpty() && !includePackages.isEmpty()) {
            warnings.add("Both include and exclude packages are specified - exclude packages take precedence");
        }
        
        for (String pkg : excludePackages) {
            if (!isValidPackageName(pkg)) {
                errors.add("Invalid exclude package name: " + pkg);
            }
        }
        
        for (String pkg : includePackages) {
            if (!isValidPackageName(pkg)) {
                errors.add("Invalid include package name: " + pkg);
            }
        }
        
        for (String exclude : excludePackages) {
            for (String include : includePackages) {
                if (include.startsWith(exclude)) {
                    warnings.add("Include package '" + include + "' is within excluded package '" + exclude + "'");
                }
            }
        }
    }
    
    private static void validatePerformanceSettings(ObfuscationConfig config, List<String> warnings, List<String> errors)
    {
        if (config.getMaxThreads() < 1) {
            errors.add("Max threads must be at least 1");
        } else if (config.getMaxThreads() > Runtime.getRuntime().availableProcessors() * 2) {
            warnings.add("Max threads (" + config.getMaxThreads() + ") is higher than recommended for this system");
        }
        
        if (config.isEnableBackup()) {
            String backupDir = config.getBackupDir();
            if (backupDir == null || backupDir.trim().isEmpty()) {
                errors.add("Backup is enabled but no backup directory specified");
            } else {
                File dir = new File(backupDir);
                if (!dir.exists() && !dir.mkdirs()) {
                    warnings.add("Backup directory does not exist and cannot be created: " + backupDir);
                } else if (!dir.canWrite()) {
                    errors.add("Cannot write to backup directory: " + backupDir);
                }
            }
        }
        
        if (config.isSequentialTransformers() && config.getMaxThreads() > 1) {
            warnings.add("Sequential transformers enabled but max threads > 1 - threading will be limited");
        }
    }
    
    private static boolean isValidPackageName(String packageName)
    {
        if (packageName == null || packageName.isEmpty()) {
            return false;
        }
        
        if (packageName.startsWith(".") || packageName.endsWith(".") || packageName.contains("..")) {
            return false;
        }
        
        String[] parts = packageName.split("\\.");
        for (String part : parts) {
            if (part.isEmpty() || !Character.isJavaIdentifierStart(part.charAt(0))) {
                return false;
            }
            for (int i = 1; i < part.length(); i++) {
                if (!Character.isJavaIdentifierPart(part.charAt(i))) {
                    return false;
                }
            }
        }
        
        return true;
    }
}