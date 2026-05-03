package taskmanager.service;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import taskmanager.api.SchedulePlanner;
import taskmanager.api.WeatherService;
import taskmanager.model.ScheduleRecommendation;
import taskmanager.model.Task;
import taskmanager.model.WeatherForecast;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Default implementation of SchedulePlanner.
 *
 * Generates recommendations by checking task weather sensitivity and current
 * forecast conditions. Uses Reactor so recommendation processing can happen
 * on a background thread.
 */
public class DefaultSchedulePlanner implements SchedulePlanner {
    private final WeatherService weatherService;

    /**
     * Creates a schedule planner.
     *
     * @param weatherService service used to fetch weather when location is provided
     */
    public DefaultSchedulePlanner(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    /**
     * Generates recommendations using a provided weather forecast.
     *
     * @param tasks tasks to analyze
     * @param forecast weather forecast
     * @return Mono emitting schedule recommendations
     */
    @Override
    public Mono<List<ScheduleRecommendation>> suggestSchedule(
            List<Task> tasks,
            WeatherForecast forecast
    ) {
        return Mono.fromCallable(() ->
                        tasks.stream()
                                .map(task -> new ScheduleRecommendation(
                                        task,
                                        recommendationFor(task, forecast)
                                ))
                                .collect(Collectors.toList())
                )
                .subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Fetches forecast by location then generates recommendations.
     *
     * @param tasks tasks to analyze
     * @param location location used for weather lookup
     * @return Mono emitting schedule recommendations
     */
    @Override
    public Mono<List<ScheduleRecommendation>> suggestScheduleForLocation(
            List<Task> tasks,
            String location
    ) {
        return weatherService.getForecast(location)
                .flatMap(forecast -> suggestSchedule(tasks, forecast));
    }

    /**
     * Creates one recommendation message for one task.
     *
     * @param task task to analyze
     * @param forecast weather forecast
     * @return recommendation message
     */
    private String recommendationFor(Task task, WeatherForecast forecast) {
        if (!task.isWeatherSensitive()) {
            return "Task is not weather sensitive. Weather: " + forecast.getCondition();
        }

        if (forecast.getPrecipitationProbability() > 0.6) {
            return "High risk: postpone or move indoors. Weather: " + forecast.getCondition();
        }

        if (forecast.getTemperatureCelsius() >= 35) {
            return "High temperature: consider a cooler time. Temperature: "
                    + forecast.getTemperatureCelsius() + "\u00b0C";
        }

        return "Good to go as planned. Weather: " + forecast.getCondition();
    }
}
