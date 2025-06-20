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
            return mappingManager.getClassMapping(internalName);
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
            String mappedOwner = mappingManager.getClassMapping(owner);
            return mappingManager.getMethodMapping(mappedOwner, name, descriptor);
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
            String mappedOwner = mappingManager.getClassMapping(owner);
            return mappingManager.getFieldMapping(mappedOwner, name);
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