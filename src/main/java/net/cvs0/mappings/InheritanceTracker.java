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
    
    public String getOuterClass(String innerClass)
    {
        if (innerClass == null || !innerClass.contains("$")) {
            return null;
        }
        
        int dollarIndex = innerClass.lastIndexOf('$');
        if (dollarIndex > 0) {
            return innerClass.substring(0, dollarIndex);
        }
        
        return null;
    }
    
    public boolean isInnerClass(String className)
    {
        return className != null && className.contains("$");
    }
    
    public boolean hasFieldAccess(String owner, String accessor)
    {
        if (owner == null || accessor == null) {
            return false;
        }
        
        if (owner.equals(accessor)) {
            return true;
        }
        
        String outerClass = getOuterClass(accessor);
        if (outerClass != null && outerClass.equals(owner)) {
            return true;
        }
        
        String accessorOuter = getOuterClass(accessor);
        String ownerOuter = getOuterClass(owner);
        if (accessorOuter != null && ownerOuter != null && accessorOuter.equals(ownerOuter)) {
            return true;
        }
        
        return false;
    }
}