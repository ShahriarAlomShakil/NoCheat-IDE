package com.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javafx.application.Platform;
import javafx.scene.control.TextArea;

/**
 * Manages code compilation and execution
 */
public class CompilationManager {
    private final TextArea consoleOutput;
    private final TextArea codeEditor;
    private final LanguageManager languageManager;
    private static final String TEMP_DIR = System.getProperty("java.io.tmpdir");
    private boolean javaCompilationSuccessful = false;
    
    public CompilationManager(TextArea codeEditor, TextArea consoleOutput) {
        this.codeEditor = codeEditor;
        this.consoleOutput = consoleOutput;
        this.languageManager = new LanguageManager();
    }
    
    /**
     * Compiles code based on selected language
     * 
     * @param language The programming language to compile
     */
    public void compileCode(String language) {
        try {
            // Clear previous output
            consoleOutput.clear();
            
            // Reset compilation status flags
            javaCompilationSuccessful = false;
            Path outputFile = Paths.get(TEMP_DIR, "nocopyoutput");
            if (Files.exists(outputFile)) {
                Files.delete(outputFile);
            }
            
            switch (language) {
                case "C":
                    compileCProgram(outputFile);
                    break;
                case "C++":
                    compileCppProgram(outputFile);
                    break;
                case "Java":
                    compileJavaProgram();
                    break;
            }
            
        } catch (IOException | InterruptedException e) {
            consoleOutput.appendText("Error: " + e.getMessage() + "\n");
        }
    }
    
    private void compileCProgram(Path outputFile) throws IOException, InterruptedException {
        Path tempFile = Files.createTempFile("nocopygcc", ".c");
        Files.writeString(tempFile, codeEditor.getText());
        
        String command = "gcc " + tempFile.toString() + " -o " + outputFile.toString();
        executeCompileCommand(command, tempFile);
    }
    
    private void compileCppProgram(Path outputFile) throws IOException, InterruptedException {
        Path tempFile = Files.createTempFile("nocopygcc", ".cpp");
        Files.writeString(tempFile, codeEditor.getText());
        
        String command = "g++ " + tempFile.toString() + " -o " + outputFile.toString();
        executeCompileCommand(command, tempFile);
    }
    
    private void compileJavaProgram() throws IOException, InterruptedException {
        // For Java, we need to ensure the class name matches the filename
        String code = codeEditor.getText();
        String className = languageManager.extractJavaClassName(code);
        
        Path javaDir = Files.createTempDirectory("nocopyjavac");
        Path javaFile = javaDir.resolve(className + ".java");
        Files.writeString(javaFile, code);
        
        String command = "javac " + javaFile.toString();
        Process process = Runtime.getRuntime().exec(command);
        
        // Read error stream for compilation errors
        BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        StringBuilder errorOutput = new StringBuilder();
        String line;
        while ((line = errorReader.readLine()) != null) {
            errorOutput.append(line).append("\n");
        }
        
        int exitCode = process.waitFor();
        if (exitCode == 0) {
            // Don't output anything on success
            Path classFile = javaDir.resolve(className + ".class");
            if (Files.exists(classFile)) {
                System.setProperty("nocopy.java.dir", javaDir.toString());
                System.setProperty("nocopy.java.class", className);
                javaCompilationSuccessful = true;
                consoleOutput.appendText("Java compilation successful.\n");
            }
        } else {
            // Only show errors if compilation failed
            final String errors = errorOutput.toString();
            Platform.runLater(() -> consoleOutput.appendText(errors));
        }
    }
    
    private void executeCompileCommand(String command, Path tempFile) throws IOException, InterruptedException {
        Process process = Runtime.getRuntime().exec(command);
        
        // Read error stream for compilation errors
        BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        StringBuilder errorOutput = new StringBuilder();
        String line;
        while ((line = errorReader.readLine()) != null) {
            errorOutput.append(line).append("\n");
        }
        
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            // Only show errors if compilation failed
            final String errors = errorOutput.toString();
            Platform.runLater(() -> consoleOutput.appendText(errors));
        } else {
            consoleOutput.appendText("Compilation successful.\n");
        }
        
        // Clean up temp file
        Files.deleteIfExists(tempFile);
    }
    
    /**
     * Runs the compiled code
     * 
     * @param language The programming language to run
     */
    public void runCode(String language) {
        try {
            // Clear any existing output and show just program output
            consoleOutput.clear();
            
            if (language.equals("Java")) {
                runJavaProgram();
            } else {
                runCompiledBinary();
            }
            
        } catch (IOException | InterruptedException e) {
            consoleOutput.appendText("Error: " + e.getMessage() + "\n");
        }
    }
    
    private void runCompiledBinary() throws IOException, InterruptedException {
        Path outputFile = Paths.get(TEMP_DIR, "nocopyoutput");
        if (!Files.exists(outputFile)) {
            consoleOutput.appendText("No compiled program found. Please compile first.\n");
            return;
        }
        
        String command = outputFile.toString();
        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            command += ".exe";
        }
        
        Process process = Runtime.getRuntime().exec(command);
        readProcessOutput(process);
    }
    
    private void runJavaProgram() throws IOException, InterruptedException {
        String javaDir = System.getProperty("nocopy.java.dir");
        String className = System.getProperty("nocopy.java.class");
        
        if (javaDir == null || className == null) {
            consoleOutput.appendText("No compiled Java program found. Please compile first.\n");
            return;
        }
        
        String command = "java -cp " + javaDir + " " + className;
        Process process = Runtime.getRuntime().exec(command);
        readProcessOutput(process);
    }
    
    private void readProcessOutput(Process process) throws IOException, InterruptedException {
        // Read standard output
        BufferedReader outputReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        while ((line = outputReader.readLine()) != null) {
            final String finalLine = line;
            Platform.runLater(() -> consoleOutput.appendText(finalLine + "\n"));
        }
        
        // Read error stream
        BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        while ((line = errorReader.readLine()) != null) {
            final String finalLine = line;
            Platform.runLater(() -> consoleOutput.appendText(finalLine + "\n"));
        }
        
        // Don't show exit code in console output
        process.waitFor();
    }
    
    /**
     * Checks if compilation was successful
     * 
     * @return true if compilation was successful for the respective language
     */
    public boolean isCompilationSuccessful() {
        // For Java, check the flag
        if (javaCompilationSuccessful) {
            return true;
        }
        
        // For C/C++, check for the output file
        Path outputFile = Paths.get(TEMP_DIR, "nocopyoutput");
        return Files.exists(outputFile);
    }
}