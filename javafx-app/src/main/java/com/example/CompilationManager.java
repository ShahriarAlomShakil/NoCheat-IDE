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
                case "Python":
                    // Python doesn't need compilation, it's interpreted.
                    // Just store the code to a temporary file for later execution
                    compilePythonProgram();
                    break;
                case "R":
                    // R is also interpreted, just save to a file
                    compileRProgram();
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
    
    private void compilePythonProgram() throws IOException {
        // Python doesn't need compilation, we just save the code to a temporary file
        Path tempFile = Files.createTempFile("nocopypython", ".py");
        Files.writeString(tempFile, codeEditor.getText());
        
        // Store the path for execution later
        System.setProperty("nocopy.python.file", tempFile.toString());
        
        consoleOutput.appendText("Python code ready to run.\n");
    }
    
    private void compileRProgram() throws IOException {
        // R doesn't need compilation, we just save the code to a temporary file
        Path tempFile = Files.createTempFile("nocopyr", ".R");
        Files.writeString(tempFile, codeEditor.getText());
        
        // Store the path for execution later
        System.setProperty("nocopy.r.file", tempFile.toString());
        
        consoleOutput.appendText("R code ready to run.\n");
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
            } else if (language.equals("Python")) {
                runPythonProgram();
            } else if (language.equals("R")) {
                runRProgram();
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
    
    private void runPythonProgram() throws IOException, InterruptedException {
        String pythonFile = System.getProperty("nocopy.python.file");
        
        if (pythonFile == null || !Files.exists(Paths.get(pythonFile))) {
            consoleOutput.appendText("No Python file found. Please prepare your code first.\n");
            return;
        }
        
        String command = "python " + pythonFile;
        
        // On some systems, the 'python' command might refer to Python 2
        // Try 'python3' if available
        try {
            Process checkPython3 = Runtime.getRuntime().exec("python3 --version");
            int exitCode = checkPython3.waitFor();
            if (exitCode == 0) {
                command = "python3 " + pythonFile;
            }
        } catch (Exception e) {
            // If python3 check fails, stick with the original command
        }
        
        Process process = Runtime.getRuntime().exec(command);
        readProcessOutput(process);
    }
    
    private void runRProgram() throws IOException, InterruptedException {
        String rFile = System.getProperty("nocopy.r.file");
        
        if (rFile == null || !Files.exists(Paths.get(rFile))) {
            consoleOutput.appendText("No R file found. Please prepare your code first.\n");
            return;
        }
        
        // Try various R command options (Rscript, R --vanilla, etc.)
        String[] possibleCommands = {
            "Rscript", 
            "R", 
            "/usr/bin/Rscript", 
            "/usr/local/bin/Rscript"
        };
        
        boolean rInstalled = false;
        String workingCommand = null;
        
        // Check which R command is available
        for (String cmd : possibleCommands) {
            try {
                Process checkR = Runtime.getRuntime().exec(cmd + " --version");
                int exitCode = checkR.waitFor();
                if (exitCode == 0) {
                    rInstalled = true;
                    workingCommand = cmd;
                    break;
                }
            } catch (Exception e) {
                // Command not found, try next
            }
        }
        
        if (!rInstalled) {
            // If R is not installed, show helpful error message
            consoleOutput.appendText("Error: R is not installed or not in the system PATH.\n\n");
            consoleOutput.appendText("To install R:\n");
            consoleOutput.appendText("- On Ubuntu/Debian: Run 'sudo apt-get install r-base'\n");
            consoleOutput.appendText("- On Fedora/RHEL: Run 'sudo dnf install R'\n");
            consoleOutput.appendText("- On Arch Linux: Run 'sudo pacman -S r'\n");
            consoleOutput.appendText("- On macOS: Install from https://cran.r-project.org/bin/macosx/\n");
            consoleOutput.appendText("- On Windows: Install from https://cran.r-project.org/bin/windows/\n\n");
            consoleOutput.appendText("After installation, restart the NoCopy IDE.\n");
            return;
        }
        
        // Format the command based on which version works
        String command = workingCommand;
        if (workingCommand.equals("R")) {
            // If using R directly instead of Rscript, add --vanilla flag
            command += " --vanilla < " + rFile;
        } else {
            // Standard Rscript command
            command += " " + rFile;
        }
        
        
        try {
            Process process;
            if (workingCommand.equals("R")) {
                // For R command, we need to use shell to support input redirection
                String[] shellCmd = {"/bin/sh", "-c", command};
                process = Runtime.getRuntime().exec(shellCmd);
            } else {
                process = Runtime.getRuntime().exec(command);
            }
            readProcessOutput(process);
        } catch (IOException e) {
            consoleOutput.appendText("Error executing R code: " + e.getMessage() + "\n");
            consoleOutput.appendText("Please check if R is properly installed and in your PATH.\n");
        }
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
        
        // For Python and R, they're interpreted so always "successful"
        if (System.getProperty("nocopy.python.file") != null || 
            System.getProperty("nocopy.r.file") != null) {
            return true;
        }
        
        // For C/C++, check for the output file
        Path outputFile = Paths.get(TEMP_DIR, "nocopyoutput");
        return Files.exists(outputFile);
    }
}