package net.cvs0.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class ConfigLoader 
{
    private final ObjectMapper objectMapper;

    public ConfigLoader() 
    {
        this.objectMapper = new ObjectMapper();
    }

    public ObfuscationConfig.Builder loadConfig(File configFile) throws IOException 
    {
        JsonNode root = objectMapper.readTree(configFile);
        ObfuscationConfig.Builder builder = new ObfuscationConfig.Builder();

        if (root.has("renameClasses")) {
            builder.renameClasses(root.get("renameClasses").asBoolean());
        }
        
        if (root.has("renameFields")) {
            builder.renameFields(root.get("renameFields").asBoolean());
        }
        
        if (root.has("renameMethods")) {
            builder.renameMethods(root.get("renameMethods").asBoolean());
        }
        
        if (root.has("renameLocalVariables")) {
            builder.renameLocalVariables(root.get("renameLocalVariables").asBoolean());
        }
        
        if (root.has("stripDebugInfo")) {
            builder.stripDebugInfo(root.get("stripDebugInfo").asBoolean());
        }
        
        if (root.has("obfuscateControlFlow")) {
            builder.obfuscateControlFlow(root.get("obfuscateControlFlow").asBoolean());
        }
        
        if (root.has("enableBackup")) {
            builder.enableBackup(root.get("enableBackup").asBoolean());
        }
        
        if (root.has("verbose")) {
            builder.verbose(root.get("verbose").asBoolean());
        }
        
        if (root.has("keepMainClass")) {
            builder.keepMainClass(root.get("keepMainClass").asBoolean());
        }
        
        if (root.has("keepStandardEntryPoints")) {
            builder.keepStandardEntryPoints(root.get("keepStandardEntryPoints").asBoolean());
        }
        
        if (root.has("sequentialTransformers")) {
            builder.sequentialTransformers(root.get("sequentialTransformers").asBoolean());
        }

        if (root.has("mainClass")) {
            builder.mainClass(root.get("mainClass").asText());
        }
        
        if (root.has("backupDir")) {
            builder.backupDir(root.get("backupDir").asText());
        }
        
        if (root.has("namingMode")) {
            try {
                NamingMode mode = NamingMode.valueOf(root.get("namingMode").asText().toUpperCase());
                builder.namingMode(mode);
            } catch (IllegalArgumentException e) {
                throw new IOException("Invalid naming mode: " + root.get("namingMode").asText());
            }
        }
        
        if (root.has("obfuscationLevel")) {
            try {
                ObfuscationLevel level = ObfuscationLevel.valueOf(root.get("obfuscationLevel").asText().toUpperCase());
                builder.obfuscationLevel(level);
            } catch (IllegalArgumentException e) {
                throw new IOException("Invalid obfuscation level: " + root.get("obfuscationLevel").asText());
            }
        }
        
        if (root.has("maxThreads")) {
            builder.maxThreads(root.get("maxThreads").asInt());
        }

        if (root.has("keepClasses")) {
            JsonNode keepClasses = root.get("keepClasses");
            if (keepClasses.isArray()) {
                List<String> classes = new ArrayList<>();
                for (JsonNode node : keepClasses) {
                    classes.add(node.asText());
                }
                builder.keepClasses(classes);
            }
        }
        
        if (root.has("keepClassPatterns")) {
            JsonNode keepClassPatterns = root.get("keepClassPatterns");
            if (keepClassPatterns.isArray()) {
                List<String> patterns = new ArrayList<>();
                for (JsonNode node : keepClassPatterns) {
                    patterns.add(node.asText());
                }
                builder.keepClassPatterns(patterns);
            }
        }
        
        if (root.has("keepMethods")) {
            JsonNode keepMethods = root.get("keepMethods");
            if (keepMethods.isArray()) {
                List<String> methods = new ArrayList<>();
                for (JsonNode node : keepMethods) {
                    methods.add(node.asText());
                }
                builder.keepMethods(methods);
            }
        }
        
        if (root.has("keepFields")) {
            JsonNode keepFields = root.get("keepFields");
            if (keepFields.isArray()) {
                List<String> fields = new ArrayList<>();
                for (JsonNode node : keepFields) {
                    fields.add(node.asText());
                }
                builder.keepFields(fields);
            }
        }
        
        if (root.has("includePackages")) {
            JsonNode includePackages = root.get("includePackages");
            if (includePackages.isArray()) {
                List<String> packages = new ArrayList<>();
                for (JsonNode node : includePackages) {
                    packages.add(node.asText());
                }
                builder.includePackages(packages);
            }
        }
        
        if (root.has("excludePackages")) {
            JsonNode excludePackages = root.get("excludePackages");
            if (excludePackages.isArray()) {
                List<String> packages = new ArrayList<>();
                for (JsonNode node : excludePackages) {
                    packages.add(node.asText());
                }
                builder.excludePackages(packages);
            }
        }

        if (root.has("customMappings")) {
            JsonNode customMappings = root.get("customMappings");
            if (customMappings.isObject()) {
                Map<String, String> mappings = new HashMap<>();
                customMappings.fields().forEachRemaining(entry -> {
                    mappings.put(entry.getKey(), entry.getValue().asText());
                });
                builder.customMappings(mappings);
            }
        }
        
        if (root.has("transformerOptions")) {
            JsonNode transformerOptions = root.get("transformerOptions");
            if (transformerOptions.isObject()) {
                Map<String, Object> options = new HashMap<>();
                transformerOptions.fields().forEachRemaining(entry -> {
                    JsonNode value = entry.getValue();
                    if (value.isBoolean()) {
                        options.put(entry.getKey(), value.asBoolean());
                    } else if (value.isInt()) {
                        options.put(entry.getKey(), value.asInt());
                    } else if (value.isDouble()) {
                        options.put(entry.getKey(), value.asDouble());
                    } else {
                        options.put(entry.getKey(), value.asText());
                    }
                });
                builder.transformerOptions(options);
            }
        }

        return builder;
    }

    public void saveConfig(ObfuscationConfig config, File configFile) throws IOException 
    {
        Map<String, Object> configMap = new HashMap<>();
        
        configMap.put("renameClasses", config.isRenameClasses());
        configMap.put("renameFields", config.isRenameFields());
        configMap.put("renameMethods", config.isRenameMethods());
        configMap.put("renameLocalVariables", config.isRenameLocalVariables());
        configMap.put("stripDebugInfo", config.isStripDebugInfo());
        configMap.put("obfuscateControlFlow", config.isObfuscateControlFlow());
        configMap.put("enableBackup", config.isEnableBackup());
        configMap.put("verbose", config.isVerbose());
        configMap.put("keepMainClass", config.isKeepMainClass());
        configMap.put("keepStandardEntryPoints", config.isKeepStandardEntryPoints());
        configMap.put("sequentialTransformers", config.isSequentialTransformers());
        
        if (config.getMainClass() != null) {
            configMap.put("mainClass", config.getMainClass());
        }
        if (config.getBackupDir() != null) {
            configMap.put("backupDir", config.getBackupDir());
        }
        
        configMap.put("namingMode", config.getNamingMode().name());
        configMap.put("obfuscationLevel", config.getObfuscationLevel().name());
        configMap.put("maxThreads", config.getMaxThreads());
        
        if (!config.getKeepClasses().isEmpty()) {
            configMap.put("keepClasses", new ArrayList<>(config.getKeepClasses()));
        }
        if (!config.getKeepClassPatterns().isEmpty()) {
            configMap.put("keepClassPatterns", new ArrayList<>(config.getKeepClassPatterns()));
        }
        if (!config.getKeepMethods().isEmpty()) {
            configMap.put("keepMethods", new ArrayList<>(config.getKeepMethods()));
        }
        if (!config.getKeepFields().isEmpty()) {
            configMap.put("keepFields", new ArrayList<>(config.getKeepFields()));
        }
        if (!config.getIncludePackages().isEmpty()) {
            configMap.put("includePackages", new ArrayList<>(config.getIncludePackages()));
        }
        if (!config.getExcludePackages().isEmpty()) {
            configMap.put("excludePackages", new ArrayList<>(config.getExcludePackages()));
        }
        if (!config.getCustomMappings().isEmpty()) {
            configMap.put("customMappings", config.getCustomMappings());
        }
        if (!config.getTransformerOptions().isEmpty()) {
            configMap.put("transformerOptions", config.getTransformerOptions());
        }

        objectMapper.writerWithDefaultPrettyPrinter().writeValue(configFile, configMap);
    }
}