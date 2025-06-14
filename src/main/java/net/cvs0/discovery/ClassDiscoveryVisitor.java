package net.cvs0.discovery;

import net.cvs0.mappings.MappingManager;
import net.cvs0.mappings.InheritanceTracker;
import org.objectweb.asm.*;

public class ClassDiscoveryVisitor extends ClassVisitor
{
    private final MappingManager mappingManager;
    private final InheritanceTracker inheritanceTracker;
    private String currentClassName;
    
    public ClassDiscoveryVisitor(MappingManager mappingManager, InheritanceTracker inheritanceTracker)
    {
        super(Opcodes.ASM9);
        this.mappingManager = mappingManager;
        this.inheritanceTracker = inheritanceTracker;
    }
    
    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces)
    {
        this.currentClassName = name;
        inheritanceTracker.addClass(name, superName, interfaces);
        super.visit(version, access, name, signature, superName, interfaces);
    }
    
    @Override
    public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value)
    {
        mappingManager.generateFieldMapping(currentClassName, name, descriptor);
        return super.visitField(access, name, descriptor, signature, value);
    }
    
    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions)
    {
        mappingManager.generateMethodMapping(currentClassName, name, descriptor);
        return super.visitMethod(access, name, descriptor, signature, exceptions);
    }
}