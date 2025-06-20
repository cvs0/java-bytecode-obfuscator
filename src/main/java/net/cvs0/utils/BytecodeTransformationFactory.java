package net.cvs0.utils;

import net.cvs0.context.ObfuscationContext;
import net.cvs0.core.AbstractClassVisitor;
import net.cvs0.core.AbstractMethodVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

import java.util.Random;
import java.util.function.Function;

public class BytecodeTransformationFactory
{
    public static ClassVisitor createInstructionObfuscator(ClassVisitor cv, ObfuscationContext context, 
                                                         InstructionObfuscator.ObfuscationStrategy strategy,
                                                         float probability)
    {
        return new AbstractClassVisitor(cv, context) {
            @Override
            protected String getTransformerName() {
                return "InstructionObfuscator";
            }

            @Override
            protected MethodVisitor createMethodVisitor(MethodVisitor mv, int access, String name, 
                                                      String descriptor, String signature, String[] exceptions) {
                return new AbstractMethodVisitor(mv, context, currentClassName, name, descriptor) {
                    private final InstructionObfuscator obfuscator = new InstructionObfuscator(random, strategy);

                    @Override
                    public void visitInsn(int opcode) {
                        if (BytecodeUtils.isConstantInstruction(opcode) && shouldProcessWithProbability(probability)) {
                            obfuscator.obfuscateConstant(super.mv, opcode);
                            logTransformation("Obfuscated instruction " + BytecodeUtils.getOpcodeString(opcode));
                        } else {
                            super.visitInsn(opcode);
                        }
                    }

                    @Override
                    protected String getTransformerName() {
                        return "InstructionObfuscator";
                    }
                };
            }
        };
    }

    public static ClassVisitor createConstantObfuscator(ClassVisitor cv, ObfuscationContext context, float probability)
    {
        return createInstructionObfuscator(cv, context, InstructionObfuscator.ObfuscationStrategy.ARITHMETIC, probability);
    }

    public static ClassVisitor createGarbageInjector(ClassVisitor cv, ObfuscationContext context, float probability)
    {
        return new AbstractClassVisitor(cv, context) {
            @Override
            protected String getTransformerName() {
                return "GarbageInjector";
            }

            @Override
            protected MethodVisitor createMethodVisitor(MethodVisitor mv, int access, String name, 
                                                      String descriptor, String signature, String[] exceptions) {
                return new AbstractMethodVisitor(mv, context, currentClassName, name, descriptor) {
                    @Override
                    public void visitInsn(int opcode) {
                        if (shouldProcessWithProbability(probability)) {
                            emitGarbage();
                            logTransformation("Injected garbage instruction");
                        }
                        super.visitInsn(opcode);
                    }

                    @Override
                    protected String getTransformerName() {
                        return "GarbageInjector";
                    }
                };
            }
        };
    }

    public static ClassVisitor createCustomTransformer(ClassVisitor cv, ObfuscationContext context,
                                                     String transformerName,
                                                     Function<MethodVisitorBuilder, MethodVisitor> methodVisitorCreator)
    {
        return new AbstractClassVisitor(cv, context) {
            @Override
            protected String getTransformerName() {
                return transformerName;
            }

            @Override
            protected MethodVisitor createMethodVisitor(MethodVisitor mv, int access, String name, 
                                                      String descriptor, String signature, String[] exceptions) {
                MethodVisitorBuilder builder = new MethodVisitorBuilder(mv, context, currentClassName, name, descriptor);
                return methodVisitorCreator.apply(builder);
            }
        };
    }

    public static class MethodVisitorBuilder
    {
        private final MethodVisitor mv;
        private final ObfuscationContext context;
        private final String className;
        private final String methodName;
        private final String methodDescriptor;

        public MethodVisitorBuilder(MethodVisitor mv, ObfuscationContext context, 
                                  String className, String methodName, String methodDescriptor)
        {
            this.mv = mv;
            this.context = context;
            this.className = className;
            this.methodName = methodName;
            this.methodDescriptor = methodDescriptor;
        }

        public MethodVisitor withInstructionObfuscation(InstructionObfuscator.ObfuscationStrategy strategy, float probability)
        {
            return new AbstractMethodVisitor(mv, context, className, methodName, methodDescriptor) {
                private final InstructionObfuscator obfuscator = new InstructionObfuscator(random, strategy);

                @Override
                public void visitInsn(int opcode) {
                    if (BytecodeUtils.isConstantInstruction(opcode) && shouldProcessWithProbability(probability)) {
                        obfuscator.obfuscateConstant(super.mv, opcode);
                        logTransformation("Obfuscated instruction");
                    } else {
                        super.visitInsn(opcode);
                    }
                }

                @Override
                protected String getTransformerName() {
                    return "CustomInstructionObfuscator";
                }
            };
        }

        public MethodVisitor withGarbageInjection(float probability)
        {
            return new AbstractMethodVisitor(mv, context, className, methodName, methodDescriptor) {
                @Override
                public void visitInsn(int opcode) {
                    if (shouldProcessWithProbability(probability)) {
                        emitGarbage();
                        logTransformation("Injected garbage");
                    }
                    super.visitInsn(opcode);
                }

                @Override
                protected String getTransformerName() {
                    return "CustomGarbageInjector";
                }
            };
        }

        public MethodVisitor build()
        {
            return mv;
        }
    }

    public static InstructionObfuscator createObfuscator(Random random, InstructionObfuscator.ObfuscationStrategy strategy)
    {
        return new InstructionObfuscator(random, strategy);
    }

    public static InstructionObfuscator createObfuscator()
    {
        return new InstructionObfuscator(new Random());
    }
}