package net.cvs0.transformers;

import net.cvs0.core.AbstractTransformer;
import net.cvs0.context.ObfuscationContext;
import net.cvs0.utils.AntiDebugger;
import org.objectweb.asm.*;

public class AntiDebuggingTransformer extends AbstractTransformer {
    
    private final AntiDebugger antiDebugger;
    private final AntiDebugger.DebuggerAction action;
    private final AntiDebugger.VMDetectionLevel vmLevel;
    
    public AntiDebuggingTransformer(AntiDebugger.DebuggerAction action) {
        this(action, AntiDebugger.VMDetectionLevel.BASIC);
    }
    
    public AntiDebuggingTransformer(AntiDebugger.DebuggerAction action, AntiDebugger.VMDetectionLevel vmLevel) {
        super("Anti-Debugging Transformer", 100);
        this.antiDebugger = new AntiDebugger();
        this.action = action != null ? action : AntiDebugger.DebuggerAction.EXIT_SILENTLY;
        this.vmLevel = vmLevel != null ? vmLevel : AntiDebugger.VMDetectionLevel.BASIC;
    }
    
    @Override
    public void transform(ClassReader reader, ClassWriter writer, ObfuscationContext context) {
        if (!context.getConfig().isAntiDebugging() && !context.getConfig().isVmDetection()) {
            return;
        }
        
        try {
            String className = reader.getClassName();
            AntiDebugger.DebuggerAction configAction = context.getConfig().getDebuggerAction();
            AntiDebugger.VMDetectionLevel configVmLevel = context.getConfig().getVmDetectionLevel();
            
            AntiDebuggingClassVisitor visitor = new AntiDebuggingClassVisitor(
                writer, className, 
                configAction != null ? configAction : this.action,
                configVmLevel != null ? configVmLevel : this.vmLevel,
                context.getConfig().isAntiDebugging(),
                context.getConfig().isVmDetection()
            );
            reader.accept(visitor, ClassReader.EXPAND_FRAMES);
        } catch (Exception e) {
            logTransformation("Failed to apply anti-debugging/VM detection to class: " + reader.getClassName() + " - " + e.getMessage(), context);
        }
    }
    
    private class AntiDebuggingClassVisitor extends ClassVisitor {
        private final String className;
        private final AntiDebugger.DebuggerAction debuggerAction;
        private final AntiDebugger.VMDetectionLevel vmDetectionLevel;
        private final boolean antiDebuggingEnabled;
        private final boolean vmDetectionEnabled;
        private boolean hasStaticInit = false;
        private boolean antiDebugInjected = false;
        
        public AntiDebuggingClassVisitor(ClassVisitor cv, String className, 
                                       AntiDebugger.DebuggerAction debuggerAction,
                                       AntiDebugger.VMDetectionLevel vmDetectionLevel,
                                       boolean antiDebuggingEnabled,
                                       boolean vmDetectionEnabled) {
            super(Opcodes.ASM9, cv);
            this.className = className;
            this.debuggerAction = debuggerAction;
            this.vmDetectionLevel = vmDetectionLevel;
            this.antiDebuggingEnabled = antiDebuggingEnabled;
            this.vmDetectionEnabled = vmDetectionEnabled;
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
            // Inject anti-debug and VM detection methods
            if (!antiDebugInjected && (antiDebuggingEnabled || vmDetectionEnabled)) {
                if (vmDetectionEnabled) {
                    antiDebugger.injectAntiDebugCheck((ClassWriter) cv, className, debuggerAction, vmDetectionLevel);
                } else if (antiDebuggingEnabled) {
                    antiDebugger.injectAntiDebugCheck((ClassWriter) cv, className, debuggerAction);
                }
                antiDebugInjected = true;
            }
            
            // If no static initializer exists, create one
            if (!hasStaticInit && (antiDebuggingEnabled || vmDetectionEnabled)) {
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
                    // Inject anti-debug and VM checks at the beginning of main method
                    if (antiDebuggingEnabled) {
                        super.visitMethodInsn(Opcodes.INVOKESTATIC,
                            className.replace('.', '/'), "checkDebugger", "()V", false);
                    }
                    if (vmDetectionEnabled) {
                        super.visitMethodInsn(Opcodes.INVOKESTATIC,
                            className.replace('.', '/'), "checkVM", "()V", false);
                    }
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
            
            if (antiDebuggingEnabled) {
                mv.visitMethodInsn(Opcodes.INVOKESTATIC,
                    className.replace('.', '/'), "checkDebugger", "()V", false);
            }
            if (vmDetectionEnabled) {
                mv.visitMethodInsn(Opcodes.INVOKESTATIC,
                    className.replace('.', '/'), "checkVM", "()V", false);
            }
            
            mv.visitLabel(skipCheck);
            mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
        }
    }
}