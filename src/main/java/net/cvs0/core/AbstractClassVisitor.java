package net.cvs0.core;

import net.cvs0.context.ObfuscationContext;
import net.cvs0.utils.BytecodeUtils;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public abstract class AbstractClassVisitor extends ClassVisitor
{
    protected final ObfuscationContext context;
    protected String currentClassName;

    public AbstractClassVisitor(ClassVisitor classVisitor, ObfuscationContext context)
    {
        super(Opcodes.ASM9, classVisitor);
        this.context = context;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces)
    {
        this.currentClassName = name;
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions)
    {
        MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
        
        if (!shouldProcessMethod(name, access)) {
            return mv;
        }
        
        return createMethodVisitor(mv, access, name, descriptor, signature, exceptions);
    }

    @Override
    public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value)
    {
        if (!shouldProcessField(name, access)) {
            return super.visitField(access, name, descriptor, signature, value);
        }
        
        return createFieldVisitor(super.visitField(access, name, descriptor, signature, value), 
                                access, name, descriptor, signature, value);
    }

    protected boolean shouldProcessMethod(String name, int access)
    {
        if (BytecodeUtils.isMethodSkippable(name, access)) {
            return false;
        }
        
        if (!context.getConfig().isInPackageScope(currentClassName)) {
            return false;
        }
        
        return !context.getConfig().shouldKeepMethod(currentClassName, name, getDescriptorFromMethodVisitor());
    }

    protected boolean shouldProcessField(String name, int access)
    {
        if (BytecodeUtils.isFieldSkippable(name, access)) {
            return false;
        }
        
        if (!context.getConfig().isInPackageScope(currentClassName)) {
            return false;
        }
        
        return !context.getConfig().shouldKeepField(currentClassName, name);
    }

    protected boolean shouldProcessClass()
    {
        return context.getConfig().isInPackageScope(currentClassName) &&
               !context.getConfig().shouldKeepClass(currentClassName);
    }

    protected void logTransformation(String message)
    {
        if (context.getConfig().isVerbose()) {
            System.out.println("[" + getTransformerName() + "] " + message + " in " + currentClassName);
        }
    }

    protected abstract String getTransformerName();

    protected abstract MethodVisitor createMethodVisitor(MethodVisitor mv, int access, String name, 
                                                       String descriptor, String signature, String[] exceptions);

    protected FieldVisitor createFieldVisitor(FieldVisitor fv, int access, String name, 
                                            String descriptor, String signature, Object value)
    {
        return fv;
    }

    private String getDescriptorFromMethodVisitor()
    {
        return "";
    }
}