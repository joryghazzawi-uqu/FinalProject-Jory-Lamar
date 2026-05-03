package taskmanager.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import taskmanager.api.StorageService;
import taskmanager.exception.InvalidTaskException;
import taskmanager.model.Task;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * JSON file implementation of StorageService.
 *
 * Saves and loads tasks from a local JSON file. File operations are executed
 * on Reactor's boundedElastic scheduler to avoid blocking the Swing UI thread.
 */
public class FileTaskStorageService implements StorageService {
    private final Path storagePath;
    private final ObjectMapper objectMapper;

    /**
     * Creates file storage service.
     *
     * @param filePath path to JSON file; defaults to tasks.json if empty
     */
    public FileTaskStorageService(String filePath) {
        this.storagePath = Path.of(
                filePath == null || filePath.isBlank() ? "tasks.json" : filePath
        );

        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    /**
     * Saves tasks to JSON.
     *
     * @param tasks tasks to save
     * @return Mono that completes when writing is done
     * @throws InvalidTaskException if saving fails
     */
    @Override
    public Mono<Void> saveTasks(List<Task> tasks) {
        return Mono.fromRunnable(() -> {
                    try {
                        Path parent = storagePath.getParent();
                        if (parent != null) {
                            Files.createDirectories(parent);
                        }

                        String json = objectMapper
                                .writerWithDefaultPrettyPrinter()
                                .writeValueAsString(tasks);

                        Files.writeString(storagePath, json);
                    } catch (IOException e) {
                        throw new InvalidTaskException("Unable to save tasks", e);
                    }
                })
                .subscribeOn(Schedulers.boundedElastic())
                .then();
    }

    /**
     * Loads tasks from JSON.
     *
     * @return Mono emitting loaded tasks or an empty list when no file exists
     * @throws InvalidTaskException if loading fails
     */
    @Override
    public Mono<List<Task>> loadTasks() {
        return Mono.fromCallable(() -> {
                    if (!Files.exists(storagePath)) {
                        return new ArrayList<Task>();
                    }

                    try {
                        String content = Files.readString(storagePath);
                        return objectMapper.readValue(
                                content,
                                new TypeReference<List<Task>>() {
                                }
                        );
                    } catch (IOException e) {
                        throw new InvalidTaskException("Unable to load tasks", e);
                    }
                })
                .subscribeOn(Schedulers.boundedElastic());
    }
}
