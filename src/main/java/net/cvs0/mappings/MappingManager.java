package net.cvs0.mappings;

import net.cvs0.config.ObfuscationConfig;
import net.cvs0.mappings.generators.ClassNameGenerator;
import net.cvs0.mappings.generators.FieldNameGenerator;
import net.cvs0.mappings.generators.MethodNameGenerator;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MappingManager
{
    private final Map<String, String> classMappings = new HashMap<>();
    private final Map<String, String> fieldMappings = new HashMap<>();
    private final Map<String, String> methodMappings = new HashMap<>();
    
    private final ClassNameGenerator classNameGenerator;
    private final FieldNameGenerator fieldNameGenerator;
    private final MethodNameGenerator methodNameGenerator;
    
    private final ObfuscationConfig config;
    private InheritanceTracker inheritanceTracker;
    
    public MappingManager(ObfuscationConfig config)
    {
        this.config = config;
        this.classNameGenerator = new ClassNameGenerator(config);
        this.fieldNameGenerator = new FieldNameGenerator(config);
        this.methodNameGenerator = new MethodNameGenerator(config);
    }
    
    public void generateClassMappings(Set<String> classNames)
    {
        for (String className : classNames) {
            if (shouldRenameClass(className)) {
                String newName = classNameGenerator.generateName(className);
                classMappings.put(className, newName);
            }
        }
    }
    
    public void generateFieldMapping(String owner, String fieldName, String descriptor)
    {
        if (shouldRenameField(owner, fieldName)) {
            String key = owner + "." + fieldName;
            String newName = fieldNameGenerator.generateName(owner, fieldName, descriptor);
            fieldMappings.put(key, newName);
        }
    }
    
    public void setInheritanceTracker(InheritanceTracker inheritanceTracker)
    {
        this.inheritanceTracker = inheritanceTracker;
    }
    
    public void generateMethodMapping(String owner, String methodName, String descriptor)
    {
        if (shouldRenameMethod(owner, methodName, descriptor)) {
            String key = owner + "." + methodName + descriptor;
            
            if (!methodMappings.containsKey(key)) {
                String newName = methodNameGenerator.generateName(owner, methodName, descriptor);
                methodMappings.put(key, newName);
                
                if (inheritanceTracker != null) {
                    propagateMethodRename(owner, methodName, descriptor, newName);
                }
            }
        }
    }
    
    private void propagateMethodRename(String owner, String methodName, String descriptor, String newName)
    {
        if (inheritanceTracker.isInterface(owner)) {
            Set<String> implementors = inheritanceTracker.getImplementorsOf(owner);
            for (String implementor : implementors) {
                String implKey = implementor + "." + methodName + descriptor;
                if (!methodMappings.containsKey(implKey)) {
                    methodMappings.put(implKey, newName);
                }
            }
        } else {
            Set<String> subclasses = inheritanceTracker.getAllSubclasses(owner);
            for (String subclass : subclasses) {
                String subKey = subclass + "." + methodName + descriptor;
                if (!methodMappings.containsKey(subKey)) {
                    methodMappings.put(subKey, newName);
                }
            }
        }
    }
    
    public String getClassMapping(String className)
    {
        return classMappings.getOrDefault(className, className);
    }
    
    public String getFieldMapping(String owner, String fieldName)
    {
        String key = owner + "." + fieldName;
        return fieldMappings.getOrDefault(key, fieldName);
    }
    
    public String getMethodMapping(String owner, String methodName, String descriptor)
    {
        String key = owner + "." + methodName + descriptor;
        return methodMappings.getOrDefault(key, methodName);
    }
    
    public Map<String, String> getClassMappings()
    {
        return new HashMap<>(classMappings);
    }
    
    public Map<String, String> getFieldMappings()
    {
        return new HashMap<>(fieldMappings);
    }
    
    public Map<String, String> getMethodMappings()
    {
        return new HashMap<>(methodMappings);
    }
    
    private boolean shouldRenameClass(String className)
    {
        if (!config.isRenameClasses()) {
            return false;
        }
        
        if (config.shouldKeepClass(className)) {
            return false;
        }
        
        String packageScope = config.getPackageScope();
        if (packageScope != null && !packageScope.isEmpty()) {
            return className.startsWith(packageScope);
        }
        
        return true;
    }
    
    private boolean shouldRenameField(String owner, String fieldName)
    {
        if (!config.isRenameFields()) {
            return false;
        }
        
        if (config.shouldKeepField(owner, fieldName)) {
            return false;
        }
        
        return shouldRenameClass(owner);
    }
    
    private boolean shouldRenameMethod(String owner, String methodName, String descriptor)
    {
        if (!config.isRenameMethods()) {
            return false;
        }
        
        if (config.shouldKeepMethod(owner, methodName, descriptor)) {
            return false;
        }
        
        if (methodName.equals("<init>") || methodName.equals("<clinit>")) {
            return false;
        }
        
        if (methodName.startsWith("lambda$")) {
            return false;
        }
        
        return shouldRenameClass(owner);
    }
}