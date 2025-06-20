# Bytecode Utilities Guide

This project now includes comprehensive bytecode manipulation utilities that can be used independently or as part of the obfuscation framework.

## Core Utilities

### BytecodeUtils
Provides static utility methods for common bytecode operations:

```java
// Push integer constants efficiently
BytecodeUtils.pushInteger(mv, 42);

// Check instruction types
if (BytecodeUtils.isConstantInstruction(opcode)) { ... }
if (BytecodeUtils.isIntegerConstant(opcode)) { ... }

// Get constant values
int value = BytecodeUtils.getConstantValue(Opcodes.ICONST_1); // returns 1

// Check if methods/fields should be skipped
if (BytecodeUtils.isMethodSkippable("main", access)) { ... }
```

### InstructionObfuscator
Handles instruction-level obfuscation with multiple strategies:

```java
Random random = new Random();
InstructionObfuscator obfuscator = new InstructionObfuscator(random, 
    InstructionObfuscator.ObfuscationStrategy.ARITHMETIC);

// Obfuscate a constant instruction
obfuscator.obfuscateConstant(mv, Opcodes.ICONST_1);

// Obfuscate specific integer values
obfuscator.obfuscateIntegerValue(mv, 42);
```

Available strategies:
- `ARITHMETIC`: Uses arithmetic operations (add, subtract, multiply, divide, xor)
- `BITWISE`: Uses bitwise operations (and, or, shift)
- `STACK_MANIPULATION`: Uses stack manipulation techniques
- `MIXED`: Randomly combines all strategies

## Abstract Base Classes

### AbstractMethodVisitor
Extends ASM's MethodVisitor with common functionality:

```java
public class MyMethodVisitor extends AbstractMethodVisitor {
    public MyMethodVisitor(MethodVisitor mv, ObfuscationContext context, 
                          String className, String methodName, String methodDescriptor) {
        super(mv, context, className, methodName, methodDescriptor);
    }
    
    @Override
    public void visitInsn(int opcode) {
        if (shouldProcessWithProbability(0.1f)) {
            emitGarbage(); // Add random garbage instructions
            logTransformation("Added garbage instruction");
        }
        super.visitInsn(opcode);
    }
    
    @Override
    protected String getTransformerName() {
        return "MyTransformer";
    }
}
```

### AbstractClassVisitor
Extends ASM's ClassVisitor with common patterns:

```java
public class MyClassVisitor extends AbstractClassVisitor {
    public MyClassVisitor(ClassVisitor cv, ObfuscationContext context) {
        super(cv, context);
    }
    
    @Override
    protected MethodVisitor createMethodVisitor(MethodVisitor mv, int access, String name, 
                                              String descriptor, String signature, String[] exceptions) {
        return new MyMethodVisitor(mv, context, currentClassName, name, descriptor);
    }
    
    @Override
    protected String getTransformerName() {
        return "MyTransformer";
    }
}
```

## Transformation Factory

The `BytecodeTransformationFactory` provides pre-built transformers:

### Quick Transformers
```java
// Create a constant obfuscator with 15% probability
ClassVisitor constantObfuscator = BytecodeTransformationFactory.createConstantObfuscator(
    writer, context, 0.15f);

// Create a garbage injector with 5% probability
ClassVisitor garbageInjector = BytecodeTransformationFactory.createGarbageInjector(
    writer, context, 0.05f);

// Create an instruction obfuscator with mixed strategy
ClassVisitor instructionObfuscator = BytecodeTransformationFactory.createInstructionObfuscator(
    writer, context, InstructionObfuscator.ObfuscationStrategy.MIXED, 0.1f);
```

### Custom Transformers
```java
ClassVisitor customTransformer = BytecodeTransformationFactory.createCustomTransformer(
    writer, context, "MyTransformer", 
    builder -> builder
        .withInstructionObfuscation(InstructionObfuscator.ObfuscationStrategy.ARITHMETIC, 0.2f)
        .withGarbageInjection(0.1f)
        .build()
);
```

## Creating Your Own Transformer

### Simple Transformer
```java
public class MySimpleTransformer extends AbstractTransformer {
    public MySimpleTransformer() {
        super("MySimpleTransformer", 100);
    }
    
    @Override
    public void transform(ClassReader reader, ClassWriter writer, ObfuscationContext context) {
        ClassVisitor visitor = BytecodeTransformationFactory.createConstantObfuscator(
            writer, context, 0.2f);
        reader.accept(visitor, 0);
    }
    
    @Override
    public boolean isEnabled(ObfuscationContext context) {
        return context.getConfig().isObfuscateConditions();
    }
}
```

### Complex Transformer with Chaining
```java
public class MyComplexTransformer extends AbstractTransformer {
    public MyComplexTransformer() {
        super("MyComplexTransformer", 200);
    }
    
    @Override
    public void transform(ClassReader reader, ClassWriter writer, ObfuscationContext context) {
        // Chain multiple transformations
        ClassVisitor chain = writer;
        
        // Add garbage injection
        chain = BytecodeTransformationFactory.createGarbageInjector(chain, context, 0.05f);
        
        // Add instruction obfuscation
        chain = BytecodeTransformationFactory.createInstructionObfuscator(
            chain, context, InstructionObfuscator.ObfuscationStrategy.BITWISE, 0.15f);
        
        // Add constant obfuscation
        chain = BytecodeTransformationFactory.createConstantObfuscator(chain, context, 0.2f);
        
        reader.accept(chain, 0);
    }
    
    @Override
    public boolean isEnabled(ObfuscationContext context) {
        return true; // Always enabled
    }
}
```

## Advanced Usage

### Custom Method Visitor
```java
public class MyAdvancedMethodVisitor extends AbstractMethodVisitor {
    private final InstructionObfuscator obfuscator;
    
    public MyAdvancedMethodVisitor(MethodVisitor mv, ObfuscationContext context, 
                                  String className, String methodName, String methodDescriptor) {
        super(mv, context, className, methodName, methodDescriptor);
        this.obfuscator = new InstructionObfuscator(random, InstructionObfuscator.ObfuscationStrategy.MIXED);
    }
    
    @Override
    public void visitInsn(int opcode) {
        // Custom logic for specific opcodes
        if (opcode == Opcodes.RETURN && shouldProcessWithProbability(0.3f)) {
            // Add dummy instructions before return
            BytecodeUtils.pushInteger(super.mv, random.nextInt(1000));
            super.visitInsn(Opcodes.POP);
            logTransformation("Added dummy instruction before return");
        }
        
        // Apply obfuscation to constants
        if (BytecodeUtils.isIntegerConstant(opcode) && shouldProcessWithProbability(0.2f)) {
            obfuscator.obfuscateConstant(super.mv, opcode);
            logTransformation("Obfuscated constant");
        } else {
            super.visitInsn(opcode);
        }
    }
    
    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
        // Custom field access obfuscation
        if (isInPackageScope() && shouldProcessWithProbability(0.1f)) {
            emitNOP(); // Add NOP before field access
            logTransformation("Added NOP before field access");
        }
        super.visitFieldInsn(opcode, owner, name, descriptor);
    }
    
    @Override
    protected String getTransformerName() {
        return "MyAdvancedTransformer";
    }
}
```

## Best Practices

1. **Chain Transformations**: Use multiple small transformers instead of one large one
2. **Use Probabilities**: Don't obfuscate every instruction - use reasonable probabilities
3. **Extend Abstract Classes**: Use `AbstractTransformer`, `AbstractClassVisitor`, and `AbstractMethodVisitor`
4. **Leverage Utilities**: Use `BytecodeUtils` and `InstructionObfuscator` for common operations
5. **Log Transformations**: Use `logTransformation()` for debugging and verbose output
6. **Check Scopes**: Always check if classes/methods are in the obfuscation scope
7. **Handle Edge Cases**: Use utility methods to skip special methods and fields

## Integration

To use these utilities in your own project, simply:

1. Copy the utility classes from `net.cvs0.utils`
2. Copy the abstract base classes from `net.cvs0.core`
3. Adapt the `ObfuscationContext` to your needs, or create your own context interface
4. Use the factory methods to create transformers quickly

The utilities are designed to be loosely coupled and can work independently of the main obfuscation framework.