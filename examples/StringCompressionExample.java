public class StringCompressionExample {
    public static void main(String[] args) {
        String shortString = "Hello";
        String longString = "This is a very long string that contains a lot of text and should be compressed when the obfuscator processes it. Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.";
        String repeatedString = "Java Bytecode Obfuscation is awesome! ".repeat(5);
        String configString = "com.example.application.configuration.DatabaseConnectionProperties";
        
        System.out.println("Short string: " + shortString);
        System.out.println("Long string: " + longString);
        System.out.println("Repeated string: " + repeatedString);
        System.out.println("Config string: " + configString);
        
        processStrings(shortString, longString, repeatedString, configString);
    }
    
    private static void processStrings(String... strings) {
        for (int i = 0; i < strings.length; i++) {
            System.out.println("Processing string " + (i + 1) + ": " + strings[i].substring(0, Math.min(50, strings[i].length())) + "...");
        }
    }
}