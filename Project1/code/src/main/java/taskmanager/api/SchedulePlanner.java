package taskmanager.api;

import reactor.core.publisher.Mono;
import taskmanager.model.ScheduleRecommendation;
import taskmanager.model.Task;
import taskmanager.model.WeatherForecast;

import java.util.List;

/**
 * Generates schedule recommendations based on tasks and weather data.
 */
public interface SchedulePlanner {

    /**
     * Generates recommendations for a list of tasks using an existing weather forecast.
     *
     * @param tasks the tasks to analyze
     * @param forecast the weather forecast used for decision making
     * @return a Mono emitting a list of schedule recommendations
     */
    Mono<List<ScheduleRecommendation>> suggestSchedule(
            List<Task> tasks,
            WeatherForecast forecast
    );

    /**
     * Fetches weather for a location and generates recommendations.
     *
     * @param tasks the tasks to analyze
     * @param location the location used to fetch weather data
     * @return a Mono emitting a list of schedule recommendations
     * @throws taskmanager.exception.WeatherAPIException if weather data cannot be fetched
     */
    Mono<List<ScheduleRecommendation>> suggestScheduleForLocation(
            List<Task> tasks,
            String location
    );
}
