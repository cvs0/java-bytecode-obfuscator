package net.cvs0.classfile;

import java.util.Objects;

public class MethodSignature
{
    private final String name;
    private final MethodDescriptor descriptor;
    private final int access;
    
    public MethodSignature(String name, String descriptor, int access)
    {
        this.name = name;
        this.descriptor = new MethodDescriptor(descriptor);
        this.access = access;
    }
    
    public MethodSignature(String name, MethodDescriptor descriptor, int access)
    {
        this.name = name;
        this.descriptor = descriptor;
        this.access = access;
    }
    
    public String getName()
    {
        return name;
    }
    
    public MethodDescriptor getDescriptor()
    {
        return descriptor;
    }
    
    public String getDescriptorString()
    {
        return descriptor.getDescriptor();
    }
    
    public int getAccess()
    {
        return access;
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
    
    public boolean isConstructor()
    {
        return JavaConstants.INIT_METHOD_NAME.equals(name);
    }
    
    public boolean isStaticInitializer()
    {
        return JavaConstants.CLINIT_METHOD_NAME.equals(name);
    }
    
    public String getSignatureString()
    {
        return name + descriptor.getDescriptor();
    }
    
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        MethodSignature that = (MethodSignature) obj;
        return Objects.equals(name, that.name) && 
               Objects.equals(descriptor, that.descriptor);
    }
    
    @Override
    public int hashCode()
    {
        return Objects.hash(name, descriptor);
    }
    
    @Override
    public String toString()
    {
        return getSignatureString();
    }
}