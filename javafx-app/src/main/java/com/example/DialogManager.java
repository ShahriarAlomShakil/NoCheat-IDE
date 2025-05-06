package com.example;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.stage.Window;

/**
 * Manages dialog creation and interaction for the application
 */
public class DialogManager {
    // Dialog tracking for focus management
    private final List<Stage> activeDialogs = new ArrayList<>();
    private final ChangeListener<Boolean> dialogFocusListener = (obs, oldVal, newVal) -> {
        // This listener allows us to track when dialogs gain/lose focus
    };
    
    /**
     * Shows a language selection dialog
     * 
     * @return The selected language or null if cancelled
     */
    public String showLanguageSelectionDialog() {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Select Programming Language");
        dialog.setHeaderText("Please select a programming language:");
        
        // Set the button types
        ButtonType selectButtonType = new ButtonType("Select");
        dialog.getDialogPane().getButtonTypes().addAll(selectButtonType, ButtonType.CANCEL);
        
        // Create the language selection ComboBox
        ComboBox<String> languageCombo = new ComboBox<>();
        languageCombo.getItems().addAll("C", "C++", "Java", "Python", "R");
        languageCombo.setValue("C++"); // Default selection
        
        // Create the grid and add the language ComboBox
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        grid.add(new Label("Language:"), 0, 0);
        grid.add(languageCombo, 1, 0);
        
        dialog.getDialogPane().setContent(grid);
        
        // Request focus on the ComboBox by default
        Platform.runLater(languageCombo::requestFocus);
        
        // Convert the result to string when the select button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == selectButtonType) {
                return languageCombo.getValue();
            }
            return null;
        });
        
        Optional<String> result = dialog.showAndWait();
        return result.orElse(null);
    }
    
    /**
     * Shows a confirmation dialog when the user attempts to exit the application
     * 
     * @param stage The main application stage
     * @return true if user confirms exit, false otherwise
     */
    public boolean showExitConfirmation(Stage stage) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Exit Confirmation");
        alert.setHeaderText("Warning: Your coding data will be deleted!");
        alert.setContentText("All your code and files will be permanently deleted if you exit. Are you sure you want to exit?");
        
        ButtonType buttonTypeYes = new ButtonType("Yes, Exit");
        ButtonType buttonTypeCancel = new ButtonType("Cancel");
        
        alert.getButtonTypes().setAll(buttonTypeYes, buttonTypeCancel);
        
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == buttonTypeYes;
    }
    
    /**
     * Checks if any dialog is currently showing
     * 
     * @param mainWindow The main application window
     * @return true if a dialog is showing, false otherwise
     */
    public boolean isDialogShowing(Window mainWindow) {
        // Check our tracked dialogs
        for (Stage dialogStage : activeDialogs) {
            if (dialogStage.isShowing()) {
                return true;
            }
        }
        
        // Search for any dialogs in the scene
        for (Window window : Window.getWindows()) {
            if (window instanceof Stage && window.isShowing() && window != mainWindow) {
                // Found a dialog or secondary window
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Adds a dialog to the tracked dialog list
     * 
     * @param dialog The dialog stage to track
     */
    public void trackDialog(Stage dialog) {
        activeDialogs.add(dialog);
        dialog.focusedProperty().addListener(dialogFocusListener);
    }
    
    /**
     * Removes a dialog from the tracked dialog list
     * 
     * @param dialog The dialog stage to stop tracking
     */
    public void untrackDialog(Stage dialog) {
        activeDialogs.remove(dialog);
        dialog.focusedProperty().removeListener(dialogFocusListener);
    }
}