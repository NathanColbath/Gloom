package org.llw.studio.editor.render;

import org.llw.studio.scene.Scene;
import org.llw.studio.systems.TransformSystem;

/**
 * Ensures world-space transforms are computed once per editor viewport frame before overlay draws.
 */
public final class EditorWorldTransforms {
    // Reuse one system instance; call sites must not each allocate TransformSystem per draw/pick.
    private static final TransformSystem TRANSFORMS = new TransformSystem();

    private EditorWorldTransforms() {
    }

    /**
     * Updates {@link org.llw.studio.ecs.components.WorldTransformComponent} for all entities in the scene.
     *
     * @param scene scene being drawn or picked
     */
    public static void ensureUpdated(Scene scene) {
        if (scene == null) {
            return;
        }
        TRANSFORMS.onUpdate(scene.world(), 0f);
    }
}
