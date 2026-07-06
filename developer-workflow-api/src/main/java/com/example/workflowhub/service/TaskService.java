package com.example.workflowhub.service;

import com.example.workflowhub.dto.TaskRequest;
import com.example.workflowhub.model.TaskResult;
import com.example.workflowhub.model.TaskType;

public interface TaskService {

    TaskType getTaskType();

    String getTaskName();

    String getDescription();

    TaskResult execute(TaskRequest request);
}