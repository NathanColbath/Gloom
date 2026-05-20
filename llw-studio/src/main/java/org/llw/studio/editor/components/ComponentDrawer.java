package org.llw.studio.editor.components;

/**
 * Renders inspector UI for a single ECS component type and writes edits back to the instance.
 *
 * @param <T> component type handled by this drawer
 */
public interface ComponentDrawer<T> {

    /**
     * Draws property fields for {@code component}; call {@link InspectorContext#markDirty()} when values change.
     */
    void draw(T component, InspectorContext context);
}
