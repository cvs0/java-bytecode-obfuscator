package net.cvs0.classfile;

import org.objectweb.asm.Type;
import java.util.HashMap;
import java.util.Map;

public class ProgramField extends ProgramMember
{
    private final Object value;
    private final Type type;
    private final Map<String, Object> annotations;
    
    public ProgramField(int access, String name, String descriptor, String signature, Object value)
    {
        super(access, name, descriptor, signature);
        this.value = value;
        this.type = Type.getType(descriptor);
        this.annotations = new HashMap<>();
    }
    
    public Object getValue()
    {
        return value;
    }
    
    public Type getType()
    {
        return type;
    }
    
    public boolean isVolatile()
    {
        return (access & JavaConstants.ACC_VOLATILE) != 0;
    }
    
    public boolean isTransient()
    {
        return (access & 0x0080) != 0;
    }
    
    public boolean isEnum()
    {
        return (access & JavaConstants.ACC_ENUM) != 0;
    }
    
    public boolean isPrimitive()
    {
        return type.getSort() != Type.OBJECT && type.getSort() != Type.ARRAY;
    }
    
    public boolean isArray()
    {
        return type.getSort() == Type.ARRAY;
    }
    
    public boolean isObject()
    {
        return type.getSort() == Type.OBJECT;
    }
    
    public String getTypeName()
    {
        return type.getClassName();
    }
    
    public String getInternalTypeName()
    {
        return type.getInternalName();
    }
    
    public boolean hasConstantValue()
    {
        return value != null;
    }
    
    public boolean isStringConstant()
    {
        return value instanceof String;
    }
    
    public boolean isNumericConstant()
    {
        return value instanceof Number;
    }
    
    public void addAnnotation(String descriptor, Object value)
    {
        annotations.put(descriptor, value);
    }
    
    public Object getAnnotation(String descriptor)
    {
        return annotations.get(descriptor);
    }
    
    public Map<String, Object> getAnnotations()
    {
        return new HashMap<>(annotations);
    }
    
    public boolean hasAnnotation(String descriptor)
    {
        return annotations.containsKey(descriptor);
    }
    
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        if (isPublic()) sb.append("public ");
        if (isPrivate()) sb.append("private ");
        if (isProtected()) sb.append("protected ");
        if (isStatic()) sb.append("static ");
        if (isFinal()) sb.append("final ");
        if (isVolatile()) sb.append("volatile ");
        if (isTransient()) sb.append("transient ");
        
        sb.append(getTypeName()).append(" ").append(getName());
        
        if (hasConstantValue()) {
            sb.append(" = ").append(value);
        }
        
        return sb.toString();
    }
}