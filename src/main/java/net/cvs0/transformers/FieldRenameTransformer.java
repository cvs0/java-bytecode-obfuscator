package net.cvs0.transformers;

import net.cvs0.context.ObfuscationContext;
import net.cvs0.core.AbstractTransformer;
import net.cvs0.mappings.remappers.MapBasedRenamer;
import org.objectweb.asm.*;

public class FieldRenameTransformer extends AbstractTransformer
{
    public FieldRenameTransformer()
    {
        super("FieldRename", 200);
    }
    
    @Override
    public void transform(ClassReader reader, ClassWriter writer, ObfuscationContext context)
    {
        if (!context.getConfig().isRenameFields()) {
            reader.accept(writer, 0);
            return;
        }
        
        ClassVisitor visitor = new FieldRenameVisitor(writer, context);
        reader.accept(visitor, 0);
    }
    
    @Override
    public boolean isEnabled(ObfuscationContext context)
    {
        return context.getConfig().isRenameFields();
    }
    
    private class FieldRenameVisitor extends ClassVisitor
    {
        private final ObfuscationContext context;
        private String currentClassName;
        
        public FieldRenameVisitor(ClassVisitor classVisitor, ObfuscationContext context)
        {
            super(Opcodes.ASM9, classVisitor);
            this.context = context;
        }
        
        @Override
        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces)
        {
            this.currentClassName = name;
            super.visit(version, access, name, signature, superName, interfaces);
        }
        
        @Override
        public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value)
        {
            if (!context.getConfig().isInPackageScope(currentClassName)) {
                return super.visitField(access, name, descriptor, signature, value);
            }
            
            String fieldKey = currentClassName + "." + name;
            
            if (context.getConfig().shouldKeepField(currentClassName, name)) {
                logTransformation("Keeping field: " + fieldKey, context);
                return super.visitField(access, name, descriptor, signature, value);
            }
            
            String newName = context.getFieldMappings().get(fieldKey);
            if (newName == null) {
                MapBasedRenamer renamer = context.getRenamer("field");
                newName = renamer.generateName(fieldKey);
                context.addFieldMapping(fieldKey, newName);
                logTransformation("Renaming field: " + fieldKey + " -> " + newName, context);
            }
            
            return super.visitField(access, newName, descriptor, signature, value);
        }
        
        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions)
        {
            MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
            return new FieldAccessRenameVisitor(mv, context);
        }
    }
    
    private static class FieldAccessRenameVisitor extends MethodVisitor
    {
        private final ObfuscationContext context;
        
        public FieldAccessRenameVisitor(MethodVisitor methodVisitor, ObfuscationContext context)
        {
            super(Opcodes.ASM9, methodVisitor);
            this.context = context;
        }
        
        @Override
        public void visitFieldInsn(int opcode, String owner, String name, String descriptor)
        {
            String mappedOwner = context.getClassMappings().getOrDefault(owner, owner);
            String fieldKey = owner + "." + name;
            
            if (context.getConfig().isInPackageScope(owner)) {
                String newName = context.getFieldMappings().getOrDefault(fieldKey, name);
                super.visitFieldInsn(opcode, mappedOwner, newName, descriptor);
            } else {
                super.visitFieldInsn(opcode, mappedOwner, name, descriptor);
            }
        }
    }
}
