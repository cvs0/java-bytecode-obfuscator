package net.cvs0.mappings;

import org.objectweb.asm.*;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.Remapper;

public class SafeClassRemapper extends ClassRemapper
{
    private final String className;
    
    public SafeClassRemapper(ClassWriter classWriter, Remapper remapper, String className)
    {
        super(classWriter, remapper);
        this.className = className;
    }
    
    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces)
    {
        try {
            super.visit(version, access, name, signature, superName, interfaces);
        } catch (Exception e) {
            throw new RuntimeException("Type " + extractMissingType(e.getMessage()) + " not present", e);
        }
    }
    
    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions)
    {
        try {
            return super.visitMethod(access, name, descriptor, signature, exceptions);
        } catch (Exception e) {
            throw new RuntimeException("Type " + extractMissingType(e.getMessage()) + " not present", e);
        }
    }
    
    @Override
    public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value)
    {
        try {
            return super.visitField(access, name, descriptor, signature, value);
        } catch (Exception e) {
            throw new RuntimeException("Type " + extractMissingType(e.getMessage()) + " not present", e);
        }
    }
    
    private String extractMissingType(String message) {
        if (message != null && message.contains("not present")) {
            int start = message.indexOf("Type ") + 5;
            int end = message.indexOf(" not present");
            if (start > 4 && end > start) {
                return message.substring(start, end);
            }
        }
        return "unknown";
    }
}