package net.cvs0.classfile;

import org.objectweb.asm.Type;
import java.util.Arrays;
import java.util.Objects;

public class MethodDescriptor
{
    private final String descriptor;
    private final Type[] argumentTypes;
    private final Type returnType;
    
    public MethodDescriptor(String descriptor)
    {
        this.descriptor = descriptor;
        this.argumentTypes = Type.getArgumentTypes(descriptor);
        this.returnType = Type.getReturnType(descriptor);
    }
    
    public MethodDescriptor(Type returnType, Type... argumentTypes)
    {
        this.returnType = returnType;
        this.argumentTypes = argumentTypes.clone();
        this.descriptor = Type.getMethodDescriptor(returnType, argumentTypes);
    }
    
    public String getDescriptor()
    {
        return descriptor;
    }
    
    public Type[] getArgumentTypes()
    {
        return argumentTypes.clone();
    }
    
    public Type getReturnType()
    {
        return returnType;
    }
    
    public int getArgumentCount()
    {
        return argumentTypes.length;
    }
    
    public Type getArgumentType(int index)
    {
        return argumentTypes[index];
    }
    
    public boolean isVoidMethod()
    {
        return returnType.equals(Type.VOID_TYPE);
    }
    
    public boolean hasArguments()
    {
        return argumentTypes.length > 0;
    }
    
    public String[] getArgumentDescriptors()
    {
        String[] descriptors = new String[argumentTypes.length];
        for (int i = 0; i < argumentTypes.length; i++) {
            descriptors[i] = argumentTypes[i].getDescriptor();
        }
        return descriptors;
    }
    
    public String getReturnTypeDescriptor()
    {
        return returnType.getDescriptor();
    }
    
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        MethodDescriptor that = (MethodDescriptor) obj;
        return Objects.equals(descriptor, that.descriptor);
    }
    
    @Override
    public int hashCode()
    {
        return Objects.hash(descriptor);
    }
    
    @Override
    public String toString()
    {
        return descriptor;
    }
}