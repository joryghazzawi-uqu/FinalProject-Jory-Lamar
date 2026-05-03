package taskmanager.exception;

/**
 * Exception thrown when weather data cannot be retrieved or processed.
 */
public class WeatherAPIException extends RuntimeException {

    /**
     * Creates an exception with a message.
     *
     * @param message explanation of the weather API error
     */
    public WeatherAPIException(String message) {
        super(message);
    }

    /**
     * Creates an exception with a message and original cause.
     *
     * @param message explanation of the weather API error
     * @param cause original exception cause
     */
    public WeatherAPIException(String message, Throwable cause) {
        super(message, cause);
    }
}
