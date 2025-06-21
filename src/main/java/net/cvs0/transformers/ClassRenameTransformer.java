package net.cvs0.transformers;

import net.cvs0.context.ObfuscationContext;
import net.cvs0.core.BaseTransformer;
import net.cvs0.mappings.GlobalRemapper;
import net.cvs0.mappings.SafeClassRemapper;
import org.objectweb.asm.*;

public class ClassRenameTransformer extends BaseTransformer
{
    public ClassRenameTransformer()
    {
        super("ClassRename", 100);
    }
    
    @Override
    public boolean isEnabled(ObfuscationContext context)
    {
        return context.getConfig().isRenameClasses() && context.getMappingManager() != null;
    }
    
    @Override
    protected boolean validateInputs(ClassReader reader, ClassWriter writer, ObfuscationContext context)
    {
        if (!super.validateInputs(reader, writer, context)) {
            return false;
        }
        
        if (context.getMappingManager() == null) {
            logTransformation("No mapping manager available, skipping transformation", context);
            return false;
        }
        
        return true;
    }
    
    @Override
    protected boolean shouldSkipClass(String className, ObfuscationContext context)
    {
        return !context.getConfig().isInPackageScope(className);
    }
    
    @Override
    protected void performTransformation(ClassReader reader, ClassWriter writer, 
                                       ObfuscationContext context, String className)
    {
        validateClassStructure(reader, className, context);
        
        GlobalRemapper globalRemapper = new GlobalRemapper(context.getMappingManager());
        SafeClassRemapper remapper = new SafeClassRemapper(writer, globalRemapper, className);
        
        String newName = context.getMappingManager().getClassMapping(className);
        boolean hasClassMapping = newName != null && !className.equals(newName);
        
        if (hasClassMapping) {
            logTransformation("Applying class mapping: " + className + " -> " + newName, context);
        } else {
            logTransformation("Updating references in class: " + className, context);
        }
        
        reader.accept(remapper, 0);
    }
    
    private void validateClassStructure(ClassReader reader, String className, ObfuscationContext context)
    {
        try {
            String superName = reader.getSuperName();
            String[] interfaces = reader.getInterfaces();
            int access = reader.getAccess();
            
            if (superName != null && superName.length() > 1000) {
                throw new IllegalStateException("Super class name is too long: " + superName.length());
            }
            
            if (interfaces != null) {
                for (String interfaceName : interfaces) {
                    if (interfaceName != null && interfaceName.length() > 1000) {
                        throw new IllegalStateException("Interface name is too long: " + interfaceName.length());
                    }
                }
            }
            
            if ((access & Opcodes.ACC_ABSTRACT) != 0 && (access & Opcodes.ACC_FINAL) != 0) {
                logTransformation("Warning: Class " + className + " has both ABSTRACT and FINAL modifiers", context);
            }
            
        } catch (Exception e) {
            logTransformation("Failed to validate class structure for " + className + ": " + e.getMessage(), context);
            throw new RuntimeException(e);
        }
    }
}
