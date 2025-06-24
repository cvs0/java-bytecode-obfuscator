package net.cvs0.obfuscation.strategies;

import net.cvs0.classfile.ProgramClass;
import net.cvs0.classfile.ProgramField;
import net.cvs0.config.ObfuscationConfig;
import net.cvs0.core.Program;
import net.cvs0.obfuscation.*;
import net.cvs0.utils.Logger;

import java.util.*;

public class FieldRenamingStrategy implements ObfuscationStrategy 
{
    private static final int PRIORITY = 300;
    private NameGenerator nameGenerator;
    private final Set<String> standardFields = new HashSet<>();

    public FieldRenamingStrategy() 
    {
        initializeStandardFields();
    }

    @Override
    public void obfuscate(Program program, ObfuscationConfig config, MappingContext mappingContext) throws ObfuscationException 
    {
        if (config.isVerbose()) {
            Logger.info("Starting field renaming obfuscation...");
        }

        nameGenerator = NameGenerator.createGenerator(config.getNamingMode());
        initializeKeepRules(program, config, mappingContext);

        Map<String, List<FieldInfo>> fieldGroups = groupFieldsByName(program, config, mappingContext);
        renameFieldGroups(fieldGroups, mappingContext, config);

        if (config.isVerbose()) {
            Logger.info("Field renaming completed. Renamed " + 
                      mappingContext.getAllFieldMappings().size() + " fields");
        }
    }

    private void initializeStandardFields() 
    {
        standardFields.addAll(Arrays.asList(
            "serialVersionUID", "INSTANCE", "TYPE", "class", "this$0"
        ));
    }

    private void initializeKeepRules(Program program, ObfuscationConfig config, MappingContext mappingContext) 
    {
        for (String keepField : config.getKeepFields()) {
            String[] parts = keepField.split("\\.");
            if (parts.length >= 2) {
                String className = parts[0];
                String fieldSignature = parts[1];
                
                int descriptorIndex = fieldSignature.indexOf(':');
                if (descriptorIndex > 0) {
                    String fieldName = fieldSignature.substring(0, descriptorIndex);
                    String descriptor = fieldSignature.substring(descriptorIndex + 1);
                    mappingContext.addKeepField(className, fieldName, descriptor);
                    nameGenerator.addReservedName(fieldName);
                }
            }
        }

        for (String customMapping : config.getCustomMappings().keySet()) {
            if (customMapping.contains(".") && customMapping.contains(":")) {
                String[] parts = customMapping.split("\\.", 2);
                String className = parts[0];
                String fieldSignature = parts[1];
                
                int descriptorIndex = fieldSignature.indexOf(':');
                if (descriptorIndex > 0) {
                    String fieldName = fieldSignature.substring(0, descriptorIndex);
                    String descriptor = fieldSignature.substring(descriptorIndex + 1);
                    String obfuscatedName = config.getCustomMappings().get(customMapping);
                    
                    mappingContext.mapField(className, fieldName, descriptor, obfuscatedName);
                    nameGenerator.addReservedName(obfuscatedName);
                }
            }
        }

        for (ProgramClass cls : program.getAllClasses()) {
            for (ProgramField field : cls.getFields()) {
                if (isSerializationField(field)) {
                    mappingContext.addKeepField(cls.getName(), field.getName(), field.getDescriptor());
                    nameGenerator.addReservedName(field.getName());
                }
            }
        }
    }

    private Map<String, List<FieldInfo>> groupFieldsByName(Program program, ObfuscationConfig config, MappingContext mappingContext) 
    {
        Map<String, List<FieldInfo>> fieldGroups = new HashMap<>();

        for (ProgramClass cls : program.getAllClasses()) {
            for (ProgramField field : cls.getFields()) {
                if (shouldRenameField(cls, field, config, mappingContext)) {
                    String groupKey = createFieldGroupKey(field);
                    FieldInfo fieldInfo = new FieldInfo(cls.getName(), field);
                    
                    fieldGroups.computeIfAbsent(groupKey, k -> new ArrayList<>()).add(fieldInfo);
                }
            }
        }

        return fieldGroups;
    }

    private boolean shouldRenameField(ProgramClass cls, ProgramField field, ObfuscationConfig config, MappingContext mappingContext) 
    {
        String className = cls.getName();
        String fieldName = field.getName();
        String descriptor = field.getDescriptor();

        if (mappingContext.shouldKeepField(className, fieldName, descriptor)) {
            return false;
        }

        if (mappingContext.hasFieldMapping(className, fieldName, descriptor)) {
            return false;
        }

        if (standardFields.contains(fieldName)) {
            return false;
        }

        if (fieldName.startsWith("this$") || fieldName.startsWith("val$")) {
            return config.getObfuscationLevel().ordinal() >= 3;
        }

        if (field.isSynthetic() && config.getObfuscationLevel().ordinal() < 2) {
            return false;
        }

        if (isEnumConstant(cls, field)) {
            return config.getObfuscationLevel().ordinal() >= 2;
        }

        if (isAnnotationField(cls, field)) {
            return config.getObfuscationLevel().ordinal() >= 2;
        }

        if (isInheritedFromLibrary(cls, field)) {
            return false;
        }

        if (!config.getIncludePackages().isEmpty()) {
            boolean included = false;
            for (String includePackage : config.getIncludePackages()) {
                String packagePrefix = includePackage.replace('.', '/');
                if (className.startsWith(packagePrefix)) {
                    included = true;
                    break;
                }
            }
            if (!included) {
                return false;
            }
        }

        for (String excludePackage : config.getExcludePackages()) {
            String packagePrefix = excludePackage.replace('.', '/');
            if (className.startsWith(packagePrefix)) {
                return false;
            }
        }

        if (config.isStayInScope()) {
            String scopePrefix = config.getScopePrefix();
            if (scopePrefix != null && !className.startsWith(scopePrefix + "/")) {
                return false;
            }
        }

        return true;
    }

    private boolean isSerializationField(ProgramField field) 
    {
        return field.getName().equals("serialVersionUID") && 
               field.getDescriptor().equals("J") && 
               field.isStatic() && field.isFinal();
    }

    private boolean isEnumConstant(ProgramClass cls, ProgramField field) 
    {
        return cls.isEnum() && field.isStatic() && field.isFinal() && 
               field.getDescriptor().equals("L" + cls.getName() + ";");
    }

    private boolean isAnnotationField(ProgramClass cls, ProgramField field) 
    {
        return cls.isAnnotation() && field.isAbstract();
    }

    private boolean isInheritedFromLibrary(ProgramClass cls, ProgramField field) 
    {
        if (cls.getSuperName() != null && (
            cls.getSuperName().startsWith("java/") || 
            cls.getSuperName().startsWith("javax/") ||
            cls.getSuperName().startsWith("android/"))) {
            return true;
        }

        return false;
    }

    private String createFieldGroupKey(ProgramField field) 
    {
        return field.getName() + ":" + field.getDescriptor();
    }

    private void renameFieldGroups(Map<String, List<FieldInfo>> fieldGroups, MappingContext mappingContext, ObfuscationConfig config) 
    {
        for (Map.Entry<String, List<FieldInfo>> entry : fieldGroups.entrySet()) {
            String groupKey = entry.getKey();
            List<FieldInfo> fields = entry.getValue();

            if (fields.isEmpty()) {
                continue;
            }

            if (hasConflictingFieldTypes(fields)) {
                renameFieldsIndividually(fields, mappingContext, config);
            } else {
                renameFieldGroup(fields, mappingContext, config);
            }
        }
    }

    private boolean hasConflictingFieldTypes(List<FieldInfo> fields) 
    {
        Set<String> descriptors = new HashSet<>();
        for (FieldInfo fieldInfo : fields) {
            descriptors.add(fieldInfo.field.getDescriptor());
        }
        return descriptors.size() > 1;
    }

    private void renameFieldsIndividually(List<FieldInfo> fields, MappingContext mappingContext, ObfuscationConfig config) 
    {
        for (FieldInfo fieldInfo : fields) {
            String obfuscatedName = nameGenerator.generateFieldName();
            mappingContext.mapField(fieldInfo.className, fieldInfo.field.getName(), 
                                  fieldInfo.field.getDescriptor(), obfuscatedName);
            
            if (config.isVerbose()) {
                Logger.debug("Mapped field (individual): " + fieldInfo.className + "." + 
                           fieldInfo.field.getName() + " -> " + obfuscatedName);
            }
        }
    }

    private void renameFieldGroup(List<FieldInfo> fields, MappingContext mappingContext, ObfuscationConfig config) 
    {
        String obfuscatedName = nameGenerator.generateFieldName();
        
        for (FieldInfo fieldInfo : fields) {
            mappingContext.mapField(fieldInfo.className, fieldInfo.field.getName(), 
                                  fieldInfo.field.getDescriptor(), obfuscatedName);
            
            if (config.isVerbose()) {
                Logger.debug("Mapped field (group): " + fieldInfo.className + "." + 
                           fieldInfo.field.getName() + " -> " + obfuscatedName);
            }
        }
    }

    @Override
    public String getName() 
    {
        return "Field Renaming";
    }

    @Override
    public boolean isEnabled(ObfuscationConfig config) 
    {
        return config.isRenameFields();
    }

    @Override
    public int getPriority() 
    {
        return PRIORITY;
    }

    private static class FieldInfo 
    {
        final String className;
        final ProgramField field;

        FieldInfo(String className, ProgramField field) 
        {
            this.className = className;
            this.field = field;
        }
    }
}