package taskmanager.api;

/**
 * Exception thrown when a task contains invalid data.
 */
public class InvalidTaskException extends RuntimeException {

    /**
     * Creates an exception with a message.
     *
     * @param message explanation of the validation error
     */
    public InvalidTaskException(String message) {
        super(message);
    }

    /**
     * Creates an exception with a message and original cause.
     *
     * @param message explanation of the validation error
     * @param cause original exception cause
     */
    public InvalidTaskException(String message, Throwable cause) {
        super(message, cause);
    }
}