package net.cvs0.classfile;

import java.util.Objects;

public abstract class ProgramMember
{
    protected final int access;
    protected final String name;
    protected final String descriptor;
    protected final String signature;
    protected ProgramClass ownerClass;
    
    protected ProgramMember(int access, String name, String descriptor, String signature)
    {
        this.access = access;
        this.name = name;
        this.descriptor = descriptor;
        this.signature = signature;
    }
    
    public int getAccess()
    {
        return access;
    }
    
    public String getName()
    {
        return name;
    }
    
    public String getDescriptor()
    {
        return descriptor;
    }
    
    public String getSignature()
    {
        return signature;
    }
    
    public ProgramClass getOwnerClass()
    {
        return ownerClass;
    }
    
    public void setOwnerClass(ProgramClass ownerClass)
    {
        this.ownerClass = ownerClass;
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
    
    public boolean isSynthetic()
    {
        return (access & JavaConstants.ACC_SYNTHETIC) != 0;
    }
    
    public boolean isDeprecated()
    {
        return (access & JavaConstants.ACC_DEPRECATED) != 0;
    }
    
    public boolean isAbstract()
    {
        return (access & JavaConstants.ACC_ABSTRACT) != 0;
    }
    
    public String getFullName()
    {
        return ownerClass != null ? ownerClass.getName() + "." + name : name;
    }
    
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ProgramMember that = (ProgramMember) obj;
        return access == that.access &&
               Objects.equals(name, that.name) &&
               Objects.equals(descriptor, that.descriptor);
    }
    
    @Override
    public int hashCode()
    {
        return Objects.hash(access, name, descriptor);
    }
    
    @Override
    public String toString()
    {
        return getFullName();
    }
}