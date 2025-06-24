package net.cvs0.classfile;

import org.objectweb.asm.Type;

public final class TypeConstants
{
    public static final Type VOID_TYPE = Type.VOID_TYPE;
    public static final Type BOOLEAN_TYPE = Type.BOOLEAN_TYPE;
    public static final Type CHAR_TYPE = Type.CHAR_TYPE;
    public static final Type BYTE_TYPE = Type.BYTE_TYPE;
    public static final Type SHORT_TYPE = Type.SHORT_TYPE;
    public static final Type INT_TYPE = Type.INT_TYPE;
    public static final Type FLOAT_TYPE = Type.FLOAT_TYPE;
    public static final Type LONG_TYPE = Type.LONG_TYPE;
    public static final Type DOUBLE_TYPE = Type.DOUBLE_TYPE;
    
    public static final int VOID = Type.VOID;
    public static final int BOOLEAN = Type.BOOLEAN;
    public static final int CHAR = Type.CHAR;
    public static final int BYTE = Type.BYTE;
    public static final int SHORT = Type.SHORT;
    public static final int INT = Type.INT;
    public static final int FLOAT = Type.FLOAT;
    public static final int LONG = Type.LONG;
    public static final int DOUBLE = Type.DOUBLE;
    public static final int ARRAY = Type.ARRAY;
    public static final int OBJECT = Type.OBJECT;
    public static final int METHOD = Type.METHOD;
    
    private TypeConstants() {}
    
    public static Type getType(String descriptor)
    {
        return Type.getType(descriptor);
    }
    
    public static Type getObjectType(String internalName)
    {
        return Type.getObjectType(internalName);
    }
    
    public static Type getMethodType(String descriptor)
    {
        return Type.getMethodType(descriptor);
    }
    
    public static Type[] getArgumentTypes(String methodDescriptor)
    {
        return Type.getArgumentTypes(methodDescriptor);
    }
    
    public static Type getReturnType(String methodDescriptor)
    {
        return Type.getReturnType(methodDescriptor);
    }
}