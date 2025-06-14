package net.cvs0.mappings.generators;

import net.cvs0.config.ObfuscationConfig;

public class MethodNameGenerator extends BaseNameGenerator
{
    public MethodNameGenerator(ObfuscationConfig config)
    {
        super("m", config);
    }
    
    public String generateName(String owner, String methodName, String descriptor)
    {
        return generateBaseName();
    }
}