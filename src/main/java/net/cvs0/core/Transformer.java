package net.cvs0.core;

import net.cvs0.context.ObfuscationContext;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

public interface Transformer
{
    void transform(ClassReader reader, ClassWriter writer, ObfuscationContext context);
    
    String getName();
    
    boolean isEnabled(ObfuscationContext context);
    
    int getPriority();
}