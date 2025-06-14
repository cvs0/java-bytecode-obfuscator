package net.cvs0.config;

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
}