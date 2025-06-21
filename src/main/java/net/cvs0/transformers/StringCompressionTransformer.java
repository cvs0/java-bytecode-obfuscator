package net.cvs0.transformers;

import net.cvs0.context.ObfuscationContext;
import net.cvs0.core.UniversalTransformer;
import net.cvs0.utils.StringCompressionMethodVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;

public class StringCompressionTransformer extends UniversalTransformer
{
    public StringCompressionTransformer()
    {
        super("StringCompression", 350);
    }
    
    @Override
    public boolean isEnabled(ObfuscationContext context)
    {
        return context.getConfig().isCompressStrings();
    }

    @Override
    protected UniversalClassVisitor createClassVisitor(ClassWriter writer, ObfuscationContext context)
    {
        return new StringCompressionClassVisitor(writer, context);
    }
    
    private static class StringCompressionClassVisitor extends UniversalClassVisitor
    {
        public StringCompressionClassVisitor(ClassWriter classVisitor, ObfuscationContext context)
        {
            super(classVisitor, context);
        }
        
        @Override
        protected String getTransformerName()
        {
            return "StringCompression";
        }
        
        @Override
        protected MethodVisitor createMethodVisitor(MethodVisitor mv, int access, String name, String descriptor)
        {
            return new StringCompressionMethodVisitor(mv, this, name, descriptor);
        }
    }
}