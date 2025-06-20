package net.cvs0.core;

import net.cvs0.context.ObfuscationContext;
import net.cvs0.utils.BytecodeUtils;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.Random;

public abstract class AbstractMethodVisitor extends MethodVisitor
{
    protected final ObfuscationContext context;
    protected final String className;
    protected final String methodName;
    protected final String methodDescriptor;
    protected final Random random;

    public AbstractMethodVisitor(MethodVisitor methodVisitor, ObfuscationContext context, 
                               String className, String methodName, String methodDescriptor)
    {
        super(Opcodes.ASM9, methodVisitor);
        this.context = context;
        this.className = className;
        this.methodName = methodName;
        this.methodDescriptor = methodDescriptor;
        this.random = new Random();
    }

    public AbstractMethodVisitor(MethodVisitor methodVisitor, ObfuscationContext context, 
                               String className, String methodName, String methodDescriptor, long seed)
    {
        super(Opcodes.ASM9, methodVisitor);
        this.context = context;
        this.className = className;
        this.methodName = methodName;
        this.methodDescriptor = methodDescriptor;
        this.random = new Random(seed);
    }

    protected boolean shouldProcessInstruction()
    {
        return context.getConfig().isInPackageScope(className);
    }

    protected boolean shouldSkipMethod()
    {
        return BytecodeUtils.isMethodSkippable(methodName, 0);
    }

    protected void logTransformation(String message)
    {
        if (context.getConfig().isVerbose()) {
            System.out.println("[" + getTransformerName() + "] " + message + " in " + className + "." + methodName);
        }
    }

    protected abstract String getTransformerName();

    protected boolean isInPackageScope()
    {
        return context.getConfig().isInPackageScope(className);
    }

    protected boolean shouldKeepMethod()
    {
        return context.getConfig().shouldKeepMethod(className, methodName, methodDescriptor);
    }

    protected String getMethodSignature()
    {
        return className + "." + methodName + methodDescriptor;
    }

    protected boolean shouldProcessWithProbability(float probability)
    {
        return random.nextFloat() < probability;
    }

    protected void emitNOP()
    {
        super.visitInsn(Opcodes.NOP);
    }

    protected void emitGarbage()
    {
        if (random.nextBoolean()) {
            BytecodeUtils.pushInteger(super.mv, random.nextInt(1000));
            super.visitInsn(Opcodes.POP);
        } else {
            emitNOP();
        }
    }
}