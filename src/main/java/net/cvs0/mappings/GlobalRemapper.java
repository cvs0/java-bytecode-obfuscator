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
        return mappingManager.getClassMapping(internalName);
    }
    
    @Override
    public String mapMethodName(String owner, String name, String descriptor)
    {
        String mappedOwner = mappingManager.getClassMapping(owner);
        return mappingManager.getMethodMapping(mappedOwner, name, descriptor);
    }
    
    @Override
    public String mapFieldName(String owner, String name, String descriptor)
    {
        String mappedOwner = mappingManager.getClassMapping(owner);
        return mappingManager.getFieldMapping(mappedOwner, name);
    }
    
    @Override
    public String mapDesc(String descriptor)
    {
        return super.mapDesc(descriptor);
    }
    
    @Override
    public String mapSignature(String signature, boolean typeSignature)
    {
        return super.mapSignature(signature, typeSignature);
    }
}