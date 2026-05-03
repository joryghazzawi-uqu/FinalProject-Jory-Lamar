package taskmanager.exception;

/**
 * Exception thrown when a task cannot be found.
 */
public class TaskNotFoundException extends RuntimeException {

    /**
     * Creates an exception for a missing task.
     *
     * @param taskId the missing task ID
     */
    public TaskNotFoundException(String taskId) {
        super("Task not found: " + taskId);
    }
}
