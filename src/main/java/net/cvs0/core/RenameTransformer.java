package net.cvs0.core;

import net.cvs0.context.ObfuscationContext;
import net.cvs0.mappings.remappers.MapBasedRenamer;
import org.objectweb.asm.ClassWriter;

public abstract class RenameTransformer extends VisitorBasedTransformer
{
    private final String renamerType;
    
    protected RenameTransformer(String name, int priority, String renamerType)
    {
        super(name, priority);
        this.renamerType = renamerType;
    }
    
    protected final MapBasedRenamer getRenamer(ObfuscationContext context)
    {
        return context.getRenamer(renamerType);
    }
    
    protected abstract class RenameClassVisitor extends BaseClassVisitor
    {
        protected final MapBasedRenamer renamer;
        
        protected RenameClassVisitor(ClassWriter classVisitor, ObfuscationContext context)
        {
            super(classVisitor, context);
            this.renamer = getRenamer(context);
        }
        
        @Override
        protected final String getTransformerName()
        {
            return getName();
        }
        
        protected final String generateMapping(String key)
        {
            return renamer.generateName(key);
        }
        
        protected final void logRename(String from, String to, String type)
        {
            logTransformation("Renaming " + type + ": " + from + " -> " + to);
        }
    }
}