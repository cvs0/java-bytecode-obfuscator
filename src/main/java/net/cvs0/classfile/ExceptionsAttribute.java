package net.cvs0.classfile;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

public class ExceptionsAttribute extends Attribute
{
    private List<String> exceptions;
    private List<Integer> exceptionIndexes;

    public ExceptionsAttribute()
    {
        super("Exceptions");
        this.exceptions = new ArrayList<>();
        this.exceptionIndexes = new ArrayList<>();
    }

    public List<String> getExceptions()
    {
        return exceptions;
    }

    public List<Integer> getExceptionIndexes()
    {
        return exceptionIndexes;
    }

    public void addException(String exception)
    {
        this.exceptions.add(exception);
    }

    public void addException(int exceptionIndex)
    {
        this.exceptionIndexes.add(exceptionIndex);
    }

    @Override
    public byte[] getData()
    {
        try
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            
            int count = Math.max(exceptions.size(), exceptionIndexes.size());
            dos.writeShort(count);
            
            for (int i = 0; i < count; i++)
            {
                if (i < exceptionIndexes.size())
                {
                    dos.writeShort(exceptionIndexes.get(i));
                }
                else
                {
                    dos.writeShort(0);
                }
            }
            
            dos.close();
            return baos.toByteArray();
        }
        catch (IOException e)
        {
            throw new RuntimeException("Failed to serialize Exceptions attribute", e);
        }
    }

    @Override
    public int getLength()
    {
        int count = Math.max(exceptions.size(), exceptionIndexes.size());
        return 2 + (count * 2);
    }

    @Override
    public String toString()
    {
        return "ExceptionsAttribute{exceptions=" + exceptions.size() + "}";
    }
}