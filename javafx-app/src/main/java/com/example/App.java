package com.example;

import java.io.File;
import java.util.Optional;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

/**
 * Main IDE Application
 */
public class App extends Application {

    private TextArea codeEditor;
    private TextArea consoleOutput;
    private ComboBox<String> languageSelector;
    
    // Managers
    private FileManager fileManager;
    private CompilationManager compilationManager;
    private LanguageManager languageManager;
    private ThemeManager themeManager;
    private KeyboardShortcutManager shortcutManager;
    private DialogManager dialogManager;
    private SecurityManager securityManager;
    private static LogManager logManager;
    
    // File Explorer
    private FileExplorer fileExplorer;

    // Static method to access LogManager from other components
    public static LogManager getLogManager() {
        return logManager;
    }
    
    @Override
    public void start(Stage stage) {
        stage.setTitle("NoCopy IDE");
        
        // Disable stage resizing and set fullscreen exit key combination
        stage.setResizable(false);
        stage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH); // Disable Esc key exit

        // Set stage to be maximized and prevent minimization
        stage.setMaximized(true);
        
        // Use the proper style to remove minimize button while keeping close button
        stage.initStyle(StageStyle.UNDECORATED);
        
        // Initialize managers
        securityManager = new SecurityManager();
        dialogManager = new DialogManager();
        logManager = new LogManager();
        
        // Ensure the default directory exists
        securityManager.ensureDefaultDirectoryExists();
        
        // Initialize UI components
        codeEditor = new TextArea();
        codeEditor.setStyle("-fx-font-family: 'monospace';");
        
        consoleOutput = new TextArea();
        consoleOutput.setEditable(false);
        consoleOutput.setStyle("-fx-font-family: 'monospace';");
        
        // Initialize managers that depend on UI components
        languageManager = new LanguageManager();
        fileManager = new FileManager(codeEditor, consoleOutput);
        compilationManager = new CompilationManager(codeEditor, consoleOutput);
        
        // Tell the file manager to use the default directory
        fileManager.setDefaultDirectory(new File(SecurityManager.getDefaultDir()));
        
        // Initialize file explorer
        fileExplorer = new FileExplorer(fileManager);
        
        // Connect file explorer with file manager for auto-refresh
        fileManager.setFileExplorer(fileExplorer);
        
        // Set file explorer callback
        fileExplorer.setOnFileSelected(file -> {
            String newLanguage = fileManager.openSpecificFile(file, languageSelector.getValue());
            languageSelector.setValue(newLanguage);
        });
        
        // Language selector
        languageSelector = new ComboBox<>();
        languageSelector.getItems().addAll("C", "C++", "Java", "Python", "R");
        languageSelector.setValue("C++");
        
        // Create UI components using the UIFactory
        Button compileAndRunBtn = UIFactory.createActionButton("Compile & Run", "⚙▶", "F5");
        
        // Create file operation buttons
        Button newFileBtn = new Button("New");
        Button saveBtn = new Button("Save");
        
        // Create exit button
        Button exitBtn = new Button("Exit");
        exitBtn.setTooltip(new Tooltip("Exit the application"));
        exitBtn.getStyleClass().add("exit-button");
        
        // Create dark mode toggle button
        Button darkModeToggleBtn = new Button("🌙 Dark Mode");
        darkModeToggleBtn.getStyleClass().add("theme-toggle-button");
        darkModeToggleBtn.setTooltip(new Tooltip("Toggle between light and dark mode"));
        
        // Create font size control buttons
        Button increaseFontBtn = new Button("A+");
        increaseFontBtn.setTooltip(new Tooltip("Increase font size"));
        increaseFontBtn.getStyleClass().add("font-size-button");
        
        Button decreaseFontBtn = new Button("A-");
        decreaseFontBtn.setTooltip(new Tooltip("Decrease font size"));
        decreaseFontBtn.getStyleClass().add("font-size-button");
        
        // Event handlers
        newFileBtn.setOnAction(e -> {
            // Show language selection dialog and create a new file with auto-increment filename
            String selectedLanguage = dialogManager.showLanguageSelectionDialog();
            if (selectedLanguage != null) {
                languageSelector.setValue(selectedLanguage);
                fileManager.newFile(selectedLanguage);
                logManager.addLogEntry("Created new " + selectedLanguage + " file");
            }
        });
        
        saveBtn.setOnAction(e -> {
            fileManager.saveFileWithAutoName(languageSelector.getValue());
            logManager.addLogEntry("Saved file: " + (fileManager.getCurrentFile() != null ? fileManager.getCurrentFile().getName() : "unknown"));
            // Auto-refresh is handled by FileManager
        });
        
        exitBtn.setOnAction(e -> showExitConfirmation(stage));
        
        // Dark mode toggle button handler
        darkModeToggleBtn.setOnAction(e -> {
            boolean isDarkMode = themeManager.toggleDarkMode();
            darkModeToggleBtn.setText(isDarkMode ? "☀️ Light Mode" : "🌙 Dark Mode");
            logManager.addLogEntry("Theme changed to " + (isDarkMode ? "dark" : "light") + " mode");
            
            // Apply dark mode class to the log panel
            logManager.updateLogPanelTheme(isDarkMode);
        });
        
        compileAndRunBtn.setOnAction(e -> {
            String language = languageSelector.getValue();
            compilationManager.compileCode(language);
            logManager.addLogEntry("Compiling " + language + " code");
            
            // Only run if compilation was successful
            Platform.runLater(() -> {
                if (compilationManager.isCompilationSuccessful()) {
                    compilationManager.runCode(language);
                    logManager.addLogEntry("Running " + language + " code");
                } else {
                    logManager.addLogEntry("Compilation failed");
                }
            });
        });
        
        // Create UI layouts using UIFactory
        HBox topBar = UIFactory.createTopBar(languageSelector, newFileBtn, saveBtn, 
                                            decreaseFontBtn, increaseFontBtn, 
                                            darkModeToggleBtn, exitBtn);
        
        HBox actionToolbar = UIFactory.createActionToolbar(compileAndRunBtn);
        
        VBox editorBox = UIFactory.createEditorPanel(codeEditor);
        VBox consoleBox = UIFactory.createConsolePanel(consoleOutput);
        
        // Add console resize button handlers
        SplitPane verticalSplitPane = UIFactory.createEditorConsoleSplitPane(editorBox, consoleBox);
        
        // Main center content with action toolbar and the vertical split pane
        VBox centerContent = new VBox(actionToolbar, verticalSplitPane);
        VBox.setVgrow(verticalSplitPane, javafx.scene.layout.Priority.ALWAYS);
        
        // Create the file explorer panel
        VBox fileExplorerBox = UIFactory.createFileExplorerPanel(fileExplorer);
        
        // Create log panel
        VBox logBox = logManager.createLogPanel();
        
        // Create main split pane with file explorer, center content, and log panel
        SplitPane horizontalSplitPane = UIFactory.createMainSplitPane(fileExplorerBox, centerContent, logBox);
        
        // Create main layout
        BorderPane mainLayout = UIFactory.createMainLayout(topBar, horizontalSplitPane);
        
        Scene scene = new Scene(mainLayout, 1100, 700);
        
        // Initialize theme manager with scene reference and text areas
        themeManager = new ThemeManager(scene, codeEditor, consoleOutput);
        themeManager.applyInitialTheme();
        
        // Apply initial theme state to the log panel
        logManager.updateLogPanelTheme(themeManager.isDarkModeEnabled());
        
        // Initialize keyboard shortcut manager
        shortcutManager = new KeyboardShortcutManager(scene, codeEditor, consoleOutput, 
                                                     languageSelector, fileManager, 
                                                     compilationManager, themeManager);
        shortcutManager.registerShortcuts();
        
        // Font size button event handlers
        increaseFontBtn.setOnAction(e -> {
            themeManager.increaseFontSize();
        });
        
        decreaseFontBtn.setOnAction(e -> {
            themeManager.decreaseFontSize();
        });
        
        // Disable all context menus in the application
        securityManager.disableAllContextMenus(scene);
        
        // Disable copy-paste functionality
        securityManager.disableCopyPaste(codeEditor, consoleOutput);
        
        // Override the default close request to show our confirmation dialog
        stage.setOnCloseRequest(event -> {
            event.consume(); // Prevent the default close action
            showExitConfirmation(stage);
        });
        
        // Apply scene to stage first
        stage.setScene(scene);
        
        // Show the stage before attempting to set fullscreen
        stage.show();
        
        // Prompt for initial language selection and create first file
        Platform.runLater(() -> {
            String initialLanguage = dialogManager.showLanguageSelectionDialog();
            if (initialLanguage != null) {
                languageSelector.setValue(initialLanguage);
                fileManager.createInitialFile(initialLanguage);
            } else {
                // Use default if user cancels
                fileManager.createInitialFile(languageSelector.getValue());
            }
            
            // Force fullscreen with a slight delay
            new Thread(() -> {
                try {
                    Thread.sleep(300);
                    Platform.runLater(() -> {
                        stage.requestFocus();
                        stage.setFullScreen(true);
                    });
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        });
        
        // Set default code
        codeEditor.setText(languageManager.getTemplateForLanguage("C++"));
        
        // Show a welcome message
        consoleOutput.appendText("Welcome to NoCheat IDE!\n");
        consoleOutput.appendText("Note: All files will be permanently deleted when you exit the application.\n");
        consoleOutput.appendText("Copy and paste functionality is disabled in the code editor to prevent cheating.\n");

        // Add focus listener to detect when the user switches to another application
        stage.focusedProperty().addListener(new ChangeListener<Boolean>() {
            private boolean isInternalFocusChange = false;
            
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                // Skip internal focus changes (like dialog windows)
                if (isInternalFocusChange) {
                    return;
                }
                
                if (!newValue) {
                    // Only delete files when focus is lost to external applications
                    // Not when internal dialogs are shown
                    Platform.runLater(() -> {
                        // Check if we're still not focused after a short delay
                        // This helps avoid false triggers from internal dialogs
                        if (!stage.isFocused() && !dialogManager.isDialogShowing(stage.getScene().getWindow())) {
                            consoleOutput.appendText("Application focus lost. Deleting code files for security...\n");
                            logManager.addLogEntry("APPLICATION SWITCHING DETECTED - Possible cheating attempt", LogManager.LogCategory.CHEATING);
                            
                            // Delete the code files
                            securityManager.cleanupDefaultDirectory();
                            
                            // Recreate the directory for when the user comes back
                            securityManager.ensureDefaultDirectoryExists();
                            
                            // Clear the code editor
                            codeEditor.clear();
                            consoleOutput.appendText("All code files have been deleted for security purposes.\n");
                            consoleOutput.appendText("This is to prevent cheating by copying code to other applications.\n");
                            
                            // Reset the file explorer to reflect the empty directory
                            fileExplorer.refresh();
                        }
                    });
                } else {
                    // User has returned to the application - log only to console, not to the security log
                    // Don't show "Application focus regained" in the security and cheating log section
                    if (codeEditor.getText().isEmpty()) {
                        // If the editor is empty after returning, set the default template
                        String language = languageSelector.getValue();
                        codeEditor.setText(languageManager.getTemplateForLanguage(language));
                        // Log as a general message (not SECURITY or CHEATING) so it won't appear in the log panel
                        logManager.addLogEntry("Reset code editor with " + language + " template", LogManager.LogCategory.GENERAL);
                    }
                }
            }
        });
        
        // Register event filters for dialog tracking
        stage.addEventFilter(WindowEvent.WINDOW_HIDING, event -> {
            // Set the internal focus change flag when a window is about to be hidden
            // This is for dialogs that are children of the main window
            if (event.getTarget() instanceof Stage && event.getTarget() != stage) {
                dialogManager.untrackDialog((Stage) event.getTarget());
            }
        });
        
        stage.addEventFilter(WindowEvent.WINDOW_SHOWING, event -> {
            // Monitor focus on dialog windows
            if (event.getTarget() instanceof Stage && event.getTarget() != stage) {
                dialogManager.trackDialog((Stage) event.getTarget());
            }
        });
    }
    
    /**
     * Shows a confirmation dialog when the user attempts to exit the application
     * 
     * @param stage The main application stage
     */
    private void showExitConfirmation(Stage stage) {
        if (dialogManager.showExitConfirmation(stage)) {
            securityManager.cleanupDefaultDirectory();
            Platform.exit();
        }
    }

    public static void main(String[] args) {
        launch();
    }
}