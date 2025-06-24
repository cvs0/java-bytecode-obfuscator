package net.cvs0.classfile.visitors;

import net.cvs0.classfile.Attribute;

public interface AttributeVisitor
{
    void visitAttribute(Attribute attribute);
    
    void visitCodeAttribute(CodeAttribute codeAttribute);
    
    void visitLineNumberTableAttribute(LineNumberTableAttribute lineNumberTableAttribute);
    
    void visitLocalVariableTableAttribute(LocalVariableTableAttribute localVariableTableAttribute);
    
    void visitLocalVariableTypeTableAttribute(LocalVariableTypeTableAttribute localVariableTypeTableAttribute);
    
    void visitSourceFileAttribute(SourceFileAttribute sourceFileAttribute);
    
    void visitInnerClassesAttribute(InnerClassesAttribute innerClassesAttribute);
    
    void visitEnclosingMethodAttribute(EnclosingMethodAttribute enclosingMethodAttribute);
    
    void visitDeprecatedAttribute(DeprecatedAttribute deprecatedAttribute);
    
    void visitSyntheticAttribute(SyntheticAttribute syntheticAttribute);
    
    void visitSignatureAttribute(SignatureAttribute signatureAttribute);
    
    void visitRuntimeVisibleAnnotationsAttribute(RuntimeVisibleAnnotationsAttribute runtimeVisibleAnnotationsAttribute);
    
    void visitRuntimeInvisibleAnnotationsAttribute(RuntimeInvisibleAnnotationsAttribute runtimeInvisibleAnnotationsAttribute);
    
    void visitRuntimeVisibleParameterAnnotationsAttribute(RuntimeVisibleParameterAnnotationsAttribute runtimeVisibleParameterAnnotationsAttribute);
    
    void visitRuntimeInvisibleParameterAnnotationsAttribute(RuntimeInvisibleParameterAnnotationsAttribute runtimeInvisibleParameterAnnotationsAttribute);
    
    void visitRuntimeVisibleTypeAnnotationsAttribute(RuntimeVisibleTypeAnnotationsAttribute runtimeVisibleTypeAnnotationsAttribute);
    
    void visitRuntimeInvisibleTypeAnnotationsAttribute(RuntimeInvisibleTypeAnnotationsAttribute runtimeInvisibleTypeAnnotationsAttribute);
    
    void visitAnnotationDefaultAttribute(AnnotationDefaultAttribute annotationDefaultAttribute);
    
    void visitBootstrapMethodsAttribute(BootstrapMethodsAttribute bootstrapMethodsAttribute);
    
    void visitMethodParametersAttribute(net.cvs0.classfile.MethodParametersAttribute methodParametersAttribute);
    
    void visitUnknownAttribute(UnknownAttribute unknownAttribute);
    
    public static class CodeAttribute extends Attribute
    {
        public CodeAttribute() { super("Code"); }
        @Override public byte[] getData() { return new byte[0]; }
        @Override public int getLength() { return 0; }
    }
    
    public static class LineNumberTableAttribute extends Attribute
    {
        public LineNumberTableAttribute() { super("LineNumberTable"); }
        @Override public byte[] getData() { return new byte[0]; }
        @Override public int getLength() { return 0; }
    }
    
    public static class LocalVariableTableAttribute extends Attribute
    {
        public LocalVariableTableAttribute() { super("LocalVariableTable"); }
        @Override public byte[] getData() { return new byte[0]; }
        @Override public int getLength() { return 0; }
    }
    
    public static class LocalVariableTypeTableAttribute extends Attribute
    {
        public LocalVariableTypeTableAttribute() { super("LocalVariableTypeTable"); }
        @Override public byte[] getData() { return new byte[0]; }
        @Override public int getLength() { return 0; }
    }
    
    public static class SourceFileAttribute extends Attribute
    {
        public SourceFileAttribute() { super("SourceFile"); }
        @Override public byte[] getData() { return new byte[0]; }
        @Override public int getLength() { return 0; }
    }
    
    public static class InnerClassesAttribute extends Attribute
    {
        public InnerClassesAttribute() { super("InnerClasses"); }
        @Override public byte[] getData() { return new byte[0]; }
        @Override public int getLength() { return 0; }
    }
    
    public static class EnclosingMethodAttribute extends Attribute
    {
        public EnclosingMethodAttribute() { super("EnclosingMethod"); }
        @Override public byte[] getData() { return new byte[0]; }
        @Override public int getLength() { return 0; }
    }
    
    public static class DeprecatedAttribute extends Attribute
    {
        public DeprecatedAttribute() { super("Deprecated"); }
        @Override public byte[] getData() { return new byte[0]; }
        @Override public int getLength() { return 0; }
    }
    
    public static class SyntheticAttribute extends Attribute
    {
        public SyntheticAttribute() { super("Synthetic"); }
        @Override public byte[] getData() { return new byte[0]; }
        @Override public int getLength() { return 0; }
    }
    
    public static class SignatureAttribute extends Attribute
    {
        public SignatureAttribute() { super("Signature"); }
        @Override public byte[] getData() { return new byte[0]; }
        @Override public int getLength() { return 0; }
    }
    
    public static class RuntimeVisibleAnnotationsAttribute extends Attribute
    {
        public RuntimeVisibleAnnotationsAttribute() { super("RuntimeVisibleAnnotations"); }
        @Override public byte[] getData() { return new byte[0]; }
        @Override public int getLength() { return 0; }
    }
    
    public static class RuntimeInvisibleAnnotationsAttribute extends Attribute
    {
        public RuntimeInvisibleAnnotationsAttribute() { super("RuntimeInvisibleAnnotations"); }
        @Override public byte[] getData() { return new byte[0]; }
        @Override public int getLength() { return 0; }
    }
    
    public static class RuntimeVisibleParameterAnnotationsAttribute extends Attribute
    {
        public RuntimeVisibleParameterAnnotationsAttribute() { super("RuntimeVisibleParameterAnnotations"); }
        @Override public byte[] getData() { return new byte[0]; }
        @Override public int getLength() { return 0; }
    }
    
    public static class RuntimeInvisibleParameterAnnotationsAttribute extends Attribute
    {
        public RuntimeInvisibleParameterAnnotationsAttribute() { super("RuntimeInvisibleParameterAnnotations"); }
        @Override public byte[] getData() { return new byte[0]; }
        @Override public int getLength() { return 0; }
    }
    
    public static class RuntimeVisibleTypeAnnotationsAttribute extends Attribute
    {
        public RuntimeVisibleTypeAnnotationsAttribute() { super("RuntimeVisibleTypeAnnotations"); }
        @Override public byte[] getData() { return new byte[0]; }
        @Override public int getLength() { return 0; }
    }
    
    public static class RuntimeInvisibleTypeAnnotationsAttribute extends Attribute
    {
        public RuntimeInvisibleTypeAnnotationsAttribute() { super("RuntimeInvisibleTypeAnnotations"); }
        @Override public byte[] getData() { return new byte[0]; }
        @Override public int getLength() { return 0; }
    }
    
    public static class AnnotationDefaultAttribute extends Attribute
    {
        public AnnotationDefaultAttribute() { super("AnnotationDefault"); }
        @Override public byte[] getData() { return new byte[0]; }
        @Override public int getLength() { return 0; }
    }
    
    public static class BootstrapMethodsAttribute extends Attribute
    {
        public BootstrapMethodsAttribute() { super("BootstrapMethods"); }
        @Override public byte[] getData() { return new byte[0]; }
        @Override public int getLength() { return 0; }
    }
    
    public static class UnknownAttribute extends Attribute
    {
        public UnknownAttribute(String name) { super(name); }
        @Override public byte[] getData() { return new byte[0]; }
        @Override public int getLength() { return 0; }
    }
}