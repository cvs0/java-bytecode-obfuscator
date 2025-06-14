package net.cvs0.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;

public class ConfigLoader
{
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    public ObfuscationConfig.Builder loadConfig(File configFile) throws IOException
    {
        if (!configFile.exists()) {
            throw new IOException("Configuration file does not exist: " + configFile.getAbsolutePath());
        }
        
        JsonNode root = objectMapper.readTree(configFile);
        ObfuscationConfig.Builder builder = new ObfuscationConfig.Builder();
        
        parseBasicSettings(root, builder);
        parseKeepRules(root, builder);
        
        return builder;
    }
    
    private void parseBasicSettings(JsonNode root, ObfuscationConfig.Builder builder)
    {
        if (root.has("mainClass")) {
            builder.mainClass(root.get("mainClass").asText());
        }
        
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
        
        if (root.has("obfuscateConditions")) {
            builder.obfuscateConditions(root.get("obfuscateConditions").asBoolean());
        }
        
        if (root.has("namingMode")) {
            String namingModeStr = root.get("namingMode").asText();
            try {
                NamingMode namingMode = NamingMode.valueOf(namingModeStr);
                builder.namingMode(namingMode);
            } catch (IllegalArgumentException e) {
                System.err.println("Warning: Invalid naming mode '" + namingModeStr + "', using default");
            }
        }
        
        if (root.has("verbose")) {
            builder.verbose(root.get("verbose").asBoolean());
        }
    }
    
    private void parseKeepRules(JsonNode root, ObfuscationConfig.Builder builder)
    {
        JsonNode keepRules = root.get("keepRules");
        if (keepRules == null) {
            return;
        }
        
        parseKeepClasses(keepRules, builder);
        parseKeepMethods(keepRules, builder);
        parseKeepFields(keepRules, builder);
        parseConveniences(keepRules, builder);
    }
    
    private void parseKeepClasses(JsonNode keepRules, ObfuscationConfig.Builder builder)
    {
        JsonNode keepClasses = keepRules.get("keepClasses");
        if (keepClasses != null && keepClasses.isArray()) {
            for (JsonNode classNode : keepClasses) {
                builder.keepClass(classNode.asText());
            }
        }
        
        JsonNode keepClassPatterns = keepRules.get("keepClassPatterns");
        if (keepClassPatterns != null && keepClassPatterns.isArray()) {
            for (JsonNode patternNode : keepClassPatterns) {
                builder.keepClassPattern(patternNode.asText());
            }
        }
    }
    
    private void parseKeepMethods(JsonNode keepRules, ObfuscationConfig.Builder builder)
    {
        JsonNode keepMethods = keepRules.get("keepMethods");
        if (keepMethods != null && keepMethods.isObject()) {
            keepMethods.fields().forEachRemaining(entry -> {
                String className = entry.getKey();
                JsonNode methods = entry.getValue();
                
                if (methods.isArray()) {
                    for (JsonNode methodNode : methods) {
                        if (methodNode.isTextual()) {
                            builder.keepClassMethod(className, methodNode.asText());
                        } else if (methodNode.isObject()) {
                            String name = methodNode.get("name").asText();
                            if (methodNode.has("descriptor")) {
                                String descriptor = methodNode.get("descriptor").asText();
                                builder.keepClassMethodWithDescriptor(className, name, descriptor);
                            } else {
                                builder.keepClassMethod(className, name);
                            }
                        }
                    }
                }
            });
        }
        
        JsonNode keepMethodPatterns = keepRules.get("keepMethodPatterns");
        if (keepMethodPatterns != null && keepMethodPatterns.isObject()) {
            keepMethodPatterns.fields().forEachRemaining(entry -> {
                String className = entry.getKey();
                JsonNode patterns = entry.getValue();
                
                if (patterns.isArray()) {
                    for (JsonNode patternNode : patterns) {
                        builder.keepClassMethodPattern(className, patternNode.asText());
                    }
                }
            });
        }
        
        JsonNode keepAllMethods = keepRules.get("keepAllMethods");
        if (keepAllMethods != null && keepAllMethods.isArray()) {
            for (JsonNode classNode : keepAllMethods) {
                builder.keepAllMethodsForClass(classNode.asText());
            }
        }
    }
    
    private void parseKeepFields(JsonNode keepRules, ObfuscationConfig.Builder builder)
    {
        JsonNode keepFields = keepRules.get("keepFields");
        if (keepFields != null && keepFields.isObject()) {
            keepFields.fields().forEachRemaining(entry -> {
                String className = entry.getKey();
                JsonNode fields = entry.getValue();
                
                if (fields.isArray()) {
                    for (JsonNode fieldNode : fields) {
                        builder.keepClassField(className, fieldNode.asText());
                    }
                }
            });
        }
        
        JsonNode keepFieldPatterns = keepRules.get("keepFieldPatterns");
        if (keepFieldPatterns != null && keepFieldPatterns.isObject()) {
            keepFieldPatterns.fields().forEachRemaining(entry -> {
                String className = entry.getKey();
                JsonNode patterns = entry.getValue();
                
                if (patterns.isArray()) {
                    for (JsonNode patternNode : patterns) {
                        builder.keepClassFieldPattern(className, patternNode.asText());
                    }
                }
            });
        }
        
        JsonNode keepAllFields = keepRules.get("keepAllFields");
        if (keepAllFields != null && keepAllFields.isArray()) {
            for (JsonNode classNode : keepAllFields) {
                builder.keepAllFieldsForClass(classNode.asText());
            }
        }
    }
    
    private void parseConveniences(JsonNode keepRules, ObfuscationConfig.Builder builder)
    {
        if (keepRules.has("keepMainClass") && keepRules.get("keepMainClass").asBoolean()) {
            builder.keepMainClass();
        }
        
        if (keepRules.has("keepStandardEntryPoints") && keepRules.get("keepStandardEntryPoints").asBoolean()) {
            builder.keepStandardEntryPoints();
        }
    }
}