package net.cvs0.classfile;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

public class InnerClassAttribute extends Attribute
{
    private List<InnerClassInfo> innerClasses;

    public InnerClassAttribute()
    {
        super("InnerClasses");
        this.innerClasses = new ArrayList<>();
    }

    public List<InnerClassInfo> getInnerClasses()
    {
        return innerClasses;
    }

    public void addInnerClass(InnerClassInfo innerClass)
    {
        this.innerClasses.add(innerClass);
    }

    @Override
    public byte[] getData()
    {
        try
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            
            dos.writeShort(innerClasses.size());
            
            for (InnerClassInfo innerClass : innerClasses)
            {
                dos.writeShort(innerClass.getInnerClassIndex());
                dos.writeShort(innerClass.getOuterClassIndex());
                dos.writeShort(innerClass.getInnerNameIndex());
                dos.writeShort(innerClass.getAccess());
            }
            
            dos.close();
            return baos.toByteArray();
        }
        catch (IOException e)
        {
            throw new RuntimeException("Failed to serialize InnerClasses attribute", e);
        }
    }

    @Override
    public int getLength()
    {
        return 2 + (innerClasses.size() * 8);
    }

    public static class InnerClassInfo
    {
        private String innerClass;
        private String outerClass;
        private String innerName;
        private int access;
        private int innerClassIndex;
        private int outerClassIndex;
        private int innerNameIndex;

        public InnerClassInfo(String innerClass, String outerClass, String innerName, int access)
        {
            this.innerClass = innerClass;
            this.outerClass = outerClass;
            this.innerName = innerName;
            this.access = access;
        }

        public InnerClassInfo(int innerClassIndex, int outerClassIndex, int innerNameIndex, int access)
        {
            this.innerClassIndex = innerClassIndex;
            this.outerClassIndex = outerClassIndex;
            this.innerNameIndex = innerNameIndex;
            this.access = access;
        }

        public String getInnerClass()
        {
            return innerClass;
        }

        public void setInnerClass(String innerClass)
        {
            this.innerClass = innerClass;
        }

        public String getOuterClass()
        {
            return outerClass;
        }

        public void setOuterClass(String outerClass)
        {
            this.outerClass = outerClass;
        }

        public String getInnerName()
        {
            return innerName;
        }

        public void setInnerName(String innerName)
        {
            this.innerName = innerName;
        }

        public int getAccess()
        {
            return access;
        }

        public void setAccess(int access)
        {
            this.access = access;
        }

        public int getInnerClassIndex()
        {
            return innerClassIndex;
        }

        public void setInnerClassIndex(int innerClassIndex)
        {
            this.innerClassIndex = innerClassIndex;
        }

        public int getOuterClassIndex()
        {
            return outerClassIndex;
        }

        public void setOuterClassIndex(int outerClassIndex)
        {
            this.outerClassIndex = outerClassIndex;
        }

        public int getInnerNameIndex()
        {
            return innerNameIndex;
        }

        public void setInnerNameIndex(int innerNameIndex)
        {
            this.innerNameIndex = innerNameIndex;
        }

        @Override
        public String toString()
        {
            return "InnerClassInfo{innerClass='" + innerClass + "', outerClass='" + outerClass + 
                   "', innerName='" + innerName + "', access=" + access + "}";
        }
    }

    @Override
    public String toString()
    {
        return "InnerClassAttribute{innerClasses=" + innerClasses.size() + "}";
    }
}