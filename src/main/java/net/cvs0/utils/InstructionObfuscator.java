package net.cvs0.utils;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.Random;
import java.util.function.Consumer;

public class InstructionObfuscator
{
    private final Random random;
    private final ObfuscationStrategy strategy;

    public InstructionObfuscator(Random random, ObfuscationStrategy strategy)
    {
        this.random = random;
        this.strategy = strategy;
    }

    public InstructionObfuscator(Random random)
    {
        this(random, ObfuscationStrategy.MIXED);
    }

    public void obfuscateConstant(MethodVisitor mv, int opcode)
    {
        if (!BytecodeUtils.isIntegerConstant(opcode)) {
            mv.visitInsn(opcode);
            return;
        }

        int value = BytecodeUtils.getConstantValue(opcode);
        obfuscateIntegerValue(mv, value);
    }

    public void obfuscateIntegerValue(MethodVisitor mv, int value)
    {
        switch (strategy) {
            case ARITHMETIC:
                obfuscateWithArithmetic(mv, value);
                break;
            case BITWISE:
                obfuscateWithBitwise(mv, value);
                break;
            case MIXED:
                if (random.nextBoolean()) {
                    obfuscateWithArithmetic(mv, value);
                } else {
                    obfuscateWithBitwise(mv, value);
                }
                break;
            case STACK_MANIPULATION:
                obfuscateWithStackManipulation(mv, value);
                break;
        }
    }

    private void obfuscateWithArithmetic(MethodVisitor mv, int value)
    {
        int method = random.nextInt(4);
        
        switch (method) {
            case 0:
                int addend = random.nextInt(100) + 1;
                BytecodeUtils.pushInteger(mv, value + addend);
                BytecodeUtils.pushInteger(mv, addend);
                mv.visitInsn(Opcodes.ISUB);
                break;
            case 1:
                int subtrahend = random.nextInt(100) + 1;
                BytecodeUtils.pushInteger(mv, value - subtrahend);
                BytecodeUtils.pushInteger(mv, subtrahend);
                mv.visitInsn(Opcodes.IADD);
                break;
            case 2:
                if (value != 0) {
                    int multiplier = random.nextInt(10) + 2;
                    BytecodeUtils.pushInteger(mv, value * multiplier);
                    BytecodeUtils.pushInteger(mv, multiplier);
                    mv.visitInsn(Opcodes.IDIV);
                } else {
                    obfuscateWithBitwise(mv, value);
                }
                break;
            case 3:
                int xorOperand = random.nextInt(256);
                BytecodeUtils.pushInteger(mv, value ^ xorOperand);
                BytecodeUtils.pushInteger(mv, xorOperand);
                mv.visitInsn(Opcodes.IXOR);
                break;
        }
    }

    private void obfuscateWithBitwise(MethodVisitor mv, int value)
    {
        int method = random.nextInt(3);
        
        switch (method) {
            case 0:
                int orOperand = random.nextInt(256);
                BytecodeUtils.pushInteger(mv, value | orOperand);
                BytecodeUtils.pushInteger(mv, ~orOperand);
                mv.visitInsn(Opcodes.IAND);
                break;
            case 1:
                int andOperand = value | (random.nextInt(256) << 8);
                BytecodeUtils.pushInteger(mv, andOperand);
                BytecodeUtils.pushInteger(mv, 0xFF);
                mv.visitInsn(Opcodes.IAND);
                break;
            case 2:
                int shiftAmount = random.nextInt(8) + 1;
                BytecodeUtils.pushInteger(mv, value << shiftAmount);
                BytecodeUtils.pushInteger(mv, shiftAmount);
                mv.visitInsn(Opcodes.ISHR);
                break;
        }
    }

    private void obfuscateWithStackManipulation(MethodVisitor mv, int value)
    {
        int dummy1 = random.nextInt(1000);
        int dummy2 = random.nextInt(1000);
        
        BytecodeUtils.pushInteger(mv, dummy1);
        BytecodeUtils.pushInteger(mv, value);
        BytecodeUtils.pushInteger(mv, dummy2);
        mv.visitInsn(Opcodes.POP);
        mv.visitInsn(Opcodes.SWAP);
        mv.visitInsn(Opcodes.POP);
    }

    public boolean shouldObfuscate(float probability)
    {
        return random.nextFloat() < probability;
    }

    public void obfuscateJump(MethodVisitor mv, int opcode, Consumer<Integer> originalJump)
    {
        if (!shouldObfuscateJumps()) {
            originalJump.accept(opcode);
            return;
        }

        switch (opcode) {
            case Opcodes.IFEQ:
                BytecodeUtils.pushInteger(mv, 0);
                originalJump.accept(Opcodes.IF_ICMPEQ);
                break;
            case Opcodes.IFNE:
                BytecodeUtils.pushInteger(mv, 0);
                originalJump.accept(Opcodes.IF_ICMPNE);
                break;
            case Opcodes.IFLT:
                BytecodeUtils.pushInteger(mv, 0);
                originalJump.accept(Opcodes.IF_ICMPLT);
                break;
            case Opcodes.IFGE:
                BytecodeUtils.pushInteger(mv, 0);
                originalJump.accept(Opcodes.IF_ICMPGE);
                break;
            case Opcodes.IFGT:
                BytecodeUtils.pushInteger(mv, 0);
                originalJump.accept(Opcodes.IF_ICMPGT);
                break;
            case Opcodes.IFLE:
                BytecodeUtils.pushInteger(mv, 0);
                originalJump.accept(Opcodes.IF_ICMPLE);
                break;
            default:
                originalJump.accept(opcode);
                break;
        }
    }

    private boolean shouldObfuscateJumps()
    {
        return strategy == ObfuscationStrategy.MIXED || strategy == ObfuscationStrategy.STACK_MANIPULATION;
    }

    public enum ObfuscationStrategy
    {
        ARITHMETIC,
        BITWISE,
        STACK_MANIPULATION,
        MIXED
    }
}