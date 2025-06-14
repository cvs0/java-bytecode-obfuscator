package net.cvs0.config;

import java.util.*;
import java.util.regex.Pattern;

public class ObfuscationConfig
{
    private final String mainClass;
    private final boolean renameClasses;
    private final boolean renameFields;
    private final boolean renameMethods;
    private final boolean renameLocalVariables;
    private final boolean verbose;
    private final KeepRules keepRules;
    private final String packageScope;

    public ObfuscationConfig(
            String mainClass,
            boolean renameClasses,
            boolean renameFields,
            boolean renameMethods,
            boolean renameLocalVariables,
            boolean verbose,
            KeepRules keepRules,
            String packageScope)
    {
        this.mainClass = mainClass;
        this.renameClasses = renameClasses;
        this.renameFields = renameFields;
        this.renameMethods = renameMethods;
        this.renameLocalVariables = renameLocalVariables;
        this.verbose = verbose;
        this.keepRules = keepRules != null ? keepRules : new KeepRules();
        this.packageScope = packageScope;
    }

    public String getMainClass()
    {
        return mainClass;
    }

    public boolean isRenameClasses()
    {
        return renameClasses;
    }

    public boolean isRenameFields()
    {
        return renameFields;
    }

    public boolean isRenameMethods()
    {
        return renameMethods;
    }
    
    public boolean isRenameLocalVariables()
    {
        return renameLocalVariables;
    }
    
    public boolean isVerbose()
    {
        return verbose;
    }
    
    public String getPackageScope()
    {
        return packageScope;
    }
    
    public KeepRules getKeepRules()
    {
        return keepRules;
    }
    
    public boolean shouldKeepClass(String className)
    {
        return keepRules.shouldKeepClass(className);
    }
    
    public boolean shouldKeepMethod(String className, String methodName, String methodDescriptor)
    {
        return keepRules.shouldKeepMethod(className, methodName, methodDescriptor);
    }
    
    public boolean shouldKeepField(String className, String fieldName)
    {
        return keepRules.shouldKeepField(className, fieldName);
    }
    
    public boolean isInPackageScope(String className)
    {
        if (packageScope == null || packageScope.isEmpty()) {
            return true;
        }
        if (className == null) {
            return false;
        }
        return className.startsWith(packageScope);
    }
    
    public static class KeepRules
    {
        private final Set<String> keepClasses;
        private final Set<Pattern> keepClassPatterns;
        private final Map<String, Set<String>> keepClassMethods;
        private final Map<String, Set<Pattern>> keepClassMethodPatterns;
        private final Map<String, Set<String>> keepClassFields;
        private final Map<String, Set<Pattern>> keepClassFieldPatterns;
        private final Set<String> keepAllMethodsForClasses;
        private final Set<String> keepAllFieldsForClasses;
        
        public KeepRules()
        {
            this.keepClasses = new HashSet<>();
            this.keepClassPatterns = new HashSet<>();
            this.keepClassMethods = new HashMap<>();
            this.keepClassMethodPatterns = new HashMap<>();
            this.keepClassFields = new HashMap<>();
            this.keepClassFieldPatterns = new HashMap<>();
            this.keepAllMethodsForClasses = new HashSet<>();
            this.keepAllFieldsForClasses = new HashSet<>();
        }
        
        public boolean shouldKeepClass(String className)
        {
            if (keepClasses.contains(className)) {
                return true;
            }
            
            for (Pattern pattern : keepClassPatterns) {
                if (pattern.matcher(className).matches()) {
                    return true;
                }
            }
            
            return false;
        }
        
        public boolean shouldKeepMethod(String className, String methodName, String methodDescriptor)
        {
            if (shouldKeepClass(className)) {
                return true;
            }
            
            if (keepAllMethodsForClasses.contains(className)) {
                return true;
            }
            
            Set<String> methodsToKeep = keepClassMethods.get(className);
            if (methodsToKeep != null) {
                String methodSignature = methodName + methodDescriptor;
                if (methodsToKeep.contains(methodName) || methodsToKeep.contains(methodSignature)) {
                    return true;
                }
            }
            
            Set<Pattern> methodPatterns = keepClassMethodPatterns.get(className);
            if (methodPatterns != null) {
                for (Pattern pattern : methodPatterns) {
                    if (pattern.matcher(methodName).matches()) {
                        return true;
                    }
                }
            }
            
            return false;
        }
        
        public boolean shouldKeepField(String className, String fieldName)
        {
            if (shouldKeepClass(className)) {
                return true;
            }
            
            if (keepAllFieldsForClasses.contains(className)) {
                return true;
            }
            
            Set<String> fieldsToKeep = keepClassFields.get(className);
            if (fieldsToKeep != null && fieldsToKeep.contains(fieldName)) {
                return true;
            }
            
            Set<Pattern> fieldPatterns = keepClassFieldPatterns.get(className);
            if (fieldPatterns != null) {
                for (Pattern pattern : fieldPatterns) {
                    if (pattern.matcher(fieldName).matches()) {
                        return true;
                    }
                }
            }
            
            return false;
        }
        
        public Set<String> getKeepClasses()
        {
            return keepClasses;
        }
        
        public Map<String, Set<String>> getKeepClassMethods()
        {
            return keepClassMethods;
        }
        
        public Map<String, Set<String>> getKeepClassFields()
        {
            return keepClassFields;
        }
    }
    
    public static class Builder
    {
        private String mainClass;
        private boolean renameClasses = true;
        private boolean renameFields = true;
        private boolean renameMethods = true;
        private boolean renameLocalVariables = true;
        private boolean verbose = false;
        private final KeepRules keepRules = new KeepRules();
        private String packageScope;
        
        public Builder mainClass(String mainClass)
        {
            this.mainClass = mainClass;
            if (mainClass != null) {
                String[] parts = mainClass.split("/");
                if (parts.length >= 2) {
                    this.packageScope = parts[0] + "/" + parts[1];
                } else if (parts.length == 1) {
                    this.packageScope = parts[0];
                } else {
                    this.packageScope = "";
                }
            }
            return this;
        }
        
        public Builder renameClasses(boolean renameClasses)
        {
            this.renameClasses = renameClasses;
            return this;
        }
        
        public Builder renameFields(boolean renameFields)
        {
            this.renameFields = renameFields;
            return this;
        }
        
        public Builder renameMethods(boolean renameMethods)
        {
            this.renameMethods = renameMethods;
            return this;
        }
        
        public Builder renameLocalVariables(boolean renameLocalVariables)
        {
            this.renameLocalVariables = renameLocalVariables;
            return this;
        }
        
        public Builder verbose(boolean verbose)
        {
            this.verbose = verbose;
            return this;
        }
        
        public Builder keepClass(String className)
        {
            keepRules.keepClasses.add(className);
            return this;
        }
        
        public Builder keepClassPattern(String classPattern)
        {
            keepRules.keepClassPatterns.add(Pattern.compile(classPattern));
            return this;
        }
        
        public Builder keepClassMethod(String className, String methodName)
        {
            keepRules.keepClassMethods.computeIfAbsent(className, k -> new HashSet<>()).add(methodName);
            return this;
        }
        
        public Builder keepClassMethodWithDescriptor(String className, String methodName, String descriptor)
        {
            keepRules.keepClassMethods.computeIfAbsent(className, k -> new HashSet<>()).add(methodName + descriptor);
            return this;
        }
        
        public Builder keepClassMethodPattern(String className, String methodPattern)
        {
            keepRules.keepClassMethodPatterns.computeIfAbsent(className, k -> new HashSet<>()).add(Pattern.compile(methodPattern));
            return this;
        }
        
        public Builder keepAllMethodsForClass(String className)
        {
            keepRules.keepAllMethodsForClasses.add(className);
            return this;
        }
        
        public Builder keepClassField(String className, String fieldName)
        {
            keepRules.keepClassFields.computeIfAbsent(className, k -> new HashSet<>()).add(fieldName);
            return this;
        }
        
        public Builder keepClassFieldPattern(String className, String fieldPattern)
        {
            keepRules.keepClassFieldPatterns.computeIfAbsent(className, k -> new HashSet<>()).add(Pattern.compile(fieldPattern));
            return this;
        }
        
        public Builder keepAllFieldsForClass(String className)
        {
            keepRules.keepAllFieldsForClasses.add(className);
            return this;
        }
        
        public Builder keepMainClass()
        {
            if (mainClass != null) {
                keepClass(mainClass);
            }
            return this;
        }
        
        public Builder keepStandardEntryPoints()
        {
            keepClassMethodPattern(".*", "main\\(\\[Ljava/lang/String;\\)V");
            keepClassMethodPattern(".*", "<init>");
            keepClassMethodPattern(".*", "<clinit>");
            return this;
        }
        
        public ObfuscationConfig build()
        {
            return new ObfuscationConfig(mainClass, renameClasses, renameFields, renameMethods, renameLocalVariables, verbose, keepRules, packageScope);
        }
    }
}
