package net.cvs0.transformers;

import net.cvs0.context.ObfuscationContext;
import net.cvs0.core.AbstractClassVisitor;
import net.cvs0.core.AbstractTransformer;
import net.cvs0.utils.ConditionObfuscationMethodVisitor;
import org.objectweb.asm.*;

public class ConditionObfuscationTransformer extends AbstractTransformer
{
    
    public ConditionObfuscationTransformer()
    {
        super("ConditionObfuscation", 250);
    }
    
    @Override
    public void transform(ClassReader reader, ClassWriter writer, ObfuscationContext context)
    {
        ClassVisitor visitor = new ConditionObfuscationClassVisitor(writer, context);
        reader.accept(visitor, 0);
    }
    
    @Override
    public boolean isEnabled(ObfuscationContext context)
    {
        return context.getConfig().isObfuscateConditions();
    }
    
    private static class ConditionObfuscationClassVisitor extends AbstractClassVisitor
    {
        public ConditionObfuscationClassVisitor(ClassVisitor classVisitor, ObfuscationContext context)
        {
            super(classVisitor, context);
        }
        
        @Override
        protected String getTransformerName()
        {
            return "ConditionObfuscation";
        }
        
        @Override
        protected MethodVisitor createMethodVisitor(MethodVisitor mv, int access, String name, 
                                                  String descriptor, String signature, String[] exceptions)
        {
            return new ConditionObfuscationMethodVisitor(mv, context, currentClassName, name, descriptor);
        }
    }
}