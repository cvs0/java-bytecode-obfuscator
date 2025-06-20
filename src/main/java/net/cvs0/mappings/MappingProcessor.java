package net.cvs0.mappings;

import net.cvs0.mappings.export.MappingData;
import net.cvs0.mappings.export.MappingExporter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

public class MappingProcessor
{
    public void writeMappings(File outputFile, Map<String, String> classMappings, 
                             Map<String, String> fieldMappings, Map<String, String> methodMappings) throws IOException
    {
        MappingData mappingData = new MappingData(classMappings, fieldMappings, methodMappings);
        MappingExporter exporter = new MappingExporter(mappingData);
        
        MappingExporter.MappingFormat format = detectFormat(outputFile);
        exporter.exportToFile(outputFile, format);
    }
    
    public void writeMappings(File outputFile, MappingData mappingData, MappingExporter.MappingFormat format) throws IOException
    {
        MappingExporter exporter = new MappingExporter(mappingData);
        exporter.exportToFile(outputFile, format);
    }
    
    public void writeMappingsJson(File outputFile, Map<String, String> classMappings, 
                                 Map<String, String> fieldMappings, Map<String, String> methodMappings) throws IOException
    {
        MappingData mappingData = new MappingData(classMappings, fieldMappings, methodMappings);
        MappingExporter exporter = new MappingExporter(mappingData);
        exporter.exportToFile(outputFile, MappingExporter.MappingFormat.JSON);
    }
    
    public void exportAllFormats(File baseOutputFile, MappingData mappingData) throws IOException
    {
        String baseName = getBaseName(baseOutputFile);
        String directory = baseOutputFile.getParent();
        
        MappingExporter exporter = new MappingExporter(mappingData);
        
        for (MappingExporter.MappingFormat format : MappingExporter.MappingFormat.values()) {
            File outputFile = new File(directory, baseName + "_" + format.name().toLowerCase() + format.getExtension());
            exporter.exportToFile(outputFile, format);
        }
    }
    
    private MappingExporter.MappingFormat detectFormat(File outputFile)
    {
        String name = outputFile.getName().toLowerCase();
        
        if (name.endsWith(".json")) {
            return MappingExporter.MappingFormat.JSON;
        } else if (name.endsWith(".srg")) {
            return MappingExporter.MappingFormat.SRG;
        } else if (name.endsWith(".tiny")) {
            return MappingExporter.MappingFormat.TINY;
        } else if (name.endsWith(".csv")) {
            return MappingExporter.MappingFormat.CSV;
        } else if (name.contains("retrace")) {
            return MappingExporter.MappingFormat.RETRACE;
        } else if (name.contains("proguard")) {
            return MappingExporter.MappingFormat.PROGUARD;
        } else if (name.contains("readable") || name.contains("report")) {
            return MappingExporter.MappingFormat.HUMAN_READABLE;
        }
        
        return MappingExporter.MappingFormat.HUMAN_READABLE;
    }
    
    private String getBaseName(File file)
    {
        String name = file.getName();
        int lastDot = name.lastIndexOf('.');
        return lastDot > 0 ? name.substring(0, lastDot) : name;
    }
    
    public void writeLegacyFormat(File outputFile, Map<String, String> classMappings, 
                                 Map<String, String> fieldMappings, Map<String, String> methodMappings) throws IOException
    {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
            writer.write("# Obfuscation Mappings\n");
            writer.write("# Format: original -> obfuscated\n\n");
            
            if (!classMappings.isEmpty()) {
                writer.write("# Class Mappings\n");
                for (Map.Entry<String, String> entry : classMappings.entrySet()) {
                    writer.write("CLASS: " + entry.getKey() + " -> " + entry.getValue() + "\n");
                }
                writer.write("\n");
            }
            
            if (!fieldMappings.isEmpty()) {
                writer.write("# Field Mappings\n");
                for (Map.Entry<String, String> entry : fieldMappings.entrySet()) {
                    writer.write("FIELD: " + entry.getKey() + " -> " + entry.getValue() + "\n");
                }
                writer.write("\n");
            }
            
            if (!methodMappings.isEmpty()) {
                writer.write("# Method Mappings\n");
                for (Map.Entry<String, String> entry : methodMappings.entrySet()) {
                    writer.write("METHOD: " + entry.getKey() + " -> " + entry.getValue() + "\n");
                }
            }
        }
    }
}
