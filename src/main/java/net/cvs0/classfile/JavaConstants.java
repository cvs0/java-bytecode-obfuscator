package net.cvs0.classfile;

import org.objectweb.asm.Opcodes;

public final class JavaConstants
{
    public static final int ACC_PUBLIC = Opcodes.ACC_PUBLIC;
    public static final int ACC_PRIVATE = Opcodes.ACC_PRIVATE;
    public static final int ACC_PROTECTED = Opcodes.ACC_PROTECTED;
    public static final int ACC_STATIC = Opcodes.ACC_STATIC;
    public static final int ACC_FINAL = Opcodes.ACC_FINAL;
    public static final int ACC_SUPER = Opcodes.ACC_SUPER;
    public static final int ACC_SYNCHRONIZED = Opcodes.ACC_SYNCHRONIZED;
    public static final int ACC_VOLATILE = Opcodes.ACC_VOLATILE;
    public static final int ACC_BRIDGE = Opcodes.ACC_BRIDGE;
    public static final int ACC_VARARGS = Opcodes.ACC_VARARGS;
    public static final int ACC_NATIVE = Opcodes.ACC_NATIVE;
    public static final int ACC_INTERFACE = Opcodes.ACC_INTERFACE;
    public static final int ACC_ABSTRACT = Opcodes.ACC_ABSTRACT;
    public static final int ACC_STRICT = Opcodes.ACC_STRICT;
    public static final int ACC_SYNTHETIC = Opcodes.ACC_SYNTHETIC;
    public static final int ACC_ANNOTATION = Opcodes.ACC_ANNOTATION;
    public static final int ACC_ENUM = Opcodes.ACC_ENUM;
    public static final int ACC_DEPRECATED = Opcodes.ACC_DEPRECATED;
    
    public static final String OBJECT_CLASS_NAME = "java/lang/Object";
    public static final String STRING_CLASS_NAME = "java/lang/String";
    public static final String CLASS_CLASS_NAME = "java/lang/Class";
    public static final String THROWABLE_CLASS_NAME = "java/lang/Throwable";
    
    public static final String INIT_METHOD_NAME = "<init>";
    public static final String CLINIT_METHOD_NAME = "<clinit>";
    
    public static final String VOID_TYPE_DESCRIPTOR = "V";
    public static final String BOOLEAN_TYPE_DESCRIPTOR = "Z";
    public static final String BYTE_TYPE_DESCRIPTOR = "B";
    public static final String CHAR_TYPE_DESCRIPTOR = "C";
    public static final String SHORT_TYPE_DESCRIPTOR = "S";
    public static final String INT_TYPE_DESCRIPTOR = "I";
    public static final String LONG_TYPE_DESCRIPTOR = "J";
    public static final String FLOAT_TYPE_DESCRIPTOR = "F";
    public static final String DOUBLE_TYPE_DESCRIPTOR = "D";
    
    private JavaConstants() {}
}