package GUI;

import javax.swing.table.AbstractTableModel;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;

public class TaskTableModel extends AbstractTableModel {
    private Project project;
    private final String[] columnNames = {"Id", "Task", "Start", "End", "Dependencies", "Resources"};
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public TaskTableModel(Project project) {
        this.project = project;
    }

    public void setProject(Project project) {
        this.project = project;
        fireTableDataChanged();
    }

    @Override
    public int getRowCount() {
        return project.getTasks().size();
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        // Convert map values to list for indexing
        List<Task> tasks = List.copyOf(project.getTasks().values());
        Task task = tasks.get(rowIndex);
        
        switch (columnIndex) {
            case 0: return task.getId();
            case 1: return task.getTitle();
            case 2: return task.getStartTime().format(dateFormatter);
            case 3: return task.getEndTime().format(dateFormatter);
            case 4: 
                List<Integer> deps = task.getDependencyIds();
                return deps.isEmpty() ? "" : deps.toString();
            case 5: 
                Set<String> team = project.getTeamForTask(task.getId());
                return team.isEmpty() ? "" : String.join(", ", team);
            default: return null;
        }
    }
}