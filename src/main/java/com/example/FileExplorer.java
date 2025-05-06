package com.example;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.function.Consumer;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

/**
 * FileExplorer component that shows a tree view of files and directories
 */
public class FileExplorer extends VBox {
    
    private final TreeView<File> treeView;
    private final FileManager fileManager;
    private File rootDirectory;
    private Consumer<File> onFileSelectedCallback;
    
    // Default NoCheat_IDE directory
    private static final String DEFAULT_DIR = System.getProperty("user.home") + File.separator + "Documents" + File.separator + "NoCheat_IDE";
    
    // Icons for file types
    private final ImageView folderIcon = createIcon("/folder-icon.png", "Folder");
    private final ImageView fileIcon = createIcon("/file-icon.png", "File");
    private final ImageView codeIcon = createIcon("/code-icon.png", "Code");
    
    public FileExplorer(FileManager fileManager) {
        this.fileManager = fileManager;
        
        // Create toolbar with refresh button only
        Button refreshBtn = UIFactory.createActionButton("Refresh", "🔄", "");
        
        // Create tree view
        treeView = new TreeView<>();
        treeView.setShowRoot(true);
        
        // Set the tree view to take full size
        VBox.setVgrow(treeView, javafx.scene.layout.Priority.ALWAYS);
        
        // Refresh button action
        refreshBtn.setOnAction(e -> refresh());
        
        // Set up tree view cell factory for custom rendering of files
        treeView.setCellFactory(tv -> new TreeCell<>() {
            @Override
            protected void updateItem(File item, boolean empty) {
                super.updateItem(item, empty);
                
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(item.getName());
                    if (item.isDirectory()) {
                        setGraphic(new ImageView(folderIcon.getImage()));
                    } else {
                        // Use code icon for recognized file types
                        if (isCodeFile(item.getName())) {
                            setGraphic(new ImageView(codeIcon.getImage()));
                        } else {
                            setGraphic(new ImageView(fileIcon.getImage()));
                        }
                    }
                }
            }
        });
        
        // Handle double-click on files
        treeView.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                TreeItem<File> selectedItem = treeView.getSelectionModel().getSelectedItem();
                if (selectedItem != null && !selectedItem.getValue().isDirectory()) {
                    if (onFileSelectedCallback != null) {
                        onFileSelectedCallback.accept(selectedItem.getValue());
                    }
                }
            }
        });
        
        // Add components to the VBox
        getChildren().addAll(refreshBtn, treeView);
        setSpacing(5);
        
        // Make the explorer take all available space
        setFillWidth(true);
        
        // Initialize the default directory
        initializeDefaultDirectory();
    }
    
    /**
     * Initializes the default NoCheat_IDE directory
     */
    private void initializeDefaultDirectory() {
        File defaultDir = new File(DEFAULT_DIR);
        
        try {
            // Create the directory if it doesn't exist
            if (!defaultDir.exists()) {
                if (!defaultDir.mkdirs()) {
                    System.err.println("Failed to create directory: " + DEFAULT_DIR);
                }
            } else {
                // Clean the directory (delete all files and folders within it)
                cleanDirectory(defaultDir);
            }
            
            // Set as root directory
            setRootDirectory(defaultDir);
            
        } catch (IOException e) {
            System.err.println("Error initializing default directory: " + e.getMessage());
        }
    }
    
    /**
     * Cleans a directory by deleting all its contents
     * 
     * @param directory The directory to clean
     * @throws IOException If deletion fails
     */
    private void cleanDirectory(File directory) throws IOException {
        if (!directory.isDirectory()) {
            return;
        }
        
        // Walk through directory and delete all files and subdirectories
        Files.walk(directory.toPath())
            .sorted(Comparator.reverseOrder())
            .map(Path::toFile)
            .filter(file -> !file.equals(directory)) // Don't delete the root directory itself
            .forEach(File::delete);
    }
    
    /**
     * Sets the root directory for the file explorer
     * 
     * @param directory The directory to set as root
     */
    public void setRootDirectory(File directory) {
        if (directory == null || !directory.isDirectory()) {
            return;
        }
        
        rootDirectory = directory;
        TreeItem<File> rootItem = new TreeItem<>(rootDirectory);
        rootItem.setGraphic(new ImageView(folderIcon.getImage()));
        
        // Populate the root node
        populateTreeItem(rootItem);
        
        // Expand the root node
        rootItem.setExpanded(true);
        
        treeView.setRoot(rootItem);
    }
    
    /**
     * Recursively populates a tree item with its children
     * 
     * @param treeItem The tree item to populate
     */
    private void populateTreeItem(TreeItem<File> treeItem) {
        File file = treeItem.getValue();
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                // Sort directories first, then files
                java.util.Arrays.sort(files, Comparator.<File, Boolean>comparing(f -> !f.isDirectory())
                        .thenComparing(File::getName));
                
                for (File childFile : files) {
                    // Skip hidden files
                    if (childFile.isHidden()) {
                        continue;
                    }
                    
                    TreeItem<File> childItem = new TreeItem<>(childFile);
                    treeItem.getChildren().add(childItem);
                    
                    // Handle directories - populate immediately and expand by default
                    if (childFile.isDirectory()) {
                        childItem.setGraphic(new ImageView(folderIcon.getImage()));
                        populateTreeItem(childItem);
                        childItem.setExpanded(true);
                    } else {
                        if (isCodeFile(childFile.getName())) {
                            childItem.setGraphic(new ImageView(codeIcon.getImage()));
                        } else {
                            childItem.setGraphic(new ImageView(fileIcon.getImage()));
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Sets a callback for when a file is selected
     * 
     * @param callback The callback function
     */
    public void setOnFileSelected(Consumer<File> callback) {
        this.onFileSelectedCallback = callback;
    }
    
    /**
     * Checks if a file is a recognized code file
     * 
     * @param fileName The file name to check
     * @return True if it's a code file
     */
    private boolean isCodeFile(String fileName) {
        fileName = fileName.toLowerCase();
        return fileName.endsWith(".c") || fileName.endsWith(".cpp") || fileName.endsWith(".h") || 
               fileName.endsWith(".hpp") || fileName.endsWith(".java") || fileName.endsWith(".py") ||
               fileName.endsWith(".js") || fileName.endsWith(".html") || fileName.endsWith(".css");
    }
    
    /**
     * Helper method to create an icon
     */
    private ImageView createIcon(String path, String fallbackText) {
        try {
            // Try to load the icon from resources
            return new ImageView(new Image(getClass().getResourceAsStream(path)));
        } catch (Exception e) {
            // If icon not found, create a blank icon (we'll show text instead)
            return new ImageView();
        }
    }
    
    /**
     * Refreshes the file explorer view
     */
    public void refresh() {
        if (rootDirectory != null) {
            setRootDirectory(rootDirectory);
        } else {
            initializeDefaultDirectory();
        }
    }
    
    /**
     * Returns the root directory being displayed
     */
    public File getRootDirectory() {
        return rootDirectory;
    }
}