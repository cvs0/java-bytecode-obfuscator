package net.cvs0.mappings.remappers;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class PackageAwareClassRenamer
{
    private final Map<String, String> mappings = new HashMap<>();
    private final AtomicInteger counter = new AtomicInteger(0);
    private final String packageScope;
    private final String prefix;
    
    public PackageAwareClassRenamer(String packageScope)
    {
        this(packageScope, "a");
    }
    
    public PackageAwareClassRenamer(String packageScope, String prefix)
    {
        this.packageScope = packageScope != null ? packageScope : "";
        this.prefix = prefix;
    }
    
    public String generateName(String originalClassName)
    {
        if (mappings.containsKey(originalClassName)) {
            return mappings.get(originalClassName);
        }
        
        String newName;
        if (packageScope.isEmpty()) {
            newName = prefix + counter.incrementAndGet();
        } else {
            if (originalClassName.startsWith(packageScope + "/")) {
                String relativePath = originalClassName.substring(packageScope.length() + 1);
                int lastSlash = relativePath.lastIndexOf('/');
                if (lastSlash != -1) {
                    String packagePart = relativePath.substring(0, lastSlash);
                    String simpleName = prefix + counter.incrementAndGet();
                    newName = packageScope + "/" + packagePart + "/" + simpleName;
                } else {
                    String simpleName = prefix + counter.incrementAndGet();
                    newName = packageScope + "/" + simpleName;
                }
            } else {
                int lastSlash = originalClassName.lastIndexOf('/');
                if (lastSlash != -1) {
                    String packagePart = originalClassName.substring(0, lastSlash);
                    String simpleName = prefix + counter.incrementAndGet();
                    newName = packagePart + "/" + simpleName;
                } else {
                    newName = prefix + counter.incrementAndGet();
                }
            }
        }
        
        mappings.put(originalClassName, newName);
        return newName;
    }
    
    public String getName(String original)
    {
        return mappings.get(original);
    }
    
    public void addMapping(String original, String obfuscated)
    {
        mappings.put(original, obfuscated);
    }
    
    public Map<String, String> getMappings()
    {
        return new HashMap<>(mappings);
    }
    
    public boolean hasMappingFor(String original)
    {
        return mappings.containsKey(original);
    }
    
    public String getPackageScope()
    {
        return packageScope;
    }
}