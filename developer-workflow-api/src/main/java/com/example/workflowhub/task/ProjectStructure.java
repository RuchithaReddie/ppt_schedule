package com.example.workflowhub.task;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public record ProjectStructure(Path projectPath, long totalFolders, long totalFiles, Map<String, Long> fileTypeCounts,
        List<String> detectedTechnologies, List<Path> topLevelFolders, List<AnalyzedProjectFile> largestFiles,
        List<AnalyzedProjectFile> files, List<Path> folders, List<Path> excludedFolders) {

    public String projectName() {
        return projectPath.getFileName().toString();
    }
}