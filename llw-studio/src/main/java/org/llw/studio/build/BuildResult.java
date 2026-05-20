package org.llw.studio.build;

import java.nio.file.Path;
import java.util.List;

/**
 * Result of a player build attempt.
 */
public final class BuildResult {
    private final boolean success;
    private final Path outputDirectory;
    private final Path contentDirectory;
    private final Path executablePath;
    private final List<String> log;
    private final String errorMessage;

    private BuildResult(
            boolean success,
            Path outputDirectory,
            Path contentDirectory,
            Path executablePath,
            List<String> log,
            String errorMessage
    ) {
        this.success = success;
        this.outputDirectory = outputDirectory;
        this.contentDirectory = contentDirectory;
        this.executablePath = executablePath;
        this.log = List.copyOf(log);
        this.errorMessage = errorMessage;
    }

    public static BuildResult success(
            Path outputDirectory,
            Path contentDirectory,
            Path executablePath,
            List<String> log
    ) {
        return new BuildResult(true, outputDirectory, contentDirectory, executablePath, log, null);
    }

    public static BuildResult failure(Path outputDirectory, List<String> log, String errorMessage) {
        return new BuildResult(false, outputDirectory, null, null, log, errorMessage);
    }

    public boolean success() {
        return success;
    }

    public Path outputDirectory() {
        return outputDirectory;
    }

    public Path contentDirectory() {
        return contentDirectory;
    }

    public Path executablePath() {
        return executablePath;
    }

    public List<String> log() {
        return log;
    }

    public String errorMessage() {
        return errorMessage;
    }
}
