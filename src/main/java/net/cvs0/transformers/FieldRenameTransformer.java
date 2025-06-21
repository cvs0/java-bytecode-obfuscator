package net.cvs0.transformers;

import net.cvs0.context.ObfuscationContext;
import net.cvs0.core.AbstractTransformer;
import net.cvs0.mappings.MappingManager;
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
        private boolean isEnum;
        private boolean isInterface;

        public FieldRenameVisitor(ClassVisitor classVisitor, ObfuscationContext context)
        {
            super(Opcodes.ASM9, classVisitor);
            this.context = context;
        }
        
        @Override
        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces)
        {
            this.currentClassName = name;
            this.isEnum = (access & Opcodes.ACC_ENUM) != 0;
            this.isInterface = (access & Opcodes.ACC_INTERFACE) != 0;
            super.visit(version, access, name, signature, superName, interfaces);
        }
        
        @Override
        public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value)
        {
            if (!context.getConfig().isInPackageScope(currentClassName)) {
                return super.visitField(access, name, descriptor, signature, value);
            }
            
            if (context.getConfig().shouldKeepField(currentClassName, name)) {
                logTransformation("Keeping field: " + currentClassName + "." + name, context);
                return super.visitField(access, name, descriptor, signature, value);
            }
            
            if (shouldSkipField(access, name, descriptor)) {
                return super.visitField(access, name, descriptor, signature, value);
            }
            
            MappingManager mappingManager = context.getMappingManager();
            mappingManager.generateFieldMapping(currentClassName, name, descriptor);
            
            String newName = mappingManager.getFieldMapping(currentClassName, name);
            if (!newName.equals(name)) {
                logTransformation("Renaming field: " + currentClassName + "." + name + " -> " + newName, context);
            }
            
            return super.visitField(access, newName, descriptor, signature, value);
        }
        
        private boolean shouldSkipField(int access, String name, String descriptor)
        {
            if (isEnum) {
                if ((access & Opcodes.ACC_ENUM) != 0) {
                    return true;
                }
                if (name.equals("$VALUES") || name.equals("ENUM$VALUES")) {
                    return true;
                }
            }
            
            if (name.startsWith("$")) {
                if (name.startsWith("$assertionsDisabled") || name.startsWith("$switch")) {
                    return true;
                }
                if (name.equals("$VALUES") || name.equals("ENUM$VALUES")) {
                    return true;
                }
            }
            
            if ((access & Opcodes.ACC_SYNTHETIC) != 0) {
                if (name.startsWith("this$") || name.startsWith("val$")) {
                    return true;
                }
            }
            
            if (name.equals("serialVersionUID")) {
                return true;
            }
            
            return false;
        }
        
        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions)
        {
            MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
            return new FieldAccessRenameVisitor(mv, context, currentClassName);
        }
    }
    
    private static class FieldAccessRenameVisitor extends MethodVisitor
    {
        private final ObfuscationContext context;
        private final String currentClassName;
        
        public FieldAccessRenameVisitor(MethodVisitor methodVisitor, ObfuscationContext context, String currentClassName)
        {
            super(Opcodes.ASM9, methodVisitor);
            this.context = context;
            this.currentClassName = currentClassName;
        }
        
        @Override
        public void visitFieldInsn(int opcode, String owner, String name, String descriptor)
        {
            String mappedOwner = resolveMappedClassName(owner);
            String mappedName = resolveMappedFieldName(owner, name);
            
            super.visitFieldInsn(opcode, mappedOwner, mappedName, descriptor);
        }
        
        private String resolveMappedClassName(String className)
        {
            if (className == null) {
                return null;
            }
            return context.getMappingManager().getClassMapping(className);
        }
        
        private String resolveMappedFieldName(String owner, String fieldName)
        {
            if (fieldName == null || owner == null) {
                return fieldName;
            }
            
            if (!context.getConfig().isInPackageScope(owner)) {
                return fieldName;
            }
            
            if (context.getConfig().shouldKeepField(owner, fieldName)) {
                return fieldName;
            }
            
            if (shouldSkipFieldAccess(fieldName)) {
                return fieldName;
            }
            
            String exactMatch = context.getMappingManager().getFieldMapping(owner, fieldName);
            if (!exactMatch.equals(fieldName)) {
                return exactMatch;
            }
            
            String resolvedOwner = resolveFieldOwner(owner, fieldName);
            if (resolvedOwner != null && !resolvedOwner.equals(owner)) {
                String inheritedMapping = context.getMappingManager().getFieldMapping(resolvedOwner, fieldName);
                if (!inheritedMapping.equals(fieldName)) {
                    return inheritedMapping;
                }
            }
            
            return fieldName;
        }
        
        private boolean shouldSkipFieldAccess(String fieldName)
        {
            if (fieldName.equals("$VALUES") || fieldName.equals("ENUM$VALUES")) {
                return true;
            }
            
            if (fieldName.startsWith("$assertionsDisabled") || fieldName.startsWith("$switch")) {
                return true;
            }
            
            if (fieldName.startsWith("this$") || fieldName.startsWith("val$")) {
                return true;
            }
            
            if (fieldName.equals("serialVersionUID")) {
                return true;
            }
            
            if (fieldName.equals("TYPE") || fieldName.equals("class")) {
                return true;
            }
            
            return false;
        }
        
        private String resolveFieldOwner(String owner, String fieldName)
        {
            String currentFieldKey = owner + "." + fieldName;
            if (context.getFieldMappings().containsKey(currentFieldKey)) {
                return owner;
            }
            
            if (context.getMappingManager().getInheritanceTracker() != null) {
                var tracker = context.getMappingManager().getInheritanceTracker();
                
                for (String superClass : tracker.getAllSuperclasses(owner)) {
                    String superFieldKey = superClass + "." + fieldName;
                    if (context.getFieldMappings().containsKey(superFieldKey)) {
                        return superClass;
                    }
                }
                
                for (String iface : tracker.getAllInterfaces(owner)) {
                    String ifaceFieldKey = iface + "." + fieldName;
                    if (context.getFieldMappings().containsKey(ifaceFieldKey)) {
                        return iface;
                    }
                }
                
                if (tracker.isInnerClass(owner)) {
                    String outerClass = tracker.getOuterClass(owner);
                    if (outerClass != null) {
                        String outerFieldKey = outerClass + "." + fieldName;
                        if (context.getFieldMappings().containsKey(outerFieldKey)) {
                            return outerClass;
                        }
                        return resolveFieldOwner(outerClass, fieldName);
                    }
                }
            }
            
            return null;
        }
        
        private String getSuperClass(String className)
        {
            try {
                return context.getCurrentClassContext() != null ? 
                    context.getCurrentClassContext().getSuperClass() : null;
            } catch (Exception e) {
                return null;
            }
        }
        
        private String[] getInterfaces(String className)
        {
            try {
                return context.getCurrentClassContext() != null ? 
                    context.getCurrentClassContext().getInterfaces() : null;
            } catch (Exception e) {
                return new String[0];
            }
        }
    }
}
