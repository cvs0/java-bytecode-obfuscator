package net.cvs0.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.cvs0.utils.AntiDebugger;

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
        parseAdvancedSettings(root, builder);
        parsePackageSettings(root, builder);
        parsePerformanceSettings(root, builder);
        
        return builder;
    }
    
    private void parseBasicSettings(JsonNode root, ObfuscationConfig.Builder builder)
    {
        if (root.has("mainClass")) {
            builder.mainClass(root.get("mainClass").asText());
        }
        
        if (root.has("obfuscationLevel")) {
            String levelStr = root.get("obfuscationLevel").asText();
            try {
                ObfuscationConfig.ObfuscationLevel level = ObfuscationConfig.ObfuscationLevel.valueOf(levelStr.toUpperCase());
                builder.obfuscationLevel(level);
            } catch (IllegalArgumentException e) {
                System.err.println("Warning: Invalid obfuscation level '" + levelStr + "', using default");
            }
        }
        
        parseObfuscationSettings(root, builder);
        parseNamingSettings(root, builder);
        parseSecuritySettings(root, builder);
        parseDebuggingSettings(root, builder);
        
        if (root.has("verbose")) {
            builder.verbose(root.get("verbose").asBoolean());
        }
        
        if (root.has("sequentialTransformers")) {
            builder.sequentialTransformers(root.get("sequentialTransformers").asBoolean());
        }
    }
    
    private void parseObfuscationSettings(JsonNode root, ObfuscationConfig.Builder builder)
    {
        JsonNode obfuscation = root.get("obfuscation");
        if (obfuscation == null) obfuscation = root;
        
        if (obfuscation.has("renameClasses")) {
            builder.renameClasses(obfuscation.get("renameClasses").asBoolean());
        }
        
        if (obfuscation.has("renameFields")) {
            builder.renameFields(obfuscation.get("renameFields").asBoolean());
        }
        
        if (obfuscation.has("renameMethods")) {
            builder.renameMethods(obfuscation.get("renameMethods").asBoolean());
        }
        
        if (obfuscation.has("renameLocalVariables")) {
            builder.renameLocalVariables(obfuscation.get("renameLocalVariables").asBoolean());
        }
        
        if (obfuscation.has("obfuscateConditions")) {
            builder.obfuscateConditions(obfuscation.get("obfuscateConditions").asBoolean());
        }
        
        if (obfuscation.has("compressStrings")) {
            builder.compressStrings(obfuscation.get("compressStrings").asBoolean());
        }
        
        if (obfuscation.has("shuffleMembers")) {
            builder.shuffleMembers(obfuscation.get("shuffleMembers").asBoolean());
        }
        
        if (obfuscation.has("optimizeCode")) {
            builder.optimizeCode(obfuscation.get("optimizeCode").asBoolean());
        }
    }
    
    private void parseNamingSettings(JsonNode root, ObfuscationConfig.Builder builder)
    {
        JsonNode naming = root.get("naming");
        if (naming == null) naming = root;
        
        if (naming.has("namingMode")) {
            String namingModeStr = naming.get("namingMode").asText();
            try {
                NamingMode namingMode = NamingMode.valueOf(namingModeStr);
                builder.namingMode(namingMode);
            } catch (IllegalArgumentException e) {
                System.err.println("Warning: Invalid naming mode '" + namingModeStr + "', using default");
            }
        }
    }
    
    private void parseSecuritySettings(JsonNode root, ObfuscationConfig.Builder builder)
    {
        JsonNode security = root.get("security");
        if (security == null) security = root;
        
        if (security.has("antiDebugging")) {
            builder.antiDebugging(security.get("antiDebugging").asBoolean());
        }
        
        if (security.has("debuggerAction")) {
            String debuggerActionStr = security.get("debuggerAction").asText();
            try {
                AntiDebugger.DebuggerAction debuggerAction = AntiDebugger.DebuggerAction.valueOf(debuggerActionStr);
                builder.debuggerAction(debuggerAction);
            } catch (IllegalArgumentException e) {
                System.err.println("Warning: Invalid debugger action '" + debuggerActionStr + "', using default");
            }
        }
    }
    
    private void parseDebuggingSettings(JsonNode root, ObfuscationConfig.Builder builder)
    {
        JsonNode debugging = root.get("debugging");
        if (debugging == null) debugging = root;
        
        if (debugging.has("preserveLineNumbers")) {
            builder.preserveLineNumbers(debugging.get("preserveLineNumbers").asBoolean());
        }
        
        if (debugging.has("preserveLocalVariableNames")) {
            builder.preserveLocalVariableNames(debugging.get("preserveLocalVariableNames").asBoolean());
        }
        
        if (debugging.has("verbose")) {
            builder.verbose(debugging.get("verbose").asBoolean());
        }
        
        if (debugging.has("generateScore")) {
            builder.generateScore(debugging.get("generateScore").asBoolean());
        }
    }
    
    private void parseAdvancedSettings(JsonNode root, ObfuscationConfig.Builder builder)
    {
        if (root.has("preserveLineNumbers")) {
            builder.preserveLineNumbers(root.get("preserveLineNumbers").asBoolean());
        }
        
        if (root.has("preserveLocalVariableNames")) {
            builder.preserveLocalVariableNames(root.get("preserveLocalVariableNames").asBoolean());
        }
        
        if (root.has("optimizeCode")) {
            builder.optimizeCode(root.get("optimizeCode").asBoolean());
        }
        
        if (root.has("compressStrings")) {
            builder.compressStrings(root.get("compressStrings").asBoolean());
        }
        
        if (root.has("shuffleMembers")) {
            builder.shuffleMembers(root.get("shuffleMembers").asBoolean());
        }
        
        JsonNode customSettings = root.get("customSettings");
        if (customSettings != null && customSettings.isObject()) {
            customSettings.fields().forEachRemaining(entry -> {
                String key = entry.getKey();
                JsonNode value = entry.getValue();
                
                if (value.isTextual()) {
                    builder.customSetting(key, value.asText());
                } else if (value.isBoolean()) {
                    builder.customSetting(key, value.asBoolean());
                } else if (value.isNumber()) {
                    if (value.isInt()) {
                        builder.customSetting(key, value.asInt());
                    } else {
                        builder.customSetting(key, value.asDouble());
                    }
                } else {
                    builder.customSetting(key, value.toString());
                }
            });
        }
    }
    
    private void parsePackageSettings(JsonNode root, ObfuscationConfig.Builder builder)
    {
        JsonNode packages = root.get("packages");
        if (packages == null) packages = root;
        
        JsonNode excludePackages = packages.get("excludePackages");
        if (excludePackages != null && excludePackages.isArray()) {
            for (JsonNode packageNode : excludePackages) {
                builder.excludePackage(packageNode.asText());
            }
        }
        
        JsonNode includePackages = packages.get("includePackages");
        if (includePackages != null && includePackages.isArray()) {
            for (JsonNode packageNode : includePackages) {
                builder.includePackage(packageNode.asText());
            }
        }
    }
    
    private void parsePerformanceSettings(JsonNode root, ObfuscationConfig.Builder builder)
    {
        JsonNode performance = root.get("performance");
        if (performance == null) performance = root;
        
        if (performance.has("maxThreads")) {
            builder.maxThreads(performance.get("maxThreads").asInt());
        }
        
        if (performance.has("sequentialTransformers")) {
            builder.sequentialTransformers(performance.get("sequentialTransformers").asBoolean());
        }
        
        JsonNode backup = root.get("backup");
        if (backup == null) backup = root;
        
        if (backup.has("enableBackup")) {
            builder.enableBackup(backup.get("enableBackup").asBoolean());
        }
        
        if (backup.has("backupDir")) {
            builder.backupDir(backup.get("backupDir").asText());
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