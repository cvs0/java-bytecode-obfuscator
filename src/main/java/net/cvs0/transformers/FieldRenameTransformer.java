package net.cvs0.transformers;

import net.cvs0.context.ObfuscationContext;
import net.cvs0.core.BaseClassVisitor;
import net.cvs0.core.BaseMethodVisitor;
import net.cvs0.core.ContextProvider;
import net.cvs0.core.RenameTransformer;
import net.cvs0.utils.TransformerUtils;
import org.objectweb.asm.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FieldRenameTransformer extends RenameTransformer
{
    private final Map<String, String> globalFieldMappings = new ConcurrentHashMap<>();
    
    public FieldRenameTransformer()
    {
        super("FieldRename", 200, "field");
    }
    
    @Override
    public boolean isEnabled(ObfuscationContext context)
    {
        return context.getConfig().isRenameFields();
    }

    @Override
    protected BaseClassVisitor createClassVisitor(org.objectweb.asm.ClassWriter writer, ObfuscationContext context, String className)
    {
        return new FieldRenameVisitor(writer, context);
    }

    private class FieldRenameVisitor extends RenameClassVisitor
    {
        private boolean isEnum;
        private boolean isInterface;

        public FieldRenameVisitor(org.objectweb.asm.ClassWriter classVisitor, ObfuscationContext context)
        {
            super(classVisitor, context);
        }
        
        @Override
        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces)
        {
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
            
            String fieldKey = getCurrentClassName() + "." + name;
            String newName = context.getMappingManager().getFieldMapping(getCurrentClassName(), name);
            
            if (newName != null && !newName.equals(name)) {
                logRename(fieldKey, newName, "field");
                return super.visitField(access, newName, descriptor, signature, value);
            } else {
                newName = generateMapping(fieldKey);
                globalFieldMappings.put(fieldKey, newName);
                context.getMappingManager().ensureFieldMapping(getCurrentClassName(), name, newName);
                context.addFieldMapping(fieldKey, newName);
                logRename(fieldKey, newName, "field");
                return super.visitField(access, newName, descriptor, signature, value);
            }
        }
        
        private boolean shouldRenameField(int access, String name, String descriptor)
        {
            if (!TransformerUtils.isValidFieldName(name)) {
                return false;
            }
            
            if (TransformerUtils.isEnumConstant(access, name, isEnum)) {
                return false;
            }
            
            if (TransformerUtils.isEnumSyntheticField(name)) {
                return false;
            }
            
            if (TransformerUtils.isCompilerGeneratedField(name)) {
                return false;
            }
            
            if (TransformerUtils.isSyntheticInnerClassField(access, name)) {
                return false;
            }
            
            return true;
        }
        
        @Override
        protected MethodVisitor createMethodVisitor(MethodVisitor mv, int access, String name, 
                                                  String descriptor, String signature, String[] exceptions)
        {
            return new FieldAccessRenameVisitor(mv, this, access, name, descriptor);
        }
    }
    
    private class FieldAccessRenameVisitor extends BaseMethodVisitor
    {
        public FieldAccessRenameVisitor(MethodVisitor methodVisitor, ContextProvider contextProvider,
                                      int access, String methodName, String methodDescriptor)
        {
            super(methodVisitor, contextProvider, access, methodName, methodDescriptor);
        }
        
        @Override
        protected String getTransformerName()
        {
            return "FieldRename";
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
            if (getContext().getMappingManager() != null) {
                String classMapping = getContext().getMappingManager().getClassMapping(className);
                if (classMapping != null) {
                    return classMapping;
                }
            }
            return className;
        }
        
        private String getMappedFieldName(String owner, String fieldName)
        {
            if (!TransformerUtils.isValidFieldName(fieldName) || owner == null) {
                return fieldName;
            }
            
            if (!getContext().getConfig().isInPackageScope(owner)) {
                return fieldName;
            }
            
            if (getContext().getConfig().shouldKeepField(owner, fieldName)) {
                return fieldName;
            }
            
            if (shouldSkipFieldMapping(fieldName)) {
                return fieldName;
            }
            
            String mappedName = getContext().getMappingManager().getFieldMapping(owner, fieldName);
            if (mappedName != null && !mappedName.equals(fieldName)) {
                return mappedName;
            }
            
            String fieldKey = owner + "." + fieldName;
            String cachedMapping = globalFieldMappings.get(fieldKey);
            if (cachedMapping != null) {
                return cachedMapping;
            }
            
            String contextMapping = getContext().getFieldMappings().get(fieldKey);
            if (contextMapping != null) {
                return contextMapping;
            }
            
            return fieldName;
        }
        
        private boolean shouldSkipFieldMapping(String fieldName)
        {
            return TransformerUtils.isEnumSyntheticField(fieldName) ||
                   TransformerUtils.isCompilerGeneratedField(fieldName) ||
                   fieldName.startsWith("this$") ||
                   fieldName.startsWith("val$");
        }
    }
}
