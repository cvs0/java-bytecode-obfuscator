package net.cvs0.obfuscation.strategies;

import net.cvs0.classfile.ProgramClass;
import net.cvs0.config.ObfuscationConfig;
import net.cvs0.core.Program;
import net.cvs0.obfuscation.*;
import net.cvs0.utils.Logger;

import java.util.*;
import java.util.regex.Pattern;

public class ClassRenamingStrategy implements ObfuscationStrategy 
{
    private static final int PRIORITY = 100;
    private NameGenerator nameGenerator;

    @Override
    public void obfuscate(Program program, ObfuscationConfig config, MappingContext mappingContext) throws ObfuscationException 
    {
        if (config.isVerbose()) {
            Logger.info("Starting class renaming obfuscation...");
        }

        nameGenerator = NameGenerator.createGenerator(config.getNamingMode());
        initializeKeepRules(program, config, mappingContext);

        List<ProgramClass> classesToRename = identifyClassesToRename(program, config, mappingContext);
        
        if (config.isVerbose()) {
            Logger.info("Found " + classesToRename.size() + " classes to rename");
        }

        Map<String, String> packageMappings = createPackageMappings(classesToRename, config);
        renameClasses(classesToRename, packageMappings, mappingContext, config);

        if (config.isVerbose()) {
            Logger.info("Class renaming completed. Renamed " + 
                      mappingContext.getAllClassMappings().size() + " classes");
        }
    }

    private void initializeKeepRules(Program program, ObfuscationConfig config, MappingContext mappingContext) 
    {
        if (config.isVerbose()) {
            Logger.debug("Initializing keep rules...");
        }

        for (String keepClass : config.getKeepClasses()) {
            mappingContext.addKeepClass(keepClass);
            nameGenerator.addReservedName(keepClass);
            if (config.isVerbose()) {
                Logger.debug("Added explicit keep class: " + keepClass);
            }
        }

        for (String pattern : config.getKeepClassPatterns()) {
            try {
                Pattern regex = Pattern.compile(pattern);
                int matchCount = 0;
                for (ProgramClass cls : program.getAllClasses()) {
                    String className = cls.getName();
                    if (regex.matcher(className).matches()) {
                        mappingContext.addKeepClass(className);
                        nameGenerator.addReservedName(className);
                        matchCount++;
                    }
                }
                Logger.info("Pattern '" + pattern + "' matched " + matchCount + " classes");
            } catch (Exception e) {
                Logger.info("Invalid regex pattern '" + pattern + "': " + e.getMessage());
            }
        }

        if (config.isKeepMainClass() && config.getMainClass() != null) {
            mappingContext.addKeepClass(config.getMainClass());
            nameGenerator.addReservedName(config.getMainClass());
            if (config.isVerbose()) {
                Logger.debug("Added keep main class: " + config.getMainClass());
            }
        }

        if (config.isKeepStandardEntryPoints()) {
            if (config.isVerbose()) {
                Logger.debug("Adding standard entry points to keep list...");
            }
            for (String entryPoint : program.getEntryPoints()) {
                mappingContext.addKeepClass(entryPoint);
                nameGenerator.addReservedName(entryPoint);
                if (config.isVerbose()) {
                    Logger.debug("Added entry point: " + entryPoint);
                }
            }
        }

        for (String customMapping : config.getCustomMappings().keySet()) {
            if (program.hasClass(customMapping)) {
                String obfuscatedName = config.getCustomMappings().get(customMapping);
                mappingContext.mapClass(customMapping, obfuscatedName);
                nameGenerator.addReservedName(obfuscatedName);
                if (config.isVerbose()) {
                    Logger.debug("Added custom mapping: " + customMapping + " -> " + obfuscatedName);
                }
            }
        }

        if (config.isVerbose()) {
            Logger.debug("Keep rules initialization completed");
        }
    }

    private List<ProgramClass> identifyClassesToRename(Program program, ObfuscationConfig config, MappingContext mappingContext) 
    {
        List<ProgramClass> classesToRename = new ArrayList<>();
        int totalClasses = 0;

        for (ProgramClass cls : program.getAllClasses()) {
            totalClasses++;
            
            if (shouldRenameClass(cls, config, mappingContext)) {
                classesToRename.add(cls);
            }
        }

        if (config.isVerbose()) {
            Logger.debug("Total classes in program: " + totalClasses);
        }

        classesToRename.sort((c1, c2) -> {
            int depthCompare = Integer.compare(getClassDepth(c1.getName()), getClassDepth(c2.getName()));
            if (depthCompare != 0) {
                return depthCompare;
            }
            return c1.getName().compareTo(c2.getName());
        });

        if (config.isVerbose()) {
            Logger.debug("Identified " + classesToRename.size() + " classes for renaming");
            for (ProgramClass cls : classesToRename) {
                Logger.debug("  - " + cls.getName());
            }
        }

        return classesToRename;
    }

    private boolean shouldRenameClass(ProgramClass cls, ObfuscationConfig config, MappingContext mappingContext) 
    {
        String className = cls.getName();

        if (mappingContext.shouldKeepClass(className)) {
            return false;
        }

        if (mappingContext.hasClassMapping(className)) {
            return false;
        }

        if (className.startsWith("java/") || className.startsWith("javax/") || 
            className.startsWith("sun/") || className.startsWith("com/sun/")) {
            return false;
        }

        if (isThirdPartyLibrary(className)) {
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
                if (config.isVerbose()) {
                    Logger.debug("  -> Skipping: not in include packages");
                }
                return false;
            }
        }

        for (String excludePackage : config.getExcludePackages()) {
            String packagePrefix = excludePackage.replace('.', '/');
            if (className.startsWith(packagePrefix)) {
                if (config.isVerbose()) {
                    Logger.debug("  -> Skipping: in exclude packages");
                }
                return false;
            }
        }

        if (cls.isAnnotation() && config.getObfuscationLevel().ordinal() < 2) {
            if (config.isVerbose()) {
                Logger.debug("  -> Skipping: annotation at low obfuscation level");
            }
            return false;
        }

        if (cls.getName().contains("$") && cls.isAnonymousClass()) {
            boolean shouldRename = config.getObfuscationLevel().ordinal() >= 2;
            if (config.isVerbose()) {
                Logger.debug("  -> Anonymous class, should rename: " + shouldRename);
            }
            return shouldRename;
        }

        if (config.isStayInScope()) {
            String scopePrefix = config.getScopePrefix();
            if (scopePrefix != null && !className.startsWith(scopePrefix + "/")) {
                if (config.isVerbose()) {
                    Logger.debug("  -> Skipping: outside scope (" + scopePrefix + ")");
                }
                return false;
            }
        }

        if (config.isVerbose()) {
            Logger.debug("  -> Will rename class: " + className);
        }
        return true;
    }

    private Map<String, String> createPackageMappings(List<ProgramClass> classesToRename, ObfuscationConfig config) 
    {
        Map<String, String> packageMappings = new HashMap<>();
        Set<String> packages = new HashSet<>();

        for (ProgramClass cls : classesToRename) {
            String packageName = cls.getPackageName();
            if (!packageName.isEmpty()) {
                packages.add(packageName);
            }
        }

        for (String packageName : packages) {
            if (!packageMappings.containsKey(packageName)) {
                String obfuscatedPackage = generateObfuscatedPackageName(packageName, config);
                packageMappings.put(packageName, obfuscatedPackage);
                
                if (config.isVerbose()) {
                    Logger.debug("Package mapping: " + packageName + " -> " + obfuscatedPackage);
                }
            }
        }

        return packageMappings;
    }

    private String generateObfuscatedPackageName(String originalPackage, ObfuscationConfig config) 
    {
        if (config.getObfuscationLevel().ordinal() < 2) {
            return originalPackage;
        }

        String[] parts = originalPackage.split("\\.");
        StringBuilder obfuscatedPackage = new StringBuilder();

        for (int i = 0; i < parts.length; i++) {
            if (i > 0) {
                obfuscatedPackage.append(".");
            }
            obfuscatedPackage.append(nameGenerator.generateClassName().toLowerCase());
        }

        return obfuscatedPackage.toString();
    }

    private void renameClasses(List<ProgramClass> classesToRename, Map<String, String> packageMappings, 
                              MappingContext mappingContext, ObfuscationConfig config) 
    {
        for (ProgramClass cls : classesToRename) {
            String originalName = cls.getName();
            String obfuscatedName = generateObfuscatedClassName(cls, packageMappings, config);
            
            mappingContext.mapClass(originalName, obfuscatedName);
            
            if (config.isVerbose()) {
                Logger.debug("Mapped class: " + originalName + " -> " + obfuscatedName);
            }
        }
    }

    private String generateObfuscatedClassName(ProgramClass cls, Map<String, String> packageMappings, ObfuscationConfig config) 
    {
        String originalName = cls.getName();
        String packageName = cls.getPackageName();
        String simpleName = cls.getSimpleName();

        String obfuscatedPackage = packageMappings.getOrDefault(packageName, packageName);
        String obfuscatedSimpleName;

        if (cls.isInnerClass()) {
            obfuscatedSimpleName = generateInnerClassName(originalName, config);
        } else {
            obfuscatedSimpleName = nameGenerator.generateClassName();
        }

        String result;
        if (obfuscatedPackage.isEmpty()) {
            result = obfuscatedSimpleName;
        } else {
            result = obfuscatedPackage.replace('.', '/') + "/" + obfuscatedSimpleName;
        }

        if (config.isVerbose()) {
            Logger.debug("Generated obfuscated name for " + originalName + ": " + result + 
                        " (package: " + packageName + " -> " + obfuscatedPackage + 
                        ", simple: " + simpleName + " -> " + obfuscatedSimpleName + ")");
        }

        return result;
    }

    private String generateInnerClassName(String originalName, ObfuscationConfig config) 
    {
        if (config.getObfuscationLevel().ordinal() < 2) {
            int lastDollar = originalName.lastIndexOf('$');
            if (lastDollar > 0) {
                String innerPart = originalName.substring(lastDollar + 1);
                if (innerPart.matches("\\d+")) {
                    return nameGenerator.generateClassName() + "$" + innerPart;
                }
            }
        }
        
        return nameGenerator.generateClassName();
    }

    private int getClassDepth(String className) 
    {
        return (int) className.chars().filter(ch -> ch == '/').count();
    }

    private boolean isThirdPartyLibrary(String className) 
    {
        return className.startsWith("com/fasterxml/jackson/") ||
               className.startsWith("org/objectweb/asm/") ||
               className.startsWith("picocli/") ||
               className.startsWith("net/fabricmc/") ||
               className.startsWith("org/apache/") ||
               className.startsWith("org/slf4j/") ||
               className.startsWith("ch/qos/logback/") ||
               className.startsWith("org/springframework/") ||
               className.startsWith("com/google/") ||
               className.startsWith("org/junit/") ||
               className.startsWith("org/hamcrest/") ||
               className.startsWith("org/mockito/") ||
               className.startsWith("kotlin/") ||
               className.startsWith("kotlinx/") ||
               className.startsWith("scala/") ||
               className.startsWith("akka/") ||
               className.startsWith("com/typesafe/") ||
               className.startsWith("org/jetbrains/") ||
               className.startsWith("META-INF/");
    }

    @Override
    public String getName() 
    {
        return "Class Renaming";
    }

    @Override
    public boolean isEnabled(ObfuscationConfig config) 
    {
        return config.isRenameClasses();
    }

    @Override
    public int getPriority() 
    {
        return PRIORITY;
    }
}