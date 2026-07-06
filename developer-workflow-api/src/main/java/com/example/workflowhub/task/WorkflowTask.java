package com.example.workflowhub.task;

import com.example.workflowhub.dto.TaskRequest;
import com.example.workflowhub.model.TaskResult;
import com.example.workflowhub.model.TaskType;

/**
 * Common contract for every workflow automation task.
 */
public interface WorkflowTask {

    TaskType getTaskType();

    String getTaskName();

    String getDescription();

    TaskResult execute(TaskRequest request);
}