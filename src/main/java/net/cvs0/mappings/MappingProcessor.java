package net.cvs0.mappings;

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
    
    public void writeMappingsJson(File outputFile, Map<String, String> classMappings, 
                                 Map<String, String> fieldMappings, Map<String, String> methodMappings) throws IOException
    {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
            writer.write("{\n");
            writer.write("  \"classes\": {\n");
            
            boolean first = true;
            for (Map.Entry<String, String> entry : classMappings.entrySet()) {
                if (!first) writer.write(",\n");
                writer.write("    \"" + entry.getKey() + "\": \"" + entry.getValue() + "\"");
                first = false;
            }
            writer.write("\n  },\n");
            
            writer.write("  \"fields\": {\n");
            first = true;
            for (Map.Entry<String, String> entry : fieldMappings.entrySet()) {
                if (!first) writer.write(",\n");
                writer.write("    \"" + entry.getKey() + "\": \"" + entry.getValue() + "\"");
                first = false;
            }
            writer.write("\n  },\n");
            
            writer.write("  \"methods\": {\n");
            first = true;
            for (Map.Entry<String, String> entry : methodMappings.entrySet()) {
                if (!first) writer.write(",\n");
                writer.write("    \"" + entry.getKey() + "\": \"" + entry.getValue() + "\"");
                first = false;
            }
            writer.write("\n  }\n");
            writer.write("}\n");
        }
    }
}
