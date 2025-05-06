package com.example;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * Manages layout creation and configuration for the application
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
    
    /**
     * Creates a flexible spacer for layout
     * 
     * @return A Region that acts as a flexible spacer
     */
    public static Region createSpacer() {
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        return spacer;
    }
    
    /**
     * Creates the top bar for the application layout
     * 
     * @param languageSelector The language selector combo box
     * @param newFileBtn New file button
     * @param saveBtn Save file button
     * @param decreaseFontBtn Decrease font size button
     * @param increaseFontBtn Increase font size button 
     * @param darkModeToggleBtn Dark mode toggle button
     * @param exitBtn Exit button
     * @return The top bar HBox layout
     */
    public static HBox createTopBar(ComboBox<String> languageSelector, 
                                   Button newFileBtn, Button saveBtn,
                                   Button decreaseFontBtn, Button increaseFontBtn,
                                   Button darkModeToggleBtn, Button exitBtn) {
        HBox topBar = new HBox(10, new Label("Language:"), languageSelector, newFileBtn, saveBtn, 
                             createSpacer(), decreaseFontBtn, increaseFontBtn, darkModeToggleBtn, exitBtn);
        topBar.setPadding(new Insets(10));
        topBar.setAlignment(Pos.CENTER_LEFT);
        return topBar;
    }
    
    /**
     * Creates the action toolbar with compile and run button
     * 
     * @param compileAndRunBtn Compile and run button
     * @return The action toolbar HBox layout
     */
    public static HBox createActionToolbar(Button compileAndRunBtn) {
        HBox actionToolbar = new HBox(15, compileAndRunBtn);
        actionToolbar.setAlignment(Pos.CENTER);
        actionToolbar.setPadding(new Insets(10));
        actionToolbar.getStyleClass().add("action-toolbar");
        return actionToolbar;
    }
    
    /**
     * Creates the console panel with resize buttons
     * 
     * @param consoleOutput The console output text area
     * @return The console panel VBox layout with resize button references
     */
    public static VBox createConsolePanel(TextArea consoleOutput) {
        // Labels for console section
        Label consoleLabel = new Label("Console Output:");
        
        // Create console resize buttons
        Button expandConsoleBtn = new Button("▲");
        expandConsoleBtn.setTooltip(new Tooltip("Expand console"));
        expandConsoleBtn.getStyleClass().add("console-resize-button");
        expandConsoleBtn.setId("expand-console-btn");
        
        Button shrinkConsoleBtn = new Button("▼");
        shrinkConsoleBtn.setTooltip(new Tooltip("Shrink console"));
        shrinkConsoleBtn.getStyleClass().add("console-resize-button");
        shrinkConsoleBtn.setId("shrink-console-btn");
        
        // Console header with label and resize buttons
        HBox consoleHeader = new HBox(5, consoleLabel, createSpacer(), shrinkConsoleBtn, expandConsoleBtn);
        consoleHeader.setAlignment(Pos.CENTER_LEFT);
        
        // Create console panel
        VBox consoleBox = new VBox(5, consoleHeader, consoleOutput);
        VBox.setVgrow(consoleOutput, Priority.ALWAYS);
        consoleBox.setPadding(new Insets(10));
        
        // Store the buttons as properties on the consoleBox for later access
        consoleBox.getProperties().put("expandButton", expandConsoleBtn);
        consoleBox.getProperties().put("shrinkButton", shrinkConsoleBtn);
        
        return consoleBox;
    }
    
    /**
     * Creates the editor panel
     * 
     * @param codeEditor The code editor text area
     * @return The editor panel VBox layout
     */
    public static VBox createEditorPanel(TextArea codeEditor) {
        Label editorLabel = new Label("Code Editor:");
        VBox editorBox = new VBox(editorLabel, codeEditor);
        VBox.setVgrow(codeEditor, Priority.ALWAYS);
        editorBox.setPadding(new Insets(10));
        return editorBox;
    }
    
    /**
     * Creates the file explorer panel with title
     * 
     * @param fileExplorer The file explorer component
     * @return The file explorer panel VBox layout
     */
    public static VBox createFileExplorerPanel(FileExplorer fileExplorer) {
        // Create the file explorer title
        Label explorerTitle = new Label("NoCheat_IDE Files");
        
        // Wrap file explorer in a VBox with a title
        VBox fileExplorerBox = new VBox(explorerTitle, fileExplorer);
        fileExplorerBox.setPadding(new Insets(10));
        VBox.setVgrow(fileExplorer, Priority.ALWAYS);
        
        // Set the file explorer box to fully expand vertically
        fileExplorerBox.setPrefHeight(Double.MAX_VALUE);
        
        // Set minimum and maximum width for the file explorer
        fileExplorerBox.setMinWidth(180);
        fileExplorerBox.setPrefWidth(220);
        fileExplorerBox.setMaxWidth(300);
        
        return fileExplorerBox;
    }
    
    /**
     * Creates the main split pane layout with file explorer, editor/console, and log panel
     * 
     * @param fileExplorerBox File explorer panel
     * @param centerContent Center content with editor and console
     * @param logBox Log panel
     * @return The configured horizontal split pane
     */
    public static SplitPane createMainSplitPane(VBox fileExplorerBox, Node centerContent, VBox logBox) {
        // Create horizontal split pane for file explorer and main content
        SplitPane horizontalSplitPane = new SplitPane();
        horizontalSplitPane.setOrientation(Orientation.HORIZONTAL);
        
        // Configure the center content to be fully resizable
        if (centerContent instanceof Region) {
            ((Region) centerContent).setMinWidth(300);
            ((Region) centerContent).setPrefWidth(Region.USE_COMPUTED_SIZE);
        }
        
        // Add file explorer, center content, and log panel to the horizontal split pane
        horizontalSplitPane.getItems().addAll(fileExplorerBox, centerContent, logBox);
        
        // Set initial divider position (15% for file explorer, 65% for center, 20% for logs)
        Platform.runLater(() -> horizontalSplitPane.setDividerPositions(0.15, 0.85));
        
        // Make SplitPane contents resizable with constraints
        SplitPane.setResizableWithParent(fileExplorerBox, true);
        SplitPane.setResizableWithParent(centerContent, true);
        SplitPane.setResizableWithParent(logBox, true);
        
        return horizontalSplitPane;
    }
    
    /**
     * Creates the vertical split pane for editor and console
     * 
     * @param editorBox Editor panel
     * @param consoleBox Console panel
     * @return The configured vertical split pane
     */
    public static SplitPane createEditorConsoleSplitPane(VBox editorBox, VBox consoleBox) {
        // Create vertical SplitPane to allow resizing between editor and console
        SplitPane verticalSplitPane = new SplitPane();
        verticalSplitPane.setOrientation(Orientation.VERTICAL);
        verticalSplitPane.getItems().addAll(editorBox, consoleBox);
        
        // Set initial divider position (70% for editor, 30% for console)
        Platform.runLater(() -> verticalSplitPane.setDividerPositions(0.7));
        
        // Get references to the console resize buttons stored as properties
        Button expandConsoleBtn = (Button) consoleBox.getProperties().get("expandButton");
        Button shrinkConsoleBtn = (Button) consoleBox.getProperties().get("shrinkButton");
        
        // Add event handlers to console resize buttons
        if (expandConsoleBtn != null) {
            expandConsoleBtn.setOnAction(e -> {
                // Increase console size (move divider up by 10%)
                double currentPos = verticalSplitPane.getDividerPositions()[0];
                double newPos = Math.max(0.3, currentPos - 0.1); // Don't let console get too small
                verticalSplitPane.setDividerPositions(newPos);
            });
        }
        
        if (shrinkConsoleBtn != null) {
            shrinkConsoleBtn.setOnAction(e -> {
                // Decrease console size (move divider down by 10%)
                double currentPos = verticalSplitPane.getDividerPositions()[0];
                double newPos = Math.min(0.9, currentPos + 0.1); // Don't let editor get too small
                verticalSplitPane.setDividerPositions(newPos);
            });
        }
        
        return verticalSplitPane;
    }
    
    /**
     * Creates the main layout with all components
     * 
     * @param topBar Top bar with controls
     * @param horizontalSplitPane Main split pane with all content
     * @return The main BorderPane layout
     */
    public static BorderPane createMainLayout(HBox topBar, SplitPane horizontalSplitPane) {
        BorderPane mainLayout = new BorderPane();
        mainLayout.setTop(topBar);
        mainLayout.setCenter(horizontalSplitPane);
        
        // Allow the SplitPane to resize with the window
        BorderPane.setMargin(horizontalSplitPane, new Insets(0));
        
        return mainLayout;
    }
}