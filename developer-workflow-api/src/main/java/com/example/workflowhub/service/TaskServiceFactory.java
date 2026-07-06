package com.example.workflowhub.service;

import com.example.workflowhub.exception.TaskNotFoundException;
import com.example.workflowhub.model.TaskType;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class TaskServiceFactory {

    private final Map<TaskType, TaskService> taskServices;

    public TaskServiceFactory(List<TaskService> taskServices) {
        // Copilot: refactored into factory pattern for extensibility and Open/Closed task routing.
        this.taskServices = taskServices.stream()
                .collect(Collectors.toUnmodifiableMap(TaskService::getTaskType, Function.identity()));
    }

    public List<TaskService> getAllTaskServices() {
        return List.copyOf(taskServices.values());
    }

    public TaskService getTaskService(TaskType taskType) {
        // Flow step: one TaskType maps to one substitutable TaskService implementation.
        TaskService taskService = taskServices.get(taskType);
        if (taskService == null) {
            throw new TaskNotFoundException("Task not found: " + taskType);
        }
        return taskService;
    }
}