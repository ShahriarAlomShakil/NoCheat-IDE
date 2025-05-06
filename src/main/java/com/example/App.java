package com.example;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 * Main IDE Application
 */
public class App extends Application {

    private TextArea codeEditor;
    private TextArea consoleOutput;
    private ComboBox<String> languageSelector;
    private static final String TEMP_DIR = System.getProperty("java.io.tmpdir");
    private static final String DEFAULT_DIR = System.getProperty("user.home") + File.separator + "Documents" + File.separator + "NoCheat_IDE";

    // Managers
    private FileManager fileManager;
    private CompilationManager compilationManager;
    private LanguageManager languageManager;
    
    // File Explorer
    private FileExplorer fileExplorer;

    @Override
    public void start(Stage stage) {
        stage.setTitle("NoCopy IDE");
        
        // Ensure the default directory exists
        ensureDefaultDirectoryExists();
        
        // Initialize UI components
        codeEditor = new TextArea();
        codeEditor.setStyle("-fx-font-family: 'monospace';");
        
        // Disable copy/paste functionality
        disableCopyPaste();
        
        consoleOutput = new TextArea();
        consoleOutput.setEditable(false);
        consoleOutput.setStyle("-fx-font-family: 'monospace';");
        
        // Initialize managers
        languageManager = new LanguageManager();
        fileManager = new FileManager(codeEditor, consoleOutput);
        compilationManager = new CompilationManager(codeEditor, consoleOutput);
        
        // Tell the file manager to use the default directory
        fileManager.setDefaultDirectory(new File(DEFAULT_DIR));
        
        // Initialize file explorer
        fileExplorer = new FileExplorer(fileManager);
        
        // Set file explorer callback
        fileExplorer.setOnFileSelected(file -> {
            String newLanguage = fileManager.openSpecificFile(file, languageSelector.getValue());
            languageSelector.setValue(newLanguage);
        });
        
        // Language selector
        languageSelector = new ComboBox<>();
        languageSelector.getItems().addAll("C", "C++", "Java");
        languageSelector.setValue("C++");
        
        // Add language change listener to update template code
        languageSelector.setOnAction(e -> {
            String language = languageSelector.getValue();
            codeEditor.setText(languageManager.getTemplateForLanguage(language));
        });
        
        // Create file operation buttons
        Button newFileBtn = new Button("New");
        Button saveBtn = new Button("Save");
        
        // Create Compile & Run button
        Button compileAndRunBtn = UIFactory.createActionButton("Compile & Run", "⚙▶", "F5");
        
        // Event handlers
        newFileBtn.setOnAction(e -> fileManager.newFile());
        saveBtn.setOnAction(e -> {
            fileManager.saveFile(stage, languageSelector.getValue());
            fileExplorer.refresh(); // Refresh file explorer after save
        });
        compileAndRunBtn.setOnAction(e -> {
            String language = languageSelector.getValue();
            compilationManager.compileCode(language);
            
            // Only run if compilation was successful
            Platform.runLater(() -> {
                if (compilationManager.isCompilationSuccessful()) {
                    compilationManager.runCode(language);
                }
            });
        });
        
        // Create layouts
        HBox topBar = new HBox(10, new Label("Language:"), languageSelector, newFileBtn, saveBtn);
        topBar.setPadding(new Insets(10));
        
        // Action toolbar with compile and run button
        HBox actionToolbar = new HBox(15, compileAndRunBtn);
        actionToolbar.setAlignment(Pos.CENTER);
        actionToolbar.setPadding(new Insets(10));
        actionToolbar.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #cccccc; -fx-border-width: 0 0 1 0;");
        
        VBox consoleBox = new VBox(new Label("Console Output:"), consoleOutput);
        VBox.setVgrow(consoleOutput, Priority.ALWAYS);
        consoleBox.setPadding(new Insets(10));
        
        VBox editorBox = new VBox(new Label("Code Editor:"), codeEditor);
        VBox.setVgrow(codeEditor, Priority.ALWAYS);
        editorBox.setPadding(new Insets(10));
        
        // Main center content with editor and action toolbar
        VBox centerContent = new VBox(actionToolbar, editorBox);
        VBox.setVgrow(editorBox, Priority.ALWAYS);
        
        // Create split pane for file explorer and main content
        SplitPane splitPane = new SplitPane();
        splitPane.setOrientation(Orientation.HORIZONTAL);
        
        // Create the file explorer title with the fixed directory path
        Label explorerTitle = new Label("NoCheat_IDE Files");
        
        // Wrap file explorer in a VBox with a title
        VBox fileExplorerBox = new VBox(explorerTitle, fileExplorer);
        fileExplorerBox.setPadding(new Insets(10));
        VBox.setVgrow(fileExplorer, Priority.ALWAYS);
        
        // Set the file explorer box to fully expand vertically
        fileExplorerBox.setPrefHeight(Double.MAX_VALUE);
        
        // Configure the center content to be fully resizable
        centerContent.setMinWidth(300);
        centerContent.setPrefWidth(Region.USE_COMPUTED_SIZE);
        
        // Add file explorer and center content to the split pane
        splitPane.getItems().addAll(fileExplorerBox, centerContent);
        
        // Set initial divider position (20% for file explorer - significantly increased)
        splitPane.setDividerPositions(0.20);
        
        // Set minimum and maximum width for the file explorer - larger constraints
        fileExplorerBox.setMinWidth(180);
        fileExplorerBox.setPrefWidth(220);
        fileExplorerBox.setMaxWidth(300);
        
        // Make SplitPane contents resizable with constraints
        SplitPane.setResizableWithParent(fileExplorerBox, true);
        SplitPane.setResizableWithParent(centerContent, true);
        
        // Handle fullscreen mode
        stage.fullScreenProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                // In fullscreen mode, maintain proportionally larger file explorer
                Platform.runLater(() -> splitPane.setDividerPositions(0.15));
            } else {
                // Return to normal setting when exiting fullscreen
                Platform.runLater(() -> splitPane.setDividerPositions(0.20));
            }
        });
        
        // Allow the SplitPane to resize with the window
        BorderPane.setMargin(splitPane, new Insets(0));
        
        BorderPane mainLayout = new BorderPane();
        mainLayout.setTop(topBar);
        mainLayout.setCenter(splitPane);
        mainLayout.setBottom(consoleBox);
        
        Scene scene = new Scene(mainLayout, 1100, 700);
        
        // Add keyboard shortcuts
        addKeyboardShortcuts(scene);
        
        // Load CSS
        scene.getStylesheets().add(getClass().getResource("/ide-styles.css").toExternalForm());
        
        // Set up close handler to clean directory on application exit
        stage.setOnCloseRequest(this::handleCloseRequest);
        
        // Register a shutdown hook as a fallback
        Runtime.getRuntime().addShutdownHook(new Thread(this::cleanupDefaultDirectory));
        
        stage.setScene(scene);
        stage.show();
        
        // Set default code
        codeEditor.setText(languageManager.getTemplateForLanguage("C++"));
        
        // Show a welcome message
        consoleOutput.appendText("Welcome to NoCopy IDE!\n");
        consoleOutput.appendText("Note: All files will be permanently deleted when you exit the application.\n");
        consoleOutput.appendText("Copy and paste functionality is disabled in the code editor to prevent cheating.\n");
    }
    
    /**
     * Disables copy and paste functionality in the code editor
     */
    private void disableCopyPaste() {
        // Prevent keyboard shortcuts for copy/paste
        codeEditor.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            // Check for Ctrl+C, Ctrl+X, Ctrl+V keyboard shortcuts
            KeyCodeCombination copyCombo = new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_DOWN);
            KeyCodeCombination cutCombo = new KeyCodeCombination(KeyCode.X, KeyCombination.CONTROL_DOWN);
            KeyCodeCombination pasteCombo = new KeyCodeCombination(KeyCode.V, KeyCombination.CONTROL_DOWN);
            
            if (copyCombo.match(event) || cutCombo.match(event) || pasteCombo.match(event)) {
                event.consume(); // Prevent default action
                consoleOutput.appendText("Copy/paste functionality is disabled in NoCopy IDE.\n");
            }
        });
        
        // Disable context menu (right-click menu) to prevent copy/paste
        codeEditor.setContextMenu(null);
    }
    
    /**
     * Handles the window close request by cleaning up the workspace
     * 
     * @param event The window event
     */
    private void handleCloseRequest(WindowEvent event) {
        cleanupDefaultDirectory();
    }
    
    /**
     * Removes all files and subdirectories from the default directory, then removes the directory itself
     */
    private void cleanupDefaultDirectory() {
        File defaultDir = new File(DEFAULT_DIR);
        if (defaultDir.exists() && defaultDir.isDirectory()) {
            try {
                // Get all files and subdirectories in reverse order (deepest first)
                Files.walk(defaultDir.toPath())
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(file -> {
                        if (!file.delete()) {
                            // Silent failure - don't print anything to console
                        }
                    });
            } catch (IOException e) {
                // Silent exception handling - don't print anything to console
            }
        }
    }
    
    /**
     * Ensures the default directory exists
     */
    private void ensureDefaultDirectoryExists() {
        File defaultDir = new File(DEFAULT_DIR);
        if (!defaultDir.exists()) {
            defaultDir.mkdirs();
        }
    }
    
    private void addKeyboardShortcuts(Scene scene) {
        // Compile and Run shortcut (F5)
        scene.getAccelerators().put(
            new KeyCodeCombination(KeyCode.F5),
            () -> {
                String language = languageSelector.getValue();
                compilationManager.compileCode(language);
                
                Platform.runLater(() -> {
                    if (compilationManager.isCompilationSuccessful()) {
                        compilationManager.runCode(language);
                    }
                });
            }
        );
        
        // Save shortcut (Ctrl+S)
        scene.getAccelerators().put(
            new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN),
            () -> {
                fileManager.saveFile((Stage) scene.getWindow(), languageSelector.getValue());
                fileExplorer.refresh(); // Refresh file explorer after save
            }
        );
    }

    public static void main(String[] args) {
        launch();
    }
}