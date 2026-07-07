package com.example.workflowhub.service;

import com.example.workflowhub.dto.TaskRequest;
import com.example.workflowhub.dto.TaskResponseDTO;
import com.example.workflowhub.dto.TaskResultDTO;
import java.util.List;

public interface TaskExecutionService {

    List<TaskResponseDTO> getAvailableTasks();

    TaskResultDTO executeTask(TaskRequest request);
}