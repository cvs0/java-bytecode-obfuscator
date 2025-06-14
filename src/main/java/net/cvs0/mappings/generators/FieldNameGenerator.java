package net.cvs0.mappings.generators;

import net.cvs0.config.ObfuscationConfig;

import java.util.concurrent.atomic.AtomicInteger;

public class FieldNameGenerator
{
    private final AtomicInteger counter = new AtomicInteger(0);
    private final String prefix;
    
    public FieldNameGenerator(ObfuscationConfig config)
    {
        this.prefix = "f";
    }
    
    public String generateName(String owner, String fieldName, String descriptor)
    {
        return prefix + counter.incrementAndGet();
    }
}