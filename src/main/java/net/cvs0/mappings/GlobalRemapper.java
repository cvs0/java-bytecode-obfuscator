package net.cvs0.mappings;

import org.objectweb.asm.commons.Remapper;

public class GlobalRemapper extends Remapper
{
    private final MappingManager mappingManager;
    
    public GlobalRemapper(MappingManager mappingManager)
    {
        this.mappingManager = mappingManager;
    }
    
    @Override
    public String map(String internalName)
    {
        if (internalName == null) {
            return null;
        }
        try {
            String mapping = mappingManager.getClassMapping(internalName);
            if (mapping != null && !mapping.equals(internalName)) {
                return mapping;
            }
            return internalName;
        } catch (Exception e) {
            return internalName;
        }
    }
    
    @Override
    public String mapMethodName(String owner, String name, String descriptor)
    {
        if (owner == null || name == null) {
            return name;
        }
        try {
            String mapping = mappingManager.getMethodMapping(owner, name, descriptor);
            if (mapping != null && !mapping.equals(name)) {
                return mapping;
            }
            return name;
        } catch (Exception e) {
            return name;
        }
    }
    
    @Override
    public String mapFieldName(String owner, String name, String descriptor)
    {
        if (owner == null || name == null) {
            return name;
        }
        try {
            String mapping = mappingManager.getFieldMapping(owner, name);
            if (mapping != null && !mapping.equals(name)) {
                return mapping;
            }
            return name;
        } catch (Exception e) {
            return name;
        }
    }
    
    @Override
    public String mapDesc(String descriptor)
    {
        if (descriptor == null) {
            return null;
        }
        try {
            return super.mapDesc(descriptor);
        } catch (Exception e) {
            return descriptor;
        }
    }
    
    @Override
    public String mapSignature(String signature, boolean typeSignature)
    {
        if (signature == null) {
            return null;
        }
        try {
            return super.mapSignature(signature, typeSignature);
        } catch (Exception e) {
            return signature;
        }
    }
}