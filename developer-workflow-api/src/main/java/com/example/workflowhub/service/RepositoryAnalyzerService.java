package com.example.workflowhub.service;

import com.example.workflowhub.dto.TaskRequest;
import com.example.workflowhub.model.TaskResult;
import com.example.workflowhub.model.TaskType;
import com.example.workflowhub.task.ProjectStructure;
import com.example.workflowhub.task.ProjectStructureAnalyzer;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class RepositoryAnalyzerService implements TaskService {

    private final ProjectStructureAnalyzer projectStructureAnalyzer;

    public RepositoryAnalyzerService(ProjectStructureAnalyzer projectStructureAnalyzer) {
        this.projectStructureAnalyzer = projectStructureAnalyzer;
    }

    @Override
    public TaskType getTaskType() {
        return TaskType.REPOSITORY_ANALYZER;
    }

    @Override
    public String getTaskName() {
        return "Repository Analyzer";
    }

    @Override
    public String getDescription() {
        return "Analyzes repository structure and produces a summary report.";
    }

    @Override
    public TaskResult execute(TaskRequest request) {
        ProjectStructure analysis = projectStructureAnalyzer.analyze(request, getTaskName());

        return new TaskResult(
                "Repository Analysis",
                buildSummary(analysis),
                buildMarkdownReport(analysis),
                "SUCCESS",
                "repository-analysis",
                "md");
    }

    private String buildSummary(ProjectStructure analysis) {
        return "Repository analysis completed for " + analysis.projectName() + " with " + analysis.totalFiles()
                + " files and " + analysis.totalFolders() + " folders.";
    }

    private String buildMarkdownReport(ProjectStructure analysis) {
        StringBuilder report = new StringBuilder();
        report.append("# Repository Analysis Report\n\n");
        report.append("## Project Overview\n\n");
        report.append("- Project name: ").append(analysis.projectName()).append("\n");
        report.append("- Total folders: ").append(analysis.totalFolders()).append("\n");
        report.append("- Total files: ").append(analysis.totalFiles()).append("\n\n");

        appendFileTypeCounts(report, analysis.fileTypeCounts());
        appendDetectedTechnologies(report, analysis.detectedTechnologies());
        appendTopLevelFolders(report, analysis);
        appendLargestFiles(report, analysis);
        appendProjectSummary(report, analysis);

        return report.toString();
    }

    private void appendFileTypeCounts(StringBuilder report, Map<String, Long> fileTypeCounts) {
        report.append("## File Type Counts\n\n");
        if (fileTypeCounts.isEmpty()) {
            report.append("No files found.\n\n");
            return;
        }
        fileTypeCounts.forEach((extension, count) -> report.append("- ").append(extension).append(": ")
                .append(count).append("\n"));
        report.append("\n");
    }

    private void appendDetectedTechnologies(StringBuilder report, List<String> technologies) {
        report.append("## Detected Technologies\n\n");
        if (technologies.isEmpty()) {
            report.append("No common technologies detected.\n\n");
            return;
        }
        technologies.forEach(technology -> report.append("- ").append(technology).append("\n"));
        report.append("\n");
    }

    private void appendTopLevelFolders(StringBuilder report, ProjectStructure analysis) {
        report.append("## Top-Level Folders\n\n");
        if (analysis.topLevelFolders().isEmpty()) {
            report.append("No top-level folders found.\n\n");
            return;
        }
        analysis.topLevelFolders().forEach(folder -> report.append("- ").append(folder.getFileName()).append("\n"));
        report.append("\n");
    }

    private void appendLargestFiles(StringBuilder report, ProjectStructure analysis) {
        report.append("## Largest Files\n\n");
        if (analysis.largestFiles().isEmpty()) {
            report.append("No files found.\n\n");
            return;
        }
        analysis.largestFiles().forEach(file -> report.append("- ").append(file.relativePath()).append(" (")
                .append(formatFileSize(file.sizeInBytes())).append(")\n"));
        report.append("\n");
    }

    private void appendProjectSummary(StringBuilder report, ProjectStructure analysis) {
        report.append("## Simple Project Summary\n\n");
        report.append(buildSummary(analysis)).append(" ");
        report.append("Detected technologies: ").append(formatList(analysis.detectedTechnologies())).append(".\n");
    }

    private String formatFileSize(long sizeInBytes) {
        if (sizeInBytes < 1024) {
            return sizeInBytes + " B";
        }
        double sizeInKb = sizeInBytes / 1024.0;
        if (sizeInKb < 1024) {
            return String.format(Locale.ROOT, "%.1f KB", sizeInKb);
        }
        return String.format(Locale.ROOT, "%.1f MB", sizeInKb / 1024.0);
    }

    private String formatList(List<String> values) {
        if (values.isEmpty()) {
            return "none";
        }
        return String.join(", ", values);
    }
}