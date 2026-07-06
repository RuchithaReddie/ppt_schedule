package com.example.workflowhub.service;

import com.example.workflowhub.dto.TaskInfoResponse;
import com.example.workflowhub.dto.TaskRequest;
import com.example.workflowhub.dto.TaskResponse;
import com.example.workflowhub.model.TaskResult;
import com.example.workflowhub.task.TaskRegistry;
import com.example.workflowhub.task.WorkflowTask;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class TaskExecutionService {

    private final TaskRegistry taskRegistry;
    private final ReportStorageService reportStorageService;

    public TaskExecutionService(TaskRegistry taskRegistry, ReportStorageService reportStorageService) {
        this.taskRegistry = taskRegistry;
        this.reportStorageService = reportStorageService;
    }

    public List<TaskInfoResponse> getAvailableTasks() {
        return taskRegistry.getAllTasks().stream()
                .map(task -> new TaskInfoResponse(task.getTaskType(), task.getTaskName(), task.getDescription()))
                .toList();
    }

    public TaskResponse executeTask(TaskRequest request) {
        WorkflowTask workflowTask = taskRegistry.getTask(request.getTaskType());
        TaskResult taskResult = workflowTask.execute(request);
        String reportFileName = reportStorageService.saveReport(taskResult);

        return new TaskResponse(
                workflowTask.getTaskType(),
                workflowTask.getTaskName(),
                taskResult.getExecutionStatus(),
                taskResult.getSummary(),
                reportFileName,
                LocalDateTime.now());
    }
}