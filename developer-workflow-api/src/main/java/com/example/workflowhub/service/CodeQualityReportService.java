package com.example.workflowhub.service;

import com.example.workflowhub.dto.TaskRequest;
import com.example.workflowhub.model.TaskResult;
import com.example.workflowhub.model.TaskType;
import com.example.workflowhub.task.AnalyzedProjectFile;
import com.example.workflowhub.task.ProjectStructure;
import com.example.workflowhub.task.ProjectStructureAnalyzer;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class CodeQualityReportService implements TaskService {

    private static final long LARGE_FILE_THRESHOLD_BYTES = 100 * 1024;
    private static final int LONG_FILE_NAME_LIMIT = 40;
    private static final Set<String> TEXT_SOURCE_EXTENSIONS = Set.of(
            ".java",
            ".ts",
            ".html",
            ".css",
            ".md",
            ".properties",
            ".json",
            ".xml");

    private final ProjectStructureAnalyzer projectStructureAnalyzer;

    public CodeQualityReportService(ProjectStructureAnalyzer projectStructureAnalyzer) {
        this.projectStructureAnalyzer = projectStructureAnalyzer;
    }

    @Override
    public TaskType getTaskType() {
        return TaskType.CODE_QUALITY_REPORT;
    }

    @Override
    public String getTaskName() {
        return "Code Quality Report";
    }

    @Override
    public String getDescription() {
        return "Creates a code quality report with improvement suggestions.";
    }

    @Override
    public TaskResult execute(TaskRequest request) {
        ProjectStructure projectStructure = projectStructureAnalyzer.analyze(request, getTaskName());
        QualityReview qualityReview = reviewProject(projectStructure);

        return new TaskResult(
                "Code Quality Report",
                buildSummary(projectStructure, qualityReview),
                buildMarkdownReport(projectStructure, qualityReview),
                "SUCCESS",
                "code-quality",
                "md");
    }

    private QualityReview reviewProject(ProjectStructure projectStructure) {
        List<String> namingIssues = findNamingIssues(projectStructure);
        TodoFixmeReview todoFixmeReview = reviewTodoFixmeComments(projectStructure);
        List<AnalyzedProjectFile> largeFiles = findLargeFiles(projectStructure.files());
        return new QualityReview(namingIssues, todoFixmeReview, largeFiles);
    }

    private List<String> findNamingIssues(ProjectStructure projectStructure) {
        List<String> issues = new ArrayList<>();
        addFileNameIssues(projectStructure.files(), issues);
        addFolderNameIssues(projectStructure.folders(), issues);
        addCaseCollisionIssues(projectStructure.files(), projectStructure.folders(), issues);
        return issues;
    }

    private void addFileNameIssues(List<AnalyzedProjectFile> files, List<String> issues) {
        for (AnalyzedProjectFile file : files) {
            String fileName = file.relativePath().getFileName().toString();
            addIf(issues, fileName.contains(" "), "File name contains spaces: " + file.relativePath());
            addIf(issues, fileName.length() > LONG_FILE_NAME_LIMIT,
                    "File name is longer than " + LONG_FILE_NAME_LIMIT + " characters: " + file.relativePath());
            addIf(issues, hasMixedCase(fileName),
                    "File name uses mixed uppercase/lowercase style: " + file.relativePath());
        }
    }

    private void addFolderNameIssues(List<Path> folders, List<String> issues) {
        for (Path folder : folders) {
            String folderName = folder.getFileName().toString();
            addIf(issues, folderName.contains(" "), "Folder name contains spaces: " + folder);
            addIf(issues, hasMixedSeparators(folderName), "Folder name mixes separators: " + folder);
            addIf(issues, hasMixedCase(folderName), "Folder name uses mixed uppercase/lowercase style: " + folder);
        }
    }

    private void addIf(List<String> issues, boolean condition, String message) {
        if (condition) {
            issues.add(message);
        }
    }

    private void addCaseCollisionIssues(List<AnalyzedProjectFile> files, List<Path> folders, List<String> issues) {
        addDuplicateCaseIssues(files.stream().map(file -> file.relativePath().getFileName().toString()).toList(),
                "File", issues);
        addDuplicateCaseIssues(folders.stream().map(folder -> folder.getFileName().toString()).toList(), "Folder", issues);
    }

    private void addDuplicateCaseIssues(List<String> names, String label, List<String> issues) {
        Map<String, Set<String>> namesByLowercase = new HashMap<>();
        for (String name : names) {
            namesByLowercase.computeIfAbsent(name.toLowerCase(Locale.ROOT), key -> new HashSet<>()).add(name);
        }

        namesByLowercase.values().stream()
                .filter(variants -> variants.size() > 1)
                .forEach(variants -> issues.add(label + " names differ only by case: " + String.join(", ", variants)));
    }

    private TodoFixmeReview reviewTodoFixmeComments(ProjectStructure projectStructure) {
        int todoCount = 0;
        int fixmeCount = 0;
        List<String> filesWithMatches = new ArrayList<>();

        for (AnalyzedProjectFile file : projectStructure.files()) {
            if (!isTextSourceFile(file.relativePath())) {
                continue;
            }

            String content = readFile(projectStructure.projectPath().resolve(file.relativePath()));
            int fileTodoCount = countOccurrences(content, "TODO");
            int fileFixmeCount = countOccurrences(content, "FIXME");
            todoCount += fileTodoCount;
            fixmeCount += fileFixmeCount;
            addTodoFixmeMatch(filesWithMatches, file, fileTodoCount, fileFixmeCount);
        }

        return new TodoFixmeReview(todoCount, fixmeCount, filesWithMatches);
    }

    private void addTodoFixmeMatch(List<String> filesWithMatches, AnalyzedProjectFile file, int todoCount,
            int fixmeCount) {
        if (todoCount == 0 && fixmeCount == 0) {
            return;
        }
        filesWithMatches.add(file.relativePath() + " (TODO: " + todoCount + ", FIXME: " + fixmeCount + ")");
    }

    private List<AnalyzedProjectFile> findLargeFiles(List<AnalyzedProjectFile> files) {
        return files.stream()
                .filter(file -> file.sizeInBytes() > LARGE_FILE_THRESHOLD_BYTES)
                .sorted(Comparator.comparingLong(AnalyzedProjectFile::sizeInBytes).reversed())
                .toList();
    }

    private String buildMarkdownReport(ProjectStructure projectStructure, QualityReview qualityReview) {
        StringBuilder report = new StringBuilder();
        appendProjectSummary(report, projectStructure);
        appendNamingReview(report, qualityReview.namingIssues());
        appendTodoFixmeReview(report, qualityReview.todoFixmeReview());
        appendLargeFileReview(report, qualityReview.largeFiles());
        appendProjectHealth(report, qualityReview);
        appendImprovementSuggestions(report, qualityReview);
        return report.toString();
    }

    private void appendProjectSummary(StringBuilder report, ProjectStructure projectStructure) {
        report.append("# Project Summary\n\n");
        report.append("- Project name: ").append(projectStructure.projectName()).append("\n");
        report.append("- Total source files: ").append(countSourceFiles(projectStructure.files())).append("\n");
        report.append("- Technologies detected: ").append(formatList(projectStructure.detectedTechnologies())).append("\n\n");
    }

    private void appendNamingReview(StringBuilder report, List<String> namingIssues) {
        report.append("# Naming Review\n\n");
        if (namingIssues.isEmpty()) {
            report.append("Naming conventions are mostly consistent.\n\n");
            return;
        }
        namingIssues.forEach(issue -> report.append("- ").append(issue).append("\n"));
        report.append("\n");
    }

    private void appendTodoFixmeReview(StringBuilder report, TodoFixmeReview review) {
        report.append("# TODO / FIXME Review\n\n");
        report.append("- TODO total: ").append(review.todoCount()).append("\n");
        report.append("- FIXME total: ").append(review.fixmeCount()).append("\n\n");

        if (review.filesWithMatches().isEmpty()) {
            report.append("No TODO or FIXME comments found in supported text source files.\n\n");
            return;
        }
        report.append("Files with TODO/FIXME comments:\n\n");
        review.filesWithMatches().forEach(file -> report.append("- ").append(file).append("\n"));
        report.append("\n");
    }

    private void appendLargeFileReview(StringBuilder report, List<AnalyzedProjectFile> largeFiles) {
        report.append("# Large File Review\n\n");
        if (largeFiles.isEmpty()) {
            report.append("No files larger than 100 KB were found.\n\n");
            return;
        }
        largeFiles.forEach(file -> report.append("- ").append(file.relativePath().getFileName()).append(" - ")
                .append(formatFileSize(file.sizeInBytes())).append(" - ").append(file.relativePath()).append("\n"));
        report.append("\n");
    }

    private void appendProjectHealth(StringBuilder report, QualityReview review) {
        report.append("# Project Health\n\n");
        report.append(review.namingIssues().isEmpty() ? "Naming conventions are mostly consistent.\n"
                : review.namingIssues().size() + " naming issue(s) found.\n");
        report.append(review.todoFixmeReview().todoCount()).append(" TODO comments found.\n");
        report.append(review.todoFixmeReview().fixmeCount() == 0 ? "No FIXME comments detected.\n"
                : review.todoFixmeReview().fixmeCount() + " FIXME comments found.\n");
        report.append(review.largeFiles().size()).append(" files are larger than 100 KB.\n");
        report.append(buildOverallHealthMessage(review)).append("\n\n");
    }

    private void appendImprovementSuggestions(StringBuilder report, QualityReview review) {
        report.append("# Improvement Suggestions\n\n");
        buildSuggestions(review).forEach(suggestion -> report.append("- ").append(suggestion).append("\n"));
    }

    private List<String> buildSuggestions(QualityReview review) {
        List<String> suggestions = new ArrayList<>();
        addIf(suggestions, review.todoFixmeReview().todoCount() > 0, "Remove completed TODO comments.");
        addIf(suggestions, review.todoFixmeReview().fixmeCount() > 0,
                "Review FIXME comments and convert them into tracked tasks.");
        addIf(suggestions, !review.largeFiles().isEmpty(), "Split very large files into smaller focused files.");
        addIf(suggestions, !review.namingIssues().isEmpty(), "Keep naming conventions consistent.");
        suggestions.add("Remove unused files.");
        suggestions.add("Maintain documentation.");
        return suggestions;
    }

    private String buildSummary(ProjectStructure projectStructure, QualityReview review) {
        return "Code quality report generated for " + projectStructure.projectName() + " with "
                + review.todoFixmeReview().todoCount() + " TODO comments, " + review.todoFixmeReview().fixmeCount()
                + " FIXME comments, and " + review.largeFiles().size() + " large files.";
    }

    private String buildOverallHealthMessage(QualityReview review) {
        if (review.namingIssues().isEmpty() && review.todoFixmeReview().fixmeCount() == 0 && review.largeFiles().isEmpty()) {
            return "Overall codebase appears well organized.";
        }
        return "Overall codebase is usable, with a few cleanup opportunities.";
    }

    private long countSourceFiles(List<AnalyzedProjectFile> files) {
        return files.stream().filter(file -> isTextSourceFile(file.relativePath())).count();
    }

    private boolean isTextSourceFile(Path filePath) {
        return TEXT_SOURCE_EXTENSIONS.contains(getFileExtension(filePath));
    }

    private String readFile(Path filePath) {
        try {
            return Files.readString(filePath);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to read source file: " + filePath, exception);
        }
    }

    private int countOccurrences(String content, String token) {
        String uppercaseContent = content.toUpperCase(Locale.ROOT);
        int count = 0;
        int index = uppercaseContent.indexOf(token);
        while (index >= 0) {
            count++;
            index = uppercaseContent.indexOf(token, index + token.length());
        }
        return count;
    }

    private boolean hasMixedCase(String value) {
        return !value.equals(value.toLowerCase(Locale.ROOT)) && !value.equals(value.toUpperCase(Locale.ROOT));
    }

    private boolean hasMixedSeparators(String value) {
        return value.contains("-") && value.contains("_");
    }

    private String getFileExtension(Path filePath) {
        String fileName = filePath.getFileName().toString();
        int extensionStart = fileName.lastIndexOf('.');
        if (extensionStart <= 0 || extensionStart == fileName.length() - 1) {
            return "[no extension]";
        }
        return fileName.substring(extensionStart).toLowerCase(Locale.ROOT);
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

    private record QualityReview(List<String> namingIssues, TodoFixmeReview todoFixmeReview,
            List<AnalyzedProjectFile> largeFiles) {
    }

    private record TodoFixmeReview(int todoCount, int fixmeCount, List<String> filesWithMatches) {
    }
}