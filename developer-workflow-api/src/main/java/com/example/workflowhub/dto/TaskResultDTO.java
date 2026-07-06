package com.example.workflowhub.dto;

import com.example.workflowhub.model.TaskType;
import java.time.LocalDateTime;

public class TaskResultDTO {

    private TaskType taskType;
    private String taskName;
    private String status;
    private String summary;
    private String reportFileName;
    private LocalDateTime generatedAt;

    public TaskResultDTO() {
    }

    public TaskResultDTO(TaskType taskType, String taskName, String status, String summary, String reportFileName,
            LocalDateTime generatedAt) {
        this.taskType = taskType;
        this.taskName = taskName;
        this.status = status;
        this.summary = summary;
        this.reportFileName = reportFileName;
        this.generatedAt = generatedAt;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getReportFileName() {
        return reportFileName;
    }

    public void setReportFileName(String reportFileName) {
        this.reportFileName = reportFileName;
    }

    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(LocalDateTime generatedAt) {
        this.generatedAt = generatedAt;
    }
}