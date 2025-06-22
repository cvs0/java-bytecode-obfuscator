package net.cvs0.config;

import net.cvs0.utils.AntiDebugger;
import java.util.*;
import java.util.regex.Pattern;

public class ObfuscationConfig
{
    private final String mainClass;
    private final boolean renameClasses;
    private final boolean renameFields;
    private final boolean renameMethods;
    private final boolean renameLocalVariables;
    private final boolean obfuscateConditions;
    private final boolean verbose;
    private final KeepRules keepRules;
    private final String packageScope;
    private final NamingMode namingMode;
    private final boolean antiDebugging;
    private final AntiDebugger.DebuggerAction debuggerAction;
    private final boolean vmDetection;
    private final AntiDebugger.VMDetectionLevel vmDetectionLevel;
    private final boolean generateScore;
    private final boolean sequentialTransformers;
    private final ObfuscationLevel obfuscationLevel;
    private final Map<String, Object> customSettings;
    private final List<String> excludePackages;
    private final List<String> includePackages;
    private final boolean preserveLineNumbers;
    private final boolean preserveLocalVariableNames;
    private final boolean optimizeCode;
    private final boolean compressStrings;
    private final boolean shuffleMembers;
    private final int maxThreads;
    private final boolean enableBackup;
    private final String backupDir;
    private final boolean floodFakeInterfaces;
    private final int fakeInterfaceCount;
    private final boolean inlineSimpleMethods;
    private final boolean insertFakeExceptions;

    public enum ObfuscationLevel
    {
        MINIMAL("Minimal obfuscation - basic renaming only"),
        BASIC("Basic obfuscation - standard settings"),
        AGGRESSIVE("Aggressive obfuscation - heavy transformations"),
        EXTREME("Extreme obfuscation - maximum security, may impact performance"),
        CUSTOM("Custom configuration");

        private final String description;

        ObfuscationLevel(String description)
        {
            this.description = description;
        }

        public String getDescription()
        {
            return description;
        }
    }

    public ObfuscationConfig(
            String mainClass,
            boolean renameClasses,
            boolean renameFields,
            boolean renameMethods,
            boolean renameLocalVariables,
            boolean obfuscateConditions,
            boolean verbose,
            KeepRules keepRules,
            String packageScope,
            NamingMode namingMode,
            boolean antiDebugging,
            AntiDebugger.DebuggerAction debuggerAction,
            boolean vmDetection,
            AntiDebugger.VMDetectionLevel vmDetectionLevel,
            boolean generateScore,
            boolean sequentialTransformers,
            ObfuscationLevel obfuscationLevel,
            Map<String, Object> customSettings,
            List<String> excludePackages,
            List<String> includePackages,
            boolean preserveLineNumbers,
            boolean preserveLocalVariableNames,
            boolean optimizeCode,
            boolean compressStrings,
            boolean shuffleMembers,
            int maxThreads,
            boolean enableBackup,
            String backupDir,
            boolean floodFakeInterfaces,
            int fakeInterfaceCount,
            boolean inlineSimpleMethods,
            boolean insertFakeExceptions)
    {
        validateConfigurationInputs(mainClass, packageScope, maxThreads, backupDir, enableBackup, 
                                   customSettings, excludePackages, includePackages,
                                   renameLocalVariables, preserveLocalVariableNames, fakeInterfaceCount);
        
        this.mainClass = sanitizeClassName(mainClass);
        this.renameClasses = renameClasses;
        this.renameFields = renameFields;
        this.renameMethods = renameMethods;
        this.renameLocalVariables = renameLocalVariables;
        this.obfuscateConditions = obfuscateConditions;
        this.verbose = verbose;
        this.keepRules = keepRules != null ? keepRules : new KeepRules();
        this.packageScope = sanitizePackageScope(packageScope);
        this.namingMode = namingMode != null ? namingMode : NamingMode.SEQUENTIAL_PREFIX;
        this.antiDebugging = antiDebugging;
        this.debuggerAction = debuggerAction != null ? debuggerAction : AntiDebugger.DebuggerAction.EXIT_SILENTLY;
        this.vmDetection = vmDetection;
        this.vmDetectionLevel = vmDetectionLevel != null ? vmDetectionLevel : AntiDebugger.VMDetectionLevel.BASIC;
        this.generateScore = generateScore;
        this.sequentialTransformers = sequentialTransformers;
        this.obfuscationLevel = obfuscationLevel != null ? obfuscationLevel : ObfuscationLevel.BASIC;
        this.customSettings = sanitizeCustomSettings(customSettings);
        this.excludePackages = sanitizePackageList(excludePackages, "excludePackages");
        this.includePackages = sanitizePackageList(includePackages, "includePackages");
        this.preserveLineNumbers = preserveLineNumbers;
        this.preserveLocalVariableNames = preserveLocalVariableNames;
        this.optimizeCode = optimizeCode;
        this.compressStrings = compressStrings;
        this.shuffleMembers = shuffleMembers;
        this.maxThreads = validateMaxThreads(maxThreads);
        this.enableBackup = enableBackup;
        this.backupDir = sanitizeBackupDir(backupDir, enableBackup);
        this.floodFakeInterfaces = floodFakeInterfaces;
        this.fakeInterfaceCount = Math.max(1, Math.min(fakeInterfaceCount, 50));
        this.inlineSimpleMethods = inlineSimpleMethods;
        this.insertFakeExceptions = insertFakeExceptions;
    }
    
    private void validateConfigurationInputs(String mainClass, String packageScope, int maxThreads,
                                           String backupDir, boolean enableBackup,
                                           Map<String, Object> customSettings,
                                           List<String> excludePackages, List<String> includePackages,
                                           boolean renameLocalVariables, boolean preserveLocalVariableNames, int fakeInterfaceCount)
    {
        if (mainClass != null && mainClass.length() > 1000) {
            throw new IllegalArgumentException("Main class name is too long (> 1000 characters)");
        }
        
        if (packageScope != null && packageScope.length() > 1000) {
            throw new IllegalArgumentException("Package scope is too long (> 1000 characters)");
        }
        
        if (maxThreads < 0) {
            throw new IllegalArgumentException("Max threads cannot be negative");
        }
        
        if (maxThreads > 1000) {
            throw new IllegalArgumentException("Max threads is too high (> 1000)");
        }
        
        if (enableBackup && (backupDir == null || backupDir.trim().isEmpty())) {
            throw new IllegalArgumentException("Backup directory must be specified when backup is enabled");
        }
        
        if (customSettings != null && customSettings.size() > 1000) {
            throw new IllegalArgumentException("Too many custom settings (> 1000)");
        }
        
        if (excludePackages != null && excludePackages.size() > 100) {
            throw new IllegalArgumentException("Too many excluded packages (> 100)");
        }
        
        if (includePackages != null && includePackages.size() > 100) {
            throw new IllegalArgumentException("Too many included packages (> 100)");
        }
        
        if (renameLocalVariables && preserveLocalVariableNames) {
            throw new IllegalArgumentException("Cannot rename local variables while preserving their names");
        }
        
        if (fakeInterfaceCount < 0) {
            throw new IllegalArgumentException("Fake interface count cannot be negative");
        }
        
        if (fakeInterfaceCount > 50) {
            throw new IllegalArgumentException("Fake interface count is too high (> 50)");
        }
    }
    
    private String sanitizeClassName(String mainClass) {
        if (mainClass == null) {
            return null;
        }
        
        String trimmed = mainClass.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        
        if (trimmed.contains("..") || trimmed.startsWith("/") || trimmed.endsWith("/")) {
            throw new IllegalArgumentException("Invalid main class name: " + mainClass);
        }
        
        return trimmed;
    }
    
    private String sanitizePackageScope(String packageScope) {
        if (packageScope == null) {
            return null;
        }
        
        String trimmed = packageScope.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        
        if (trimmed.contains("..") || trimmed.startsWith("/") || trimmed.endsWith("/")) {
            throw new IllegalArgumentException("Invalid package scope: " + packageScope);
        }
        
        return trimmed;
    }
    
    private Map<String, Object> sanitizeCustomSettings(Map<String, Object> customSettings) {
        if (customSettings == null) {
            return new HashMap<>();
        }
        
        Map<String, Object> sanitized = new HashMap<>();
        for (Map.Entry<String, Object> entry : customSettings.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            
            if (key == null || key.trim().isEmpty()) {
                continue;
            }
            
            if (key.length() > 100) {
                throw new IllegalArgumentException("Custom setting key too long: " + key.substring(0, 50) + "...");
            }
            
            sanitized.put(key.trim(), value);
        }
        
        return sanitized;
    }
    
    private List<String> sanitizePackageList(List<String> packages, String listName) {
        if (packages == null) {
            return new ArrayList<>();
        }
        
        List<String> sanitized = new ArrayList<>();
        for (String pkg : packages) {
            if (pkg == null) {
                continue;
            }
            
            String trimmed = pkg.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            
            if (trimmed.length() > 500) {
                throw new IllegalArgumentException(listName + " package name too long: " + trimmed.substring(0, 50) + "...");
            }
            
            if (trimmed.contains("..") || trimmed.startsWith(".") || trimmed.endsWith(".")) {
                throw new IllegalArgumentException("Invalid package name in " + listName + ": " + trimmed);
            }
            
            sanitized.add(trimmed);
        }
        
        return sanitized;
    }
    
    private int validateMaxThreads(int maxThreads) {
        if (maxThreads <= 0) {
            return Runtime.getRuntime().availableProcessors();
        }
        
        int processors = Runtime.getRuntime().availableProcessors();
        if (maxThreads > processors * 4) {
            return processors * 2;
        }
        
        return maxThreads;
    }
    
    private String sanitizeBackupDir(String backupDir, boolean enableBackup) {
        if (!enableBackup) {
            return backupDir;
        }
        
        if (backupDir == null) {
            return null;
        }
        
        String trimmed = backupDir.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        
        if (trimmed.length() > 500) {
            throw new IllegalArgumentException("Backup directory path too long: " + trimmed.substring(0, 50) + "...");
        }
        
        return trimmed;
    }

    public String getMainClass()
    {
        return mainClass;
    }

    public boolean isRenameClasses()
    {
        return renameClasses;
    }

    public boolean isRenameFields()
    {
        return renameFields;
    }

    public boolean isRenameMethods()
    {
        return renameMethods;
    }
    
    public boolean isRenameLocalVariables()
    {
        return renameLocalVariables;
    }
    
    public boolean isObfuscateConditions()
    {
        return obfuscateConditions;
    }
    
    public boolean isVerbose()
    {
        return verbose;
    }
    
    public String getPackageScope()
    {
        return packageScope;
    }
    
    public NamingMode getNamingMode()
    {
        return namingMode;
    }
    
    public KeepRules getKeepRules()
    {
        return keepRules;
    }
    
    public boolean isAntiDebugging()
    {
        return antiDebugging;
    }
    
    public AntiDebugger.DebuggerAction getDebuggerAction()
    {
        return debuggerAction;
    }
    
    public boolean isVmDetection()
    {
        return vmDetection;
    }
    
    public AntiDebugger.VMDetectionLevel getVmDetectionLevel()
    {
        return vmDetectionLevel;
    }
    
    public boolean isGenerateScore()
    {
        return generateScore;
    }
    
    public boolean isSequentialTransformers()
    {
        return sequentialTransformers;
    }
    
    public ObfuscationLevel getObfuscationLevel()
    {
        return obfuscationLevel;
    }
    
    public Map<String, Object> getCustomSettings()
    {
        return customSettings;
    }
    
    public List<String> getExcludePackages()
    {
        return excludePackages;
    }
    
    public List<String> getIncludePackages()
    {
        return includePackages;
    }
    
    public boolean isPreserveLineNumbers()
    {
        return preserveLineNumbers;
    }
    
    public boolean isPreserveLocalVariableNames()
    {
        return preserveLocalVariableNames;
    }
    
    public boolean isOptimizeCode()
    {
        return optimizeCode;
    }
    
    public boolean isCompressStrings()
    {
        return compressStrings;
    }
    
    public boolean isShuffleMembers()
    {
        return shuffleMembers;
    }
    
    public int getMaxThreads()
    {
        return maxThreads;
    }
    
    public boolean isEnableBackup()
    {
        return enableBackup;
    }
    
    public String getBackupDir()
    {
        return backupDir;
    }
    
    public boolean isFloodFakeInterfaces()
    {
        return floodFakeInterfaces;
    }
    
    public int getFakeInterfaceCount()
    {
        return fakeInterfaceCount;
    }
    
    public boolean isInlineSimpleMethods()
    {
        return inlineSimpleMethods;
    }
    
    public boolean isInsertFakeExceptions()
    {
        return insertFakeExceptions;
    }
    
    public boolean shouldKeepClass(String className)
    {
        return keepRules.shouldKeepClass(className);
    }
    
    public boolean shouldKeepMethod(String className, String methodName, String methodDescriptor)
    {
        return keepRules.shouldKeepMethod(className, methodName, methodDescriptor);
    }
    
    public boolean shouldKeepField(String className, String fieldName)
    {
        return keepRules.shouldKeepField(className, fieldName);
    }
    
    public boolean isInPackageScope(String className)
    {
        if (className == null) {
            return false;
        }
        
        for (String excludePackage : excludePackages) {
            if (className.startsWith(excludePackage)) {
                return false;
            }
        }
        
        if (!includePackages.isEmpty()) {
            for (String includePackage : includePackages) {
                if (className.startsWith(includePackage)) {
                    return true;
                }
            }
            return false;
        }
        
        if (packageScope == null || packageScope.isEmpty()) {
            return true;
        }
        
        return className.startsWith(packageScope);
    }
    
    public static class KeepRules
    {
        private final Set<String> keepClasses;
        private final Set<Pattern> keepClassPatterns;
        private final Map<String, Set<String>> keepClassMethods;
        private final Map<String, Set<Pattern>> keepClassMethodPatterns;
        private final Map<String, Set<String>> keepClassFields;
        private final Map<String, Set<Pattern>> keepClassFieldPatterns;
        private final Set<String> keepAllMethodsForClasses;
        private final Set<String> keepAllFieldsForClasses;
        
        public KeepRules()
        {
            this.keepClasses = new HashSet<>();
            this.keepClassPatterns = new HashSet<>();
            this.keepClassMethods = new HashMap<>();
            this.keepClassMethodPatterns = new HashMap<>();
            this.keepClassFields = new HashMap<>();
            this.keepClassFieldPatterns = new HashMap<>();
            this.keepAllMethodsForClasses = new HashSet<>();
            this.keepAllFieldsForClasses = new HashSet<>();
        }
        
        public boolean shouldKeepClass(String className)
        {
            if (keepClasses.contains(className)) {
                return true;
            }
            
            for (Pattern pattern : keepClassPatterns) {
                if (pattern.matcher(className).matches()) {
                    return true;
                }
            }
            
            return false;
        }
        
        public boolean shouldKeepMethod(String className, String methodName, String methodDescriptor)
        {
            if (shouldKeepClass(className)) {
                return true;
            }
            
            if (keepAllMethodsForClasses.contains(className)) {
                return true;
            }
            
            Set<String> methodsToKeep = keepClassMethods.get(className);
            if (methodsToKeep != null) {
                String methodSignature = methodName + methodDescriptor;
                if (methodsToKeep.contains(methodName) || methodsToKeep.contains(methodSignature)) {
                    return true;
                }
            }
            
            Set<Pattern> methodPatterns = keepClassMethodPatterns.get(className);
            if (methodPatterns != null) {
                for (Pattern pattern : methodPatterns) {
                    if (pattern.matcher(methodName).matches()) {
                        return true;
                    }
                }
            }
            
            return false;
        }
        
        public boolean shouldKeepField(String className, String fieldName)
        {
            if (shouldKeepClass(className)) {
                return true;
            }
            
            if (keepAllFieldsForClasses.contains(className)) {
                return true;
            }
            
            Set<String> fieldsToKeep = keepClassFields.get(className);
            if (fieldsToKeep != null && fieldsToKeep.contains(fieldName)) {
                return true;
            }
            
            Set<Pattern> fieldPatterns = keepClassFieldPatterns.get(className);
            if (fieldPatterns != null) {
                for (Pattern pattern : fieldPatterns) {
                    if (pattern.matcher(fieldName).matches()) {
                        return true;
                    }
                }
            }
            
            return false;
        }
        
        public Set<String> getKeepClasses()
        {
            return keepClasses;
        }
        
        public Map<String, Set<String>> getKeepClassMethods()
        {
            return keepClassMethods;
        }
        
        public Map<String, Set<String>> getKeepClassFields()
        {
            return keepClassFields;
        }
    }
    
    public static class Builder
    {
        private String mainClass;
        private boolean renameClasses = false;
        private boolean renameFields = false;
        private boolean renameMethods = false;
        private boolean renameLocalVariables = false;
        private boolean obfuscateConditions = false;
        private boolean verbose = false;
        private final KeepRules keepRules = new KeepRules();
        private String packageScope;
        private NamingMode namingMode = NamingMode.SEQUENTIAL_PREFIX;
        private boolean antiDebugging = false;
        private AntiDebugger.DebuggerAction debuggerAction = AntiDebugger.DebuggerAction.EXIT_SILENTLY;
        private boolean vmDetection = false;
        private AntiDebugger.VMDetectionLevel vmDetectionLevel = AntiDebugger.VMDetectionLevel.BASIC;
        private boolean generateScore = false;
        private boolean sequentialTransformers = false;
        private ObfuscationLevel obfuscationLevel = ObfuscationLevel.BASIC;
        private final Map<String, Object> customSettings = new HashMap<>();
        private final List<String> excludePackages = new ArrayList<>();
        private final List<String> includePackages = new ArrayList<>();
        private boolean preserveLineNumbers = false;
        private boolean preserveLocalVariableNames = false;
        private boolean optimizeCode = false;
        private boolean compressStrings = false;
        private boolean shuffleMembers = false;
        private int maxThreads = Runtime.getRuntime().availableProcessors();
        private boolean enableBackup = false;
        private String backupDir;
        private boolean floodFakeInterfaces = false;
        private int fakeInterfaceCount = 10;
        private boolean inlineSimpleMethods = false;
        private boolean insertFakeExceptions = false;
        
        public Builder mainClass(String mainClass)
        {
            this.mainClass = mainClass;
            if (mainClass != null && !mainClass.isEmpty()) {
                String[] parts = mainClass.split("/");
                if (parts.length >= 2 && parts[0] != null && parts[1] != null) {
                    this.packageScope = parts[0] + "/" + parts[1];
                } else if (parts.length == 1 && parts[0] != null) {
                    this.packageScope = parts[0];
                } else {
                    this.packageScope = "";
                }
            }
            return this;
        }
        
        public Builder renameClasses(boolean renameClasses)
        {
            this.renameClasses = renameClasses;
            return this;
        }
        
        public Builder renameFields(boolean renameFields)
        {
            this.renameFields = renameFields;
            return this;
        }
        
        public Builder renameMethods(boolean renameMethods)
        {
            this.renameMethods = renameMethods;
            return this;
        }
        
        public Builder renameLocalVariables(boolean renameLocalVariables)
        {
            this.renameLocalVariables = renameLocalVariables;
            return this;
        }
        
        public Builder obfuscateConditions(boolean obfuscateConditions)
        {
            this.obfuscateConditions = obfuscateConditions;
            return this;
        }
        
        public Builder verbose(boolean verbose)
        {
            this.verbose = verbose;
            return this;
        }
        
        public Builder namingMode(NamingMode namingMode)
        {
            this.namingMode = namingMode;
            return this;
        }
        
        public Builder keepClass(String className)
        {
            keepRules.keepClasses.add(className);
            return this;
        }
        
        public Builder keepClassPattern(String classPattern)
        {
            keepRules.keepClassPatterns.add(Pattern.compile(classPattern));
            return this;
        }
        
        public Builder keepClassMethod(String className, String methodName)
        {
            keepRules.keepClassMethods.computeIfAbsent(className, k -> new HashSet<>()).add(methodName);
            return this;
        }
        
        public Builder keepClassMethodWithDescriptor(String className, String methodName, String descriptor)
        {
            keepRules.keepClassMethods.computeIfAbsent(className, k -> new HashSet<>()).add(methodName + descriptor);
            return this;
        }
        
        public Builder keepClassMethodPattern(String className, String methodPattern)
        {
            keepRules.keepClassMethodPatterns.computeIfAbsent(className, k -> new HashSet<>()).add(Pattern.compile(methodPattern));
            return this;
        }
        
        public Builder keepAllMethodsForClass(String className)
        {
            keepRules.keepAllMethodsForClasses.add(className);
            return this;
        }
        
        public Builder keepClassField(String className, String fieldName)
        {
            keepRules.keepClassFields.computeIfAbsent(className, k -> new HashSet<>()).add(fieldName);
            return this;
        }
        
        public Builder keepClassFieldPattern(String className, String fieldPattern)
        {
            keepRules.keepClassFieldPatterns.computeIfAbsent(className, k -> new HashSet<>()).add(Pattern.compile(fieldPattern));
            return this;
        }
        
        public Builder keepAllFieldsForClass(String className)
        {
            keepRules.keepAllFieldsForClasses.add(className);
            return this;
        }
        
        public Builder keepMainClass()
        {
            if (mainClass != null) {
                keepClass(mainClass);
            }
            return this;
        }
        
        public Builder keepStandardEntryPoints()
        {
            KeepRulesManager.applyRulesToBuilder(this, "STANDARD_ENTRY_POINTS");
            return this;
        }
        
        public Builder antiDebugging(boolean antiDebugging)
        {
            this.antiDebugging = antiDebugging;
            return this;
        }
        
        public Builder debuggerAction(AntiDebugger.DebuggerAction debuggerAction)
        {
            this.debuggerAction = debuggerAction;
            return this;
        }
        
        public Builder vmDetection(boolean vmDetection)
        {
            this.vmDetection = vmDetection;
            return this;
        }
        
        public Builder vmDetectionLevel(AntiDebugger.VMDetectionLevel vmDetectionLevel)
        {
            this.vmDetectionLevel = vmDetectionLevel;
            return this;
        }
        
        public Builder generateScore(boolean generateScore)
        {
            this.generateScore = generateScore;
            return this;
        }
        
        public Builder sequentialTransformers(boolean sequentialTransformers)
        {
            this.sequentialTransformers = sequentialTransformers;
            return this;
        }
        
        public Builder obfuscationLevel(ObfuscationLevel level)
        {
            this.obfuscationLevel = level;
            applyPresetForLevel(level);
            return this;
        }
        
        public Builder customSetting(String key, Object value)
        {
            this.customSettings.put(key, value);
            return this;
        }
        
        public Builder excludePackage(String packageName)
        {
            this.excludePackages.add(packageName);
            return this;
        }
        
        public Builder includePackage(String packageName)
        {
            this.includePackages.add(packageName);
            return this;
        }
        
        public Builder preserveLineNumbers(boolean preserve)
        {
            this.preserveLineNumbers = preserve;
            return this;
        }
        
        public Builder preserveLocalVariableNames(boolean preserve)
        {
            this.preserveLocalVariableNames = preserve;
            return this;
        }
        
        public Builder optimizeCode(boolean optimize)
        {
            this.optimizeCode = optimize;
            return this;
        }
        
        public Builder compressStrings(boolean compress)
        {
            this.compressStrings = compress;
            return this;
        }
        
        public Builder shuffleMembers(boolean shuffle)
        {
            this.shuffleMembers = shuffle;
            return this;
        }
        
        public Builder maxThreads(int threads)
        {
            this.maxThreads = Math.max(1, threads);
            return this;
        }
        
        public Builder enableBackup(boolean enable)
        {
            this.enableBackup = enable;
            return this;
        }
        
        public Builder backupDir(String dir)
        {
            this.backupDir = dir;
            return this;
        }
        
        public Builder floodFakeInterfaces(boolean flood)
        {
            this.floodFakeInterfaces = flood;
            return this;
        }
        
        public Builder fakeInterfaceCount(int count)
        {
            this.fakeInterfaceCount = Math.max(1, Math.min(count, 50));
            return this;
        }
        
        public Builder inlineSimpleMethods(boolean inline)
        {
            this.inlineSimpleMethods = inline;
            return this;
        }
        
        public Builder insertFakeExceptions(boolean insert)
        {
            this.insertFakeExceptions = insert;
            return this;
        }
        
        private void applyPresetForLevel(ObfuscationLevel level)
        {
            switch (level) {
                case MINIMAL:
                    renameClasses = true;
                    renameFields = false;
                    renameMethods = false;
                    renameLocalVariables = false;
                    obfuscateConditions = false;
                    antiDebugging = false;
                    namingMode = NamingMode.SEQUENTIAL_PREFIX;
                    break;
                    
                case BASIC:
                    renameClasses = true;
                    renameFields = true;
                    renameMethods = true;
                    renameLocalVariables = false;
                    obfuscateConditions = false;
                    antiDebugging = false;
                    namingMode = NamingMode.SEQUENTIAL_ALPHA;
                    break;
                    
                case AGGRESSIVE:
                    renameClasses = true;
                    renameFields = true;
                    renameMethods = true;
                    renameLocalVariables = true;
                    obfuscateConditions = true;
                    antiDebugging = true;
                    vmDetection = true;
                    vmDetectionLevel = AntiDebugger.VMDetectionLevel.COMPREHENSIVE;
                    namingMode = NamingMode.RANDOM_SHORT;
                    shuffleMembers = true;
                    floodFakeInterfaces = true;
                    fakeInterfaceCount = 15;
                    inlineSimpleMethods = true;
                    insertFakeExceptions = true;
                    break;
                    
                case EXTREME:
                    renameClasses = true;
                    renameFields = true;
                    renameMethods = true;
                    renameLocalVariables = true;
                    obfuscateConditions = true;
                    antiDebugging = true;
                    vmDetection = true;
                    vmDetectionLevel = AntiDebugger.VMDetectionLevel.PARANOID;
                    debuggerAction = AntiDebugger.DebuggerAction.CORRUPT_EXECUTION;
                    namingMode = NamingMode.RANDOM_LONG;
                    shuffleMembers = true;
                    compressStrings = true;
                    floodFakeInterfaces = true;
                    fakeInterfaceCount = 25;
                    inlineSimpleMethods = true;
                    insertFakeExceptions = true;
                    break;
                    
                case CUSTOM:
                default:
                    break;
            }
        }
        
        public ObfuscationConfig build()
        {
            ConfigValidator.ValidationResult validation = ConfigValidator.validateBuilder(this);
            if (!validation.isValid()) {
                throw new IllegalStateException("Invalid configuration: " + String.join(", ", validation.getErrors()));
            }
            
            return new ObfuscationConfig(
                mainClass, renameClasses, renameFields, renameMethods, renameLocalVariables, 
                obfuscateConditions, verbose, keepRules, packageScope, namingMode, 
                antiDebugging, debuggerAction, vmDetection, vmDetectionLevel, generateScore, sequentialTransformers,
                obfuscationLevel, customSettings, excludePackages, includePackages,
                preserveLineNumbers, preserveLocalVariableNames, optimizeCode, 
                compressStrings, shuffleMembers, maxThreads, enableBackup, backupDir,
                floodFakeInterfaces, fakeInterfaceCount, inlineSimpleMethods, insertFakeExceptions
            );
        }
        
        public ConfigValidator.ValidationResult validate()
        {
            return ConfigValidator.validateBuilder(this);
        }
        
        public boolean isValid()
        {
            return validate().isValid();
        }
    }
}
