package taskmanager.model;

/**
 * Represents a scheduling recommendation for a specific task.
 *
 * @param task the task related to the recommendation
 * @param recommendation the recommendation message
 */
public record ScheduleRecommendation(Task task, String recommendation) {
}
