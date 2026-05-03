package taskmanager.impl;

import taskmanager.api.StorageService;
import taskmanager.api.TaskManager;
import taskmanager.api.WeatherService;
import taskmanager.service.FileTaskStorageService;
import taskmanager.service.OpenWeatherService;

/**
 * Builder implementation for creating a configured TaskManager.
 */
public class DefaultTaskManagerBuilder implements TaskManager.TaskManagerBuilder {
    private String apiKey;
    private String storagePath = "tasks.json";

    /**
     * Sets the weather API key.
     *
     * @param apiKey weather API key
     * @return current builder
     */
    @Override
    public TaskManager.TaskManagerBuilder withWeatherApiKey(String apiKey) {
        this.apiKey = apiKey;
        return this;
    }

    /**
     * Sets the storage path.
     *
     * @param path JSON file path for task storage
     * @return current builder
     */
    @Override
    public TaskManager.TaskManagerBuilder withStoragePath(String path) {
        if (path != null && !path.isBlank()) {
            this.storagePath = path;
        }
        return this;
    }

    /**
     * Builds a TaskManager with task storage, weather service, and planner.
     *
     * @return configured TaskManager
     */
    @Override
    public TaskManager build() {
        WeatherService weatherService = createWeatherService();
        StorageService storageService = new FileTaskStorageService(storagePath);
        DefaultTaskService taskService = new DefaultTaskService(storageService);
        DefaultSchedulePlanner planner = new DefaultSchedulePlanner(weatherService);

        return new DefaultTaskManager(taskService, planner, weatherService);
    }

    /**
     * Creates real weather service when API key exists, otherwise fallback service.
     *
     * @return weather service implementation
     */
    private WeatherService createWeatherService() {
        if (apiKey == null || apiKey.isBlank()) {
            return new FallbackWeatherService();
        }
        return new OpenWeatherService(apiKey);
    }
}
