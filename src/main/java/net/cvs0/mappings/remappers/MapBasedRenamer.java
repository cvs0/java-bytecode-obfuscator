package net.cvs0.mappings.remappers;

import net.cvs0.config.NamingMode;
import net.cvs0.config.ObfuscationConfig;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class MapBasedRenamer
{
    private final Map<String, String> mappings = new HashMap<>();
    private final AtomicInteger counter = new AtomicInteger(0);
    private final String prefix;
    private final NamingMode namingMode;
    private final Random random = new Random();
    
    public MapBasedRenamer()
    {
        this("a", NamingMode.SEQUENTIAL_PREFIX);
    }
    
    public MapBasedRenamer(String prefix)
    {
        this(prefix, NamingMode.SEQUENTIAL_PREFIX);
    }
    
    public MapBasedRenamer(String prefix, NamingMode namingMode)
    {
        this.prefix = prefix;
        this.namingMode = namingMode != null ? namingMode : NamingMode.SEQUENTIAL_PREFIX;
    }
    
    public MapBasedRenamer(String prefix, ObfuscationConfig config)
    {
        this.prefix = prefix;
        this.namingMode = config != null ? config.getNamingMode() : NamingMode.SEQUENTIAL_PREFIX;
    }
    
    public String generateName(String original)
    {
        if (mappings.containsKey(original)) {
            return mappings.get(original);
        }
        
        String newName = generateBaseName();
        mappings.put(original, newName);
        return newName;
    }
    
    private String generateBaseName()
    {
        switch (namingMode) {
            case SEQUENTIAL_PREFIX:
                return prefix + counter.incrementAndGet();
                
            case SEQUENTIAL_ALPHA:
                return generateAlphabeticName(counter.incrementAndGet());
                
            case RANDOM_SHORT:
                return generateRandomName(4);
                
            case RANDOM_LONG:
                return generateRandomName(8 + random.nextInt(8));
                
            case SINGLE_CHAR:
                return generateSingleCharName();
                
            default:
                return prefix + counter.incrementAndGet();
        }
    }
    
    private String generateAlphabeticName(int index)
    {
        StringBuilder result = new StringBuilder();
        index--;
        
        do {
            result.insert(0, (char) ('a' + (index % 26)));
            index /= 26;
        } while (index > 0);
        
        return result.toString();
    }
    
    private String generateRandomName(int length)
    {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < length; i++) {
            result.append((char) ('a' + random.nextInt(26)));
        }
        return result.toString();
    }
    
    private String generateSingleCharName()
    {
        int index = counter.incrementAndGet();
        if (index <= 26) {
            return String.valueOf((char) ('a' + index - 1));
        } else {
            return prefix + (index - 26);
        }
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
