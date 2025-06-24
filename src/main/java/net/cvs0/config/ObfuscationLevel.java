package net.cvs0.config;

public enum ObfuscationLevel 
{
    LIGHT("Light obfuscation - basic renaming only"),
    MEDIUM("Medium obfuscation - renaming + some control flow"),
    HEAVY("Heavy obfuscation - comprehensive transformation"),
    MAXIMUM("Maximum obfuscation - all techniques enabled");

    private final String description;

    ObfuscationLevel(String description) 
    {
        this.description = description;
    }

    public String getDescription() 
    {
        return description;
    }
    
    public boolean includesControlFlow() 
    {
        return this == MEDIUM || this == HEAVY || this == MAXIMUM;
    }
    
    public boolean includesStringObfuscation() 
    {
        return this == HEAVY || this == MAXIMUM;
    }
    
    public boolean includesFieldEncryption() 
    {
        return this == MAXIMUM;
    }
}