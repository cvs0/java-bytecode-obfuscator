package net.cvs0.utils;

import net.cvs0.context.ObfuscationContext;
import net.cvs0.core.AbstractMethodVisitor;
import org.objectweb.asm.MethodVisitor;

public class ConditionObfuscationMethodVisitor extends AbstractMethodVisitor
{
    private final InstructionObfuscator obfuscator;
    private final float obfuscationProbability;

    public ConditionObfuscationMethodVisitor(MethodVisitor methodVisitor, ObfuscationContext context, 
                                           String className, String methodName, String methodDescriptor)
    {
        this(methodVisitor, context, className, methodName, methodDescriptor, 0.1f);
    }

    public ConditionObfuscationMethodVisitor(MethodVisitor methodVisitor, ObfuscationContext context, 
                                           String className, String methodName, String methodDescriptor,
                                           float obfuscationProbability)
    {
        super(methodVisitor, context, className, methodName, methodDescriptor);
        this.obfuscationProbability = obfuscationProbability;
        this.obfuscator = new InstructionObfuscator(random, InstructionObfuscator.ObfuscationStrategy.MIXED);
    }

    @Override
    public void visitInsn(int opcode)
    {
        if (shouldObfuscateConstant(opcode) && shouldProcessWithProbability(obfuscationProbability)) {
            obfuscator.obfuscateConstant(super.mv, opcode);
            logTransformation("Obfuscated constant " + BytecodeUtils.getOpcodeString(opcode));
        } else {
            super.visitInsn(opcode);
        }
    }

    private boolean shouldObfuscateConstant(int opcode)
    {
        return BytecodeUtils.isIntegerConstant(opcode) && shouldProcessInstruction();
    }

    @Override
    protected String getTransformerName()
    {
        return "ConditionObfuscation";
    }

    public void setObfuscationStrategy(InstructionObfuscator.ObfuscationStrategy strategy)
    {
        InstructionObfuscator newObfuscator = new InstructionObfuscator(random, strategy);
    }
}