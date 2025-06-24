package net.cvs0.classfile;

import org.objectweb.asm.Type;
import java.util.*;

public class ProgramClass
{
    private final int version;
    private final int access;
    private final String name;
    private final String signature;
    private final String superName;
    private final String[] interfaces;
    private final List<ProgramField> fields;
    private final List<ProgramMethod> methods;
    private final List<Attribute> attributes;
    private final Map<String, Object> annotations;
    
    public ProgramClass(int version, int access, String name, String signature, String superName, String[] interfaces)
    {
        this.version = version;
        this.access = access;
        this.name = name;
        this.signature = signature;
        this.superName = superName;
        this.interfaces = interfaces != null ? interfaces.clone() : new String[0];
        this.fields = new ArrayList<>();
        this.methods = new ArrayList<>();
        this.attributes = new ArrayList<>();
        this.annotations = new HashMap<>();
    }
    
    public int getVersion()
    {
        return version;
    }
    
    public int getAccess()
    {
        return access;
    }
    
    public String getName()
    {
        return name;
    }
    
    public String getSignature()
    {
        return signature;
    }
    
    public String getSuperName()
    {
        return superName;
    }
    
    public String[] getInterfaces()
    {
        return interfaces.clone();
    }
    
    public List<ProgramField> getFields()
    {
        return new ArrayList<>(fields);
    }
    
    public List<ProgramMethod> getMethods()
    {
        return new ArrayList<>(methods);
    }
    
    public List<Attribute> getAttributes()
    {
        return new ArrayList<>(attributes);
    }
    
    public void addField(ProgramField field)
    {
        field.setOwnerClass(this);
        fields.add(field);
    }
    
    public void addMethod(ProgramMethod method)
    {
        method.setOwnerClass(this);
        methods.add(method);
    }
    
    public void addAttribute(Attribute attribute)
    {
        attributes.add(attribute);
    }
    
    public ProgramField findField(String name, String descriptor)
    {
        for (ProgramField field : fields) {
            if (field.getName().equals(name) && field.getDescriptor().equals(descriptor)) {
                return field;
            }
        }
        return null;
    }
    
    public ProgramMethod findMethod(String name, String descriptor)
    {
        for (ProgramMethod method : methods) {
            if (method.getName().equals(name) && method.getDescriptor().equals(descriptor)) {
                return method;
            }
        }
        return null;
    }
    
    public boolean isPublic()
    {
        return (access & JavaConstants.ACC_PUBLIC) != 0;
    }
    
    public boolean isPrivate()
    {
        return (access & JavaConstants.ACC_PRIVATE) != 0;
    }
    
    public boolean isProtected()
    {
        return (access & JavaConstants.ACC_PROTECTED) != 0;
    }
    
    public boolean isFinal()
    {
        return (access & JavaConstants.ACC_FINAL) != 0;
    }
    
    public boolean isSuper()
    {
        return (access & JavaConstants.ACC_SUPER) != 0;
    }
    
    public boolean isInterface()
    {
        return (access & JavaConstants.ACC_INTERFACE) != 0;
    }
    
    public boolean isAbstract()
    {
        return (access & JavaConstants.ACC_ABSTRACT) != 0;
    }
    
    public boolean isSynthetic()
    {
        return (access & JavaConstants.ACC_SYNTHETIC) != 0;
    }
    
    public boolean isAnnotation()
    {
        return (access & JavaConstants.ACC_ANNOTATION) != 0;
    }
    
    public boolean isEnum()
    {
        return (access & JavaConstants.ACC_ENUM) != 0;
    }
    
    public boolean isDeprecated()
    {
        return (access & JavaConstants.ACC_DEPRECATED) != 0;
    }
    
    public boolean hasInterfaces()
    {
        return interfaces.length > 0;
    }
    
    public boolean implementsInterface(String interfaceName)
    {
        for (String iface : interfaces) {
            if (iface.equals(interfaceName)) {
                return true;
            }
        }
        return false;
    }
    
    public boolean extendsSuperClass(String superClassName)
    {
        return superName != null && superName.equals(superClassName);
    }
    
    public String getSimpleName()
    {
        int lastSlash = name.lastIndexOf('/');
        if (lastSlash != -1) {
            return name.substring(lastSlash + 1);
        }
        return name;
    }
    
    public String getPackageName()
    {
        int lastSlash = name.lastIndexOf('/');
        if (lastSlash != -1) {
            return name.substring(0, lastSlash).replace('/', '.');
        }
        return "";
    }
    
    public String getClassName()
    {
        return name.replace('/', '.');
    }
    
    public Type getType()
    {
        return Type.getObjectType(name);
    }
    
    public boolean isInnerClass()
    {
        return name.contains("$");
    }
    
    public boolean isAnonymousClass()
    {
        String simpleName = getSimpleName();
        if (simpleName.contains("$")) {
            String[] parts = simpleName.split("\\$");
            String lastPart = parts[parts.length - 1];
            try {
                Integer.parseInt(lastPart);
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return false;
    }
    
    public Map<String, Object> getAnnotations()
    {
        return new HashMap<>(annotations);
    }
    
    public void addAnnotation(String descriptor, Object value)
    {
        annotations.put(descriptor, value);
    }
    
    public boolean hasAnnotation(String descriptor)
    {
        return annotations.containsKey(descriptor);
    }
    
    public Object getAnnotation(String descriptor)
    {
        return annotations.get(descriptor);
    }
    
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        if (isPublic()) sb.append("public ");
        if (isPrivate()) sb.append("private ");
        if (isProtected()) sb.append("protected ");
        if (isFinal()) sb.append("final ");
        if (isAbstract()) sb.append("abstract ");
        if (isSynthetic()) sb.append("synthetic ");
        
        if (isInterface()) {
            sb.append("interface ");
        } else if (isEnum()) {
            sb.append("enum ");
        } else if (isAnnotation()) {
            sb.append("@interface ");
        } else {
            sb.append("class ");
        }
        
        sb.append(getClassName());
        
        if (superName != null && !superName.equals(JavaConstants.OBJECT_CLASS_NAME)) {
            sb.append(" extends ").append(superName.replace('/', '.'));
        }
        
        if (hasInterfaces()) {
            sb.append(" implements ");
            for (int i = 0; i < interfaces.length; i++) {
                if (i > 0) sb.append(", ");
                sb.append(interfaces[i].replace('/', '.'));
            }
        }
        
        return sb.toString();
    }
}