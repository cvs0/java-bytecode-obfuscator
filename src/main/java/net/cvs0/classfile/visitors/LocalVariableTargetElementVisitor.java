package net.cvs0.classfile.visitors;

import org.objectweb.asm.Label;

public interface LocalVariableTargetElementVisitor
{
    void visitLocalVariableTargetElement(LocalVariableTarget element);
    
    void visitLocalVariableRangeTargetElement(LocalVariableRangeTargetElement element);
    
    void visitLocalVariableIndexTargetElement(LocalVariableIndexTargetElement element);
    
    void visitLocalVariableTypeTargetElement(LocalVariableTypeTargetElement element);
    
    public abstract static class LocalVariableTargetElement
    {
        protected final int index;
        
        protected LocalVariableTargetElement(int index)
        {
            this.index = index;
        }
        
        public int getIndex() { return index; }
        
        public abstract ElementKind getElementKind();
        
        public enum ElementKind
        {
            VARIABLE_TARGET,
            RANGE_TARGET,
            INDEX_TARGET,
            TYPE_TARGET
        }
        
        @Override
        public String toString()
        {
            return getClass().getSimpleName() + "[index=" + index + "]";
        }
    }
    
    public static class LocalVariableTarget extends LocalVariableTargetElement
    {
        private final String name;
        private final String descriptor;
        private final String signature;
        private final Label start;
        private final Label end;
        
        public LocalVariableTarget(int index, String name, String descriptor, String signature, Label start, Label end)
        {
            super(index);
            this.name = name;
            this.descriptor = descriptor;
            this.signature = signature;
            this.start = start;
            this.end = end;
        }
        
        public String getName() { return name; }
        public String getDescriptor() { return descriptor; }
        public String getSignature() { return signature; }
        public Label getStart() { return start; }
        public Label getEnd() { return end; }
        
        @Override
        public ElementKind getElementKind() { return ElementKind.VARIABLE_TARGET; }
        
        @Override
        public String toString()
        {
            return "LocalVariableTarget[index=" + index + " name=" + name + " desc=" + descriptor + "]";
        }
    }
    
    public static class LocalVariableRangeTargetElement extends LocalVariableTargetElement
    {
        private final Label[] startLabels;
        private final Label[] endLabels;
        private final int[] indices;
        
        public LocalVariableRangeTargetElement(int index, Label[] startLabels, Label[] endLabels, int[] indices)
        {
            super(index);
            this.startLabels = startLabels;
            this.endLabels = endLabels;
            this.indices = indices;
        }
        
        public Label[] getStartLabels() { return startLabels; }
        public Label[] getEndLabels() { return endLabels; }
        public int[] getIndices() { return indices; }
        
        @Override
        public ElementKind getElementKind() { return ElementKind.RANGE_TARGET; }
        
        @Override
        public String toString()
        {
            return "LocalVariableRangeTargetElement[index=" + index + " ranges=" + indices.length + "]";
        }
    }
    
    public static class LocalVariableIndexTargetElement extends LocalVariableTargetElement
    {
        private final int variableIndex;
        
        public LocalVariableIndexTargetElement(int index, int variableIndex)
        {
            super(index);
            this.variableIndex = variableIndex;
        }
        
        public int getVariableIndex() { return variableIndex; }
        
        @Override
        public ElementKind getElementKind() { return ElementKind.INDEX_TARGET; }
        
        @Override
        public String toString()
        {
            return "LocalVariableIndexTargetElement[index=" + index + " varIndex=" + variableIndex + "]";
        }
    }
    
    public static class LocalVariableTypeTargetElement extends LocalVariableTargetElement
    {
        private final String typeDescriptor;
        private final String typeSignature;
        
        public LocalVariableTypeTargetElement(int index, String typeDescriptor, String typeSignature)
        {
            super(index);
            this.typeDescriptor = typeDescriptor;
            this.typeSignature = typeSignature;
        }
        
        public String getTypeDescriptor() { return typeDescriptor; }
        public String getTypeSignature() { return typeSignature; }
        
        @Override
        public ElementKind getElementKind() { return ElementKind.TYPE_TARGET; }
        
        @Override
        public String toString()
        {
            return "LocalVariableTypeTargetElement[index=" + index + " typeDesc=" + typeDescriptor + "]";
        }
    }
}