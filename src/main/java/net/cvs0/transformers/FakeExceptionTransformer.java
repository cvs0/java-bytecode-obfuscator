package net.cvs0.transformers;

import net.cvs0.context.ObfuscationContext;
import net.cvs0.core.BaseTransformer;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

import java.util.*;

public class FakeExceptionTransformer extends BaseTransformer
{
    private static final String[] EXCEPTION_TYPES = {
        "java/lang/IllegalArgumentException",
        "java/lang/IllegalStateException", 
        "java/lang/RuntimeException",
        "java/lang/SecurityException",
        "java/lang/UnsupportedOperationException"
    };
    
    private static final String[] EXCEPTION_MESSAGES = {
        "Invalid operation",
        "Illegal state",
        "Security violation",
        "Access denied",
        "Operation not supported",
        "Invalid parameter",
        "System error"
    };
    
    private final Random random = new Random();
    
    public FakeExceptionTransformer()
    {
        super("FakeException", 25);
    }
    
    @Override
    public boolean isEnabled(ObfuscationContext context)
    {
        return context.getConfig().isInsertFakeExceptions();
    }
    
    @Override
    protected boolean shouldSkipClass(String className, ObfuscationContext context)
    {
        if (super.shouldSkipClass(className, context)) {
            return true;
        }
        
        return className.startsWith("java/") || 
               className.startsWith("javax/") || 
               className.startsWith("sun/") ||
               className.startsWith("com/sun/") ||
               className.contains("$Lambda$") ||
               className.contains("$$");
    }
    
    @Override
    protected void performTransformation(ClassReader reader, ClassWriter writer, 
                                       ObfuscationContext context, String className)
    {
        try {
            ClassNode classNode = new ClassNode();
            reader.accept(classNode, ClassReader.EXPAND_FRAMES);
            
            if ((classNode.access & Opcodes.ACC_INTERFACE) != 0 || 
                (classNode.access & Opcodes.ACC_ANNOTATION) != 0) {
                copyClass(reader, writer);
                return;
            }
            
            boolean modified = false;
            
            if (classNode.methods != null) {
                for (MethodNode method : classNode.methods) {
                    if (shouldProcessMethod(method)) {
                        if (insertFakeExceptions(method, context)) {
                            modified = true;
                        }
                    }
                }
            }
            
            if (modified) {
                logTransformation("Inserted fake exception checks in class: " + className, context);
                classNode.accept(writer);
            } else {
                copyClass(reader, writer);
            }
        } catch (Exception e) {
            if (context.getConfig().isVerbose()) {
                System.err.println("[FakeException] Error processing class " + className + ": " + e.getMessage());
            }
            copyClass(reader, writer);
        }
    }
    
    private boolean shouldProcessMethod(MethodNode method)
    {
        if (method.name.equals("<init>") || method.name.equals("<clinit>")) {
            return false;
        }
        
        if ((method.access & Opcodes.ACC_ABSTRACT) != 0 ||
            (method.access & Opcodes.ACC_NATIVE) != 0) {
            return false;
        }
        
        if (method.instructions == null || method.instructions.size() < 5) {
            return false;
        }
        
        return true;
    }
    
    private boolean insertFakeExceptions(MethodNode method, ObfuscationContext context)
    {
        try {
            InsnList instructions = method.instructions;
            List<AbstractInsnNode> insertionPoints = findInsertionPoints(instructions);
            
            if (insertionPoints.isEmpty()) {
                return false;
            }
            
            int insertionCount = Math.min(random.nextInt(3) + 1, insertionPoints.size());
            Collections.shuffle(insertionPoints, random);
            
            boolean modified = false;
            
            for (int i = 0; i < insertionCount; i++) {
                AbstractInsnNode insertPoint = insertionPoints.get(i);
                InsnList fakeCheck = createFakeExceptionCheck();
                
                if (fakeCheck != null) {
                    instructions.insertBefore(insertPoint, fakeCheck);
                    modified = true;
                    
                    if (context.getConfig().isVerbose()) {
                        System.out.println("[FakeException] Inserted fake check in method: " + method.name);
                    }
                }
            }
            
            return modified;
        } catch (Exception e) {
            return false;
        }
    }
    
    private List<AbstractInsnNode> findInsertionPoints(InsnList instructions)
    {
        List<AbstractInsnNode> points = new ArrayList<>();
        
        for (AbstractInsnNode insn : instructions) {
            if (insn instanceof VarInsnNode && 
                (insn.getOpcode() == Opcodes.ILOAD || insn.getOpcode() == Opcodes.ALOAD)) {
                points.add(insn);
            } else if (insn instanceof FieldInsnNode && insn.getOpcode() == Opcodes.GETFIELD) {
                points.add(insn.getNext());
            } else if (insn instanceof MethodInsnNode && 
                      !((MethodInsnNode) insn).name.equals("<init>")) {
                AbstractInsnNode next = insn.getNext();
                if (next != null && !(next instanceof InsnNode && 
                    (next.getOpcode() >= Opcodes.IRETURN && next.getOpcode() <= Opcodes.RETURN))) {
                    points.add(next);
                }
            }
        }
        
        return points;
    }
    
    private InsnList createFakeExceptionCheck()
    {
        InsnList list = new InsnList();
        
        try {
            int checkType = random.nextInt(3);
            
            switch (checkType) {
                case 0:
                    createSimpleNullCheck(list);
                    break;
                case 1:
                    createSimpleMathCheck(list);
                    break;
                case 2:
                    createSimpleStringCheck(list);
                    break;
            }
            
            return list;
        } catch (Exception e) {
            return null;
        }
    }
    
    private void createSimpleNullCheck(InsnList list)
    {
        list.add(new LdcInsnNode("fake_check_" + random.nextInt(1000)));
        list.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/String", "hashCode", "()I", false));
        list.add(new InsnNode(Opcodes.POP));
    }
    
    private void createSimpleMathCheck(InsnList list)
    {
        list.add(new LdcInsnNode(1.0));
        list.add(new LdcInsnNode(2.0));
        list.add(new InsnNode(Opcodes.DADD));
        list.add(new InsnNode(Opcodes.POP2));
    }
    
    private void createSimpleStringCheck(InsnList list)
    {
        list.add(new LdcInsnNode("obf_" + random.nextInt(100)));
        list.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/String", "length", "()I", false));
        list.add(new InsnNode(Opcodes.POP));
    }
}