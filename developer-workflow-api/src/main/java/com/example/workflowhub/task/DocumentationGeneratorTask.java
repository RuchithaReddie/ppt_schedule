package com.example.workflowhub.task;

import com.example.workflowhub.dto.TaskRequest;
import com.example.workflowhub.model.TaskResult;
import com.example.workflowhub.model.TaskType;
import java.nio.file.Path;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class DocumentationGeneratorTask implements WorkflowTask {

    private static final List<String> IMPORTANT_FILE_NAMES = List.of(
            "README.md",
            "pom.xml",
            "package.json",
            "angular.json",
            "application.properties",
            ".gitignore");

    private static final List<String> SUGGESTED_README_SECTIONS = List.of(
            "Introduction",
            "Prerequisites",
            "Installation",
            "Build Instructions",
            "Running the Application",
            "Project Structure",
            "Technologies Used",
            "Contributing",
            "License");

    private final ProjectStructureAnalyzer projectStructureAnalyzer;

    public DocumentationGeneratorTask(ProjectStructureAnalyzer projectStructureAnalyzer) {
        this.projectStructureAnalyzer = projectStructureAnalyzer;
    }

    @Override
    public TaskType getTaskType() {
        return TaskType.DOCUMENTATION_GENERATOR;
    }

    @Override
    public String getTaskName() {
        return "Documentation Generator";
    }

    @Override
    public String getDescription() {
        return "Generates project documentation content from workflow inputs.";
    }

    @Override
    public TaskResult execute(TaskRequest request) {
        ProjectStructure projectStructure = projectStructureAnalyzer.analyze(request, getTaskName());

        return new TaskResult(
                "Project Documentation",
                buildSummary(projectStructure),
                buildMarkdownDocumentation(projectStructure),
                "SUCCESS",
                "documentation",
                "md");
    }

    private String buildSummary(ProjectStructure projectStructure) {
        return "Documentation generated for " + projectStructure.projectName() + " using detected project metadata.";
    }

    private String buildMarkdownDocumentation(ProjectStructure projectStructure) {
        StringBuilder documentation = new StringBuilder();
        appendProjectOverview(documentation, projectStructure);
        appendProjectStructure(documentation, projectStructure.topLevelFolders());
        appendTechnologies(documentation, projectStructure.detectedTechnologies());
        appendImportantFiles(documentation, findImportantFiles(projectStructure.files()));
        appendSuggestedReadmeSections(documentation);
        appendDocumentationSummary(documentation, projectStructure);
        return documentation.toString();
    }

    private void appendProjectOverview(StringBuilder documentation, ProjectStructure projectStructure) {
        documentation.append("# Project Overview\n\n");
        documentation.append("- Project name: ").append(projectStructure.projectName()).append("\n");
        documentation.append("- Root folder: ").append(projectStructure.projectPath()).append("\n");
        documentation.append("- Total files: ").append(projectStructure.totalFiles()).append("\n");
        documentation.append("- Total folders: ").append(projectStructure.totalFolders()).append("\n\n");
    }

    private void appendProjectStructure(StringBuilder documentation, List<Path> topLevelFolders) {
        documentation.append("# Project Structure\n\n");
        if (topLevelFolders.isEmpty()) {
            documentation.append("No first-level folders found.\n\n");
            return;
        }
        topLevelFolders.forEach(folder -> documentation.append(folder.getFileName()).append("/\n"));
        documentation.append("\n");
    }

    private void appendTechnologies(StringBuilder documentation, List<String> detectedTechnologies) {
        documentation.append("# Technologies Detected\n\n");
        if (detectedTechnologies.isEmpty()) {
            documentation.append("No common technologies detected.\n\n");
            return;
        }
        detectedTechnologies.forEach(technology -> documentation.append("- ").append(technology).append("\n"));
        documentation.append("\n");
    }

    private void appendImportantFiles(StringBuilder documentation, List<Path> importantFiles) {
        documentation.append("# Important Files\n\n");
        if (importantFiles.isEmpty()) {
            documentation.append("No common important files found.\n\n");
            return;
        }
        importantFiles.forEach(file -> documentation.append("- ").append(file).append("\n"));
        documentation.append("\n");
    }

    private void appendSuggestedReadmeSections(StringBuilder documentation) {
        documentation.append("# Suggested README Sections\n\n");
        SUGGESTED_README_SECTIONS.forEach(section -> documentation.append("- ").append(section).append("\n"));
        documentation.append("\n");
    }

    private void appendDocumentationSummary(StringBuilder documentation, ProjectStructure projectStructure) {
        documentation.append("# Documentation Summary\n\n");
        documentation.append(buildProjectDescription(projectStructure)).append("\n");
    }

    private List<Path> findImportantFiles(List<AnalyzedProjectFile> files) {
        return files.stream()
                .map(AnalyzedProjectFile::relativePath)
                .filter(this::isImportantFile)
                .sorted()
                .toList();
    }

    private boolean isImportantFile(Path filePath) {
        String fileName = filePath.getFileName().toString();
        return IMPORTANT_FILE_NAMES.stream().anyMatch(fileName::equalsIgnoreCase);
    }

    private String buildProjectDescription(ProjectStructure projectStructure) {
        List<String> technologies = projectStructure.detectedTechnologies();
        if (technologies.isEmpty()) {
            return "This project contains " + projectStructure.totalFiles() + " files across "
                    + projectStructure.totalFolders() + " folders. No common framework markers were detected.";
        }

        return "This project appears to use " + formatTechnologyList(technologies) + ". The repository contains "
                + projectStructure.totalFiles() + " files across " + projectStructure.totalFolders()
                + " folders, with first-level folders that describe the main project modules.";
    }

    private String formatTechnologyList(List<String> technologies) {
        if (technologies.size() == 1) {
            return technologies.get(0);
        }
        return String.join(", ", technologies.subList(0, technologies.size() - 1)) + " and "
                + technologies.get(technologies.size() - 1);
    }
}