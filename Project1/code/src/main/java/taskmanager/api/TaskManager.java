package taskmanager.api;

import reactor.core.publisher.Mono;
import taskmanager.model.Task;
import taskmanager.model.WeatherForecast;
import taskmanager.service.DefaultTaskManagerBuilder;

import java.util.List;

/**
 * Main facade interface for the Smart Task Manager system.
 *
 * This interface provides high-level operations used by the UI layer,
 * including adding tasks, removing tasks, retrieving tasks,
 * fetching weather data, and accessing the schedule planner.
 */
public interface TaskManager {

    /**
     * Adds a new task to the system.
     *
     * @param task the task to add
     * @throws taskmanager.exception.InvalidTaskException if the task is null or contains invalid data
     */
    void addTask(Task task);

    /**
     * Removes a task by its unique ID.
     *
     * @param taskId the ID of the task to remove
     * @throws taskmanager.exception.InvalidTaskException if the task ID is null or empty
     * @throws taskmanager.exception.TaskNotFoundException if no task exists with the given ID
     */
    void removeTask(String taskId);

    /**
     * Returns all stored tasks.
     *
     * @return a list containing all tasks
     */
    List<Task> getTasks();

    /**
     * Fetches weather data asynchronously for a specific location.
     *
     * @param location the city or location name
     * @return a Mono that emits the weather forecast
     * @throws taskmanager.exception.WeatherAPIException if the weather service fails
     */
    Mono<WeatherForecast> fetchWeather(String location);

    /**
     * Returns the schedule planner used to generate recommendations.
     *
     * @return the schedule planner instance
     */
    SchedulePlanner getPlanner();

    /**
     * Creates a new TaskManager builder.
     *
     * @return a builder for constructing a TaskManager instance
     */
    static TaskManagerBuilder builder() {
        return new DefaultTaskManagerBuilder();
    }

    /**
     * Builder interface for configuring and creating a TaskManager.
     */
    interface TaskManagerBuilder {

        /**
         * Sets the weather API key.
         *
         * @param apiKey the API key used by the weather service
         * @return the current builder instance
         */
        TaskManagerBuilder withWeatherApiKey(String apiKey);

        /**
         * Sets the file path used for task storage.
         *
         * @param path the storage file path
         * @return the current builder instance
         */
        TaskManagerBuilder withStoragePath(String path);

        /**
         * Builds the configured TaskManager.
         *
         * @return a ready-to-use TaskManager instance
         */
        TaskManager build();
    }
}
