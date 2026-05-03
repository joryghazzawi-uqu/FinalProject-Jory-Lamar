package taskmanager.model;

import java.time.LocalDateTime;

/**
 * Represents weather forecast data used by the schedule planner.
 */
public class WeatherForecast {
    private final String location;
    private final LocalDateTime time;
    private final double temperatureCelsius;
    private final String condition;
    private final double precipitationProbability;

    /**
     * Creates a weather forecast object.
     *
     * @param location forecast location
     * @param time forecast time
     * @param temperatureCelsius temperature in Celsius
     * @param condition weather condition such as Clear, Rain, or Clouds
     * @param precipitationProbability probability of precipitation from 0.0 to 1.0
     */
    public WeatherForecast(
            String location,
            LocalDateTime time,
            double temperatureCelsius,
            String condition,
            double precipitationProbability
    ) {
        this.location = location;
        this.time = time;
        this.temperatureCelsius = temperatureCelsius;
        this.condition = condition;
        this.precipitationProbability = precipitationProbability;
    }

    /**
     * @return forecast location
     */
    public String getLocation() {
        return location;
    }

    /**
     * @return forecast time
     */
    public LocalDateTime getTime() {
        return time;
    }

    /**
     * @return temperature in Celsius
     */
    public double getTemperatureCelsius() {
        return temperatureCelsius;
    }

    /**
     * @return weather condition
     */
    public String getCondition() {
        return condition;
    }

    /**
     * @return precipitation probability from 0.0 to 1.0
     */
    public double getPrecipitationProbability() {
        return precipitationProbability;
    }
}
