package net.cvs0.transformers;

import net.cvs0.context.ObfuscationContext;
import net.cvs0.core.AbstractTransformer;
import org.objectweb.asm.*;

import java.util.Random;

public class ConditionObfuscationTransformer extends AbstractTransformer
{
    
    public ConditionObfuscationTransformer()
    {
        super("ConditionObfuscation", 250);
    }
    
    @Override
    public void transform(ClassReader reader, ClassWriter writer, ObfuscationContext context)
    {
        ClassVisitor visitor = new ConditionObfuscationVisitor(writer, context);
        reader.accept(visitor, 0);
    }
    
    @Override
    public boolean isEnabled(ObfuscationContext context)
    {
        return context.getConfig().isObfuscateConditions();
    }
    
    private class ConditionObfuscationVisitor extends ClassVisitor
    {
        private final ObfuscationContext context;
        private String currentClassName;
        
        public ConditionObfuscationVisitor(ClassVisitor classVisitor, ObfuscationContext context)
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
            
            return new ConditionObfuscationMethodVisitor(mv, context, currentClassName, name, descriptor);
        }
    }
    
    private class ConditionObfuscationMethodVisitor extends MethodVisitor
    {
        private final ObfuscationContext context;
        private final String className;
        private final String methodName;
        private final String methodDescriptor;
        private final Random methodRandom = new Random();
        
        public ConditionObfuscationMethodVisitor(MethodVisitor methodVisitor, ObfuscationContext context, 
                                               String className, String methodName, String methodDescriptor)
        {
            super(Opcodes.ASM9, methodVisitor);
            this.context = context;
            this.className = className;
            this.methodName = methodName;
            this.methodDescriptor = methodDescriptor;
        }
        
        @Override
        public void visitInsn(int opcode)
        {
            if (shouldObfuscateConstant(opcode) && methodRandom.nextFloat() < 0.1f) {
                obfuscateConstant(opcode);
                logTransformation("Obfuscated constant in " + className + "." + methodName, context);
            } else {
                super.visitInsn(opcode);
            }
        }
        
        private boolean shouldObfuscateConstant(int opcode)
        {
            return opcode == Opcodes.ICONST_1 || opcode == Opcodes.ICONST_0;
        }
        
        private void obfuscateConstant(int opcode)
        {
            switch (opcode) {
                case Opcodes.ICONST_1:
                    int strategy1 = methodRandom.nextInt(3);
                    switch (strategy1) {
                        case 0:
                            super.visitLdcInsn(2);
                            super.visitInsn(Opcodes.ICONST_1);
                            super.visitInsn(Opcodes.ISUB);
                            break;
                        case 1:
                            super.visitLdcInsn(3);
                            super.visitLdcInsn(2);
                            super.visitInsn(Opcodes.ISUB);
                            break;
                        default:
                            super.visitInsn(Opcodes.ICONST_1);
                            break;
                    }
                    break;
                    
                case Opcodes.ICONST_0:
                    int strategy0 = methodRandom.nextInt(3);
                    switch (strategy0) {
                        case 0:
                            super.visitInsn(Opcodes.ICONST_1);
                            super.visitInsn(Opcodes.ICONST_1);
                            super.visitInsn(Opcodes.ISUB);
                            break;
                        case 1:
                            super.visitLdcInsn(5);
                            super.visitLdcInsn(5);
                            super.visitInsn(Opcodes.ISUB);
                            break;
                        default:
                            super.visitInsn(Opcodes.ICONST_0);
                            break;
                    }
                    break;
                    
                default:
                    super.visitInsn(opcode);
                    break;
            }
        }
    }
}