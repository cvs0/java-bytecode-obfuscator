package net.cvs0.config;

public class ConfigPresets
{
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