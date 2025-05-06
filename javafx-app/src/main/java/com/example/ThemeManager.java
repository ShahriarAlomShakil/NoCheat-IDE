package com.example;

import javafx.scene.Scene;
import javafx.scene.control.TextArea;

/**
 * Manages theme settings for the IDE
 */
public class ThemeManager {
    
    // Theme state
    private boolean darkModeEnabled = false;
    
    // Font size settings
    private int currentFontSize = 14; // Default font size
    private static final int MIN_FONT_SIZE = 8;
    private static final int MAX_FONT_SIZE = 32;
    
    // CSS stylesheet paths
    private static final String LIGHT_THEME_CSS = "/ide-styles.css";
    private static final String DARK_THEME_CSS = "/dark-theme.css";
    
    // Scene reference
    private final Scene scene;
    
    // Text areas to manage
    private TextArea codeEditor;
    private TextArea consoleOutput;
    
    /**
     * Creates a new theme manager with a reference to the scene
     * 
     * @param scene The application scene
     */
    public ThemeManager(Scene scene) {
        this.scene = scene;
    }
    
    /**
     * Creates a new theme manager with a reference to the scene and text areas
     * 
     * @param scene The application scene
     * @param codeEditor The code editor text area
     * @param consoleOutput The console output text area
     */
    public ThemeManager(Scene scene, TextArea codeEditor, TextArea consoleOutput) {
        this.scene = scene;
        this.codeEditor = codeEditor;
        this.consoleOutput = consoleOutput;
        
        // Apply initial font size
        updateFontSize();
    }
    
    /**
     * Toggles between dark and light mode
     * 
     * @return The current dark mode state after toggling
     */
    public boolean toggleDarkMode() {
        darkModeEnabled = !darkModeEnabled;
        updateTheme();
        return darkModeEnabled;
    }
    
    /**
     * Updates the scene's stylesheets based on the current theme
     */
    private void updateTheme() {
        scene.getStylesheets().clear();
        
        if (darkModeEnabled) {
            // Add dark theme first, then light theme (dark theme will override light theme styles)
            scene.getStylesheets().add(getClass().getResource(LIGHT_THEME_CSS).toExternalForm());
            scene.getStylesheets().add(getClass().getResource(DARK_THEME_CSS).toExternalForm());
        } else {
            // Light theme only
            scene.getStylesheets().add(getClass().getResource(LIGHT_THEME_CSS).toExternalForm());
        }
    }
    
    /**
     * Returns whether dark mode is currently enabled
     * 
     * @return true if dark mode is enabled
     */
    public boolean isDarkModeEnabled() {
        return darkModeEnabled;
    }
    
    /**
     * Apply initial theme to the scene
     */
    public void applyInitialTheme() {
        updateTheme();
    }
    
    /**
     * Increases the font size
     * 
     * @return The new font size
     */
    public int increaseFontSize() {
        if (currentFontSize < MAX_FONT_SIZE) {
            currentFontSize += 2;
            updateFontSize();
        }
        return currentFontSize;
    }
    
    /**
     * Decreases the font size
     * 
     * @return The new font size
     */
    public int decreaseFontSize() {
        if (currentFontSize > MIN_FONT_SIZE) {
            currentFontSize -= 2;
            updateFontSize();
        }
        return currentFontSize;
    }
    
    /**
     * Gets the current font size
     * 
     * @return The current font size
     */
    public int getCurrentFontSize() {
        return currentFontSize;
    }
    
    /**
     * Updates the font size for the text areas
     */
    private void updateFontSize() {
        if (codeEditor != null && consoleOutput != null) {
            String fontStyle = String.format("-fx-font-family: 'monospace'; -fx-font-size: %dpx;", currentFontSize);
            codeEditor.setStyle(fontStyle);
            consoleOutput.setStyle(fontStyle);
        }
    }
    
    /**
     * Sets the text areas to manage font size for
     * 
     * @param codeEditor The code editor text area
     * @param consoleOutput The console output text area
     */
    public void setTextAreas(TextArea codeEditor, TextArea consoleOutput) {
        this.codeEditor = codeEditor;
        this.consoleOutput = consoleOutput;
        updateFontSize();
    }
}