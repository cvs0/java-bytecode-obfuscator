package net.cvs0.discovery;

import net.cvs0.mappings.InheritanceTracker;
import org.objectweb.asm.*;

public class InheritanceDiscoveryVisitor extends ClassVisitor
{
    private final InheritanceTracker inheritanceTracker;
    
    public InheritanceDiscoveryVisitor(InheritanceTracker inheritanceTracker)
    {
        super(Opcodes.ASM9);
        this.inheritanceTracker = inheritanceTracker;
    }
    
    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces)
    {
        inheritanceTracker.addClass(name, superName, interfaces);
        
        if ((access & Opcodes.ACC_INTERFACE) != 0) {
            inheritanceTracker.addInterface(name);
        }
        
        super.visit(version, access, name, signature, superName, interfaces);
    }
}