package org.llw.studio.editor.launcher;

import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Blocking Swing folder picker marshalled onto the EDT from the GLFW main thread.
 */
public final class NativeFolderChooser {
    private volatile boolean pending;
    private String dialogTitle = "Select Folder";
    private Path initialDirectory;
    private final AtomicReference<Optional<Path>> result = new AtomicReference<>();

    /**
     * Queues a folder dialog for the next {@link #poll()}.
     *
     * @param title      dialog title
     * @param initialDir starting directory, or null
     */
    public void request(String title, Path initialDir) {
        dialogTitle = title == null ? "Select Folder" : title;
        initialDirectory = initialDir;
        pending = true;
    }

    /**
     * Shows the dialog if a request is pending (blocks until the user dismisses it).
     *
     * @return true if a dialog was shown this call
     * <p>Implementation note: Call from the main loop; not re-entrant while a dialog is open.
     */
    public boolean poll() {
        if (!pending) {
            return false;
        }
        pending = false;
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Optional<Path>> chosen = new AtomicReference<>(Optional.empty());
        SwingUtilities.invokeLater(() -> {
            try {
                JFileChooser chooser = new JFileChooser();
                chooser.setDialogTitle(dialogTitle);
                chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                if (initialDirectory != null && java.nio.file.Files.isDirectory(initialDirectory)) {
                    chooser.setCurrentDirectory(initialDirectory.toFile());
                }
                if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                    chosen.set(Optional.of(chooser.getSelectedFile().toPath().toAbsolutePath().normalize()));
                }
            } finally {
                latch.countDown();
            }
        });
        try {
            latch.await();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        result.set(chosen.get());
        return true;
    }

    /**
     * @return chosen folder and clears the stored result
     */
    public Optional<Path> takeResult() {
        Optional<Path> value = result.getAndSet(null);
        return value == null ? Optional.empty() : value;
    }
}
