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
        if (fieldName == null || owner == null) {
            return;
        }
        
        if (shouldRenameField(owner, fieldName)) {
            String key = owner + "." + fieldName;
            
            if (!fieldMappings.containsKey(key)) {
                String inheritedMapping = findInheritedFieldMapping(owner, fieldName);
                
                if (inheritedMapping != null) {
                    fieldMappings.put(key, inheritedMapping);
                    propagateFieldRename(owner, fieldName, inheritedMapping);
                } else {
                    String newName = fieldNameGenerator.generateName(owner, fieldName, descriptor);
                    fieldMappings.put(key, newName);
                    
                    if (inheritanceTracker != null) {
                        propagateFieldRename(owner, fieldName, newName);
                    }
                }
            }
        }
    }
    
    public void ensureFieldMapping(String owner, String fieldName, String newName)
    {
        if (fieldName == null || owner == null || newName == null) {
            return;
        }
        
        String key = owner + "." + fieldName;
        fieldMappings.put(key, newName);
        
        if (inheritanceTracker != null) {
            propagateFieldRename(owner, fieldName, newName);
        }
    }
    
    private String findInheritedFieldMapping(String owner, String fieldName)
    {
        if (inheritanceTracker == null) {
            return null;
        }
        
        for (String superClass : inheritanceTracker.getAllSuperclasses(owner)) {
            String superKey = superClass + "." + fieldName;
            if (fieldMappings.containsKey(superKey)) {
                return fieldMappings.get(superKey);
            }
        }
        
        for (String iface : inheritanceTracker.getAllInterfaces(owner)) {
            String ifaceKey = iface + "." + fieldName;
            if (fieldMappings.containsKey(ifaceKey)) {
                return fieldMappings.get(ifaceKey);
            }
        }
        
        return null;
    }
    
    private void propagateFieldRename(String owner, String fieldName, String newName)
    {
        if (inheritanceTracker == null) {
            return;
        }
        
        for (String subClass : inheritanceTracker.getAllSubclasses(owner)) {
            String subKey = subClass + "." + fieldName;
            if (!fieldMappings.containsKey(subKey) && shouldRenameField(subClass, fieldName)) {
                fieldMappings.put(subKey, newName);
            }
        }
        
        if (inheritanceTracker.isInterface(owner)) {
            for (String implementor : inheritanceTracker.getImplementorsOf(owner)) {
                String implKey = implementor + "." + fieldName;
                if (!fieldMappings.containsKey(implKey) && shouldRenameField(implementor, fieldName)) {
                    fieldMappings.put(implKey, newName);
                }
            }
        }
        
        if (inheritanceTracker.isInnerClass(owner)) {
            String outerClass = inheritanceTracker.getOuterClass(owner);
            if (outerClass != null && inheritanceTracker.hasFieldAccess(owner, outerClass)) {
                String outerKey = outerClass + "." + fieldName;
                if (!fieldMappings.containsKey(outerKey) && shouldRenameField(outerClass, fieldName)) {
                    fieldMappings.put(outerKey, newName);
                }
            }
        }
    }
    
    public void setInheritanceTracker(InheritanceTracker inheritanceTracker)
    {
        this.inheritanceTracker = inheritanceTracker;
    }
    
    public InheritanceTracker getInheritanceTracker()
    {
        return inheritanceTracker;
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
        String directMapping = fieldMappings.get(key);
        if (directMapping != null) {
            return directMapping;
        }
        
        if (inheritanceTracker != null) {
            String inheritedMapping = findInheritedFieldMapping(owner, fieldName);
            if (inheritedMapping != null) {
                return inheritedMapping;
            }
        }
        
        return fieldName;
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
        
        if (!shouldRenameClass(owner)) {
            return false;
        }
        
        if (fieldName.equals("$VALUES") || fieldName.equals("ENUM$VALUES")) {
            return false;
        }
        
        if (fieldName.startsWith("$assertionsDisabled") || fieldName.startsWith("$switch")) {
            return false;
        }
        
        if (fieldName.startsWith("this$") || fieldName.startsWith("val$")) {
            return false;
        }
        
        if (fieldName.equals("serialVersionUID")) {
            return false;
        }
        
        return true;
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