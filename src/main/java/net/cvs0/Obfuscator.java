package net.cvs0;

import net.cvs0.config.ObfuscationConfig;
import net.cvs0.core.ObfuscationEngine;
import net.cvs0.transformers.ClassRenameTransformer;
import net.cvs0.transformers.FieldRenameTransformer;
import net.cvs0.transformers.MethodRenameTransformer;
import net.cvs0.transformers.LocalVariableRenameTransformer;
import net.cvs0.transformers.ConditionObfuscationTransformer;

import java.io.File;
import java.io.IOException;

public class Obfuscator
{
    private final ObfuscationEngine engine;
    
    public Obfuscator()
    {
        this.engine = new ObfuscationEngine();
        registerDefaultTransformers();
    }
    
    private void registerDefaultTransformers()
    {
        engine.registerTransformer(new ClassRenameTransformer());
        engine.registerTransformer(new FieldRenameTransformer());
        engine.registerTransformer(new MethodRenameTransformer());
        engine.registerTransformer(new ConditionObfuscationTransformer());
        engine.registerTransformer(new LocalVariableRenameTransformer());
    }
    
    public void obfuscate(File inputJar, File outputJar, ObfuscationConfig config, File mappingsFile) throws IOException
    {
        engine.obfuscate(inputJar, outputJar, config, mappingsFile);
    }
    
    public ObfuscationEngine getEngine()
    {
        return engine;
    }
}
