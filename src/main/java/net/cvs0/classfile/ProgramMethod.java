package net.cvs0.classfile;

import org.objectweb.asm.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class ProgramMethod extends ProgramMember
{
    private final String[] exceptions;
    private final MethodDescriptor methodDescriptor;
    private final List<Instruction> instructions;
    private final List<Attribute> attributes;
    private int maxStack;
    private int maxLocals;
    private BasicMethodInfo methodInfo;
    private final Map<String, Object> annotations;
    private final List<String> methodCalls;
    private final List<String> fieldAccesses;
    private int instructionCount = 0;
    private boolean hasCodeFlag = false;
    
    public ProgramMethod(int access, String name, String descriptor, String signature, String[] exceptions)
    {
        super(access, name, descriptor, signature);
        this.exceptions = exceptions != null ? exceptions.clone() : new String[0];
        this.methodDescriptor = new MethodDescriptor(descriptor);
        this.instructions = new ArrayList<>();
        this.attributes = new ArrayList<>();
        this.annotations = new HashMap<>();
        this.methodCalls = new ArrayList<>();
        this.fieldAccesses = new ArrayList<>();
    }
    
    public String[] getExceptions()
    {
        return exceptions.clone();
    }
    
    public MethodDescriptor getMethodDescriptor()
    {
        return methodDescriptor;
    }
    
    public Type[] getArgumentTypes()
    {
        return methodDescriptor.getArgumentTypes();
    }
    
    public Type getReturnType()
    {
        return methodDescriptor.getReturnType();
    }
    
    public int getArgumentCount()
    {
        return methodDescriptor.getArgumentCount();
    }
    
    public boolean hasArguments()
    {
        return methodDescriptor.hasArguments();
    }
    
    public boolean hasExceptions()
    {
        return exceptions.length > 0;
    }
    
    public List<Instruction> getInstructions()
    {
        return new ArrayList<>(instructions);
    }
    
    public void addInstruction(Instruction instruction)
    {
        instructions.add(instruction);
    }
    
    public void clearInstructions()
    {
        instructions.clear();
    }
    
    public List<Attribute> getAttributes()
    {
        return new ArrayList<>(attributes);
    }
    
    public void addAttribute(Attribute attribute)
    {
        attributes.add(attribute);
    }
    
    public int getMaxStack()
    {
        return maxStack;
    }
    
    public void setMaxStack(int maxStack)
    {
        this.maxStack = maxStack;
    }
    
    public int getMaxLocals()
    {
        return maxLocals;
    }
    
    public void setMaxLocals(int maxLocals)
    {
        this.maxLocals = maxLocals;
    }
    
    public boolean isAbstract()
    {
        return (access & JavaConstants.ACC_ABSTRACT) != 0;
    }
    
    public boolean isNative()
    {
        return (access & JavaConstants.ACC_NATIVE) != 0;
    }
    
    public boolean isSynchronized()
    {
        return (access & JavaConstants.ACC_SYNCHRONIZED) != 0;
    }
    
    public boolean isBridge()
    {
        return (access & JavaConstants.ACC_BRIDGE) != 0;
    }
    
    public boolean isVarArgs()
    {
        return (access & JavaConstants.ACC_VARARGS) != 0;
    }
    
    public boolean isConstructor()
    {
        return JavaConstants.INIT_METHOD_NAME.equals(name);
    }
    
    public boolean isStaticInitializer()
    {
        return JavaConstants.CLINIT_METHOD_NAME.equals(name);
    }
    
    public boolean isVoidMethod()
    {
        return methodDescriptor.isVoidMethod();
    }
    
    public boolean hasCode()
    {
        return hasCodeFlag;
    }
    
    public void setHasCode(boolean hasCode)
    {
        this.hasCodeFlag = hasCode;
    }
    
    public MethodSignature getMethodSignature()
    {
        return new MethodSignature(name, methodDescriptor, access);
    }
    
    public BasicMethodInfo getBasicInfo()
    {
        return new BasicMethodInfo(name, descriptor, access, exceptions);
    }
    
    public BasicMethodInfo getMethodInfo()
    {
        return methodInfo;
    }
    
    public void setMethodInfo(BasicMethodInfo methodInfo)
    {
        this.methodInfo = methodInfo;
    }
    
    public void addAnnotation(String key, Object value)
    {
        annotations.put(key, value);
    }
    
    public Object getAnnotation(String key)
    {
        return annotations.get(key);
    }
    
    public Map<String, Object> getAnnotations()
    {
        return new HashMap<>(annotations);
    }
    
    public void addMethodCall(String owner, String name, String descriptor)
    {
        methodCalls.add(owner + "." + name + descriptor);
    }
    
    public void addFieldAccess(String owner, String name, String descriptor)
    {
        fieldAccesses.add(owner + "." + name + ":" + descriptor);
    }
    
    public List<String> getMethodCalls()
    {
        return new ArrayList<>(methodCalls);
    }
    
    public List<String> getFieldAccesses()
    {
        return new ArrayList<>(fieldAccesses);
    }
    
    public int getInstructionCount()
    {
        return instructionCount;
    }
    
    public void setInstructionCount(int instructionCount)
    {
        this.instructionCount = instructionCount;
    }
    
    public boolean hasAnnotation(String descriptor)
    {
        return annotations.containsKey(descriptor) || 
               (descriptor.contains("reflect") && annotations.keySet().stream()
                   .anyMatch(key -> key.contains("reflect")));
    }
    
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        if (isPublic()) sb.append("public ");
        if (isPrivate()) sb.append("private ");
        if (isProtected()) sb.append("protected ");
        if (isStatic()) sb.append("static ");
        if (isFinal()) sb.append("final ");
        if (isAbstract()) sb.append("abstract ");
        if (isNative()) sb.append("native ");
        if (isSynchronized()) sb.append("synchronized ");
        
        sb.append(getReturnType().getClassName()).append(" ").append(name).append("(");
        
        Type[] argTypes = getArgumentTypes();
        for (int i = 0; i < argTypes.length; i++) {
            if (i > 0) sb.append(", ");
            sb.append(argTypes[i].getClassName());
        }
        
        sb.append(")");
        
        if (hasExceptions()) {
            sb.append(" throws ");
            for (int i = 0; i < exceptions.length; i++) {
                if (i > 0) sb.append(", ");
                sb.append(exceptions[i].replace('/', '.'));
            }
        }
        
        return sb.toString();
    }
}