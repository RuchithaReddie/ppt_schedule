package com.example.workflowhub.task;

import java.nio.file.Path;

public record AnalyzedProjectFile(Path relativePath, long sizeInBytes) {
}