package net.cvs0.transformers;

import net.cvs0.context.ObfuscationContext;
import net.cvs0.core.UniversalTransformer;
import net.cvs0.mappings.remappers.MapBasedRenamer;
import org.objectweb.asm.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FieldRenameTransformer extends UniversalTransformer
{
    private final Map<String, String> globalFieldMappings = new ConcurrentHashMap<>();
    
    public FieldRenameTransformer()
    {
        super("FieldRename", 200);
    }
    
    @Override
    public boolean isEnabled(ObfuscationContext context)
    {
        return context.getConfig().isRenameFields();
    }

    @Override
    protected UniversalClassVisitor createClassVisitor(ClassWriter writer, ObfuscationContext context)
    {
        return new FieldRenameVisitor(writer, context);
    }

    private class FieldRenameVisitor extends UniversalClassVisitor
    {
        private final ObfuscationContext context;
        private String currentClassName;
        private boolean isEnum;
        private boolean isInterface;
        private MapBasedRenamer fieldRenamer;

        public FieldRenameVisitor(ClassVisitor classVisitor, ObfuscationContext context)
        {
            super(classVisitor, context);
            this.context = context;
            this.fieldRenamer = context.getRenamer("field");
        }

        @Override
        public String getTransformerName()
        {
            return "FieldRename";
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
            if (!shouldRenameField(access, name, descriptor)) {
                return super.visitField(access, name, descriptor, signature, value);
            }
            
            String fieldKey = currentClassName + "." + name;
            String newName = context.getMappingManager().getFieldMapping(currentClassName, name);
            
            if (newName != null && !newName.equals(name)) {
                logTransformation("Renaming field: " + fieldKey + " -> " + newName);
                return super.visitField(access, newName, descriptor, signature, value);
            } else {
                newName = fieldRenamer.generateName(fieldKey);
                globalFieldMappings.put(fieldKey, newName);
                context.getMappingManager().ensureFieldMapping(currentClassName, name, newName);
                context.addFieldMapping(fieldKey, newName);
                logTransformation("Generating field mapping: " + fieldKey + " -> " + newName);
                return super.visitField(access, newName, descriptor, signature, value);
            }
        }
        
        private boolean shouldRenameField(int access, String name, String descriptor)
        {
            if (!context.getConfig().isInPackageScope(currentClassName)) {
                if (context.getConfig().isVerbose()) {
                    System.out.println("[FieldRename] Skipping field " + currentClassName + "." + name + " - not in package scope");
                }
                return false;
            }
            
            if (context.getConfig().shouldKeepField(currentClassName, name)) {
                if (context.getConfig().isVerbose()) {
                    System.out.println("[FieldRename] Keeping field " + currentClassName + "." + name + " - in keep rules");
                }
                return false;
            }
            
            if (name == null) {
                return false;
            }
            
            if (isEnum && (access & Opcodes.ACC_ENUM) != 0) {
                if (context.getConfig().isVerbose()) {
                    System.out.println("[FieldRename] Skipping enum constant " + currentClassName + "." + name);
                }
                return false;
            }
            
            if (name.equals("$VALUES") || name.equals("ENUM$VALUES")) {
                if (context.getConfig().isVerbose()) {
                    System.out.println("[FieldRename] Skipping enum values array " + currentClassName + "." + name);
                }
                return false;
            }
            
            if (name.startsWith("$assertionsDisabled") || name.startsWith("$switch")) {
                if (context.getConfig().isVerbose()) {
                    System.out.println("[FieldRename] Skipping compiler-generated field " + currentClassName + "." + name);
                }
                return false;
            }
            
            if ((access & Opcodes.ACC_SYNTHETIC) != 0 && (name.startsWith("this$") || name.startsWith("val$"))) {
                if (context.getConfig().isVerbose()) {
                    System.out.println("[FieldRename] Skipping synthetic field " + currentClassName + "." + name);
                }
                return false;
            }
            
            if (name.equals("serialVersionUID")) {
                if (context.getConfig().isVerbose()) {
                    System.out.println("[FieldRename] Skipping serialVersionUID field " + currentClassName + "." + name);
                }
                return false;
            }
            
            if (name.equals("TYPE") || name.equals("class")) {
                if (context.getConfig().isVerbose()) {
                    System.out.println("[FieldRename] Skipping TYPE/class field " + currentClassName + "." + name);
                }
                return false;
            }
            
            if (context.getConfig().isVerbose()) {
                System.out.println("[FieldRename] Will rename field " + currentClassName + "." + name);
            }
            return true;
        }
        
        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions)
        {
            MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
            return new FieldAccessRenameVisitor(mv, context, currentClassName);
        }
    }
    
    private class FieldAccessRenameVisitor extends MethodVisitor
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
            String mappedOwner = getMappedClassName(owner);
            String mappedName = getMappedFieldName(owner, name);
            
            super.visitFieldInsn(opcode, mappedOwner, mappedName, descriptor);
        }
        
        private String getMappedClassName(String className)
        {
            if (className == null) {
                return null;
            }
            return context.getClassMappings().getOrDefault(className, className);
        }
        
        private String getMappedFieldName(String owner, String fieldName)
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
            
            if (shouldSkipFieldMapping(fieldName)) {
                return fieldName;
            }
            
            String mappedName = context.getMappingManager().getFieldMapping(owner, fieldName);
            if (mappedName != null && !mappedName.equals(fieldName)) {
                return mappedName;
            }
            
            String fieldKey = owner + "." + fieldName;
            String cachedMapping = globalFieldMappings.get(fieldKey);
            if (cachedMapping != null) {
                return cachedMapping;
            }
            
            String contextMapping = context.getFieldMappings().get(fieldKey);
            if (contextMapping != null) {
                return contextMapping;
            }
            
            return fieldName;
        }
        
        private boolean shouldSkipFieldMapping(String fieldName)
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
    }
}
