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
            String backupDir)
    {
        this.mainClass = mainClass;
        this.renameClasses = renameClasses;
        this.renameFields = renameFields;
        this.renameMethods = renameMethods;
        this.renameLocalVariables = renameLocalVariables;
        this.obfuscateConditions = obfuscateConditions;
        this.verbose = verbose;
        this.keepRules = keepRules != null ? keepRules : new KeepRules();
        this.packageScope = packageScope;
        this.namingMode = namingMode != null ? namingMode : NamingMode.SEQUENTIAL_PREFIX;
        this.antiDebugging = antiDebugging;
        this.debuggerAction = debuggerAction != null ? debuggerAction : AntiDebugger.DebuggerAction.EXIT_SILENTLY;
        this.generateScore = generateScore;
        this.sequentialTransformers = sequentialTransformers;
        this.obfuscationLevel = obfuscationLevel != null ? obfuscationLevel : ObfuscationLevel.BASIC;
        this.customSettings = customSettings != null ? new HashMap<>(customSettings) : new HashMap<>();
        this.excludePackages = excludePackages != null ? new ArrayList<>(excludePackages) : new ArrayList<>();
        this.includePackages = includePackages != null ? new ArrayList<>(includePackages) : new ArrayList<>();
        this.preserveLineNumbers = preserveLineNumbers;
        this.preserveLocalVariableNames = preserveLocalVariableNames;
        this.optimizeCode = optimizeCode;
        this.compressStrings = compressStrings;
        this.shuffleMembers = shuffleMembers;
        this.maxThreads = maxThreads > 0 ? maxThreads : Runtime.getRuntime().availableProcessors();
        this.enableBackup = enableBackup;
        this.backupDir = backupDir;
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
        private boolean renameClasses = true;
        private boolean renameFields = true;
        private boolean renameMethods = true;
        private boolean renameLocalVariables = true;
        private boolean obfuscateConditions = false;
        private boolean verbose = false;
        private final KeepRules keepRules = new KeepRules();
        private String packageScope;
        private NamingMode namingMode = NamingMode.SEQUENTIAL_PREFIX;
        private boolean antiDebugging = false;
        private AntiDebugger.DebuggerAction debuggerAction = AntiDebugger.DebuggerAction.EXIT_SILENTLY;
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
            keepClassMethodPattern(".*", "main\\(\\[Ljava/lang/String;\\)V");
            keepClassMethodPattern(".*", "<init>");
            keepClassMethodPattern(".*", "<clinit>");
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
                    namingMode = NamingMode.RANDOM_SHORT;
                    shuffleMembers = true;
                    break;
                    
                case EXTREME:
                    renameClasses = true;
                    renameFields = true;
                    renameMethods = true;
                    renameLocalVariables = true;
                    obfuscateConditions = true;
                    antiDebugging = true;
                    debuggerAction = AntiDebugger.DebuggerAction.CORRUPT_EXECUTION;
                    namingMode = NamingMode.RANDOM_LONG;
                    shuffleMembers = true;
                    compressStrings = true;
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
                antiDebugging, debuggerAction, generateScore, sequentialTransformers,
                obfuscationLevel, customSettings, excludePackages, includePackages,
                preserveLineNumbers, preserveLocalVariableNames, optimizeCode, 
                compressStrings, shuffleMembers, maxThreads, enableBackup, backupDir
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
