package net.cvs0.obfuscation;

import net.cvs0.core.Program;
import net.cvs0.config.ObfuscationConfig;

public interface ObfuscationStrategy 
{
    void obfuscate(Program program, ObfuscationConfig config, MappingContext mappingContext) throws ObfuscationException;
    
    String getName();
    
    boolean isEnabled(ObfuscationConfig config);
    
    int getPriority();
}