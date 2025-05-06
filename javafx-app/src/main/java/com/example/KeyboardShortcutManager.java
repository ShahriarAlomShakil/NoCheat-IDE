package com.example;

import java.util.stream.Collectors;

import javafx.application.Platform;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.stage.Stage;

/**
 * Manages keyboard shortcuts for the application
 */
public class KeyboardShortcutManager {
    private final Scene scene;
    private final TextArea codeEditor;
    private final TextArea consoleOutput;
    private final ComboBox<String> languageSelector;
    private final FileManager fileManager;
    private final CompilationManager compilationManager;
    private final ThemeManager themeManager;
    
    /**
     * Constructs a new keyboard shortcut manager
     * 
     * @param scene The application scene
     * @param codeEditor The code editor text area
     * @param consoleOutput The console output text area
     * @param languageSelector The language selector combo box
     * @param fileManager The file manager
     * @param compilationManager The compilation manager
     * @param themeManager The theme manager
     */
    public KeyboardShortcutManager(Scene scene, TextArea codeEditor, TextArea consoleOutput, 
                                  ComboBox<String> languageSelector, FileManager fileManager, 
                                  CompilationManager compilationManager, ThemeManager themeManager) {
        this.scene = scene;
        this.codeEditor = codeEditor;
        this.consoleOutput = consoleOutput;
        this.languageSelector = languageSelector;
        this.fileManager = fileManager;
        this.compilationManager = compilationManager;
        this.themeManager = themeManager;
    }
    
    /**
     * Registers all keyboard shortcuts for the application
     */
    public void registerShortcuts() {
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
                // Auto-refresh is handled by FileManager
            }
        );
        
        // Dark mode toggle shortcut (Ctrl+D)
        scene.getAccelerators().put(
            new KeyCodeCombination(KeyCode.D, KeyCombination.CONTROL_DOWN),
            () -> {
                themeManager.toggleDarkMode();
            }
        );
        
        // Increase font size shortcut (Ctrl+Plus or Ctrl+Equals)
        scene.getAccelerators().put(
            new KeyCodeCombination(KeyCode.EQUALS, KeyCombination.CONTROL_DOWN),
            () -> {
                themeManager.increaseFontSize();
            }
        );
        
        // Decrease font size shortcut (Ctrl+Minus)
        scene.getAccelerators().put(
            new KeyCodeCombination(KeyCode.MINUS, KeyCombination.CONTROL_DOWN),
            () -> {
                themeManager.decreaseFontSize();
            }
        );
        
        // Expand console shortcut (Ctrl+Down)
        scene.getAccelerators().put(
            new KeyCodeCombination(KeyCode.DOWN, KeyCombination.CONTROL_DOWN),
            () -> {
                for (SplitPane splitPane : scene.getRoot().lookupAll(".split-pane").stream()
                        .filter(node -> node instanceof SplitPane)
                        .map(node -> (SplitPane) node)
                        .filter(sp -> sp.getOrientation() == Orientation.VERTICAL)
                        .collect(Collectors.toList())) {
                    
                    double currentPos = splitPane.getDividerPositions()[0];
                    if (currentPos > 0.2) { // Don't let editor disappear completely
                        splitPane.setDividerPositions(currentPos - 0.1);
                    }
                }
            }
        );
        
        // Shrink console shortcut (Ctrl+Up)
        scene.getAccelerators().put(
            new KeyCodeCombination(KeyCode.UP, KeyCombination.CONTROL_DOWN),
            () -> {
                for (SplitPane splitPane : scene.getRoot().lookupAll(".split-pane").stream()
                        .filter(node -> node instanceof SplitPane)
                        .map(node -> (SplitPane) node)
                        .filter(sp -> sp.getOrientation() == Orientation.VERTICAL)
                        .collect(Collectors.toList())) {
                    
                    double currentPos = splitPane.getDividerPositions()[0];
                    if (currentPos < 0.9) { // Don't let console disappear completely
                        splitPane.setDividerPositions(currentPos + 0.1);
                    }
                }
            }
        );
    }
}