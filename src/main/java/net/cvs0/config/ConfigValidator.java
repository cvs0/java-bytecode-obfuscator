package net.cvs0.config;

import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class ConfigValidator 
{
    public static ValidationResult validate(ObfuscationConfig config) 
    {
        ValidationResult result = new ValidationResult();
        
        if (config == null) {
            result.addError("Configuration cannot be null");
            return result;
        }

        if (config.getMaxThreads() <= 0) {
            result.addError("Maximum threads must be greater than 0");
        }
        
        if (config.getMaxThreads() > Runtime.getRuntime().availableProcessors() * 2) {
            result.addWarning("Maximum threads (" + config.getMaxThreads() + ") is higher than recommended (" + 
                             (Runtime.getRuntime().availableProcessors() * 2) + ")");
        }

        if (!config.isRenameClasses() && !config.isRenameFields() && !config.isRenameMethods()) {
            result.addWarning("No renaming options enabled - obfuscation will be minimal");
        }

        if (config.getKeepClasses().isEmpty() && config.getKeepClassPatterns().isEmpty() && 
            !config.isKeepMainClass() && !config.isKeepStandardEntryPoints()) {
            result.addWarning("No keep rules specified - this may break the application");
        }

        if (config.getMainClass() != null && config.getMainClass().trim().isEmpty()) {
            result.addError("Main class cannot be empty");
        }

        if (config.getBackupDir() != null && config.getBackupDir().trim().isEmpty()) {
            result.addError("Backup directory cannot be empty");
        }

        for (String className : config.getKeepClasses()) {
            if (className == null || className.trim().isEmpty()) {
                result.addError("Keep class name cannot be null or empty");
            }
        }

        for (String pattern : config.getKeepClassPatterns()) {
            if (pattern == null || pattern.trim().isEmpty()) {
                result.addError("Keep class pattern cannot be null or empty");
            } else {
                try {
                    Pattern.compile(pattern);
                } catch (PatternSyntaxException e) {
                    result.addError("Invalid regex pattern '" + pattern + "': " + e.getMessage());
                }
            }
        }

        for (String packageName : config.getIncludePackages()) {
            if (packageName == null || packageName.trim().isEmpty()) {
                result.addError("Include package name cannot be null or empty");
            }
        }

        for (String packageName : config.getExcludePackages()) {
            if (packageName == null || packageName.trim().isEmpty()) {
                result.addError("Exclude package name cannot be null or empty");
            }
        }

        Set<String> intersection = new HashSet<>(config.getIncludePackages());
        intersection.retainAll(config.getExcludePackages());
        if (!intersection.isEmpty()) {
            result.addError("Packages cannot be both included and excluded: " + intersection);
        }

        if (config.isObfuscateControlFlow() && config.getObfuscationLevel() == ObfuscationLevel.LIGHT) {
            result.addWarning("Control flow obfuscation is enabled but obfuscation level is LIGHT");
        }

        return result;
    }

    public static class ValidationResult 
    {
        private final List<String> errors = new ArrayList<>();
        private final List<String> warnings = new ArrayList<>();

        public void addError(String error) 
        {
            errors.add(error);
        }

        public void addWarning(String warning) 
        {
            warnings.add(warning);
        }

        public boolean hasErrors() 
        {
            return !errors.isEmpty();
        }

        public boolean hasWarnings() 
        {
            return !warnings.isEmpty();
        }

        public List<String> getErrors() 
        {
            return new ArrayList<>(errors);
        }

        public List<String> getWarnings() 
        {
            return new ArrayList<>(warnings);
        }

        public boolean isValid() 
        {
            return !hasErrors();
        }
    }
}