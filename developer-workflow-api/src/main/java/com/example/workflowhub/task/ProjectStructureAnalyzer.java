package com.example.workflowhub.task;

import com.example.workflowhub.dto.TaskRequest;
import com.example.workflowhub.exception.InvalidProjectFolderException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;
import org.springframework.stereotype.Component;

@Component
public class ProjectStructureAnalyzer {

    private static final int LARGEST_FILE_LIMIT = 5;

    public ProjectStructure analyze(TaskRequest request, String taskName) {
        Path projectPath = resolveProjectPath(request, taskName);
        List<Path> topLevelFolders = findTopLevelFolders(projectPath);
        List<AnalyzedProjectFile> files = findFiles(projectPath);
        List<Path> folders = findFolders(projectPath);
        Map<String, Long> fileTypeCounts = countFileTypes(files);
        List<AnalyzedProjectFile> largestFiles = findLargestFiles(files);
        List<String> detectedTechnologies = detectTechnologies(projectPath, files, fileTypeCounts);

        return new ProjectStructure(projectPath, folders.size(), files.size(), fileTypeCounts, detectedTechnologies,
            topLevelFolders, largestFiles, files, folders);
    }

    private Path resolveProjectPath(TaskRequest request, String taskName) {
        if (request.getProjectPath() == null || request.getProjectPath().isBlank()) {
            throw new InvalidProjectFolderException("Project folder path is required for " + taskName + ".");
        }

        Path projectPath = Path.of(request.getProjectPath()).toAbsolutePath().normalize();
        if (!Files.exists(projectPath) || !Files.isDirectory(projectPath)) {
            throw new InvalidProjectFolderException("Project folder does not exist: " + request.getProjectPath());
        }
        return projectPath;
    }

    private List<Path> findTopLevelFolders(Path projectPath) {
        try (Stream<Path> paths = Files.list(projectPath)) {
            return paths.filter(Files::isDirectory)
                    .sorted(Comparator.comparing(path -> path.getFileName().toString().toLowerCase(Locale.ROOT)))
                    .toList();
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to read project folders", exception);
        }
    }

    private List<AnalyzedProjectFile> findFiles(Path projectPath) {
        try (Stream<Path> paths = Files.walk(projectPath)) {
            return paths.filter(Files::isRegularFile)
                    .map(path -> toAnalyzedProjectFile(projectPath, path))
                    .toList();
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to read project files", exception);
        }
    }

    private List<Path> findFolders(Path projectPath) {
        try (Stream<Path> paths = Files.walk(projectPath)) {
            return paths.filter(path -> !path.equals(projectPath))
                    .filter(Files::isDirectory)
                    .map(projectPath::relativize)
                    .toList();
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to read project folders", exception);
        }
    }

    private AnalyzedProjectFile toAnalyzedProjectFile(Path projectPath, Path filePath) {
        try {
            return new AnalyzedProjectFile(projectPath.relativize(filePath), Files.size(filePath));
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to read file size: " + filePath, exception);
        }
    }

    private Map<String, Long> countFileTypes(List<AnalyzedProjectFile> files) {
        Map<String, Long> fileTypeCounts = new TreeMap<>();
        for (AnalyzedProjectFile file : files) {
            fileTypeCounts.merge(getFileExtension(file.relativePath()), 1L, Long::sum);
        }
        return fileTypeCounts;
    }

    private List<AnalyzedProjectFile> findLargestFiles(List<AnalyzedProjectFile> files) {
        return files.stream()
                .sorted(Comparator.comparingLong(AnalyzedProjectFile::sizeInBytes).reversed())
                .limit(LARGEST_FILE_LIMIT)
                .toList();
    }

    private List<String> detectTechnologies(Path projectPath, List<AnalyzedProjectFile> files,
            Map<String, Long> fileTypeCounts) {
        List<String> technologies = new ArrayList<>();
        String pomContent = readFileIfExists(projectPath.resolve("pom.xml"));
        String packageJsonContent = readFileIfExists(projectPath.resolve("package.json"));

        addIf(technologies, !pomContent.isBlank(), "Maven");
        addIf(technologies, Files.exists(projectPath.resolve("package.json")), "Node.js");
        addIf(technologies, Files.exists(projectPath.resolve("angular.json")) || packageJsonContent.contains("@angular/core"),
                "Angular");
        addIf(technologies, Files.exists(projectPath.resolve("build.gradle")), "Gradle");
        addIf(technologies, fileTypeCounts.containsKey(".java"), "Java");
        addIf(technologies, fileTypeCounts.containsKey(".ts"), "TypeScript");
        addIf(technologies, fileTypeCounts.containsKey(".html"), "HTML");
        addIf(technologies, fileTypeCounts.containsKey(".css"), "CSS");
        addIf(technologies, containsFileNamed(files, "application.properties") || pomContent.contains("spring-boot"),
                "Spring Boot");
        addIf(technologies, containsFileNamed(files, "README.md"), "Documentation");

        return technologies;
    }

    private void addIf(List<String> values, boolean condition, String value) {
        if (condition) {
            values.add(value);
        }
    }

    private boolean containsFileNamed(List<AnalyzedProjectFile> files, String fileName) {
        return files.stream().anyMatch(file -> file.relativePath().getFileName().toString().equalsIgnoreCase(fileName));
    }

    private String readFileIfExists(Path filePath) {
        if (!Files.exists(filePath)) {
            return "";
        }

        try {
            return Files.readString(filePath).toLowerCase(Locale.ROOT);
        } catch (IOException exception) {
            return "";
        }
    }

    private String getFileExtension(Path filePath) {
        String fileName = filePath.getFileName().toString();
        int extensionStart = fileName.lastIndexOf('.');
        if (extensionStart <= 0 || extensionStart == fileName.length() - 1) {
            return "[no extension]";
        }
        return fileName.substring(extensionStart).toLowerCase(Locale.ROOT);
    }
}

