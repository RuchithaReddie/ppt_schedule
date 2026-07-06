package com.example.workflowhub.dto;

import com.example.workflowhub.model.TaskType;

public class TaskResponseDTO {

    private TaskType taskType;
    private String taskName;
    private String description;

    public TaskResponseDTO() {
    }

    public TaskResponseDTO(TaskType taskType, String taskName, String description) {
        this.taskType = taskType;
        this.taskName = taskName;
        this.description = description;
    }

    public TaskType getTaskType() {
        return taskType;
    }

    public void setTaskType(TaskType taskType) {
        this.taskType = taskType;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}