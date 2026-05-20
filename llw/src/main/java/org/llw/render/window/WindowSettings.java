package org.llw.render.window;

/**
 * Mutable configuration for {@link Window} creation.
 * <p>
 * Default values: title {@code "Gloom"}, size {@code 1280×720}, resizable {@code true},
 * vsync {@code true}. Setter methods return {@code this} for fluent chaining.
 */
public final class WindowSettings {
    private String title = "Gloom";
    private int width = 1280;
    private int height = 720;
    private boolean resizable = true;
    private boolean vsync = true;

    /**
     * Returns the window title shown in the title bar.
     *
     * @return current title text
     */
    public String title() {
        return title;
    }

    /**
     * Sets the window title shown in the title bar.
     *
     * @param title title text; must not be {@code null}
     * @return this settings instance for chaining
     */
    public WindowSettings title(String title) {
        this.title = title;
        return this;
    }

    /**
     * Returns the initial window width in screen coordinates.
     *
     * @return width in pixels
     */
    public int width() {
        return width;
    }

    /**
     * Returns the initial window height in screen coordinates.
     *
     * @return height in pixels
     */
    public int height() {
        return height;
    }

    /**
     * Sets the initial window size in screen coordinates.
     *
     * @param width  initial width in pixels
     * @param height initial height in pixels
     * @return this settings instance for chaining
     */
    public WindowSettings size(int width, int height) {
        this.width = width;
        this.height = height;
        return this;
    }

    /**
     * Returns whether the window may be resized by the user.
     *
     * @return {@code true} if resizable
     */
    public boolean resizable() {
        return resizable;
    }

    /**
     * Sets whether the window may be resized by the user.
     *
     * @param resizable {@code true} to allow resizing
     * @return this settings instance for chaining
     */
    public WindowSettings resizable(boolean resizable) {
        this.resizable = resizable;
        return this;
    }

    /**
     * Returns whether vertical sync is enabled.
     *
     * @return {@code true} if vsync is enabled
     */
    public boolean vsync() {
        return vsync;
    }

    /**
     * Sets whether vertical sync is enabled.
     *
     * @param vsync {@code true} to enable vsync
     * @return this settings instance for chaining
     */
    public WindowSettings vsync(boolean vsync) {
        this.vsync = vsync;
        return this;
    }
}
