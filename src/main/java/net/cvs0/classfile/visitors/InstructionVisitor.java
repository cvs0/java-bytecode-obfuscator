package net.cvs0.classfile.visitors;

import net.cvs0.classfile.*;
import org.objectweb.asm.Label;

public interface InstructionVisitor
{
    void visitInstruction(Instruction instruction);
    
    void visitConstantInstruction(ConstantInstruction instruction);
    
    void visitVariableInstruction(VariableInstruction instruction);
    
    void visitFieldInstruction(FieldInstruction instruction);
    
    void visitMethodInstruction(MethodInstruction instruction);
    
    void visitTypeInstruction(TypeInstruction instruction);
    
    void visitJumpInstruction(JumpInstruction instruction);
    
    void visitLookupSwitchInstruction(LookupSwitchInstruction instruction);
    
    void visitTableSwitchInstruction(TableSwitchInstruction instruction);
    
    void visitInvokeDynamicInstruction(InvokeDynamicInstruction instruction);
    
    void visitMultiANewArrayInstruction(MultiANewArrayInstruction instruction);
    
    void visitLocalVariableInstruction(LocalVariableInstruction instruction);
    
    void visitIincInstruction(IincInstruction instruction);
    
    void visitLabelInstruction(LabelInstruction instruction);
    
    void visitLineNumberInstruction(LineNumberInstruction instruction);
    
    void visitFrameInstruction(FrameInstruction instruction);
    
    void visitTryCatchBlockInstruction(TryCatchBlockInstruction instruction);
    
    public static class FieldInstruction extends Instruction
    {
        private final String owner;
        private final String name;
        private final String descriptor;
        
        public FieldInstruction(int opcode, String owner, String name, String descriptor)
        {
            super(opcode);
            this.owner = owner;
            this.name = name;
            this.descriptor = descriptor;
        }
        
        public String getOwner() { return owner; }
        public String getName() { return name; }
        public String getDescriptor() { return descriptor; }
        
        @Override
        public String getMnemonic()
        {
            switch (opcode) {
                case 178: return "getstatic";
                case 179: return "putstatic";
                case 180: return "getfield";
                case 181: return "putfield";
                default: return "unknown";
            }
        }
        
        @Override
        public int getSize() { return 3; }
        
        @Override
        public String toString()
        {
            return getMnemonic() + " " + owner + "." + name + " " + descriptor;
        }
    }
    
    public static class MethodInstruction extends Instruction
    {
        private final String owner;
        private final String name;
        private final String descriptor;
        private final boolean isInterface;
        
        public MethodInstruction(int opcode, String owner, String name, String descriptor, boolean isInterface)
        {
            super(opcode);
            this.owner = owner;
            this.name = name;
            this.descriptor = descriptor;
            this.isInterface = isInterface;
        }
        
        public String getOwner() { return owner; }
        public String getName() { return name; }
        public String getDescriptor() { return descriptor; }
        public boolean isInterface() { return isInterface; }
        
        @Override
        public String getMnemonic()
        {
            switch (opcode) {
                case 182: return "invokevirtual";
                case 183: return "invokespecial";
                case 184: return "invokestatic";
                case 185: return "invokeinterface";
                case 186: return "invokedynamic";
                default: return "unknown";
            }
        }
        
        @Override
        public int getSize() { return opcode == 185 ? 5 : 3; }
        
        @Override
        public String toString()
        {
            return getMnemonic() + " " + owner + "." + name + " " + descriptor;
        }
    }
    
    public static class TypeInstruction extends Instruction
    {
        private final String type;
        
        public TypeInstruction(int opcode, String type)
        {
            super(opcode);
            this.type = type;
        }
        
        public String getType() { return type; }
        
        @Override
        public String getMnemonic()
        {
            switch (opcode) {
                case 187: return "new";
                case 189: return "anewarray";
                case 192: return "checkcast";
                case 193: return "instanceof";
                default: return "unknown";
            }
        }
        
        @Override
        public int getSize() { return 3; }
        
        @Override
        public String toString()
        {
            return getMnemonic() + " " + type;
        }
    }
    
    public static class JumpInstruction extends Instruction
    {
        private final Label label;
        
        public JumpInstruction(int opcode, Label label)
        {
            super(opcode);
            this.label = label;
        }
        
        public Label getLabel() { return label; }
        
        @Override
        public String getMnemonic()
        {
            switch (opcode) {
                case 153: return "ifeq";
                case 154: return "ifne";
                case 155: return "iflt";
                case 156: return "ifge";
                case 157: return "ifgt";
                case 158: return "ifle";
                case 159: return "if_icmpeq";
                case 160: return "if_icmpne";
                case 161: return "if_icmplt";
                case 162: return "if_icmpge";
                case 163: return "if_icmpgt";
                case 164: return "if_icmple";
                case 165: return "if_acmpeq";
                case 166: return "if_acmpne";
                case 167: return "goto";
                case 168: return "jsr";
                case 198: return "ifnull";
                case 199: return "ifnonnull";
                default: return "unknown";
            }
        }
        
        @Override
        public int getSize() { return 3; }
        
        @Override
        public String toString()
        {
            return getMnemonic() + " " + label;
        }
    }
    
    public static class LookupSwitchInstruction extends Instruction
    {
        private final Label defaultLabel;
        private final int[] keys;
        private final Label[] labels;
        
        public LookupSwitchInstruction(Label defaultLabel, int[] keys, Label[] labels)
        {
            super(171);
            this.defaultLabel = defaultLabel;
            this.keys = keys;
            this.labels = labels;
        }
        
        public Label getDefaultLabel() { return defaultLabel; }
        public int[] getKeys() { return keys; }
        public Label[] getLabels() { return labels; }
        
        @Override
        public String getMnemonic() { return "lookupswitch"; }
        
        @Override
        public int getSize() { return 9 + keys.length * 8; }
    }
    
    public static class TableSwitchInstruction extends Instruction
    {
        private final int min;
        private final int max;
        private final Label defaultLabel;
        private final Label[] labels;
        
        public TableSwitchInstruction(int min, int max, Label defaultLabel, Label[] labels)
        {
            super(170);
            this.min = min;
            this.max = max;
            this.defaultLabel = defaultLabel;
            this.labels = labels;
        }
        
        public int getMin() { return min; }
        public int getMax() { return max; }
        public Label getDefaultLabel() { return defaultLabel; }
        public Label[] getLabels() { return labels; }
        
        @Override
        public String getMnemonic() { return "tableswitch"; }
        
        @Override
        public int getSize() { return 13 + labels.length * 4; }
    }
    
    public static class InvokeDynamicInstruction extends Instruction
    {
        private final String name;
        private final String descriptor;
        private final Object bootstrapMethodHandle;
        private final Object[] bootstrapMethodArguments;
        
        public InvokeDynamicInstruction(String name, String descriptor, Object bootstrapMethodHandle, Object[] bootstrapMethodArguments)
        {
            super(186);
            this.name = name;
            this.descriptor = descriptor;
            this.bootstrapMethodHandle = bootstrapMethodHandle;
            this.bootstrapMethodArguments = bootstrapMethodArguments;
        }
        
        public String getName() { return name; }
        public String getDescriptor() { return descriptor; }
        public Object getBootstrapMethodHandle() { return bootstrapMethodHandle; }
        public Object[] getBootstrapMethodArguments() { return bootstrapMethodArguments; }
        
        @Override
        public String getMnemonic() { return "invokedynamic"; }
        
        @Override
        public int getSize() { return 5; }
    }
    
    public static class MultiANewArrayInstruction extends Instruction
    {
        private final String type;
        private final int dimensions;
        
        public MultiANewArrayInstruction(String type, int dimensions)
        {
            super(197);
            this.type = type;
            this.dimensions = dimensions;
        }
        
        public String getType() { return type; }
        public int getDimensions() { return dimensions; }
        
        @Override
        public String getMnemonic() { return "multianewarray"; }
        
        @Override
        public int getSize() { return 4; }
    }
    
    public static class LocalVariableInstruction extends Instruction
    {
        private final int variable;
        
        public LocalVariableInstruction(int opcode, int variable)
        {
            super(opcode);
            this.variable = variable;
        }
        
        public int getVariable() { return variable; }
        
        @Override
        public String getMnemonic() { return "localvar_" + opcode; }
        
        @Override
        public int getSize() { return 2; }
    }
    
    public static class IincInstruction extends Instruction
    {
        private final int variable;
        private final int increment;
        
        public IincInstruction(int variable, int increment)
        {
            super(132);
            this.variable = variable;
            this.increment = increment;
        }
        
        public int getVariable() { return variable; }
        public int getIncrement() { return increment; }
        
        @Override
        public String getMnemonic() { return "iinc"; }
        
        @Override
        public int getSize() { return 3; }
    }
    
    public static class LabelInstruction extends Instruction
    {
        private final Label label;
        
        public LabelInstruction(Label label)
        {
            super(-1);
            this.label = label;
        }
        
        public Label getLabel() { return label; }
        
        @Override
        public String getMnemonic() { return "label"; }
        
        @Override
        public int getSize() { return 0; }
    }
    
    public static class LineNumberInstruction extends Instruction
    {
        private final int line;
        private final Label start;
        
        public LineNumberInstruction(int line, Label start)
        {
            super(-2);
            this.line = line;
            this.start = start;
        }
        
        public int getLine() { return line; }
        public Label getStart() { return start; }
        
        @Override
        public String getMnemonic() { return "linenumber"; }
        
        @Override
        public int getSize() { return 0; }
    }
    
    public static class FrameInstruction extends Instruction
    {
        private final int type;
        private final int numLocal;
        private final Object[] local;
        private final int numStack;
        private final Object[] stack;
        
        public FrameInstruction(int type, int numLocal, Object[] local, int numStack, Object[] stack)
        {
            super(-3);
            this.type = type;
            this.numLocal = numLocal;
            this.local = local;
            this.numStack = numStack;
            this.stack = stack;
        }
        
        public int getType() { return type; }
        public int getNumLocal() { return numLocal; }
        public Object[] getLocal() { return local; }
        public int getNumStack() { return numStack; }
        public Object[] getStack() { return stack; }
        
        @Override
        public String getMnemonic() { return "frame"; }
        
        @Override
        public int getSize() { return 0; }
    }
    
    public static class TryCatchBlockInstruction extends Instruction
    {
        private final Label start;
        private final Label end;
        private final Label handler;
        private final String type;
        
        public TryCatchBlockInstruction(Label start, Label end, Label handler, String type)
        {
            super(-4);
            this.start = start;
            this.end = end;
            this.handler = handler;
            this.type = type;
        }
        
        public Label getStart() { return start; }
        public Label getEnd() { return end; }
        public Label getHandler() { return handler; }
        public String getType() { return type; }
        
        @Override
        public String getMnemonic() { return "trycatch"; }
        
        @Override
        public int getSize() { return 0; }
    }
}