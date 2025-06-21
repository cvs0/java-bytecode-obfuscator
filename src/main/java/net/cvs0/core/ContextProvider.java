package net.cvs0.core;

import net.cvs0.context.ObfuscationContext;

public interface ContextProvider
{
    ObfuscationContext getContext();
    String getCurrentClassName();
}