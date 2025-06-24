package net.cvs0.classfile.visitors;

import org.objectweb.asm.Label;

public interface TargetInfoVisitor
{
    void visitTargetInfo(TargetInfo targetInfo);
    
    void visitTypeParameterTarget(TypeParameterTarget typeParameterTarget);
    
    void visitSupertypeTarget(SupertypeTarget supertypeTarget);
    
    void visitTypeParameterBoundTarget(TypeParameterBoundTarget typeParameterBoundTarget);
    
    void visitEmptyTarget(EmptyTarget emptyTarget);
    
    void visitFormalParameterTarget(FormalParameterTarget formalParameterTarget);
    
    void visitThrowsTarget(ThrowsTarget throwsTarget);
    
    void visitLocalVarTarget(LocalVarTarget localVarTarget);
    
    void visitCatchTarget(CatchTarget catchTarget);
    
    void visitOffsetTarget(OffsetTarget offsetTarget);
    
    void visitTypeArgumentTarget(TypeArgumentTarget typeArgumentTarget);
    
    public abstract static class TargetInfo
    {
        protected final int targetType;
        
        protected TargetInfo(int targetType)
        {
            this.targetType = targetType;
        }
        
        public int getTargetType() { return targetType; }
        
        public abstract TargetKind getTargetKind();
        
        public enum TargetKind
        {
            TYPE_PARAMETER,
            SUPERTYPE,
            TYPE_PARAMETER_BOUND,
            EMPTY,
            FORMAL_PARAMETER,
            THROWS,
            LOCAL_VAR,
            CATCH,
            OFFSET,
            TYPE_ARGUMENT
        }
        
        @Override
        public String toString()
        {
            return getClass().getSimpleName() + "[targetType=" + targetType + "]";
        }
    }
    
    public static class TypeParameterTarget extends TargetInfo
    {
        private final int typeParameterIndex;
        
        public TypeParameterTarget(int targetType, int typeParameterIndex)
        {
            super(targetType);
            this.typeParameterIndex = typeParameterIndex;
        }
        
        public int getTypeParameterIndex() { return typeParameterIndex; }
        
        @Override
        public TargetKind getTargetKind() { return TargetKind.TYPE_PARAMETER; }
        
        @Override
        public String toString()
        {
            return "TypeParameterTarget[targetType=" + targetType + " index=" + typeParameterIndex + "]";
        }
    }
    
    public static class SupertypeTarget extends TargetInfo
    {
        private final int supertypeIndex;
        
        public SupertypeTarget(int targetType, int supertypeIndex)
        {
            super(targetType);
            this.supertypeIndex = supertypeIndex;
        }
        
        public int getSupertypeIndex() { return supertypeIndex; }
        
        @Override
        public TargetKind getTargetKind() { return TargetKind.SUPERTYPE; }
        
        @Override
        public String toString()
        {
            return "SupertypeTarget[targetType=" + targetType + " index=" + supertypeIndex + "]";
        }
    }
    
    public static class TypeParameterBoundTarget extends TargetInfo
    {
        private final int typeParameterIndex;
        private final int boundIndex;
        
        public TypeParameterBoundTarget(int targetType, int typeParameterIndex, int boundIndex)
        {
            super(targetType);
            this.typeParameterIndex = typeParameterIndex;
            this.boundIndex = boundIndex;
        }
        
        public int getTypeParameterIndex() { return typeParameterIndex; }
        public int getBoundIndex() { return boundIndex; }
        
        @Override
        public TargetKind getTargetKind() { return TargetKind.TYPE_PARAMETER_BOUND; }
        
        @Override
        public String toString()
        {
            return "TypeParameterBoundTarget[targetType=" + targetType + " paramIndex=" + typeParameterIndex + " boundIndex=" + boundIndex + "]";
        }
    }
    
    public static class EmptyTarget extends TargetInfo
    {
        public EmptyTarget(int targetType)
        {
            super(targetType);
        }
        
        @Override
        public TargetKind getTargetKind() { return TargetKind.EMPTY; }
        
        @Override
        public String toString()
        {
            return "EmptyTarget[targetType=" + targetType + "]";
        }
    }
    
    public static class FormalParameterTarget extends TargetInfo
    {
        private final int formalParameterIndex;
        
        public FormalParameterTarget(int targetType, int formalParameterIndex)
        {
            super(targetType);
            this.formalParameterIndex = formalParameterIndex;
        }
        
        public int getFormalParameterIndex() { return formalParameterIndex; }
        
        @Override
        public TargetKind getTargetKind() { return TargetKind.FORMAL_PARAMETER; }
        
        @Override
        public String toString()
        {
            return "FormalParameterTarget[targetType=" + targetType + " index=" + formalParameterIndex + "]";
        }
    }
    
    public static class ThrowsTarget extends TargetInfo
    {
        private final int throwsTypeIndex;
        
        public ThrowsTarget(int targetType, int throwsTypeIndex)
        {
            super(targetType);
            this.throwsTypeIndex = throwsTypeIndex;
        }
        
        public int getThrowsTypeIndex() { return throwsTypeIndex; }
        
        @Override
        public TargetKind getTargetKind() { return TargetKind.THROWS; }
        
        @Override
        public String toString()
        {
            return "ThrowsTarget[targetType=" + targetType + " index=" + throwsTypeIndex + "]";
        }
    }
    
    public static class LocalVarTarget extends TargetInfo
    {
        private final Label[] start;
        private final Label[] end;
        private final int[] index;
        
        public LocalVarTarget(int targetType, Label[] start, Label[] end, int[] index)
        {
            super(targetType);
            this.start = start;
            this.end = end;
            this.index = index;
        }
        
        public Label[] getStart() { return start; }
        public Label[] getEnd() { return end; }
        public int[] getIndex() { return index; }
        
        @Override
        public TargetKind getTargetKind() { return TargetKind.LOCAL_VAR; }
        
        @Override
        public String toString()
        {
            return "LocalVarTarget[targetType=" + targetType + " vars=" + index.length + "]";
        }
    }
    
    public static class CatchTarget extends TargetInfo
    {
        private final int exceptionTableIndex;
        
        public CatchTarget(int targetType, int exceptionTableIndex)
        {
            super(targetType);
            this.exceptionTableIndex = exceptionTableIndex;
        }
        
        public int getExceptionTableIndex() { return exceptionTableIndex; }
        
        @Override
        public TargetKind getTargetKind() { return TargetKind.CATCH; }
        
        @Override
        public String toString()
        {
            return "CatchTarget[targetType=" + targetType + " index=" + exceptionTableIndex + "]";
        }
    }
    
    public static class OffsetTarget extends TargetInfo
    {
        private final Label label;
        
        public OffsetTarget(int targetType, Label label)
        {
            super(targetType);
            this.label = label;
        }
        
        public Label getLabel() { return label; }
        
        @Override
        public TargetKind getTargetKind() { return TargetKind.OFFSET; }
        
        @Override
        public String toString()
        {
            return "OffsetTarget[targetType=" + targetType + " label=" + label + "]";
        }
    }
    
    public static class TypeArgumentTarget extends TargetInfo
    {
        private final Label label;
        private final int typeArgumentIndex;
        
        public TypeArgumentTarget(int targetType, Label label, int typeArgumentIndex)
        {
            super(targetType);
            this.label = label;
            this.typeArgumentIndex = typeArgumentIndex;
        }
        
        public Label getLabel() { return label; }
        public int getTypeArgumentIndex() { return typeArgumentIndex; }
        
        @Override
        public TargetKind getTargetKind() { return TargetKind.TYPE_ARGUMENT; }
        
        @Override
        public String toString()
        {
            return "TypeArgumentTarget[targetType=" + targetType + " label=" + label + " argIndex=" + typeArgumentIndex + "]";
        }
    }
}