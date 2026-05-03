package taskmanager.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.retry.Retry;
import taskmanager.api.WeatherService;
import taskmanager.exception.WeatherAPIException;
import taskmanager.model.WeatherForecast;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WeatherService implementation that connects to the OpenWeatherMap API.
 *
 * This class fetches real weather data, converts the API JSON response into
 * a WeatherForecast object, caches results for a short time, and uses Reactor
 * to run network operations on a background thread.
 */
public class OpenWeatherService implements WeatherService {
    private static final String WEATHER_URL =
            "https://api.openweathermap.org/data/2.5/weather";

    private static final Duration CACHE_EXPIRATION = Duration.ofMinutes(15);

    private final String apiKey;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();

    /**
     * Creates an OpenWeatherService.
     *
     * @param apiKey OpenWeatherMap API key
     */
    public OpenWeatherService(String apiKey) {
        this.apiKey = apiKey;
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Fetches a weather forecast for a location.
     *
     * @param location city or location name
     * @return Mono emitting the weather forecast
     * @throws WeatherAPIException if the location is empty or the API request fails
     */
    @Override
    public Mono<WeatherForecast> getForecast(String location) {
        if (location == null || location.isBlank()) {
            return Mono.error(new WeatherAPIException("Location must not be empty"));
        }

        return Mono.fromCallable(() -> fetchForecast(location.trim()))
                .subscribeOn(Schedulers.boundedElastic())
                .retryWhen(
                        Retry.backoff(2, Duration.ofSeconds(1))
                                .filter(error ->
                                        error instanceof IOException
                                                || error instanceof WeatherAPIException
                                )
                )
                .onErrorMap(error -> {
                    if (error instanceof WeatherAPIException) {
                        return error;
                    }
                    return new WeatherAPIException(
                            "Failed to fetch weather for " + location,
                            error
                    );
                });
    }

    /**
     * Performs the HTTP request and parses the API response.
     *
     * @param location city or location name
     * @return parsed weather forecast
     * @throws IOException if network or JSON parsing fails
     * @throws InterruptedException if the HTTP request is interrupted
     */
    private WeatherForecast fetchForecast(String location)
            throws IOException, InterruptedException {

        String cacheKey = location.toLowerCase();
        CacheEntry cached = cache.get(cacheKey);

        if (cached != null && LocalDateTime.now().isBefore(cached.expiration())) {
            return cached.forecast();
        }

        String encodedLocation = URLEncoder.encode(
                location,
                StandardCharsets.UTF_8
        );

        URI uri = URI.create(
                WEATHER_URL
                        + "?q=" + encodedLocation
                        + "&units=metric"
                        + "&appid=" + apiKey
        );

        HttpRequest request = HttpRequest.newBuilder(uri)
                .GET()
                .timeout(Duration.ofSeconds(10))
                .build();

        HttpResponse<String> response = httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );

        if (response.statusCode() >= 400) {
            throw new WeatherAPIException(
                    "Weather API returned status " + response.statusCode()
            );
        }

        JsonNode node = objectMapper.readTree(response.body());

        double temperature = node.path("main").path("temp").asDouble(Double.NaN);
        String condition = readCondition(node);
        double precipitationProbability = calculatePrecipitationProbability(node);

        WeatherForecast forecast = new WeatherForecast(
                location,
                LocalDateTime.now(),
                temperature,
                condition,
                precipitationProbability
        );

        cache.put(
                cacheKey,
                new CacheEntry(
                        forecast,
                        LocalDateTime.now().plus(CACHE_EXPIRATION)
                )
        );

        return forecast;
    }

    /**
     * Reads the main weather condition from the JSON response.
     *
     * @param node API response JSON
     * @return weather condition text
     */
    private String readCondition(JsonNode node) {
        JsonNode weatherArray = node.path("weather");

        if (weatherArray.isArray() && !weatherArray.isEmpty()) {
            return weatherArray.get(0).path("main").asText("Unknown");
        }

        return "Unknown";
    }

    /**
     * Calculates an approximate precipitation probability.
     *
     * OpenWeather current weather API does not always provide probability,
     * so this method estimates it using rain, snow, and cloud data.
     *
     * @param node API response JSON
     * @return probability value between 0.0 and 1.0
     */
    private double calculatePrecipitationProbability(JsonNode node) {
        if (node.has("rain") || node.has("snow")) {
            return 0.9;
        }

        return node.path("clouds").path("all").asDouble(0.0) / 100.0;
    }

    /**
     * Cache record storing a forecast and its expiration time.
     *
     * @param forecast cached forecast
     * @param expiration cache expiration time
     */
    private record CacheEntry(
            WeatherForecast forecast,
            LocalDateTime expiration
    ) {
    }
}
