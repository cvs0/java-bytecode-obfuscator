package net.cvs0.transformers;

import net.cvs0.context.ObfuscationContext;
import net.cvs0.core.AbstractTransformer;
import net.cvs0.utils.BytecodeTransformationFactory;
import net.cvs0.utils.InstructionObfuscator;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

public class ExampleCustomTransformer extends AbstractTransformer
{
    public ExampleCustomTransformer()
    {
        super("ExampleCustom", 100);
    }
    
    @Override
    public void transform(ClassReader reader, ClassWriter writer, ObfuscationContext context)
    {
        ClassVisitor constantObfuscator = BytecodeTransformationFactory.createConstantObfuscator(
            writer, context, 0.15f);
        
        ClassVisitor garbageInjector = BytecodeTransformationFactory.createGarbageInjector(
            constantObfuscator, context, 0.05f);
        
        ClassVisitor complexObfuscator = BytecodeTransformationFactory.createInstructionObfuscator(
            garbageInjector, context, InstructionObfuscator.ObfuscationStrategy.MIXED, 0.1f);
        
        reader.accept(complexObfuscator, 0);
    }
    
    @Override
    public boolean isEnabled(ObfuscationContext context)
    {
        return context.getConfig().isObfuscateConditions();
    }
}