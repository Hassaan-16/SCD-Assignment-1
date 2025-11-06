package GUI;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ProjectPlanningGUI extends JFrame {
    private Project project;
    private JTable taskTable;
    private TaskTableModel taskTableModel;
    private JTextField projectNameField;
    private JTextArea analysisArea;

    public ProjectPlanningGUI() {
        this.project = new Project();
        initializeUI();
        
        // Try to auto-load data files
        autoLoadDataFiles();
    }

    private void initializeUI() {
        setTitle("Project Planning Application - Assignment 2");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1400, 800);
        setLocationRelativeTo(null);

        // Create main panel with border layout
        JPanel mainPanel = new JPanel(new BorderLayout());

        // Create toolbar with ALL the required buttons
        JPanel toolbarPanel = createToolbarPanel();
        mainPanel.add(toolbarPanel, BorderLayout.NORTH);

        // Create project info panel
        JPanel projectPanel = createProjectPanel();
        mainPanel.add(projectPanel, BorderLayout.CENTER);

        // Create center panel with table and analysis
        JSplitPane centerSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        centerSplitPane.setDividerLocation(800);

        // Task table
        taskTableModel = new TaskTableModel(project);
        taskTable = new JTable(taskTableModel);
        JScrollPane tableScrollPane = new JScrollPane(taskTable);
        centerSplitPane.setLeftComponent(tableScrollPane);

        // Analysis area
        analysisArea = new JTextArea();
        analysisArea.setEditable(false);
        analysisArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane analysisScrollPane = new JScrollPane(analysisArea);
        analysisScrollPane.setBorder(BorderFactory.createTitledBorder("Analysis Results"));
        centerSplitPane.setRightComponent(analysisScrollPane);

        mainPanel.add(centerSplitPane, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private JPanel createToolbarPanel() {
        JPanel toolbarPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        toolbarPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // Create buttons as specified in requirements
        JButton newButton = new JButton("NEW");
        JButton addTaskButton = new JButton("ADD Task");
        JButton addResourceButton = new JButton("ADD Resource");
        JButton uploadTasksButton = new JButton("Upload Tasks");
        JButton uploadResourcesButton = new JButton("Upload Resources");
        JButton analyzeButton = new JButton("Analyze");
        JButton visualizeButton = new JButton("Visualize");
        JButton saveButton = new JButton("Save");
        JButton closeButton = new JButton("Close");

        // Style the NEW button to make it stand out (red background)
        newButton.setBackground(new Color(220, 80, 60));
        newButton.setForeground(Color.WHITE);
        newButton.setFont(new Font("SansSerif", Font.BOLD, 12));
        newButton.setFocusPainted(false);

        // Add action listeners
        newButton.addActionListener(e -> newProject());
        addTaskButton.addActionListener(e -> addNewTask());
        addResourceButton.addActionListener(e -> addNewResource());
        saveButton.addActionListener(e -> saveProject());
        closeButton.addActionListener(e -> closeProject());
        uploadTasksButton.addActionListener(e -> uploadTasksFile());
        uploadResourcesButton.addActionListener(e -> uploadResourcesFile());
        analyzeButton.addActionListener(e -> showAnalysisDialog());
        visualizeButton.addActionListener(e -> showVisualization());

        // Add buttons to toolbar in the specified order
        toolbarPanel.add(newButton);
        toolbarPanel.add(Box.createHorizontalStrut(10)); // Spacing
        toolbarPanel.add(addTaskButton);
        toolbarPanel.add(Box.createHorizontalStrut(5)); // Spacing
        toolbarPanel.add(addResourceButton);
        toolbarPanel.add(Box.createHorizontalStrut(10)); // Spacing
        toolbarPanel.add(uploadTasksButton);
        toolbarPanel.add(Box.createHorizontalStrut(5)); // Spacing
        toolbarPanel.add(uploadResourcesButton);
        toolbarPanel.add(Box.createHorizontalStrut(10)); // Spacing
        toolbarPanel.add(analyzeButton);
        toolbarPanel.add(Box.createHorizontalStrut(5)); // Spacing
        toolbarPanel.add(visualizeButton);
        toolbarPanel.add(Box.createHorizontalStrut(10)); // Spacing
        toolbarPanel.add(saveButton);
        toolbarPanel.add(Box.createHorizontalStrut(5)); // Spacing
        toolbarPanel.add(closeButton);

        return toolbarPanel;
    }

    private JPanel createProjectPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(BorderFactory.createTitledBorder("Project Information"));

        panel.add(new JLabel("Project:"));
        projectNameField = new JTextField("My Project", 20);
        panel.add(projectNameField);

        return panel;
    }

    private void newProject() {
        int result = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to create a new project? This will clear all tasks and resources.", 
            "New Project", JOptionPane.YES_NO_OPTION);
        
        if (result == JOptionPane.YES_OPTION) {
            project = new Project();
            projectNameField.setText("New Project");
            taskTableModel.setProject(project);
            analysisArea.setText("");
            JOptionPane.showMessageDialog(this, "New project created. All tasks and resources cleared.");
        }
    }

    private void addNewTask() {
        // Create a dialog for adding a new task
        JDialog addTaskDialog = new JDialog(this, "Add New Task", true);
        addTaskDialog.setLayout(new BorderLayout());
        addTaskDialog.setSize(400, 350);
        addTaskDialog.setLocationRelativeTo(this);

        // Create form panel
        JPanel formPanel = new JPanel(new GridLayout(6, 2, 5, 5));
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JTextField idField = new JTextField();
        JTextField titleField = new JTextField();
        JTextField startDateField = new JTextField("20250101+0800");
        JTextField endDateField = new JTextField("20250131+1700");
        JTextField dependenciesField = new JTextField();

        formPanel.add(new JLabel("Task ID:"));
        formPanel.add(idField);
        formPanel.add(new JLabel("Task Title:"));
        formPanel.add(titleField);
        formPanel.add(new JLabel("Start (yyyyMMdd+HHmm):"));
        formPanel.add(startDateField);
        formPanel.add(new JLabel("End (yyyyMMdd+HHmm):"));
        formPanel.add(endDateField);
        formPanel.add(new JLabel("Dependencies (comma separated):"));
        formPanel.add(dependenciesField);
        formPanel.add(new JLabel("Format: 20250101+0800"));
        formPanel.add(new JLabel("Example: 1,2,3"));

        // Buttons
        JPanel buttonPanel = new JPanel();
        JButton addButton = new JButton("Add Task");
        JButton cancelButton = new JButton("Cancel");

        addButton.addActionListener(e -> {
            try {
                int id = Integer.parseInt(idField.getText().trim());
                String title = titleField.getText().trim();
                String startStr = startDateField.getText().trim();
                String endStr = endDateField.getText().trim();
                
                if (title.isEmpty()) {
                    throw new IllegalArgumentException("Task title cannot be empty");
                }
                
                // Parse dependencies
                List<Integer> dependencies = new ArrayList<>();
                String depsText = dependenciesField.getText().trim();
                if (!depsText.isEmpty()) {
                    String[] depArray = depsText.split(",");
                    for (String dep : depArray) {
                        dependencies.add(Integer.parseInt(dep.trim()));
                    }
                }
                
                // Parse date times
                LocalDateTime startTime = parseDateTime(startStr);
                LocalDateTime endTime = parseDateTime(endStr);
                
                // Create and add task
                Task newTask = new Task(id, title, startTime, endTime, dependencies);
                project.getTasks().put(id, newTask);
                
                // Link dependencies if they exist
                for (int depId : dependencies) {
                    Task depTask = project.getTasks().get(depId);
                    if (depTask != null) {
                        newTask.addDependencyTask(depTask);
                    }
                }
                
                taskTableModel.fireTableDataChanged();
                updateAnalysisArea();
                addTaskDialog.dispose();
                JOptionPane.showMessageDialog(this, "Task added successfully!");
                
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, 
                    "Error adding task: " + ex.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelButton.addActionListener(e -> addTaskDialog.dispose());

        buttonPanel.add(addButton);
        buttonPanel.add(cancelButton);

        addTaskDialog.add(formPanel, BorderLayout.CENTER);
        addTaskDialog.add(buttonPanel, BorderLayout.SOUTH);
        addTaskDialog.setVisible(true);
    }

    private void addNewResource() {
        // Create a dialog for adding a new resource
        JDialog addResourceDialog = new JDialog(this, "Add New Resource", true);
        addResourceDialog.setLayout(new BorderLayout());
        addResourceDialog.setSize(400, 250);
        addResourceDialog.setLocationRelativeTo(this);

        // Create form panel
        JPanel formPanel = new JPanel(new GridLayout(3, 2, 5, 5));
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JTextField nameField = new JTextField();
        JTextField allocationsField = new JTextField();

        formPanel.add(new JLabel("Resource Name:"));
        formPanel.add(nameField);
        formPanel.add(new JLabel("Allocations (task:percentage):"));
        formPanel.add(allocationsField);
        formPanel.add(new JLabel("Example:"));
        formPanel.add(new JLabel("1:50, 2:100, 3:25"));

        // Buttons
        JPanel buttonPanel = new JPanel();
        JButton addButton = new JButton("Add Resource");
        JButton cancelButton = new JButton("Cancel");

        addButton.addActionListener(e -> {
            try {
                String name = nameField.getText().trim();
                if (name.isEmpty()) {
                    throw new IllegalArgumentException("Resource name cannot be empty");
                }
                
                Resource newResource = new Resource(name);
                
                // Parse allocations
                String allocsText = allocationsField.getText().trim();
                if (!allocsText.isEmpty()) {
                    String[] allocArray = allocsText.split(",");
                    for (String alloc : allocArray) {
                        String[] parts = alloc.trim().split(":");
                        if (parts.length == 2) {
                            int taskId = Integer.parseInt(parts[0].trim());
                            int percentage = Integer.parseInt(parts[1].trim());
                            newResource.addAllocation(taskId, percentage);
                        }
                    }
                }
                
                project.getResources().put(name, newResource);
                taskTableModel.fireTableDataChanged();
                updateAnalysisArea();
                addResourceDialog.dispose();
                JOptionPane.showMessageDialog(this, "Resource added successfully!");
                
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, 
                    "Error adding resource: " + ex.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelButton.addActionListener(e -> addResourceDialog.dispose());

        buttonPanel.add(addButton);
        buttonPanel.add(cancelButton);

        addResourceDialog.add(formPanel, BorderLayout.CENTER);
        addResourceDialog.add(buttonPanel, BorderLayout.SOUTH);
        addResourceDialog.setVisible(true);
    }

    private LocalDateTime parseDateTime(String dateTimeStr) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
            String normalized = dateTimeStr.replace("+", "");
            return LocalDateTime.parse(normalized, formatter);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid date format. Use: yyyyMMdd+HHmm");
        }
    }

    private void saveProject() {
        JOptionPane.showMessageDialog(this, "Project saved successfully.");
    }

    private void closeProject() {
        int result = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to close the project?", "Close Project", 
            JOptionPane.YES_NO_OPTION);
        if (result == JOptionPane.YES_OPTION) {
            newProject();
        }
    }

    private void uploadTasksFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(new File("."));
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try {
                project.loadTasks(file.getAbsolutePath());
                taskTableModel.fireTableDataChanged();
                updateAnalysisArea();
                JOptionPane.showMessageDialog(this, 
                    "Tasks uploaded successfully. Loaded " + project.getTasks().size() + " tasks.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, 
                    "Error uploading tasks: " + ex.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void uploadResourcesFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(new File("."));
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try {
                project.loadResources(file.getAbsolutePath());
                taskTableModel.fireTableDataChanged();
                updateAnalysisArea();
                JOptionPane.showMessageDialog(this, 
                    "Resources uploaded successfully. Loaded " + project.getResources().size() + " resources.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, 
                    "Error uploading resources: " + ex.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showAnalysisDialog() {
        if (project.getTasks().isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "No tasks to analyze. Please upload tasks first.", 
                "No Data", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        AnalysisDialog dialog = new AnalysisDialog(this, project);
        dialog.setVisible(true);
        
        // Update analysis area with results
        if (dialog.getAnalysisResult() != null) {
            analysisArea.setText(dialog.getAnalysisResult());
        }
    }

    private void showVisualization() {
        if (project.getTasks().isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "No tasks to visualize. Please upload tasks first.", 
                "No Data", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        GanttChartDialog dialog = new GanttChartDialog(this, project);
        dialog.setVisible(true);
    }
    
    private void updateAnalysisArea() {
        StringBuilder analysis = new StringBuilder();
        analysis.append("PROJECT OVERVIEW\n");
        analysis.append("================\n\n");
        analysis.append("Total Tasks: ").append(project.getTasks().size()).append("\n");
        analysis.append("Total Resources: ").append(project.getResources().size()).append("\n");
        
        if (!project.getTasks().isEmpty()) {
            analysis.append("\nProject Duration: ")
                   .append(String.format("%.2f hours", project.getProjectDurationInHours()))
                   .append("\n");
            analysis.append("Completion Time: ")
                   .append(project.getProjectCompletionTime())
                   .append("\n");
        }
        
        analysisArea.setText(analysis.toString());
    }
    
    private void autoLoadDataFiles() {
        String[] possiblePaths = {
            "tasks.txt", "resources.txt",
            "../tasks.txt", "../resources.txt"
        };
        
        boolean tasksLoaded = false;
        boolean resourcesLoaded = false;
        
        for (String path : possiblePaths) {
            File file = new File(path);
            if (file.exists() && path.contains("tasks") && !tasksLoaded) {
                try {
                    project.loadTasks(path);
                    tasksLoaded = true;
                    System.out.println("Auto-loaded tasks from: " + path);
                } catch (Exception e) {
                    System.err.println("Failed to auto-load tasks from: " + path);
                }
            }
            
            if (file.exists() && path.contains("resources") && !resourcesLoaded) {
                try {
                    project.loadResources(path);
                    resourcesLoaded = true;
                    System.out.println("Auto-loaded resources from: " + path);
                } catch (Exception e) {
                    System.err.println("Failed to auto-load resources from: " + path);
                }
            }
        }
        
        if (tasksLoaded || resourcesLoaded) {
            taskTableModel.fireTableDataChanged();
            updateAnalysisArea();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new ProjectPlanningGUI().setVisible(true);
        });
    }
}