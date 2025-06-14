package net.cvs0.core;

import net.cvs0.transformers.ClassRenameTransformer;
import net.cvs0.transformers.FieldRenameTransformer;
import net.cvs0.transformers.MethodRenameTransformer;

import java.util.ArrayList;
import java.util.List;

public class TransformerFactory
{
    public static List<Transformer> createDefaultTransformers()
    {
        List<Transformer> transformers = new ArrayList<>();
        transformers.add(new ClassRenameTransformer());
        transformers.add(new FieldRenameTransformer());
        transformers.add(new MethodRenameTransformer());
        return transformers;
    }
    
    public static List<Transformer> createRenameTransformers()
    {
        return createDefaultTransformers();
    }
    
    public static Transformer createClassRenameTransformer()
    {
        return new ClassRenameTransformer();
    }
    
    public static Transformer createFieldRenameTransformer()
    {
        return new FieldRenameTransformer();
    }
    
    public static Transformer createMethodRenameTransformer()
    {
        return new MethodRenameTransformer();
    }
}