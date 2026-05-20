package org.llw.util.log;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * Appends formatted records to a single log file.
 */
public final class FileLogSink implements LogSink {
    private final BufferedWriter writer;
    private final Object lock = new Object();

    public FileLogSink(Path file) throws IOException {
        Files.createDirectories(file.getParent());
        writer = Files.newBufferedWriter(
                file,
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND
        );
    }

    @Override
    public void log(LogRecord record) {
        synchronized (lock) {
            try {
                writer.write(LogFormatter.format(record));
                writer.newLine();
            } catch (IOException e) {
                System.err.println("Failed to write log file: " + e.getMessage());
            }
        }
    }

    @Override
    public void flush() {
        synchronized (lock) {
            try {
                writer.flush();
            } catch (IOException e) {
                System.err.println("Failed to flush log file: " + e.getMessage());
            }
        }
    }

    @Override
    public void close() {
        synchronized (lock) {
            try {
                writer.close();
            } catch (IOException e) {
                System.err.println("Failed to close log file: " + e.getMessage());
            }
        }
    }
}
