package net.cvs0.transformers;

import net.cvs0.context.ObfuscationContext;
import net.cvs0.core.BaseClassVisitor;
import net.cvs0.core.RenameTransformer;
import net.cvs0.utils.Logger;
import net.cvs0.mappings.remappers.MapBasedRenamer;
import org.objectweb.asm.*;

public class MethodRenameTransformer extends RenameTransformer
{
    public MethodRenameTransformer()
    {
        super("MethodRename", 300, "method");
    }
    
    @Override
    public boolean isEnabled(ObfuscationContext context)
    {
        return context.getConfig().isRenameMethods();
    }

    @Override
    protected BaseClassVisitor createClassVisitor(org.objectweb.asm.ClassWriter writer, ObfuscationContext context, String className)
    {
        return new MethodRenameVisitor(writer, context);
    }
    
    private class MethodRenameVisitor extends BaseClassVisitor
    {
        public MethodRenameVisitor(ClassVisitor classVisitor, ObfuscationContext context)
        {
            super(classVisitor, context);
        }
        
        @Override
        protected String getTransformerName()
        {
            return "MethodRename";
        }
        
        @Override
        protected MethodVisitor createMethodVisitor(MethodVisitor mv, int access, String name, 
                                                  String descriptor, String signature, String[] exceptions)
        {
            String methodKey = currentClassName + "." + name + descriptor;
            
            String newName = context.getMethodMappings().get(methodKey);
            if (newName == null) {
                if (context.getMappingManager() != null) {
                    context.getMappingManager().generateMethodMapping(currentClassName, name, descriptor);
                    newName = context.getMappingManager().getMethodMapping(currentClassName, name, descriptor);
                    context.addMethodMapping(methodKey, newName);
                } else {
                    MapBasedRenamer renamer = context.getRenamer("method");
                    newName = renamer.generateName(methodKey);
                    context.addMethodMapping(methodKey, newName);
                }
                Logger.mapping(methodKey, newName);
            }
            
            MethodVisitor renamedMv = cv.visitMethod(access, newName, descriptor, signature, exceptions);
            return new MethodCallRenameVisitor(renamedMv, context, currentClassName);
        }
        
        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions)
        {
            if (!shouldProcessMethod(name, access, descriptor)) {
                MethodVisitor mv = cv.visitMethod(access, name, descriptor, signature, exceptions);
                return new MethodCallRenameVisitor(mv, context, currentClassName);
            }
            
            return createMethodVisitor(null, access, name, descriptor, signature, exceptions);
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
            String mappedOwner = owner;
            if (context.getMappingManager() != null) {
                String classMapping = context.getMappingManager().getClassMapping(owner);
                if (classMapping != null) {
                    mappedOwner = classMapping;
                }
            }
            
            if (shouldRenameMethodCall(name, owner)) {
                String newName = name;
                if (context.getMappingManager() != null) {
                    String methodMapping = context.getMappingManager().getMethodMapping(owner, name, descriptor);
                    if (methodMapping != null) {
                        newName = methodMapping;
                    }
                }
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
                    
                    String mappedOwner = owner;
                    if (context.getMappingManager() != null) {
                        String classMapping = context.getMappingManager().getClassMapping(owner);
                        if (classMapping != null) {
                            mappedOwner = classMapping;
                        }
                    }
                    
                    if (shouldRenameMethodCall(methodName, owner)) {
                        String newMethodName = methodName;
                        if (context.getMappingManager() != null) {
                            String methodMapping = context.getMappingManager().getMethodMapping(owner, methodName, methodDesc);
                            if (methodMapping != null) {
                                newMethodName = methodMapping;
                            }
                        }
                        newArgs[i] = new Handle(handle.getTag(), mappedOwner, newMethodName, methodDesc, handle.isInterface());
                    } else {
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
            if (name != null && (name.equals("<init>") || name.equals("<clinit>"))) {
                return false;
            }
            
            if (name != null && name.startsWith("lambda$")) {
                return false;
            }
            
            if (name != null && (name.equals("values") || name.equals("valueOf") || name.equals("$values"))) {
                return false;
            }
            
            if (!context.getConfig().isInPackageScope(owner)) {
                return false;
            }
            
            return true;
        }
    }
}
