package net.cvs0.config;

import java.io.File;
import java.util.*;

public class ObfuscationConfig 
{
    private final boolean renameClasses;
    private final boolean renameFields;
    private final boolean renameMethods;
    private final boolean addSyntheticMembers;
    private final boolean stripDebugInfo;
    private final boolean obfuscateControlFlow;
    private final boolean enableBackup;
    private final boolean verbose;
    private final boolean keepMainClass;
    private final boolean keepStandardEntryPoints;
    private final boolean sequentialTransformers;
    private final boolean stayInScope;
    
    private final String mainClass;
    private final String backupDir;
    private final NamingMode namingMode;
    private final ObfuscationLevel obfuscationLevel;
    private final int maxThreads;
    
    private final Set<String> keepClasses;
    private final Set<String> keepClassPatterns;
    private final Set<String> keepMethods;
    private final Set<String> keepFields;
    private final Set<String> includePackages;
    private final Set<String> excludePackages;
    
    private final Map<String, String> customMappings;
    private final Map<String, Object> transformerOptions;

    private ObfuscationConfig(Builder builder) 
    {
        this.renameClasses = builder.renameClasses;
        this.renameFields = builder.renameFields;
        this.renameMethods = builder.renameMethods;
        this.addSyntheticMembers = builder.addSyntheticMembers;
        this.stripDebugInfo = builder.stripDebugInfo;
        this.obfuscateControlFlow = builder.obfuscateControlFlow;
        this.enableBackup = builder.enableBackup;
        this.verbose = builder.verbose;
        this.keepMainClass = builder.keepMainClass;
        this.keepStandardEntryPoints = builder.keepStandardEntryPoints;
        this.sequentialTransformers = builder.sequentialTransformers;
        this.stayInScope = builder.stayInScope;
        
        this.mainClass = builder.mainClass;
        this.backupDir = builder.backupDir;
        this.namingMode = builder.namingMode;
        this.obfuscationLevel = builder.obfuscationLevel;
        this.maxThreads = builder.maxThreads;
        
        this.keepClasses = Collections.unmodifiableSet(new HashSet<>(builder.keepClasses));
        this.keepClassPatterns = Collections.unmodifiableSet(new HashSet<>(builder.keepClassPatterns));
        this.keepMethods = Collections.unmodifiableSet(new HashSet<>(builder.keepMethods));
        this.keepFields = Collections.unmodifiableSet(new HashSet<>(builder.keepFields));
        this.includePackages = Collections.unmodifiableSet(new HashSet<>(builder.includePackages));
        this.excludePackages = Collections.unmodifiableSet(new HashSet<>(builder.excludePackages));
        
        this.customMappings = Collections.unmodifiableMap(new HashMap<>(builder.customMappings));
        this.transformerOptions = Collections.unmodifiableMap(new HashMap<>(builder.transformerOptions));
    }

    public boolean isRenameClasses() { return renameClasses; }
    public boolean isRenameFields() { return renameFields; }
    public boolean isRenameMethods() { return renameMethods; }
    public boolean isAddSyntheticMembers() { return addSyntheticMembers; }
    public boolean isStripDebugInfo() { return stripDebugInfo; }
    public boolean isObfuscateControlFlow() { return obfuscateControlFlow; }
    public boolean isEnableBackup() { return enableBackup; }
    public boolean isVerbose() { return verbose; }
    public boolean isKeepMainClass() { return keepMainClass; }
    public boolean isKeepStandardEntryPoints() { return keepStandardEntryPoints; }
    public boolean isSequentialTransformers() { return sequentialTransformers; }
    public boolean isStayInScope() { return stayInScope; }
    
    public String getMainClass() { return mainClass; }
    public String getBackupDir() { return backupDir; }
    public NamingMode getNamingMode() { return namingMode; }
    public ObfuscationLevel getObfuscationLevel() { return obfuscationLevel; }
    public int getMaxThreads() { return maxThreads; }
    
    public Set<String> getKeepClasses() { return keepClasses; }
    public Set<String> getKeepClassPatterns() { return keepClassPatterns; }
    public Set<String> getKeepMethods() { return keepMethods; }
    public Set<String> getKeepFields() { return keepFields; }
    public Set<String> getIncludePackages() { return includePackages; }
    public Set<String> getExcludePackages() { return excludePackages; }
    
    public Map<String, String> getCustomMappings() { return customMappings; }
    public Map<String, Object> getTransformerOptions() { return transformerOptions; }
    
    public String getScopePrefix() {
        if (!stayInScope || mainClass == null) {
            return null;
        }
        
        String[] parts = mainClass.split("/");
        String result = null;
        if (parts.length >= 2) {
            result = parts[0] + "/" + parts[1];
        }
        
        return result;
    }

    public static class Builder 
    {
        private boolean renameClasses = true;
        private boolean renameFields = true;
        private boolean renameMethods = true;
        private boolean addSyntheticMembers = false;
        private boolean stripDebugInfo = false;
        private boolean obfuscateControlFlow = false;
        private boolean enableBackup = false;
        private boolean verbose = false;
        private boolean keepMainClass = true;
        private boolean keepStandardEntryPoints = true;
        private boolean sequentialTransformers = false;
        private boolean stayInScope = false;
        
        private String mainClass;
        private String backupDir = "backups";
        private NamingMode namingMode = NamingMode.SEQUENTIAL_PREFIX;
        private ObfuscationLevel obfuscationLevel = ObfuscationLevel.MEDIUM;
        private int maxThreads = Runtime.getRuntime().availableProcessors();
        
        private Set<String> keepClasses = new HashSet<>();
        private Set<String> keepClassPatterns = new HashSet<>();
        private Set<String> keepMethods = new HashSet<>();
        private Set<String> keepFields = new HashSet<>();
        private Set<String> includePackages = new HashSet<>();
        private Set<String> excludePackages = new HashSet<>();
        
        private Map<String, String> customMappings = new HashMap<>();
        private Map<String, Object> transformerOptions = new HashMap<>();

        public Builder renameClasses(boolean renameClasses) { this.renameClasses = renameClasses; return this; }
        public Builder renameFields(boolean renameFields) { this.renameFields = renameFields; return this; }
        public Builder renameMethods(boolean renameMethods) { this.renameMethods = renameMethods; return this; }
        public Builder addSyntheticMembers(boolean addSyntheticMembers) { this.addSyntheticMembers = addSyntheticMembers; return this; }
        public Builder stripDebugInfo(boolean stripDebugInfo) { this.stripDebugInfo = stripDebugInfo; return this; }
        public Builder obfuscateControlFlow(boolean obfuscateControlFlow) { this.obfuscateControlFlow = obfuscateControlFlow; return this; }
        public Builder enableBackup(boolean enableBackup) { this.enableBackup = enableBackup; return this; }
        public Builder verbose(boolean verbose) { this.verbose = verbose; return this; }
        public Builder keepMainClass(boolean keepMainClass) { this.keepMainClass = keepMainClass; return this; }
        public Builder keepStandardEntryPoints(boolean keepStandardEntryPoints) { this.keepStandardEntryPoints = keepStandardEntryPoints; return this; }
        public Builder sequentialTransformers(boolean sequentialTransformers) { this.sequentialTransformers = sequentialTransformers; return this; }
        public Builder stayInScope(boolean stayInScope) { this.stayInScope = stayInScope; return this; }
        
        public Builder mainClass(String mainClass) { this.mainClass = mainClass; return this; }
        public Builder backupDir(String backupDir) { this.backupDir = backupDir; return this; }
        public Builder namingMode(NamingMode namingMode) { this.namingMode = namingMode; return this; }
        public Builder obfuscationLevel(ObfuscationLevel obfuscationLevel) { this.obfuscationLevel = obfuscationLevel; return this; }
        public Builder maxThreads(int maxThreads) { this.maxThreads = Math.max(1, maxThreads); return this; }
        
        public Builder keepClass(String className) { this.keepClasses.add(className); return this; }
        public Builder keepClasses(Collection<String> classNames) { this.keepClasses.addAll(classNames); return this; }
        public Builder keepClassPattern(String pattern) { this.keepClassPatterns.add(pattern); return this; }
        public Builder keepClassPatterns(Collection<String> patterns) { this.keepClassPatterns.addAll(patterns); return this; }
        public Builder keepMethod(String methodSignature) { this.keepMethods.add(methodSignature); return this; }
        public Builder keepMethods(Collection<String> methodSignatures) { this.keepMethods.addAll(methodSignatures); return this; }
        public Builder keepField(String fieldSignature) { this.keepFields.add(fieldSignature); return this; }
        public Builder keepFields(Collection<String> fieldSignatures) { this.keepFields.addAll(fieldSignatures); return this; }
        public Builder includePackage(String packageName) { this.includePackages.add(packageName); return this; }
        public Builder includePackages(Collection<String> packageNames) { this.includePackages.addAll(packageNames); return this; }
        public Builder excludePackage(String packageName) { this.excludePackages.add(packageName); return this; }
        public Builder excludePackages(Collection<String> packageNames) { this.excludePackages.addAll(packageNames); return this; }
        
        public Builder customMapping(String original, String obfuscated) { this.customMappings.put(original, obfuscated); return this; }
        public Builder customMappings(Map<String, String> mappings) { this.customMappings.putAll(mappings); return this; }
        public Builder transformerOption(String key, Object value) { this.transformerOptions.put(key, value); return this; }
        public Builder transformerOptions(Map<String, Object> options) { this.transformerOptions.putAll(options); return this; }

        public ObfuscationConfig build() 
        {
            return new ObfuscationConfig(this);
        }
    }
}