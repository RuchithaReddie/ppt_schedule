package com.example.workflowhub.service;

import com.example.workflowhub.dto.TaskRequest;
import com.example.workflowhub.dto.TaskResponseDTO;
import com.example.workflowhub.dto.TaskResultDTO;
import com.example.workflowhub.model.TaskResult;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class DefaultTaskExecutionService implements TaskExecutionService {

    private final TaskServiceFactory taskServiceFactory;
    private final ReportStorageService reportStorageService;

    public DefaultTaskExecutionService(TaskServiceFactory taskServiceFactory, ReportStorageService reportStorageService) {
        this.taskServiceFactory = taskServiceFactory;
        this.reportStorageService = reportStorageService;
    }

    @Override
    public List<TaskResponseDTO> getAvailableTasks() {
        return taskServiceFactory.getAllTaskServices().stream()
                .map(this::toTaskResponse)
                .toList();
    }

    @Override
    public TaskResultDTO executeTask(TaskRequest request) {
        // Copilot: generated service layer based on task type and kept controller free of business logic.
        // Flow step: factory selects the correct task service, service runs the task, DTO returns to Angular.
        TaskService taskService = taskServiceFactory.getTaskService(request.getTaskType());
        TaskResult taskResult = taskService.execute(request);
        String reportFileName = reportStorageService.saveReport(taskResult);
        return toTaskResult(taskService, taskResult, reportFileName);
    }

    private TaskResponseDTO toTaskResponse(TaskService taskService) {
        return new TaskResponseDTO(taskService.getTaskType(), taskService.getTaskName(), taskService.getDescription());
    }

    private TaskResultDTO toTaskResult(TaskService taskService, TaskResult taskResult, String reportFileName) {
        return new TaskResultDTO(
                taskService.getTaskType(),
                taskService.getTaskName(),
                taskResult.getExecutionStatus(),
                taskResult.getSummary(),
                reportFileName,
                LocalDateTime.now());
    }
}