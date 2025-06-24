package net.cvs0.classfile;

import org.objectweb.asm.Opcodes;

public class ConstantInstruction extends Instruction
{
    private final Object value;
    
    public ConstantInstruction(int opcode, Object value)
    {
        super(opcode);
        this.value = value;
    }
    
    public Object getValue()
    {
        return value;
    }
    
    @Override
    public String getMnemonic()
    {
        switch (opcode) {
            case Opcodes.ACONST_NULL: return "aconst_null";
            case Opcodes.ICONST_M1: return "iconst_m1";
            case Opcodes.ICONST_0: return "iconst_0";
            case Opcodes.ICONST_1: return "iconst_1";
            case Opcodes.ICONST_2: return "iconst_2";
            case Opcodes.ICONST_3: return "iconst_3";
            case Opcodes.ICONST_4: return "iconst_4";
            case Opcodes.ICONST_5: return "iconst_5";
            case Opcodes.LCONST_0: return "lconst_0";
            case Opcodes.LCONST_1: return "lconst_1";
            case Opcodes.FCONST_0: return "fconst_0";
            case Opcodes.FCONST_1: return "fconst_1";
            case Opcodes.FCONST_2: return "fconst_2";
            case Opcodes.DCONST_0: return "dconst_0";
            case Opcodes.DCONST_1: return "dconst_1";
            case Opcodes.BIPUSH: return "bipush";
            case Opcodes.SIPUSH: return "sipush";
            case Opcodes.LDC: return "ldc";
            default: return "unknown";
        }
    }
    
    @Override
    public int getSize()
    {
        switch (opcode) {
            case Opcodes.ACONST_NULL:
            case Opcodes.ICONST_M1:
            case Opcodes.ICONST_0:
            case Opcodes.ICONST_1:
            case Opcodes.ICONST_2:
            case Opcodes.ICONST_3:
            case Opcodes.ICONST_4:
            case Opcodes.ICONST_5:
            case Opcodes.LCONST_0:
            case Opcodes.LCONST_1:
            case Opcodes.FCONST_0:
            case Opcodes.FCONST_1:
            case Opcodes.FCONST_2:
            case Opcodes.DCONST_0:
            case Opcodes.DCONST_1:
                return 1;
            case Opcodes.BIPUSH:
            case Opcodes.LDC:
                return 2;
            case Opcodes.SIPUSH:
                return 3;
            default:
                return 1;
        }
    }
    
    public boolean isNull()
    {
        return opcode == Opcodes.ACONST_NULL;
    }
    
    public boolean isInteger()
    {
        return opcode >= Opcodes.ICONST_M1 && opcode <= Opcodes.ICONST_5;
    }
    
    public boolean isLong()
    {
        return opcode == Opcodes.LCONST_0 || opcode == Opcodes.LCONST_1;
    }
    
    public boolean isFloat()
    {
        return opcode >= Opcodes.FCONST_0 && opcode <= Opcodes.FCONST_2;
    }
    
    public boolean isDouble()
    {
        return opcode == Opcodes.DCONST_0 || opcode == Opcodes.DCONST_1;
    }
    
    public boolean isString()
    {
        return opcode == Opcodes.LDC && value instanceof String;
    }
    
    public boolean isClass()
    {
        return opcode == Opcodes.LDC && value instanceof org.objectweb.asm.Type;
    }
    
    public boolean isPrimitive()
    {
        return isInteger() || isLong() || isFloat() || isDouble();
    }
    
    public int getIntValue()
    {
        switch (opcode) {
            case Opcodes.ICONST_M1: return -1;
            case Opcodes.ICONST_0: return 0;
            case Opcodes.ICONST_1: return 1;
            case Opcodes.ICONST_2: return 2;
            case Opcodes.ICONST_3: return 3;
            case Opcodes.ICONST_4: return 4;
            case Opcodes.ICONST_5: return 5;
            case Opcodes.BIPUSH:
            case Opcodes.SIPUSH:
                return (Integer) value;
            default:
                throw new IllegalStateException("Not an integer constant instruction");
        }
    }
    
    @Override
    public String toString()
    {
        if (value != null) {
            return getMnemonic() + " " + value;
        }
        return getMnemonic();
    }
}