package com.example;

import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;

/**
 * Factory class for creating UI components with consistent styling
 */
public class UIFactory {
    
    /**
     * Creates an action button with consistent styling
     * 
     * @param text Button text
     * @param icon Icon prefix
     * @param shortcutKey Keyboard shortcut description
     * @return A styled button
     */
    public static Button createActionButton(String text, String icon, String shortcutKey) {
        Button button = new Button(icon + " " + text);
        button.setTooltip(new Tooltip(text + " (" + shortcutKey + ")"));
        button.getStyleClass().add("action-button");
        button.setFocusTraversable(false);
        return button;
    }
}