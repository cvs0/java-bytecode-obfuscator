package net.cvs0.utils;

import org.objectweb.asm.Opcodes;

public class TransformerUtils
{
    public static boolean isEnumConstant(int access, String name, boolean isEnum)
    {
        return isEnum && (access & Opcodes.ACC_ENUM) != 0;
    }
    
    public static boolean isEnumSyntheticField(String name)
    {
        return name.equals("$VALUES") || name.equals("ENUM$VALUES");
    }
    
    public static boolean isCompilerGeneratedField(String name)
    {
        return name.startsWith("$assertionsDisabled") || 
               name.startsWith("$switch") ||
               name.equals("serialVersionUID") ||
               name.equals("TYPE") ||
               name.equals("class");
    }
    
    public static boolean isSyntheticInnerClassField(int access, String name)
    {
        return (access & Opcodes.ACC_SYNTHETIC) != 0 && 
               (name.startsWith("this$") || name.startsWith("val$"));
    }
    
    public static boolean isSpecialMethod(String name)
    {
        return name.equals("<init>") || 
               name.equals("<clinit>") || 
               name.equals("main") ||
               name.startsWith("lambda$");
    }
    
    public static boolean isEnumMethod(String name)
    {
        return name.equals("values") || 
               name.equals("valueOf") || 
               name.equals("$values");
    }
    
    public static boolean isSyntheticOrBridge(int access)
    {
        return (access & Opcodes.ACC_SYNTHETIC) != 0 || 
               (access & Opcodes.ACC_BRIDGE) != 0;
    }
    
    public static boolean isMainMethod(String name, int access, String descriptor)
    {
        return name.equals("main") && 
               (access & Opcodes.ACC_STATIC) != 0 && 
               "([Ljava/lang/String;)V".equals(descriptor);
    }
    
    public static boolean isValidClassName(String className)
    {
        return className != null && 
               !className.isEmpty() && 
               className.length() <= 1000;
    }
    
    public static boolean isValidFieldName(String fieldName)
    {
        return fieldName != null && 
               !fieldName.isEmpty() && 
               fieldName.length() <= 255;
    }
    
    public static boolean isValidMethodName(String methodName)
    {
        return methodName != null && 
               !methodName.isEmpty() && 
               methodName.length() <= 255;
    }
}