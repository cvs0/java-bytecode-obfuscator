package net.cvs0.mappings;

import net.cvs0.config.ObfuscationConfig;
import net.cvs0.mappings.generators.ClassNameGenerator;
import net.cvs0.mappings.generators.FieldNameGenerator;
import net.cvs0.mappings.generators.MethodNameGenerator;

import java.util.HashMap;
import java.util.HashSet;
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
        if (fieldName == null) {
            return;
        }
        
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
        if (methodName == null) {
            return;
        }
        
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
        Set<String> allRelatedClasses = new HashSet<>();
        
        if (inheritanceTracker.isInterface(owner)) {
            allRelatedClasses.addAll(inheritanceTracker.getImplementorsOf(owner));
        } else {
            allRelatedClasses.addAll(inheritanceTracker.getAllSubclasses(owner));
            allRelatedClasses.addAll(inheritanceTracker.getAllSuperclasses(owner));
            allRelatedClasses.addAll(inheritanceTracker.getAllInterfaces(owner));
        }
        
        for (String relatedClass : allRelatedClasses) {
            String relatedKey = relatedClass + "." + methodName + descriptor;
            if (!methodMappings.containsKey(relatedKey)) {
                methodMappings.put(relatedKey, newName);
            }
        }
        
        if (!inheritanceTracker.isInterface(owner)) {
            Set<String> interfaces = inheritanceTracker.getAllInterfaces(owner);
            for (String iface : interfaces) {
                Set<String> implementors = inheritanceTracker.getImplementorsOf(iface);
                for (String implementor : implementors) {
                    String implKey = implementor + "." + methodName + descriptor;
                    if (!methodMappings.containsKey(implKey)) {
                        methodMappings.put(implKey, newName);
                    }
                }
            }
        }
    }
    
    public String getClassMapping(String className)
    {
        if (className == null) {
            return null;
        }
        return classMappings.getOrDefault(className, className);
    }
    
    public String getFieldMapping(String owner, String fieldName)
    {
        if (fieldName == null) {
            return null;
        }
        String key = owner + "." + fieldName;
        return fieldMappings.getOrDefault(key, fieldName);
    }
    
    public String getMethodMapping(String owner, String methodName, String descriptor)
    {
        if (methodName == null) {
            return null;
        }
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
        
        return config.isInPackageScope(className);
    }
    
    private boolean shouldRenameField(String owner, String fieldName)
    {
        if (fieldName == null) {
            return false;
        }
        
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
        if (methodName == null) {
            return false;
        }
        
        if (!config.isRenameMethods()) {
            return false;
        }
        
        if (config.shouldKeepMethod(owner, methodName, descriptor)) {
            return false;
        }
        
        if (methodName != null && (methodName.equals("<init>") || methodName.equals("<clinit>"))) {
            return false;
        }
        
        if (methodName != null && methodName.startsWith("lambda$")) {
            return false;
        }
        
        if (methodName != null && (methodName.equals("values") || methodName.equals("valueOf") || methodName.equals("$values"))) {
            return false;
        }
        
        return shouldRenameClass(owner);
    }
}