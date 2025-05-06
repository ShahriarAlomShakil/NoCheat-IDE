package com.example;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.paint.CycleMethod;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;

/**
 * Manages the log panel functionality for the application
 */
public class LogManager {
    private TextFlow logPanel;
    private ScrollPane logScrollPane;
    private DateTimeFormatter logTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
    private DateTimeFormatter startTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private LocalDateTime applicationStartTime;
    private int cheatingAttempts = 0;
    private Label cheatingAttemptsLabel;
    private Label startTimeLabel;
    private Label headerLabel;
    private VBox statsBox;
    private boolean isDarkMode = false;
    
    // Log categories
    public enum LogCategory {
        GENERAL,
        SECURITY,
        CHEATING,
        FILE,
        COMPILE
    }
    
    public LogManager() {
        // Record application start time when LogManager is instantiated
        applicationStartTime = LocalDateTime.now();
    }
    
    /**
     * Creates and initializes the log panel
     * 
     * @return A VBox containing the log panel and its header
     */
    public VBox createLogPanel() {
        // Create log panel title with enhanced styling
        headerLabel = new Label("SECURITY & CHEATING LOGS");
        headerLabel.getStyleClass().add("section-header");
        headerLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
        headerLabel.setAlignment(Pos.CENTER);
        headerLabel.setMaxWidth(Double.MAX_VALUE);
        headerLabel.setPadding(new Insets(10, 5, 10, 5));
        
        // Add a rectangular background for the header
        Rectangle headerBackground = new Rectangle();
        headerBackground.getStyleClass().add("header-background");
        headerBackground.setHeight(40);
        headerBackground.setWidth(250); // Will be resized with parent
        
        // Create the text flow panel for logs with improved styling
        logPanel = new TextFlow();
        logPanel.getStyleClass().add("log-panel");
        logPanel.setPadding(new Insets(10));
        
        // Wrap in scroll pane with enhanced styling
        logScrollPane = new ScrollPane(logPanel);
        logScrollPane.setFitToWidth(true);
        logScrollPane.setFitToHeight(true);
        logScrollPane.getStyleClass().add("log-scroll-pane");
        
        // Create start time label with improved styling
        startTimeLabel = new Label("Started: " + applicationStartTime.format(startTimeFormatter));
        startTimeLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        startTimeLabel.setTextFill(Color.BLUE);
        startTimeLabel.setPadding(new Insets(5, 0, 5, 0));
        startTimeLabel.setAlignment(Pos.CENTER);
        startTimeLabel.setMaxWidth(Double.MAX_VALUE);
        
        // Create cheating attempts counter label with pulse effect for dark mode
        cheatingAttemptsLabel = new Label("Cheating Attempts: 0");
        cheatingAttemptsLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        cheatingAttemptsLabel.setTextFill(Color.RED);
        cheatingAttemptsLabel.setPadding(new Insets(8, 0, 8, 0));
        cheatingAttemptsLabel.setAlignment(Pos.CENTER);
        cheatingAttemptsLabel.setMaxWidth(Double.MAX_VALUE);
        
        // Create stats container for the bottom of the log panel
        statsBox = new VBox(5, startTimeLabel, cheatingAttemptsLabel);
        statsBox.setAlignment(Pos.CENTER);
        statsBox.getStyleClass().add("stats-container");
        statsBox.setPadding(new Insets(10, 5, 10, 5));
        
        // Create log panel container with a vertical layout
        VBox logBox = new VBox(5);
        logBox.getChildren().addAll(headerLabel, logScrollPane, statsBox);
        VBox.setVgrow(logScrollPane, Priority.ALWAYS);
        logBox.setPadding(new Insets(0, 0, 0, 0));
        logBox.getStyleClass().add("log-container");
        
        // Set width constraints
        logBox.setMinWidth(200);
        logBox.setPrefWidth(250);
        logBox.setMaxWidth(350);
        
        // Add initial log entry
        addLogEntry("Log system initialized", LogCategory.GENERAL);
        addLogEntry("NoCheat IDE started", LogCategory.GENERAL);
        addLogEntry("Security monitoring active", LogCategory.SECURITY);
        
        return logBox;
    }
    
    /**
     * Adds a log entry to the log panel with GENERAL category
     * 
     * @param message The message to log
     */
    public void addLogEntry(String message) {
        addLogEntry(message, LogCategory.GENERAL);
    }
    
    /**
     * Adds a log entry to the log panel with specified category
     * 
     * @param message The message to log
     * @param category The log category
     */
    public void addLogEntry(String message, LogCategory category) {
        // If it's a cheating attempt, increment the counter and update UI
        if (category == LogCategory.CHEATING) {
            cheatingAttempts++;
            if (cheatingAttemptsLabel != null) {
                Platform.runLater(() -> {
                    // Update the counter text
                    cheatingAttemptsLabel.setText("Cheating Attempts: " + cheatingAttempts);
                    
                    // Add highlight effect for new cheating attempt
                    DropShadow glow = new DropShadow();
                    glow.setColor(isDarkMode ? Color.TOMATO : Color.RED);
                    glow.setWidth(20);
                    glow.setHeight(20);
                    cheatingAttemptsLabel.setEffect(glow);
                    
                    // After a short delay, remove the glow effect
                    new Thread(() -> {
                        try {
                            Thread.sleep(2000);
                            Platform.runLater(() -> cheatingAttemptsLabel.setEffect(null));
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }).start();
                });
            }
        }
        
        // Only display security and cheating related logs
        if (category != LogCategory.SECURITY && category != LogCategory.CHEATING) {
            return;
        }
        
        Platform.runLater(() -> {
            // Create a timestamp
            String timestamp = LocalDateTime.now().format(logTimeFormatter);
            
            // Create text nodes for timestamp and message
            Text timestampText = new Text("[" + timestamp + "] ");
            timestampText.getStyleClass().add("log-timestamp");
            
            // Create category text with distinctive styling
            Text categoryText = new Text("[" + category.name() + "] ");
            categoryText.getStyleClass().add("log-category");
            categoryText.getStyleClass().add("log-category-" + category.name().toLowerCase());
            
            Text messageText = new Text(message + "\n");
            messageText.getStyleClass().add("log-message");
            
            // Apply different styling based on log category and theme
            if (isDarkMode) {
                // Dark mode styling
                switch(category) {
                    case SECURITY:
                        timestampText.setFill(Color.LIGHTBLUE);
                        categoryText.setFill(Color.DEEPSKYBLUE);
                        messageText.setFill(Color.LIGHTCYAN);
                        timestampText.setFont(Font.font("monospace", FontWeight.NORMAL, 12));
                        categoryText.setFont(Font.font("monospace", FontWeight.BOLD, 12));
                        messageText.setFont(Font.font("monospace", FontWeight.NORMAL, 12));
                        break;
                    case CHEATING:
                        // Create a glow effect for cheating alerts in dark mode
                        Glow glow = new Glow(0.5);
                        
                        timestampText.setFill(Color.TOMATO);
                        categoryText.setFill(Color.ORANGERED);
                        messageText.setFill(Color.CORAL);
                        timestampText.setFont(Font.font("monospace", FontWeight.BOLD, 12));
                        categoryText.setFont(Font.font("monospace", FontWeight.BOLD, 12));
                        messageText.setFont(Font.font("monospace", FontWeight.BOLD, 12));
                        
                        // Apply glow effect to all text components
                        timestampText.setEffect(glow);
                        categoryText.setEffect(glow);
                        messageText.setEffect(glow);
                        break;
                    default:
                        timestampText.setFill(Color.LIGHTGRAY);
                        categoryText.setFill(Color.WHITE);
                        messageText.setFill(Color.LIGHTGRAY);
                }
            } else {
                // Light mode styling
                switch(category) {
                    case SECURITY:
                        timestampText.setFill(Color.DARKBLUE);
                        categoryText.setFill(Color.BLUE);
                        messageText.setFill(Color.BLACK);
                        timestampText.setFont(Font.font("monospace", FontWeight.NORMAL, 12));
                        categoryText.setFont(Font.font("monospace", FontWeight.BOLD, 12));
                        messageText.setFont(Font.font("monospace", FontWeight.NORMAL, 12));
                        break;
                    case CHEATING:
                        // For cheating, set all text components to red
                        timestampText.setFill(Color.DARKRED);
                        categoryText.setFill(Color.RED);
                        messageText.setFill(Color.RED);
                        timestampText.setFont(Font.font("monospace", FontWeight.BOLD, 12));
                        categoryText.setFont(Font.font("monospace", FontWeight.BOLD, 12));
                        messageText.setFont(Font.font("monospace", FontWeight.BOLD, 12));
                        break;
                    default:
                        timestampText.setFill(Color.GRAY);
                        categoryText.setFill(Color.BLACK);
                        messageText.setFill(Color.BLACK);
                }
            }
            
            // Add to the log panel
            logPanel.getChildren().addAll(timestampText, categoryText, messageText);
            
            // Auto-scroll to the bottom
            logScrollPane.setVvalue(1.0);
        });
    }
    
    /**
     * Updates the log panel theme based on dark mode state
     * 
     * @param isDarkMode Whether dark mode is enabled
     */
    public void updateLogPanelTheme(boolean isDarkMode) {
        this.isDarkMode = isDarkMode;
        VBox logContainer = (VBox) logScrollPane.getParent();
        
        if (isDarkMode) {
            // Apply dark mode classes
            logContainer.getStyleClass().add("dark-mode");
            logPanel.getStyleClass().add("dark-mode");
            logScrollPane.getStyleClass().add("dark-mode");
            statsBox.getStyleClass().add("dark-mode");
            
            // Apply dark mode header styling
            headerLabel.setTextFill(Color.LIGHTGOLDENRODYELLOW);
            headerLabel.setStyle("-fx-background-color: #2c2c2c; -fx-border-color: #555555; -fx-border-width: 0 0 1 0;");
            
            // Update stats labels for dark mode
            startTimeLabel.setTextFill(Color.LIGHTSKYBLUE);
            
            // Create a special effect for the cheating attempts counter in dark mode
            cheatingAttemptsLabel.setTextFill(Color.TOMATO);
            cheatingAttemptsLabel.setStyle("-fx-background-color: #3c2c2c; -fx-border-radius: 5; -fx-background-radius: 5;");
        } else {
            // Remove dark mode classes
            logContainer.getStyleClass().remove("dark-mode");
            logPanel.getStyleClass().remove("dark-mode");
            logScrollPane.getStyleClass().remove("dark-mode");
            statsBox.getStyleClass().remove("dark-mode");
            
            // Reset light mode header styling
            headerLabel.setTextFill(Color.BLACK);
            headerLabel.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #cccccc; -fx-border-width: 0 0 1 0;");
            
            // Reset stats labels for light mode
            startTimeLabel.setTextFill(Color.BLUE);
            
            // Reset cheating attempts label for light mode
            cheatingAttemptsLabel.setTextFill(Color.RED);
            cheatingAttemptsLabel.setStyle("-fx-background-color: #fff0f0; -fx-border-radius: 5; -fx-background-radius: 5;");
        }
        
        // Update existing log entries to match the new theme
        updateExistingLogEntries();
    }
    
    /**
     * Updates existing log entries to match the current theme
     */
    private void updateExistingLogEntries() {
        // Iterate through all children of the logPanel
        for (int i = 0; i < logPanel.getChildren().size(); i += 3) { // Groups of 3: timestamp, category, message
            if (i + 2 >= logPanel.getChildren().size()) break;
            
            Text timestampText = (Text) logPanel.getChildren().get(i);
            Text categoryText = (Text) logPanel.getChildren().get(i + 1);
            Text messageText = (Text) logPanel.getChildren().get(i + 2);
            
            // Determine the category from the category text
            String categoryStr = categoryText.getText().trim();
            if (categoryStr.startsWith("[") && categoryStr.endsWith("]")) {
                categoryStr = categoryStr.substring(1, categoryStr.length() - 1);
                LogCategory category;
                try {
                    category = LogCategory.valueOf(categoryStr);
                } catch (IllegalArgumentException e) {
                    category = LogCategory.GENERAL;
                }
                
                // Apply appropriate styles based on category and current theme
                if (isDarkMode) {
                    switch(category) {
                        case SECURITY:
                            timestampText.setFill(Color.LIGHTBLUE);
                            categoryText.setFill(Color.DEEPSKYBLUE);
                            messageText.setFill(Color.LIGHTCYAN);
                            break;
                        case CHEATING:
                            timestampText.setFill(Color.TOMATO);
                            categoryText.setFill(Color.ORANGERED);
                            messageText.setFill(Color.CORAL);
                            break;
                        default:
                            timestampText.setFill(Color.LIGHTGRAY);
                            categoryText.setFill(Color.WHITE);
                            messageText.setFill(Color.LIGHTGRAY);
                    }
                } else {
                    switch(category) {
                        case SECURITY:
                            timestampText.setFill(Color.DARKBLUE);
                            categoryText.setFill(Color.BLUE);
                            messageText.setFill(Color.BLACK);
                            break;
                        case CHEATING:
                            timestampText.setFill(Color.DARKRED);
                            categoryText.setFill(Color.RED);
                            messageText.setFill(Color.RED);
                            break;
                        default:
                            timestampText.setFill(Color.GRAY);
                            categoryText.setFill(Color.BLACK);
                            messageText.setFill(Color.BLACK);
                    }
                }
            }
        }
    }
    
    /**
     * Gets the log scroll pane
     * 
     * @return The log scroll pane
     */
    public ScrollPane getLogScrollPane() {
        return logScrollPane;
    }
    
    /**
     * Gets the application start time
     * 
     * @return The application start time
     */
    public LocalDateTime getApplicationStartTime() {
        return applicationStartTime;
    }
    
    /**
     * Gets the number of cheating attempts
     * 
     * @return The number of cheating attempts
     */
    public int getCheatingAttempts() {
        return cheatingAttempts;
    }
}