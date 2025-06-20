package net.cvs0.context;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ClassContext
{
    private final String className;
    private final TransformationContext globalContext;
    private final Map<String, Object> classProperties;
    private final Set<String> processedMethods;
    private final Set<String> processedFields;
    
    private String superClass;
    private String[] interfaces;
    private int access;
    private boolean isInterface;
    private boolean isAbstract;
    private boolean isEnum;
    private boolean isAnnotation;
    
    private long methodCount;
    private long fieldCount;
    private long transformationsApplied;

    public ClassContext(String className, TransformationContext globalContext)
    {
        this.className = className;
        this.globalContext = globalContext;
        this.classProperties = new HashMap<>();
        this.processedMethods = new HashSet<>();
        this.processedFields = new HashSet<>();
        
        initializeClassInfo();
    }

    private void initializeClassInfo()
    {
        setProperty("className", className);
        setProperty("startTime", System.currentTimeMillis());
        setProperty("thread", Thread.currentThread().getName());
    }

    public void setClassInfo(int access, String superName, String[] interfaces)
    {
        this.access = access;
        this.superClass = superName;
        this.interfaces = interfaces != null ? interfaces.clone() : new String[0];
        
        this.isInterface = (access & 0x0200) != 0;
        this.isAbstract = (access & 0x0400) != 0;
        this.isEnum = (access & 0x4000) != 0;
        this.isAnnotation = (access & 0x2000) != 0;
        
        setProperty("access", access);
        setProperty("superClass", superName);
        setProperty("interfaces", interfaces);
        setProperty("isInterface", isInterface);
        setProperty("isAbstract", isAbstract);
        setProperty("isEnum", isEnum);
        setProperty("isAnnotation", isAnnotation);
    }

    public String getClassName()
    {
        return className;
    }

    public TransformationContext getGlobalContext()
    {
        return globalContext;
    }

    public void setProperty(String key, Object value)
    {
        classProperties.put(key, value);
    }

    public <T> T getProperty(String key, Class<T> type)
    {
        Object value = classProperties.get(key);
        return type.isInstance(value) ? type.cast(value) : null;
    }

    public <T> T getProperty(String key, T defaultValue, Class<T> type)
    {
        T value = getProperty(key, type);
        return value != null ? value : defaultValue;
    }

    public void recordMethodProcessed(String methodName, String descriptor)
    {
        String methodKey = methodName + descriptor;
        processedMethods.add(methodKey);
        methodCount++;
        setProperty("methodCount", methodCount);
    }

    public void recordFieldProcessed(String fieldName)
    {
        processedFields.add(fieldName);
        fieldCount++;
        setProperty("fieldCount", fieldCount);
    }

    public void recordTransformation(String transformationType)
    {
        transformationsApplied++;
        globalContext.recordTransformation();
        setProperty("transformationsApplied", transformationsApplied);
        
        String counterKey = "transformation." + transformationType;
        Long currentCount = getProperty(counterKey, Long.class);
        setProperty(counterKey, currentCount != null ? currentCount + 1 : 1L);
    }

    public boolean isMethodProcessed(String methodName, String descriptor)
    {
        return processedMethods.contains(methodName + descriptor);
    }

    public boolean isFieldProcessed(String fieldName)
    {
        return processedFields.contains(fieldName);
    }

    public boolean shouldProcess()
    {
        return globalContext.getConfig().isInPackageScope(className) &&
               !globalContext.getConfig().shouldKeepClass(className) &&
               !globalContext.isAborted();
    }

    public boolean shouldProcessMethod(String methodName, String descriptor)
    {
        return shouldProcess() &&
               !globalContext.getConfig().shouldKeepMethod(className, methodName, descriptor);
    }

    public boolean shouldProcessField(String fieldName)
    {
        return shouldProcess() &&
               !globalContext.getConfig().shouldKeepField(className, fieldName);
    }

    public void logTransformation(String transformationType, String message)
    {
        if (globalContext.getConfig().isVerbose()) {
            System.out.println(String.format("[%s] %s in %s: %s", 
                             transformationType, globalContext.getCurrentPhase(), className, message));
        }
    }

    public String getSuperClass()
    {
        return superClass;
    }

    public String[] getInterfaces()
    {
        return interfaces != null ? interfaces.clone() : new String[0];
    }

    public int getAccess()
    {
        return access;
    }

    public boolean isInterface()
    {
        return isInterface;
    }

    public boolean isAbstract()
    {
        return isAbstract;
    }

    public boolean isEnum()
    {
        return isEnum;
    }

    public boolean isAnnotation()
    {
        return isAnnotation;
    }

    public long getMethodCount()
    {
        return methodCount;
    }

    public long getFieldCount()
    {
        return fieldCount;
    }

    public long getTransformationsApplied()
    {
        return transformationsApplied;
    }

    public Set<String> getProcessedMethods()
    {
        return new HashSet<>(processedMethods);
    }

    public Set<String> getProcessedFields()
    {
        return new HashSet<>(processedFields);
    }

    public void complete()
    {
        setProperty("endTime", System.currentTimeMillis());
        Long startTime = getProperty("startTime", Long.class);
        if (startTime != null) {
            setProperty("processingDuration", System.currentTimeMillis() - startTime);
        }
        setProperty("completed", true);
    }

    @Override
    public String toString()
    {
        return "ClassContext{" +
               "className='" + className + '\'' +
               ", methods=" + methodCount +
               ", fields=" + fieldCount +
               ", transformations=" + transformationsApplied +
               ", processed=" + (processedMethods.size() + processedFields.size()) +
               '}';
    }
}