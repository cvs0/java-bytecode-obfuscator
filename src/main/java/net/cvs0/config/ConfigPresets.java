package net.cvs0.config;

import net.cvs0.utils.AntiDebugger;

public class ConfigPresets
{
    public static ObfuscationConfig.Builder createPresetForLevel(ObfuscationConfig.ObfuscationLevel level)
    {
        return new ObfuscationConfig.Builder().obfuscationLevel(level);
    }
    
    public static ObfuscationConfig.Builder createProductionObfuscation()
    {
        return new ObfuscationConfig.Builder()
                .obfuscationLevel(ObfuscationConfig.ObfuscationLevel.AGGRESSIVE)
                .enableBackup(true)
                .generateScore(true)
                .optimizeCode(true)
                .compressStrings(true)
                .shuffleMembers(true)
                .keepStandardEntryPoints();
    }
    
    public static ObfuscationConfig.Builder createDevelopmentObfuscation()
    {
        return new ObfuscationConfig.Builder()
                .obfuscationLevel(ObfuscationConfig.ObfuscationLevel.BASIC)
                .verbose(true)
                .preserveLineNumbers(true)
                .enableBackup(true)
                .keepStandardEntryPoints();
    }
    
    public static ObfuscationConfig.Builder createSecureObfuscation()
    {
        return new ObfuscationConfig.Builder()
                .obfuscationLevel(ObfuscationConfig.ObfuscationLevel.EXTREME)
                .antiDebugging(true)
                .vmDetection(true)
                .vmDetectionLevel(AntiDebugger.VMDetectionLevel.COMPREHENSIVE)
                .debuggerAction(AntiDebugger.DebuggerAction.CORRUPT_EXECUTION)
                .generateScore(true)
                .optimizeCode(true)
                .compressStrings(true)
                .shuffleMembers(true)
                .enableBackup(true)
                .keepMainClass()
                .keepStandardEntryPoints();
    }
    
    public static ObfuscationConfig.Builder createPerformanceOptimizedObfuscation()
    {
        return new ObfuscationConfig.Builder()
                .obfuscationLevel(ObfuscationConfig.ObfuscationLevel.BASIC)
                .maxThreads(Runtime.getRuntime().availableProcessors())
                .optimizeCode(true)
                .sequentialTransformers(false)
                .keepStandardEntryPoints();
    }
    public static ObfuscationConfig.Builder createBasicObfuscation()
    {
        return new ObfuscationConfig.Builder()
                .renameClasses(true)
                .renameFields(true)
                .renameMethods(true)
                .keepStandardEntryPoints();
    }
    
    public static ObfuscationConfig.Builder createLibraryObfuscation()
    {
        return new ObfuscationConfig.Builder()
                .renameClasses(false)
                .renameFields(true)
                .renameMethods(false)
                .keepStandardEntryPoints()
                .keepClassMethodPattern(".*", ".*public.*");
    }
    
    public static ObfuscationConfig.Builder createApplicationObfuscation(String mainClass)
    {
        return new ObfuscationConfig.Builder()
                .mainClass(mainClass)
                .renameClasses(true)
                .renameFields(true)
                .renameMethods(true)
                .keepMainClass()
                .keepStandardEntryPoints();
    }
    
    public static ObfuscationConfig.Builder createDebugObfuscation()
    {
        return new ObfuscationConfig.Builder()
                .renameClasses(true)
                .renameFields(true)
                .renameMethods(true)
                .verbose(true)
                .keepStandardEntryPoints();
    }
    
    public static ObfuscationConfig.Builder createMinimalObfuscation()
    {
        return new ObfuscationConfig.Builder()
                .renameClasses(false)
                .renameFields(true)
                .renameMethods(false)
                .keepStandardEntryPoints();
    }
    
    public static ObfuscationConfig.Builder createAggressiveObfuscation()
    {
        return new ObfuscationConfig.Builder()
                .renameClasses(true)
                .renameFields(true)
                .renameMethods(true)
                .keepClassMethodPattern(".*", "main\\(\\[Ljava/lang/String;\\)V");
    }
    
    public static ObfuscationConfig.Builder createWebApplicationObfuscation()
    {
        return new ObfuscationConfig.Builder()
                .renameClasses(true)
                .renameFields(true)
                .renameMethods(true)
                .keepStandardEntryPoints()
                .keepClassMethodPattern(".*Controller", ".*")
                .keepClassMethodPattern(".*Service", ".*public.*")
                .keepClassMethodPattern(".*Repository", ".*public.*")
                .keepClassFieldPattern(".*Entity", ".*")
                .keepClassFieldPattern(".*Model", ".*");
    }
    
    public static ObfuscationConfig.Builder createSpringBootObfuscation(String mainClass)
    {
        return new ObfuscationConfig.Builder()
                .mainClass(mainClass)
                .renameClasses(true)
                .renameFields(true)
                .renameMethods(true)
                .keepMainClass()
                .keepStandardEntryPoints()
                .keepClassPattern(".*Application")
                .keepClassMethodPattern(".*Controller", ".*")
                .keepClassMethodPattern(".*Service", ".*public.*")
                .keepClassMethodPattern(".*Repository", ".*")
                .keepClassFieldPattern(".*Entity", ".*")
                .keepClassFieldPattern(".*Configuration", ".*")
                .keepClassPattern(".*Config.*");
    }
    
    public static ObfuscationConfig.Builder createTestSafeObfuscation()
    {
        return new ObfuscationConfig.Builder()
                .renameClasses(true)
                .renameFields(true)
                .renameMethods(true)
                .keepStandardEntryPoints()
                .keepClassPattern(".*Test.*")
                .keepClassPattern(".*Mock.*")
                .keepClassMethodPattern(".*", "test.*")
                .keepClassMethodPattern(".*", "setUp")
                .keepClassMethodPattern(".*", "tearDown")
                .keepClassMethodPattern(".*", "before.*")
                .keepClassMethodPattern(".*", "after.*");
    }
}