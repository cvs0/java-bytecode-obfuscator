package net.cvs0.core;

import net.cvs0.context.ObfuscationContext;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

public abstract class AbstractTransformer implements Transformer
{
    protected final String name;
    protected final int priority;
    
    protected AbstractTransformer(String name, int priority)
    {
        this.name = name;
        this.priority = priority;
    }
    
    @Override
    public String getName()
    {
        return name;
    }
    
    @Override
    public int getPriority()
    {
        return priority;
    }
    
    @Override
    public boolean isEnabled(ObfuscationContext context)
    {
        return true;
    }
    
    protected boolean shouldSkipClass(String className, ObfuscationContext context)
    {
        return context.getConfig().shouldKeepClass(className);
    }
    
    protected void logTransformation(String message, ObfuscationContext context)
    {
        if (context.getConfig().isVerbose()) {
            System.out.println("[" + getName() + "] " + message);
        }
    }
}