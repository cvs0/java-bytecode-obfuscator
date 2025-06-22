package net.cvs0.transformers;

import net.cvs0.context.ObfuscationContext;
import net.cvs0.core.BaseTransformer;
import org.objectweb.asm.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class FakeInterfaceTransformer extends BaseTransformer
{
    private final Set<String> generatedInterfaces = new HashSet<>();
    private final Random random = new Random();
    private static final String GENERATED_INTERFACES_KEY = "fakeInterfaceTransformer.generatedInterfaces";
    
    public FakeInterfaceTransformer()
    {
        super("FakeInterface", 50);
    }
    
    @Override
    public boolean isEnabled(ObfuscationContext context)
    {
        return context.getConfig().isFloodFakeInterfaces();
    }
    
    @Override
    protected boolean shouldSkipClass(String className, ObfuscationContext context)
    {
        if (super.shouldSkipClass(className, context)) {
            return true;
        }
        
        return className.startsWith("java/") || 
               className.startsWith("javax/") || 
               className.startsWith("sun/") ||
               className.startsWith("com/sun/") ||
               className.endsWith("$") ||
               className.contains("$$");
    }
    
    @Override
    protected void performTransformation(ClassReader reader, ClassWriter writer, 
                                       ObfuscationContext context, String className)
    {
        int access = reader.getAccess();
        
        if ((access & Opcodes.ACC_INTERFACE) != 0 || 
            (access & Opcodes.ACC_ANNOTATION) != 0 ||
            (access & Opcodes.ACC_ENUM) != 0) {
            copyClass(reader, writer);
            return;
        }
        
        String[] originalInterfaces = reader.getInterfaces();
        List<String> fakeInterfaces = generateFakeInterfaces(context, className);
        
        if (fakeInterfaces.isEmpty()) {
            copyClass(reader, writer);
            return;
        }
        
        List<String> allInterfaces = new ArrayList<>();
        if (originalInterfaces != null) {
            allInterfaces.addAll(Arrays.asList(originalInterfaces));
        }
        allInterfaces.addAll(fakeInterfaces);
        
        String[] interfaceArray = allInterfaces.toArray(new String[0]);
        
        logTransformation("Adding " + fakeInterfaces.size() + " fake interfaces to class: " + className, context);
        
        ClassVisitor cv = new FakeInterfaceClassVisitor(writer, interfaceArray, fakeInterfaces);
        reader.accept(cv, 0);
        
        createAndStoreFakeInterfaces(context, fakeInterfaces);
    }
    
    private List<String> generateFakeInterfaces(ObfuscationContext context, String baseClassName)
    {
        List<String> interfaces = new ArrayList<>();
        int count = context.getConfig().getFakeInterfaceCount();
        
        for (int i = 0; i < count; i++) {
            String interfaceName = generateInterfaceName(baseClassName, i);
            if (!generatedInterfaces.contains(interfaceName)) {
                interfaces.add(interfaceName);
                generatedInterfaces.add(interfaceName);
            }
        }
        
        return interfaces;
    }
    
    private String generateInterfaceName(String baseClassName, int index)
    {
        String[] namingOptions = {
            "Serializable", "Cloneable", "Comparable", "Iterable", "Collection",
            "Map", "Set", "List", "Queue", "Deque", "Observer", "EventListener",
            "Runnable", "Callable", "Future", "Component", "Service", "Handler",
            "Manager", "Controller", "Repository", "Entity", "Model", "View",
            "Processor", "Builder", "Factory", "Strategy", "Command", "State",
            "Chain", "Visitor", "Template", "Bridge", "Adapter", "Decorator",
            "Facade", "Proxy", "Observer", "Mediator", "Memento", "Iterator"
        };
        
        String packagePath = extractPackagePath(baseClassName);
        String interfaceBaseName = namingOptions[random.nextInt(namingOptions.length)];
        
        if (random.nextBoolean()) {
            interfaceBaseName += generateRandomSuffix();
        }
        
        return packagePath + "I" + interfaceBaseName + (index > 0 ? index : "");
    }
    
    private String extractPackagePath(String className)
    {
        int lastSlash = className.lastIndexOf('/');
        if (lastSlash == -1) {
            return "";
        }
        return className.substring(0, lastSlash + 1);
    }
    
    private String generateRandomSuffix()
    {
        String[] suffixes = {"Impl", "Base", "Core", "Ext", "Helper", "Util", "Support"};
        return suffixes[random.nextInt(suffixes.length)];
    }
    
    private void createAndStoreFakeInterfaces(ObfuscationContext context, List<String> interfaceNames)
    {
        Map<String, byte[]> generatedInterfacesMap = getOrCreateGeneratedInterfacesMap(context);
        
        for (String interfaceName : interfaceNames) {
            if (!generatedInterfacesMap.containsKey(interfaceName)) {
                byte[] interfaceBytes = createFakeInterface(interfaceName);
                generatedInterfacesMap.put(interfaceName, interfaceBytes);
                logTransformation("Generated fake interface: " + interfaceName, context);
            }
        }
    }
    
    @SuppressWarnings("unchecked")
    private Map<String, byte[]> getOrCreateGeneratedInterfacesMap(ObfuscationContext context)
    {
        Map<String, byte[]> map = (Map<String, byte[]>) context.getProperty(GENERATED_INTERFACES_KEY);
        if (map == null) {
            map = new ConcurrentHashMap<>();
            context.setProperty(GENERATED_INTERFACES_KEY, map);
        }
        return map;
    }
    
    private byte[] createFakeInterface(String interfaceName)
    {
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        
        cw.visit(Opcodes.V1_8, 
                Opcodes.ACC_PUBLIC | Opcodes.ACC_INTERFACE | Opcodes.ACC_ABSTRACT,
                interfaceName, 
                null, 
                "java/lang/Object", 
                null);
        
        addFakeInterfaceMethods(cw);
        
        cw.visitEnd();
        
        return cw.toByteArray();
    }
    
    private void addFakeInterfaceMethods(ClassWriter cw)
    {
        int methodCount = 1 + random.nextInt(3);
        
        for (int i = 0; i < methodCount; i++) {
            String methodName = generateMethodName(i);
            String descriptor = generateMethodDescriptor();
            
            MethodVisitor mv = cw.visitMethod(
                Opcodes.ACC_PUBLIC | Opcodes.ACC_ABSTRACT,
                methodName,
                descriptor,
                null,
                null
            );
            mv.visitEnd();
        }
    }
    
    private String generateMethodName(int index)
    {
        String[] methodNames = {
            "process", "handle", "execute", "perform", "validate", "initialize",
            "configure", "setup", "cleanup", "finalize", "update", "refresh",
            "reset", "start", "stop", "pause", "resume", "check", "verify"
        };
        
        String baseName = methodNames[random.nextInt(methodNames.length)];
        if (index > 0 || random.nextBoolean()) {
            baseName += (index + 1);
        }
        
        return baseName;
    }
    
    private String generateMethodDescriptor()
    {
        String[] descriptors = {
            "()V", "()Z", "()I", "()J", "()Ljava/lang/String;",
            "(I)V", "(Z)V", "(Ljava/lang/String;)V", "(Ljava/lang/Object;)V",
            "(I)I", "(Ljava/lang/String;)Ljava/lang/String;", "(Ljava/lang/Object;)Z"
        };
        
        return descriptors[random.nextInt(descriptors.length)];
    }
    


    
    private static class FakeInterfaceClassVisitor extends ClassVisitor
    {
        private final String[] allInterfaces;
        private final List<String> fakeInterfaces;
        private final Set<String> implementedMethods = new HashSet<>();
        
        public FakeInterfaceClassVisitor(ClassVisitor cv, String[] allInterfaces, List<String> fakeInterfaces)
        {
            super(Opcodes.ASM9, cv);
            this.allInterfaces = allInterfaces;
            this.fakeInterfaces = fakeInterfaces;
        }
        
        @Override
        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces)
        {
            super.visit(version, access, name, signature, superName, allInterfaces);
        }
        
        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions)
        {
            implementedMethods.add(name + descriptor);
            return super.visitMethod(access, name, descriptor, signature, exceptions);
        }
        
        @Override
        public void visitEnd()
        {
            addStubImplementations();
            super.visitEnd();
        }
        
        private void addStubImplementations()
        {
            for (String fakeInterface : fakeInterfaces) {
                addStubMethodsForInterface(fakeInterface);
            }
        }
        
        private void addStubMethodsForInterface(String interfaceName)
        {
            Random random = new Random(interfaceName.hashCode());
            int methodCount = 1 + random.nextInt(4);
            
            for (int i = 0; i < methodCount; i++) {
                String methodName = generateMethodName(random, i);
                String descriptor = generateMethodDescriptor(random);
                String methodSignature = methodName + descriptor;
                
                if (!implementedMethods.contains(methodSignature)) {
                    addStubMethod(methodName, descriptor);
                    implementedMethods.add(methodSignature);
                }
            }
        }
        
        private String generateMethodName(Random random, int index)
        {
            String[] methodNames = {
                "process", "handle", "execute", "perform", "validate", "initialize",
                "configure", "setup", "cleanup", "finalize", "update", "refresh",
                "reset", "start", "stop", "pause", "resume", "check", "verify",
                "calculate", "compute", "transform", "convert", "parse", "format"
            };
            
            String baseName = methodNames[random.nextInt(methodNames.length)];
            if (index > 0 || random.nextBoolean()) {
                baseName += (index + 1);
            }
            
            return baseName;
        }
        
        private String generateMethodDescriptor(Random random)
        {
            String[] descriptors = {
                "()V", "()Z", "()I", "()J", "()F", "()D", "()Ljava/lang/String;",
                "(I)V", "(Z)V", "(Ljava/lang/String;)V", "(Ljava/lang/Object;)V",
                "(I)I", "(Ljava/lang/String;)Ljava/lang/String;", "(Ljava/lang/Object;)Z",
                "()[B", "()[I", "()[Ljava/lang/String;", "(II)I", "(Ljava/lang/String;I)V"
            };
            
            return descriptors[random.nextInt(descriptors.length)];
        }
        
        private void addStubMethod(String methodName, String descriptor)
        {
            MethodVisitor mv = cv.visitMethod(
                Opcodes.ACC_PUBLIC,
                methodName,
                descriptor,
                null,
                null
            );
            
            mv.visitCode();
            
            Type returnType = Type.getReturnType(descriptor);
            switch (returnType.getSort()) {
                case Type.VOID:
                    mv.visitInsn(Opcodes.RETURN);
                    break;
                case Type.BOOLEAN:
                case Type.CHAR:
                case Type.BYTE:
                case Type.SHORT:
                case Type.INT:
                    mv.visitInsn(Opcodes.ICONST_0);
                    mv.visitInsn(Opcodes.IRETURN);
                    break;
                case Type.FLOAT:
                    mv.visitInsn(Opcodes.FCONST_0);
                    mv.visitInsn(Opcodes.FRETURN);
                    break;
                case Type.LONG:
                    mv.visitInsn(Opcodes.LCONST_0);
                    mv.visitInsn(Opcodes.LRETURN);
                    break;
                case Type.DOUBLE:
                    mv.visitInsn(Opcodes.DCONST_0);
                    mv.visitInsn(Opcodes.DRETURN);
                    break;
                default:
                    mv.visitInsn(Opcodes.ACONST_NULL);
                    mv.visitInsn(Opcodes.ARETURN);
                    break;
            }
            
            mv.visitMaxs(2, 1);
            mv.visitEnd();
        }
    }
}