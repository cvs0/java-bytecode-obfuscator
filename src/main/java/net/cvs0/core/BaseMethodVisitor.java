package net.cvs0.core;

import net.cvs0.context.ObfuscationContext;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public abstract class BaseMethodVisitor extends MethodVisitor implements ContextProvider
{
    private final ContextProvider contextProvider;
    private final String methodName;
    private final String methodDescriptor;
    private final int methodAccess;

    protected BaseMethodVisitor(MethodVisitor methodVisitor, ContextProvider contextProvider, 
                              int access, String methodName, String methodDescriptor)
    {
        super(Opcodes.ASM9, methodVisitor);
        this.contextProvider = contextProvider;
        this.methodAccess = access;
        this.methodName = methodName;
        this.methodDescriptor = methodDescriptor;
    }

    @Override
    public final ObfuscationContext getContext()
    {
        return contextProvider.getContext();
    }

    @Override
    public final String getCurrentClassName()
    {
        return contextProvider.getCurrentClassName();
    }

    protected final String getMethodName()
    {
        return methodName;
    }

    protected final String getMethodDescriptor()
    {
        return methodDescriptor;
    }

    protected final int getMethodAccess()
    {
        return methodAccess;
    }

    protected final String getMethodKey()
    {
        return getCurrentClassName() + "." + methodName + methodDescriptor;
    }

    protected void logTransformation(String message)
    {
        if (getContext().getConfig().isVerbose()) {
            System.out.println("[" + getTransformerName() + "] " + message + " in " + getCurrentClassName() + "." + methodName);
        }
    }

    protected abstract String getTransformerName();
}