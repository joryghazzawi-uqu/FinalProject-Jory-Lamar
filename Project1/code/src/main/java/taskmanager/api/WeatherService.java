package taskmanager.api;

import reactor.core.publisher.Mono;
import taskmanager.model.WeatherForecast;

/**
 * Provides weather forecast data asynchronously.
 */
public interface WeatherService {

    /**
     * Fetches weather data for a location.
     *
     * @param location the city or location name
     * @return a Mono emitting the weather forecast
     */
    Mono<WeatherForecast> getForecast(String location);
}
