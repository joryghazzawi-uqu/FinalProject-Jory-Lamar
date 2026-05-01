package taskmanager.impl;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import taskmanager.api.WeatherForecast;
import taskmanager.service.WeatherService;

import java.time.LocalDateTime;

/**
 * Fallback weather service used when no real API key is provided.
 *
 * This allows the application to run even without internet access or
 * weather API configuration.
 */
public class FallbackWeatherService implements WeatherService {

    /**
     * Returns a default clear-weather forecast.
     *
     * @param location location name
     * @return Mono emitting a default WeatherForecast
     */
    @Override
    public Mono<WeatherForecast> getForecast(String location) {
        return Mono.fromCallable(() -> new WeatherForecast(
                        location == null || location.isBlank() ? "Unknown" : location,
                        LocalDateTime.now(),
                        22.0,
                        "Clear",
                        0.1
                ))
                .subscribeOn(Schedulers.boundedElastic());
    }
}
