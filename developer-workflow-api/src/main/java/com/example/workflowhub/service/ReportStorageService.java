package com.example.workflowhub.service;

import com.example.workflowhub.model.TaskResult;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ReportStorageService {

    private static final DateTimeFormatter FILE_TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd-HHmmss");

    private final Path reportsFolder;

    public ReportStorageService(@Value("${workflow.reports.folder:reports}") String reportsFolder) {
        this.reportsFolder = Path.of(reportsFolder);
    }

    public String saveReport(TaskResult taskResult) {
        try {
            Files.createDirectories(reportsFolder);
            String fileName = buildFileName(taskResult);
            Files.writeString(reportsFolder.resolve(fileName), taskResult.getReportContent(), StandardCharsets.UTF_8);
            return fileName;
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to save task report", exception);
        }
    }

    private String buildFileName(TaskResult taskResult) {
        String timestamp = LocalDateTime.now().format(FILE_TIMESTAMP_FORMAT);
        String fileNamePrefix = getFileNamePrefix(taskResult);
        String fileExtension = getFileExtension(taskResult);
        return fileNamePrefix + "-" + timestamp + "." + fileExtension;
    }

    private String getFileNamePrefix(TaskResult taskResult) {
        if (taskResult.getReportFileNamePrefix() != null && !taskResult.getReportFileNamePrefix().isBlank()) {
            return sanitizeFileName(taskResult.getReportFileNamePrefix());
        }
        return sanitizeFileName(taskResult.getTitle());
    }

    private String getFileExtension(TaskResult taskResult) {
        if (taskResult.getReportFileExtension() != null && !taskResult.getReportFileExtension().isBlank()) {
            return taskResult.getReportFileExtension().replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
        }
        return "txt";
    }

    private String sanitizeFileName(String value) {
        return value.toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", "");
    }
}