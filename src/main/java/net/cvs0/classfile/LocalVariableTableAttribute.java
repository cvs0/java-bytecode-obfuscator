package net.cvs0.classfile;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

public class LocalVariableTableAttribute extends Attribute
{
    private List<LocalVariableInfo> localVariables;

    public LocalVariableTableAttribute()
    {
        super("LocalVariableTable");
        this.localVariables = new ArrayList<>();
    }

    public List<LocalVariableInfo> getLocalVariables()
    {
        return localVariables;
    }

    public void addLocalVariable(LocalVariableInfo localVariable)
    {
        this.localVariables.add(localVariable);
    }

    public void addLocalVariable(int startPc, int length, String name, String descriptor, int index)
    {
        this.localVariables.add(new LocalVariableInfo(startPc, length, name, descriptor, index));
    }

    @Override
    public byte[] getData()
    {
        try
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            
            dos.writeShort(localVariables.size());
            
            for (LocalVariableInfo localVariable : localVariables)
            {
                dos.writeShort(localVariable.getStartPc());
                dos.writeShort(localVariable.getLength());
                dos.writeShort(localVariable.getNameIndex());
                dos.writeShort(localVariable.getDescriptorIndex());
                dos.writeShort(localVariable.getIndex());
            }
            
            dos.close();
            return baos.toByteArray();
        }
        catch (IOException e)
        {
            throw new RuntimeException("Failed to serialize LocalVariableTable attribute", e);
        }
    }

    @Override
    public int getLength()
    {
        return 2 + (localVariables.size() * 10);
    }

    public static class LocalVariableInfo
    {
        private int startPc;
        private int length;
        private String name;
        private String descriptor;
        private int index;
        private int nameIndex;
        private int descriptorIndex;

        public LocalVariableInfo(int startPc, int length, String name, String descriptor, int index)
        {
            this.startPc = startPc;
            this.length = length;
            this.name = name;
            this.descriptor = descriptor;
            this.index = index;
        }

        public LocalVariableInfo(int startPc, int length, int nameIndex, int descriptorIndex, int index)
        {
            this.startPc = startPc;
            this.length = length;
            this.nameIndex = nameIndex;
            this.descriptorIndex = descriptorIndex;
            this.index = index;
        }

        public int getStartPc()
        {
            return startPc;
        }

        public void setStartPc(int startPc)
        {
            this.startPc = startPc;
        }

        public int getLength()
        {
            return length;
        }

        public void setLength(int length)
        {
            this.length = length;
        }

        public String getName()
        {
            return name;
        }

        public void setName(String name)
        {
            this.name = name;
        }

        public String getDescriptor()
        {
            return descriptor;
        }

        public void setDescriptor(String descriptor)
        {
            this.descriptor = descriptor;
        }

        public int getIndex()
        {
            return index;
        }

        public void setIndex(int index)
        {
            this.index = index;
        }

        public int getNameIndex()
        {
            return nameIndex;
        }

        public void setNameIndex(int nameIndex)
        {
            this.nameIndex = nameIndex;
        }

        public int getDescriptorIndex()
        {
            return descriptorIndex;
        }

        public void setDescriptorIndex(int descriptorIndex)
        {
            this.descriptorIndex = descriptorIndex;
        }

        @Override
        public String toString()
        {
            return "LocalVariableInfo{startPc=" + startPc + ", length=" + length + ", name='" + name + "', descriptor='" + descriptor + "', index=" + index + "}";
        }
    }

    @Override
    public String toString()
    {
        return "LocalVariableTableAttribute{localVariables=" + localVariables.size() + "}";
    }
}