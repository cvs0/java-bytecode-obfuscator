package net.cvs0.core;

import net.cvs0.context.ObfuscationContext;
import net.cvs0.utils.BytecodeUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Opcodes;

public abstract class UniversalTransformer extends AbstractTransformer
{
    protected UniversalTransformer(String name, int priority)
    {
        super(name, priority);
    }

    @Override
    public void transform(ClassReader reader, ClassWriter writer, ObfuscationContext context)
    {
        if (!shouldTransform(context, reader.getClassName())) {
            reader.accept(writer, 0);
            return;
        }
        
        UniversalClassVisitor visitor = createClassVisitor(writer, context);
        reader.accept(visitor, 0);
    }

    protected boolean shouldTransform(ObfuscationContext context, String className)
    {
        return context.getConfig().isInPackageScope(className);
    }

    protected abstract UniversalClassVisitor createClassVisitor(ClassWriter writer, ObfuscationContext context);

    protected static abstract class UniversalClassVisitor extends ClassVisitor implements ContextProvider
    {
        protected final ObfuscationContext context;
        private String currentClassName;

        protected UniversalClassVisitor(ClassVisitor classVisitor, ObfuscationContext context)
        {
            super(Opcodes.ASM9, classVisitor);
            this.context = context;
        }

        @Override
        public ObfuscationContext getContext()
        {
            return context;
        }

        @Override
        public String getCurrentClassName()
        {
            return currentClassName;
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
            
            if (!shouldProcessMethod(name, access, descriptor)) {
                return mv;
            }
            
            return createMethodVisitor(mv, access, name, descriptor);
        }

        @Override
        public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value)
        {
            if (!shouldProcessField(name, access, descriptor)) {
                return super.visitField(access, name, descriptor, signature, value);
            }
            
            return createFieldVisitor(super.visitField(access, name, descriptor, signature, value), 
                                    access, name, descriptor);
        }

        protected boolean shouldProcessMethod(String name, int access, String descriptor)
        {
            if (BytecodeUtils.isMethodSkippable(name, access)) {
                return false;
            }
            
            if (!context.getConfig().isInPackageScope(currentClassName)) {
                return false;
            }
            
            return !context.getConfig().shouldKeepMethod(currentClassName, name, descriptor);
        }

        protected boolean shouldProcessField(String name, int access, String descriptor)
        {
            if (BytecodeUtils.isFieldSkippable(name, access)) {
                return false;
            }
            
            if (!context.getConfig().isInPackageScope(currentClassName)) {
                return false;
            }
            
            return !context.getConfig().shouldKeepField(currentClassName, name);
        }

        protected void logTransformation(String message)
        {
            if (context.getConfig().isVerbose()) {
                System.out.println("[" + getTransformerName() + "] " + message + " in " + currentClassName);
            }
        }

        protected abstract String getTransformerName();
        
        protected MethodVisitor createMethodVisitor(MethodVisitor mv, int access, String name, String descriptor)
        {
            return mv;
        }

        protected FieldVisitor createFieldVisitor(FieldVisitor fv, int access, String name, String descriptor)
        {
            return fv;
        }
    }

    public static abstract class UniversalMethodVisitor extends MethodVisitor implements ContextProvider
    {
        private final ContextProvider contextProvider;
        private final String methodName;
        private final String methodDescriptor;

        protected UniversalMethodVisitor(MethodVisitor methodVisitor, ContextProvider contextProvider, 
                                       String methodName, String methodDescriptor)
        {
            super(Opcodes.ASM9, methodVisitor);
            this.contextProvider = contextProvider;
            this.methodName = methodName;
            this.methodDescriptor = methodDescriptor;
        }

        @Override
        public ObfuscationContext getContext()
        {
            return contextProvider.getContext();
        }

        @Override
        public String getCurrentClassName()
        {
            return contextProvider.getCurrentClassName();
        }

        protected String getMethodName()
        {
            return methodName;
        }

        protected String getMethodDescriptor()
        {
            return methodDescriptor;
        }

        protected void logTransformation(String message)
        {
            if (getContext().getConfig().isVerbose()) {
                System.out.println("[" + getTransformerName() + "] " + message + " in " + getCurrentClassName() + "." + methodName);
            }
        }

        protected abstract String getTransformerName();
    }
}