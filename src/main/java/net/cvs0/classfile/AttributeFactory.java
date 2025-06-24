package net.cvs0.classfile;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class AttributeFactory
{
    public static Attribute createAttribute(String name, byte[] data)
    {
        switch (name)
        {
            case "SourceFile":
                return createSourceFileAttribute(data);
            case "InnerClasses":
                return createInnerClassAttribute(data);
            case "LineNumberTable":
                return createLineNumberTableAttribute(data);
            case "LocalVariableTable":
                return createLocalVariableTableAttribute(data);
            case "Exceptions":
                return createExceptionsAttribute(data);
            case "ConstantValue":
                return createConstantValueAttribute(data);
            case "Code":
                return createCodeAttribute(data);
            default:
                return new UnknownAttribute(name, data);
        }
    }

    private static SourceFileAttribute createSourceFileAttribute(byte[] data)
    {
        try
        {
            DataInputStream dis = new DataInputStream(new ByteArrayInputStream(data));
            int sourceFileIndex = dis.readUnsignedShort();
            dis.close();
            return new SourceFileAttribute(sourceFileIndex);
        }
        catch (IOException e)
        {
            throw new RuntimeException("Failed to parse SourceFile attribute", e);
        }
    }

    private static InnerClassAttribute createInnerClassAttribute(byte[] data)
    {
        try
        {
            DataInputStream dis = new DataInputStream(new ByteArrayInputStream(data));
            InnerClassAttribute attribute = new InnerClassAttribute();
            
            int numberOfInnerClasses = dis.readUnsignedShort();
            for (int i = 0; i < numberOfInnerClasses; i++)
            {
                int innerClassIndex = dis.readUnsignedShort();
                int outerClassIndex = dis.readUnsignedShort();
                int innerNameIndex = dis.readUnsignedShort();
                int access = dis.readUnsignedShort();
                
                attribute.addInnerClass(new InnerClassAttribute.InnerClassInfo(
                    innerClassIndex, outerClassIndex, innerNameIndex, access));
            }
            
            dis.close();
            return attribute;
        }
        catch (IOException e)
        {
            throw new RuntimeException("Failed to parse InnerClasses attribute", e);
        }
    }

    private static LineNumberTableAttribute createLineNumberTableAttribute(byte[] data)
    {
        try
        {
            DataInputStream dis = new DataInputStream(new ByteArrayInputStream(data));
            LineNumberTableAttribute attribute = new LineNumberTableAttribute();
            
            int numberOfLineNumbers = dis.readUnsignedShort();
            for (int i = 0; i < numberOfLineNumbers; i++)
            {
                int startPc = dis.readUnsignedShort();
                int lineNumber = dis.readUnsignedShort();
                attribute.addLineNumber(startPc, lineNumber);
            }
            
            dis.close();
            return attribute;
        }
        catch (IOException e)
        {
            throw new RuntimeException("Failed to parse LineNumberTable attribute", e);
        }
    }

    private static LocalVariableTableAttribute createLocalVariableTableAttribute(byte[] data)
    {
        try
        {
            DataInputStream dis = new DataInputStream(new ByteArrayInputStream(data));
            LocalVariableTableAttribute attribute = new LocalVariableTableAttribute();
            
            int numberOfLocalVariables = dis.readUnsignedShort();
            for (int i = 0; i < numberOfLocalVariables; i++)
            {
                int startPc = dis.readUnsignedShort();
                int length = dis.readUnsignedShort();
                int nameIndex = dis.readUnsignedShort();
                int descriptorIndex = dis.readUnsignedShort();
                int index = dis.readUnsignedShort();
                
                attribute.addLocalVariable(new LocalVariableTableAttribute.LocalVariableInfo(
                    startPc, length, nameIndex, descriptorIndex, index));
            }
            
            dis.close();
            return attribute;
        }
        catch (IOException e)
        {
            throw new RuntimeException("Failed to parse LocalVariableTable attribute", e);
        }
    }

    private static ExceptionsAttribute createExceptionsAttribute(byte[] data)
    {
        try
        {
            DataInputStream dis = new DataInputStream(new ByteArrayInputStream(data));
            ExceptionsAttribute attribute = new ExceptionsAttribute();
            
            int numberOfExceptions = dis.readUnsignedShort();
            for (int i = 0; i < numberOfExceptions; i++)
            {
                int exceptionIndex = dis.readUnsignedShort();
                attribute.addException(exceptionIndex);
            }
            
            dis.close();
            return attribute;
        }
        catch (IOException e)
        {
            throw new RuntimeException("Failed to parse Exceptions attribute", e);
        }
    }

    private static ConstantValueAttribute createConstantValueAttribute(byte[] data)
    {
        try
        {
            DataInputStream dis = new DataInputStream(new ByteArrayInputStream(data));
            int constantValueIndex = dis.readUnsignedShort();
            dis.close();
            return new ConstantValueAttribute(constantValueIndex);
        }
        catch (IOException e)
        {
            throw new RuntimeException("Failed to parse ConstantValue attribute", e);
        }
    }

    private static CodeAttribute createCodeAttribute(byte[] data)
    {
        try
        {
            DataInputStream dis = new DataInputStream(new ByteArrayInputStream(data));
            
            int maxStack = dis.readUnsignedShort();
            int maxLocals = dis.readUnsignedShort();
            int codeLength = dis.readInt();
            
            byte[] code = new byte[codeLength];
            dis.readFully(code);
            
            CodeAttribute attribute = new CodeAttribute(maxStack, maxLocals, code);
            
            int numberOfExceptionHandlers = dis.readUnsignedShort();
            for (int i = 0; i < numberOfExceptionHandlers; i++)
            {
                int startPc = dis.readUnsignedShort();
                int endPc = dis.readUnsignedShort();
                int handlerPc = dis.readUnsignedShort();
                int catchTypeIndex = dis.readUnsignedShort();
                
                attribute.addExceptionHandler(new CodeAttribute.ExceptionHandler(
                    startPc, endPc, handlerPc, catchTypeIndex));
            }
            
            int numberOfAttributes = dis.readUnsignedShort();
            for (int i = 0; i < numberOfAttributes; i++)
            {
                int nameLength = dis.readUnsignedShort();
                byte[] nameBytes = new byte[nameLength];
                dis.readFully(nameBytes);
                String attributeName = new String(nameBytes);
                
                int attributeLength = dis.readInt();
                byte[] attributeData = new byte[attributeLength];
                dis.readFully(attributeData);
                
                Attribute nestedAttribute = createAttribute(attributeName, attributeData);
                attribute.addAttribute(nestedAttribute);
            }
            
            dis.close();
            return attribute;
        }
        catch (IOException e)
        {
            throw new RuntimeException("Failed to parse Code attribute", e);
        }
    }

    public static class UnknownAttribute extends Attribute
    {
        private final byte[] data;

        public UnknownAttribute(String name, byte[] data)
        {
            super(name);
            this.data = data;
        }

        @Override
        public byte[] getData()
        {
            return data;
        }

        @Override
        public int getLength()
        {
            return data.length;
        }

        @Override
        public String toString()
        {
            return "UnknownAttribute{name='" + name + "', length=" + data.length + "}";
        }
    }
}