package net.cvs0.obfuscation;

import net.cvs0.config.NamingMode;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public abstract class NameGenerator 
{
    protected final Set<String> usedNames = new HashSet<>();
    protected final Set<String> reservedNames = new HashSet<>();

    public NameGenerator() 
    {
        initializeReservedNames();
    }

    public abstract String generateClassName();
    public abstract String generateMethodName();
    public abstract String generateFieldName();
    public abstract String generateVariableName();

    public void addReservedName(String name) 
    {
        reservedNames.add(name);
    }

    public void addReservedNames(Collection<String> names) 
    {
        reservedNames.addAll(names);
    }

    public boolean isNameUsed(String name) 
    {
        return usedNames.contains(name) || reservedNames.contains(name);
    }

    public void reset() 
    {
        usedNames.clear();
    }

    private void initializeReservedNames() 
    {
        reservedNames.addAll(Arrays.asList(
            "abstract", "assert", "boolean", "break", "byte", "case", "catch", "char",
            "class", "const", "continue", "default", "do", "double", "else", "enum",
            "extends", "final", "finally", "float", "for", "goto", "if", "implements",
            "import", "instanceof", "int", "interface", "long", "native", "new", "package",
            "private", "protected", "public", "return", "short", "static", "strictfp",
            "super", "switch", "synchronized", "this", "throw", "throws", "transient",
            "try", "void", "volatile", "while", "true", "false", "null"
        ));
    }

    public static NameGenerator createGenerator(NamingMode mode) 
    {
        return switch (mode) {
            case SEQUENTIAL_PREFIX -> new SequentialPrefixGenerator();
            case SEQUENTIAL_ALPHA -> new SequentialAlphaGenerator();
            case RANDOM_SHORT -> new RandomShortGenerator();
            case RANDOM_LONG -> new RandomLongGenerator();
            case SINGLE_CHAR -> new SingleCharGenerator();
        };
    }

    public static class SequentialPrefixGenerator extends NameGenerator 
    {
        private int classCounter = 1;
        private int methodCounter = 1;
        private int fieldCounter = 1;
        private int variableCounter = 1;

        @Override
        public String generateClassName() 
        {
            String name;
            do {
                name = "a" + classCounter++;
            } while (isNameUsed(name));
            usedNames.add(name);
            return name;
        }

        @Override
        public String generateMethodName() 
        {
            String name;
            do {
                name = "b" + methodCounter++;
            } while (isNameUsed(name));
            usedNames.add(name);
            return name;
        }

        @Override
        public String generateFieldName() 
        {
            String name;
            do {
                name = "c" + fieldCounter++;
            } while (isNameUsed(name));
            usedNames.add(name);
            return name;
        }

        @Override
        public String generateVariableName() 
        {
            String name;
            do {
                name = "d" + variableCounter++;
            } while (isNameUsed(name));
            usedNames.add(name);
            return name;
        }
    }

    public static class SequentialAlphaGenerator extends NameGenerator 
    {
        private int classCounter = 0;
        private int methodCounter = 0;
        private int fieldCounter = 0;
        private int variableCounter = 0;

        @Override
        public String generateClassName() 
        {
            String name;
            do {
                name = generateAlphaSequence(classCounter++);
            } while (isNameUsed(name));
            usedNames.add(name);
            return name;
        }

        @Override
        public String generateMethodName() 
        {
            String name;
            do {
                name = generateAlphaSequence(methodCounter++);
            } while (isNameUsed(name));
            usedNames.add(name);
            return name;
        }

        @Override
        public String generateFieldName() 
        {
            String name;
            do {
                name = generateAlphaSequence(fieldCounter++);
            } while (isNameUsed(name));
            usedNames.add(name);
            return name;
        }

        @Override
        public String generateVariableName() 
        {
            String name;
            do {
                name = generateAlphaSequence(variableCounter++);
            } while (isNameUsed(name));
            usedNames.add(name);
            return name;
        }

        private String generateAlphaSequence(int index) 
        {
            StringBuilder sb = new StringBuilder();
            int temp = index;
            do {
                sb.insert(0, (char) ('a' + (temp % 26)));
                temp /= 26;
            } while (temp > 0);
            return sb.toString();
        }
    }

    public static class RandomShortGenerator extends NameGenerator 
    {
        private static final String CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        private static final int MIN_LENGTH = 3;
        private static final int MAX_LENGTH = 8;

        @Override
        public String generateClassName() 
        {
            return generateRandomName();
        }

        @Override
        public String generateMethodName() 
        {
            return generateRandomName();
        }

        @Override
        public String generateFieldName() 
        {
            return generateRandomName();
        }

        @Override
        public String generateVariableName() 
        {
            return generateRandomName();
        }

        private String generateRandomName() 
        {
            String name;
            do {
                int length = ThreadLocalRandom.current().nextInt(MIN_LENGTH, MAX_LENGTH + 1);
                StringBuilder sb = new StringBuilder(length);
                for (int i = 0; i < length; i++) {
                    sb.append(CHARS.charAt(ThreadLocalRandom.current().nextInt(CHARS.length())));
                }
                name = sb.toString();
            } while (isNameUsed(name));
            usedNames.add(name);
            return name;
        }
    }

    public static class RandomLongGenerator extends NameGenerator 
    {
        private static final String CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_$";
        private static final int MIN_LENGTH = 8;
        private static final int MAX_LENGTH = 20;

        @Override
        public String generateClassName() 
        {
            return generateRandomName();
        }

        @Override
        public String generateMethodName() 
        {
            return generateRandomName();
        }

        @Override
        public String generateFieldName() 
        {
            return generateRandomName();
        }

        @Override
        public String generateVariableName() 
        {
            return generateRandomName();
        }

        private String generateRandomName() 
        {
            String name;
            do {
                int length = ThreadLocalRandom.current().nextInt(MIN_LENGTH, MAX_LENGTH + 1);
                StringBuilder sb = new StringBuilder(length);
                
                sb.append(CHARS.charAt(ThreadLocalRandom.current().nextInt(52)));
                
                for (int i = 1; i < length; i++) {
                    sb.append(CHARS.charAt(ThreadLocalRandom.current().nextInt(CHARS.length())));
                }
                name = sb.toString();
            } while (isNameUsed(name));
            usedNames.add(name);
            return name;
        }
    }

    public static class SingleCharGenerator extends NameGenerator 
    {
        private int classIndex = 0;
        private int methodIndex = 0;
        private int fieldIndex = 0;
        private int variableIndex = 0;

        @Override
        public String generateClassName() 
        {
            return generateSingleChar(classIndex++);
        }

        @Override
        public String generateMethodName() 
        {
            return generateSingleChar(methodIndex++);
        }

        @Override
        public String generateFieldName() 
        {
            return generateSingleChar(fieldIndex++);
        }

        @Override
        public String generateVariableName() 
        {
            return generateSingleChar(variableIndex++);
        }

        private String generateSingleChar(int index) 
        {
            String name;
            do {
                if (index < 26) {
                    name = String.valueOf((char) ('a' + index));
                } else if (index < 52) {
                    name = String.valueOf((char) ('A' + (index - 26)));
                } else {
                    int cycles = (index - 52) / 52;
                    int remainder = (index - 52) % 52;
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i <= cycles; i++) {
                        if (remainder < 26) {
                            sb.append((char) ('a' + remainder));
                        } else {
                            sb.append((char) ('A' + (remainder - 26)));
                        }
                    }
                    name = sb.toString();
                }
                if (!isNameUsed(name)) {
                    break;
                }
                index++;
            } while (true);
            
            usedNames.add(name);
            return name;
        }
    }
}