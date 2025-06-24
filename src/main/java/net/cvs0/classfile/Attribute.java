package net.cvs0.classfile;

public abstract class Attribute
{
    protected final String name;
    
    protected Attribute(String name)
    {
        this.name = name;
    }
    
    public String getName()
    {
        return name;
    }
    
    public abstract byte[] getData();
    
    public abstract int getLength();
    
    @Override
    public String toString()
    {
        return name;
    }
}