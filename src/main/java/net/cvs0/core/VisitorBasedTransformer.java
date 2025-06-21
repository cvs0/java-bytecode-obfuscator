package net.cvs0.core;

import net.cvs0.context.ObfuscationContext;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

public abstract class VisitorBasedTransformer extends BaseTransformer
{
    protected VisitorBasedTransformer(String name, int priority)
    {
        super(name, priority);
    }
    
    @Override
    protected final void performTransformation(ClassReader reader, ClassWriter writer, 
                                             ObfuscationContext context, String className)
    {
        BaseClassVisitor visitor = createClassVisitor(writer, context, className);
        reader.accept(visitor, getClassReaderFlags());
    }
    
    protected int getClassReaderFlags()
    {
        return 0;
    }
    
    protected abstract BaseClassVisitor createClassVisitor(ClassWriter writer, ObfuscationContext context, String className);
}