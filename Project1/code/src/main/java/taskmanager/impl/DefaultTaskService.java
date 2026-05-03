package taskmanager.impl;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import taskmanager.api.StorageService;
import taskmanager.api.TaskService;
import taskmanager.exception.InvalidTaskException;
import taskmanager.exception.TaskNotFoundException;
import taskmanager.model.Task;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Default implementation of TaskService.
 *
 * Stores tasks in a thread-safe ConcurrentHashMap and saves them to JSON storage.
 * Reactor is used with boundedElastic scheduler to run file operations away from
 * the Swing Event Dispatch Thread.
 */
public class DefaultTaskService implements TaskService {
    private final Map<String, Task> tasks = new ConcurrentHashMap<>();
    private final StorageService storageService;

    /**
     * Creates the service and loads existing tasks from storage.
     *
     * @param storageService storage service used to load and save tasks
     */
    public DefaultTaskService(StorageService storageService) {
        this.storageService = storageService;

        storageService.loadTasks()
                .subscribeOn(Schedulers.boundedElastic())
                .doOnNext(loadedTasks ->
                        loadedTasks.forEach(task -> tasks.put(task.getId(), task))
                )
                .doOnError(error ->
                        System.err.println("Failed to load tasks: " + error.getMessage())
                )
                .subscribe();
    }

    /**
     * Adds or updates a task asynchronously.
     *
     * @param task task to add or update
     * @return Mono that completes after validation, memory update, and file save
     * @throws InvalidTaskException if task data is invalid
     */
    @Override
    public Mono<Void> addTask(Task task) {
        return Mono.fromRunnable(() -> validateTask(task))
                .then(Mono.fromRunnable(() -> tasks.put(task.getId(), task)))
                .then(Mono.defer(() -> storageService.saveTasks(List.copyOf(tasks.values()))))
                .then()
                .subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Removes a task asynchronously.
     *
     * @param taskId task ID
     * @return Mono that completes after removal and file save
     * @throws InvalidTaskException if taskId is empty
     * @throws TaskNotFoundException if taskId does not exist
     */
    @Override
    public Mono<Void> removeTask(String taskId) {
        return Mono.fromRunnable(() -> {
                    if (taskId == null || taskId.isBlank()) {
                        throw new InvalidTaskException("Task ID must not be empty");
                    }

                    if (tasks.remove(taskId) == null) {
                        throw new TaskNotFoundException(taskId);
                    }
                })
                .then(Mono.defer(() -> storageService.saveTasks(List.copyOf(tasks.values()))))
                .then()
                .subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Finds a task by ID.
     *
     * @param taskId task ID
     * @return Mono emitting the task
     * @throws TaskNotFoundException if no task exists with the given ID
     */
    @Override
    public Mono<Task> findTaskById(String taskId) {
        return Mono.justOrEmpty(tasks.get(taskId))
                .switchIfEmpty(Mono.error(new TaskNotFoundException(taskId)))
                .subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Returns all tasks as a Flux.
     *
     * @return Flux containing all tasks
     */
    @Override
    public Flux<Task> findAllTasks() {
        return Flux.defer(() -> Flux.fromIterable(new ArrayList<>(tasks.values())))
                .subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Returns all tasks as a List inside a Mono.
     *
     * @return Mono emitting list of tasks
     */
    @Override
    public Mono<List<Task>> findAllTasksAsList() {
        return Mono.<List<Task>>fromCallable(() -> new ArrayList<>(tasks.values()))
                .subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Validates task data before saving.
     *
     * @param task task to validate
     * @throws InvalidTaskException if validation fails
     */
    private void validateTask(Task task) {
        if (task == null) {
            throw new InvalidTaskException("Task cannot be null");
        }

        if (task.getId() == null || task.getId().isBlank()) {
            throw new InvalidTaskException("Task ID must not be empty");
        }

        if (task.getTitle() == null || task.getTitle().isBlank()) {
            throw new InvalidTaskException("Task title must not be empty");
        }

        if (task.getDueDateTime() == null) {
            throw new InvalidTaskException("Task due date and time must be set");
        }

        if (task.getDueDateTime().isBefore(LocalDateTime.now())) {
            throw new InvalidTaskException("Task due date must be in the future");
        }
    }
}
