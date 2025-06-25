package net.cvs0.obfuscation.strategies;

import net.cvs0.classfile.*;
import net.cvs0.config.ObfuscationConfig;
import net.cvs0.core.Program;
import net.cvs0.obfuscation.*;
import net.cvs0.utils.Logger;

import java.util.*;

public class SyntheticMemberStrategy implements ObfuscationStrategy 
{
    private static final int PRIORITY = 500;
    private NameGenerator nameGenerator;
    private final Random random = new Random();

    @Override
    public void obfuscate(Program program, ObfuscationConfig config, MappingContext mappingContext) throws ObfuscationException 
    {
        Logger.info("Starting synthetic member addition obfuscation...");

        nameGenerator = NameGenerator.createGenerator(config.getNamingMode());

        int processedClasses = 0;
        int addedFields = 0;
        int addedMethods = 0;

        for (ProgramClass cls : program.getAllClasses()) {
            if (shouldProcessClass(cls, config, mappingContext)) {
                processedClasses++;
                
                int fieldsAdded = addSyntheticFields(cls, config);
                int methodsAdded = addSyntheticMethods(cls, config);
                
                addedFields += fieldsAdded;
                addedMethods += methodsAdded;

                if (config.isVerbose() && (fieldsAdded > 0 || methodsAdded > 0)) {
                    Logger.debug("Added " + fieldsAdded + " synthetic fields and " + 
                               methodsAdded + " synthetic methods to class " + cls.getName());
                }
            }
        }

        Logger.info("Synthetic member addition completed. Processed " + processedClasses + 
                   " classes, added " + addedFields + " synthetic fields and " + 
                   addedMethods + " synthetic methods");
    }

    private boolean shouldProcessClass(ProgramClass cls, ObfuscationConfig config, MappingContext mappingContext) 
    {
        String className = cls.getName();

        if (mappingContext.shouldKeepClass(className)) {
            return false;
        }

        if (className.startsWith("java/") || className.startsWith("javax/") || 
            className.startsWith("sun/") || className.startsWith("com/sun/")) {
            return false;
        }

        if (isThirdPartyLibrary(className)) {
            return false;
        }

        if (cls.isInterface() || cls.isAnnotation()) {
            return false;
        }

        if (config.isStayInScope()) {
            String scopePrefix = config.getScopePrefix();
            if (scopePrefix != null && !className.startsWith(scopePrefix + "/")) {
                return false;
            }
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

        return true;
    }

    private int addSyntheticFields(ProgramClass cls, ObfuscationConfig config) 
    {
        int fieldsToAdd = random.nextInt(3) + 1;
        int addedFields = 0;

        for (int i = 0; i < fieldsToAdd; i++) {
            String fieldName = generateSyntheticFieldName();
            String descriptor = generateRandomFieldDescriptor();
            
            if (!hasField(cls, fieldName)) {
                int access = JavaConstants.ACC_PRIVATE | JavaConstants.ACC_SYNTHETIC;
                if (random.nextBoolean()) {
                    access |= JavaConstants.ACC_STATIC;
                }
                if (random.nextBoolean()) {
                    access |= JavaConstants.ACC_FINAL;
                }

                Object defaultValue = generateDefaultValue(descriptor);
                ProgramField syntheticField = new ProgramField(access, fieldName, descriptor, null, defaultValue);
                syntheticField.setOwnerClass(cls);
                
                cls.addField(syntheticField);
                addedFields++;
            }
        }

        return addedFields;
    }

    private int addSyntheticMethods(ProgramClass cls, ObfuscationConfig config) 
    {
        int methodsToAdd = random.nextInt(2) + 1;
        int addedMethods = 0;

        for (int i = 0; i < methodsToAdd; i++) {
            String methodName = generateSyntheticMethodName();
            String descriptor = generateRandomMethodDescriptor();
            
            if (!hasMethod(cls, methodName, descriptor)) {
                int access = JavaConstants.ACC_PRIVATE | JavaConstants.ACC_SYNTHETIC;
                if (random.nextBoolean()) {
                    access |= JavaConstants.ACC_STATIC;
                }

                ProgramMethod syntheticMethod = new ProgramMethod(access, methodName, descriptor, null, null);
                syntheticMethod.setOwnerClass(cls);
                syntheticMethod.setHasCode(true);
                
                addDummyMethodBody(syntheticMethod, descriptor);
                
                cls.addMethod(syntheticMethod);
                addedMethods++;
            }
        }

        return addedMethods;
    }

    private String generateSyntheticFieldName() 
    {
        return nameGenerator.generateFieldName();
    }

    private String generateSyntheticMethodName() 
    {
        return nameGenerator.generateMethodName();
    }

    private String generateRandomFieldDescriptor() 
    {
        String[] descriptors = {
            "I",           // int
            "J",           // long
            "Z",           // boolean
            "Ljava/lang/String;",  // String
            "Ljava/lang/Object;",  // Object
            "[I",          // int[]
            "[Ljava/lang/String;"  // String[]
        };
        return descriptors[random.nextInt(descriptors.length)];
    }

    private String generateRandomMethodDescriptor() 
    {
        String[] returnTypes = {"V", "I", "Z", "Ljava/lang/String;", "Ljava/lang/Object;"};
        String[] paramTypes = {"", "I", "Ljava/lang/String;", "ILjava/lang/String;"};
        
        String params = paramTypes[random.nextInt(paramTypes.length)];
        String returnType = returnTypes[random.nextInt(returnTypes.length)];
        
        return "(" + params + ")" + returnType;
    }

    private Object generateDefaultValue(String descriptor) 
    {
        switch (descriptor) {
            case "I":
                return random.nextInt(1000);
            case "J":
                return (long) random.nextInt(1000);
            case "Z":
                return random.nextBoolean();
            case "Ljava/lang/String;":
                return generateRandomString();
            default:
                return null;
        }
    }

    private String generateRandomString() 
    {
        String[] strings = {
            "synthetic", "dummy", "placeholder", "temp", "buffer", 
            "cache", "helper", "util", "data", "info"
        };
        return strings[random.nextInt(strings.length)] + random.nextInt(100);
    }

    private void addDummyMethodBody(ProgramMethod method, String descriptor) 
    {
        CodeAttribute codeAttr = new CodeAttribute();
        codeAttr.setMaxStack(2);
        codeAttr.setMaxLocals(getLocalVariableCount(descriptor, method.isStatic()));
        
        byte[] bytecode = generateReturnBytecode(getReturnType(descriptor));
        codeAttr.setCode(bytecode);
        
        method.addAttribute(codeAttr);
    }

    private byte[] generateReturnBytecode(String returnType) 
    {
        switch (returnType) {
            case "V":
                return new byte[]{(byte) 177}; // RETURN
            case "I":
            case "Z":
                return new byte[]{(byte) 3, (byte) 172}; // ICONST_0, IRETURN
            case "J":
                return new byte[]{(byte) 9, (byte) 173}; // LCONST_0, LRETURN
            default:
                return new byte[]{(byte) 1, (byte) 176}; // ACONST_NULL, ARETURN
        }
    }

    private String getReturnType(String descriptor) 
    {
        int parenIndex = descriptor.indexOf(')');
        return descriptor.substring(parenIndex + 1);
    }

    private int getLocalVariableCount(String descriptor, boolean isStatic) 
    {
        int count = isStatic ? 0 : 1; // 'this' parameter for non-static methods
        
        String params = descriptor.substring(1, descriptor.indexOf(')'));
        for (int i = 0; i < params.length(); i++) {
            char c = params.charAt(i);
            if (c == 'L') {
                while (params.charAt(i) != ';') i++;
                count++;
            } else if (c == '[') {
                while (params.charAt(i) == '[') i++;
                if (params.charAt(i) == 'L') {
                    while (params.charAt(i) != ';') i++;
                }
                count++;
            } else if (c == 'J' || c == 'D') {
                count += 2; // long and double take 2 slots
            } else if (c != ')') {
                count++;
            }
        }
        
        return count;
    }

    private boolean hasField(ProgramClass cls, String fieldName) 
    {
        for (ProgramField field : cls.getFields()) {
            if (field.getName().equals(fieldName)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasMethod(ProgramClass cls, String methodName, String descriptor) 
    {
        for (ProgramMethod method : cls.getMethods()) {
            if (method.getName().equals(methodName) && method.getDescriptor().equals(descriptor)) {
                return true;
            }
        }
        return false;
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
               className.startsWith("org/mockito/");
    }

    @Override
    public int getPriority() 
    {
        return PRIORITY;
    }

    @Override
    public String getName() 
    {
        return "Synthetic Member Addition";
    }

    @Override
    public boolean isEnabled(ObfuscationConfig config) 
    {
        boolean enabled = config.isAddSyntheticMembers();
        Logger.debug("SyntheticMemberStrategy.isEnabled() = " + enabled);
        return enabled;
    }
}