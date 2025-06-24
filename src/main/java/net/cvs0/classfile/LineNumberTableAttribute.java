package net.cvs0.classfile;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

public class LineNumberTableAttribute extends Attribute
{
    private List<LineNumberInfo> lineNumbers;

    public LineNumberTableAttribute()
    {
        super("LineNumberTable");
        this.lineNumbers = new ArrayList<>();
    }

    public List<LineNumberInfo> getLineNumbers()
    {
        return lineNumbers;
    }

    public void addLineNumber(LineNumberInfo lineNumber)
    {
        this.lineNumbers.add(lineNumber);
    }

    public void addLineNumber(int startPc, int lineNumber)
    {
        this.lineNumbers.add(new LineNumberInfo(startPc, lineNumber));
    }

    @Override
    public byte[] getData()
    {
        try
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            
            dos.writeShort(lineNumbers.size());
            
            for (LineNumberInfo lineNumber : lineNumbers)
            {
                dos.writeShort(lineNumber.getStartPc());
                dos.writeShort(lineNumber.getLineNumber());
            }
            
            dos.close();
            return baos.toByteArray();
        }
        catch (IOException e)
        {
            throw new RuntimeException("Failed to serialize LineNumberTable attribute", e);
        }
    }

    @Override
    public int getLength()
    {
        return 2 + (lineNumbers.size() * 4);
    }

    public static class LineNumberInfo
    {
        private int startPc;
        private int lineNumber;

        public LineNumberInfo(int startPc, int lineNumber)
        {
            this.startPc = startPc;
            this.lineNumber = lineNumber;
        }

        public int getStartPc()
        {
            return startPc;
        }

        public void setStartPc(int startPc)
        {
            this.startPc = startPc;
        }

        public int getLineNumber()
        {
            return lineNumber;
        }

        public void setLineNumber(int lineNumber)
        {
            this.lineNumber = lineNumber;
        }

        @Override
        public String toString()
        {
            return "LineNumberInfo{startPc=" + startPc + ", lineNumber=" + lineNumber + "}";
        }
    }

    @Override
    public String toString()
    {
        return "LineNumberTableAttribute{lineNumbers=" + lineNumbers.size() + "}";
    }
}