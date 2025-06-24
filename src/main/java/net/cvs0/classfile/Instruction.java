package net.cvs0.classfile;

public abstract class Instruction
{
    protected final int opcode;
    
    protected Instruction(int opcode)
    {
        this.opcode = opcode;
    }
    
    public int getOpcode()
    {
        return opcode;
    }
    
    public abstract String getMnemonic();
    
    public abstract int getSize();
    
    public boolean isJumpInstruction()
    {
        return opcode >= 153 && opcode <= 168;
    }
    
    public boolean isReturnInstruction()
    {
        return (opcode >= 172 && opcode <= 177) || opcode == 191;
    }
    
    public boolean isLoadInstruction()
    {
        return (opcode >= 21 && opcode <= 25) || (opcode >= 42 && opcode <= 53);
    }
    
    public boolean isStoreInstruction()
    {
        return (opcode >= 54 && opcode <= 58) || (opcode >= 75 && opcode <= 86);
    }
    
    public boolean isFieldInstruction()
    {
        return opcode >= 178 && opcode <= 181;
    }
    
    public boolean isMethodInstruction()
    {
        return (opcode >= 182 && opcode <= 186) || opcode == 169;
    }
    
    public boolean isConstantInstruction()
    {
        return (opcode >= 1 && opcode <= 20) || opcode == 18 || opcode == 19 || opcode == 20;
    }
    
    public boolean isArithmeticInstruction()
    {
        return opcode >= 96 && opcode <= 132;
    }
    
    public boolean isArrayInstruction()
    {
        return (opcode >= 46 && opcode <= 53) || (opcode >= 79 && opcode <= 86) || 
               opcode == 188 || opcode == 189 || opcode == 197;
    }
    
    public boolean isStackInstruction()
    {
        return opcode >= 87 && opcode <= 95;
    }
    
    public boolean isConversionInstruction()
    {
        return opcode >= 133 && opcode <= 147;
    }
    
    public boolean isComparisonInstruction()
    {
        return opcode >= 148 && opcode <= 166;
    }
    
    public boolean isObjectInstruction()
    {
        return opcode == 187 || opcode == 192 || opcode == 193 || opcode == 194 || opcode == 195;
    }
    
    @Override
    public String toString()
    {
        return getMnemonic();
    }
}