package net.cvs0.transformers;

import net.cvs0.context.ObfuscationContext;
import net.cvs0.core.AbstractTransformer;
import net.cvs0.mappings.remappers.MapBasedRenamer;
import net.cvs0.utils.Logger;
import org.objectweb.asm.*;

public class MethodRenameTransformer extends AbstractTransformer
 {
    public MethodRenameTransformer()
    {
        super("MethodRename", 300);
    }
    
    @Override
    public void transform(ClassReader reader, ClassWriter writer, ObfuscationContext context)
    {
        if (!context.getConfig().isRenameMethods()) {
            reader.accept(writer, 0);
            return;
        }
        
        ClassVisitor visitor = new MethodRenameVisitor(writer, context);
        reader.accept(visitor, 0);
    }
    
    @Override
    public boolean isEnabled(ObfuscationContext context)
    {
        return context.getConfig().isRenameMethods();
    }
    
    private class MethodRenameVisitor extends ClassVisitor
    {
        private final ObfuscationContext context;
        private String currentClassName;
        
        public MethodRenameVisitor(ClassVisitor classVisitor, ObfuscationContext context)
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
            if (shouldSkipMethod(name, access)) {
                MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
                return new MethodCallRenameVisitor(mv, context, currentClassName);
            }
            
            if (!context.getConfig().isInPackageScope(currentClassName)) {
                MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
                return new MethodCallRenameVisitor(mv, context, currentClassName);
            }
            
            String methodKey = currentClassName + "." + name + descriptor;
            
            if (context.getConfig().shouldKeepMethod(currentClassName, name, descriptor)) {
                Logger.debug("Keeping method: " + methodKey);
                MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
                return new MethodCallRenameVisitor(mv, context, currentClassName);
            }
            
            String newName = context.getMethodMappings().get(methodKey);
            if (newName == null) {
                MapBasedRenamer renamer = context.getRenamer("method");
                newName = renamer.generateName(methodKey);
                context.addMethodMapping(methodKey, newName);
                Logger.mapping(methodKey, newName);
            }
            
            MethodVisitor mv = super.visitMethod(access, newName, descriptor, signature, exceptions);
            return new MethodCallRenameVisitor(mv, context, currentClassName);
        }
        
        private boolean shouldSkipMethod(String name, int access)
        {
            if (name.equals("<init>") || name.equals("<clinit>")) {
                return true;
            }
            
            if (name.startsWith("lambda$")) {
                return true;
            }
            
            if ((access & Opcodes.ACC_SYNTHETIC) != 0) {
                return true;
            }
            
            if ((access & Opcodes.ACC_BRIDGE) != 0) {
                return true;
            }
            
            if (name.equals("main") && name.contains("([Ljava/lang/String;)V")) {
                return true;
            }
            
            return false;
        }
    }
    
    private static class MethodCallRenameVisitor extends MethodVisitor
    {
        private final ObfuscationContext context;
        private final String currentClassName;
        
        public MethodCallRenameVisitor(MethodVisitor methodVisitor, ObfuscationContext context, String currentClassName)
        {
            super(Opcodes.ASM9, methodVisitor);
            this.context = context;
            this.currentClassName = currentClassName;
        }
        
        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface)
        {
            String mappedOwner = context.getClassMappings().getOrDefault(owner, owner);
            
            if (shouldRenameMethodCall(name, owner)) {
                String methodKey = owner + "." + name + descriptor;
                String newName = context.getMethodMappings().getOrDefault(methodKey, name);
                super.visitMethodInsn(opcode, mappedOwner, newName, descriptor, isInterface);
            } else {
                super.visitMethodInsn(opcode, mappedOwner, name, descriptor, isInterface);
            }
        }
        
        @Override
        public void visitInvokeDynamicInsn(String name, String descriptor, Handle bootstrapMethodHandle, Object... bootstrapMethodArguments)
        {
            Object[] newArgs = new Object[bootstrapMethodArguments.length];
            
            for (int i = 0; i < bootstrapMethodArguments.length; i++) {
                Object arg = bootstrapMethodArguments[i];
                if (arg instanceof Handle) {
                    Handle handle = (Handle) arg;
                    String owner = handle.getOwner();
                    String methodName = handle.getName();
                    String methodDesc = handle.getDesc();
                    
                    if (shouldRenameMethodCall(methodName, owner)) {
                        String methodKey = owner + "." + methodName + methodDesc;
                        String newMethodName = context.getMethodMappings().getOrDefault(methodKey, methodName);
                        String mappedOwner = context.getClassMappings().getOrDefault(owner, owner);
                        
                        newArgs[i] = new Handle(handle.getTag(), mappedOwner, newMethodName, methodDesc, handle.isInterface());
                    } else {
                        String mappedOwner = context.getClassMappings().getOrDefault(owner, owner);
                        newArgs[i] = new Handle(handle.getTag(), mappedOwner, methodName, methodDesc, handle.isInterface());
                    }
                } else {
                    newArgs[i] = arg;
                }
            }
            
            super.visitInvokeDynamicInsn(name, descriptor, bootstrapMethodHandle, newArgs);
        }
        
        private boolean shouldRenameMethodCall(String name, String owner)
        {
            if (name.equals("<init>") || name.equals("<clinit>")) {
                return false;
            }
            
            if (name.startsWith("lambda$")) {
                return false;
            }
            
            if (!context.getConfig().isInPackageScope(owner)) {
                return false;
            }
            
            return true;
        }
    }
}
