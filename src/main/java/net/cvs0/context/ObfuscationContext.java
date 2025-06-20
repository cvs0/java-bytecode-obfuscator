package net.cvs0.context;

import net.cvs0.config.ObfuscationConfig;
import net.cvs0.mappings.MappingManager;
import net.cvs0.mappings.export.MappingData;
import net.cvs0.mappings.remappers.MapBasedRenamer;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ObfuscationContext
{
    private final TransformationContext transformationContext;
    private final Map<String, ClassContext> classContexts = new ConcurrentHashMap<>();
    private final Map<String, MapBasedRenamer> renamers = new HashMap<>();
    private final ThreadLocal<ClassContext> currentClassContext = new ThreadLocal<>();
    
    private ObfuscationConfig config;
    private String currentClassName;
    private MappingManager mappingManager;
    
    public ObfuscationContext(ObfuscationConfig config)
    {
        this.config = config;
        this.transformationContext = new TransformationContext(config);
        this.mappingManager = transformationContext.getMappingManager();
    }
    
    public TransformationContext getTransformationContext()
    {
        return transformationContext;
    }
    
    public ClassContext getClassContext(String className)
    {
        return classContexts.computeIfAbsent(className, name -> new ClassContext(name, transformationContext));
    }
    
    public ClassContext getCurrentClassContext()
    {
        return currentClassContext.get();
    }
    
    public void setCurrentClassContext(String className)
    {
        ClassContext context = getClassContext(className);
        currentClassContext.set(context);
        this.currentClassName = className;
    }
    
    public void setProperty(String key, Object value)
    {
        transformationContext.setGlobalProperty(key, value);
    }
    
    public Object getProperty(String key)
    {
        return transformationContext.getGlobalProperty(key, Object.class);
    }
    
    public <T> T getProperty(String key, Class<T> type)
    {
        return transformationContext.getGlobalProperty(key, type);
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
        return transformationContext.getGlobalProperty(key, Object.class) != null;
    }
    
    public Map<String, String> getClassMappings()
    {
        return transformationContext.getMappingData().getClassMappings();
    }
    
    public Map<String, String> getFieldMappings()
    {
        return transformationContext.getMappingData().getFieldMappings();
    }
    
    public Map<String, String> getMethodMappings()
    {
        return transformationContext.getMappingData().getMethodMappings();
    }
    
    public void addClassMapping(String original, String obfuscated)
    {
        transformationContext.getMappingData().addClassMapping(original, obfuscated);
    }
    
    public void addFieldMapping(String original, String obfuscated)
    {
        transformationContext.getMappingData().addFieldMapping(original, obfuscated);
    }
    
    public void addMethodMapping(String original, String obfuscated)
    {
        transformationContext.getMappingData().addMethodMapping(original, obfuscated);
    }
    
    public MappingData getMappingData()
    {
        return transformationContext.getMappingData();
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
        setCurrentClassContext(currentClassName);
    }
    
    public MappingManager getMappingManager()
    {
        return mappingManager;
    }
    
    public void setMappingManager(MappingManager mappingManager)
    {
        this.mappingManager = mappingManager;
    }
    
    public void recordTransformation(String type)
    {
        ClassContext classContext = getCurrentClassContext();
        if (classContext != null) {
            classContext.recordTransformation(type);
        } else {
            transformationContext.recordTransformation();
        }
    }
    
    public void setPhase(String phase)
    {
        transformationContext.setPhase(phase);
    }
    
    public void logProgress(String message)
    {
        transformationContext.logProgress(message);
    }
    
    public void cleanup()
    {
        transformationContext.cleanup();
        currentClassContext.remove();
    }
}
