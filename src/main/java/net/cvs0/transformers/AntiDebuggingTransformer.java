package net.cvs0.transformers;

import net.cvs0.core.AbstractTransformer;
import net.cvs0.context.ObfuscationContext;
import net.cvs0.utils.AntiDebugger;
import org.objectweb.asm.*;

public class AntiDebuggingTransformer extends AbstractTransformer {
    
    private final AntiDebugger antiDebugger;
    private final AntiDebugger.DebuggerAction action;
    
    public AntiDebuggingTransformer(AntiDebugger.DebuggerAction action) {
        super("Anti-Debugging Transformer", 100);
        this.antiDebugger = new AntiDebugger();
        this.action = action;
    }
    
    @Override
    public void transform(ClassReader reader, ClassWriter writer, ObfuscationContext context) {
        if (!context.getConfig().isAntiDebugging()) {
            return;
        }
        
        try {
            String className = reader.getClassName();
            AntiDebuggingClassVisitor visitor = new AntiDebuggingClassVisitor(writer, className);
            reader.accept(visitor, ClassReader.EXPAND_FRAMES);
        } catch (Exception e) {
            logTransformation("Failed to apply anti-debugging to class: " + reader.getClassName() + " - " + e.getMessage(), context);
        }
    }
    
    private class AntiDebuggingClassVisitor extends ClassVisitor {
        private final String className;
        private boolean hasStaticInit = false;
        private boolean antiDebugInjected = false;
        
        public AntiDebuggingClassVisitor(ClassVisitor cv, String className) {
            super(Opcodes.ASM9, cv);
            this.className = className;
        }
        
        @Override
        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            super.visit(version, access, name, signature, superName, interfaces);
        }
        
        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
            MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
            
            if ("<clinit>".equals(name)) {
                hasStaticInit = true;
                return new StaticInitVisitor(mv);
            } else if ("main".equals(name) && (access & Opcodes.ACC_STATIC) != 0 && "([Ljava/lang/String;)V".equals(descriptor)) {
                return new MainMethodVisitor(mv);
            }
            
            return mv;
        }
        
        @Override
        public void visitEnd() {
            // Inject anti-debug method
            if (!antiDebugInjected) {
                antiDebugger.injectAntiDebugCheck((ClassWriter) cv, className, action);
                antiDebugInjected = true;
            }
            
            // If no static initializer exists, create one
            if (!hasStaticInit) {
                MethodVisitor mv = super.visitMethod(
                    Opcodes.ACC_STATIC,
                    "<clinit>",
                    "()V",
                    null,
                    null
                );
                mv.visitCode();
                injectStaticInitCallWithClassName(mv, className);
                mv.visitInsn(Opcodes.RETURN);
                mv.visitMaxs(2, 0);
                mv.visitEnd();
            }
            
            super.visitEnd();
        }
        
        private class StaticInitVisitor extends MethodVisitor {
            private boolean injected = false;
            
            public StaticInitVisitor(MethodVisitor mv) {
                super(Opcodes.ASM9, mv);
            }
            
            @Override
            public void visitCode() {
                super.visitCode();
                if (!injected) {
                    injectStaticInitCallWithClassName(this, className);
                    injected = true;
                }
            }
        }
        
        private class MainMethodVisitor extends MethodVisitor {
            private boolean injected = false;
            
            public MainMethodVisitor(MethodVisitor mv) {
                super(Opcodes.ASM9, mv);
            }
            
            @Override
            public void visitCode() {
                super.visitCode();
                if (!injected) {
                    // Inject anti-debug check at the beginning of main method
                    super.visitMethodInsn(Opcodes.INVOKESTATIC,
                        className.replace('.', '/'), "checkDebugger", "()V", false);
                    injected = true;
                }
            }
        }
        
        private void injectStaticInitCallWithClassName(MethodVisitor mv, String className) {
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, 
                "java/lang/Thread", "currentThread", "()Ljava/lang/Thread;", false);
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                "java/lang/Thread", "getName", "()Ljava/lang/String;", false);
            mv.visitLdcInsn("main");
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                "java/lang/String", "equals", "(Ljava/lang/Object;)Z", false);
            
            Label skipCheck = new Label();
            mv.visitJumpInsn(Opcodes.IFEQ, skipCheck);
            
            mv.visitMethodInsn(Opcodes.INVOKESTATIC,
                className.replace('.', '/'), "checkDebugger", "()V", false);
            
            mv.visitLabel(skipCheck);
            mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
        }
    }
}