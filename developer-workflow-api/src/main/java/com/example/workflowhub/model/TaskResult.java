package com.example.workflowhub.model;

public class TaskResult {

    private String title;
    private String summary;
    private String reportContent;
    private String executionStatus;
    private String reportFileNamePrefix;
    private String reportFileExtension;

    public TaskResult() {
    }

    public TaskResult(String title, String summary, String reportContent, String executionStatus) {
        this.title = title;
        this.summary = summary;
        this.reportContent = reportContent;
        this.executionStatus = executionStatus;
    }

    public TaskResult(String title, String summary, String reportContent, String executionStatus,
            String reportFileNamePrefix, String reportFileExtension) {
        this.title = title;
        this.summary = summary;
        this.reportContent = reportContent;
        this.executionStatus = executionStatus;
        this.reportFileNamePrefix = reportFileNamePrefix;
        this.reportFileExtension = reportFileExtension;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getReportContent() {
        return reportContent;
    }

    public void setReportContent(String reportContent) {
        this.reportContent = reportContent;
    }

    public String getExecutionStatus() {
        return executionStatus;
    }

    public void setExecutionStatus(String executionStatus) {
        this.executionStatus = executionStatus;
    }

    public String getReportFileNamePrefix() {
        return reportFileNamePrefix;
    }

    public void setReportFileNamePrefix(String reportFileNamePrefix) {
        this.reportFileNamePrefix = reportFileNamePrefix;
    }

    public String getReportFileExtension() {
        return reportFileExtension;
    }

    public void setReportFileExtension(String reportFileExtension) {
        this.reportFileExtension = reportFileExtension;
    }
}