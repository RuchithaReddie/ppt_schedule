package com.example.workflowhub.controller;

import com.example.workflowhub.dto.TaskRequest;
import com.example.workflowhub.dto.TaskResponseDTO;
import com.example.workflowhub.dto.TaskResultDTO;
import com.example.workflowhub.service.TaskExecutionService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskExecutionService taskExecutionService;

    public TaskController(TaskExecutionService taskExecutionService) {
        this.taskExecutionService = taskExecutionService;
    }

    @GetMapping
    public ResponseEntity<List<TaskResponseDTO>> getAvailableTasks() {
        // Copilot: controller stays HTTP-only; orchestration is delegated to the service layer.
        return ResponseEntity.ok(taskExecutionService.getAvailableTasks());
    }

    @PostMapping("/execute")
    public ResponseEntity<TaskResultDTO> executeTask(@Valid @RequestBody TaskRequest request) {
        // Flow step: API request enters here, then moves to the use-case service boundary.
        return ResponseEntity.ok(taskExecutionService.executeTask(request));
    }
}