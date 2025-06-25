package net.cvs0.core;

import net.cvs0.classfile.ProgramClass;
import net.cvs0.classfile.ProgramField;
import net.cvs0.classfile.ProgramMethod;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class Program 
{
    private final Map<String, ProgramClass> classes;
    private final Map<String, Set<String>> classHierarchy;
    private final Map<String, Set<String>> interfaceImplementations;
    private final Map<String, Set<String>> innerClassRelations;
    private final Set<String> entryPoints;
    private final Map<String, Set<String>> dependencies;

    public Program() 
    {
        this.classes = new ConcurrentHashMap<>();
        this.classHierarchy = new ConcurrentHashMap<>();
        this.interfaceImplementations = new ConcurrentHashMap<>();
        this.innerClassRelations = new ConcurrentHashMap<>();
        this.entryPoints = ConcurrentHashMap.newKeySet();
        this.dependencies = new ConcurrentHashMap<>();
    }

    public void addClass(ProgramClass programClass) 
    {
        if (programClass == null) {
            throw new IllegalArgumentException("Program class cannot be null");
        }
        
        String className = programClass.getName();
        classes.put(className, programClass);
        
        buildHierarchyInfo(programClass);
        buildDependencyInfo(programClass);
        detectEntryPoints(programClass);
    }

    public void removeClass(String className) 
    {
        classes.remove(className);
        classHierarchy.remove(className);
        interfaceImplementations.remove(className);
        innerClassRelations.remove(className);
        entryPoints.remove(className);
        dependencies.remove(className);
        
        classHierarchy.values().forEach(set -> set.remove(className));
        interfaceImplementations.values().forEach(set -> set.remove(className));
        innerClassRelations.values().forEach(set -> set.remove(className));
        dependencies.values().forEach(set -> set.remove(className));
    }

    public ProgramClass getClass(String className) 
    {
        return classes.get(className);
    }

    public Collection<ProgramClass> getAllClasses() 
    {
        return new ArrayList<>(classes.values());
    }

    public Set<String> getClassNames() 
    {
        return new HashSet<>(classes.keySet());
    }

    public boolean hasClass(String className) 
    {
        return classes.containsKey(className);
    }

    public int getClassCount() 
    {
        return classes.size();
    }

    public Set<String> getSubclasses(String className) 
    {
        return classHierarchy.getOrDefault(className, Collections.emptySet());
    }

    public Set<String> getImplementors(String interfaceName) 
    {
        return interfaceImplementations.getOrDefault(interfaceName, Collections.emptySet());
    }

    public Set<String> getInnerClasses(String outerClassName) 
    {
        return innerClassRelations.getOrDefault(outerClassName, Collections.emptySet());
    }

    public Set<String> getEntryPoints() 
    {
        return new HashSet<>(entryPoints);
    }

    public void addEntryPoint(String className) 
    {
        entryPoints.add(className);
    }

    public void removeEntryPoint(String className) 
    {
        entryPoints.remove(className);
    }

    public Set<String> getDependencies(String className) 
    {
        return dependencies.getOrDefault(className, Collections.emptySet());
    }

    public Set<String> getAllDependencies(String className) 
    {
        Set<String> allDeps = new HashSet<>();
        Set<String> visited = new HashSet<>();
        collectAllDependencies(className, allDeps, visited);
        return allDeps;
    }

    public List<ProgramClass> getClassesInPackage(String packageName) 
    {
        String packagePrefix = packageName.replace('.', '/');
        return classes.values().stream()
            .filter(cls -> cls.getName().startsWith(packagePrefix))
            .collect(Collectors.toList());
    }

    public Set<String> getPackages() 
    {
        return classes.values().stream()
            .map(ProgramClass::getPackageName)
            .filter(pkg -> !pkg.isEmpty())
            .collect(Collectors.toSet());
    }

    public List<ProgramClass> getTopologicallyOrderedClasses() 
    {
        List<ProgramClass> result = new ArrayList<>();
        Set<String> visited = new HashSet<>();
        Set<String> inProgress = new HashSet<>();
        
        for (String className : classes.keySet()) {
            if (!visited.contains(className)) {
                topologicalSort(className, visited, inProgress, result);
            }
        }
        
        return result;
    }

    public boolean isReachableFrom(String fromClass, String toClass) 
    {
        if (fromClass.equals(toClass)) {
            return true;
        }
        
        Set<String> visited = new HashSet<>();
        return isReachableFromRecursive(fromClass, toClass, visited);
    }

    public Map<String, Integer> getClassSizes() 
    {
        Map<String, Integer> sizes = new HashMap<>();
        for (ProgramClass cls : classes.values()) {
            int size = cls.getFields().size() + cls.getMethods().size();
            sizes.put(cls.getName(), size);
        }
        return sizes;
    }

    public List<ProgramMethod> findMethodsWithName(String methodName) 
    {
        return classes.values().stream()
            .flatMap(cls -> cls.getMethods().stream())
            .filter(method -> method.getName().equals(methodName))
            .collect(Collectors.toList());
    }

    public List<ProgramField> findFieldsWithName(String fieldName) 
    {
        return classes.values().stream()
            .flatMap(cls -> cls.getFields().stream())
            .filter(field -> field.getName().equals(fieldName))
            .collect(Collectors.toList());
    }

    public void clear() 
    {
        classes.clear();
        classHierarchy.clear();
        interfaceImplementations.clear();
        innerClassRelations.clear();
        entryPoints.clear();
        dependencies.clear();
    }

    private void buildHierarchyInfo(ProgramClass programClass) 
    {
        String className = programClass.getName();
        String superName = programClass.getSuperName();
        
        if (superName != null) {
            classHierarchy.computeIfAbsent(superName, k -> ConcurrentHashMap.newKeySet()).add(className);
        }
        
        for (String interfaceName : programClass.getInterfaces()) {
            interfaceImplementations.computeIfAbsent(interfaceName, k -> ConcurrentHashMap.newKeySet()).add(className);
        }
        
        if (programClass.isInnerClass()) {
            String outerClassName = getOuterClassName(className);
            if (outerClassName != null) {
                innerClassRelations.computeIfAbsent(outerClassName, k -> ConcurrentHashMap.newKeySet()).add(className);
            }
        }
    }

    private void buildDependencyInfo(ProgramClass programClass) 
    {
        String className = programClass.getName();
        Set<String> classDependencies = ConcurrentHashMap.newKeySet();
        
        if (programClass.getSuperName() != null) {
            classDependencies.add(programClass.getSuperName());
        }
        
        for (String interfaceName : programClass.getInterfaces()) {
            classDependencies.add(interfaceName);
        }
        
        for (ProgramField field : programClass.getFields()) {
            String fieldType = extractClassFromDescriptor(field.getDescriptor());
            if (fieldType != null) {
                classDependencies.add(fieldType);
            }
        }
        
        for (ProgramMethod method : programClass.getMethods()) {
            Set<String> methodDependencies = extractClassesFromMethodDescriptor(method.getDescriptor());
            classDependencies.addAll(methodDependencies);
        }
        
        classDependencies.remove(className);
        if (!classDependencies.isEmpty()) {
            dependencies.put(className, classDependencies);
        }
    }

    private void detectEntryPoints(ProgramClass programClass) 
    {
        String className = programClass.getName();
        
        for (ProgramMethod method : programClass.getMethods()) {
            if (method.getName().equals("main") && 
                method.getDescriptor().equals("([Ljava/lang/String;)V") &&
                method.isStatic() && method.isPublic()) {
                entryPoints.add(className);
                break;
            }
        }
        

    }

    private void collectAllDependencies(String className, Set<String> allDeps, Set<String> visited) 
    {
        if (visited.contains(className)) {
            return;
        }
        visited.add(className);
        
        Set<String> directDeps = dependencies.getOrDefault(className, Collections.emptySet());
        for (String dep : directDeps) {
            if (allDeps.add(dep)) {
                collectAllDependencies(dep, allDeps, visited);
            }
        }
    }

    private void topologicalSort(String className, Set<String> visited, Set<String> inProgress, List<ProgramClass> result) 
    {
        if (inProgress.contains(className)) {
            return;
        }
        if (visited.contains(className)) {
            return;
        }
        
        inProgress.add(className);
        
        Set<String> deps = dependencies.getOrDefault(className, Collections.emptySet());
        for (String dep : deps) {
            if (classes.containsKey(dep)) {
                topologicalSort(dep, visited, inProgress, result);
            }
        }
        
        inProgress.remove(className);
        visited.add(className);
        
        ProgramClass cls = classes.get(className);
        if (cls != null) {
            result.add(cls);
        }
    }

    private boolean isReachableFromRecursive(String fromClass, String toClass, Set<String> visited) 
    {
        if (visited.contains(fromClass)) {
            return false;
        }
        visited.add(fromClass);
        
        Set<String> directDeps = dependencies.getOrDefault(fromClass, Collections.emptySet());
        if (directDeps.contains(toClass)) {
            return true;
        }
        
        for (String dep : directDeps) {
            if (isReachableFromRecursive(dep, toClass, visited)) {
                return true;
            }
        }
        
        return false;
    }

    private String getOuterClassName(String innerClassName) 
    {
        int dollarIndex = innerClassName.lastIndexOf('$');
        if (dollarIndex > 0) {
            return innerClassName.substring(0, dollarIndex);
        }
        return null;
    }

    private String extractClassFromDescriptor(String descriptor) 
    {
        if (descriptor.startsWith("L") && descriptor.endsWith(";")) {
            return descriptor.substring(1, descriptor.length() - 1);
        }
        return null;
    }

    private Set<String> extractClassesFromMethodDescriptor(String descriptor) 
    {
        Set<String> classes = new HashSet<>();
        int paramStart = descriptor.indexOf('(');
        int paramEnd = descriptor.indexOf(')');
        
        if (paramStart >= 0 && paramEnd > paramStart) {
            String params = descriptor.substring(paramStart + 1, paramEnd);
            extractClassesFromTypeDescriptor(params, classes);
            
            String returnType = descriptor.substring(paramEnd + 1);
            extractClassesFromTypeDescriptor(returnType, classes);
        }
        
        return classes;
    }

    private void extractClassesFromTypeDescriptor(String descriptor, Set<String> classes) 
    {
        int i = 0;
        while (i < descriptor.length()) {
            char c = descriptor.charAt(i);
            if (c == 'L') {
                int semicolon = descriptor.indexOf(';', i);
                if (semicolon > i) {
                    String className = descriptor.substring(i + 1, semicolon);
                    classes.add(className);
                    i = semicolon + 1;
                } else {
                    i++;
                }
            } else if (c == '[') {
                i++;
            } else {
                i++;
            }
        }
    }
}