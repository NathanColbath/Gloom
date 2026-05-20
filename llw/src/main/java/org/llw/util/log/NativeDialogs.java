package org.llw.util.log;

import org.lwjgl.util.tinyfd.TinyFileDialogs;

/**
 * Native modal dialogs via tinyfiledialogs.
 */
public final class NativeDialogs {
    private NativeDialogs() {
    }

    /**
     * Shows a blocking error dialog when a display is available.
     *
     * @param title   dialog title
     * @param message body text
     */
    public static void showError(String title, String message) {
        try {
            TinyFileDialogs.tinyfd_messageBox(title, message, "ok", "error", 1);
        } catch (Throwable ex) {
            System.err.println(title + ": " + message);
            if (ex.getMessage() != null) {
                System.err.println("Native dialog unavailable: " + ex.getMessage());
            }
        }
    }
}
