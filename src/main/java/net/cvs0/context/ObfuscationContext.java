package net.cvs0.context;

import net.cvs0.config.ObfuscationConfig;
import net.cvs0.mappings.MappingManager;
import net.cvs0.mappings.remappers.MapBasedRenamer;

import java.util.HashMap;
import java.util.Map;

public class ObfuscationContext
{
    private final Map<String, Object> properties = new HashMap<>();
    private final Map<String, String> classMappings = new HashMap<>();
    private final Map<String, String> fieldMappings = new HashMap<>();
    private final Map<String, String> methodMappings = new HashMap<>();
    private final Map<String, MapBasedRenamer> renamers = new HashMap<>();
    
    private ObfuscationConfig config;
    private String currentClassName;
    private MappingManager mappingManager;
    
    public ObfuscationContext(ObfuscationConfig config)
    {
        this.config = config;
    }
    
    public void setProperty(String key, Object value)
    {
        properties.put(key, value);
    }
    
    public Object getProperty(String key)
    {
        return properties.get(key);
    }
    
    public <T> T getProperty(String key, Class<T> type)
    {
        Object value = properties.get(key);
        if (value != null && type.isInstance(value)) {
            return type.cast(value);
        }
        return null;
    }
    
    public void setConfig(ObfuscationConfig config)
    {
        this.config = config;
    }
    
    public ObfuscationConfig getConfig()
    {
        return config;
    }
    
    public boolean hasProperty(String key)
    {
        return properties.containsKey(key);
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
    
    public MapBasedRenamer getRenamer(String type)
    {
        return renamers.computeIfAbsent(type, k -> {
            String prefix = getDefaultPrefix(type);
            return new MapBasedRenamer(prefix, config);
        });
    }
    
    private String getDefaultPrefix(String type)
    {
        switch (type.toLowerCase()) {
            case "class":
                return "a";
            case "field":
                return "f";
            case "method":
                return "m";
            case "localvar":
            case "local":
                return "v";
            default:
                return "a";
        }
    }
    
    public void setRenamer(String type, MapBasedRenamer renamer)
    {
        renamers.put(type, renamer);
    }
    
    public boolean isVerbose()
    {
        return config != null && config.isVerbose();
    }
    
    public String getCurrentClassName()
    {
        return currentClassName;
    }
    
    public void setCurrentClassName(String currentClassName)
    {
        this.currentClassName = currentClassName;
    }
    
    public MappingManager getMappingManager()
    {
        return mappingManager;
    }
    
    public void setMappingManager(MappingManager mappingManager)
    {
        this.mappingManager = mappingManager;
    }
}
