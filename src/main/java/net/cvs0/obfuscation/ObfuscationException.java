package net.cvs0.obfuscation;

public class ObfuscationException extends Exception 
{
    public ObfuscationException(String message) 
    {
        super(message);
    }

    public ObfuscationException(String message, Throwable cause) 
    {
        super(message, cause);
    }

    public ObfuscationException(Throwable cause) 
    {
        super(cause);
    }
}