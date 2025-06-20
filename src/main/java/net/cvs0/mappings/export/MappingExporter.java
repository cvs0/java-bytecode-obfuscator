package net.cvs0.mappings.export;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.TreeMap;

public class MappingExporter
{
    private final MappingData mappingData;
    private final ObjectMapper objectMapper;

    public MappingExporter(MappingData mappingData)
    {
        this.mappingData = mappingData;
        this.objectMapper = new ObjectMapper();
    }

    public void exportToFile(File outputFile, MappingFormat format) throws IOException
    {
        switch (format) {
            case PROGUARD:
                exportProGuardFormat(outputFile);
                break;
            case SRG:
                exportSrgFormat(outputFile);
                break;
            case TINY:
                exportTinyFormat(outputFile);
                break;
            case JSON:
                exportJsonFormat(outputFile);
                break;
            case CSV:
                exportCsvFormat(outputFile);
                break;
            case HUMAN_READABLE:
                exportHumanReadableFormat(outputFile);
                break;
            case RETRACE:
                exportRetraceFormat(outputFile);
                break;
            default:
                throw new IllegalArgumentException("Unsupported format: " + format);
        }
    }

    private void exportProGuardFormat(File outputFile) throws IOException
    {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
            writer.write("# ProGuard Mapping File\n");
            writer.write("# Generated: " + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + "\n\n");

            TreeMap<String, String> sortedClasses = new TreeMap<>(mappingData.getClassMappings());
            
            for (Map.Entry<String, String> classEntry : sortedClasses.entrySet()) {
                String originalClass = classEntry.getKey();
                String obfuscatedClass = classEntry.getValue();
                
                writer.write(originalClass.replace('/', '.') + " -> " + obfuscatedClass.replace('/', '.') + ":\n");
                
                TreeMap<String, String> classFields = new TreeMap<>();
                TreeMap<String, String> classMethods = new TreeMap<>();
                
                for (Map.Entry<String, String> fieldEntry : mappingData.getFieldMappings().entrySet()) {
                    String fieldKey = fieldEntry.getKey();
                    if (fieldKey.startsWith(originalClass + ".")) {
                        String fieldName = fieldKey.substring(originalClass.length() + 1);
                        classFields.put(fieldName, fieldEntry.getValue());
                    }
                }
                
                for (Map.Entry<String, String> methodEntry : mappingData.getMethodMappings().entrySet()) {
                    String methodKey = methodEntry.getKey();
                    if (methodKey.startsWith(originalClass + ".")) {
                        String methodSignature = methodKey.substring(originalClass.length() + 1);
                        classMethods.put(methodSignature, methodEntry.getValue());
                    }
                }
                
                for (Map.Entry<String, String> field : classFields.entrySet()) {
                    writer.write("    " + field.getKey() + " -> " + field.getValue() + "\n");
                }
                
                for (Map.Entry<String, String> method : classMethods.entrySet()) {
                    String methodSig = method.getKey();
                    int descriptorIndex = methodSig.lastIndexOf('(');
                    String methodName = methodSig.substring(0, descriptorIndex);
                    String descriptor = methodSig.substring(descriptorIndex);
                    writer.write("    " + methodName + descriptor + " -> " + method.getValue() + "\n");
                }
                
                writer.write("\n");
            }
        }
    }

    private void exportSrgFormat(File outputFile) throws IOException
    {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
            writer.write("# SRG Mapping File\n");
            writer.write("# Generated: " + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + "\n\n");

            for (Map.Entry<String, String> entry : new TreeMap<>(mappingData.getClassMappings()).entrySet()) {
                writer.write("CL: " + entry.getKey() + " " + entry.getValue() + "\n");
            }

            writer.write("\n");

            for (Map.Entry<String, String> entry : new TreeMap<>(mappingData.getFieldMappings()).entrySet()) {
                String[] parts = entry.getKey().split("\\.", 2);
                if (parts.length == 2) {
                    writer.write("FD: " + parts[0] + "/" + parts[1] + " " + 
                               mappingData.getClassMappings().getOrDefault(parts[0], parts[0]) + "/" + entry.getValue() + "\n");
                }
            }

            writer.write("\n");

            for (Map.Entry<String, String> entry : new TreeMap<>(mappingData.getMethodMappings()).entrySet()) {
                String methodKey = entry.getKey();
                int lastDot = methodKey.lastIndexOf('.');
                int descriptorStart = methodKey.indexOf('(', lastDot);
                
                if (lastDot > 0 && descriptorStart > lastDot) {
                    String className = methodKey.substring(0, lastDot);
                    String methodName = methodKey.substring(lastDot + 1, descriptorStart);
                    String descriptor = methodKey.substring(descriptorStart);
                    
                    String obfuscatedClass = mappingData.getClassMappings().getOrDefault(className, className);
                    writer.write("MD: " + className + "/" + methodName + " " + descriptor + " " + 
                               obfuscatedClass + "/" + entry.getValue() + " " + descriptor + "\n");
                }
            }
        }
    }

    private void exportTinyFormat(File outputFile) throws IOException
    {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
            writer.write("tiny\t2\t0\tobfuscated\tdeobfuscated\n");

            for (Map.Entry<String, String> entry : new TreeMap<>(mappingData.getClassMappings()).entrySet()) {
                writer.write("c\t" + entry.getValue() + "\t" + entry.getKey() + "\n");
                
                String originalClass = entry.getKey();
                
                for (Map.Entry<String, String> fieldEntry : mappingData.getFieldMappings().entrySet()) {
                    String fieldKey = fieldEntry.getKey();
                    if (fieldKey.startsWith(originalClass + ".")) {
                        String fieldName = fieldKey.substring(originalClass.length() + 1);
                        writer.write("\tf\t\t" + fieldEntry.getValue() + "\t" + fieldName + "\n");
                    }
                }
                
                for (Map.Entry<String, String> methodEntry : mappingData.getMethodMappings().entrySet()) {
                    String methodKey = methodEntry.getKey();
                    if (methodKey.startsWith(originalClass + ".")) {
                        String methodSignature = methodKey.substring(originalClass.length() + 1);
                        int descriptorIndex = methodSignature.lastIndexOf('(');
                        String methodName = methodSignature.substring(0, descriptorIndex);
                        String descriptor = methodSignature.substring(descriptorIndex);
                        writer.write("\tm\t" + descriptor + "\t" + methodEntry.getValue() + "\t" + methodName + "\n");
                    }
                }
            }
        }
    }

    private void exportJsonFormat(File outputFile) throws IOException
    {
        ObjectNode root = objectMapper.createObjectNode();
        
        ObjectNode metadata = root.putObject("metadata");
        metadata.put("version", "1.0");
        metadata.put("generated", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        metadata.put("format", "JSON");
        
        ObjectNode mappings = root.putObject("mappings");
        
        ObjectNode classes = mappings.putObject("classes");
        for (Map.Entry<String, String> entry : new TreeMap<>(mappingData.getClassMappings()).entrySet()) {
            classes.put(entry.getKey(), entry.getValue());
        }
        
        ObjectNode fields = mappings.putObject("fields");
        for (Map.Entry<String, String> entry : new TreeMap<>(mappingData.getFieldMappings()).entrySet()) {
            fields.put(entry.getKey(), entry.getValue());
        }
        
        ObjectNode methods = mappings.putObject("methods");
        for (Map.Entry<String, String> entry : new TreeMap<>(mappingData.getMethodMappings()).entrySet()) {
            methods.put(entry.getKey(), entry.getValue());
        }
        
        ObjectNode statistics = root.putObject("statistics");
        statistics.put("classes", mappingData.getClassMappings().size());
        statistics.put("fields", mappingData.getFieldMappings().size());
        statistics.put("methods", mappingData.getMethodMappings().size());
        statistics.put("total", mappingData.getClassMappings().size() + 
                               mappingData.getFieldMappings().size() + 
                               mappingData.getMethodMappings().size());
        
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(outputFile, root);
    }

    private void exportCsvFormat(File outputFile) throws IOException
    {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
            writer.write("Type,Original,Obfuscated,Class,Member\n");
            
            for (Map.Entry<String, String> entry : new TreeMap<>(mappingData.getClassMappings()).entrySet()) {
                writer.write("CLASS," + entry.getKey() + "," + entry.getValue() + ",," + "\n");
            }
            
            for (Map.Entry<String, String> entry : new TreeMap<>(mappingData.getFieldMappings()).entrySet()) {
                String[] parts = entry.getKey().split("\\.", 2);
                if (parts.length == 2) {
                    writer.write("FIELD," + entry.getKey() + "," + entry.getValue() + "," + parts[0] + "," + parts[1] + "\n");
                }
            }
            
            for (Map.Entry<String, String> entry : new TreeMap<>(mappingData.getMethodMappings()).entrySet()) {
                String methodKey = entry.getKey();
                int lastDot = methodKey.lastIndexOf('.');
                int descriptorStart = methodKey.indexOf('(', lastDot);
                
                if (lastDot > 0 && descriptorStart > lastDot) {
                    String className = methodKey.substring(0, lastDot);
                    String methodName = methodKey.substring(lastDot + 1, descriptorStart);
                    writer.write("METHOD," + entry.getKey() + "," + entry.getValue() + "," + className + "," + methodName + "\n");
                }
            }
        }
    }

    private void exportHumanReadableFormat(File outputFile) throws IOException
    {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
            writer.write("==================================================\n");
            writer.write("           OBFUSCATION MAPPING REPORT\n");
            writer.write("==================================================\n");
            writer.write("Generated: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "\n");
            writer.write("Total Mappings: " + (mappingData.getClassMappings().size() + 
                                             mappingData.getFieldMappings().size() + 
                                             mappingData.getMethodMappings().size()) + "\n");
            writer.write("==================================================\n\n");

            writer.write("📋 STATISTICS:\n");
            writer.write("  Classes:  " + mappingData.getClassMappings().size() + "\n");
            writer.write("  Fields:   " + mappingData.getFieldMappings().size() + "\n");
            writer.write("  Methods:  " + mappingData.getMethodMappings().size() + "\n\n");

            if (!mappingData.getClassMappings().isEmpty()) {
                writer.write("🏛️  CLASS MAPPINGS:\n");
                writer.write("=" + "=".repeat(80) + "\n");
                for (Map.Entry<String, String> entry : new TreeMap<>(mappingData.getClassMappings()).entrySet()) {
                    writer.write(String.format("  %-40s → %s\n", entry.getKey(), entry.getValue()));
                }
                writer.write("\n");
            }

            if (!mappingData.getFieldMappings().isEmpty()) {
                writer.write("🔧 FIELD MAPPINGS:\n");
                writer.write("=" + "=".repeat(80) + "\n");
                for (Map.Entry<String, String> entry : new TreeMap<>(mappingData.getFieldMappings()).entrySet()) {
                    writer.write(String.format("  %-40s → %s\n", entry.getKey(), entry.getValue()));
                }
                writer.write("\n");
            }

            if (!mappingData.getMethodMappings().isEmpty()) {
                writer.write("⚙️  METHOD MAPPINGS:\n");
                writer.write("=" + "=".repeat(80) + "\n");
                for (Map.Entry<String, String> entry : new TreeMap<>(mappingData.getMethodMappings()).entrySet()) {
                    writer.write(String.format("  %-40s → %s\n", entry.getKey(), entry.getValue()));
                }
                writer.write("\n");
            }

            writer.write("==================================================\n");
            writer.write("                    END OF REPORT\n");
            writer.write("==================================================\n");
        }
    }

    private void exportRetraceFormat(File outputFile) throws IOException
    {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
            writer.write("# Retrace Mapping File\n");
            writer.write("# Use with: retrace.bat -verbose mapping.txt stacktrace.txt\n");
            writer.write("# Generated: " + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + "\n\n");

            TreeMap<String, String> sortedClasses = new TreeMap<>(mappingData.getClassMappings());
            
            for (Map.Entry<String, String> classEntry : sortedClasses.entrySet()) {
                String originalClass = classEntry.getKey().replace('/', '.');
                String obfuscatedClass = classEntry.getValue().replace('/', '.');
                
                writer.write(originalClass + " -> " + obfuscatedClass + ":\n");
                
                for (Map.Entry<String, String> methodEntry : mappingData.getMethodMappings().entrySet()) {
                    String methodKey = methodEntry.getKey();
                    if (methodKey.startsWith(classEntry.getKey() + ".")) {
                        String methodSignature = methodKey.substring(classEntry.getKey().length() + 1);
                        int descriptorIndex = methodSignature.lastIndexOf('(');
                        String methodName = methodSignature.substring(0, descriptorIndex);
                        String descriptor = methodSignature.substring(descriptorIndex);
                        
                        writer.write("    1:1000:" + methodName + descriptor + " -> " + methodEntry.getValue() + "\n");
                    }
                }
                writer.write("\n");
            }
        }
    }

    public enum MappingFormat
    {
        PROGUARD("ProGuard mapping format", ".txt"),
        SRG("SRG (Mod Coder Pack) format", ".srg"),
        TINY("Tiny mapping format", ".tiny"),
        JSON("JSON format", ".json"),
        CSV("CSV format", ".csv"),
        HUMAN_READABLE("Human readable format", ".txt"),
        RETRACE("Retrace format", ".txt");

        private final String description;
        private final String extension;

        MappingFormat(String description, String extension)
        {
            this.description = description;
            this.extension = extension;
        }

        public String getDescription()
        {
            return description;
        }

        public String getExtension()
        {
            return extension;
        }
    }
}