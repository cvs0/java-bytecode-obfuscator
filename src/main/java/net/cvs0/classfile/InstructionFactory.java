package net.cvs0.classfile;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public class InstructionFactory
{
    public static ConstantInstruction createConstantInstruction(Object value)
    {
        if (value == null) {
            return new ConstantInstruction(Opcodes.ACONST_NULL, null);
        } else if (value instanceof Integer) {
            int intValue = (Integer) value;
            if (intValue >= -1 && intValue <= 5) {
                return new ConstantInstruction(Opcodes.ICONST_0 + intValue, intValue);
            } else if (intValue >= Byte.MIN_VALUE && intValue <= Byte.MAX_VALUE) {
                return new ConstantInstruction(Opcodes.BIPUSH, intValue);
            } else if (intValue >= Short.MIN_VALUE && intValue <= Short.MAX_VALUE) {
                return new ConstantInstruction(Opcodes.SIPUSH, intValue);
            } else {
                return new ConstantInstruction(Opcodes.LDC, intValue);
            }
        } else if (value instanceof Long) {
            long longValue = (Long) value;
            if (longValue == 0L) {
                return new ConstantInstruction(Opcodes.LCONST_0, longValue);
            } else if (longValue == 1L) {
                return new ConstantInstruction(Opcodes.LCONST_1, longValue);
            } else {
                return new ConstantInstruction(Opcodes.LDC, longValue);
            }
        } else if (value instanceof Float) {
            float floatValue = (Float) value;
            if (floatValue == 0.0f) {
                return new ConstantInstruction(Opcodes.FCONST_0, floatValue);
            } else if (floatValue == 1.0f) {
                return new ConstantInstruction(Opcodes.FCONST_1, floatValue);
            } else if (floatValue == 2.0f) {
                return new ConstantInstruction(Opcodes.FCONST_2, floatValue);
            } else {
                return new ConstantInstruction(Opcodes.LDC, floatValue);
            }
        } else if (value instanceof Double) {
            double doubleValue = (Double) value;
            if (doubleValue == 0.0) {
                return new ConstantInstruction(Opcodes.DCONST_0, doubleValue);
            } else if (doubleValue == 1.0) {
                return new ConstantInstruction(Opcodes.DCONST_1, doubleValue);
            } else {
                return new ConstantInstruction(Opcodes.LDC, doubleValue);
            }
        } else {
            return new ConstantInstruction(Opcodes.LDC, value);
        }
    }
    
    public static VariableInstruction createLoadInstruction(Type type, int variable)
    {
        int opcode;
        switch (type.getSort()) {
            case Type.BOOLEAN:
            case Type.BYTE:
            case Type.CHAR:
            case Type.SHORT:
            case Type.INT:
                opcode = Opcodes.ILOAD;
                break;
            case Type.LONG:
                opcode = Opcodes.LLOAD;
                break;
            case Type.FLOAT:
                opcode = Opcodes.FLOAD;
                break;
            case Type.DOUBLE:
                opcode = Opcodes.DLOAD;
                break;
            case Type.ARRAY:
            case Type.OBJECT:
                opcode = Opcodes.ALOAD;
                break;
            default:
                throw new IllegalArgumentException("Invalid type for load instruction: " + type);
        }
        return new VariableInstruction(opcode, variable);
    }
    
    public static VariableInstruction createStoreInstruction(Type type, int variable)
    {
        int opcode;
        switch (type.getSort()) {
            case Type.BOOLEAN:
            case Type.BYTE:
            case Type.CHAR:
            case Type.SHORT:
            case Type.INT:
                opcode = Opcodes.ISTORE;
                break;
            case Type.LONG:
                opcode = Opcodes.LSTORE;
                break;
            case Type.FLOAT:
                opcode = Opcodes.FSTORE;
                break;
            case Type.DOUBLE:
                opcode = Opcodes.DSTORE;
                break;
            case Type.ARRAY:
            case Type.OBJECT:
                opcode = Opcodes.ASTORE;
                break;
            default:
                throw new IllegalArgumentException("Invalid type for store instruction: " + type);
        }
        return new VariableInstruction(opcode, variable);
    }
    
    public static Instruction createReturnInstruction(Type type)
    {
        int opcode;
        switch (type.getSort()) {
            case Type.VOID:
                opcode = Opcodes.RETURN;
                break;
            case Type.BOOLEAN:
            case Type.BYTE:
            case Type.CHAR:
            case Type.SHORT:
            case Type.INT:
                opcode = Opcodes.IRETURN;
                break;
            case Type.LONG:
                opcode = Opcodes.LRETURN;
                break;
            case Type.FLOAT:
                opcode = Opcodes.FRETURN;
                break;
            case Type.DOUBLE:
                opcode = Opcodes.DRETURN;
                break;
            case Type.ARRAY:
            case Type.OBJECT:
                opcode = Opcodes.ARETURN;
                break;
            default:
                throw new IllegalArgumentException("Invalid type for return instruction: " + type);
        }
        return new SimpleInstruction(opcode);
    }
    
    public static Instruction createSimpleInstruction(int opcode)
    {
        return new SimpleInstruction(opcode);
    }
    
    public static class SimpleInstruction extends Instruction
    {
        public SimpleInstruction(int opcode)
        {
            super(opcode);
        }
        
        @Override
        public String getMnemonic()
        {
            switch (opcode) {
                case Opcodes.NOP: return "nop";
                case Opcodes.POP: return "pop";
                case Opcodes.POP2: return "pop2";
                case Opcodes.DUP: return "dup";
                case Opcodes.DUP_X1: return "dup_x1";
                case Opcodes.DUP_X2: return "dup_x2";
                case Opcodes.DUP2: return "dup2";
                case Opcodes.DUP2_X1: return "dup2_x1";
                case Opcodes.DUP2_X2: return "dup2_x2";
                case Opcodes.SWAP: return "swap";
                case Opcodes.IADD: return "iadd";
                case Opcodes.LADD: return "ladd";
                case Opcodes.FADD: return "fadd";
                case Opcodes.DADD: return "dadd";
                case Opcodes.ISUB: return "isub";
                case Opcodes.LSUB: return "lsub";
                case Opcodes.FSUB: return "fsub";
                case Opcodes.DSUB: return "dsub";
                case Opcodes.IMUL: return "imul";
                case Opcodes.LMUL: return "lmul";
                case Opcodes.FMUL: return "fmul";
                case Opcodes.DMUL: return "dmul";
                case Opcodes.IDIV: return "idiv";
                case Opcodes.LDIV: return "ldiv";
                case Opcodes.FDIV: return "fdiv";
                case Opcodes.DDIV: return "ddiv";
                case Opcodes.IREM: return "irem";
                case Opcodes.LREM: return "lrem";
                case Opcodes.FREM: return "frem";
                case Opcodes.DREM: return "drem";
                case Opcodes.INEG: return "ineg";
                case Opcodes.LNEG: return "lneg";
                case Opcodes.FNEG: return "fneg";
                case Opcodes.DNEG: return "dneg";
                case Opcodes.RETURN: return "return";
                case Opcodes.IRETURN: return "ireturn";
                case Opcodes.LRETURN: return "lreturn";
                case Opcodes.FRETURN: return "freturn";
                case Opcodes.DRETURN: return "dreturn";
                case Opcodes.ARETURN: return "areturn";
                case Opcodes.ATHROW: return "athrow";
                case Opcodes.MONITORENTER: return "monitorenter";
                case Opcodes.MONITOREXIT: return "monitorexit";
                default: return "unknown(" + opcode + ")";
            }
        }
        
        @Override
        public int getSize()
        {
            return 1;
        }
    }
}