package com.example;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import javafx.scene.control.TextArea;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/**
 * Manages file operations for the IDE
 */
public class FileManager {
    
    private File currentFile = null;
    private final TextArea codeEditor;
    private final TextArea consoleOutput;
    private final LanguageManager languageManager;
    private File defaultDirectory;
    
    public FileManager(TextArea codeEditor, TextArea consoleOutput) {
        this.codeEditor = codeEditor;
        this.consoleOutput = consoleOutput;
        this.languageManager = new LanguageManager();
    }
    
    /**
     * Sets the default directory for file operations
     * 
     * @param directory The default directory to use
     */
    public void setDefaultDirectory(File directory) {
        if (directory != null && directory.isDirectory()) {
            this.defaultDirectory = directory;
        }
    }
    
    /**
     * Creates a new empty file
     */
    public void newFile() {
        codeEditor.clear();
        currentFile = null;
    }
    
    /**
     * Opens a file and loads its content into the editor
     * 
     * @param stage The current stage
     * @param currentLanguage The currently selected language
     * @return The selected language after auto-detection
     */
    public String openFile(Stage stage, String currentLanguage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open File");
        
        // Set initial directory to the default directory
        if (defaultDirectory != null && defaultDirectory.exists()) {
            fileChooser.setInitialDirectory(defaultDirectory);
        }
        
        // Add file filters based on selected language
        languageManager.updateFileFilters(fileChooser, currentLanguage);
        
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            return openSpecificFile(file, currentLanguage);
        }
        
        return currentLanguage; // Return unchanged if no new language detected
    }
    
    /**
     * Opens a specific file from the file explorer
     * 
     * @param file The file to open
     * @param currentLanguage The currently selected language
     * @return The detected language based on file extension
     */
    public String openSpecificFile(File file, String currentLanguage) {
        if (file != null && file.isFile()) {
            try {
                String content = Files.readString(file.toPath());
                codeEditor.setText(content);
                currentFile = file;
                consoleOutput.appendText("File opened successfully\n");
                
                // Auto-select language based on file extension
                String fileName = file.getName().toLowerCase();
                if (fileName.endsWith(".c")) {
                    return "C";
                } else if (fileName.endsWith(".cpp") || fileName.endsWith(".hpp") || fileName.endsWith(".h")) {
                    return "C++";
                } else if (fileName.endsWith(".java")) {
                    return "Java";
                }
                
            } catch (IOException e) {
                consoleOutput.appendText("Error opening file\n");
            }
        }
        
        return currentLanguage; // Return unchanged if no new language detected
    }
    
    /**
     * Saves the current content to a file
     * 
     * @param stage The current stage
     * @param currentLanguage The currently selected language
     */
    public void saveFile(Stage stage, String currentLanguage) {
        if (currentFile == null) {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save File");
            
            // Set initial directory to the default directory
            if (defaultDirectory != null && defaultDirectory.exists()) {
                fileChooser.setInitialDirectory(defaultDirectory);
            }
            
            // Add file filters based on selected language
            languageManager.updateFileFilters(fileChooser, currentLanguage);
            
            // Determine a default file name based on language
            String suggestedFileName = "untitled";
            if ("C".equals(currentLanguage)) {
                suggestedFileName += ".c";
            } else if ("C++".equals(currentLanguage)) {
                suggestedFileName += ".cpp";
            } else if ("Java".equals(currentLanguage)) {
                suggestedFileName += ".java";
            }
            
            fileChooser.setInitialFileName(suggestedFileName);
            currentFile = fileChooser.showSaveDialog(stage);
        }
        
        if (currentFile != null) {
            try {
                // Ensure we're saving within the default directory
                if (defaultDirectory != null && !isFileInDirectory(currentFile, defaultDirectory)) {
                    // If not in default directory, create a new file in the default directory
                    File newFile = new File(defaultDirectory, currentFile.getName());
                    currentFile = newFile;
                }
                
                Files.writeString(currentFile.toPath(), codeEditor.getText());
                consoleOutput.appendText("File saved successfully\n");
            } catch (IOException e) {
                consoleOutput.appendText("Error saving file\n");
            }
        }
    }
    
    /**
     * Checks if a file is located within a directory or its subdirectories
     * 
     * @param file The file to check
     * @param directory The directory to check within
     * @return True if the file is in the directory or subdirectory
     */
    private boolean isFileInDirectory(File file, File directory) {
        try {
            String filePath = file.getCanonicalPath();
            String dirPath = directory.getCanonicalPath();
            return filePath.startsWith(dirPath);
        } catch (IOException e) {
            return false;
        }
    }
    
    /**
     * Gets the current file
     * 
     * @return The current file or null if none is open
     */
    public File getCurrentFile() {
        return currentFile;
    }
}