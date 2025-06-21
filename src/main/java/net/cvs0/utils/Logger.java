package net.cvs0.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Logger
{
    private static final String RESET = "\u001B[0m";
    private static final String RED = "\u001B[31m";
    private static final String GREEN = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String BLUE = "\u001B[34m";
    private static final String CYAN = "\u001B[36m";
    private static final String GRAY = "\u001B[90m";
    private static final String BOLD = "\u001B[1m";
    
    private static final String SUCCESS_EMOJI = "✅";
    private static final String ERROR_EMOJI = "❌";
    private static final String WARNING_EMOJI = "⚠️";
    private static final String INFO_EMOJI = "ℹ️";
    private static final String DEBUG_EMOJI = "🔍";
    private static final String PROCESS_EMOJI = "⚙️";
    private static final String ARROW_EMOJI = "➡️";
    private static final String CHECKMARK_EMOJI = "✓";
    private static final String CROSS_EMOJI = "✗";
    
    private static boolean verboseMode = false;
    private static boolean colorEnabled = true;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    
    public static void setVerbose(boolean verbose)
    {
        verboseMode = verbose;
    }
    
    public static void setColorEnabled(boolean enabled)
    {
        colorEnabled = enabled;
    }
    
    public static void success(String message)
    {
        log(LogLevel.SUCCESS, SUCCESS_EMOJI, GREEN, message);
    }
    
    public static void error(String message)
    {
        log(LogLevel.ERROR, ERROR_EMOJI, RED, message);
    }
    
    public static void warning(String message)
    {
        log(LogLevel.WARNING, WARNING_EMOJI, YELLOW, message);
    }
    
    public static void info(String message)
    {
        log(LogLevel.INFO, INFO_EMOJI, BLUE, message);
    }
    
    public static void debug(String message)
    {
        if (verboseMode) {
            log(LogLevel.DEBUG, DEBUG_EMOJI, GRAY, message);
        }
    }
    
    public static void process(String message)
    {
        log(LogLevel.PROCESS, PROCESS_EMOJI, CYAN, message);
    }
    
    public static void step(String message)
    {
        log(LogLevel.STEP, ARROW_EMOJI, BLUE, message);
    }
    
    public static void result(String message, boolean isSuccess)
    {
        if (isSuccess) {
            log(LogLevel.SUCCESS, CHECKMARK_EMOJI, GREEN, message);
        } else {
            log(LogLevel.ERROR, CROSS_EMOJI, RED, message);
        }
    }
    
    public static void phase(String phaseName)
    {
        String separator = "═".repeat(50);
        log(LogLevel.PHASE, "", BOLD + CYAN, separator);
        log(LogLevel.PHASE, PROCESS_EMOJI, BOLD + CYAN, " " + phaseName.toUpperCase());
        log(LogLevel.PHASE, "", BOLD + CYAN, separator);
    }
    
    public static void subPhase(String subPhaseName)
    {
        String separator = "─".repeat(30);
        log(LogLevel.SUB_PHASE, "", CYAN, separator);
        log(LogLevel.SUB_PHASE, ARROW_EMOJI, CYAN, " " + subPhaseName);
        log(LogLevel.SUB_PHASE, "", CYAN, separator);
    }
    
    public static void stats(String label, Object value)
    {
        if (verboseMode) {
            log(LogLevel.STATS, "📊", GRAY, String.format("%-20s: %s", label, value));
        }
    }
    
    public static void transformation(String message)
    {
        if (verboseMode) {
            log(LogLevel.TRANSFORMATION, "🔄", GRAY, message);
        }
    }
    
    public static void mapping(String original, String obfuscated)
    {
        if (verboseMode) {
            log(LogLevel.MAPPING, "🗺️", GRAY, String.format("%s → %s", original, obfuscated));
        }
    }
    
    private static void log(LogLevel level, String emoji, String color, String message)
    {
        if (level == null) {
            return;
        }
        
        try {
            String timestamp = LocalDateTime.now().format(TIME_FORMATTER);
            String formattedMessage;
            
            emoji = emoji != null ? emoji : "";
            color = color != null ? color : "";
            message = message != null ? message : "null";
            
            if (colorEnabled) {
                formattedMessage = String.format("%s[%s] %s %s%s%s", 
                    GRAY, timestamp, emoji, color, message, RESET);
            } else {
                formattedMessage = String.format("[%s] %s %s", 
                    timestamp, emoji, message);
            }
            
            if (level == LogLevel.ERROR) {
                System.err.println(formattedMessage);
                System.err.flush();
            } else {
                System.out.println(formattedMessage);
                System.out.flush();
            }
        } catch (Exception e) {
            System.err.println("[LOGGER ERROR] Failed to log message: " + e.getMessage());
            if (message != null) {
                System.err.println("[FALLBACK] " + message);
            }
        }
    }
    
    private enum LogLevel
    {
        SUCCESS, ERROR, WARNING, INFO, DEBUG, PROCESS, STEP, PHASE, SUB_PHASE, STATS, TRANSFORMATION, MAPPING
    }
}