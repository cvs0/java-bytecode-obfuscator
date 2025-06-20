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
    
    public void injectAntiDebugCheck(ClassWriter cw, String className, DebuggerAction action) {
        MethodVisitor mv = cw.visitMethod(
            Opcodes.ACC_PRIVATE + Opcodes.ACC_STATIC,
            "checkDebugger",
            "()V",
            null,
            null
        );
        
        mv.visitCode();
        
        injectSimpleAntiDebugCheck(mv, action);
        
        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(3, 0);
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
    
    private void executeActionSimple(MethodVisitor mv, DebuggerAction action) {
        switch (action) {
            case EXIT_SILENTLY:
                mv.visitInsn(Opcodes.ICONST_0);
                mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/System", "exit", "(I)V", false);
                break;
            case EXIT_WITH_ERROR:
                mv.visitInsn(Opcodes.ICONST_M1);
                mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/System", "exit", "(I)V", false);
                break;
            default:
                break;
        }
    }
}