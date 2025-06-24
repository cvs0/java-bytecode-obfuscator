package net.cvs0.classfile.visitors;

import org.objectweb.asm.Label;

public interface LocalVariableInfoVisitor
{
    void visitLocalVariable(LocalVariableInfo localVariableInfo);
    
    void visitLocalVariableType(LocalVariableTypeInfo localVariableTypeInfo);
    
    void visitParameter(ParameterInfo parameterInfo);
    
    public static class LocalVariableInfo
    {
        private final String name;
        private final String descriptor;
        private final String signature;
        private final Label start;
        private final Label end;
        private final int index;
        
        public LocalVariableInfo(String name, String descriptor, String signature, Label start, Label end, int index)
        {
            this.name = name;
            this.descriptor = descriptor;
            this.signature = signature;
            this.start = start;
            this.end = end;
            this.index = index;
        }
        
        public String getName() { return name; }
        public String getDescriptor() { return descriptor; }
        public String getSignature() { return signature; }
        public Label getStart() { return start; }
        public Label getEnd() { return end; }
        public int getIndex() { return index; }
        
        @Override
        public String toString()
        {
            return "LocalVariable[" + name + " " + descriptor + " index=" + index + "]";
        }
    }
    
    public static class LocalVariableTypeInfo
    {
        private final String name;
        private final String signature;
        private final Label start;
        private final Label end;
        private final int index;
        
        public LocalVariableTypeInfo(String name, String signature, Label start, Label end, int index)
        {
            this.name = name;
            this.signature = signature;
            this.start = start;
            this.end = end;
            this.index = index;
        }
        
        public String getName() { return name; }
        public String getSignature() { return signature; }
        public Label getStart() { return start; }
        public Label getEnd() { return end; }
        public int getIndex() { return index; }
        
        @Override
        public String toString()
        {
            return "LocalVariableType[" + name + " " + signature + " index=" + index + "]";
        }
    }
    
    public static class ParameterInfo
    {
        private final String name;
        private final int access;
        
        public ParameterInfo(String name, int access)
        {
            this.name = name;
            this.access = access;
        }
        
        public String getName() { return name; }
        public int getAccess() { return access; }
        
        public boolean isFinal() { return (access & 0x0010) != 0; }
        public boolean isSynthetic() { return (access & 0x1000) != 0; }
        public boolean isMandated() { return (access & 0x8000) != 0; }
        
        @Override
        public String toString()
        {
            StringBuilder sb = new StringBuilder("Parameter[");
            if (isFinal()) sb.append("final ");
            if (isSynthetic()) sb.append("synthetic ");
            if (isMandated()) sb.append("mandated ");
            sb.append(name).append("]");
            return sb.toString();
        }
    }
}