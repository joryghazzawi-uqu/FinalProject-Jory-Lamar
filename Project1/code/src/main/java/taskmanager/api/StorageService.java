package taskmanager.api;

import reactor.core.publisher.Mono;
import taskmanager.model.Task;

import java.util.List;

/**
 * Handles task persistence.
 */
public interface StorageService {

    /**
     * Saves tasks to persistent storage.
     *
     * @param tasks the tasks to save
     * @return a Mono that completes when saving finishes
     */
    Mono<Void> saveTasks(List<Task> tasks);

    /**
     * Loads tasks from persistent storage.
     *
     * @return a Mono emitting the loaded tasks
     */
    Mono<List<Task>> loadTasks();
}
