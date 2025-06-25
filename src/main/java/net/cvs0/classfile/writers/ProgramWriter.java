package net.cvs0.classfile.writers;

import net.cvs0.classfile.*;
import net.cvs0.classfile.Attribute;
import net.cvs0.core.Program;
import net.cvs0.utils.Logger;
import org.objectweb.asm.*;

import java.io.*;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

public class ProgramWriter 
{
    public void writeProgram(Program program, File outputJar, Map<String, byte[]> resources, 
                           Map<String, String> manifestAttributes) throws IOException 
    {
        try (JarOutputStream jarOut = new JarOutputStream(new FileOutputStream(outputJar))) {
            
            for (ProgramClass programClass : program.getAllClasses()) {
                String className = programClass.getName();
                byte[] classData = writeClass(programClass);
                
                JarEntry entry = new JarEntry(className + ".class");
                jarOut.putNextEntry(entry);
                jarOut.write(classData);
                jarOut.closeEntry();
            }
            
            for (Map.Entry<String, byte[]> resourceEntry : resources.entrySet()) {
                String resourceName = resourceEntry.getKey();
                byte[] resourceData = resourceEntry.getValue();
                
                if (resourceName.endsWith(".class")) {
                    continue;
                }
                
                JarEntry entry = new JarEntry(resourceName);
                jarOut.putNextEntry(entry);
                jarOut.write(resourceData);
                jarOut.closeEntry();
            }
        }
    }
    
    private byte[] writeClass(ProgramClass programClass) 
    {
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        
        String[] interfaces = programClass.getInterfaces();
        classWriter.visit(
            programClass.getVersion(),
            programClass.getAccess(),
            programClass.getName(),
            programClass.getSignature(),
            programClass.getSuperName(),
            interfaces
        );
        
        for (ProgramField field : programClass.getFields()) {
            writeField(classWriter, field);
        }
        
        for (ProgramMethod method : programClass.getMethods()) {
            writeMethod(classWriter, method);
        }
        
        for (Attribute attribute : programClass.getAttributes()) {
            writeAttribute(classWriter, attribute);
        }
        
        classWriter.visitEnd();
        return classWriter.toByteArray();
    }
    
    private void writeField(ClassWriter classWriter, ProgramField field) 
    {
        FieldVisitor fieldVisitor = classWriter.visitField(
            field.getAccess(),
            field.getName(),
            field.getDescriptor(),
            field.getSignature(),
            field.getValue()
        );
        
        for (Attribute attribute : field.getAttributes()) {
            writeFieldAttribute(fieldVisitor, attribute);
        }
        
        fieldVisitor.visitEnd();
    }
    
    private void writeMethod(ClassWriter classWriter, ProgramMethod method) 
    {
        MethodVisitor methodVisitor = classWriter.visitMethod(
            method.getAccess(),
            method.getName(),
            method.getDescriptor(),
            method.getSignature(),
            method.getExceptions()
        );
        
        if (method.hasCode()) {
            methodVisitor.visitCode();
            
            for (Attribute attribute : method.getAttributes()) {
                writeMethodAttribute(methodVisitor, attribute);
            }
            
            methodVisitor.visitMaxs(0, 0);
        }
        
        methodVisitor.visitEnd();
    }
    
    private void writeAttribute(ClassVisitor classVisitor, Attribute attribute)
    {
        if (attribute instanceof SourceFileAttribute) {
            SourceFileAttribute sourceFile = (SourceFileAttribute) attribute;
            classVisitor.visitSource(sourceFile.getSourceFile(), null);
        } else if (attribute instanceof InnerClassAttribute) {
            InnerClassAttribute innerClass = (InnerClassAttribute) attribute;
            for (InnerClassAttribute.InnerClassInfo info : innerClass.getInnerClasses()) {
                classVisitor.visitInnerClass(
                    info.getInnerClass(),
                    info.getOuterClass(),
                    info.getInnerName(),
                    info.getAccess()
                );
            }
        }
    }
    
    private void writeFieldAttribute(FieldVisitor fieldVisitor, Attribute attribute) 
    {
    }
    
    private void writeMethodAttribute(MethodVisitor methodVisitor, Attribute attribute) 
    {
        if (attribute instanceof CodeAttribute) {
            CodeAttribute codeAttr = (CodeAttribute) attribute;
            
            for (Attribute codeSubAttr : codeAttr.getAttributes()) {
                if (codeSubAttr instanceof LocalVariableTableAttribute) {
                    LocalVariableTableAttribute localVarTable = (LocalVariableTableAttribute) codeSubAttr;
                    for (LocalVariableTableAttribute.LocalVariableInfo localVar : localVarTable.getLocalVariables()) {
                        methodVisitor.visitLocalVariable(
                            localVar.getName(),
                            localVar.getDescriptor(),
                            null,
                            new Label(),
                            new Label(),
                            localVar.getIndex()
                        );
                    }
                } else if (codeSubAttr instanceof LocalVariableTypeTableAttribute) {
                    LocalVariableTypeTableAttribute localVarTypeTable = (LocalVariableTypeTableAttribute) codeSubAttr;
                    for (LocalVariableTypeTableAttribute.LocalVariableTypeInfo typeInfo : localVarTypeTable.getLocalVariableTypes()) {
                        methodVisitor.visitLocalVariable(
                            typeInfo.getName(),
                            null,
                            typeInfo.getSignature(),
                            new Label(),
                            new Label(),
                            typeInfo.getIndex()
                        );
                    }
                }
            }
        } else if (attribute instanceof MethodParametersAttribute) {
            MethodParametersAttribute methodParams = (MethodParametersAttribute) attribute;
            for (MethodParametersAttribute.Parameter param : methodParams.getParameters()) {
                methodVisitor.visitParameter(param.getName(), param.getAccess());
            }
        }
    }
}