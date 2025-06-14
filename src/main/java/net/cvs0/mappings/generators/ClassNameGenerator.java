package net.cvs0.mappings.generators;

import net.cvs0.config.ObfuscationConfig;

import java.util.concurrent.atomic.AtomicInteger;

public class ClassNameGenerator
{
    private final AtomicInteger counter = new AtomicInteger(0);
    private final String packageScope;
    private final String prefix;
    
    public ClassNameGenerator(ObfuscationConfig config)
    {
        this.packageScope = config.getPackageScope();
        this.prefix = "a";
    }
    
    public String generateName(String originalClassName)
    {
        if (packageScope == null || packageScope.isEmpty()) {
            return prefix + counter.incrementAndGet();
        }
        
        if (originalClassName.startsWith(packageScope + "/")) {
            String relativePath = originalClassName.substring(packageScope.length() + 1);
            int lastSlash = relativePath.lastIndexOf('/');
            if (lastSlash != -1) {
                String packagePart = relativePath.substring(0, lastSlash);
                String simpleName = prefix + counter.incrementAndGet();
                return packageScope + "/" + packagePart + "/" + simpleName;
            } else {
                String simpleName = prefix + counter.incrementAndGet();
                return packageScope + "/" + simpleName;
            }
        } else {
            int lastSlash = originalClassName.lastIndexOf('/');
            if (lastSlash != -1) {
                String packagePart = originalClassName.substring(0, lastSlash);
                String simpleName = prefix + counter.incrementAndGet();
                return packagePart + "/" + simpleName;
            } else {
                return prefix + counter.incrementAndGet();
            }
        }
    }
}