package net.cvs0.core;

import net.cvs0.context.ObfuscationContext;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

public abstract class BaseTransformer implements Transformer
{
    protected final String name;
    protected final int priority;
    
    protected BaseTransformer(String name, int priority)
    {
        this.name = name;
        this.priority = priority;
    }
    
    @Override
    public final String getName()
    {
        return name;
    }
    
    @Override
    public final int getPriority()
    {
        return priority;
    }
    
    @Override
    public boolean isEnabled(ObfuscationContext context)
    {
        return true;
    }
    
    @Override
    public final void transform(ClassReader reader, ClassWriter writer, ObfuscationContext context)
    {
        if (!validateInputs(reader, writer, context)) {
            return;
        }
        
        if (!isEnabled(context)) {
            copyClass(reader, writer);
            return;
        }
        
        String className = extractClassName(reader);
        if (className == null || shouldSkipClass(className, context)) {
            copyClass(reader, writer);
            return;
        }
        
        try {
            performTransformation(reader, writer, context, className);
        } catch (SecurityException e) {
            logTransformation("Security restriction while transforming class " + className + ": " + e.getMessage(), context);
            throw e;
        } catch (OutOfMemoryError e) {
            logTransformation("Out of memory while transforming class " + className, context);
            throw e;
        } catch (Exception e) {
            logTransformation("Failed to transform class " + className + ": " + e.getMessage(), context);
            if (context.getConfig().isVerbose()) {
                e.printStackTrace();
            }
            copyClass(reader, writer);
        }
    }
    
    protected boolean validateInputs(ClassReader reader, ClassWriter writer, ObfuscationContext context)
    {
        if (context == null) {
            throw new IllegalArgumentException("ObfuscationContext cannot be null");
        }
        
        if (reader == null) {
            throw new IllegalArgumentException("ClassReader cannot be null");
        }
        
        if (writer == null) {
            throw new IllegalArgumentException("ClassWriter cannot be null");
        }
        
        return true;
    }
    
    protected String extractClassName(ClassReader reader)
    {
        try {
            String className = reader.getClassName();
            if (className == null || className.isEmpty()) {
                return null;
            }
            
            if (className.length() > 1000) {
                return null;
            }
            
            return className;
        } catch (Exception e) {
            return null;
        }
    }
    
    protected boolean shouldSkipClass(String className, ObfuscationContext context)
    {
        return context.getConfig().shouldKeepClass(className) ||
               !context.getConfig().isInPackageScope(className);
    }
    
    protected void copyClass(ClassReader reader, ClassWriter writer)
    {
        reader.accept(writer, 0);
    }
    
    protected void logTransformation(String message, ObfuscationContext context)
    {
        if (context.getConfig().isVerbose()) {
            System.out.println("[" + getName() + "] " + message);
        }
    }
    
    protected abstract void performTransformation(ClassReader reader, ClassWriter writer, 
                                                ObfuscationContext context, String className);
}