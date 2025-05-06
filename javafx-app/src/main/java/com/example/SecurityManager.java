package com.example;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

import javafx.event.Event;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;

/**
 * Manages security-related operations for the application
 */
public class SecurityManager {
    private static final String DEFAULT_DIR;
    
    static {
        DEFAULT_DIR = System.getProperty("user.home") + File.separator + "Documents" + File.separator + "NoCheat_IDE";
    }
    
    /**
     * Ensures the default directory exists
     */
    public void ensureDefaultDirectoryExists() {
        File defaultDir = new File(DEFAULT_DIR);
        if (!defaultDir.exists()) {
            defaultDir.mkdirs();
        }
    }
    
    /**
     * Removes all files and subdirectories from the default directory, then removes the directory itself
     */
    public void cleanupDefaultDirectory() {
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
     * Disables copy and paste functionality in the provided text areas
     * 
     * @param textAreas The text areas to disable copy/paste for
     */
    public void disableCopyPaste(TextArea... textAreas) {
        for (TextArea textArea : textAreas) {
            // Prevent keyboard shortcuts for copy/paste
            textArea.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
                // Check for Ctrl+C, Ctrl+X, Ctrl+V keyboard shortcuts
                KeyCodeCombination copyCombo = new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_DOWN);
                KeyCodeCombination cutCombo = new KeyCodeCombination(KeyCode.X, KeyCombination.CONTROL_DOWN);
                KeyCodeCombination pasteCombo = new KeyCodeCombination(KeyCode.V, KeyCombination.CONTROL_DOWN);
                
                if (copyCombo.match(event) || cutCombo.match(event) || pasteCombo.match(event)) {
                    event.consume(); // Prevent default action
                    
                    // Get LogManager from App instance if possible
                    try {
                        LogManager logManager = App.getLogManager();
                        if (logManager != null) {
                            // Log the attempt
                            String action = "Unknown";
                            if (copyCombo.match(event)) action = "Copy";
                            else if (cutCombo.match(event)) action = "Cut";
                            else if (pasteCombo.match(event)) action = "Paste";
                            
                            logManager.addLogEntry("Attempted " + action + " operation blocked", LogManager.LogCategory.CHEATING);
                        }
                    } catch (Exception e) {
                        // Silently fail if we can't log
                    }
                }
            });
            
            // Disable context menu (right-click menu)
            textArea.setContextMenu(null);
        }
    }
    
    /**
     * Disables all context menus in the application
     * 
     * @param scene The application scene
     */
    public void disableAllContextMenus(Scene scene) {
        // Disable all context menus application-wide
        scene.addEventFilter(javafx.scene.input.ContextMenuEvent.CONTEXT_MENU_REQUESTED, 
            Event::consume);
    }
    
    /**
     * Gets the default directory path
     * 
     * @return The default directory path
     */
    public static String getDefaultDir() {
        return DEFAULT_DIR;
    }
}