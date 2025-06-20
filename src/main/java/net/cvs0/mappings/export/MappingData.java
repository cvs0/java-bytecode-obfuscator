package net.cvs0.mappings.export;

import java.util.HashMap;
import java.util.Map;

public class MappingData
{
    private final Map<String, String> classMappings;
    private final Map<String, String> fieldMappings;
    private final Map<String, String> methodMappings;
    private final Map<String, Object> metadata;

    public MappingData()
    {
        this.classMappings = new HashMap<>();
        this.fieldMappings = new HashMap<>();
        this.methodMappings = new HashMap<>();
        this.metadata = new HashMap<>();
    }

    public MappingData(Map<String, String> classMappings, 
                      Map<String, String> fieldMappings, 
                      Map<String, String> methodMappings)
    {
        this.classMappings = new HashMap<>(classMappings);
        this.fieldMappings = new HashMap<>(fieldMappings);
        this.methodMappings = new HashMap<>(methodMappings);
        this.metadata = new HashMap<>();
    }

    public Map<String, String> getClassMappings()
    {
        return classMappings;
    }

    public Map<String, String> getFieldMappings()
    {
        return fieldMappings;
    }

    public Map<String, String> getMethodMappings()
    {
        return methodMappings;
    }

    public Map<String, Object> getMetadata()
    {
        return metadata;
    }

    public void addClassMapping(String original, String obfuscated)
    {
        classMappings.put(original, obfuscated);
    }

    public void addFieldMapping(String original, String obfuscated)
    {
        fieldMappings.put(original, obfuscated);
    }

    public void addMethodMapping(String original, String obfuscated)
    {
        methodMappings.put(original, obfuscated);
    }

    public void addMetadata(String key, Object value)
    {
        metadata.put(key, value);
    }

    public void merge(MappingData other)
    {
        this.classMappings.putAll(other.classMappings);
        this.fieldMappings.putAll(other.fieldMappings);
        this.methodMappings.putAll(other.methodMappings);
        this.metadata.putAll(other.metadata);
    }

    public int getTotalMappings()
    {
        return classMappings.size() + fieldMappings.size() + methodMappings.size();
    }

    public boolean isEmpty()
    {
        return classMappings.isEmpty() && fieldMappings.isEmpty() && methodMappings.isEmpty();
    }

    public void clear()
    {
        classMappings.clear();
        fieldMappings.clear();
        methodMappings.clear();
        metadata.clear();
    }

    @Override
    public String toString()
    {
        return "MappingData{" +
               "classes=" + classMappings.size() +
               ", fields=" + fieldMappings.size() +
               ", methods=" + methodMappings.size() +
               ", total=" + getTotalMappings() +
               '}';
    }
}