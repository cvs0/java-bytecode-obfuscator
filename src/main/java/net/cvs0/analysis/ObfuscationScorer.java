package net.cvs0.analysis;

import net.cvs0.config.ObfuscationConfig;
import net.cvs0.context.ObfuscationContext;

import java.util.*;

public class ObfuscationScorer {
    
    public static class ObfuscationScore {
        private final int totalScore;
        private final int maxPossibleScore;
        private final Map<ScoreCategory, CategoryScore> categoryScores;
        private final List<String> recommendations;
        private final SecurityLevel securityLevel;
        
        public ObfuscationScore(int totalScore, int maxPossibleScore, 
                              Map<ScoreCategory, CategoryScore> categoryScores,
                              List<String> recommendations) {
            this.totalScore = totalScore;
            this.maxPossibleScore = maxPossibleScore;
            this.categoryScores = categoryScores;
            this.recommendations = recommendations;
            this.securityLevel = calculateSecurityLevel();
        }
        
        public int getTotalScore() { return totalScore; }
        public int getMaxPossibleScore() { return maxPossibleScore; }
        public double getPercentage() { return (double) totalScore / maxPossibleScore * 100; }
        public Map<ScoreCategory, CategoryScore> getCategoryScores() { return categoryScores; }
        public List<String> getRecommendations() { return recommendations; }
        public SecurityLevel getSecurityLevel() { return securityLevel; }
        
        private SecurityLevel calculateSecurityLevel() {
            double percentage = getPercentage();
            if (percentage >= 90) return SecurityLevel.MAXIMUM;
            if (percentage >= 75) return SecurityLevel.HIGH;
            if (percentage >= 50) return SecurityLevel.MEDIUM;
            if (percentage >= 25) return SecurityLevel.LOW;
            return SecurityLevel.MINIMAL;
        }
        
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("🏆 OBFUSCATION RESISTANCE SCORE\n");
            sb.append("=====================================\n");
            sb.append(String.format("Total Score: %d/%d (%.1f%%)\n", 
                totalScore, maxPossibleScore, getPercentage()));
            sb.append(String.format("Security Level: %s %s\n\n", 
                securityLevel.getEmoji(), securityLevel.name()));
            
            sb.append("📊 CATEGORY BREAKDOWN:\n");
            sb.append("-------------------------------------\n");
            for (Map.Entry<ScoreCategory, CategoryScore> entry : categoryScores.entrySet()) {
                CategoryScore score = entry.getValue();
                sb.append(String.format("%s %s: %d/%d (%.1f%%)\n",
                    entry.getKey().getEmoji(),
                    entry.getKey().getDisplayName(),
                    score.getScore(),
                    score.getMaxScore(),
                    score.getPercentage()));
            }
            
            if (!recommendations.isEmpty()) {
                sb.append("\n💡 RECOMMENDATIONS:\n");
                sb.append("-------------------------------------\n");
                for (String recommendation : recommendations) {
                    sb.append("• ").append(recommendation).append("\n");
                }
            }
            
            return sb.toString();
        }
    }
    
    public static class CategoryScore {
        private final int score;
        private final int maxScore;
        private final List<String> details;
        
        public CategoryScore(int score, int maxScore, List<String> details) {
            this.score = score;
            this.maxScore = maxScore;
            this.details = details;
        }
        
        public int getScore() { return score; }
        public int getMaxScore() { return maxScore; }
        public double getPercentage() { return maxScore > 0 ? (double) score / maxScore * 100 : 0; }
        public List<String> getDetails() { return details; }
    }
    
    public enum ScoreCategory {
        NAME_OBFUSCATION("🏷️", "Name Obfuscation"),
        CONTROL_FLOW("🔀", "Control Flow"),
        DATA_PROTECTION("🔒", "Data Protection"),
        ANTI_ANALYSIS("🛡️", "Anti-Analysis"),
        STRUCTURAL("🏗️", "Structural Changes"),
        ADVANCED("⚡", "Advanced Techniques");
        
        private final String emoji;
        private final String displayName;
        
        ScoreCategory(String emoji, String displayName) {
            this.emoji = emoji;
            this.displayName = displayName;
        }
        
        public String getEmoji() { return emoji; }
        public String getDisplayName() { return displayName; }
    }
    
    public enum SecurityLevel {
        MINIMAL("🔴"),
        LOW("🟠"),
        MEDIUM("🟡"),
        HIGH("🟢"),
        MAXIMUM("🟣");
        
        private final String emoji;
        
        SecurityLevel(String emoji) {
            this.emoji = emoji;
        }
        
        public String getEmoji() { return emoji; }
    }
    
    private final ObfuscationConfig config;
    private final ObfuscationContext context;
    
    public ObfuscationScorer(ObfuscationConfig config, ObfuscationContext context) {
        this.config = config;
        this.context = context;
    }
    
    public ObfuscationScore calculateScore() {
        Map<ScoreCategory, CategoryScore> categoryScores = new HashMap<>();
        List<String> recommendations = new ArrayList<>();
        int totalScore = 0;
        int maxPossibleScore = 0;
        
        // Score Name Obfuscation
        CategoryScore nameScore = scoreNameObfuscation(recommendations);
        categoryScores.put(ScoreCategory.NAME_OBFUSCATION, nameScore);
        totalScore += nameScore.getScore();
        maxPossibleScore += nameScore.getMaxScore();
        
        // Score Control Flow
        CategoryScore controlScore = scoreControlFlow(recommendations);
        categoryScores.put(ScoreCategory.CONTROL_FLOW, controlScore);
        totalScore += controlScore.getScore();
        maxPossibleScore += controlScore.getMaxScore();
        
        // Score Data Protection
        CategoryScore dataScore = scoreDataProtection(recommendations);
        categoryScores.put(ScoreCategory.DATA_PROTECTION, dataScore);
        totalScore += dataScore.getScore();
        maxPossibleScore += dataScore.getMaxScore();
        
        // Score Anti-Analysis
        CategoryScore antiScore = scoreAntiAnalysis(recommendations);
        categoryScores.put(ScoreCategory.ANTI_ANALYSIS, antiScore);
        totalScore += antiScore.getScore();
        maxPossibleScore += antiScore.getMaxScore();
        
        // Score Structural Changes
        CategoryScore structuralScore = scoreStructuralChanges(recommendations);
        categoryScores.put(ScoreCategory.STRUCTURAL, structuralScore);
        totalScore += structuralScore.getScore();
        maxPossibleScore += structuralScore.getMaxScore();
        
        // Score Advanced Techniques
        CategoryScore advancedScore = scoreAdvancedTechniques(recommendations);
        categoryScores.put(ScoreCategory.ADVANCED, advancedScore);
        totalScore += advancedScore.getScore();
        maxPossibleScore += advancedScore.getMaxScore();
        
        return new ObfuscationScore(totalScore, maxPossibleScore, categoryScores, recommendations);
    }
    
    private CategoryScore scoreNameObfuscation(List<String> recommendations) {
        int score = 0;
        int maxScore = 100;
        List<String> details = new ArrayList<>();
        
        // Class renaming (25 points)
        if (config.isRenameClasses()) {
            score += 25;
            details.add("Classes are renamed");
            
            // Bonus for naming mode
            switch (config.getNamingMode()) {
                case RANDOM_LONG:
                    score += 10;
                    details.add("Using strong random long names");
                    break;
                case RANDOM_SHORT:
                    score += 8;
                    details.add("Using random short names");
                    break;
                case SINGLE_CHAR:
                    score += 5;
                    details.add("Using single character names");
                    break;
                case SEQUENTIAL_ALPHA:
                    score += 3;
                    details.add("Using sequential alphabetic names");
                    break;
                default:
                    score += 1;
                    details.add("Using sequential prefix names");
            }
        } else {
            recommendations.add("Enable class renaming for better obfuscation");
        }
        
        // Method renaming (25 points)
        if (config.isRenameMethods()) {
            score += 25;
            details.add("Methods are renamed");
        } else {
            recommendations.add("Enable method renaming");
        }
        
        // Field renaming (20 points)
        if (config.isRenameFields()) {
            score += 20;
            details.add("Fields are renamed");
        } else {
            recommendations.add("Enable field renaming");
        }
        
        // Local variable renaming (10 points)
        if (config.isRenameLocalVariables()) {
            score += 10;
            details.add("Local variables are renamed");
        } else {
            recommendations.add("Enable local variable renaming for complete obfuscation");
        }
        
        return new CategoryScore(score, maxScore, details);
    }
    
    private CategoryScore scoreControlFlow(List<String> recommendations) {
        int score = 0;
        int maxScore = 80;
        List<String> details = new ArrayList<>();
        
        // Condition obfuscation (30 points)
        if (config.isObfuscateConditions()) {
            score += 30;
            details.add("Boolean conditions are obfuscated");
        } else {
            recommendations.add("Enable condition obfuscation to hide boolean logic");
        }
        
        // Anti-debugging checks (50 points)
        if (config.isAntiDebugging()) {
            score += 50;
            details.add("Anti-debugging protection enabled");
        } else {
            recommendations.add("Enable anti-debugging for runtime protection");
        }
        
        return new CategoryScore(score, maxScore, details);
    }
    
    private CategoryScore scoreDataProtection(List<String> recommendations) {
        int score = 0;
        int maxScore = 60;
        List<String> details = new ArrayList<>();
        
        // Currently no data protection features implemented
        recommendations.add("Consider adding string encryption");
        recommendations.add("Consider adding array obfuscation");
        recommendations.add("Consider adding constant pool encryption");
        
        return new CategoryScore(score, maxScore, details);
    }
    
    private CategoryScore scoreAntiAnalysis(List<String> recommendations) {
        int score = 0;
        int maxScore = 70;
        List<String> details = new ArrayList<>();
        
        // Anti-debugging (40 points)
        if (config.isAntiDebugging()) {
            score += 40;
            details.add("Runtime debugging detection");
        }
        
        // Currently no other anti-analysis features
        recommendations.add("Consider adding decompiler detection");
        recommendations.add("Consider adding integrity checking");
        recommendations.add("Consider adding VM detection");
        
        return new CategoryScore(score, maxScore, details);
    }
    
    private CategoryScore scoreStructuralChanges(List<String> recommendations) {
        int score = 0;
        int maxScore = 50;
        List<String> details = new ArrayList<>();
        
        // Analyze mapping complexity
        if (context.getMappingManager() != null) {
            int classMappings = context.getMappingManager().getClassMappings().size();
            int methodMappings = context.getMappingManager().getMethodMappings().size();
            int fieldMappings = context.getMappingManager().getFieldMappings().size();
            
            if (classMappings > 0) {
                score += 15;
                details.add(String.format("%d classes obfuscated", classMappings));
            }
            
            if (methodMappings > 0) {
                score += 20;
                details.add(String.format("%d methods obfuscated", methodMappings));
            }
            
            if (fieldMappings > 0) {
                score += 15;
                details.add(String.format("%d fields obfuscated", fieldMappings));
            }
        }
        
        recommendations.add("Consider adding dead code injection");
        recommendations.add("Consider adding fake method generation");
        
        return new CategoryScore(score, maxScore, details);
    }
    
    private CategoryScore scoreAdvancedTechniques(List<String> recommendations) {
        int score = 0;
        int maxScore = 90;
        List<String> details = new ArrayList<>();
        
        // Currently no advanced techniques implemented
        recommendations.add("Consider adding method virtualization");
        recommendations.add("Consider adding instruction substitution");
        recommendations.add("Consider adding opaque predicates");
        recommendations.add("Consider adding polymorphic code generation");
        
        return new CategoryScore(score, maxScore, details);
    }
}