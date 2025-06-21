package net.cvs0.context;

import net.cvs0.config.ObfuscationConfig;
import net.cvs0.mappings.MappingManager;
import net.cvs0.mappings.export.MappingData;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

public class TransformationContext
{
    private final ConcurrentMap<String, Object> globalProperties = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Object> threadLocalProperties = new ConcurrentHashMap<>();
    private final ThreadLocal<ConcurrentMap<String, Object>> localContext = ThreadLocal.withInitial(ConcurrentHashMap::new);
    
    private final ObfuscationConfig config;
    private final MappingManager mappingManager;
    private final MappingData mappingData;
    
    private final AtomicLong transformationCounter = new AtomicLong(0);
    private final long startTime;
    
    private volatile String currentPhase = "INITIALIZATION";
    private volatile boolean aborted = false;

    public TransformationContext(ObfuscationConfig config)
    {
        this.config = config;
        this.mappingManager = new MappingManager(config);
        this.mappingData = new MappingData();
        this.startTime = System.currentTimeMillis();
        
        initializeDefaults();
    }

    private void initializeDefaults()
    {
        setGlobalProperty("verbose", config.isVerbose());
        String packageScope = config.getPackageScope();
        setGlobalProperty("config.packageScope", packageScope != null ? packageScope : "");
        setGlobalProperty("transformation.startTime", startTime);
        setGlobalProperty("transformation.thread", Thread.currentThread().getName());
    }

    public void setGlobalProperty(String key, Object value)
    {
        globalProperties.put(key, value);
    }

    public <T> T getGlobalProperty(String key, Class<T> type)
    {
        Object value = globalProperties.get(key);
        return type.isInstance(value) ? type.cast(value) : null;
    }

    public <T> T getGlobalProperty(String key, T defaultValue, Class<T> type)
    {
        T value = getGlobalProperty(key, type);
        return value != null ? value : defaultValue;
    }

    public void setThreadProperty(String key, Object value)
    {
        threadLocalProperties.put(Thread.currentThread().getName() + "." + key, value);
    }

    public <T> T getThreadProperty(String key, Class<T> type)
    {
        Object value = threadLocalProperties.get(Thread.currentThread().getName() + "." + key);
        return type.isInstance(value) ? type.cast(value) : null;
    }

    public void setLocalProperty(String key, Object value)
    {
        localContext.get().put(key, value);
    }

    public <T> T getLocalProperty(String key, Class<T> type)
    {
        Object value = localContext.get().get(key);
        return type.isInstance(value) ? type.cast(value) : null;
    }

    public <T> T computeIfAbsent(String key, Supplier<T> supplier, Class<T> type)
    {
        Object value = globalProperties.computeIfAbsent(key, k -> supplier.get());
        return type.isInstance(value) ? type.cast(value) : null;
    }

    public ObfuscationConfig getConfig()
    {
        return config;
    }

    public MappingManager getMappingManager()
    {
        return mappingManager;
    }

    public MappingData getMappingData()
    {
        return mappingData;
    }

    public void recordTransformation()
    {
        transformationCounter.incrementAndGet();
    }

    public long getTransformationCount()
    {
        return transformationCounter.get();
    }

    public void setPhase(String phase)
    {
        this.currentPhase = phase;
        setGlobalProperty("transformation.currentPhase", phase);
        setGlobalProperty("transformation.phaseStartTime", System.currentTimeMillis());
        
        if (config.isVerbose()) {
            System.out.println("[PHASE] " + phase + " (transformations: " + getTransformationCount() + ")");
        }
    }

    public String getCurrentPhase()
    {
        return currentPhase;
    }

    public void abort(String reason)
    {
        this.aborted = true;
        setGlobalProperty("transformation.aborted", true);
        setGlobalProperty("transformation.abortReason", reason);
        
        if (config.isVerbose()) {
            System.err.println("[ABORT] " + reason);
        }
    }

    public boolean isAborted()
    {
        return aborted;
    }

    public long getElapsedTime()
    {
        return System.currentTimeMillis() - startTime;
    }

    public void logProgress(String message)
    {
        if (config.isVerbose()) {
            long elapsed = getElapsedTime();
            System.out.println(String.format("[%s] %s (elapsed: %dms, transformations: %d)", 
                             currentPhase, message, elapsed, getTransformationCount()));
        }
    }

    public ContextSnapshot createSnapshot()
    {
        return new ContextSnapshot(this);
    }

    public void cleanup()
    {
        threadLocalProperties.clear();
        localContext.remove();
        
        setGlobalProperty("transformation.endTime", System.currentTimeMillis());
        setGlobalProperty("transformation.totalDuration", getElapsedTime());
        setGlobalProperty("transformation.completed", !aborted);
    }

    public static class ContextSnapshot
    {
        private final String phase;
        private final long transformationCount;
        private final long elapsedTime;
        private final boolean aborted;
        private final ConcurrentMap<String, Object> propertiesSnapshot;

        private ContextSnapshot(TransformationContext context)
        {
            this.phase = context.currentPhase;
            this.transformationCount = context.getTransformationCount();
            this.elapsedTime = context.getElapsedTime();
            this.aborted = context.aborted;
            this.propertiesSnapshot = new ConcurrentHashMap<>(context.globalProperties);
        }

        public String getPhase()
        {
            return phase;
        }

        public long getTransformationCount()
        {
            return transformationCount;
        }

        public long getElapsedTime()
        {
            return elapsedTime;
        }

        public boolean isAborted()
        {
            return aborted;
        }

        public <T> T getProperty(String key, Class<T> type)
        {
            Object value = propertiesSnapshot.get(key);
            return type.isInstance(value) ? type.cast(value) : null;
        }
    }
}