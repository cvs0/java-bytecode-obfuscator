package net.cvs0.transformers;

import net.cvs0.context.ObfuscationContext;
import net.cvs0.core.AbstractTransformer;
import net.cvs0.mappings.remappers.MapBasedRenamer;
import org.objectweb.asm.*;

public class LocalVariableRenameTransformer extends AbstractTransformer
{
    public LocalVariableRenameTransformer()
    {
        super("LocalVariableRename", 400);
    }
    
    @Override
    public void transform(ClassReader reader, ClassWriter writer, ObfuscationContext context)
    {
        ClassVisitor visitor = new LocalVariableRenameVisitor(writer, context);
        reader.accept(visitor, 0);
    }
    
    @Override
    public boolean isEnabled(ObfuscationContext context)
    {
        return context.getConfig().isRenameLocalVariables();
    }
    
    private class LocalVariableRenameVisitor extends ClassVisitor
    {
        private final ObfuscationContext context;
        private String currentClassName;
        
        public LocalVariableRenameVisitor(ClassVisitor classVisitor, ObfuscationContext context)
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
            
            if (!context.getConfig().isInPackageScope(currentClassName)) {
                return mv;
            }
            
            return new LocalVariableMethodVisitor(mv, context, currentClassName, name, descriptor);
        }
    }
    
    private class LocalVariableMethodVisitor extends MethodVisitor
    {
        private final ObfuscationContext context;
        private final String className;
        private final String methodName;
        private final String methodDescriptor;
        private final MapBasedRenamer renamer;
        
        public LocalVariableMethodVisitor(MethodVisitor methodVisitor, ObfuscationContext context, 
                                        String className, String methodName, String methodDescriptor)
        {
            super(Opcodes.ASM9, methodVisitor);
            this.context = context;
            this.className = className;
            this.methodName = methodName;
            this.methodDescriptor = methodDescriptor;
            this.renamer = context.getRenamer("localvar");
        }
        
        @Override
        public void visitLocalVariable(String name, String descriptor, String signature, Label start, Label end, int index)
        {
            if (shouldRenameLocalVariable(name, index)) {
                String variableKey = className + "." + methodName + methodDescriptor + "." + name + "." + index;
                String newName = renamer.generateName(variableKey);
                
                logTransformation("Renaming local variable: " + name + " -> " + newName + " (index: " + index + ")", context);
                super.visitLocalVariable(newName, descriptor, signature, start, end, index);
            } else {
                super.visitLocalVariable(name, descriptor, signature, start, end, index);
            }
        }
        
        @Override
        public void visitParameter(String name, int access)
        {
            if (name != null && shouldRenameParameter(name)) {
                String parameterKey = className + "." + methodName + methodDescriptor + ".param." + name;
                String newName = renamer.generateName(parameterKey);
                
                logTransformation("Renaming parameter: " + name + " -> " + newName, context);
                super.visitParameter(newName, access);
            } else {
                super.visitParameter(name, access);
            }
        }
        
        private boolean shouldRenameLocalVariable(String name, int index)
        {
            if (name == null || name.isEmpty()) {
                return false;
            }
            
            if (name.equals("this")) {
                return false;
            }
            
            if (name.startsWith("$")) {
                return false;
            }
            
            return true;
        }
        
        private boolean shouldRenameParameter(String name)
        {
            if (name == null || name.isEmpty()) {
                return false;
            }
            
            if (name.equals("this")) {
                return false;
            }
            
            if (name.startsWith("$")) {
                return false;
            }
            
            return true;
        }
    }
}