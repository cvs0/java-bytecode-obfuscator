package net.cvs0.transformers;

import net.cvs0.context.ObfuscationContext;
import net.cvs0.core.AbstractTransformer;
import net.cvs0.mappings.GlobalRemapper;
import net.cvs0.mappings.SafeClassRemapper;
import org.objectweb.asm.*;
import org.objectweb.asm.commons.ClassRemapper;

public class ClassRenameTransformer extends AbstractTransformer
{
    public ClassRenameTransformer()
    {
        super("ClassRename", 100);
    }
    
    @Override
    public void transform(ClassReader reader, ClassWriter writer, ObfuscationContext context)
    {
        if (context == null) {
            throw new IllegalArgumentException("ObfuscationContext cannot be null");
        }
        
        if (reader == null) {
            throw new IllegalArgumentException("ClassReader cannot be null");
        }
        
        if (writer == null) {
            throw new IllegalArgumentException("ClassWriter cannot be null");
        }
        
        if (context.getMappingManager() == null) {
            logTransformation("No mapping manager available, skipping transformation", context);
            reader.accept(writer, 0);
            return;
        }
        
        String className = null;
        try {
            className = reader.getClassName();
        } catch (Exception e) {
            logTransformation("Failed to get class name from reader: " + e.getMessage() + " - skipping transformation", context);
            reader.accept(writer, 0);
            return;
        }
        
        if (className == null || className.isEmpty()) {
            logTransformation("ClassReader returned null or empty className - skipping transformation", context);
            reader.accept(writer, 0);
            return;
        }
        
        if (className.length() > 1000) {
            logTransformation("Class name is suspiciously long (" + className.length() + " chars) - skipping transformation", context);
            reader.accept(writer, 0);
            return;
        }
        
        try {
            validateClassStructure(reader, className, context);
            
            GlobalRemapper globalRemapper = new GlobalRemapper(context.getMappingManager());
            SafeClassRemapper remapper = new SafeClassRemapper(writer, globalRemapper, className);
            
            String newName = context.getMappingManager().getClassMapping(className);
            if (newName == null) {
                logTransformation("No mapping found for class: " + className, context);
                reader.accept(writer, 0);
                return;
            }
            
            if (!className.equals(newName)) {
                logTransformation("Applying mapping: " + className + " -> " + newName, context);
            }
            
            reader.accept(remapper, 0);
            
        } catch (SecurityException e) {
            logTransformation("Security restriction while transforming class " + className + ": " + e.getMessage(), context);
            throw e;
        } catch (OutOfMemoryError e) {
            logTransformation("Out of memory while transforming class " + className, context);
            throw e;
        } catch (Exception e) {
            logTransformation("Failed to apply remapping for class " + className + ": " + e.getMessage() + " - using original", context);
            if (context.getConfig().isVerbose()) {
                e.printStackTrace();
            }
            reader.accept(writer, 0);
        }
    }
    
    private void validateClassStructure(ClassReader reader, String className, ObfuscationContext context) throws Exception
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
            throw e;
        }
    }
    
    @Override
    public boolean isEnabled(ObfuscationContext context)
    {
        return context.getConfig().isRenameClasses() && context.getMappingManager() != null;
    }
}
