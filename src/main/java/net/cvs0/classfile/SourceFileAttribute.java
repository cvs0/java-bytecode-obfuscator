package net.cvs0.classfile;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class SourceFileAttribute extends Attribute
{
    private String sourceFile;
    private int sourceFileIndex;

    public SourceFileAttribute(String sourceFile)
    {
        super("SourceFile");
        this.sourceFile = sourceFile;
        this.sourceFileIndex = 0;
    }

    public SourceFileAttribute(int sourceFileIndex)
    {
        super("SourceFile");
        this.sourceFileIndex = sourceFileIndex;
        this.sourceFile = null;
    }

    public String getSourceFile()
    {
        return sourceFile;
    }

    public void setSourceFile(String sourceFile)
    {
        this.sourceFile = sourceFile;
    }

    public int getSourceFileIndex()
    {
        return sourceFileIndex;
    }

    public void setSourceFileIndex(int sourceFileIndex)
    {
        this.sourceFileIndex = sourceFileIndex;
    }

    @Override
    public byte[] getData()
    {
        try
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            
            dos.writeShort(sourceFileIndex);
            
            dos.close();
            return baos.toByteArray();
        }
        catch (IOException e)
        {
            throw new RuntimeException("Failed to serialize SourceFile attribute", e);
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
        return "SourceFileAttribute{sourceFile='" + sourceFile + "', sourceFileIndex=" + sourceFileIndex + "}";
    }
}