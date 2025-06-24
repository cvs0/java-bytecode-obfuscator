package net.cvs0.classfile;

import org.objectweb.asm.Opcodes;

public class VariableInstruction extends Instruction
{
    private final int variable;
    
    public VariableInstruction(int opcode, int variable)
    {
        super(opcode);
        this.variable = variable;
    }
    
    public int getVariable()
    {
        return variable;
    }
    
    @Override
    public String getMnemonic()
    {
        switch (opcode) {
            case Opcodes.ILOAD: return "iload";
            case Opcodes.LLOAD: return "lload";
            case Opcodes.FLOAD: return "fload";
            case Opcodes.DLOAD: return "dload";
            case Opcodes.ALOAD: return "aload";
            case Opcodes.ISTORE: return "istore";
            case Opcodes.LSTORE: return "lstore";
            case Opcodes.FSTORE: return "fstore";
            case Opcodes.DSTORE: return "dstore";
            case Opcodes.ASTORE: return "astore";
            case Opcodes.RET: return "ret";
            case Opcodes.IINC: return "iinc";
            default: return "unknown";
        }
    }
    
    @Override
    public int getSize()
    {
        if (variable <= 3 && (opcode == Opcodes.ILOAD || opcode == Opcodes.LLOAD || 
                              opcode == Opcodes.FLOAD || opcode == Opcodes.DLOAD || 
                              opcode == Opcodes.ALOAD || opcode == Opcodes.ISTORE || 
                              opcode == Opcodes.LSTORE || opcode == Opcodes.FSTORE || 
                              opcode == Opcodes.DSTORE || opcode == Opcodes.ASTORE)) {
            return 1;
        }
        return variable <= 255 ? 2 : 4;
    }
    
    public boolean isLoad()
    {
        return opcode >= Opcodes.ILOAD && opcode <= Opcodes.ALOAD;
    }
    
    public boolean isStore()
    {
        return opcode >= Opcodes.ISTORE && opcode <= Opcodes.ASTORE;
    }
    
    public boolean isIncrement()
    {
        return opcode == Opcodes.IINC;
    }
    
    public boolean isReturn()
    {
        return opcode == Opcodes.RET;
    }
    
    @Override
    public String toString()
    {
        return getMnemonic() + " " + variable;
    }
}