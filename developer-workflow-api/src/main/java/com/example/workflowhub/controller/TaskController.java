package com.example.workflowhub.controller;

import com.example.workflowhub.dto.TaskInfoResponse;
import com.example.workflowhub.dto.TaskRequest;
import com.example.workflowhub.dto.TaskResponse;
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
    public ResponseEntity<List<TaskInfoResponse>> getAvailableTasks() {
        return ResponseEntity.ok(taskExecutionService.getAvailableTasks());
    }

    @PostMapping("/execute")
    public ResponseEntity<TaskResponse> executeTask(@Valid @RequestBody TaskRequest request) {
        return ResponseEntity.ok(taskExecutionService.executeTask(request));
    }
}