package net.cvs0.mappings;

import java.util.*;

public class InheritanceTracker
{
    private final Map<String, Set<String>> interfaceImplementors = new HashMap<>();
    private final Map<String, Set<String>> classInterfaces = new HashMap<>();
    private final Map<String, String> classSuperClass = new HashMap<>();
    private final Map<String, Set<String>> classSubClasses = new HashMap<>();
    private final Set<String> interfaceClasses = new HashSet<>();
    
    public void addClass(String className, String superClass, String[] interfaces)
    {
        if (superClass != null && !superClass.equals("java/lang/Object")) {
            classSuperClass.put(className, superClass);
            classSubClasses.computeIfAbsent(superClass, k -> new HashSet<>()).add(className);
        }
        
        if (interfaces != null) {
            Set<String> classInterfaceSet = new HashSet<>(Arrays.asList(interfaces));
            classInterfaces.put(className, classInterfaceSet);
            
            for (String iface : interfaces) {
                interfaceImplementors.computeIfAbsent(iface, k -> new HashSet<>()).add(className);
            }
        }
    }
    
    public Set<String> getImplementorsOf(String interfaceName)
    {
        Set<String> result = new HashSet<>();
        Set<String> directImplementors = interfaceImplementors.get(interfaceName);
        if (directImplementors != null) {
            result.addAll(directImplementors);
            
            for (String implementor : directImplementors) {
                result.addAll(getAllSubclasses(implementor));
            }
        }
        return result;
    }
    
    public Set<String> getAllSubclasses(String className)
    {
        Set<String> result = new HashSet<>();
        Set<String> directSubclasses = classSubClasses.get(className);
        if (directSubclasses != null) {
            for (String subclass : directSubclasses) {
                result.add(subclass);
                result.addAll(getAllSubclasses(subclass));
            }
        }
        return result;
    }
    
    public Set<String> getAllSuperclasses(String className)
    {
        Set<String> result = new HashSet<>();
        String superClass = classSuperClass.get(className);
        if (superClass != null) {
            result.add(superClass);
            result.addAll(getAllSuperclasses(superClass));
        }
        return result;
    }
    
    public Set<String> getAllInterfaces(String className)
    {
        Set<String> result = new HashSet<>();
        Set<String> directInterfaces = classInterfaces.get(className);
        if (directInterfaces != null) {
            result.addAll(directInterfaces);
        }
        
        String superClass = classSuperClass.get(className);
        if (superClass != null) {
            result.addAll(getAllInterfaces(superClass));
        }
        
        return result;
    }
    
    public void addInterface(String interfaceName)
    {
        interfaceClasses.add(interfaceName);
    }
    
    public boolean isInterface(String className)
    {
        return interfaceClasses.contains(className);
    }
}