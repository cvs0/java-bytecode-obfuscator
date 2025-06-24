package net.cvs0.obfuscation;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MappingContext 
{
    private final Map<String, String> classMappings = new ConcurrentHashMap<>();
    private final Map<String, String> fieldMappings = new ConcurrentHashMap<>();
    private final Map<String, String> methodMappings = new ConcurrentHashMap<>();
    private final Map<String, String> reverseMappings = new ConcurrentHashMap<>();
    
    private final Set<String> keepClasses = ConcurrentHashMap.newKeySet();
    private final Set<String> keepMethods = ConcurrentHashMap.newKeySet();
    private final Set<String> keepFields = ConcurrentHashMap.newKeySet();
    
    private final Map<String, Set<String>> hierarchyMappings = new ConcurrentHashMap<>();

    public void mapClass(String originalName, String obfuscatedName) 
    {
        if (originalName == null || obfuscatedName == null) {
            throw new IllegalArgumentException("Names cannot be null");
        }
        classMappings.put(originalName, obfuscatedName);
        reverseMappings.put(obfuscatedName, originalName);
    }

    public void mapField(String className, String originalName, String descriptor, String obfuscatedName) 
    {
        String key = className + "." + originalName + ":" + descriptor;
        fieldMappings.put(key, obfuscatedName);
        reverseMappings.put(obfuscatedName, key);
    }

    public void mapMethod(String className, String originalName, String descriptor, String obfuscatedName) 
    {
        String key = className + "." + originalName + descriptor;
        methodMappings.put(key, obfuscatedName);
        reverseMappings.put(obfuscatedName, key);
    }

    public String getObfuscatedClassName(String originalName) 
    {
        return classMappings.getOrDefault(originalName, originalName);
    }

    public String getObfuscatedFieldName(String className, String fieldName, String descriptor) 
    {
        String key = className + "." + fieldName + ":" + descriptor;
        return fieldMappings.getOrDefault(key, fieldName);
    }

    public String getObfuscatedMethodName(String className, String methodName, String descriptor) 
    {
        String key = className + "." + methodName + descriptor;
        return methodMappings.getOrDefault(key, methodName);
    }

    public String getOriginalName(String obfuscatedName) 
    {
        return reverseMappings.get(obfuscatedName);
    }

    public boolean hasClassMapping(String originalName) 
    {
        return classMappings.containsKey(originalName);
    }

    public boolean hasFieldMapping(String className, String fieldName, String descriptor) 
    {
        String key = className + "." + fieldName + ":" + descriptor;
        return fieldMappings.containsKey(key);
    }

    public boolean hasMethodMapping(String className, String methodName, String descriptor) 
    {
        String key = className + "." + methodName + descriptor;
        return methodMappings.containsKey(key);
    }

    public void addKeepClass(String className) 
    {
        keepClasses.add(className);
    }

    public void addKeepMethod(String className, String methodName, String descriptor) 
    {
        String key = className + "." + methodName + descriptor;
        keepMethods.add(key);
    }

    public void addKeepField(String className, String fieldName, String descriptor) 
    {
        String key = className + "." + fieldName + ":" + descriptor;
        keepFields.add(key);
    }

    public boolean shouldKeepClass(String className) 
    {
        return keepClasses.contains(className);
    }

    public boolean shouldKeepMethod(String className, String methodName, String descriptor) 
    {
        String key = className + "." + methodName + descriptor;
        return keepMethods.contains(key);
    }

    public boolean shouldKeepField(String className, String fieldName, String descriptor) 
    {
        String key = className + "." + fieldName + ":" + descriptor;
        return keepFields.contains(key);
    }

    public void addHierarchyMapping(String className, String member) 
    {
        hierarchyMappings.computeIfAbsent(className, k -> ConcurrentHashMap.newKeySet()).add(member);
    }

    public Set<String> getHierarchyMappings(String className) 
    {
        return hierarchyMappings.getOrDefault(className, Collections.emptySet());
    }

    public Map<String, String> getAllClassMappings() 
    {
        return new HashMap<>(classMappings);
    }

    public Map<String, String> getAllFieldMappings() 
    {
        return new HashMap<>(fieldMappings);
    }

    public Map<String, String> getAllMethodMappings() 
    {
        return new HashMap<>(methodMappings);
    }

    public int getTotalMappings() 
    {
        return classMappings.size() + fieldMappings.size() + methodMappings.size();
    }

    public void clear() 
    {
        classMappings.clear();
        fieldMappings.clear();
        methodMappings.clear();
        reverseMappings.clear();
        keepClasses.clear();
        keepMethods.clear();
        keepFields.clear();
        hierarchyMappings.clear();
    }

    public void printMappingStats() 
    {
        System.out.println("Mapping Statistics:");
        System.out.println("  Classes mapped: " + classMappings.size());
        System.out.println("  Fields mapped: " + fieldMappings.size());
        System.out.println("  Methods mapped: " + methodMappings.size());
        System.out.println("  Classes kept: " + keepClasses.size());
        System.out.println("  Methods kept: " + keepMethods.size());
        System.out.println("  Fields kept: " + keepFields.size());
        System.out.println("  Total mappings: " + getTotalMappings());
    }
}