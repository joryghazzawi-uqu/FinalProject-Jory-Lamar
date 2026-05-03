package taskmanager.ui.swing;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import taskmanager.api.SchedulePlanner;
import taskmanager.api.TaskManager;
import taskmanager.model.ScheduleRecommendation;
import taskmanager.model.Task;
import taskmanager.model.WeatherForecast;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;


/**
 * Swing graphical user interface for the Smart Task Manager.
 *
 * This frame allows the user to add, edit, delete, and view tasks.
 * It also allows weather lookup and schedule recommendation generation.
 *
 * Heavy work is executed using Reactor on background threads, while all UI
 * updates are safely executed on the Swing Event Dispatch Thread using
 * SwingUtilities.invokeLater.
 */
public class SmartTaskManagerFrame extends JFrame {
    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final TaskManager taskManager;
    private final SchedulePlanner planner;
    private final DefaultTableModel tableModel;
    private final JTable taskTable;
    private final JLabel statusLabel;
    private final JTextField locationField;

    /**
     * Creates the main application window.
     *
     * @param taskManager main TaskManager facade used by the UI
     */
    public SmartTaskManagerFrame(TaskManager taskManager) {
        this.taskManager = taskManager;
        this.planner = taskManager.getPlanner();

        setTitle("Smart Task Manager");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(800, 500);
        setLocationRelativeTo(null);

        this.tableModel = new DefaultTableModel(
                new String[]{"ID", "Title", "Due Time", "Weather Sensitive", "Status"},
                0
        ) {
            /**
             * Prevents editing directly inside the table.
             *
             * @param row table row
             * @param column table column
             * @return false because editing is handled through dialogs
             */
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        this.taskTable = new JTable(tableModel);
        this.statusLabel = new JLabel("Ready");
        this.locationField = new JTextField("Jeddah", 16);

        JPanel topPanel = new JPanel(new BorderLayout(8, 8));
        topPanel.add(new JLabel("Location:"), BorderLayout.WEST);
        topPanel.add(locationField, BorderLayout.EAST);
        topPanel.add(statusLabel, BorderLayout.CENTER);

        JButton addButton = new JButton("Add Task");
        JButton editButton = new JButton("Edit Task");
        JButton deleteButton = new JButton("Delete Task");
        JButton weatherButton = new JButton("Check Weather");
        JButton suggestButton = new JButton("Get Suggestions");
        JButton refreshButton = new JButton("Refresh");

        editButton.setEnabled(false);
        deleteButton.setEnabled(false);
        weatherButton.setEnabled(false);

        taskTable.getSelectionModel().addListSelectionListener(event -> {
            boolean enabled = taskTable.getSelectedRow() >= 0;
            editButton.setEnabled(enabled);
            deleteButton.setEnabled(enabled);
            weatherButton.setEnabled(enabled);
        });

        addButton.addActionListener(event -> showTaskDialog(null));
        editButton.addActionListener(event -> editSelectedTask());
        deleteButton.addActionListener(event -> deleteSelectedTask());
        weatherButton.addActionListener(event -> updateSelectedTaskWeather());
        suggestButton.addActionListener(event -> suggestSchedule());
        refreshButton.addActionListener(event -> loadTasks());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(weatherButton);
        buttonPanel.add(suggestButton);
        buttonPanel.add(refreshButton);

        setLayout(new BorderLayout(8, 8));
        add(topPanel, BorderLayout.NORTH);
        add(new JScrollPane(taskTable), BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        loadTasks();
    }

    /**
     * Loads tasks from the manager and refreshes the table.
     *
     * Uses boundedElastic so loading does not block the Swing UI thread.
     */
    private void loadTasks() {
        statusLabel.setText("Loading tasks...");

        Mono.fromCallable(taskManager::getTasks)
                .subscribeOn(Schedulers.boundedElastic())
                .doOnNext(tasks ->
                        SwingUtilities.invokeLater(() -> populateTable(tasks))
                )
                .doOnError(error ->
                        SwingUtilities.invokeLater(() ->
                                showError("Load failed", error)
                        )
                )
                .subscribe();
    }

    /**
     * Fills the task table with task data.
     *
     * @param tasks tasks to display
     */
    private void populateTable(List<Task> tasks) {
        tableModel.setRowCount(0);

        for (Task task : tasks) {
            tableModel.addRow(new Object[]{
                    task.getId(),
                    task.getTitle(),
                    DATE_TIME_FORMATTER.format(task.getDueDateTime()),
                    task.isWeatherSensitive(),
                    "Not checked"
            });
        }

        statusLabel.setText("Tasks loaded: " + tasks.size());
    }

    /**
     * Opens the edit dialog for the selected task.
     */
    private void editSelectedTask() {
        int selectedRow = taskTable.getSelectedRow();

        if (selectedRow < 0) {
            return;
        }

        String taskId = (String) tableModel.getValueAt(selectedRow, 0);
        Task task = findTaskById(taskId);

        if (task != null) {
            showTaskDialog(task);
        }
    }

    /**
     * Deletes the selected task.
     *
     * The deletion is executed in a background thread and the UI is updated
     * afterward on the Event Dispatch Thread.
     */
    private void deleteSelectedTask() {
        int selectedRow = taskTable.getSelectedRow();

        if (selectedRow < 0) {
            return;
        }

        String taskId = (String) tableModel.getValueAt(selectedRow, 0);
        statusLabel.setText("Deleting task...");

        Mono.fromRunnable(() -> taskManager.removeTask(taskId))
                .subscribeOn(Schedulers.boundedElastic())
                .doOnSuccess(ignore ->
                        SwingUtilities.invokeLater(() -> {
                            statusLabel.setText("Deleted task " + taskId);
                            loadTasks();
                        })
                )
                .doOnError(error ->
                        SwingUtilities.invokeLater(() ->
                                showError("Delete failed", error)
                        )
                )
                .subscribe();
    }

    /**
     * Fetches weather for the selected task and updates its status column.
     */
    private void updateSelectedTaskWeather() {
        int selectedRow = taskTable.getSelectedRow();

        if (selectedRow < 0) {
            return;
        }

        String taskId = (String) tableModel.getValueAt(selectedRow, 0);
        String location = locationField.getText().trim();

        statusLabel.setText("Updating weather...");

        taskManager.fetchWeather(location)
                .subscribeOn(Schedulers.boundedElastic())
                .doOnNext(forecast ->
                        SwingUtilities.invokeLater(() ->
                                applyWeatherStatus(taskId, forecast)
                        )
                )
                .doOnError(error ->
                        SwingUtilities.invokeLater(() ->
                                showError("Weather update failed", error)
                        )
                )
                .subscribe();
    }

    /**
     * Updates the table status column based on weather data.
     *
     * @param taskId task ID to update
     * @param forecast weather forecast used to decide status
     */
    private void applyWeatherStatus(String taskId, WeatherForecast forecast) {
        Task task = findTaskById(taskId);
        String status;

        if (task == null) {
            status = "Unknown task";
        } else if (!task.isWeatherSensitive()) {
            status = "Indoor task - not affected by weather";
        } else if (forecast.getPrecipitationProbability() > 0.6) {
            status = "Rain expected - consider rescheduling";
        } else if (forecast.getTemperatureCelsius() >= 35) {
            status = "High temperature - consider a cooler time";
        } else {
            status = "Suitable weather conditions";
        }

        for (int i = 0; i < tableModel.getRowCount(); i++) {
            if (taskId.equals(tableModel.getValueAt(i, 0))) {
                tableModel.setValueAt(status, i, 4);
                break;
            }
        }

        statusLabel.setText(String.format(
                "Weather for %s: %s, %.1f°C",
                forecast.getLocation(),
                forecast.getCondition(),
                forecast.getTemperatureCelsius()
        ));
    }

    /**
     * Generates schedule recommendations for all current tasks.
     *
     * The method first loads tasks in the background, then requests
     * schedule recommendations from the planner.
     */
    private void suggestSchedule() {
        String location = locationField.getText().trim();
        statusLabel.setText("Generating schedule suggestions...");

        Mono.fromCallable(taskManager::getTasks)
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(tasks -> planner.suggestScheduleForLocation(tasks, location))
                .doOnNext(recommendations ->
                        SwingUtilities.invokeLater(() ->
                                showRecommendations(recommendations)
                        )
                )
                .doOnError(error ->
                        SwingUtilities.invokeLater(() ->
                                showError("Schedule suggestion failed", error)
                        )
                )
                .subscribe();
    }

    /**
     * Displays schedule recommendations in a popup window.
     *
     * @param recommendations list of generated recommendations
     */
private void showRecommendations(List<ScheduleRecommendation> recommendations) {
    if (recommendations.isEmpty()) {
        JOptionPane.showMessageDialog(
                this,
                "No tasks available.",
                "Schedule Suggestions",
                JOptionPane.INFORMATION_MESSAGE
        );
        return;
    }

    StringBuilder message = new StringBuilder();

    message.append("Schedule Suggestions for ")
            .append(locationField.getText().trim())
            .append("\n\n");

    for (ScheduleRecommendation rec : recommendations) {

        Task task = rec.task();
        String recommendation = rec.recommendation();

        String decision;

        if (recommendation.contains("postpone")
                || recommendation.contains("Rain")
                || recommendation.contains("rain")) {
            decision = "Reschedule the task due to expected rain.";
        } else if (recommendation.contains("High temperature")) {
            decision = "Consider doing this task at a cooler time.";
        } else if (recommendation.contains("not weather sensitive")) {
            decision = "This task can be done as planned (not affected by weather).";
        } else {
            decision = "Conditions are suitable to proceed as planned.";
        }

        message.append("• ")
                .append(task.getTitle())
                .append("\n")
                .append("  Time: ")
                .append(DATE_TIME_FORMATTER.format(task.getDueDateTime()))
                .append("\n")
                .append("  Advice: ")
                .append(decision)
                .append("\n\n");
    }

    JTextArea area = new JTextArea(message.toString());
    area.setEditable(false);
    area.setLineWrap(true);
    area.setWrapStyleWord(true);


    area.setOpaque(false);
    area.setBorder(null);


    JScrollPane scrollPane = new JScrollPane(area);
    scrollPane.setPreferredSize(new Dimension(550, 300));

    scrollPane.setBorder(null);
    scrollPane.getViewport().setOpaque(false);

    JOptionPane.showMessageDialog(
            this,
            scrollPane,
            "Schedule Suggestions",
            JOptionPane.INFORMATION_MESSAGE
    );

    statusLabel.setText("Suggestions ready.");
}
    /**
     * Opens a dialog used for both adding and editing tasks.
     *
     * @param existingTask existing task when editing, or null when adding
     */
    private void showTaskDialog(Task existingTask) {
        JTextField titleField = new JTextField(
                existingTask == null ? "" : existingTask.getTitle(),
                20
        );

        JTextField descriptionField = new JTextField(
                existingTask == null ? "" : existingTask.getDescription(),
                20
        );

        JTextField dueField = new JTextField(
                existingTask == null
                        ? DATE_TIME_FORMATTER.format(LocalDateTime.now().plusHours(1))
                        : DATE_TIME_FORMATTER.format(existingTask.getDueDateTime()),
                20
        );

        JCheckBox weatherSensitiveBox = new JCheckBox(
                "Weather Sensitive",
                existingTask != null && existingTask.isWeatherSensitive()
        );

        JPanel form = new JPanel(new GridLayout(0, 1, 4, 4));
        form.add(new JLabel("Title:"));
        form.add(titleField);
        form.add(new JLabel("Description:"));
        form.add(descriptionField);
        form.add(new JLabel("Due date/time (yyyy-MM-dd HH:mm):"));
        form.add(dueField);
        form.add(weatherSensitiveBox);

        int option = JOptionPane.showConfirmDialog(
                this,
                form,
                existingTask == null ? "Add Task" : "Edit Task",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (option != JOptionPane.OK_OPTION) {
            return;
        }

        LocalDateTime dueDate;

        try {
            dueDate = LocalDateTime.parse(
                    dueField.getText().trim(),
                    DATE_TIME_FORMATTER
            );
        } catch (DateTimeParseException e) {
            showError("Invalid date format", e);
            return;
        }

        Task task = new Task(
                existingTask == null ? generateTaskId() : existingTask.getId(),
                titleField.getText().trim(),
                dueDate,
                weatherSensitiveBox.isSelected()
        );

        task.setDescription(descriptionField.getText().trim());

        statusLabel.setText(existingTask == null ? "Adding task..." : "Updating task...");

        Mono.fromRunnable(() -> taskManager.addTask(task))
                .subscribeOn(Schedulers.boundedElastic())
                .doOnSuccess(ignore ->
                        SwingUtilities.invokeLater(() -> {
                            statusLabel.setText(
                                    existingTask == null
                                            ? "Task added."
                                            : "Task updated."
                            );
                            loadTasks();
                        })
                )
                .doOnError(error ->
                        SwingUtilities.invokeLater(() ->
                                showError("Task save failed", error)
                        )
                )
                .subscribe();
    }

    /**
     * Generates a simple unique task ID.
     *
     * @return generated task ID
     */
    private String generateTaskId() {
        return "task-" + System.currentTimeMillis();
    }

    /**
     * Searches for a task by ID from the current task list.
     *
     * @param taskId task ID
     * @return matching task, or null if not found
     */
    private Task findTaskById(String taskId) {
        return taskManager.getTasks()
                .stream()
                .filter(task -> task.getId().equals(taskId))
                .findFirst()
                .orElse(null);
    }

    /**
     * Displays an error message in the status label and a popup dialog.
     *
     * @param title error dialog title
     * @param error exception to display
     */
    private void showError(String title, Throwable error) {
        statusLabel.setText(title + ": " + error.getMessage());

        JOptionPane.showMessageDialog(
                this,
                error.getMessage(),
                title,
                JOptionPane.ERROR_MESSAGE
        );
    }
}
