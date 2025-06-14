package net.cvs0.mappings.generators;

import net.cvs0.config.NamingMode;
import net.cvs0.config.ObfuscationConfig;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class BaseNameGenerator
{
    protected final NamingMode namingMode;
    protected final AtomicInteger counter = new AtomicInteger(0);
    protected final Random random = new Random();
    protected final String prefix;
    
    protected BaseNameGenerator(String prefix, ObfuscationConfig config)
    {
        this.prefix = prefix;
        this.namingMode = config.getNamingMode();
    }
    
    protected String generateBaseName()
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
}