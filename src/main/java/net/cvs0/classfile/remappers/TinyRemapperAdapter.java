package net.cvs0.classfile.remappers;

import net.cvs0.config.ObfuscationConfig;
import net.cvs0.obfuscation.MappingContext;
import net.cvs0.utils.Logger;
import net.fabricmc.tinyremapper.TinyRemapper;
import net.fabricmc.tinyremapper.IMappingProvider;
import net.fabricmc.tinyremapper.OutputConsumerPath;
import net.fabricmc.tinyremapper.NonClassCopyMode;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

public class TinyRemapperAdapter 
{
    private final ObfuscationConfig config;
    
    public TinyRemapperAdapter(ObfuscationConfig config) 
    {
        this.config = config;
    }
    public void remap(File inputJar, File outputJar, MappingContext mappingContext, 
                     Map<String, byte[]> resources, Map<String, String> manifestAttributes) throws IOException 
    {
        if (config.isVerbose()) {
            Logger.info("Starting JAR remapping with TinyRemapper...");
        }

        Path inputPath = inputJar.toPath();
        Path outputPath = outputJar.toPath();

        TinyRemapper.Builder builder = TinyRemapper.newRemapper();
        builder.withMappings(createMappingProvider(mappingContext));
        
        TinyRemapper remapper = builder.build();
        
        try {
            try (OutputConsumerPath outputConsumer = new OutputConsumerPath.Builder(outputPath).build()) {
                outputConsumer.addNonClassFiles(inputPath, NonClassCopyMode.FIX_META_INF, remapper);
                remapper.readInputs(inputPath);
                remapper.apply(outputConsumer);
            }
            
            // Remove original classes that were mapped to avoid duplication
            removeOriginalMappedClasses(outputPath, mappingContext);
            
            updateManifestInJar(outputPath, mappingContext, manifestAttributes);
            
            addResourcesToJar(outputPath, resources);
        } finally {
            remapper.finish();
        }

        if (config.isVerbose()) {
            Logger.info("JAR remapping completed successfully");
        }
    }

    private IMappingProvider createMappingProvider(MappingContext mappingContext) 
    {
        return out -> {
            for (Map.Entry<String, String> entry : mappingContext.getAllClassMappings().entrySet()) {
                out.acceptClass(entry.getKey(), entry.getValue());
            }
            
            for (Map.Entry<String, String> entry : mappingContext.getAllFieldMappings().entrySet()) {
                String key = entry.getKey();
                int dotIndex = key.indexOf('.');
                int colonIndex = key.indexOf(':', dotIndex);
                if (dotIndex > 0 && colonIndex > dotIndex) {
                    String owner = key.substring(0, dotIndex);
                    String name = key.substring(dotIndex + 1, colonIndex);
                    String desc = key.substring(colonIndex + 1);
                    out.acceptField(new IMappingProvider.Member(owner, name, desc), entry.getValue());
                }
            }
            
            for (Map.Entry<String, String> entry : mappingContext.getAllMethodMappings().entrySet()) {
                String key = entry.getKey();
                int dotIndex = key.indexOf('.');
                int parenIndex = key.indexOf('(', dotIndex);
                if (dotIndex > 0 && parenIndex > dotIndex) {
                    String owner = key.substring(0, dotIndex);
                    String name = key.substring(dotIndex + 1, parenIndex);
                    String desc = key.substring(parenIndex);
                    out.acceptMethod(new IMappingProvider.Member(owner, name, desc), entry.getValue());
                }
            }
        };
    }

    private void updateManifest(Manifest manifest, MappingContext mappingContext, Map<String, String> manifestAttributes) 
    {
        if (manifestAttributes.containsKey("Main-Class")) {
            String mainClass = manifestAttributes.get("Main-Class");
            String internalMainClass = mainClass.replace('.', '/');
            String mappedMainClass = mappingContext.getObfuscatedClassName(internalMainClass);
            if (!mappedMainClass.equals(internalMainClass)) {
                String dottedMappedMainClass = mappedMainClass.replace('/', '.');
                manifest.getMainAttributes().putValue("Main-Class", dottedMappedMainClass);
                
                if (config.isVerbose()) {
                    Logger.info("Updated Main-Class in manifest: " + mainClass + " -> " + dottedMappedMainClass);
                }
            }
        }

        for (Map.Entry<String, String> entry : manifestAttributes.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (!key.equals("Main-Class")) {
                manifest.getMainAttributes().putValue(key, value);
            }
        }
    }

    private void updateManifestInJar(Path jarPath, MappingContext mappingContext, Map<String, String> manifestAttributes) throws IOException 
    {
        Path tempJar = Files.createTempFile("temp-", ".jar");
        
        try (JarInputStream inputStream = new JarInputStream(Files.newInputStream(jarPath));
             JarOutputStream outputStream = new JarOutputStream(Files.newOutputStream(tempJar))) {
            
            Manifest manifest = inputStream.getManifest();
            if (manifest != null) {
                updateManifest(manifest, mappingContext, manifestAttributes);
                outputStream.putNextEntry(new JarEntry("META-INF/MANIFEST.MF"));
                manifest.write(outputStream);
                outputStream.closeEntry();
            }
            
            JarEntry entry;
            while ((entry = inputStream.getNextJarEntry()) != null) {
                if (entry.getName().equals("META-INF/MANIFEST.MF")) {
                    continue;
                }
                
                outputStream.putNextEntry(new JarEntry(entry.getName()));
                inputStream.transferTo(outputStream);
                outputStream.closeEntry();
            }
        }
        
        Files.move(tempJar, jarPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
    }
    
    private void addResourcesToJar(Path jarPath, Map<String, byte[]> resources) throws IOException 
    {
        if (resources.isEmpty()) {
            return;
        }
        
        Path tempJar = Files.createTempFile("temp-", ".jar");
        
        try (JarInputStream inputStream = new JarInputStream(Files.newInputStream(jarPath));
             JarOutputStream outputStream = new JarOutputStream(Files.newOutputStream(tempJar))) {
            
            Set<String> existingEntries = new HashSet<>();
            
            Manifest manifest = inputStream.getManifest();
            if (manifest != null) {
                outputStream.putNextEntry(new JarEntry("META-INF/MANIFEST.MF"));
                manifest.write(outputStream);
                outputStream.closeEntry();
                existingEntries.add("META-INF/MANIFEST.MF");
            }
            
            JarEntry entry;
            while ((entry = inputStream.getNextJarEntry()) != null) {
                if (entry.getName().equals("META-INF/MANIFEST.MF")) {
                    continue;
                }
                
                outputStream.putNextEntry(new JarEntry(entry.getName()));
                inputStream.transferTo(outputStream);
                outputStream.closeEntry();
                existingEntries.add(entry.getName());
            }
            
            for (Map.Entry<String, byte[]> resourceEntry : resources.entrySet()) {
                String resourceName = resourceEntry.getKey();
                byte[] resourceData = resourceEntry.getValue();

                if (resourceName.equals("META-INF/MANIFEST.MF") || resourceName.endsWith(".class")) {
                    continue;
                }

                if (existingEntries.contains(resourceName)) {
                    if (config.isVerbose()) {
                        Logger.debug("Skipping duplicate resource: " + resourceName);
                    }
                    continue;
                }

                outputStream.putNextEntry(new JarEntry(resourceName));
                outputStream.write(resourceData);
                outputStream.closeEntry();
                existingEntries.add(resourceName);

                if (config.isVerbose()) {
                    Logger.debug("Added resource: " + resourceName);
                }
            }
        }
        
        Files.move(tempJar, jarPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
    }
    
    private void removeOriginalMappedClasses(Path jarPath, MappingContext mappingContext) throws IOException 
    {
        if (mappingContext.getAllClassMappings().isEmpty()) {
            return;
        }
        
        Path tempJar = Files.createTempFile("obfuscated-temp", ".jar");
        Set<String> classesToRemove = new HashSet<>();
        
        // Build set of original class names that were mapped (these should be removed)
        for (String originalClass : mappingContext.getAllClassMappings().keySet()) {
            classesToRemove.add(originalClass + ".class");
        }
        
        if (config.isVerbose()) {
            Logger.debug("Removing " + classesToRemove.size() + " original mapped classes to prevent duplication");
        }
        
        try (JarInputStream inputStream = new JarInputStream(Files.newInputStream(jarPath))) {
            Manifest manifest = inputStream.getManifest();
            
            try (JarOutputStream outputStream = new JarOutputStream(Files.newOutputStream(tempJar), manifest)) {
                JarEntry entry;
                while ((entry = inputStream.getNextJarEntry()) != null) {
                    String entryName = entry.getName();
                    
                    // Skip original classes that were mapped
                    if (classesToRemove.contains(entryName)) {
                        if (config.isVerbose()) {
                            Logger.debug("Removed original mapped class: " + entryName);
                        }
                        continue;
                    }
                    
                    outputStream.putNextEntry(new JarEntry(entry.getName()));
                    inputStream.transferTo(outputStream);
                    outputStream.closeEntry();
                }
            }
        }
        
        Files.move(tempJar, jarPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
    }
}