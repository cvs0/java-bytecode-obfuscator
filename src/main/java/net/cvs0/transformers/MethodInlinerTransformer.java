package net.cvs0.transformers;

import net.cvs0.context.ObfuscationContext;
import net.cvs0.core.BaseTransformer;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MethodInlinerTransformer extends BaseTransformer
{
    private static final String INLINE_CANDIDATES_KEY = "methodInliner.candidates";
    private static final String INLINED_METHODS_KEY = "methodInliner.inlined";
    
    public MethodInlinerTransformer()
    {
        super("MethodInliner", 30);
    }
    
    @Override
    public boolean isEnabled(ObfuscationContext context)
    {
        return context.getConfig().isInlineSimpleMethods();
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
            reader.accept(classNode, ClassReader.SKIP_FRAMES);
            
            if ((classNode.access & Opcodes.ACC_INTERFACE) != 0 || 
                (classNode.access & Opcodes.ACC_ANNOTATION) != 0) {
                copyClass(reader, writer);
                return;
            }
            
            analyzeInlineCandidates(context, classNode);
            
            boolean inlined = performInlining(context, classNode);
            
            if (inlined) {
                logTransformation("Inlined methods in class: " + className, context);
                classNode.accept(writer);
            } else {
                copyClass(reader, writer);
            }
        } catch (Exception e) {
            if (context.getConfig().isVerbose()) {
                System.err.println("[MethodInliner] Error processing class " + className + ": " + e.getMessage());
                e.printStackTrace();
            }
            copyClass(reader, writer);
        }
    }
    
    private void analyzeInlineCandidates(ObfuscationContext context, ClassNode classNode)
    {
        if (classNode == null || classNode.methods == null) {
            return;
        }
        
        Map<String, MethodInfo> candidates = getOrCreateCandidatesMap(context);
        
        for (MethodNode method : classNode.methods) {
            if (method != null && isInlineCandidate(method, classNode)) {
                String methodKey = classNode.name + "." + method.name + method.desc;
                candidates.put(methodKey, new MethodInfo(classNode.name, method));
                if (context.getConfig().isVerbose()) {
                    System.out.println("[MethodInliner] Found inline candidate: " + methodKey);
                }
            }
        }
    }
    
    private boolean isInlineCandidate(MethodNode method, ClassNode classNode)
    {
        if ((method.access & Opcodes.ACC_ABSTRACT) != 0 ||
            (method.access & Opcodes.ACC_NATIVE) != 0 ||
            (method.access & Opcodes.ACC_SYNCHRONIZED) != 0) {
            return false;
        }
        
        if (method.name.equals("<init>") || method.name.equals("<clinit>")) {
            return false;
        }
        
        if (method.instructions == null || method.instructions.size() > 15) {
            return false;
        }
        
        if (hasComplexControlFlow(method)) {
            return false;
        }
        
        if (hasRecursiveCall(method, classNode.name)) {
            return false;
        }
        
        if (hasExceptionHandling(method)) {
            return false;
        }
        
        if (!isSimpleMethod(method)) {
            return false;
        }
        
        return true;
    }
    
    private boolean hasComplexControlFlow(MethodNode method)
    {
        for (AbstractInsnNode insn : method.instructions) {
            if (insn instanceof JumpInsnNode || 
                insn instanceof LookupSwitchInsnNode || 
                insn instanceof TableSwitchInsnNode) {
                return true;
            }
        }
        return false;
    }
    
    private boolean hasRecursiveCall(MethodNode method, String className)
    {
        for (AbstractInsnNode insn : method.instructions) {
            if (insn instanceof MethodInsnNode) {
                MethodInsnNode methodInsn = (MethodInsnNode) insn;
                if (methodInsn.owner.equals(className) && 
                    methodInsn.name.equals(method.name) && 
                    methodInsn.desc.equals(method.desc)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    private boolean hasExceptionHandling(MethodNode method)
    {
        return method.tryCatchBlocks != null && !method.tryCatchBlocks.isEmpty();
    }
    
    private boolean isSimpleMethod(MethodNode method)
    {
        if (method.instructions == null) {
            return false;
        }
        
        int complexInstructions = 0;
        for (AbstractInsnNode insn : method.instructions) {
            if (insn instanceof MethodInsnNode || 
                insn instanceof FieldInsnNode ||
                insn instanceof TypeInsnNode ||
                insn instanceof MultiANewArrayInsnNode ||
                insn instanceof InvokeDynamicInsnNode) {
                complexInstructions++;
                if (complexInstructions > 2) {
                    return false;
                }
            }
        }
        
        Type[] paramTypes = Type.getArgumentTypes(method.desc);
        return paramTypes.length <= 3;
    }
    
    private boolean performInlining(ObfuscationContext context, ClassNode classNode)
    {
        Map<String, MethodInfo> candidates = getOrCreateCandidatesMap(context);
        Set<String> inlinedMethods = getOrCreateInlinedSet(context);
        boolean inlined = false;
        
        for (MethodNode method : classNode.methods) {
            if (inlineMethodCalls(method, candidates, inlinedMethods, context)) {
                inlined = true;
            }
        }
        
        return inlined;
    }
    
    private boolean inlineMethodCalls(MethodNode method, Map<String, MethodInfo> candidates, 
                                    Set<String> inlinedMethods, ObfuscationContext context)
    {
        if (method == null || method.instructions == null) {
            return false;
        }
        
        boolean inlined = false;
        InsnList newInstructions = new InsnList();
        Map<LabelNode, LabelNode> labelMap = new HashMap<>();
        
        try {
            for (AbstractInsnNode insn : method.instructions) {
                if (insn == null) {
                    continue;
                }
                
                if (insn instanceof MethodInsnNode) {
                    MethodInsnNode methodCall = (MethodInsnNode) insn;
                    String methodKey = methodCall.owner + "." + methodCall.name + methodCall.desc;
                    
                    MethodInfo candidateInfo = candidates.get(methodKey);
                    if (candidateInfo != null) {
                        if (context.getConfig().isVerbose()) {
                            System.out.println("[MethodInliner] Checking inline call: " + methodKey);
                        }
                        if (shouldInlineCall(methodCall, candidateInfo)) {
                            InsnList inlinedCode = createInlinedCode(candidateInfo.method, methodCall);
                            if (inlinedCode != null && inlinedCode.size() > 0) {
                                newInstructions.add(inlinedCode);
                                inlinedMethods.add(methodKey);
                                inlined = true;
                                logTransformation("Inlined method call: " + methodKey, context);
                                continue;
                            }
                        }
                    }
                }
                
                try {
                    AbstractInsnNode cloned = insn.clone(labelMap);
                    if (cloned != null) {
                        newInstructions.add(cloned);
                    }
                } catch (Exception e) {
                    newInstructions.add(insn);
                }
            }
            
            if (inlined) {
                method.instructions = newInstructions;
            }
        } catch (Exception e) {
            return false;
        }
        
        return inlined;
    }
    
    private boolean shouldInlineCall(MethodInsnNode methodCall, MethodInfo candidateInfo)
    {
        if (methodCall.getOpcode() == Opcodes.INVOKEVIRTUAL || 
            methodCall.getOpcode() == Opcodes.INVOKEINTERFACE) {
            return false;
        }
        
        Type[] argTypes = Type.getArgumentTypes(methodCall.desc);
        if (argTypes.length > 3) {
            return false;
        }
        
        return true;
    }
    
    private InsnList createInlinedCode(MethodNode targetMethod, MethodInsnNode methodCall)
    {
        try {
            if (targetMethod == null || targetMethod.instructions == null) {
                return null;
            }
            
            boolean hasFieldAccess = false;
            for (AbstractInsnNode insn : targetMethod.instructions) {
                if (insn instanceof FieldInsnNode) {
                    hasFieldAccess = true;
                    break;
                }
            }
            
            if (hasFieldAccess) {
                return null;
            }
            
            InsnList inlinedCode = new InsnList();
            
            for (AbstractInsnNode insn : targetMethod.instructions) {
                if (insn == null) {
                    continue;
                }
                
                if (insn instanceof InsnNode && isReturnInstruction(insn.getOpcode())) {
                    if (insn.getOpcode() == Opcodes.RETURN) {
                        break;
                    }
                    continue;
                } else if (insn instanceof LineNumberNode || insn instanceof FrameNode || insn instanceof LabelNode) {
                    continue;
                } else if (insn instanceof JumpInsnNode || 
                          insn instanceof LookupSwitchInsnNode || 
                          insn instanceof TableSwitchInsnNode) {
                    return null;
                } else {
                    try {
                        AbstractInsnNode cloned = insn.clone(new HashMap<>());
                        if (cloned != null) {
                            inlinedCode.add(cloned);
                        }
                    } catch (Exception e) {
                        return null;
                    }
                }
            }
            
            return inlinedCode.size() > 0 ? inlinedCode : null;
            
        } catch (Exception e) {
            return null;
        }
    }
    
    private int getStoreOpcode(Type type)
    {
        switch (type.getSort()) {
            case Type.BOOLEAN:
            case Type.CHAR:
            case Type.BYTE:
            case Type.SHORT:
            case Type.INT:
                return Opcodes.ISTORE;
            case Type.FLOAT:
                return Opcodes.FSTORE;
            case Type.LONG:
                return Opcodes.LSTORE;
            case Type.DOUBLE:
                return Opcodes.DSTORE;
            default:
                return Opcodes.ASTORE;
        }
    }
    
    private boolean isReturnInstruction(int opcode)
    {
        return opcode >= Opcodes.IRETURN && opcode <= Opcodes.RETURN;
    }
    
    @SuppressWarnings("unchecked")
    private Map<String, MethodInfo> getOrCreateCandidatesMap(ObfuscationContext context)
    {
        Map<String, MethodInfo> map = (Map<String, MethodInfo>) context.getProperty(INLINE_CANDIDATES_KEY);
        if (map == null) {
            map = new ConcurrentHashMap<>();
            context.setProperty(INLINE_CANDIDATES_KEY, map);
        }
        return map;
    }
    
    @SuppressWarnings("unchecked")
    private Set<String> getOrCreateInlinedSet(ObfuscationContext context)
    {
        Set<String> set = (Set<String>) context.getProperty(INLINED_METHODS_KEY);
        if (set == null) {
            set = ConcurrentHashMap.newKeySet();
            context.setProperty(INLINED_METHODS_KEY, set);
        }
        return set;
    }
    
    private static class MethodInfo
    {
        final String className;
        final MethodNode method;
        
        MethodInfo(String className, MethodNode method)
        {
            this.className = className;
            this.method = method;
        }
    }
}