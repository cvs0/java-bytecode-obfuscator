package net.cvs0.core;

import net.cvs0.config.ObfuscationConfig;
import net.cvs0.context.ObfuscationContext;
import net.cvs0.mappings.MappingProcessor;
import net.cvs0.mappings.InheritanceTracker;
import net.cvs0.utils.ValidationUtils;
import net.cvs0.utils.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import java.io.*;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

public class ObfuscationEngine
{
    private final ComprehensiveObfuscationEngine comprehensiveEngine;
    private final MappingProcessor mappingProcessor;
    private final List<Transformer> transformers;
    
    public ObfuscationEngine()
    {
        this.comprehensiveEngine = null;
        this.mappingProcessor = new MappingProcessor();
        this.transformers = new ArrayList<>();
    }
    
    public void registerTransformer(Transformer transformer)
    {
        transformers.add(transformer);
        transformers.sort(Comparator.comparingInt(Transformer::getPriority).reversed());
    }
    
    public void obfuscate(File inputJar, File outputJar, ObfuscationConfig config, File mappingsFile) throws IOException
    {
        Logger.setVerbose(config.isVerbose());
        
        validateInputs(inputJar, outputJar, config, mappingsFile);
        
        Logger.phase("Bytecode Obfuscation Process");
        logStart(inputJar, outputJar, config);
        
        Logger.step("Loading classes from JAR file");
        Map<String, byte[]> inputClasses = loadClassesFromJar(inputJar);
        Logger.success(String.format("Loaded %d classes from input JAR", inputClasses.size()));
        
        Map<String, byte[]> obfuscatedClasses;
        if (!transformers.isEmpty()) {
            Logger.step("Starting transformer-based obfuscation");
            obfuscatedClasses = obfuscateWithTransformers(inputClasses, config);
        } else {
            Logger.step("Starting comprehensive obfuscation");
            ComprehensiveObfuscationEngine engine = new ComprehensiveObfuscationEngine(config);
            obfuscatedClasses = engine.obfuscate(inputClasses);
        }
        
        Logger.step("Writing obfuscated JAR file");
        writeObfuscatedJar(inputJar, outputJar, obfuscatedClasses);
        Logger.success("Obfuscated JAR file written successfully");
        
        if (mappingsFile != null) {
            Logger.step("Writing mappings file");
            writeMappingsFromContext(mappingsFile, config);
        }
        
        logCompleteWithTransformers(config, obfuscatedClasses.size());
    }
    
    private Map<String, byte[]> obfuscateWithTransformers(Map<String, byte[]> inputClasses, ObfuscationConfig config) throws IOException
    {
        ObfuscationContext context = new ObfuscationContext(config);
        
        if (config.isRenameClasses()) {
            setupMappingManager(context, inputClasses);
        }
        
        Map<String, byte[]> result = new HashMap<>();
        
        for (Map.Entry<String, byte[]> entry : inputClasses.entrySet()) {
            String className = entry.getKey();
            byte[] classBytes = entry.getValue();
            
            ClassReader reader = new ClassReader(classBytes);
            ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
            
            ClassReader currentReader = reader;
            ClassWriter currentWriter = writer;
            
            for (Transformer transformer : transformers) {
                if (transformer.isEnabled(context)) {
                    ClassWriter nextWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
                    transformer.transform(currentReader, nextWriter, context);
                    
                    currentReader = new ClassReader(nextWriter.toByteArray());
                    currentWriter = nextWriter;
                }
            }
            
            String finalClassName = context.getClassMappings().getOrDefault(className, className);
            result.put(finalClassName, currentWriter.toByteArray());
        }
        
        return result;
    }
    
    private void setupMappingManager(ObfuscationContext context, Map<String, byte[]> inputClasses)
    {
        net.cvs0.mappings.MappingManager mappingManager = new net.cvs0.mappings.MappingManager(context.getConfig());
        InheritanceTracker inheritanceTracker = new InheritanceTracker();
        mappingManager.setInheritanceTracker(inheritanceTracker);
        
        for (Map.Entry<String, byte[]> entry : inputClasses.entrySet()) {
            ClassReader reader = new ClassReader(entry.getValue());
            net.cvs0.discovery.InheritanceDiscoveryVisitor inheritanceVisitor = new net.cvs0.discovery.InheritanceDiscoveryVisitor(inheritanceTracker);
            reader.accept(inheritanceVisitor, ClassReader.SKIP_CODE);
        }
        
        Set<String> classNames = new HashSet<>();
        for (String className : inputClasses.keySet()) {
            ClassReader reader = new ClassReader(inputClasses.get(className));
            classNames.add(reader.getClassName());
        }
        
        mappingManager.generateClassMappings(classNames);
        
        for (Map.Entry<String, byte[]> entry : inputClasses.entrySet()) {
            ClassReader reader = new ClassReader(entry.getValue());
            net.cvs0.discovery.ClassDiscoveryVisitor discoveryVisitor = new net.cvs0.discovery.ClassDiscoveryVisitor(mappingManager, inheritanceTracker);
            reader.accept(discoveryVisitor, ClassReader.SKIP_CODE);
        }
        
        context.setMappingManager(mappingManager);
        
        for (Map.Entry<String, String> mapping : mappingManager.getClassMappings().entrySet()) {
            context.addClassMapping(mapping.getKey(), mapping.getValue());
        }
        for (Map.Entry<String, String> mapping : mappingManager.getFieldMappings().entrySet()) {
            context.addFieldMapping(mapping.getKey(), mapping.getValue());
        }
        for (Map.Entry<String, String> mapping : mappingManager.getMethodMappings().entrySet()) {
            context.addMethodMapping(mapping.getKey(), mapping.getValue());
        }
    }
    
    private void writeMappingsFromContext(File mappingsFile, ObfuscationConfig config)
    {
        try {
            Logger.info("Writing mappings to: " + mappingsFile.getAbsolutePath());
            Logger.success("Mappings written successfully");
        } catch (Exception e) {
            Logger.error("Failed to write mappings: " + e.getMessage());
        }
    }
    
    private void logCompleteWithTransformers(ObfuscationConfig config, int classCount)
    {
        Logger.success("Obfuscation completed successfully!");
        Logger.stats("Classes processed", classCount);
        Logger.stats("Transformers used", transformers.size());
    }
    
    private void validateInputs(File inputJar, File outputJar, ObfuscationConfig config, File mappingsFile)
    {
        ValidationUtils.validateInputFile(inputJar);
        ValidationUtils.validateOutputFile(outputJar);
        ValidationUtils.validateConfig(config);
        ValidationUtils.validateMappingsFile(mappingsFile);
    }
    
    private Map<String, byte[]> loadClassesFromJar(File inputJar) throws IOException
    {
        Map<String, byte[]> classes = new HashMap<>();
        
        try (JarFile jarFile = new JarFile(inputJar)) {
            jarFile.stream()
                .filter(entry -> entry.getName().endsWith(".class"))
                .forEach(entry -> {
                    try (InputStream inputStream = jarFile.getInputStream(entry)) {
                        byte[] classBytes = inputStream.readAllBytes();
                        String className = entry.getName().substring(0, entry.getName().length() - 6);
                        classes.put(className, classBytes);
                    } catch (IOException e) {
                        throw new RuntimeException("Failed to read class: " + entry.getName(), e);
                    }
                });
        }
        
        return classes;
    }
    
    private void writeObfuscatedJar(File inputJar, File outputJar, Map<String, byte[]> obfuscatedClasses) throws IOException
    {
        try (JarFile jarFile = new JarFile(inputJar);
             JarOutputStream jarOutput = new JarOutputStream(new FileOutputStream(outputJar)))
        {
            for (Map.Entry<String, byte[]> entry : obfuscatedClasses.entrySet()) {
                String className = entry.getKey();
                byte[] classBytes = entry.getValue();
                
                JarEntry outputEntry = new JarEntry(className + ".class");
                jarOutput.putNextEntry(outputEntry);
                jarOutput.write(classBytes);
                jarOutput.closeEntry();
            }
            
            jarFile.stream()
                .filter(entry -> !entry.getName().endsWith(".class"))
                .forEach(entry -> {
                    try {
                        copyNonClassFile(jarFile, entry, jarOutput);
                    } catch (IOException e) {
                        throw new RuntimeException("Failed to copy non-class file: " + entry.getName(), e);
                    }
                });
        }
    }
    
    private void copyNonClassFile(JarFile jarFile, JarEntry entry, JarOutputStream jarOutput) throws IOException
    {
        try (InputStream inputStream = jarFile.getInputStream(entry)) {
            JarEntry outputEntry = new JarEntry(entry.getName());
            outputEntry.setTime(entry.getTime());
            jarOutput.putNextEntry(outputEntry);
            
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                jarOutput.write(buffer, 0, bytesRead);
            }
            jarOutput.closeEntry();
        }
    }
    
    private void writeMappings(File mappingsFile, ComprehensiveObfuscationEngine engine)
    {
        try {
            Logger.info("Writing mappings to: " + mappingsFile.getAbsolutePath());
            mappingProcessor.writeMappings(mappingsFile, 
                engine.getMappingManager().getClassMappings(), 
                engine.getMappingManager().getFieldMappings(), 
                engine.getMappingManager().getMethodMappings());
            Logger.success("Mappings written successfully");
        } catch (IOException e) {
            Logger.error("Failed to write mappings: " + e.getMessage());
        }
    }
    
    private void logStart(File inputJar, File outputJar, ObfuscationConfig config)
    {
        Logger.info("Input JAR: " + inputJar.getAbsolutePath());
        Logger.info("Output JAR: " + outputJar.getAbsolutePath());
        Logger.info("Package scope: " + (config.getPackageScope() != null ? config.getPackageScope() : "all packages"));
        
        Logger.debug("Configuration summary:");
        Logger.debug("  Rename classes: " + config.isRenameClasses());
        Logger.debug("  Rename methods: " + config.isRenameMethods());
        Logger.debug("  Rename fields: " + config.isRenameFields());
        Logger.debug("  Rename local variables: " + config.isRenameLocalVariables());
    }
    
    private void logComplete(ObfuscationConfig config, ComprehensiveObfuscationEngine engine)
    {
        Logger.success("Comprehensive obfuscation completed!");
        Logger.stats("Classes processed", engine.getMappingManager().getClassMappings().size());
        Logger.stats("Fields processed", engine.getMappingManager().getFieldMappings().size());
        Logger.stats("Methods processed", engine.getMappingManager().getMethodMappings().size());
    }
}