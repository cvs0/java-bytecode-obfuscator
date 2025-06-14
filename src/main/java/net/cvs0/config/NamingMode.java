package net.cvs0.config;

public enum NamingMode
{
    SEQUENTIAL_PREFIX("Sequential with prefix (a1, a2, a3...)"),
    SEQUENTIAL_ALPHA("Sequential alphabetic (a, b, c... aa, ab, ac...)"),
    RANDOM_SHORT("Random short names (abcd, xyzk, mnop...)"),
    RANDOM_LONG("Random long names (highly obfuscated)"),
    SINGLE_CHAR("Single character names (a, b, c...)");
    
    private final String description;
    
    NamingMode(String description)
    {
        this.description = description;
    }
    
    public String getDescription()
    {
        return description;
    }
}