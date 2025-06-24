package net.cvs0.classfile;

import org.objectweb.asm.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class BasicMethodInfo
{
    private final String name;
    private final String descriptor;
    private final int access;
    private final Type[] argumentTypes;
    private final Type returnType;
    private final String[] exceptions;
    private final List<String> localVariableNames;
    private final Map<String, String> parameters;
    
    public BasicMethodInfo(String name, String descriptor, int access, String[] exceptions)
    {
        this.name = name;
        this.descriptor = descriptor;
        this.access = access;
        this.argumentTypes = Type.getArgumentTypes(descriptor);
        this.returnType = Type.getReturnType(descriptor);
        this.exceptions = exceptions != null ? exceptions.clone() : new String[0];
        this.localVariableNames = new ArrayList<>();
        this.parameters = new HashMap<>();
    }
    
    public BasicMethodInfo(String name, String descriptor, int access)
    {
        this(name, descriptor, access, null);
    }
    
    public String getName()
    {
        return name;
    }
    
    public String getDescriptor()
    {
        return descriptor;
    }
    
    public int getAccess()
    {
        return access;
    }
    
    public Type[] getArgumentTypes()
    {
        return argumentTypes.clone();
    }
    
    public Type getReturnType()
    {
        return returnType;
    }
    
    public String[] getExceptions()
    {
        return exceptions.clone();
    }
    
    public int getArgumentCount()
    {
        return argumentTypes.length;
    }
    
    public boolean hasArguments()
    {
        return argumentTypes.length > 0;
    }
    
    public boolean hasExceptions()
    {
        return exceptions.length > 0;
    }
    
    public boolean isPublic()
    {
        return (access & JavaConstants.ACC_PUBLIC) != 0;
    }
    
    public boolean isPrivate()
    {
        return (access & JavaConstants.ACC_PRIVATE) != 0;
    }
    
    public boolean isProtected()
    {
        return (access & JavaConstants.ACC_PROTECTED) != 0;
    }
    
    public boolean isStatic()
    {
        return (access & JavaConstants.ACC_STATIC) != 0;
    }
    
    public boolean isFinal()
    {
        return (access & JavaConstants.ACC_FINAL) != 0;
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
    
    public boolean isSynthetic()
    {
        return (access & JavaConstants.ACC_SYNTHETIC) != 0;
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
        return returnType.equals(Type.VOID_TYPE);
    }
    
    public String getSignature()
    {
        return name + descriptor;
    }
    
    public void addLocalVariable(String name, String descriptor, String signature, int index)
    {
        localVariableNames.add(name);
    }
    
    public void addParameter(String name, int access)
    {
        parameters.put(name, String.valueOf(access));
    }
    
    public List<String> getLocalVariableNames()
    {
        return new ArrayList<>(localVariableNames);
    }
    
    public Map<String, String> getParameters()
    {
        return new HashMap<>(parameters);
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
        
        sb.append(returnType.getClassName()).append(" ").append(name).append("(");
        
        for (int i = 0; i < argumentTypes.length; i++) {
            if (i > 0) sb.append(", ");
            sb.append(argumentTypes[i].getClassName());
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