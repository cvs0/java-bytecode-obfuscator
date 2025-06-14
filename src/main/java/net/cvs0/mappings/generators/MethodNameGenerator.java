package net.cvs0.mappings.generators;

import net.cvs0.config.ObfuscationConfig;

import java.util.concurrent.atomic.AtomicInteger;

public class MethodNameGenerator
{
    private final AtomicInteger counter = new AtomicInteger(0);
    private final String prefix;
    
    public MethodNameGenerator(ObfuscationConfig config)
    {
        this.prefix = "m";
    }
    
    public String generateName(String owner, String methodName, String descriptor)
    {
        return prefix + counter.incrementAndGet();
    }
}