package net.cvs0.classfile;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

public class CodeAttribute extends Attribute
{
    private int maxStack;
    private int maxLocals;
    private byte[] code;
    private List<ExceptionHandler> exceptionHandlers;
    private List<Attribute> attributes;

    public CodeAttribute()
    {
        super("Code");
        this.exceptionHandlers = new ArrayList<>();
        this.attributes = new ArrayList<>();
    }

    public CodeAttribute(int maxStack, int maxLocals, byte[] code)
    {
        super("Code");
        this.maxStack = maxStack;
        this.maxLocals = maxLocals;
        this.code = code;
        this.exceptionHandlers = new ArrayList<>();
        this.attributes = new ArrayList<>();
    }

    public int getMaxStack()
    {
        return maxStack;
    }

    public void setMaxStack(int maxStack)
    {
        this.maxStack = maxStack;
    }

    public int getMaxLocals()
    {
        return maxLocals;
    }

    public void setMaxLocals(int maxLocals)
    {
        this.maxLocals = maxLocals;
    }

    public byte[] getCode()
    {
        return code;
    }

    public void setCode(byte[] code)
    {
        this.code = code;
    }

    public List<ExceptionHandler> getExceptionHandlers()
    {
        return exceptionHandlers;
    }

    public void addExceptionHandler(ExceptionHandler handler)
    {
        this.exceptionHandlers.add(handler);
    }

    public List<Attribute> getAttributes()
    {
        return attributes;
    }

    public void addAttribute(Attribute attribute)
    {
        this.attributes.add(attribute);
    }

    @Override
    public byte[] getData()
    {
        try
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            
            dos.writeShort(maxStack);
            dos.writeShort(maxLocals);
            dos.writeInt(code != null ? code.length : 0);
            
            if (code != null)
            {
                dos.write(code);
            }
            
            dos.writeShort(exceptionHandlers.size());
            for (ExceptionHandler handler : exceptionHandlers)
            {
                dos.writeShort(handler.getStartPc());
                dos.writeShort(handler.getEndPc());
                dos.writeShort(handler.getHandlerPc());
                dos.writeShort(handler.getCatchTypeIndex());
            }
            
            dos.writeShort(attributes.size());
            for (Attribute attribute : attributes)
            {
                byte[] nameBytes = attribute.getName().getBytes();
                dos.writeShort(nameBytes.length);
                dos.write(nameBytes);
                
                byte[] data = attribute.getData();
                dos.writeInt(data.length);
                dos.write(data);
            }
            
            dos.close();
            return baos.toByteArray();
        }
        catch (IOException e)
        {
            throw new RuntimeException("Failed to serialize Code attribute", e);
        }
    }

    @Override
    public int getLength()
    {
        int length = 12 + (code != null ? code.length : 0);
        length += exceptionHandlers.size() * 8;
        
        for (Attribute attribute : attributes)
        {
            length += 6 + attribute.getName().getBytes().length + attribute.getLength();
        }
        
        return length;
    }

    public static class ExceptionHandler
    {
        private int startPc;
        private int endPc;
        private int handlerPc;
        private int catchTypeIndex;
        private String catchType;

        public ExceptionHandler(int startPc, int endPc, int handlerPc, int catchTypeIndex)
        {
            this.startPc = startPc;
            this.endPc = endPc;
            this.handlerPc = handlerPc;
            this.catchTypeIndex = catchTypeIndex;
        }

        public ExceptionHandler(int startPc, int endPc, int handlerPc, String catchType)
        {
            this.startPc = startPc;
            this.endPc = endPc;
            this.handlerPc = handlerPc;
            this.catchType = catchType;
            this.catchTypeIndex = 0;
        }

        public int getStartPc()
        {
            return startPc;
        }

        public void setStartPc(int startPc)
        {
            this.startPc = startPc;
        }

        public int getEndPc()
        {
            return endPc;
        }

        public void setEndPc(int endPc)
        {
            this.endPc = endPc;
        }

        public int getHandlerPc()
        {
            return handlerPc;
        }

        public void setHandlerPc(int handlerPc)
        {
            this.handlerPc = handlerPc;
        }

        public int getCatchTypeIndex()
        {
            return catchTypeIndex;
        }

        public void setCatchTypeIndex(int catchTypeIndex)
        {
            this.catchTypeIndex = catchTypeIndex;
        }

        public String getCatchType()
        {
            return catchType;
        }

        public void setCatchType(String catchType)
        {
            this.catchType = catchType;
        }

        @Override
        public String toString()
        {
            return "ExceptionHandler{startPc=" + startPc + ", endPc=" + endPc + 
                   ", handlerPc=" + handlerPc + ", catchType='" + catchType + "'}";
        }
    }

    @Override
    public String toString()
    {
        return "CodeAttribute{maxStack=" + maxStack + ", maxLocals=" + maxLocals + 
               ", codeLength=" + (code != null ? code.length : 0) + 
               ", exceptionHandlers=" + exceptionHandlers.size() + 
               ", attributes=" + attributes.size() + "}";
    }
}