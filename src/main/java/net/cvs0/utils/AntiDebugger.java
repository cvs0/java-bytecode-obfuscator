package net.cvs0.utils;

import org.objectweb.asm.*;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.List;
import java.util.Random;

public class AntiDebugger {
    
    public enum DebuggerAction {
        EXIT_SILENTLY,
        EXIT_WITH_ERROR,
        CORRUPT_EXECUTION,
        INFINITE_LOOP,
        FAKE_EXECUTION
    }
    
    public enum VMDetectionLevel {
        BASIC,
        COMPREHENSIVE,
        PARANOID
    }
    
    public void injectAntiDebugCheck(ClassWriter cw, String className, DebuggerAction action) {
        injectAntiDebugCheck(cw, className, action, VMDetectionLevel.BASIC);
    }
    
    public void injectAntiDebugCheck(ClassWriter cw, String className, DebuggerAction action, VMDetectionLevel vmLevel) {
        if (cw == null) {
            throw new IllegalArgumentException("ClassWriter cannot be null");
        }
        if (className == null || className.isEmpty()) {
            throw new IllegalArgumentException("Class name cannot be null or empty");
        }
        if (action == null) {
            action = DebuggerAction.EXIT_SILENTLY;
        }
        if (vmLevel == null) {
            vmLevel = VMDetectionLevel.BASIC;
        }
        
        try {
            injectDebuggerDetection(cw, action);
            injectVMDetection(cw, action, vmLevel);
        } catch (Exception e) {
            throw new RuntimeException("Failed to inject anti-debug/VM checks for class " + className, e);
        }
    }
    
    private void injectDebuggerDetection(ClassWriter cw, DebuggerAction action) {
        MethodVisitor mv = cw.visitMethod(
            Opcodes.ACC_PRIVATE + Opcodes.ACC_STATIC,
            "checkDebugger",
            "()V",
            null,
            null
        );
        
        if (mv == null) {
            throw new IllegalStateException("Failed to create debugger check method visitor");
        }
        
        mv.visitCode();
        injectSimpleAntiDebugCheck(mv, action);
        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(3, 0);
        mv.visitEnd();
    }
    
    private void injectVMDetection(ClassWriter cw, DebuggerAction action, VMDetectionLevel level) {
        MethodVisitor mv = cw.visitMethod(
            Opcodes.ACC_PRIVATE + Opcodes.ACC_STATIC,
            "checkVM",
            "()V",
            null,
            null
        );
        
        if (mv == null) {
            throw new IllegalStateException("Failed to create VM check method visitor");
        }
        
        mv.visitCode();
        
        switch (level) {
            case BASIC:
                injectBasicVMDetection(mv, action);
                break;
            case COMPREHENSIVE:
                injectBasicVMDetection(mv, action);
                injectAdvancedVMDetection(mv, action);
                break;
            case PARANOID:
                injectBasicVMDetection(mv, action);
                injectAdvancedVMDetection(mv, action);
                injectParanoidVMDetection(mv, action);
                break;
        }
        
        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(4, 2);
        mv.visitEnd();
    }
    
    private void injectSimpleAntiDebugCheck(MethodVisitor mv, DebuggerAction action) {
        Label continueLabel = new Label();
        
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/management/ManagementFactory", 
            "getRuntimeMXBean", "()Ljava/lang/management/RuntimeMXBean;", false);
        mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, "java/lang/management/RuntimeMXBean",
            "getInputArguments", "()Ljava/util/List;", true);
        mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, "java/util/List",
            "toString", "()Ljava/lang/String;", true);
        
        mv.visitLdcInsn("jdwp");
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String",
            "contains", "(Ljava/lang/CharSequence;)Z", false);
        mv.visitJumpInsn(Opcodes.IFEQ, continueLabel);
        
        executeActionSimple(mv, action);
        
        mv.visitLabel(continueLabel);
        mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
    }
    
    private void injectBasicVMDetection(MethodVisitor mv, DebuggerAction action) {
        Label continueLabel = new Label();
        
        mv.visitLdcInsn("java.vm.name");
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/System", "getProperty", "(Ljava/lang/String;)Ljava/lang/String;", false);
        mv.visitVarInsn(Opcodes.ASTORE, 0);
        
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        Label nullCheck = new Label();
        mv.visitJumpInsn(Opcodes.IFNULL, nullCheck);
        
        checkVMIndicator(mv, 0, "VMware", action, continueLabel);
        checkVMIndicator(mv, 0, "VirtualBox", action, continueLabel);
        checkVMIndicator(mv, 0, "QEMU", action, continueLabel);
        checkVMIndicator(mv, 0, "Xen", action, continueLabel);
        
        mv.visitLabel(nullCheck);
        mv.visitFrame(Opcodes.F_APPEND, 1, new Object[]{"java/lang/String"}, 0, null);
        
        mv.visitLdcInsn("os.name");
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/System", "getProperty", "(Ljava/lang/String;)Ljava/lang/String;", false);
        mv.visitVarInsn(Opcodes.ASTORE, 1);
        
        mv.visitVarInsn(Opcodes.ALOAD, 1);
        Label osNullCheck = new Label();
        mv.visitJumpInsn(Opcodes.IFNULL, osNullCheck);
        
        checkVMIndicator(mv, 1, "VMware", action, continueLabel);
        
        mv.visitLabel(osNullCheck);
        mv.visitFrame(Opcodes.F_APPEND, 1, new Object[]{"java/lang/String"}, 0, null);
        mv.visitLabel(continueLabel);
        mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
    }
    
    private void injectAdvancedVMDetection(MethodVisitor mv, DebuggerAction action) {
        Label continueLabel = new Label();
        
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Runtime", "getRuntime", "()Ljava/lang/Runtime;", false);
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Runtime", "availableProcessors", "()I", false);
        mv.visitInsn(Opcodes.ICONST_2);
        mv.visitJumpInsn(Opcodes.IF_ICMPGE, continueLabel);
        
        executeAction(mv, action);
        
        mv.visitLabel(continueLabel);
        mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
        
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/management/ManagementFactory", "getRuntimeMXBean", "()Ljava/lang/management/RuntimeMXBean;", false);
        mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, "java/lang/management/RuntimeMXBean", "getUptime", "()J", true);
        mv.visitLdcInsn(10000L);
        mv.visitInsn(Opcodes.LCMP);
        Label uptimeOk = new Label();
        mv.visitJumpInsn(Opcodes.IFGE, uptimeOk);
        
        executeAction(mv, action);
        
        mv.visitLabel(uptimeOk);
        mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
    }
    
    private void injectParanoidVMDetection(MethodVisitor mv, DebuggerAction action) {
        Label continueLabel = new Label();
        
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/System", "nanoTime", "()J", false);
        mv.visitVarInsn(Opcodes.LSTORE, 0);
        
        for (int i = 0; i < 1000; i++) {
            mv.visitInsn(Opcodes.NOP);
        }
        
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/System", "nanoTime", "()J", false);
        mv.visitVarInsn(Opcodes.LLOAD, 0);
        mv.visitInsn(Opcodes.LSUB);
        mv.visitLdcInsn(50000L);
        mv.visitInsn(Opcodes.LCMP);
        mv.visitJumpInsn(Opcodes.IFLE, continueLabel);
        
        executeAction(mv, action);
        
        mv.visitLabel(continueLabel);
        mv.visitFrame(Opcodes.F_APPEND, 1, new Object[]{Opcodes.LONG}, 0, null);
        
        mv.visitLdcInsn("user.name");
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/System", "getProperty", "(Ljava/lang/String;)Ljava/lang/String;", false);
        mv.visitVarInsn(Opcodes.ASTORE, 2);
        
        mv.visitVarInsn(Opcodes.ALOAD, 2);
        Label userCheck = new Label();
        mv.visitJumpInsn(Opcodes.IFNULL, userCheck);
        
        checkVMIndicator(mv, 2, "sandbox", action, userCheck);
        checkVMIndicator(mv, 2, "malware", action, userCheck);
        checkVMIndicator(mv, 2, "virus", action, userCheck);
        checkVMIndicator(mv, 2, "test", action, userCheck);
        
        mv.visitLabel(userCheck);
        mv.visitFrame(Opcodes.F_APPEND, 1, new Object[]{"java/lang/String"}, 0, null);
        
        mv.visitLdcInsn("java.home");
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/System", "getProperty", "(Ljava/lang/String;)Ljava/lang/String;", false);
        mv.visitVarInsn(Opcodes.ASTORE, 3);
        
        mv.visitVarInsn(Opcodes.ALOAD, 3);
        Label homeCheck = new Label();
        mv.visitJumpInsn(Opcodes.IFNULL, homeCheck);
        
        checkVMIndicator(mv, 3, "sandbox", action, homeCheck);
        checkVMIndicator(mv, 3, "vm", action, homeCheck);
        
        mv.visitLabel(homeCheck);
        mv.visitFrame(Opcodes.F_APPEND, 1, new Object[]{"java/lang/String"}, 0, null);
    }
    
    private void checkVMIndicator(MethodVisitor mv, int varIndex, String indicator, DebuggerAction action, Label continueLabel) {
        mv.visitVarInsn(Opcodes.ALOAD, varIndex);
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "toLowerCase", "()Ljava/lang/String;", false);
        mv.visitLdcInsn(indicator.toLowerCase());
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "contains", "(Ljava/lang/CharSequence;)Z", false);
        mv.visitJumpInsn(Opcodes.IFEQ, continueLabel);
        
        executeAction(mv, action);
    }
    
    private void executeActionSimple(MethodVisitor mv, DebuggerAction action) {
        executeAction(mv, action);
    }
    
    private void executeAction(MethodVisitor mv, DebuggerAction action) {
        switch (action) {
            case EXIT_SILENTLY:
                mv.visitInsn(Opcodes.ICONST_0);
                mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/System", "exit", "(I)V", false);
                break;
            case EXIT_WITH_ERROR:
                mv.visitInsn(Opcodes.ICONST_M1);
                mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/System", "exit", "(I)V", false);
                break;
            case CORRUPT_EXECUTION:
                mv.visitTypeInsn(Opcodes.NEW, "java/lang/RuntimeException");
                mv.visitInsn(Opcodes.DUP);
                mv.visitLdcInsn("System integrity check failed");
                mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/RuntimeException", "<init>", "(Ljava/lang/String;)V", false);
                mv.visitInsn(Opcodes.ATHROW);
                break;
            case INFINITE_LOOP:
                Label loopStart = new Label();
                mv.visitLabel(loopStart);
                mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
                mv.visitLdcInsn(1L);
                mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Thread", "sleep", "(J)V", false);
                mv.visitJumpInsn(Opcodes.GOTO, loopStart);
                break;
            case FAKE_EXECUTION:
                mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
                mv.visitLdcInsn("Application terminated normally");
                mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
                mv.visitInsn(Opcodes.ICONST_0);
                mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/System", "exit", "(I)V", false);
                break;
            default:
                break;
        }
    }
    
    public void injectVMDetectionInitializer(ClassWriter cw, VMDetectionLevel level) {
        MethodVisitor mv = cw.visitMethod(
            Opcodes.ACC_STATIC,
            "<clinit>",
            "()V",
            null,
            null
        );
        
        mv.visitCode();
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, cw.toString(), "checkVM", "()V", false);
        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }
}