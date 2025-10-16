// GanttChartDialog.java
import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.*;

public class GanttChartDialog extends JDialog {
    private Project project;
    private GanttChartPanel chartPanel;

    public GanttChartDialog(JFrame parent, Project project) {
        super(parent, "Project Gantt Chart", true);
        this.project = project;
        initializeUI();
        setSize(1000, 600);
        setLocationRelativeTo(parent);
    }

    private void initializeUI() {
        setLayout(new BorderLayout());
        
        chartPanel = new GanttChartPanel(project);
        add(new JScrollPane(chartPanel), BorderLayout.CENTER);
        
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dispose());
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(closeButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }
}

class GanttChartPanel extends JPanel {
    private Project project;
    private static final int ROW_HEIGHT = 40;
    private static final int MARGIN = 50;
    private static final int TASK_LABEL_WIDTH = 200;

    public GanttChartPanel(Project project) {
        this.project = project;
        setPreferredSize(new Dimension(1200, calculateHeight()));
    }

    private int calculateHeight() {
        return MARGIN * 2 + project.getTasks().size() * ROW_HEIGHT;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        drawGanttChart(g2d);
    }

    private void drawGanttChart(Graphics2D g2d) {
        List<Task> tasks = new ArrayList<>(project.getTasks().values());
        if (tasks.isEmpty()) {
            g2d.drawString("No tasks to display", MARGIN, MARGIN);
            return;
        }

        // Sort tasks by start time
        tasks.sort(Comparator.comparing(Task::getStartTime));

        // Find project time range
        LocalDateTime projectStart = tasks.stream()
                .map(Task::getStartTime)
                .min(LocalDateTime::compareTo)
                .orElse(LocalDateTime.now());
        LocalDateTime projectEnd = tasks.stream()
                .map(Task::getEndTime)
                .max(LocalDateTime::compareTo)
                .orElse(LocalDateTime.now());

        long totalDays = ChronoUnit.DAYS.between(projectStart, projectEnd) + 1;
        int chartWidth = getWidth() - MARGIN - TASK_LABEL_WIDTH;

        // Draw title and timeline
        drawTimeline(g2d, projectStart, totalDays, chartWidth);

        // Draw tasks
        for (int i = 0; i < tasks.size(); i++) {
            Task task = tasks.get(i);
            int y = MARGIN + i * ROW_HEIGHT;

            // Draw task label
            g2d.setColor(Color.BLACK);
            g2d.drawString(task.getId() + ": " + task.getTitle(), 
                          MARGIN, y + ROW_HEIGHT / 2 + 5);

            // Draw task bar
            drawTaskBar(g2d, task, projectStart, totalDays, chartWidth, y);
        }
    }

    private void drawTimeline(Graphics2D g2d, LocalDateTime start, long totalDays, int chartWidth) {
        g2d.setColor(Color.BLACK);
        g2d.drawString("Gantt Chart - " + start.getYear(), MARGIN, 20);

        // Draw timeline header
        int timelineY = MARGIN - 20;
        g2d.drawLine(TASK_LABEL_WIDTH, timelineY, TASK_LABEL_WIDTH + chartWidth, timelineY);

        // Draw day markers
        for (int day = 0; day <= totalDays; day += Math.max(1, totalDays / 20)) {
            int x = TASK_LABEL_WIDTH + (int) (day * (chartWidth / (double) totalDays));
            g2d.drawLine(x, timelineY - 5, x, timelineY + 5);
            g2d.drawString("Day " + day, x - 10, timelineY - 10);
        }
    }

    private void drawTaskBar(Graphics2D g2d, Task task, LocalDateTime projectStart, 
                           long totalDays, int chartWidth, int y) {
        long startOffset = ChronoUnit.DAYS.between(projectStart, task.getStartTime());
        long taskDuration = ChronoUnit.DAYS.between(task.getStartTime(), task.getEndTime()) + 1;

        int startX = TASK_LABEL_WIDTH + (int) (startOffset * (chartWidth / (double) totalDays));
        int taskWidth = Math.max(5, (int) (taskDuration * (chartWidth / (double) totalDays)));

        // Choose color based on task dependencies
        Color taskColor = task.getDependencyIds().isEmpty() ? 
            new Color(70, 130, 180) : // Steel blue for independent tasks
            new Color(34, 139, 34);   // Forest green for dependent tasks

        g2d.setColor(taskColor);
        g2d.fillRect(startX, y, taskWidth, ROW_HEIGHT - 10);
        
        g2d.setColor(Color.BLACK);
        g2d.drawRect(startX, y, taskWidth, ROW_HEIGHT - 10);

        // Draw task info on bar
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("SansSerif", Font.BOLD, 10));
        if (taskWidth > 50) {
            g2d.drawString("T" + task.getId(), startX + 5, y + ROW_HEIGHT / 2);
        }
    }
}