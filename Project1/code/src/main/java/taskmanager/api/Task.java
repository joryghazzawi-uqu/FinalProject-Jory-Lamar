package taskmanager.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

/**
 * Represents a task in the Smart Task Manager.
 *
 * A task contains an ID, title, description, due date and time,
 * and a flag indicating whether it is affected by weather.
 */
public class Task {
    private final String id;
    private String title;
    private String description;
    private LocalDateTime dueDateTime;
    private boolean weatherSensitive;

    /**
     * Creates a new task.
     *
     * @param id unique task ID
     * @param title task title
     * @param dueDateTime task due date and time
     * @param weatherSensitive true if the task depends on weather conditions
     */
    @JsonCreator
    public Task(
            @JsonProperty("id") String id,
            @JsonProperty("title") String title,
            @JsonProperty("dueDateTime") LocalDateTime dueDateTime,
            @JsonProperty("weatherSensitive") boolean weatherSensitive
    ) {
        this.id = id;
        this.title = title;
        this.dueDateTime = dueDateTime;
        this.weatherSensitive = weatherSensitive;
    }

    /**
     * @return the task ID
     */
    public String getId() {
        return id;
    }

    /**
     * @return the task title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Updates the task title.
     *
     * @param title the new title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @return the task description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Updates the task description.
     *
     * @param description the new description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return the task due date and time
     */
    public LocalDateTime getDueDateTime() {
        return dueDateTime;
    }

    /**
     * Updates the task due date and time.
     *
     * @param dueDateTime the new due date and time
     */
    public void setDueDateTime(LocalDateTime dueDateTime) {
        this.dueDateTime = dueDateTime;
    }

    /**
     * @return true if the task depends on weather conditions
     */
    public boolean isWeatherSensitive() {
        return weatherSensitive;
    }

    /**
     * Updates whether the task is weather sensitive.
     *
     * @param weatherSensitive true if weather affects this task
     */
    public void setWeatherSensitive(boolean weatherSensitive) {
        this.weatherSensitive = weatherSensitive;
    }
}