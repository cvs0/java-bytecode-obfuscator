package net.cvs0;

import net.cvs0.config.ObfuscationConfig;
import net.cvs0.core.ObfuscationEngine;
import net.cvs0.transformers.AntiDebuggingTransformer;
import net.cvs0.transformers.ClassRenameTransformer;
import net.cvs0.transformers.FieldRenameTransformer;
import net.cvs0.transformers.MethodRenameTransformer;
import net.cvs0.transformers.LocalVariableRenameTransformer;
import net.cvs0.transformers.ConditionObfuscationTransformer;
import net.cvs0.transformers.StringCompressionTransformer;
import net.cvs0.transformers.FakeInterfaceTransformer;
import net.cvs0.transformers.FakeExceptionTransformer;
import net.cvs0.transformers.MethodInlinerTransformer;

import net.cvs0.utils.AntiDebugger;

import java.io.File;
import java.io.IOException;
import net.cvs0.mappings.export.MappingExporter;

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
        engine.registerTransformer(new StringCompressionTransformer());
        engine.registerTransformer(new FakeInterfaceTransformer());
        engine.registerTransformer(new FakeExceptionTransformer());
        engine.registerTransformer(new MethodInlinerTransformer());
    }
    
    public void obfuscate(File inputJar, File outputJar, ObfuscationConfig config, File mappingsFile, MappingExporter.MappingFormat format) throws IOException
    {
        if (inputJar == null) {
            throw new IllegalArgumentException("Input JAR file cannot be null");
        }
        if (outputJar == null) {
            throw new IllegalArgumentException("Output JAR file cannot be null");
        }
        if (config == null) {
            throw new IllegalArgumentException("Obfuscation config cannot be null");
        }
        
        setupConditionalTransformers(config);
        engine.obfuscate(inputJar, outputJar, config, mappingsFile, format);
    }
    
    private void setupConditionalTransformers(ObfuscationConfig config)
    {
        if (config.isAntiDebugging()) {
            AntiDebugger.DebuggerAction action = config.getDebuggerAction() != null 
                ? config.getDebuggerAction() 
                : AntiDebugger.DebuggerAction.EXIT_SILENTLY;
            engine.registerTransformer(new AntiDebuggingTransformer(action));
        }
    }
    
    public ObfuscationEngine getEngine()
    {
        return engine;
    }
}
