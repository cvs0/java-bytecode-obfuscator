package net.cvs0.mappings.generators;

import net.cvs0.config.ObfuscationConfig;

public class FieldNameGenerator extends BaseNameGenerator
{
    public FieldNameGenerator(ObfuscationConfig config)
    {
        super("f", config);
    }
    
    public String generateName(String owner, String fieldName, String descriptor)
    {
        return generateBaseName();
    }
}