package net.cvs0.classfile;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

public class LocalVariableTypeTableAttribute extends Attribute
{
    private List<LocalVariableTypeInfo> localVariableTypes;

    public LocalVariableTypeTableAttribute()
    {
        super("LocalVariableTypeTable");
        this.localVariableTypes = new ArrayList<>();
    }

    public List<LocalVariableTypeInfo> getLocalVariableTypes()
    {
        return localVariableTypes;
    }

    public void addLocalVariableType(LocalVariableTypeInfo localVariableType)
    {
        this.localVariableTypes.add(localVariableType);
    }

    public void addLocalVariableType(int startPc, int length, String name, String signature, int index)
    {
        this.localVariableTypes.add(new LocalVariableTypeInfo(startPc, length, name, signature, index));
    }

    @Override
    public byte[] getData()
    {
        try
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            
            dos.writeShort(localVariableTypes.size());
            
            for (LocalVariableTypeInfo localVariableType : localVariableTypes)
            {
                dos.writeShort(localVariableType.getStartPc());
                dos.writeShort(localVariableType.getLength());
                dos.writeShort(localVariableType.getNameIndex());
                dos.writeShort(localVariableType.getSignatureIndex());
                dos.writeShort(localVariableType.getIndex());
            }
            
            dos.close();
            return baos.toByteArray();
        }
        catch (IOException e)
        {
            throw new RuntimeException("Failed to serialize LocalVariableTypeTable attribute", e);
        }
    }

    @Override
    public int getLength()
    {
        return 2 + (localVariableTypes.size() * 10);
    }

    public static class LocalVariableTypeInfo
    {
        private int startPc;
        private int length;
        private String name;
        private String signature;
        private int index;
        private int nameIndex;
        private int signatureIndex;

        public LocalVariableTypeInfo(int startPc, int length, String name, String signature, int index)
        {
            this.startPc = startPc;
            this.length = length;
            this.name = name;
            this.signature = signature;
            this.index = index;
        }

        public LocalVariableTypeInfo(int startPc, int length, int nameIndex, int signatureIndex, int index)
        {
            this.startPc = startPc;
            this.length = length;
            this.nameIndex = nameIndex;
            this.signatureIndex = signatureIndex;
            this.index = index;
        }

        public int getStartPc() { return startPc; }
        public void setStartPc(int startPc) { this.startPc = startPc; }
        public int getLength() { return length; }
        public void setLength(int length) { this.length = length; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getSignature() { return signature; }
        public void setSignature(String signature) { this.signature = signature; }
        public int getIndex() { return index; }
        public void setIndex(int index) { this.index = index; }
        public int getNameIndex() { return nameIndex; }
        public void setNameIndex(int nameIndex) { this.nameIndex = nameIndex; }
        public int getSignatureIndex() { return signatureIndex; }
        public void setSignatureIndex(int signatureIndex) { this.signatureIndex = signatureIndex; }

        @Override
        public String toString()
        {
            return "LocalVariableTypeInfo{startPc=" + startPc + ", length=" + length + ", name='" + name + "', signature='" + signature + "', index=" + index + "}";
        }
    }

    @Override
    public String toString()
    {
        return "LocalVariableTypeTableAttribute{localVariableTypes=" + localVariableTypes.size() + "}";
    }
}