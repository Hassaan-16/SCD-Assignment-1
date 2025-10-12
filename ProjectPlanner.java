import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Map;
import java.util.Set;

public class ProjectPlanningGUI extends JFrame {
    private ProjectPlanner projectPlanner;
    private Project project;
    private JTable taskTable;
    private TaskTableModel taskTableModel;
    private JTextField projectNameField;
    private JTextArea analysisArea;

    public ProjectPlanningGUI() {
        this.projectPlanner = new ProjectPlanner();
        this.project = new Project();
        initializeUI();
    }

    private void initializeUI() {
        setTitle("Project Planning Application - Assignment 2");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1400, 800);
        setLocationRelativeTo(null);

        // Create main panel with border layout
        JPanel mainPanel = new JPanel(new BorderLayout());

        // Create toolbar
        JToolBar toolBar = createToolBar();
        mainPanel.add(toolBar, BorderLayout.NORTH);

        // Create project info panel
        JPanel projectPanel = createProjectPanel();
        mainPanel.add(projectPanel, BorderLayout.NORTH);

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

        mainPanel.add(centerSplitPane, BorderLayout.CENTER);

        add(mainPanel);
    }

    private JToolBar createToolBar() {
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);

        JButton newButton = new JButton("New");
        JButton saveButton = new JButton("Save");
        JButton closeButton = new JButton("Close");
        JButton uploadTasksButton = new JButton("Upload Tasks");
        JButton uploadResourcesButton = new JButton("Upload Resources");
        JButton analyzeButton = new JButton("Analyze");
        JButton visualizeButton = new JButton("Visualize");

        // Add action listeners
        newButton.addActionListener(e -> newProject());
        saveButton.addActionListener(e -> saveProject());
        closeButton.addActionListener(e -> closeProject());
        uploadTasksButton.addActionListener(e -> uploadTasksFile());
        uploadResourcesButton.addActionListener(e -> uploadResourcesFile());
        analyzeButton.addActionListener(e -> showAnalysisDialog());
        visualizeButton.addActionListener(e -> showVisualization());

        toolBar.add(newButton);
        toolBar.add(saveButton);
        toolBar.add(closeButton);
        toolBar.addSeparator();
        toolBar.add(uploadTasksButton);
        toolBar.add(uploadResourcesButton);
        toolBar.addSeparator();
        toolBar.add(analyzeButton);
        toolBar.add(visualizeButton);

        return toolBar;
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
        project = new Project();
        projectNameField.setText("New Project");
        taskTableModel.setProject(project);
        analysisArea.setText("");
        JOptionPane.showMessageDialog(this, "New project created.");
    }

    private void saveProject() {
        // Implementation for saving project data
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
                JOptionPane.showMessageDialog(this, "Tasks uploaded successfully.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, 
                    "Error uploading tasks: " + ex.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
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
                JOptionPane.showMessageDialog(this, "Resources uploaded successfully.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, 
                    "Error uploading resources: " + ex.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }

    private void showAnalysisDialog() {
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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeel());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new ProjectPlanningGUI().setVisible(true);
        });
    }
}
