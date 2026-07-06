package com.example.workflowhub.dto;

import com.example.workflowhub.model.TaskType;
import jakarta.validation.constraints.NotNull;

public class TaskRequest {

    @NotNull
    private TaskType taskType;
    private String projectPath;

    public TaskRequest() {
    }

    public TaskRequest(TaskType taskType) {
        this.taskType = taskType;
    }

    public TaskRequest(TaskType taskType, String projectPath) {
        this.taskType = taskType;
        this.projectPath = projectPath;
    }

    public TaskType getTaskType() {
        return taskType;
    }

    public void setTaskType(TaskType taskType) {
        this.taskType = taskType;
    }

    public String getProjectPath() {
        return projectPath;
    }

    public void setProjectPath(String projectPath) {
        this.projectPath = projectPath;
    }
}