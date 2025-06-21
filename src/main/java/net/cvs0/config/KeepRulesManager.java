package net.cvs0.config;

import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;

public class KeepRulesManager
{
    private static final Map<String, KeepRuleSet> PREDEFINED_RULES = new HashMap<>();
    
    static {
        initializePredefinedRules();
    }
    
    private static void initializePredefinedRules()
    {
        KeepRuleSet standardEntryPoints = new KeepRuleSet("Standard Entry Points");
        standardEntryPoints.addMethodKeepRule("*", "main\\(\\[Ljava/lang/String;\\)V");
        standardEntryPoints.addMethodKeepRule("*", "<init>");
        standardEntryPoints.addMethodKeepRule("*", "<clinit>");
        
        PREDEFINED_RULES.put("STANDARD_ENTRY_POINTS", standardEntryPoints);
        
        KeepRuleSet serializationRules = new KeepRuleSet("Serialization Rules");
        serializationRules.addFieldKeepRule("*", "serialVersionUID");
        serializationRules.addMethodKeepRule("*", "writeObject\\(Ljava/io/ObjectOutputStream;\\)V");
        serializationRules.addMethodKeepRule("*", "readObject\\(Ljava/io/ObjectInputStream;\\)V");
        serializationRules.addMethodKeepRule("*", "readObjectNoData\\(\\)V");
        
        PREDEFINED_RULES.put("SERIALIZATION", serializationRules);
    }
    
    public static KeepRuleSet getPredefinedRules(String ruleName)
    {
        return PREDEFINED_RULES.get(ruleName);
    }
    
    public static void applyRulesToBuilder(ObfuscationConfig.Builder builder, String... ruleNames)
    {
        for (String ruleName : ruleNames) {
            KeepRuleSet rules = getPredefinedRules(ruleName);
            if (rules != null) {
                rules.applyToBuilder(builder);
            }
        }
    }
    
    public static class KeepRuleSet
    {
        private final String name;
        private final Set<FieldKeepRule> fieldRules = new HashSet<>();
        private final Set<MethodKeepRule> methodRules = new HashSet<>();
        private final Set<ClassKeepRule> classRules = new HashSet<>();
        
        public KeepRuleSet(String name)
        {
            this.name = name;
        }
        
        public void addFieldKeepRule(String classPattern, String fieldPattern)
        {
            fieldRules.add(new FieldKeepRule(classPattern, fieldPattern));
        }
        
        public void addMethodKeepRule(String classPattern, String methodPattern)
        {
            methodRules.add(new MethodKeepRule(classPattern, methodPattern));
        }
        
        public void addClassKeepRule(String classPattern)
        {
            classRules.add(new ClassKeepRule(classPattern));
        }
        
        public void applyToBuilder(ObfuscationConfig.Builder builder)
        {
            for (FieldKeepRule rule : fieldRules) {
                if (rule.getFieldPattern().equals("*")) {
                    builder.keepAllFieldsForClass(rule.getClassPattern());
                } else {
                    builder.keepClassFieldPattern(rule.getClassPattern(), rule.getFieldPattern());
                }
            }
            
            for (MethodKeepRule rule : methodRules) {
                if (rule.getMethodPattern().equals("*")) {
                    builder.keepAllMethodsForClass(rule.getClassPattern());
                } else {
                    builder.keepClassMethodPattern(rule.getClassPattern(), rule.getMethodPattern());
                }
            }
            
            for (ClassKeepRule rule : classRules) {
                builder.keepClassPattern(rule.getClassPattern());
            }
        }
        
        private static class FieldKeepRule
        {
            final String classPattern;
            final String fieldPattern;
            
            FieldKeepRule(String classPattern, String fieldPattern)
            {
                this.classPattern = classPattern;
                this.fieldPattern = fieldPattern;
            }
            
            public String getClassPattern()
            {
                return classPattern;
            }
            
            public String getFieldPattern()
            {
                return fieldPattern;
            }
            
            @Override
            public boolean equals(Object o)
            {
                if (this == o) return true;
                if (!(o instanceof FieldKeepRule)) return false;
                FieldKeepRule that = (FieldKeepRule) o;
                return getClassPattern().equals(that.getClassPattern()) && getFieldPattern().equals(that.getFieldPattern());
            }
            
            @Override
            public int hashCode()
            {
                return getClassPattern().hashCode() * 31 + getFieldPattern().hashCode();
            }
        }
        
        private static class MethodKeepRule
        {
            final String classPattern;
            final String methodPattern;
            
            MethodKeepRule(String classPattern, String methodPattern)
            {
                this.classPattern = classPattern;
                this.methodPattern = methodPattern;
            }
            
            public String getClassPattern()
            {
                return classPattern;
            }
            
            public String getMethodPattern()
            {
                return methodPattern;
            }
            
            @Override
            public boolean equals(Object o)
            {
                if (this == o) return true;
                if (!(o instanceof MethodKeepRule)) return false;
                MethodKeepRule that = (MethodKeepRule) o;
                return getClassPattern().equals(that.getClassPattern()) && getMethodPattern().equals(that.getMethodPattern());
            }
            
            @Override
            public int hashCode()
            {
                return getClassPattern().hashCode() * 31 + getMethodPattern().hashCode();
            }
        }
        
        private static class ClassKeepRule
        {
            final String classPattern;
            
            ClassKeepRule(String classPattern)
            {
                this.classPattern = classPattern;
            }
            
            public String getClassPattern()
            {
                return classPattern;
            }
            
            @Override
            public boolean equals(Object o)
            {
                if (this == o) return true;
                if (!(o instanceof ClassKeepRule)) return false;
                ClassKeepRule that = (ClassKeepRule) o;
                return getClassPattern().equals(that.getClassPattern());
            }
            
            @Override
            public int hashCode()
            {
                return getClassPattern().hashCode();
            }
        }
    }
}