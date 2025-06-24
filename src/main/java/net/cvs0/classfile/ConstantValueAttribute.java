package net.cvs0.classfile;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ConstantValueAttribute extends Attribute
{
    private int constantValueIndex;
    private Object constantValue;

    public ConstantValueAttribute(int constantValueIndex)
    {
        super("ConstantValue");
        this.constantValueIndex = constantValueIndex;
    }

    public ConstantValueAttribute(Object constantValue)
    {
        super("ConstantValue");
        this.constantValue = constantValue;
        this.constantValueIndex = 0;
    }

    public int getConstantValueIndex()
    {
        return constantValueIndex;
    }

    public void setConstantValueIndex(int constantValueIndex)
    {
        this.constantValueIndex = constantValueIndex;
    }

    public Object getConstantValue()
    {
        return constantValue;
    }

    public void setConstantValue(Object constantValue)
    {
        this.constantValue = constantValue;
    }

    @Override
    public byte[] getData()
    {
        try
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            
            dos.writeShort(constantValueIndex);
            
            dos.close();
            return baos.toByteArray();
        }
        catch (IOException e)
        {
            throw new RuntimeException("Failed to serialize ConstantValue attribute", e);
        }
    }

    @Override
    public int getLength()
    {
        return 2;
    }

    @Override
    public String toString()
    {
        return "ConstantValueAttribute{constantValueIndex=" + constantValueIndex + ", constantValue=" + constantValue + "}";
    }
}