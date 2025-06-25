package net.cvs0.classfile;

import java.util.ArrayList;
import java.util.List;

public class MethodParametersAttribute extends Attribute
{
    private final List<Parameter> parameters;
    
    public MethodParametersAttribute()
    {
        super("MethodParameters");
        this.parameters = new ArrayList<>();
    }
    
    public void addParameter(String name, int access)
    {
        parameters.add(new Parameter(name, access));
    }
    
    public List<Parameter> getParameters()
    {
        return new ArrayList<>(parameters);
    }
    
    public int getParameterCount()
    {
        return parameters.size();
    }
    
    public Parameter getParameter(int index)
    {
        return parameters.get(index);
    }
    
    @Override
    public byte[] getData()
    {
        try {
            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            java.io.DataOutputStream dos = new java.io.DataOutputStream(baos);
            
            dos.writeByte(parameters.size());
            
            for (Parameter param : parameters) {
                dos.writeShort(0);
                dos.writeShort(param.getAccess());
            }
            
            dos.close();
            return baos.toByteArray();
        } catch (java.io.IOException e) {
            throw new RuntimeException("Failed to serialize MethodParameters attribute", e);
        }
    }
    
    @Override
    public int getLength()
    {
        return 1 + (parameters.size() * 4);
    }
    
    public static class Parameter
    {
        private String name;
        private final int access;
        
        public Parameter(String name, int access)
        {
            this.name = name;
            this.access = access;
        }
        
        public String getName()
        {
            return name;
        }
        
        public void setName(String name)
        {
            this.name = name;
        }
        
        public int getAccess()
        {
            return access;
        }
        
        public boolean isFinal()
        {
            return (access & JavaConstants.ACC_FINAL) != 0;
        }
        
        public boolean isSynthetic()
        {
            return (access & JavaConstants.ACC_SYNTHETIC) != 0;
        }
        
        public boolean isMandated()
        {
            return (access & 0x8000) != 0;
        }
        
        @Override
        public String toString()
        {
            StringBuilder sb = new StringBuilder();
            if (isFinal()) sb.append("final ");
            if (isSynthetic()) sb.append("synthetic ");
            if (isMandated()) sb.append("mandated ");
            sb.append(name != null ? name : "");
            return sb.toString();
        }
    }
    
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder("MethodParameters[");
        for (int i = 0; i < parameters.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(parameters.get(i));
        }
        sb.append("]");
        return sb.toString();
    }
}