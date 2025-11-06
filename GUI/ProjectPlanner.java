package GUI;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ProjectPlanner {
    
    // Main method to run the application
    public static void main(String[] args) {
        try {
            Project project = new Project();
            
            // Try to find files in different locations
            String tasksFile = findFile("tasks.txt");
            String resourcesFile = findFile("resources.txt");
            
            if (tasksFile == null) {
                System.err.println("ERROR: Could not find tasks.txt file!");
                System.err.println("Looking in: " + System.getProperty("user.dir"));
                return;
            }
            if (resourcesFile == null) {
                System.err.println("ERROR: Could not find resources.txt file!");
                System.err.println("Looking in: " + System.getProperty("user.dir"));
                return;
            }
            
            System.out.println("Loading tasks from: " + tasksFile);
            project.loadTasks(tasksFile);
            
            System.out.println("Loading resources from: " + resourcesFile);
            project.loadResources(resourcesFile);
            
            System.out.println("\nProject Analysis Results:");
            System.out.println("=".repeat(50));
            
            // 1. Project completion time and duration
            System.out.println("1. Project Completion Time: " + project.getProjectCompletionTime());
            System.out.printf("   Project Duration: %.2f hours (%.2f days)%n",
                project.getProjectDurationInHours(),
                project.getProjectDurationInHours() / 24);
            System.out.println();
            
            // 2. Highlight overlapping tasks
            System.out.println("2. Overlapping Tasks:");
            List<String> overlappingTasks = project.findOverlappingTasks();
            if (overlappingTasks.isEmpty()) {
                System.out.println("   No overlapping tasks found");
            } else {
                overlappingTasks.forEach(overlap -> 
                    System.out.println("   - " + overlap));
            }
            System.out.println();
            
            // 3. Find teams for each task
            System.out.println("3. Teams for Each Task:");
            project.getTasks().keySet().stream()
                .sorted()
                .forEach(taskId -> {
                    Set<String> team = project.getTeamForTask(taskId);
                    System.out.printf("   Task %d: %s%n", taskId, 
                        team.isEmpty() ? "No team assigned" : String.join(", ", team));
                });
            System.out.println();
            
            // 4. Find total effort for each resource
            System.out.println("4. Total Effort per Resource (hours):");
            Map<String, Double> effort = project.getResourceEffort();
            if (effort.isEmpty()) {
                System.out.println("   No resource effort data available");
            } else {
                effort.forEach((name, hours) -> 
                    System.out.printf("   %s: %.2f hours%n", name, hours));
            }
            
            // Debug information
            System.out.println();
            System.out.println("Debug Information:");
            System.out.println("Total tasks loaded: " + project.getTasks().size());
            System.out.println("Total resources loaded: " + project.getResources().size());
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static String findFile(String filename) {
        // Check current directory
        if (Files.exists(Paths.get(filename))) {
            return filename;
        }
        
        // Check in parent directory
        if (Files.exists(Paths.get("../" + filename))) {
            return "../" + filename;
        }
        
        // Check in GUI directory
        if (Files.exists(Paths.get("GUI/" + filename))) {
            return "GUI/" + filename;
        }
        
        return null;
    }
}

// Project class - main container and manager
class Project {
    private final Map<Integer, Task> tasks = new HashMap<>();
    private final Map<String, Resource> resources = new HashMap<>();
    private final List<Allocation> allocations = new ArrayList<>();
    
    public void loadTasks(String filename) throws FileParseException {
        List<String> lines = readLines(filename);
        List<Task> taskList = new ArrayList<>();
        
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (line.isEmpty()) continue;
            
            try {
                Task task = parseTaskLine(line);
                taskList.add(task);
            } catch (Exception e) {
                throw new FileParseException(
                    String.format("Error parsing line %d: %s", i + 1, line), e);
            }
        }
        
        for (Task task : taskList) {
            tasks.put(task.getId(), task);
        }
        
        // Link dependency tasks
        for (Task task : tasks.values()) {
            for (int depId : task.getDependencyIds()) {
                Task depTask = tasks.get(depId);
                if (depTask != null) {
                    task.addDependencyTask(depTask);
                }
            }
        }
        
        System.out.println("Successfully loaded " + tasks.size() + " tasks");
    }
    
    public void loadResources(String filename) throws FileParseException {
        List<String> lines = readLines(filename);
        List<Resource> resourceList = new ArrayList<>();
        
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (line.isEmpty()) continue;
            
            try {
                Resource resource = parseResourceLine(line);
                resourceList.add(resource);
            } catch (Exception e) {
                throw new FileParseException(
                    String.format("Error parsing line %d: %s", i + 1, line), e);
            }
        }
        
        for (Resource resource : resourceList) {
            resources.put(resource.getName(), resource);
            
            // Create allocation objects
            for (Map.Entry<Integer, Integer> entry : resource.getAllocations().entrySet()) {
                Task task = tasks.get(entry.getKey());
                if (task != null) {
                    allocations.add(new Allocation(resource, task, entry.getValue()));
                }
            }
        }
        
        System.out.println("Successfully loaded " + resources.size() + " resources");
    }
    
    private Task parseTaskLine(String line) throws DateTimeParseException {
        String[] parts = line.split(",");
        if (parts.length < 4) {
            throw new IllegalArgumentException("Invalid task format: " + line);
        }
        
        int id = Integer.parseInt(parts[0].trim());
        String title = parts[1].trim();
        LocalDateTime startTime = parseDateTime(parts[2].trim());
        LocalDateTime endTime = parseDateTime(parts[3].trim());
        
        List<Integer> dependencies = new ArrayList<>();
        if (parts.length > 4) {
            for (int i = 4; i < parts.length; i++) {
                String dep = parts[i].trim();
                if (!dep.isEmpty()) {
                    dependencies.add(Integer.parseInt(dep));
                }
            }
        }
        
        return new Task(id, title, startTime, endTime, dependencies);
    }
    
    private Resource parseResourceLine(String line) {
        String[] parts = line.split(",");
        if (parts.length < 1) {
            throw new IllegalArgumentException("Invalid resource format: " + line);
        }
        
        String name = parts[0].trim();
        Resource resource = new Resource(name);
        
        for (int i = 1; i < parts.length; i++) {
            String allocation = parts[i].trim();
            if (allocation.contains(":")) {
                String[] allocationParts = allocation.split(":");
                if (allocationParts.length == 2) {
                    int taskId = Integer.parseInt(allocationParts[0].trim());
                    int loadPercentage = Integer.parseInt(allocationParts[1].trim());
                    resource.addAllocation(taskId, loadPercentage);
                }
            }
        }
        
        return resource;
    }
    
    private LocalDateTime parseDateTime(String dateTimeStr) throws DateTimeParseException {
        Pattern DATE_PATTERN = Pattern.compile("\\d{8}\\+\\d{4}");
        DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
        
        if (!DATE_PATTERN.matcher(dateTimeStr).matches()) {
            throw new DateTimeParseException("Invalid datetime format", dateTimeStr, 0);
        }
        
        String normalized = dateTimeStr.replace("+", "");
        return LocalDateTime.parse(normalized, FORMATTER);
    }
    
    private List<String> readLines(String filename) throws FileParseException {
        try {
            return Files.readAllLines(Paths.get(filename));
        } catch (IOException e) {
            throw new FileParseException("Could not read file: " + filename, e);
        }
    }
    
    public LocalDateTime getProjectCompletionTime() {
        return tasks.values().stream()
            .map(Task::getEndTime)
            .max(LocalDateTime::compareTo)
            .orElse(null);
    }
    
    public double getProjectDurationInHours() {
        Optional<LocalDateTime> minStart = tasks.values().stream()
            .map(Task::getStartTime)
            .min(LocalDateTime::compareTo);
        
        Optional<LocalDateTime> maxEnd = tasks.values().stream()
            .map(Task::getEndTime)
            .max(LocalDateTime::compareTo);
        
        if (minStart.isPresent() && maxEnd.isPresent()) {
            return java.time.Duration.between(minStart.get(), maxEnd.get()).toHours();
        }
        
        return 0;
    }
    
    public List<String> findOverlappingTasks() {
        List<String> overlaps = new ArrayList<>();
        
        for (Task task : tasks.values()) {
            for (Task depTask : task.getDependencyTasks()) {
                if (task.overlapsWith(depTask)) {
                    String overlap = String.format(
                        "Task %d ('%s') overlaps with dependency Task %d ('%s')",
                        task.getId(), task.getTitle(), depTask.getId(), depTask.getTitle()
                    );
                    overlaps.add(overlap);
                }
            }
        }
        
        return overlaps;
    }
    
    public Set<String> getTeamForTask(int taskId) {
        return allocations.stream()
            .filter(allocation -> allocation.getTask().getId() == taskId)
            .map(allocation -> allocation.getResource().getName())
            .collect(Collectors.toSet());
    }
    
    public Map<String, Double> getResourceEffort() {
        Map<String, Double> effort = new HashMap<>();
        
        for (Resource resource : resources.values()) {
            effort.put(resource.getName(), resource.calculateTotalEffort(tasks));
        }
        
        return effort;
    }
    
    public Map<Integer, Task> getTasks() { return new HashMap<>(tasks); }
    public Map<String, Resource> getResources() { return new HashMap<>(resources); }
    public List<Allocation> getAllocations() { return new ArrayList<>(allocations); }
}

// Task class
class Task {
    private final int id;
    private final String title;
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;
    private final List<Integer> dependencyIds;
    private final List<Task> dependencyTasks;
    
    public Task(int id, String title, LocalDateTime startTime, LocalDateTime endTime, List<Integer> dependencyIds) {
        this.id = id;
        this.title = title;
        this.startTime = startTime;
        this.endTime = endTime;
        this.dependencyIds = new ArrayList<>(dependencyIds);
        this.dependencyTasks = new ArrayList<>();
    }
    
    public int getId() { return id; }
    public String getTitle() { return title; }
    public LocalDateTime getStartTime() { return startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public List<Integer> getDependencyIds() { return new ArrayList<>(dependencyIds); }
    public List<Task> getDependencyTasks() { return new ArrayList<>(dependencyTasks); }
    
    public void addDependencyTask(Task task) {
        if (!dependencyTasks.contains(task)) {
            dependencyTasks.add(task);
        }
    }
    
    public double getDurationInHours() {
        java.time.Duration duration = java.time.Duration.between(startTime, endTime);
        return duration.toHours();
    }
    
    public boolean overlapsWith(Task other) {
        return startTime.isBefore(other.endTime) && other.startTime.isBefore(endTime);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return id == task.id;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return String.format("Task %d: %s (%s to %s)", id, title, startTime, endTime);
    }
}

// Resource class
class Resource {
    private final String name;
    private final Map<Integer, Integer> allocations;
    
    public Resource(String name) {
        this.name = name;
        this.allocations = new HashMap<>();
    }
    
    public String getName() { return name; }
    public Map<Integer, Integer> getAllocations() { return new HashMap<>(allocations); }
    
    public void addAllocation(int taskId, int loadPercentage) {
        allocations.put(taskId, loadPercentage);
    }
    
    public double calculateTotalEffort(Map<Integer, Task> tasks) {
        return allocations.entrySet().stream()
            .mapToDouble(entry -> {
                Task task = tasks.get(entry.getKey());
                if (task != null) {
                    return task.getDurationInHours() * (entry.getValue() / 100.0);
                }
                return 0;
            })
            .sum();
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Resource resource = (Resource) o;
        return Objects.equals(name, resource.name);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
    
    @Override
    public String toString() {
        return String.format("Resource: %s", name);
    }
}

// Allocation class (relationship between Resource and Task)
class Allocation {
    private final Resource resource;
    private final Task task;
    private final int loadPercentage;
    
    public Allocation(Resource resource, Task task, int loadPercentage) {
        this.resource = resource;
        this.task = task;
        this.loadPercentage = loadPercentage;
    }
    
    public Resource getResource() { return resource; }
    public Task getTask() { return task; }
    public int getLoadPercentage() { return loadPercentage; }
    
    public double getEffortHours() {
        return task.getDurationInHours() * (loadPercentage / 100.0);
    }
    
    @Override
    public String toString() {
        return String.format("%s -> %s (%d%%)", resource.getName(), task.getTitle(), loadPercentage);
    }
}

// Custom exception class
class FileParseException extends Exception {
    public FileParseException(String message) {
        super(message);
    }
    
    public FileParseException(String message, Throwable cause) {
        super(message, cause);
    }

    
 public static void main(String[] args) {
        // Launch GUI immediately
        javax.swing.SwingUtilities.invokeLater(() -> {
            new ProjectPlanningGUI().setVisible(true);
        });
    }
}


