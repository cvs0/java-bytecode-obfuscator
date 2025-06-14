package net.cvs0.core;

import net.cvs0.context.ObfuscationContext;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class TransformerPipeline
{
    private final List<Transformer> transformers;
    
    public TransformerPipeline()
    {
        this.transformers = new ArrayList<>();
    }
    
    public void addTransformer(Transformer transformer)
    {
        transformers.add(transformer);
        transformers.sort(Comparator.comparingInt(Transformer::getPriority));
    }
    
    public void removeTransformer(String name)
    {
        transformers.removeIf(t -> t.getName().equals(name));
    }
    
    public byte[] process(byte[] classBytes, ObfuscationContext context)
    {
        try {
            ClassReader reader = new ClassReader(classBytes);
            ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
            
            boolean anyTransformed = false;
            
            for (Transformer transformer : transformers) {
                if (transformer.isEnabled(context)) {
                    try {
                        ClassReader currentReader = anyTransformed ? new ClassReader(writer.toByteArray()) : reader;
                        ClassWriter currentWriter = new ClassWriter(currentReader, ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
                        
                        transformer.transform(currentReader, currentWriter, context);
                        writer = currentWriter;
                        anyTransformed = true;
                    } catch (Exception e) {
                        if (context.isVerbose()) {
                            System.err.println("Warning: Transformer " + transformer.getName() + " failed for class " + context.getCurrentClassName() + ": " + e.getMessage());
                        }
                        continue;
                    }
                }
            }
            
            if (!anyTransformed) {
                reader.accept(writer, 0);
            }
            
            return writer.toByteArray();
            
        } catch (Exception e) {
            if (context.isVerbose()) {
                System.err.println("Error processing class " + context.getCurrentClassName() + ": " + e.getMessage());
                e.printStackTrace();
            }
            return classBytes;
        }
    }
    
    public List<Transformer> getTransformers()
    {
        return new ArrayList<>(transformers);
    }
    
    public void clear()
    {
        transformers.clear();
    }
}