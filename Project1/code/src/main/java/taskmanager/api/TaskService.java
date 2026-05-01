package taskmanager.api;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Service interface responsible for task CRUD operations.
 *
 * This service uses Project Reactor types:
 * Mono for single-result operations and Flux for multi-result streams.
 */
public interface TaskService {

    /**
     * Adds or updates a task asynchronously.
     *
     * @param task the task to add or update
     * @return a Mono that completes when the task is saved
     * @throws InvalidTaskException if the task data is invalid
     */
    Mono<Void> addTask(Task task);

    /**
     * Removes a task asynchronously by ID.
     *
     * @param taskId the task ID
     * @return a Mono that completes when the task is removed
     * @throws InvalidTaskException if the task ID is invalid
     * @throws TaskNotFoundException if the task does not exist
     */
    Mono<Void> removeTask(String taskId);

    /**
     * Finds a task by its ID.
     *
     * @param taskId the task ID
     * @return a Mono emitting the task if found
     * @throws TaskNotFoundException if the task does not exist
     */
    Mono<Task> findTaskById(String taskId);

    /**
     * Returns all tasks as a reactive stream.
     *
     * @return a Flux emitting all stored tasks
     */
    Flux<Task> findAllTasks();

    /**
     * Returns all tasks as a List wrapped in a Mono.
     *
     * @return a Mono emitting a list of all tasks
     */
    Mono<List<Task>> findAllTasksAsList();
}