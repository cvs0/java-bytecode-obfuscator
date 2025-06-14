package net.cvs0.core;

import net.cvs0.config.ObfuscationConfig;
import net.cvs0.discovery.ClassDiscoveryVisitor;
import net.cvs0.discovery.InheritanceDiscoveryVisitor;
import net.cvs0.mappings.GlobalRemapper;
import net.cvs0.mappings.MappingManager;
import net.cvs0.mappings.InheritanceTracker;
import net.cvs0.utils.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.commons.ClassRemapper;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ComprehensiveObfuscationEngine
{
    private final ObfuscationConfig config;
    private final MappingManager mappingManager;
    private final GlobalRemapper globalRemapper;
    
    public ComprehensiveObfuscationEngine(ObfuscationConfig config)
    {
        this.config = config;
        this.mappingManager = new MappingManager(config);
        this.globalRemapper = new GlobalRemapper(mappingManager);
    }
    
    public Map<String, byte[]> obfuscate(Map<String, byte[]> classes) throws IOException
    {
        if (config.isVerbose()) {
            System.out.println("Starting comprehensive obfuscation...");
        }
        
        Set<String> classNames = new HashSet<>();
        Map<String, ClassReader> readers = new HashMap<>();
        
        for (Map.Entry<String, byte[]> entry : classes.entrySet()) {
            ClassReader reader = new ClassReader(entry.getValue());
            String className = reader.getClassName();
            classNames.add(className);
            readers.put(className, reader);
        }
        
        discoverAndGenerateMappings(readers, classNames);
        
        return applyMappings(readers);
    }
    
    private void discoverAndGenerateMappings(Map<String, ClassReader> readers, Set<String> classNames)
    {
        Logger.subPhase("Discovery and Mapping Generation");
        
        Logger.step("Building inheritance hierarchy");
        InheritanceTracker inheritanceTracker = new InheritanceTracker();
        mappingManager.setInheritanceTracker(inheritanceTracker);
        
        for (ClassReader reader : readers.values()) {
            InheritanceDiscoveryVisitor inheritanceVisitor = new InheritanceDiscoveryVisitor(inheritanceTracker);
            reader.accept(inheritanceVisitor, ClassReader.SKIP_CODE);
        }
        Logger.success("Inheritance hierarchy built successfully");
        
        Logger.step("Generating class mappings");
        mappingManager.generateClassMappings(classNames);
        Logger.success(String.format("Generated %d class mappings", mappingManager.getClassMappings().size()));
        
        Logger.step("Generating field and method mappings");
        for (ClassReader reader : readers.values()) {
            ClassDiscoveryVisitor discoveryVisitor = new ClassDiscoveryVisitor(mappingManager, inheritanceTracker);
            reader.accept(discoveryVisitor, ClassReader.SKIP_CODE);
        }
        
        Logger.success("Mapping generation completed");
        Logger.stats("Class mappings", mappingManager.getClassMappings().size());
        Logger.stats("Field mappings", mappingManager.getFieldMappings().size());
        Logger.stats("Method mappings", mappingManager.getMethodMappings().size());
    }
    
    private Map<String, byte[]> applyMappings(Map<String, ClassReader> readers)
    {
        Logger.subPhase("Applying Mappings");
        Logger.step("Processing classes with generated mappings");
        
        Map<String, byte[]> obfuscatedClasses = new HashMap<>();
        int processedCount = 0;
        
        for (Map.Entry<String, ClassReader> entry : readers.entrySet()) {
            String originalName = entry.getKey();
            ClassReader reader = entry.getValue();
            
            ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
            ClassRemapper remapper = new ClassRemapper(writer, globalRemapper);
            
            reader.accept(remapper, 0);
            
            String newName = mappingManager.getClassMapping(originalName);
            obfuscatedClasses.put(newName, writer.toByteArray());
            
            if (!originalName.equals(newName)) {
                Logger.transformation("Processed class: " + originalName + " → " + newName);
            }
            processedCount++;
        }
        
        Logger.success(String.format("Successfully processed %d classes", processedCount));
        return obfuscatedClasses;
    }
    
    public MappingManager getMappingManager()
    {
        return mappingManager;
    }
}