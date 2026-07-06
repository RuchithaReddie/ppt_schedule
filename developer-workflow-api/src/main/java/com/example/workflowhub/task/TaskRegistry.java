package com.example.workflowhub.task;

import com.example.workflowhub.exception.TaskNotFoundException;
import com.example.workflowhub.model.TaskType;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class TaskRegistry {

    private final Map<TaskType, WorkflowTask> workflowTasks;

    public TaskRegistry(List<WorkflowTask> workflowTasks) {
        this.workflowTasks = workflowTasks.stream()
                .collect(Collectors.toUnmodifiableMap(WorkflowTask::getTaskType, Function.identity()));
    }

    public List<WorkflowTask> getAllTasks() {
        return List.copyOf(workflowTasks.values());
    }

    public WorkflowTask getTask(TaskType taskType) {
        WorkflowTask workflowTask = workflowTasks.get(taskType);
        if (workflowTask == null) {
            throw new TaskNotFoundException("Task not found: " + taskType);
        }
        return workflowTask;
    }
}