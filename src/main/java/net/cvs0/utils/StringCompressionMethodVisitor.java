package net.cvs0.utils;

import net.cvs0.context.ObfuscationContext;
import org.objectweb.asm.*;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.zip.Deflater;
import java.util.zip.Inflater;
import java.util.concurrent.ThreadLocalRandom;

public class StringCompressionMethodVisitor extends MethodVisitor
{
    private final ObfuscationContext context;
    private final String className;
    private final String methodName;
    private final String methodDescriptor;
    private static final int MIN_STRING_LENGTH = 10;
    private static final double COMPRESSION_PROBABILITY = 0.7;
    
    public StringCompressionMethodVisitor(MethodVisitor methodVisitor, ObfuscationContext context,
                                        String className, String methodName, String methodDescriptor)
    {
        super(Opcodes.ASM9, methodVisitor);
        this.context = context;
        this.className = className;
        this.methodName = methodName;
        this.methodDescriptor = methodDescriptor;
    }
    
    @Override
    public void visitLdcInsn(Object value)
    {
        if (value instanceof String && shouldCompressString((String) value)) {
            String originalString = (String) value;
            String compressedData = StringCompressionMethodVisitor.compressString(originalString);
            
            if (compressedData != null && compressedData.length() < originalString.length()) {
                generateDecompressionCode(compressedData);
                
                if (context.getConfig().isVerbose()) {
                    System.out.println("🗜️  Compressed string in " + className + "." + methodName + 
                                     " (saved " + (originalString.length() - compressedData.length()) + " chars)");
                }
                return;
            }
        }
        
        super.visitLdcInsn(value);
    }
    
    private boolean shouldCompressString(String str)
    {
        if (str == null || str.length() < MIN_STRING_LENGTH) {
            return false;
        }
        
        if (context.getConfig().shouldKeepClass(className)) {
            return false;
        }
        
        if (isLikelySpecialString(str)) {
            return false;
        }
        
        return ThreadLocalRandom.current().nextDouble() < COMPRESSION_PROBABILITY;
    }
    
    private boolean isLikelySpecialString(String str)
    {
        String lower = str.toLowerCase();
        
        return lower.matches("^[a-z0-9._-]+$") ||
               lower.startsWith("http") ||
               lower.startsWith("ftp") ||
               lower.startsWith("jdbc:") ||
               lower.contains("@") ||
               lower.matches(".*\\.(java|class|jar|xml|json|properties)$") ||
               str.matches("^[A-Z_][A-Z0-9_]*$") ||
               str.length() < 3;
    }
    
    public static String compressString(String input)
    {
        try {
            byte[] inputBytes = input.getBytes(StandardCharsets.UTF_8);
            
            Deflater deflater = new Deflater(Deflater.BEST_COMPRESSION);
            deflater.setInput(inputBytes);
            deflater.finish();
            
            byte[] compressedBytes = new byte[inputBytes.length * 2];
            int compressedLength = deflater.deflate(compressedBytes);
            deflater.end();
            
            if (compressedLength >= inputBytes.length) {
                return null;
            }
            
            byte[] finalCompressed = new byte[compressedLength];
            System.arraycopy(compressedBytes, 0, finalCompressed, 0, compressedLength);
            
            return Base64.getEncoder().encodeToString(finalCompressed);
        } catch (Exception e) {
            return null;
        }
    }
    
    private void generateDecompressionCode(String compressedData)
    {
        super.visitLdcInsn(compressedData);
        
        super.visitMethodInsn(Opcodes.INVOKESTATIC, 
                            "java/util/Base64", 
                            "getDecoder", 
                            "()Ljava/util/Base64$Decoder;", 
                            false);
        
        super.visitInsn(Opcodes.SWAP);
        
        super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, 
                            "java/util/Base64$Decoder", 
                            "decode", 
                            "(Ljava/lang/String;)[B", 
                            false);
        
        super.visitTypeInsn(Opcodes.NEW, "java/util/zip/Inflater");
        super.visitInsn(Opcodes.DUP);
        super.visitMethodInsn(Opcodes.INVOKESPECIAL, 
                            "java/util/zip/Inflater", 
                            "<init>", 
                            "()V", 
                            false);
        
        super.visitInsn(Opcodes.DUP);
        super.visitInsn(Opcodes.DUP2_X1);
        super.visitInsn(Opcodes.POP2);
        
        super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, 
                            "java/util/zip/Inflater", 
                            "setInput", 
                            "([B)V", 
                            false);
        
        super.visitIntInsn(Opcodes.SIPUSH, 1024);
        super.visitIntInsn(Opcodes.NEWARRAY, Opcodes.T_BYTE);
        
        super.visitInsn(Opcodes.DUP2);
        super.visitInsn(Opcodes.SWAP);
        
        super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, 
                            "java/util/zip/Inflater", 
                            "inflate", 
                            "([B)I", 
                            false);
        
        super.visitTypeInsn(Opcodes.NEW, "java/lang/String");
        super.visitInsn(Opcodes.DUP_X2);
        super.visitInsn(Opcodes.DUP_X2);
        super.visitInsn(Opcodes.POP);
        
        super.visitInsn(Opcodes.ICONST_0);
        super.visitInsn(Opcodes.SWAP);
        
        super.visitFieldInsn(Opcodes.GETSTATIC, 
                           "java/nio/charset/StandardCharsets", 
                           "UTF_8", 
                           "Ljava/nio/charset/Charset;");
        
        super.visitMethodInsn(Opcodes.INVOKESPECIAL, 
                            "java/lang/String", 
                            "<init>", 
                            "([BIILjava/nio/charset/Charset;)V", 
                            false);
    }
    
    public static String decompressString(String compressedData)
    {
        try {
            byte[] compressedBytes = Base64.getDecoder().decode(compressedData);
            
            Inflater inflater = new Inflater();
            inflater.setInput(compressedBytes);
            
            byte[] decompressedBytes = new byte[1024];
            int decompressedLength = inflater.inflate(decompressedBytes);
            inflater.end();
            
            return new String(decompressedBytes, 0, decompressedLength, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return compressedData;
        }
    }
}