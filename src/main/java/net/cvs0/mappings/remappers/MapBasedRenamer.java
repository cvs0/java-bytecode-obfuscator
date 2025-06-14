package net.cvs0.mappings.remappers;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class MapBasedRenamer
{
    private final Map<String, String> mappings = new HashMap<>();
    private final AtomicInteger counter = new AtomicInteger(0);
    private final String prefix;
    
    public MapBasedRenamer()
    {
        this("a");
    }
    
    public MapBasedRenamer(String prefix)
    {
        this.prefix = prefix;
    }
    
    public String generateName(String original)
    {
        if (mappings.containsKey(original)) {
            return mappings.get(original);
        }
        
        String newName = prefix + counter.incrementAndGet();
        mappings.put(original, newName);
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
}
