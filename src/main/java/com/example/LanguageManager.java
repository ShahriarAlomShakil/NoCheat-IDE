package com.example;

import javafx.stage.FileChooser;

/**
 * Manages programming language related operations
 */
public class LanguageManager {
    
    /**
     * Get template code for the selected language
     * 
     * @param language The programming language
     * @return Template code as a string
     */
    public String getTemplateForLanguage(String language) {
        switch (language) {
            case "C":
                return getTemplateForC();
            case "C++":
                return getTemplateForCpp();
            case "Java":
                return getTemplateForJava();
            default:
                return getTemplateForCpp(); // Default to C++
        }
    }
    
    private String getTemplateForC() {
        return "#include <stdio.h>\n\n"
                + "int main() {\n"
                + "    printf(\"Hello, World!\\n\");\n"
                + "    return 0;\n"
                + "}";
    }
    
    private String getTemplateForCpp() {
        return "#include <iostream>\n\n"
                + "int main() {\n"
                + "    std::cout << \"Hello, World!\" << std::endl;\n"
                + "    return 0;\n"
                + "}";
    }
    
    private String getTemplateForJava() {
        return "public class Main {\n"
                + "    public static void main(String[] args) {\n"
                + "        System.out.println(\"Hello, World!\");\n"
                + "    }\n"
                + "}";
    }
    
    /**
     * Add appropriate file filters to the file chooser based on language
     * 
     * @param fileChooser The file chooser to update
     * @param language The selected language
     */
    public void updateFileFilters(FileChooser fileChooser, String language) {
        fileChooser.getExtensionFilters().clear();
        
        switch (language) {
            case "C":
                fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("C Files", "*.c", "*.h"),
                    new FileChooser.ExtensionFilter("All Files", "*.*")
                );
                break;
            case "C++":
                fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("C++ Files", "*.cpp", "*.hpp", "*.h"),
                    new FileChooser.ExtensionFilter("All Files", "*.*")
                );
                break;
            case "Java":
                fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Java Files", "*.java"),
                    new FileChooser.ExtensionFilter("All Files", "*.*")
                );
                break;
            default:
                fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("All Source Files", "*.c", "*.cpp", "*.h", "*.hpp", "*.java"),
                    new FileChooser.ExtensionFilter("All Files", "*.*")
                );
        }
    }
    
    /**
     * Extracts the Java class name from code
     * 
     * @param code The Java source code
     * @return The main class name
     */
    public String extractJavaClassName(String code) {
        // Simple regex to extract the class name
        // This is a basic implementation; a more robust solution would use a proper parser
        String className = "Main"; // Default class name
        
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("public\\s+class\\s+(\\w+)");
        java.util.regex.Matcher matcher = pattern.matcher(code);
        if (matcher.find()) {
            className = matcher.group(1);
        }
        
        return className;
    }
}