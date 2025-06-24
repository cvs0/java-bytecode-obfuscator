package net.cvs0.classfile.visitors;

import org.objectweb.asm.Label;
import org.objectweb.asm.Type;

public interface LocalVariableTypeInfoVisitor
{
    void visitLocalVariableTypeInfo(LocalVariableTypeInfo localVariableTypeInfo);
    
    void visitGenericLocalVariable(GenericLocalVariableInfo genericLocalVariableInfo);
    
    void visitTypeParameter(TypeParameterInfo typeParameterInfo);
    
    void visitTypeArgument(TypeArgumentInfo typeArgumentInfo);
    
    public static class LocalVariableTypeInfo
    {
        private final String name;
        private final String signature;
        private final Label start;
        private final Label end;
        private final int index;
        private final Type type;
        
        public LocalVariableTypeInfo(String name, String signature, Label start, Label end, int index)
        {
            this.name = name;
            this.signature = signature;
            this.start = start;
            this.end = end;
            this.index = index;
            this.type = signature != null ? Type.getType(signature) : null;
        }
        
        public String getName() { return name; }
        public String getSignature() { return signature; }
        public Label getStart() { return start; }
        public Label getEnd() { return end; }
        public int getIndex() { return index; }
        public Type getType() { return type; }
        
        public boolean hasGenericSignature() { return signature != null && !signature.isEmpty(); }
        
        @Override
        public String toString()
        {
            return "LocalVariableTypeInfo[" + name + " " + signature + " index=" + index + "]";
        }
    }
    
    public static class GenericLocalVariableInfo extends LocalVariableTypeInfo
    {
        private final String[] typeParameters;
        private final String[] bounds;
        
        public GenericLocalVariableInfo(String name, String signature, Label start, Label end, int index, String[] typeParameters, String[] bounds)
        {
            super(name, signature, start, end, index);
            this.typeParameters = typeParameters;
            this.bounds = bounds;
        }
        
        public String[] getTypeParameters() { return typeParameters; }
        public String[] getBounds() { return bounds; }
        
        public boolean hasTypeParameters() { return typeParameters != null && typeParameters.length > 0; }
        public boolean hasBounds() { return bounds != null && bounds.length > 0; }
        
        @Override
        public String toString()
        {
            StringBuilder sb = new StringBuilder("GenericLocalVariableInfo[");
            sb.append(getName()).append(" ").append(getSignature());
            if (hasTypeParameters()) {
                sb.append(" typeParams=").append(java.util.Arrays.toString(typeParameters));
            }
            if (hasBounds()) {
                sb.append(" bounds=").append(java.util.Arrays.toString(bounds));
            }
            sb.append(" index=").append(getIndex()).append("]");
            return sb.toString();
        }
    }
    
    public static class TypeParameterInfo
    {
        private final String name;
        private final String[] bounds;
        private final int index;
        
        public TypeParameterInfo(String name, String[] bounds, int index)
        {
            this.name = name;
            this.bounds = bounds;
            this.index = index;
        }
        
        public String getName() { return name; }
        public String[] getBounds() { return bounds; }
        public int getIndex() { return index; }
        
        public boolean hasBounds() { return bounds != null && bounds.length > 0; }
        
        @Override
        public String toString()
        {
            StringBuilder sb = new StringBuilder("TypeParameter[");
            sb.append(name);
            if (hasBounds()) {
                sb.append(" extends ").append(String.join(" & ", bounds));
            }
            sb.append("]");
            return sb.toString();
        }
    }
    
    public static class TypeArgumentInfo
    {
        private final String signature;
        private final WildcardType wildcardType;
        private final Type type;
        
        public TypeArgumentInfo(String signature, WildcardType wildcardType)
        {
            this.signature = signature;
            this.wildcardType = wildcardType;
            this.type = signature != null ? Type.getType(signature) : null;
        }
        
        public String getSignature() { return signature; }
        public WildcardType getWildcardType() { return wildcardType; }
        public Type getType() { return type; }
        
        public boolean isWildcard() { return wildcardType != WildcardType.NONE; }
        
        public enum WildcardType
        {
            NONE,
            EXTENDS,
            SUPER,
            UNBOUNDED
        }
        
        @Override
        public String toString()
        {
            StringBuilder sb = new StringBuilder("TypeArgument[");
            if (isWildcard()) {
                sb.append("?");
                switch (wildcardType) {
                    case EXTENDS:
                        sb.append(" extends ").append(signature);
                        break;
                    case SUPER:
                        sb.append(" super ").append(signature);
                        break;
                    case UNBOUNDED:
                        break;
                }
            } else {
                sb.append(signature);
            }
            sb.append("]");
            return sb.toString();
        }
    }
}