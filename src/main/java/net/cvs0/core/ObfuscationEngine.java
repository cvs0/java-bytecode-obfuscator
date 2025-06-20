package net.cvs0.core;

import net.cvs0.analysis.ObfuscationScorer;
import net.cvs0.config.ObfuscationConfig;
import net.cvs0.context.ObfuscationContext;
import net.cvs0.mappings.MappingProcessor;
import net.cvs0.mappings.InheritanceTracker;
import net.cvs0.transformers.AntiDebuggingTransformer;
import net.cvs0.utils.ValidationUtils;
import net.cvs0.utils.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import java.io.*;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import net.cvs0.mappings.export.MappingExporter;
import net.cvs0.mappings.export.MappingData;

public class ObfuscationEngine
{
    private final ComprehensiveObfuscationEngine comprehensiveEngine;
    private final MappingProcessor mappingProcessor;
    private final List<Transformer> transformers;
    private ObfuscationContext lastContext;
    
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
    
    public void obfuscate(File inputJar, File outputJar, ObfuscationConfig config, File mappingsFile, MappingExporter.MappingFormat format) throws IOException
    {
        Logger.setVerbose(config.isVerbose());
        
        validateInputs(inputJar, outputJar, config, mappingsFile);
        
        Logger.phase("Bytecode Obfuscation Process");
        logStart(inputJar, outputJar, config);
        
        Logger.step("Loading classes from JAR file");
        Map<String, byte[]> inputClasses = loadClassesFromJar(inputJar);
        Logger.success(String.format("Loaded %d classes from input JAR", inputClasses.size()));
        
        Map<String, byte[]> obfuscatedClasses;
        ComprehensiveObfuscationEngine comprehensiveEngine = null;
        
        if (!transformers.isEmpty()) {
            Logger.step("Starting transformer-based obfuscation");
            obfuscatedClasses = obfuscateWithTransformers(inputClasses, config);
        } else {
            Logger.step("Starting comprehensive obfuscation");
            comprehensiveEngine = new ComprehensiveObfuscationEngine(config);
            obfuscatedClasses = comprehensiveEngine.obfuscate(inputClasses);
        }
        
        Logger.step("Writing obfuscated JAR file");
        writeObfuscatedJar(inputJar, outputJar, obfuscatedClasses);
        Logger.success("Obfuscated JAR file written successfully");
        
        if (mappingsFile != null) {
            Logger.step("Writing mappings file with format: " + format);
            writeMappingsFromContextWithFormat(mappingsFile, config, format, comprehensiveEngine);
        }
        
        if (config.isGenerateScore()) {
            Logger.step("Generating obfuscation resistance score");
            generateObfuscationScore(config, lastContext);
        }
        
        logCompleteWithTransformers(config, obfuscatedClasses.size());
    }
    
    private Map<String, byte[]> obfuscateWithTransformers(Map<String, byte[]> inputClasses, ObfuscationConfig config) throws IOException
    {
        ObfuscationContext context = new ObfuscationContext(config);
        this.lastContext = context;
        
        if (config.isRenameClasses() || config.isRenameMethods() || config.isRenameFields()) {
            setupMappingManager(context, inputClasses);
        }
        
        if (config.isSequentialTransformers()) {
            return obfuscateSequentially(inputClasses, config, context);
        }
        
        Map<String, byte[]> result = new HashMap<>();
        
        for (Map.Entry<String, byte[]> entry : inputClasses.entrySet()) {
            String className = entry.getKey();
            byte[] classBytes = entry.getValue();
            
            ClassReader reader = new ClassReader(classBytes);
            String actualClassName = reader.getClassName();
            
            boolean shouldProcess = shouldProcessClass(actualClassName, config);
            if (!shouldProcess) {
                result.put(className, classBytes);
                continue;
            }
            
            ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
            
            ClassReader currentReader = reader;
            ClassWriter currentWriter = writer;
            
            for (Transformer transformer : transformers) {
                if (transformer.isEnabled(context)) {
                    ClassWriter nextWriter = new ClassWriter(currentReader, ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
                    transformer.transform(currentReader, nextWriter, context);
                    
                    byte[] transformedBytes = nextWriter.toByteArray();
                    
                    try {
                        ClassReader testReader = new ClassReader(transformedBytes);
                        String testClassName = testReader.getClassName();
                        if (testClassName == null) {
                            System.err.println("Transformer " + transformer.getName() + " produced invalid bytecode for class " + className + " - skipping further transformations");
                            break;
                        }
                        currentReader = testReader;
                        currentWriter = nextWriter;
                    } catch (Exception e) {
                        System.err.println("Transformer " + transformer.getName() + " produced malformed bytecode for class " + className + ": " + e.getMessage() + " - skipping further transformations");
                        break;
                    }
                }
            }
            
            String finalClassName = context.getClassMappings().getOrDefault(className, className);
            result.put(finalClassName, currentWriter.toByteArray());
        }
        
        return result;
    }
    
    private Map<String, byte[]> obfuscateSequentially(Map<String, byte[]> inputClasses, ObfuscationConfig config, ObfuscationContext context) throws IOException
    {
        Map<String, byte[]> result = new HashMap<>(inputClasses);
        
        for (Transformer transformer : transformers) {
            if (transformer.isEnabled(context)) {
                Map<String, byte[]> currentResult = new HashMap<>();
                
                for (Map.Entry<String, byte[]> entry : result.entrySet()) {
                    String className = entry.getKey();
                    byte[] classBytes = entry.getValue();
                    
                    ClassReader reader = new ClassReader(classBytes);
                    String actualClassName = reader.getClassName();
                    
                    boolean shouldProcess = shouldProcessClass(actualClassName, config);
                    if (!shouldProcess) {
                        currentResult.put(className, classBytes);
                        continue;
                    }
                    
                    ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
                    
                    try {
                        transformer.transform(reader, writer, context);
                        byte[] transformedBytes = writer.toByteArray();
                        
                        ClassReader testReader = new ClassReader(transformedBytes);
                        String testClassName = testReader.getClassName();
                        if (testClassName == null) {
                            System.err.println("Transformer " + transformer.getName() + " produced invalid bytecode for class " + className + " - using original");
                            currentResult.put(className, classBytes);
                        } else {
                            String finalClassName = context.getClassMappings().getOrDefault(className, className);
                            currentResult.put(finalClassName, transformedBytes);
                        }
                    } catch (Exception e) {
                        System.err.println("Transformer " + transformer.getName() + " failed for class " + className + ": " + e.getMessage() + " - using original");
                        currentResult.put(className, classBytes);
                    }
                }
                
                result = currentResult;
            }
        }
        
        return result;
    }
    
    private void setupMappingManager(ObfuscationContext context, Map<String, byte[]> inputClasses)
    {
        net.cvs0.mappings.MappingManager mappingManager = new net.cvs0.mappings.MappingManager(context.getConfig());
        InheritanceTracker inheritanceTracker = new InheritanceTracker();
        mappingManager.setInheritanceTracker(inheritanceTracker);
        
        for (Map.Entry<String, byte[]> entry : inputClasses.entrySet()) {
            try {
                ClassReader reader = new ClassReader(entry.getValue());
                String className = reader.getClassName();
                if (className != null && shouldProcessClass(className, context.getConfig())) {
                    net.cvs0.discovery.InheritanceDiscoveryVisitor inheritanceVisitor = new net.cvs0.discovery.InheritanceDiscoveryVisitor(inheritanceTracker);
                    reader.accept(inheritanceVisitor, ClassReader.SKIP_CODE);
                }
            } catch (Exception e) {
                System.err.println("Skipping class during inheritance discovery: " + entry.getKey() + " - " + e.getMessage());
            }
        }
        
        Set<String> classNames = new HashSet<>();
        for (String className : inputClasses.keySet()) {
            try {
                ClassReader reader = new ClassReader(inputClasses.get(className));
                String readerClassName = reader.getClassName();
                if (readerClassName != null && shouldProcessClass(readerClassName, context.getConfig())) {
                    classNames.add(readerClassName);
                }
            } catch (Exception e) {
                System.err.println("Skipping class during name collection: " + className + " - " + e.getMessage());
            }
        }
        
        mappingManager.generateClassMappings(classNames);
        
        for (Map.Entry<String, byte[]> entry : inputClasses.entrySet()) {
            try {
                ClassReader reader = new ClassReader(entry.getValue());
                String className = reader.getClassName();
                if (className != null && shouldProcessClass(className, context.getConfig())) {
                    net.cvs0.discovery.ClassDiscoveryVisitor discoveryVisitor = new net.cvs0.discovery.ClassDiscoveryVisitor(mappingManager, inheritanceTracker);
                    reader.accept(discoveryVisitor, ClassReader.SKIP_CODE);
                }
            } catch (Exception e) {
                System.err.println("Skipping class during discovery: " + entry.getKey() + " - " + e.getMessage());
            }
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
    
    private void writeMappingsFromContextWithFormat(File mappingsFile, ObfuscationConfig config, MappingExporter.MappingFormat format, ComprehensiveObfuscationEngine comprehensiveEngine)
    {
        try {
            if (format == null) {
                format = detectFormatFromExtension(mappingsFile);
            }
            
            MappingData mappingData = collectMappingsFromContext(config, comprehensiveEngine);
            
            if (mappingData.isEmpty()) {
                Logger.warning("No mappings to export");
                return;
            }
            
            Logger.info("Writing mappings to: " + mappingsFile.getAbsolutePath() + " in format: " + format);
            
            MappingExporter exporter = new MappingExporter(mappingData);
            exporter.exportToFile(mappingsFile, format);
            
            Logger.success("Mappings written successfully (" + mappingData.getTotalMappings() + " total mappings)");
        } catch (Exception e) {
            Logger.error("Failed to write mappings: " + e.getMessage());
            if (config.isVerbose()) {
                e.printStackTrace();
            }
        }
    }
    
    private MappingExporter.MappingFormat detectFormatFromExtension(File mappingsFile)
    {
        String fileName = mappingsFile.getName().toLowerCase();
        
        if (fileName.endsWith(".json")) {
            return MappingExporter.MappingFormat.JSON;
        } else if (fileName.endsWith(".csv")) {
            return MappingExporter.MappingFormat.CSV;
        } else if (fileName.endsWith(".srg")) {
            return MappingExporter.MappingFormat.SRG;
        } else if (fileName.endsWith(".tiny")) {
            return MappingExporter.MappingFormat.TINY;
        } else if (fileName.contains("retrace") || fileName.contains("stacktrace")) {
            return MappingExporter.MappingFormat.RETRACE;
        } else if (fileName.contains("human") || fileName.contains("readable")) {
            return MappingExporter.MappingFormat.HUMAN_READABLE;
        } else {
            return MappingExporter.MappingFormat.PROGUARD;
        }
    }
    
    private MappingData collectMappingsFromContext(ObfuscationConfig config, ComprehensiveObfuscationEngine comprehensiveEngine)
    {
        MappingData mappingData = new MappingData();
        
        if (comprehensiveEngine != null && comprehensiveEngine.getMappingManager() != null) {
            net.cvs0.mappings.MappingManager mappingManager = comprehensiveEngine.getMappingManager();
            
            for (Map.Entry<String, String> entry : mappingManager.getClassMappings().entrySet()) {
                mappingData.addClassMapping(entry.getKey(), entry.getValue());
            }
            
            for (Map.Entry<String, String> entry : mappingManager.getFieldMappings().entrySet()) {
                mappingData.addFieldMapping(entry.getKey(), entry.getValue());
            }
            
            for (Map.Entry<String, String> entry : mappingManager.getMethodMappings().entrySet()) {
                mappingData.addMethodMapping(entry.getKey(), entry.getValue());
            }
        } else if (lastContext != null) {
            Map<String, String> classMappings = lastContext.getClassMappings();
            Map<String, String> fieldMappings = lastContext.getFieldMappings();
            Map<String, String> methodMappings = lastContext.getMethodMappings();
            
            for (Map.Entry<String, String> entry : classMappings.entrySet()) {
                mappingData.addClassMapping(entry.getKey(), entry.getValue());
            }
            
            for (Map.Entry<String, String> entry : fieldMappings.entrySet()) {
                mappingData.addFieldMapping(entry.getKey(), entry.getValue());
            }
            
            for (Map.Entry<String, String> entry : methodMappings.entrySet()) {
                mappingData.addMethodMapping(entry.getKey(), entry.getValue());
            }
        }
        
        mappingData.addMetadata("obfuscator", "Java Bytecode Obfuscator");
        mappingData.addMetadata("version", "1.0.0");
        mappingData.addMetadata("timestamp", java.time.LocalDateTime.now().toString());
        mappingData.addMetadata("config", config.toString());
        
        return mappingData;
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
                        
                        try {
                            ClassReader testReader = new ClassReader(classBytes);
                            String className = testReader.getClassName();
                            if (className != null && !className.isEmpty()) {
                                classes.put(className, classBytes);
                            }
                        } catch (Exception e) {
                            System.err.println("Skipping invalid class file: " + entry.getName() + " - " + e.getMessage());
                        }
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
    
    private void generateObfuscationScore(ObfuscationConfig config, ObfuscationContext context)
    {
        try {
            ObfuscationScorer scorer = new ObfuscationScorer(config, context);
            ObfuscationScorer.ObfuscationScore score = scorer.calculateScore();
            
            Logger.info("\n" + score.toString());
            
        } catch (Exception e) {
            Logger.error("Failed to generate obfuscation score: " + e.getMessage());
        }
    }
    
    private boolean shouldProcessClass(String className, ObfuscationConfig config)
    {
        if (className == null) {
            return false;
        }
        
        if (config.getPackageScope() != null && !config.getPackageScope().isEmpty()) {
            return className.startsWith(config.getPackageScope().replace('.', '/'));
        }
        
        return true;
    }
}