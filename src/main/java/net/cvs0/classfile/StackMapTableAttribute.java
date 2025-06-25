package net.cvs0.classfile;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

public class StackMapTableAttribute extends Attribute
{
    private List<StackMapFrame> frames;

    public StackMapTableAttribute()
    {
        super("StackMapTable");
        this.frames = new ArrayList<>();
    }

    public List<StackMapFrame> getFrames()
    {
        return frames;
    }

    public void addFrame(StackMapFrame frame)
    {
        this.frames.add(frame);
    }

    @Override
    public byte[] getData()
    {
        try
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            
            dos.writeShort(frames.size());
            
            for (StackMapFrame frame : frames)
            {
                dos.write(frame.getData());
            }
            
            dos.close();
            return baos.toByteArray();
        }
        catch (IOException e)
        {
            throw new RuntimeException("Failed to serialize StackMapTable attribute", e);
        }
    }

    @Override
    public int getLength()
    {
        int length = 2;
        for (StackMapFrame frame : frames)
        {
            length += frame.getLength();
        }
        return length;
    }

    public static class StackMapFrame
    {
        private int frameType;
        private byte[] data;

        public StackMapFrame(int frameType, byte[] data)
        {
            this.frameType = frameType;
            this.data = data;
        }

        public int getFrameType() { return frameType; }
        public byte[] getData() { return data; }
        public int getLength() { return data != null ? data.length : 0; }

        @Override
        public String toString()
        {
            return "StackMapFrame{frameType=" + frameType + ", dataLength=" + (data != null ? data.length : 0) + "}";
        }
    }

    @Override
    public String toString()
    {
        return "StackMapTableAttribute{frames=" + frames.size() + "}";
    }
}