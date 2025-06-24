package net.cvs0.classfile.visitors;

import org.objectweb.asm.TypePath;

public interface TypeAnnotationVisitor
{
    void visitTypeAnnotation(TypeAnnotation typeAnnotation);
    
    void visitParameterAnnotation(ParameterAnnotation parameterAnnotation);
    
    void visitLocalVariableAnnotation(LocalVariableAnnotation localVariableAnnotation);
    
    void visitFieldAnnotation(FieldAnnotation fieldAnnotation);
    
    void visitMethodAnnotation(MethodAnnotation methodAnnotation);
    
    void visitClassAnnotation(ClassAnnotation classAnnotation);
    
    public abstract static class TypeAnnotation
    {
        protected final int typeRef;
        protected final TypePath typePath;
        protected final String descriptor;
        protected final boolean visible;
        
        protected TypeAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible)
        {
            this.typeRef = typeRef;
            this.typePath = typePath;
            this.descriptor = descriptor;
            this.visible = visible;
        }
        
        public int getTypeRef() { return typeRef; }
        public TypePath getTypePath() { return typePath; }
        public String getDescriptor() { return descriptor; }
        public boolean isVisible() { return visible; }
        
        public abstract AnnotationType getAnnotationType();
        
        public enum AnnotationType
        {
            PARAMETER,
            LOCAL_VARIABLE,
            FIELD,
            METHOD,
            CLASS,
            GENERIC
        }
        
        @Override
        public String toString()
        {
            return getClass().getSimpleName() + "[" + descriptor + " visible=" + visible + "]";
        }
    }
    
    public static class ParameterAnnotation extends TypeAnnotation
    {
        private final int parameterIndex;
        
        public ParameterAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible, int parameterIndex)
        {
            super(typeRef, typePath, descriptor, visible);
            this.parameterIndex = parameterIndex;
        }
        
        public int getParameterIndex() { return parameterIndex; }
        
        @Override
        public AnnotationType getAnnotationType() { return AnnotationType.PARAMETER; }
        
        @Override
        public String toString()
        {
            return "ParameterAnnotation[param=" + parameterIndex + " " + descriptor + " visible=" + visible + "]";
        }
    }
    
    public static class LocalVariableAnnotation extends TypeAnnotation
    {
        private final org.objectweb.asm.Label[] start;
        private final org.objectweb.asm.Label[] end;
        private final int[] index;
        
        public LocalVariableAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible, 
                                     org.objectweb.asm.Label[] start, org.objectweb.asm.Label[] end, int[] index)
        {
            super(typeRef, typePath, descriptor, visible);
            this.start = start;
            this.end = end;
            this.index = index;
        }
        
        public org.objectweb.asm.Label[] getStart() { return start; }
        public org.objectweb.asm.Label[] getEnd() { return end; }
        public int[] getIndex() { return index; }
        
        @Override
        public AnnotationType getAnnotationType() { return AnnotationType.LOCAL_VARIABLE; }
        
        @Override
        public String toString()
        {
            return "LocalVariableAnnotation[" + descriptor + " visible=" + visible + " vars=" + index.length + "]";
        }
    }
    
    public static class FieldAnnotation extends TypeAnnotation
    {
        private final String fieldName;
        private final String fieldDescriptor;
        
        public FieldAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible, String fieldName, String fieldDescriptor)
        {
            super(typeRef, typePath, descriptor, visible);
            this.fieldName = fieldName;
            this.fieldDescriptor = fieldDescriptor;
        }
        
        public String getFieldName() { return fieldName; }
        public String getFieldDescriptor() { return fieldDescriptor; }
        
        @Override
        public AnnotationType getAnnotationType() { return AnnotationType.FIELD; }
        
        @Override
        public String toString()
        {
            return "FieldAnnotation[field=" + fieldName + " " + descriptor + " visible=" + visible + "]";
        }
    }
    
    public static class MethodAnnotation extends TypeAnnotation
    {
        private final String methodName;
        private final String methodDescriptor;
        
        public MethodAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible, String methodName, String methodDescriptor)
        {
            super(typeRef, typePath, descriptor, visible);
            this.methodName = methodName;
            this.methodDescriptor = methodDescriptor;
        }
        
        public String getMethodName() { return methodName; }
        public String getMethodDescriptor() { return methodDescriptor; }
        
        @Override
        public AnnotationType getAnnotationType() { return AnnotationType.METHOD; }
        
        @Override
        public String toString()
        {
            return "MethodAnnotation[method=" + methodName + " " + descriptor + " visible=" + visible + "]";
        }
    }
    
    public static class ClassAnnotation extends TypeAnnotation
    {
        private final String className;
        
        public ClassAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible, String className)
        {
            super(typeRef, typePath, descriptor, visible);
            this.className = className;
        }
        
        public String getClassName() { return className; }
        
        @Override
        public AnnotationType getAnnotationType() { return AnnotationType.CLASS; }
        
        @Override
        public String toString()
        {
            return "ClassAnnotation[class=" + className + " " + descriptor + " visible=" + visible + "]";
        }
    }
}