package net.cvs0.utils;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.Random;

public class BytecodeUtils
{
    public static void pushInteger(MethodVisitor mv, int value)
    {
        switch (value) {
            case -1:
                mv.visitInsn(Opcodes.ICONST_M1);
                break;
            case 0:
                mv.visitInsn(Opcodes.ICONST_0);
                break;
            case 1:
                mv.visitInsn(Opcodes.ICONST_1);
                break;
            case 2:
                mv.visitInsn(Opcodes.ICONST_2);
                break;
            case 3:
                mv.visitInsn(Opcodes.ICONST_3);
                break;
            case 4:
                mv.visitInsn(Opcodes.ICONST_4);
                break;
            case 5:
                mv.visitInsn(Opcodes.ICONST_5);
                break;
            default:
                if (value >= Byte.MIN_VALUE && value <= Byte.MAX_VALUE) {
                    mv.visitIntInsn(Opcodes.BIPUSH, value);
                } else if (value >= Short.MIN_VALUE && value <= Short.MAX_VALUE) {
                    mv.visitIntInsn(Opcodes.SIPUSH, value);
                } else {
                    mv.visitLdcInsn(value);
                }
                break;
        }
    }

    public static void pushLong(MethodVisitor mv, long value)
    {
        if (value == 0L) {
            mv.visitInsn(Opcodes.LCONST_0);
        } else if (value == 1L) {
            mv.visitInsn(Opcodes.LCONST_1);
        } else {
            mv.visitLdcInsn(value);
        }
    }

    public static void pushFloat(MethodVisitor mv, float value)
    {
        if (value == 0.0f) {
            mv.visitInsn(Opcodes.FCONST_0);
        } else if (value == 1.0f) {
            mv.visitInsn(Opcodes.FCONST_1);
        } else if (value == 2.0f) {
            mv.visitInsn(Opcodes.FCONST_2);
        } else {
            mv.visitLdcInsn(value);
        }
    }

    public static void pushDouble(MethodVisitor mv, double value)
    {
        if (value == 0.0) {
            mv.visitInsn(Opcodes.DCONST_0);
        } else if (value == 1.0) {
            mv.visitInsn(Opcodes.DCONST_1);
        } else {
            mv.visitLdcInsn(value);
        }
    }

    public static boolean isConstantInstruction(int opcode)
    {
        return opcode >= Opcodes.ACONST_NULL && opcode <= Opcodes.DCONST_1;
    }

    public static boolean isIntegerConstant(int opcode)
    {
        return opcode >= Opcodes.ICONST_M1 && opcode <= Opcodes.ICONST_5;
    }

    public static boolean isLongConstant(int opcode)
    {
        return opcode == Opcodes.LCONST_0 || opcode == Opcodes.LCONST_1;
    }

    public static boolean isFloatConstant(int opcode)
    {
        return opcode >= Opcodes.FCONST_0 && opcode <= Opcodes.FCONST_2;
    }

    public static boolean isDoubleConstant(int opcode)
    {
        return opcode == Opcodes.DCONST_0 || opcode == Opcodes.DCONST_1;
    }

    public static int getConstantValue(int opcode)
    {
        switch (opcode) {
            case Opcodes.ICONST_M1: return -1;
            case Opcodes.ICONST_0: return 0;
            case Opcodes.ICONST_1: return 1;
            case Opcodes.ICONST_2: return 2;
            case Opcodes.ICONST_3: return 3;
            case Opcodes.ICONST_4: return 4;
            case Opcodes.ICONST_5: return 5;
            default: throw new IllegalArgumentException("Not a constant opcode: " + opcode);
        }
    }

    public static void generateArithmetic(MethodVisitor mv, int targetValue, Random random)
    {
        int operation = random.nextInt(4);
        int operand1 = random.nextInt(20) + 1;
        int operand2;

        switch (operation) {
            case 0:
                operand2 = operand1 + targetValue;
                pushInteger(mv, operand2);
                pushInteger(mv, operand1);
                mv.visitInsn(Opcodes.ISUB);
                break;
            case 1:
                operand2 = targetValue - operand1;
                pushInteger(mv, operand1);
                pushInteger(mv, operand2);
                mv.visitInsn(Opcodes.IADD);
                break;
            case 2:
                if (targetValue != 0) {
                    operand2 = operand1 * targetValue;
                    pushInteger(mv, operand2);
                    pushInteger(mv, operand1);
                    mv.visitInsn(Opcodes.IDIV);
                } else {
                    pushInteger(mv, operand1);
                    pushInteger(mv, operand1);
                    mv.visitInsn(Opcodes.ISUB);
                }
                break;
            case 3:
                operand2 = operand1 ^ targetValue;
                pushInteger(mv, operand1);
                pushInteger(mv, operand2);
                mv.visitInsn(Opcodes.IXOR);
                break;
        }
    }

    public static boolean isMethodSkippable(String name, int access)
    {
        if (name != null && (name.equals("<init>") || name.equals("<clinit>"))) {
            return true;
        }
        
        if (name != null && name.startsWith("lambda$")) {
            return true;
        }
        
        if (name != null && (name.equals("values") || name.equals("valueOf") || name.equals("$values"))) {
            return true;
        }
        
        if ((access & Opcodes.ACC_SYNTHETIC) != 0) {
            return true;
        }
        
        if ((access & Opcodes.ACC_BRIDGE) != 0) {
            return true;
        }
        
        if (name != null && name.equals("main")) {
            return true;
        }
        
        return false;
    }

    public static boolean isFieldSkippable(String name, int access)
    {
        if (name != null && name.startsWith("$")) {
            return true;
        }
        
        if ((access & Opcodes.ACC_SYNTHETIC) != 0) {
            return true;
        }
        
        return false;
    }

    public static String getOpcodeString(int opcode)
    {
        switch (opcode) {
            case Opcodes.ICONST_M1: return "ICONST_M1";
            case Opcodes.ICONST_0: return "ICONST_0";
            case Opcodes.ICONST_1: return "ICONST_1";
            case Opcodes.ICONST_2: return "ICONST_2";
            case Opcodes.ICONST_3: return "ICONST_3";
            case Opcodes.ICONST_4: return "ICONST_4";
            case Opcodes.ICONST_5: return "ICONST_5";
            case Opcodes.LCONST_0: return "LCONST_0";
            case Opcodes.LCONST_1: return "LCONST_1";
            case Opcodes.FCONST_0: return "FCONST_0";
            case Opcodes.FCONST_1: return "FCONST_1";
            case Opcodes.FCONST_2: return "FCONST_2";
            case Opcodes.DCONST_0: return "DCONST_0";
            case Opcodes.DCONST_1: return "DCONST_1";
            default: return "UNKNOWN_" + opcode;
        }
    }
}