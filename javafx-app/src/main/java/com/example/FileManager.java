package com.example;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
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
    
    // File mapping to store content for each file
    private final Map<String, String> fileContentMap = new HashMap<>();
    
    // File counter for auto-numbering files
    private int fileCounter = 1;
    
    // File explorer reference to auto-refresh when needed
    private FileExplorer fileExplorer;
    
    public FileManager(TextArea codeEditor, TextArea consoleOutput) {
        this.codeEditor = codeEditor;
        this.consoleOutput = consoleOutput;
        this.languageManager = new LanguageManager();
        
        // Add a change listener to the code editor to track content changes in real-time
        this.codeEditor.textProperty().addListener((observable, oldValue, newValue) -> {
            if (currentFile != null) {
                // Update the content map whenever text changes
                fileContentMap.put(currentFile.getAbsolutePath(), newValue);
            }
        });
    }
    
    /**
     * Sets the file explorer reference for auto-refresh
     * 
     * @param fileExplorer The file explorer instance
     */
    public void setFileExplorer(FileExplorer fileExplorer) {
        this.fileExplorer = fileExplorer;
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
     * Creates a new file with auto-incremented number and appropriate template for the language
     * 
     * @param language The programming language to use
     */
    public void newFile(String language) {
        // First, save the current file's content
        if (currentFile != null) {
            fileContentMap.put(currentFile.getAbsolutePath(), codeEditor.getText());
            try {
                Files.writeString(currentFile.toPath(), codeEditor.getText());
                consoleOutput.appendText("Current file saved before creating new one\n");
            } catch (IOException e) {
                consoleOutput.appendText("Error saving current file: " + e.getMessage() + "\n");
            }
        }
        
        // Increment the file counter
        fileCounter++;
        
        // Generate new filename
        String extension = getExtensionForLanguage(language);
        String fileName = "Program_" + fileCounter + extension;
        
        // Create the file object
        File newFile = new File(defaultDirectory, fileName);
        
        // Set template code for the selected language
        String templateCode = languageManager.getTemplateForLanguage(language);
        
        try {
            // Write the template code to the file
            Files.writeString(newFile.toPath(), templateCode);
            
            // Store in our content map
            fileContentMap.put(newFile.getAbsolutePath(), templateCode);
            
            // Set as current file
            currentFile = newFile;
            
            // Update the editor
            codeEditor.setText(templateCode);
            
            consoleOutput.appendText("Created new file: " + fileName + "\n");
            
            // Refresh the file explorer
            if (fileExplorer != null) {
                fileExplorer.refresh();
            }
        } catch (IOException e) {
            consoleOutput.appendText("Error creating new file: " + e.getMessage() + "\n");
        }
    }
    
    /**
     * Creates a new file with the initial counter (Program_1) during app startup
     * 
     * @param language The programming language to use
     */
    public void createInitialFile(String language) {
        // Reset counter to 1 for the first file
        fileCounter = 1;
        
        // Generate filename
        String extension = getExtensionForLanguage(language);
        String fileName = "Program_" + fileCounter + extension;
        
        // Create the file object
        File newFile = new File(defaultDirectory, fileName);
        
        // Set template code for the selected language
        String templateCode = languageManager.getTemplateForLanguage(language);
        
        try {
            // Write the template code to the file
            Files.writeString(newFile.toPath(), templateCode);
            
            // Store in our content map
            fileContentMap.put(newFile.getAbsolutePath(), templateCode);
            
            // Set as current file
            currentFile = newFile;
            
            // Update the editor
            codeEditor.setText(templateCode);
            
            consoleOutput.appendText("Created initial file: " + fileName + "\n");
            
            // Refresh the file explorer
            if (fileExplorer != null) {
                fileExplorer.refresh();
            }
        } catch (IOException e) {
            consoleOutput.appendText("Error creating initial file: " + e.getMessage() + "\n");
        }
    }
    
    /**
     * Opens a file and loads its content into the editor
     * 
     * @param stage The current stage
     * @param currentLanguage The currently selected language
     * @return The selected language after auto-detection
     */
    public String openFile(Stage stage, String currentLanguage) {
        // Save current file content to the map before opening
        saveCurrentFileToMap();
        
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
        // If trying to open the already open file, do nothing
        if (currentFile != null && currentFile.getAbsolutePath().equals(file.getAbsolutePath())) {
            return detectLanguageFromFile(file, currentLanguage);
        }
        
        // Save the current file's content before switching
        if (currentFile != null) {
            String content = codeEditor.getText();
            fileContentMap.put(currentFile.getAbsolutePath(), content);
            
            try {
                Files.writeString(currentFile.toPath(), content);
                consoleOutput.appendText("Current file saved before switching\n");
            } catch (IOException e) {
                consoleOutput.appendText("Error saving current file: " + e.getMessage() + "\n");
            }
        }
        
        if (file != null && file.isFile()) {
            try {
                // Get file content - try from cache first for performance
                String content;
                if (fileContentMap.containsKey(file.getAbsolutePath())) {
                    content = fileContentMap.get(file.getAbsolutePath());
                    consoleOutput.appendText("Loaded file from cache: " + file.getName() + "\n");
                } else {
                    try {
                        // Read from disk if not in cache
                        content = Files.readString(file.toPath());
                        fileContentMap.put(file.getAbsolutePath(), content);
                        consoleOutput.appendText("Loaded file from disk: " + file.getName() + "\n");
                    } catch (IOException e) {
                        // If file can't be read, create default content
                        String language = detectLanguageFromFile(file, currentLanguage);
                        content = languageManager.getTemplateForLanguage(language);
                        fileContentMap.put(file.getAbsolutePath(), content);
                        consoleOutput.appendText("Created new content for file: " + file.getName() + "\n");
                    }
                }
                
                // Update current file reference
                currentFile = file;
                
                // Update editor with the content - do this AFTER setting currentFile
                codeEditor.setText(content);
                
                return detectLanguageFromFile(file, currentLanguage);
                
            } catch (Exception e) {
                consoleOutput.appendText("Error opening file: " + e.getMessage() + "\n");
            }
        }
        
        return currentLanguage; // Return unchanged if no new language detected
    }
    
    /**
     * Detect language from file extension
     */
    private String detectLanguageFromFile(File file, String defaultLanguage) {
        if (file != null) {
            String fileName = file.getName().toLowerCase();
            if (fileName.endsWith(".c")) {
                return "C";
            } else if (fileName.endsWith(".cpp") || fileName.endsWith(".hpp") || fileName.endsWith(".h")) {
                return "C++";
            } else if (fileName.endsWith(".java")) {
                return "Java";
            } else if (fileName.endsWith(".py")) {
                return "Python";
            } else if (fileName.endsWith(".r")) {
                return "R";
            }
        }
        return defaultLanguage;
    }
    
    /**
     * Get file extension for a language
     */
    private String getExtensionForLanguage(String language) {
        if ("C".equals(language)) {
            return ".c";
        } else if ("C++".equals(language)) {
            return ".cpp";
        } else if ("Java".equals(language)) {
            return ".java";
        } else if ("Python".equals(language)) {
            return ".py";
        } else if ("R".equals(language)) {
            return ".r";
        }
        return ".txt";
    }
    
    /**
     * Saves current file content to the map
     */
    private void saveCurrentFileToMap() {
        if (currentFile != null) {
            String currentContent = codeEditor.getText();
            fileContentMap.put(currentFile.getAbsolutePath(), currentContent);
        }
    }
    
    /**
     * Saves current file to disk
     */
    private void saveFileToDisk() {
        if (currentFile != null) {
            try {
                // Get content from the map
                String content = fileContentMap.get(currentFile.getAbsolutePath());
                if (content == null) {
                    content = codeEditor.getText();
                }
                
                // Write to file
                Files.writeString(currentFile.toPath(), content);
                consoleOutput.appendText("File saved as " + currentFile.getName() + "\n");
                
                // Auto-refresh file explorer
                if (fileExplorer != null) {
                    fileExplorer.refresh();
                }
            } catch (IOException e) {
                consoleOutput.appendText("Error saving file: " + e.getMessage() + "\n");
            }
        }
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
            suggestedFileName += getExtensionForLanguage(currentLanguage);
            
            fileChooser.setInitialFileName(suggestedFileName);
            currentFile = fileChooser.showSaveDialog(stage);
        }
        
        // Save the current content to the map
        saveCurrentFileToMap();
        
        // Save to disk
        saveFileToDisk();
    }
    
    /**
     * Saves the current content to a file with automatically generated name
     * 
     * @param language The currently selected language
     */
    public void saveFileWithAutoName(String language) {
        saveCurrentFileToMap();
        saveFileToDisk();
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