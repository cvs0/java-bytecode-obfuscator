package net.cvs0.context;

import java.util.HashMap;
import java.util.Map;

public class RemapperContext
{
    private final Map<String, String> classMappings = new HashMap<>();
    private final Map<String, String> fieldMappings = new HashMap<>();
    private final Map<String, String> methodMappings = new HashMap<>();
    
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
    
    public String getClassMapping(String original)
    {
        return classMappings.get(original);
    }
    
    public String getFieldMapping(String original)
    {
        return fieldMappings.get(original);
    }
    
    public String getMethodMapping(String original)
    {
        return methodMappings.get(original);
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
}
