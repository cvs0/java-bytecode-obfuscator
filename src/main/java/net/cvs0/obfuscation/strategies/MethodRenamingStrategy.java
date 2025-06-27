package net.cvs0.obfuscation.strategies;

import net.cvs0.classfile.ProgramClass;
import net.cvs0.classfile.ProgramMethod;
import net.cvs0.config.ObfuscationConfig;
import net.cvs0.core.Program;
import net.cvs0.obfuscation.*;
import net.cvs0.utils.Logger;

import java.util.*;
import java.util.regex.Pattern;

public class MethodRenamingStrategy implements ObfuscationStrategy 
{
    private static final int PRIORITY = 200;
    private NameGenerator nameGenerator;
    private final Set<String> standardMethods = new HashSet<>();

    public MethodRenamingStrategy() 
    {
        initializeStandardMethods();
    }

    @Override
    public void obfuscate(Program program, ObfuscationConfig config, MappingContext mappingContext) throws ObfuscationException 
    {
        if (config.isVerbose()) {
            Logger.info("Starting method renaming obfuscation...");
        }

        nameGenerator = NameGenerator.createGenerator(config.getNamingMode());
        initializeKeepRules(program, config, mappingContext);

        Map<String, List<MethodInfo>> methodGroups = groupMethodsBySignature(program, config, mappingContext);
        renameMethodGroups(methodGroups, mappingContext, config);

        if (config.isVerbose()) {
            Logger.info("Method renaming completed. Renamed " + 
                      mappingContext.getAllMethodMappings().size() + " methods");
        }
    }

    private void initializeStandardMethods() 
    {
        standardMethods.addAll(Arrays.asList(
            "<init>", "<clinit>", "main", "toString", "equals", "hashCode", "clone",
            "finalize", "wait", "notify", "notifyAll", "getClass"
        ));
    }

    private void initializeKeepRules(Program program, ObfuscationConfig config, MappingContext mappingContext) 
    {
        for (String keepMethod : config.getKeepMethods()) {
            String[] parts = keepMethod.split("\\.");
            if (parts.length >= 2) {
                String className = parts[0];
                String methodSignature = parts[1];
                
                int descriptorIndex = methodSignature.indexOf('(');
                if (descriptorIndex > 0) {
                    String methodName = methodSignature.substring(0, descriptorIndex);
                    String descriptor = methodSignature.substring(descriptorIndex);
                    mappingContext.addKeepMethod(className, methodName, descriptor);
                    nameGenerator.addReservedName(methodName);
                }
            }
        }

        if (config.isKeepStandardEntryPoints()) {
            for (ProgramClass cls : program.getAllClasses()) {
                for (ProgramMethod method : cls.getMethods()) {
                    if (isStandardEntryPoint(method)) {
                        mappingContext.addKeepMethod(cls.getName(), method.getName(), method.getDescriptor());
                        nameGenerator.addReservedName(method.getName());
                    }
                }
            }
        }

        for (String pattern : config.getKeepMethodsInClassPatterns()) {
            Pattern regex = Pattern.compile(pattern);
            for (ProgramClass cls : program.getAllClasses()) {
                if (regex.matcher(cls.getName()).matches()) {
                    for (ProgramMethod method : cls.getMethods()) {
                        mappingContext.addKeepMethod(cls.getName(), method.getName(), method.getDescriptor());
                        nameGenerator.addReservedName(method.getName());
                    }
                }
            }
        }

        for (String customMapping : config.getCustomMappings().keySet()) {
            if (customMapping.contains(".") && customMapping.contains("(")) {
                String[] parts = customMapping.split("\\.", 2);
                String className = parts[0];
                String methodSignature = parts[1];
                
                int descriptorIndex = methodSignature.indexOf('(');
                if (descriptorIndex > 0) {
                    String methodName = methodSignature.substring(0, descriptorIndex);
                    String descriptor = methodSignature.substring(descriptorIndex);
                    String obfuscatedName = config.getCustomMappings().get(customMapping);
                    
                    mappingContext.mapMethod(className, methodName, descriptor, obfuscatedName);
                    nameGenerator.addReservedName(obfuscatedName);
                }
            }
        }
    }

    private Map<String, List<MethodInfo>> groupMethodsBySignature(Program program, ObfuscationConfig config, MappingContext mappingContext) 
    {
        Map<String, List<MethodInfo>> methodGroups = new HashMap<>();

        for (ProgramClass cls : program.getAllClasses()) {
            for (ProgramMethod method : cls.getMethods()) {
                if (shouldRenameMethod(cls, method, config, mappingContext)) {
                    String groupKey = createMethodGroupKey(method);
                    MethodInfo methodInfo = new MethodInfo(cls.getName(), method);
                    
                    methodGroups.computeIfAbsent(groupKey, k -> new ArrayList<>()).add(methodInfo);
                }
            }
        }

        return methodGroups;
    }

    private boolean shouldRenameMethod(ProgramClass cls, ProgramMethod method, ObfuscationConfig config, MappingContext mappingContext) 
    {
        String className = cls.getName();
        String methodName = method.getName();
        String descriptor = method.getDescriptor();

        if (mappingContext.shouldKeepMethod(className, methodName, descriptor)) {
            return false;
        }

        if (mappingContext.hasMethodMapping(className, methodName, descriptor)) {
            return false;
        }

        if (standardMethods.contains(methodName)) {
            return false;
        }

        if (method.isNative()) {
            return false;
        }

        if (methodName.startsWith("lambda$")) {
            return config.getObfuscationLevel().ordinal() >= 3;
        }

        if (method.isSynthetic() && config.getObfuscationLevel().ordinal() < 2) {
            return false;
        }

        if (isOverridingLibraryMethod(cls, method)) {
            return false;
        }

        if (isInterfaceMethod(cls, method) && hasImplementorsOutsideProgram(cls, method)) {
            return false;
        }

        if (!config.getIncludePackages().isEmpty()) {
            boolean included = false;
            for (String includePackage : config.getIncludePackages()) {
                String packagePrefix = includePackage.replace('.', '/');
                if (className.startsWith(packagePrefix)) {
                    included = true;
                    break;
                }
            }
            if (!included) {
                return false;
            }
        }

        for (String excludePackage : config.getExcludePackages()) {
            String packagePrefix = excludePackage.replace('.', '/');
            if (className.startsWith(packagePrefix)) {
                return false;
            }
        }

        if (config.isStayInScope()) {
            String scopePrefix = config.getScopePrefix();
            if (scopePrefix != null && !className.startsWith(scopePrefix + "/")) {
                return false;
            }
        }

        return true;
    }

    private boolean isStandardEntryPoint(ProgramMethod method) 
    {
        if (method.getName().equals("main") && 
            method.getDescriptor().equals("([Ljava/lang/String;)V") &&
            method.isStatic() && method.isPublic()) {
            return true;
        }

        if (method.getName().equals("<init>") && method.isPublic()) {
            return true;
        }

        return false;
    }

    private boolean isOverridingLibraryMethod(ProgramClass cls, ProgramMethod method) 
    {
        if (cls.getSuperName() != null && (
            cls.getSuperName().startsWith("java/") || 
            cls.getSuperName().startsWith("javax/") ||
            cls.getSuperName().startsWith("android/"))) {
            return true;
        }

        for (String interfaceName : cls.getInterfaces()) {
            if (interfaceName.startsWith("java/") || 
                interfaceName.startsWith("javax/") ||
                interfaceName.startsWith("android/")) {
                return true;
            }
        }

        return false;
    }

    private boolean isInterfaceMethod(ProgramClass cls, ProgramMethod method) 
    {
        return cls.isInterface() && !method.isStatic() && !method.isPrivate();
    }

    private boolean hasImplementorsOutsideProgram(ProgramClass cls, ProgramMethod method) 
    {
        return false;
    }

    private String createMethodGroupKey(ProgramMethod method) 
    {
        return method.getName() + ":" + method.getDescriptor();
    }

    private void renameMethodGroups(Map<String, List<MethodInfo>> methodGroups, MappingContext mappingContext, ObfuscationConfig config) 
    {
        for (Map.Entry<String, List<MethodInfo>> entry : methodGroups.entrySet()) {
            String groupKey = entry.getKey();
            List<MethodInfo> methods = entry.getValue();

            if (methods.isEmpty()) {
                continue;
            }

            String obfuscatedName = nameGenerator.generateMethodName();
            
            for (MethodInfo methodInfo : methods) {
                mappingContext.mapMethod(methodInfo.className, methodInfo.method.getName(), 
                                       methodInfo.method.getDescriptor(), obfuscatedName);
                
                if (config.isVerbose()) {
                    Logger.debug("Mapped method: " + methodInfo.className + "." + 
                               methodInfo.method.getName() + " -> " + obfuscatedName);
                }
            }
        }
    }

    @Override
    public String getName() 
    {
        return "Method Renaming";
    }

    @Override
    public boolean isEnabled(ObfuscationConfig config) 
    {
        return config.isRenameMethods();
    }

    @Override
    public int getPriority() 
    {
        return PRIORITY;
    }

    private static class MethodInfo 
    {
        final String className;
        final ProgramMethod method;

        MethodInfo(String className, ProgramMethod method) 
        {
            this.className = className;
            this.method = method;
        }
    }
}