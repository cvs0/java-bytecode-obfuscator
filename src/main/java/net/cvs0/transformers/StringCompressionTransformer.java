package net.cvs0.transformers;

import net.cvs0.context.ObfuscationContext;
import net.cvs0.core.AbstractClassVisitor;
import net.cvs0.core.AbstractTransformer;
import net.cvs0.utils.StringCompressionMethodVisitor;
import org.objectweb.asm.*;

public class StringCompressionTransformer extends AbstractTransformer
{
    
    public StringCompressionTransformer()
    {
        super("StringCompression", 350);
    }
    
    @Override
    public void transform(ClassReader reader, ClassWriter writer, ObfuscationContext context)
    {
        logTransformation("Applying string compression", context);
        ClassVisitor visitor = new StringCompressionClassVisitor(writer, context);
        reader.accept(visitor, 0);
    }
    
    @Override
    public boolean isEnabled(ObfuscationContext context)
    {
        return context.getConfig().isCompressStrings();
    }
    
    private static class StringCompressionClassVisitor extends AbstractClassVisitor
    {
        public StringCompressionClassVisitor(ClassVisitor classVisitor, ObfuscationContext context)
        {
            super(classVisitor, context);
        }
        
        @Override
        protected String getTransformerName()
        {
            return "StringCompression";
        }
        
        @Override
        protected MethodVisitor createMethodVisitor(MethodVisitor mv, int access, String name, 
                                                  String descriptor, String signature, String[] exceptions)
        {
            return new StringCompressionMethodVisitor(mv, context, currentClassName, name, descriptor);
        }
    }
}