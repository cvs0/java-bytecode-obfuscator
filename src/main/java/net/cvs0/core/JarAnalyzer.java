package net.cvs0.core;

import net.cvs0.classfile.*;
import net.cvs0.config.ObfuscationConfig;
import net.cvs0.utils.Logger;
import org.objectweb.asm.*;

import java.io.*;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

public class JarAnalyzer 
{
    private final ObfuscationConfig config;
    private final Program program;
    private final Map<String, byte[]> resources;
    private final Map<String, String> manifestAttributes;

    public JarAnalyzer(ObfuscationConfig config) 
    {
        this.config = config;
        this.program = new Program();
        this.resources = new HashMap<>();
        this.manifestAttributes = new HashMap<>();
    }

    public AnalysisResult analyze(File jarFile) throws IOException 
    {
        if (!jarFile.exists()) {
            throw new FileNotFoundException("JAR file not found: " + jarFile.getAbsolutePath());
        }

        if (config.isVerbose()) {
            Logger.info("Analyzing JAR file: " + jarFile.getAbsolutePath());
        }

        long startTime = System.currentTimeMillis();
        int classesProcessed = 0;
        int resourcesProcessed = 0;

        try (JarFile jar = new JarFile(jarFile)) {
            Enumeration<JarEntry> entries = jar.entries();
            
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String entryName = entry.getName();
                
                if (entry.isDirectory()) {
                    continue;
                }
                
                try (InputStream inputStream = jar.getInputStream(entry)) {
                    byte[] entryData = inputStream.readAllBytes();
                    
                    if (entryName.endsWith(".class")) {
                        processClassEntry(entryName, entryData);
                        classesProcessed++;
                    } else if (entryName.equals("META-INF/MANIFEST.MF")) {
                        processManifest(entryData);
                    } else {
                        processResourceEntry(entryName, entryData);
                        resourcesProcessed++;
                    }
                }
            }
        }

        long analysisTime = System.currentTimeMillis() - startTime;
        
        if (config.isVerbose()) {
            Logger.info("Analysis completed in " + analysisTime + "ms");
            Logger.info("Classes processed: " + classesProcessed);
            Logger.info("Resources processed: " + resourcesProcessed);
            Logger.info("Entry points found: " + program.getEntryPoints().size());
        }

        return new AnalysisResult(program, resources, manifestAttributes, analysisTime);
    }

    private void processClassEntry(String entryName, byte[] classData) 
    {
        try {
            String className = entryName.substring(0, entryName.length() - 6);
            
            if (shouldSkipClass(className)) {
                if (config.isVerbose()) {
                    Logger.debug("Skipping class: " + className);
                }
                return;
            }

            ClassReader classReader = new ClassReader(classData);
            ClassAnalysisVisitor visitor = new ClassAnalysisVisitor();
            classReader.accept(visitor, ClassReader.EXPAND_FRAMES);
            
            ProgramClass programClass = visitor.getProgramClass();
            if (programClass != null) {
                program.addClass(programClass);
                
                if (config.isVerbose()) {
                    Logger.debug("Analyzed class: " + programClass.getName() + 
                                " (" + programClass.getMethods().size() + " methods, " + 
                                programClass.getFields().size() + " fields)");
                }
            }
            
        } catch (Exception e) {
            Logger.error("Failed to analyze class " + entryName + ": " + e.getMessage());
            if (config.isVerbose()) {
                e.printStackTrace();
            }
        }
    }

    private void processResourceEntry(String entryName, byte[] resourceData) 
    {
        resources.put(entryName, resourceData);
        
        if (config.isVerbose()) {
            Logger.debug("Processed resource: " + entryName + " (" + resourceData.length + " bytes)");
        }
    }

    private void processManifest(byte[] manifestData) throws IOException 
    {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(manifestData)))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains(": ")) {
                    String[] parts = line.split(": ", 2);
                    if (parts.length == 2) {
                        manifestAttributes.put(parts[0].trim(), parts[1].trim());
                    }
                }
            }
        }
        
        resources.put("META-INF/MANIFEST.MF", manifestData);
        
        if (config.isVerbose()) {
            Logger.debug("Processed manifest with " + manifestAttributes.size() + " attributes");
        }
    }

    private boolean shouldSkipClass(String className) 
    {
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
                return true;
            }
        }

        for (String excludePackage : config.getExcludePackages()) {
            String packagePrefix = excludePackage.replace('.', '/');
            if (className.startsWith(packagePrefix)) {
                return true;
            }
        }

        return false;
    }

    private class ClassAnalysisVisitor extends ClassVisitor 
    {
        private ProgramClass programClass;
        private final List<ProgramField> fields = new ArrayList<>();
        private final List<ProgramMethod> methods = new ArrayList<>();

        public ClassAnalysisVisitor() {
            super(Opcodes.ASM9);
        }

        @Override
        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) 
        {
            this.programClass = new ProgramClass(version, access, name, signature, superName, interfaces);
        }

        @Override
        public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) 
        {
            ProgramField field = new ProgramField(access, name, descriptor, signature, value);
            fields.add(field);
            return new FieldAnalysisVisitor(field);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) 
        {
            ProgramMethod method = new ProgramMethod(access, name, descriptor, signature, exceptions);
            methods.add(method);
            return new MethodAnalysisVisitor(method);
        }

        @Override
        public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) 
        {
            return new AnnotationAnalysisVisitor(descriptor, visible, programClass);
        }

        @Override
        public void visitInnerClass(String name, String outerName, String innerName, int access) 
        {
            InnerClassAttribute innerClassAttribute = new InnerClassAttribute();
            InnerClassAttribute.InnerClassInfo innerClassInfo = new InnerClassAttribute.InnerClassInfo(name, outerName, innerName, access);
            innerClassAttribute.addInnerClass(innerClassInfo);
            programClass.addAttribute(innerClassAttribute);
        }

        @Override
        public void visitSource(String source, String debug) 
        {
            if (source != null) {
                SourceFileAttribute sourceFile = new SourceFileAttribute(source);
                programClass.addAttribute(sourceFile);
            }
        }

        @Override
        public void visitEnd() 
        {
            for (ProgramField field : fields) {
                programClass.addField(field);
            }
            for (ProgramMethod method : methods) {
                programClass.addMethod(method);
            }
        }

        public ProgramClass getProgramClass() 
        {
            return programClass;
        }
    }

    private class FieldAnalysisVisitor extends FieldVisitor 
    {
        private final ProgramField field;

        public FieldAnalysisVisitor(ProgramField field) {
            super(Opcodes.ASM9);
            this.field = field;
        }

        @Override
        public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) 
        {
            return new AnnotationAnalysisVisitor(descriptor, visible, field);
        }
    }

    private class MethodAnalysisVisitor extends MethodVisitor 
    {
        private final ProgramMethod method;

        public MethodAnalysisVisitor(ProgramMethod method) {
            super(Opcodes.ASM9);
            this.method = method;
        }

        @Override
        public void visitCode() 
        {
            method.setHasCode(true);
        }

        @Override
        public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) 
        {
            return new AnnotationAnalysisVisitor(descriptor, visible, method);
        }

        @Override
        public void visitLocalVariable(String name, String descriptor, String signature, Label start, Label end, int index) 
        {
            LocalVariableTableAttribute localVar = new LocalVariableTableAttribute();
            method.addAttribute(localVar);
        }

        @Override
        public void visitLineNumber(int line, Label start) 
        {
            LineNumberTableAttribute lineNumber = new LineNumberTableAttribute();
            method.addAttribute(lineNumber);
        }

        @Override
        public void visitParameter(String name, int access) 
        {
            MethodParametersAttribute parameter = new MethodParametersAttribute();
            parameter.addParameter(name, access);
            method.addAttribute(parameter);
        }
    }

    private class AnnotationAnalysisVisitor extends AnnotationVisitor 
    {
        private final String descriptor;
        private final boolean visible;
        private final Object target;
        private final Map<String, Object> values = new HashMap<>();

        public AnnotationAnalysisVisitor(String descriptor, boolean visible, Object target) {
            super(Opcodes.ASM9);
            this.descriptor = descriptor;
            this.visible = visible;
            this.target = target;
        }

        @Override
        public void visit(String name, Object value) 
        {
            values.put(name, value);
        }

        @Override
        public void visitEnd() 
        {
            if (target instanceof ProgramClass) {
                ((ProgramClass) target).addAnnotation(descriptor, values);
            } else if (target instanceof ProgramField) {
                ((ProgramField) target).addAnnotation(descriptor, values);
            } else if (target instanceof ProgramMethod) {
                ((ProgramMethod) target).addAnnotation(descriptor, values);
            }
        }
    }

    public static class AnalysisResult 
    {
        private final Program program;
        private final Map<String, byte[]> resources;
        private final Map<String, String> manifestAttributes;
        private final long analysisTime;

        public AnalysisResult(Program program, Map<String, byte[]> resources, 
                            Map<String, String> manifestAttributes, long analysisTime) 
        {
            this.program = program;
            this.resources = new HashMap<>(resources);
            this.manifestAttributes = new HashMap<>(manifestAttributes);
            this.analysisTime = analysisTime;
        }

        public Program getProgram() { return program; }
        public Map<String, byte[]> getResources() { return new HashMap<>(resources); }
        public Map<String, String> getManifestAttributes() { return new HashMap<>(manifestAttributes); }
        public long getAnalysisTime() { return analysisTime; }
    }
}