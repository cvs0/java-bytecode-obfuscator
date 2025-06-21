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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
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
        if (transformer == null) {
            throw new IllegalArgumentException("Transformer cannot be null");
        }
        
        synchronized (transformers) {
            if (transformers.stream().anyMatch(t -> t.getName().equals(transformer.getName()))) {
                Logger.warning("Transformer with name '" + transformer.getName() + "' already registered, skipping duplicate");
                return;
            }
            transformers.add(transformer);
            transformers.sort(Comparator.comparingInt(Transformer::getPriority).reversed());
        }
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
        checkMemoryUsage("before transformation");
        
        ObfuscationContext context = new ObfuscationContext(config);
        this.lastContext = context;
        
        if (inputClasses.isEmpty()) {
            Logger.warning("No classes to transform");
            return new HashMap<>();
        }
        
        Logger.debug("Starting transformation of " + inputClasses.size() + " classes");
        
        try {
            if (config.isRenameClasses() || config.isRenameMethods() || config.isRenameFields()) {
                setupMappingManager(context, inputClasses);
            }
            
            checkMemoryUsage("after mapping setup");
            
            if (config.isSequentialTransformers()) {
                return obfuscateSequentially(inputClasses, config, context);
            }
            
            return obfuscateParallel(inputClasses, config, context);
            
        } catch (OutOfMemoryError e) {
            Logger.error("Out of memory during transformation. Try reducing batch size or increasing heap space.");
            System.gc();
            throw new IOException("Out of memory during transformation", e);
        } catch (Exception e) {
            Logger.error("Unexpected error during transformation: " + e.getMessage());
            throw new IOException("Transformation failed", e);
        }
    }
    
    private Map<String, byte[]> obfuscateParallel(Map<String, byte[]> inputClasses, ObfuscationConfig config, ObfuscationContext context) throws IOException
    {
        Map<String, byte[]> result = new ConcurrentHashMap<>();
        AtomicInteger processedCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);
        
        for (Map.Entry<String, byte[]> entry : inputClasses.entrySet()) {
            String className = entry.getKey();
            byte[] classBytes = entry.getValue();
            
            if (classBytes == null || classBytes.length == 0) {
                Logger.warning("Skipping null or empty class bytes for: " + className);
                errorCount.incrementAndGet();
                continue;
            }
            
            try {
                ClassReader reader = new ClassReader(classBytes);
                String actualClassName = reader.getClassName();
                
                if (actualClassName == null) {
                    Logger.warning("Skipping class with null name: " + className);
                    errorCount.incrementAndGet();
                    continue;
                }
                
                boolean shouldProcess = shouldProcessClass(actualClassName, config);
                if (!shouldProcess) {
                    result.put(className, classBytes);
                    continue;
                }
                
                byte[] transformedBytes = transformClass(reader, context, className);
                String finalClassName = context.getClassMappings().getOrDefault(className, className);
                result.put(finalClassName, transformedBytes);
                
                int processed = processedCount.incrementAndGet();
                if (processed % 50 == 0) {
                    Logger.debug("Processed " + processed + "/" + inputClasses.size() + " classes");
                    checkMemoryUsage("during transformation");
                }
                
            } catch (Exception e) {
                Logger.error("Failed to transform class " + className + ": " + e.getMessage());
                errorCount.incrementAndGet();
                if (context.getConfig().isVerbose()) {
                    e.printStackTrace();
                }
                result.put(className, classBytes);
            }
        }
        
        int errors = errorCount.get();
        if (errors > 0) {
            Logger.warning("Transformation completed with " + errors + " errors");
        }
        
        return result;
    }
    
    private byte[] transformClass(ClassReader reader, ObfuscationContext context, String className) throws Exception
    {
        ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        
        ClassReader currentReader = reader;
        ClassWriter currentWriter = writer;
        
        synchronized (transformers) {
            for (Transformer transformer : transformers) {
                if (transformer.isEnabled(context)) {
                    try {
                        ClassWriter nextWriter = new ClassWriter(currentReader, ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
                        transformer.transform(currentReader, nextWriter, context);
                        
                        byte[] transformedBytes = nextWriter.toByteArray();
                        
                        ClassReader testReader = new ClassReader(transformedBytes);
                        String testClassName = testReader.getClassName();
                        if (testClassName == null) {
                            Logger.error("Transformer " + transformer.getName() + " produced invalid bytecode for class " + className + " - skipping further transformations");
                            break;
                        }
                        currentReader = testReader;
                        currentWriter = nextWriter;
                    } catch (Exception e) {
                        Logger.error("Transformer " + transformer.getName() + " failed for class " + className + ": " + e.getMessage() + " - skipping further transformations");
                        if (context.getConfig().isVerbose()) {
                            e.printStackTrace();
                        }
                        break;
                    }
                }
            }
        }
        
        return currentWriter.toByteArray();
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
                            Logger.error("Transformer " + transformer.getName() + " produced invalid bytecode for class " + className + " - using original");
                            currentResult.put(className, classBytes);
                        } else {
                            String finalClassName = context.getClassMappings().getOrDefault(className, className);
                            currentResult.put(finalClassName, transformedBytes);
                        }
                    } catch (Exception e) {
                        Logger.error("Transformer " + transformer.getName() + " failed for class " + className + ": " + e.getMessage() + " - using original");
                        if (context.getConfig().isVerbose()) {
                            e.printStackTrace();
                        }
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
                Logger.warning("Skipping class during inheritance discovery: " + entry.getKey() + " - " + e.getMessage());
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
                Logger.warning("Skipping class during name collection: " + className + " - " + e.getMessage());
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
                Logger.warning("Skipping class during discovery: " + entry.getKey() + " - " + e.getMessage());
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
        ValidationUtils.validateFileSystemCapacity(inputJar, outputJar);
        
        if (inputJar.equals(outputJar)) {
            throw new IllegalArgumentException("Input and output files cannot be the same: " + inputJar.getAbsolutePath());
        }
        
        try {
            String inputCanonical = inputJar.getCanonicalPath();
            String outputCanonical = outputJar.getCanonicalPath();
            if (inputCanonical.equals(outputCanonical)) {
                throw new IllegalArgumentException("Input and output files resolve to the same path: " + inputCanonical);
            }
        } catch (java.io.IOException e) {
            Logger.warning("Could not resolve canonical paths for comparison: " + e.getMessage());
        }
    }
    
    private Map<String, byte[]> loadClassesFromJar(File inputJar) throws IOException
    {
        Map<String, byte[]> classes = new HashMap<>();
        int totalEntries = 0;
        int processedEntries = 0;
        int skippedEntries = 0;
        
        try (JarFile jarFile = new JarFile(inputJar, true, java.util.zip.ZipFile.OPEN_READ)) {
            java.util.Enumeration<java.util.jar.JarEntry> entries = jarFile.entries();
            
            while (entries.hasMoreElements()) {
                java.util.jar.JarEntry entry = entries.nextElement();
                totalEntries++;
                
                if (!entry.getName().endsWith(".class")) {
                    continue;
                }
                
                if (entry.isDirectory()) {
                    Logger.warning("Skipping directory entry with .class extension: " + entry.getName());
                    skippedEntries++;
                    continue;
                }
                
                if (entry.getSize() > 50 * 1024 * 1024) {
                    Logger.warning("Skipping very large class file (> 50MB): " + entry.getName() + " (" + entry.getSize() + " bytes)");
                    skippedEntries++;
                    continue;
                }
                
                if (entry.getName().contains("../") || entry.getName().startsWith("/")) {
                    Logger.warning("Skipping entry with suspicious path: " + entry.getName());
                    skippedEntries++;
                    continue;
                }
                
                try {
                    processClassEntry(jarFile, entry, classes);
                    processedEntries++;
                } catch (Exception e) {
                    Logger.error("Failed to process class entry " + entry.getName() + ": " + e.getMessage());
                    skippedEntries++;
                    if (e instanceof SecurityException) {
                        throw new IOException("Critical error while processing JAR", e);
                    }
                }
            }
            
            Logger.debug(String.format("JAR processing summary - Total entries: %d, Processed: %d, Skipped: %d", 
                totalEntries, processedEntries, skippedEntries));
                
            if (classes.isEmpty()) {
                throw new IOException("No valid class files found in JAR: " + inputJar.getAbsolutePath());
            }
        } catch (java.util.zip.ZipException e) {
            throw new IOException("Corrupted or invalid JAR file: " + inputJar.getAbsolutePath(), e);
        } catch (SecurityException e) {
            throw new IOException("Security restriction while reading JAR: " + inputJar.getAbsolutePath(), e);
        }
        
        return classes;
    }
    
    private void processClassEntry(JarFile jarFile, java.util.jar.JarEntry entry, Map<String, byte[]> classes) throws IOException
    {
        try (InputStream inputStream = jarFile.getInputStream(entry)) {
            if (inputStream == null) {
                Logger.warning("Skipping entry with null input stream: " + entry.getName());
                return;
            }
            
            byte[] buffer = new byte[8192];
            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            int bytesRead;
            long totalBytesRead = 0;
            long maxSize = 50 * 1024 * 1024;
            
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                totalBytesRead += bytesRead;
                if (totalBytesRead > maxSize) {
                    throw new IOException("Class file too large: " + entry.getName());
                }
                baos.write(buffer, 0, bytesRead);
            }
            
            byte[] classBytes = baos.toByteArray();
            if (classBytes.length == 0) {
                Logger.warning("Skipping empty class file: " + entry.getName());
                return;
            }
            
            if (classBytes.length < 8) {
                Logger.warning("Skipping suspiciously small class file: " + entry.getName() + " (" + classBytes.length + " bytes)");
                return;
            }
            
            if (!isValidClassFile(classBytes)) {
                Logger.warning("Skipping invalid class file (bad magic number): " + entry.getName());
                return;
            }
            
            try {
                ClassReader testReader = new ClassReader(classBytes);
                String className = testReader.getClassName();
                
                if (className == null || className.isEmpty()) {
                    Logger.warning("Skipping class with null or empty name: " + entry.getName());
                    return;
                }
                
                if (className.length() > 1000) {
                    Logger.warning("Skipping class with suspiciously long name: " + entry.getName() + " (length: " + className.length() + ")");
                    return;
                }
                
                if (classes.containsKey(className)) {
                    Logger.warning("Duplicate class found, using first occurrence: " + className);
                    return;
                }
                
                classes.put(className, classBytes);
                Logger.debug("Loaded class: " + className + " (" + classBytes.length + " bytes)");
                
            } catch (IllegalArgumentException e) {
                Logger.warning("Skipping class with invalid bytecode structure " + entry.getName() + ": " + e.getMessage());
            } catch (ArrayIndexOutOfBoundsException e) {
                Logger.warning("Skipping class with corrupted bytecode " + entry.getName() + ": " + e.getMessage());
            }
        }
    }
    
    private boolean isValidClassFile(byte[] classBytes) {
        if (classBytes.length < 4) {
            return false;
        }
        
        int magic = (classBytes[0] & 0xFF) << 24 |
                   (classBytes[1] & 0xFF) << 16 |
                   (classBytes[2] & 0xFF) << 8 |
                   (classBytes[3] & 0xFF);
        
        return magic == 0xCAFEBABE;
    }
    
    private void writeObfuscatedJar(File inputJar, File outputJar, Map<String, byte[]> obfuscatedClasses) throws IOException
    {
        File tempFile = null;
        try {
            ValidationUtils.validateFileSystemCapacity(inputJar, outputJar);
            
            tempFile = new File(outputJar.getAbsolutePath() + ".tmp");
            if (tempFile.exists() && !tempFile.delete()) {
                Logger.warning("Could not delete existing temp file: " + tempFile.getAbsolutePath());
            }
            
            try (JarFile jarFile = new JarFile(inputJar);
                 JarOutputStream jarOutput = new JarOutputStream(
                     new java.io.BufferedOutputStream(new FileOutputStream(tempFile), 65536)))
            {
                jarOutput.setLevel(java.util.zip.Deflater.BEST_COMPRESSION);
                
                Set<String> addedEntries = new HashSet<>();
                int classesWritten = 0;
                int resourcesWritten = 0;
                
                for (Map.Entry<String, byte[]> entry : obfuscatedClasses.entrySet()) {
                    String className = entry.getKey();
                    byte[] classBytes = entry.getValue();
                    
                    if (classBytes == null || classBytes.length == 0) {
                        Logger.warning("Skipping null or empty class: " + className);
                        continue;
                    }
                    
                    String entryName = className + ".class";
                    if (addedEntries.contains(entryName)) {
                        Logger.warning("Duplicate entry skipped: " + entryName);
                        continue;
                    }
                    
                    try {
                        JarEntry outputEntry = new JarEntry(entryName);
                        outputEntry.setTime(System.currentTimeMillis());
                        jarOutput.putNextEntry(outputEntry);
                        jarOutput.write(classBytes);
                        jarOutput.closeEntry();
                        addedEntries.add(entryName);
                        classesWritten++;
                        
                        if (classesWritten % 100 == 0) {
                            Logger.debug("Written " + classesWritten + " classes");
                        }
                    } catch (java.util.zip.ZipException e) {
                        Logger.error("Failed to write class " + className + ": " + e.getMessage());
                        throw new IOException("Failed to write class to JAR", e);
                    }
                }
                
                java.util.Enumeration<java.util.jar.JarEntry> entries = jarFile.entries();
                while (entries.hasMoreElements()) {
                    java.util.jar.JarEntry entry = entries.nextElement();
                    
                    if (entry.getName().endsWith(".class") || addedEntries.contains(entry.getName())) {
                        continue;
                    }
                    
                    if (entry.getName().startsWith("META-INF/") && 
                        (entry.getName().endsWith(".SF") || entry.getName().endsWith(".DSA") || entry.getName().endsWith(".RSA"))) {
                        Logger.debug("Skipping signature file: " + entry.getName());
                        continue;
                    }
                    
                    try {
                        copyNonClassFile(jarFile, entry, jarOutput, addedEntries);
                        resourcesWritten++;
                    } catch (IOException e) {
                        Logger.error("Failed to copy resource " + entry.getName() + ": " + e.getMessage());
                        if (entry.getName().equals("META-INF/MANIFEST.MF")) {
                            throw new IOException("Failed to copy critical manifest file", e);
                        }
                    }
                }
                
                Logger.debug(String.format("JAR writing completed - Classes: %d, Resources: %d", classesWritten, resourcesWritten));
            }
            
            if (!tempFile.renameTo(outputJar)) {
                java.nio.file.Files.move(tempFile.toPath(), outputJar.toPath(), 
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }
            
        } catch (IOException e) {
            if (tempFile != null && tempFile.exists()) {
                if (!tempFile.delete()) {
                    Logger.warning("Could not clean up temp file: " + tempFile.getAbsolutePath());
                }
            }
            throw e;
        } catch (Exception e) {
            if (tempFile != null && tempFile.exists()) {
                if (!tempFile.delete()) {
                    Logger.warning("Could not clean up temp file: " + tempFile.getAbsolutePath());
                }
            }
            throw new IOException("Unexpected error during JAR writing", e);
        }
    }
    
    private void copyNonClassFile(JarFile jarFile, JarEntry entry, JarOutputStream jarOutput, Set<String> addedEntries) throws IOException
    {
        if (addedEntries.contains(entry.getName())) {
            Logger.debug("Skipping duplicate entry: " + entry.getName());
            return;
        }
        
        if (entry.getSize() > 100 * 1024 * 1024) {
            Logger.warning("Skipping very large resource file: " + entry.getName() + " (" + entry.getSize() + " bytes)");
            return;
        }
        
        try (InputStream inputStream = jarFile.getInputStream(entry)) {
            if (inputStream == null) {
                Logger.warning("Skipping entry with null input stream: " + entry.getName());
                return;
            }
            
            JarEntry outputEntry = new JarEntry(entry.getName());
            if (entry.getTime() != -1) {
                outputEntry.setTime(entry.getTime());
            } else {
                outputEntry.setTime(System.currentTimeMillis());
            }
            
            if (entry.getComment() != null) {
                outputEntry.setComment(entry.getComment());
            }
            
            jarOutput.putNextEntry(outputEntry);
            
            byte[] buffer = new byte[8192];
            int bytesRead;
            long totalBytesRead = 0;
            long maxSize = 100 * 1024 * 1024;
            
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                totalBytesRead += bytesRead;
                if (totalBytesRead > maxSize) {
                    Logger.warning("Resource file too large, truncating: " + entry.getName());
                    break;
                }
                jarOutput.write(buffer, 0, bytesRead);
            }
            
            jarOutput.closeEntry();
            addedEntries.add(entry.getName());
            
        } catch (java.util.zip.ZipException e) {
            Logger.warning("Failed to copy resource due to ZIP error: " + entry.getName() + " - " + e.getMessage());
        } catch (IOException e) {
            Logger.warning("Failed to copy resource: " + entry.getName() + " - " + e.getMessage());
            throw e;
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
    
    private void checkMemoryUsage(String phase) {
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        long maxMemory = runtime.maxMemory();
        
        double usedPercent = (double) usedMemory / maxMemory * 100;
        
        Logger.debug(String.format("Memory usage %s: %.1f%% (%d MB / %d MB)", 
            phase, usedPercent, usedMemory / (1024 * 1024), maxMemory / (1024 * 1024)));
        
        if (usedPercent > 85) {
            Logger.warning("High memory usage detected (" + String.format("%.1f%%", usedPercent) + "), suggesting garbage collection");
            System.gc();
            
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            long newUsedMemory = runtime.totalMemory() - runtime.freeMemory();
            double newUsedPercent = (double) newUsedMemory / maxMemory * 100;
            Logger.debug(String.format("Memory usage after GC: %.1f%%", newUsedPercent));
        }
    }
}