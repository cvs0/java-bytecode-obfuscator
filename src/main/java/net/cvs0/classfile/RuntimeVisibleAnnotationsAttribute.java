package net.cvs0.classfile;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

public class RuntimeVisibleAnnotationsAttribute extends Attribute
{
    private List<AnnotationInfo> annotations;

    public RuntimeVisibleAnnotationsAttribute()
    {
        super("RuntimeVisibleAnnotations");
        this.annotations = new ArrayList<>();
    }

    public List<AnnotationInfo> getAnnotations()
    {
        return annotations;
    }

    public void addAnnotation(AnnotationInfo annotation)
    {
        this.annotations.add(annotation);
    }

    public void addAnnotation(String type, Map<String, Object> values)
    {
        this.annotations.add(new AnnotationInfo(type, values));
    }

    @Override
    public byte[] getData()
    {
        try
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            
            dos.writeShort(annotations.size());
            
            for (AnnotationInfo annotation : annotations)
            {
                dos.write(annotation.getData());
            }
            
            dos.close();
            return baos.toByteArray();
        }
        catch (IOException e)
        {
            throw new RuntimeException("Failed to serialize RuntimeVisibleAnnotations attribute", e);
        }
    }

    @Override
    public int getLength()
    {
        int length = 2;
        for (AnnotationInfo annotation : annotations)
        {
            length += annotation.getLength();
        }
        return length;
    }

    public static class AnnotationInfo
    {
        private String type;
        private Map<String, Object> values;

        public AnnotationInfo(String type, Map<String, Object> values)
        {
            this.type = type;
            this.values = values;
        }

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public Map<String, Object> getValues() { return values; }
        public void setValues(Map<String, Object> values) { this.values = values; }

        public byte[] getData()
        {
            return new byte[0];
        }

        public int getLength()
        {
            return 4 + (values != null ? values.size() * 8 : 0);
        }

        @Override
        public String toString()
        {
            return "AnnotationInfo{type='" + type + "', values=" + (values != null ? values.size() : 0) + "}";
        }
    }

    @Override
    public String toString()
    {
        return "RuntimeVisibleAnnotationsAttribute{annotations=" + annotations.size() + "}";
    }
}