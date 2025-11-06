package GUI;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Set;
import java.util.Map;

public class AnalysisDialog extends JDialog {
    private Project project;
    private JTextArea resultArea;
    private ButtonGroup analysisGroup;
    private String analysisResult;

    public AnalysisDialog(JFrame parent, Project project) {
        super(parent, "Project Analysis", true);
        this.project = project;
        initializeUI();
        pack();
        setLocationRelativeTo(parent);
        setSize(600, 500);
    }

    private void initializeUI() {
        setLayout(new BorderLayout());

        // Analysis options panel
        JPanel optionsPanel = new JPanel(new GridLayout(5, 1));
        optionsPanel.setBorder(BorderFactory.createTitledBorder("Analysis Options"));

        analysisGroup = new ButtonGroup();

        JRadioButton completionTimeBtn = new JRadioButton("Project completion time and duration", true);
        JRadioButton overlappingTasksBtn = new JRadioButton("Overlapping tasks");
        JRadioButton resourcesTeamsBtn = new JRadioButton("Resources and teams");
        JRadioButton effortBreakdownBtn = new JRadioButton("Effort breakdown: Resource-wise");

        completionTimeBtn.setActionCommand("completion");
        overlappingTasksBtn.setActionCommand("overlapping");
        resourcesTeamsBtn.setActionCommand("teams");
        effortBreakdownBtn.setActionCommand("effort");

        analysisGroup.add(completionTimeBtn);
        analysisGroup.add(overlappingTasksBtn);
        analysisGroup.add(resourcesTeamsBtn);
        analysisGroup.add(effortBreakdownBtn);

        optionsPanel.add(completionTimeBtn);
        optionsPanel.add(overlappingTasksBtn);
        optionsPanel.add(resourcesTeamsBtn);
        optionsPanel.add(effortBreakdownBtn);

        // Result area
        resultArea = new JTextArea(15, 50);
        resultArea.setEditable(false);
        resultArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(resultArea);

        // Buttons
        JPanel buttonPanel = new JPanel();
        JButton analyzeBtn = new JButton("Analyze");
        JButton closeBtn = new JButton("Close");

        analyzeBtn.addActionListener(e -> performAnalysis());
        closeBtn.addActionListener(e -> dispose());

        buttonPanel.add(analyzeBtn);
        buttonPanel.add(closeBtn);

        add(optionsPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void performAnalysis() {
        resultArea.setText("");
        StringBuilder result = new StringBuilder();
        
        String selected = analysisGroup.getSelection().getActionCommand();
        
        switch (selected) {
            case "completion":
                analyzeCompletionTime(result);
                break;
            case "overlapping":
                analyzeOverlappingTasks(result);
                break;
            case "teams":
                analyzeResourcesAndTeams(result);
                break;
            case "effort":
                analyzeEffortBreakdown(result);
                break;
        }
        
        analysisResult = result.toString();
        resultArea.setText(analysisResult);
    }

    private void analyzeCompletionTime(StringBuilder result) {
        result.append("PROJECT COMPLETION ANALYSIS\n");
        result.append("===========================\n\n");
        
        result.append("Project Completion Time: ")
              .append(project.getProjectCompletionTime())
              .append("\n");
        result.append("Project Duration: ")
              .append(String.format("%.2f hours (%.2f days)", 
                  project.getProjectDurationInHours(),
                  project.getProjectDurationInHours() / 24))
              .append("\n");
    }

    private void analyzeOverlappingTasks(StringBuilder result) {
        result.append("OVERLAPPING TASKS ANALYSIS\n");
        result.append("==========================\n\n");
        
        List<String> overlapping = project.findOverlappingTasks();
        if (overlapping.isEmpty()) {
            result.append("No overlapping tasks found.\n");
        } else {
            result.append("Overlapping task pairs:\n");
            for (String pair : overlapping) {
                result.append("  • ").append(pair).append("\n");
            }
        }
    }

    private void analyzeResourcesAndTeams(StringBuilder result) {
        result.append("RESOURCES AND TEAMS ANALYSIS\n");
        result.append("============================\n\n");
        
        result.append("Task Teams:\n");
        project.getTasks().keySet().stream()
            .sorted()
            .forEach(taskId -> {
                Set<String> team = project.getTeamForTask(taskId);
                result.append(String.format("  Task %d: %s%n", taskId, 
                    team.isEmpty() ? "No team assigned" : String.join(", ", team)));
            });
    }

    private void analyzeEffortBreakdown(StringBuilder result) {
        result.append("EFFORT BREAKDOWN ANALYSIS\n");
        result.append("=========================\n\n");
        
        Map<String, Double> effort = project.getResourceEffort();
        result.append("Resource Effort (in hours):\n");
        for (Map.Entry<String, Double> entry : effort.entrySet()) {
            result.append(String.format("  %-10s: %6.2f hours%n", entry.getKey(), entry.getValue()));
        }
    }

    public String getAnalysisResult() {
        return analysisResult;
    }
}