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
    private static final String PURPLE = "\u001B[35m";
    private static final String CYAN = "\u001B[36m";
    private static final String WHITE = "\u001B[37m";
    
    private static boolean enableColors = true;
    private static boolean enableTimestamps = true;
    private static DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    public static void info(String message) 
    {
        log("INFO", message, BLUE);
    }

    public static void success(String message) 
    {
        log("SUCCESS", message, GREEN);
    }

    public static void warn(String message) 
    {
        log("WARN", message, YELLOW);
    }

    public static void error(String message) 
    {
        log("ERROR", message, RED);
    }

    public static void debug(String message) 
    {
        log("DEBUG", message, CYAN);
    }

    public static void trace(String message) 
    {
        log("TRACE", message, PURPLE);
    }

    private static void log(String level, String message, String color) 
    {
        StringBuilder sb = new StringBuilder();
        
        if (enableTimestamps) {
            sb.append("[").append(LocalDateTime.now().format(timeFormatter)).append("] ");
        }
        
        if (enableColors) {
            sb.append(color);
        }
        
        sb.append("[").append(level).append("]");
        
        if (enableColors) {
            sb.append(RESET);
        }
        
        sb.append(" ").append(message);
        
        if ("ERROR".equals(level)) {
            System.err.println(sb.toString());
        } else {
            System.out.println(sb.toString());
        }
    }

    public static void setEnableColors(boolean enableColors) 
    {
        Logger.enableColors = enableColors;
    }

    public static void setEnableTimestamps(boolean enableTimestamps) 
    {
        Logger.enableTimestamps = enableTimestamps;
    }

    public static void setTimeFormatter(DateTimeFormatter formatter) 
    {
        Logger.timeFormatter = formatter;
    }
}