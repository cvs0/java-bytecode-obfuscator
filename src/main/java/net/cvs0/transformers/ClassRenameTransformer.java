package net.cvs0.transformers;

import net.cvs0.context.ObfuscationContext;
import net.cvs0.core.AbstractTransformer;
import net.cvs0.mappings.GlobalRemapper;
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
        if (context.getMappingManager() == null) {
            logTransformation("No mapping manager available, skipping transformation", context);
            reader.accept(writer, 0);
            return;
        }
        
        String className = reader.getClassName();
        GlobalRemapper globalRemapper = new GlobalRemapper(context.getMappingManager());
        ClassRemapper remapper = new ClassRemapper(writer, globalRemapper);
        
        String newName = context.getMappingManager().getClassMapping(className);
        if (!className.equals(newName)) {
            logTransformation("Applying mapping: " + className + " -> " + newName, context);
        }
        
        reader.accept(remapper, 0);
    }
    
    @Override
    public boolean isEnabled(ObfuscationContext context)
    {
        return context.getConfig().isRenameClasses() && context.getMappingManager() != null;
    }
}
