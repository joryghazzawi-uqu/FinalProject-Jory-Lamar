package taskmanager.impl;

import reactor.core.publisher.Mono;
import taskmanager.api.SchedulePlanner;
import taskmanager.api.TaskManager;
import taskmanager.api.TaskService;
import taskmanager.api.WeatherService;
import taskmanager.exception.WeatherAPIException;
import taskmanager.model.Task;
import taskmanager.model.WeatherForecast;

import java.util.List;

/**
 * Default implementation of the TaskManager facade.
 *
 * This class connects the UI-facing API with the internal services:
 * TaskService, WeatherService, and SchedulePlanner.
 */
public class DefaultTaskManager implements TaskManager {
    private final TaskService taskService;
    private final SchedulePlanner planner;
    private final WeatherService weatherService;

    /**
     * Creates a DefaultTaskManager.
     *
     * @param taskService service used for task operations
     * @param planner service used for schedule recommendations
     * @param weatherService service used for weather lookup
     */
    public DefaultTaskManager(
            TaskService taskService,
            SchedulePlanner planner,
            WeatherService weatherService
    ) {
        this.taskService = taskService;
        this.planner = planner;
        this.weatherService = weatherService;
    }

    /**
     * Adds or updates a task.
     *
     * @param task the task to save
     * @throws taskmanager.exception.InvalidTaskException if the task is invalid
     */
    @Override
    public void addTask(Task task) {
        taskService.addTask(task).block();
    }

    /**
     * Removes a task by ID.
     *
     * @param taskId the task ID
     * @throws taskmanager.exception.TaskNotFoundException if the task is not found
     */
    @Override
    public void removeTask(String taskId) {
        taskService.removeTask(taskId).block();
    }

    /**
     * Gets all tasks.
     *
     * @return list of stored tasks
     */
    @Override
    public List<Task> getTasks() {
        return taskService.findAllTasksAsList().block();
    }

    /**
     * Fetches weather asynchronously.
     *
     * @param location the location name
     * @return Mono emitting weather forecast
     */
    @Override
    public Mono<WeatherForecast> fetchWeather(String location) {
        return weatherService.getForecast(location)
                .onErrorMap(error -> {
                    if (error instanceof WeatherAPIException) {
                        return error;
                    }
                    return new WeatherAPIException("Weather lookup failed", error);
                });
    }

    /**
     * Gets the schedule planner.
     *
     * @return planner instance
     */
    @Override
    public SchedulePlanner getPlanner() {
        return planner;
    }
}
