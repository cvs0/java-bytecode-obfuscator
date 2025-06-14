package net.cvs0.mappings.generators;

import net.cvs0.config.ObfuscationConfig;

public class ClassNameGenerator extends BaseNameGenerator
{
    private final String packageScope;
    
    public ClassNameGenerator(ObfuscationConfig config)
    {
        super("a", config);
        this.packageScope = config.getPackageScope();
    }
    
    public String generateName(String originalClassName)
    {
        String baseName = generateBaseName();
        
        if (packageScope == null || packageScope.isEmpty()) {
            return baseName;
        }
        
        if (originalClassName.startsWith(packageScope + "/")) {
            String relativePath = originalClassName.substring(packageScope.length() + 1);
            int lastSlash = relativePath.lastIndexOf('/');
            if (lastSlash != -1) {
                String packagePart = relativePath.substring(0, lastSlash);
                return packageScope + "/" + packagePart + "/" + baseName;
            } else {
                return packageScope + "/" + baseName;
            }
        } else {
            int lastSlash = originalClassName.lastIndexOf('/');
            if (lastSlash != -1) {
                String packagePart = originalClassName.substring(0, lastSlash);
                return packagePart + "/" + baseName;
            } else {
                return baseName;
            }
        }
    }
}