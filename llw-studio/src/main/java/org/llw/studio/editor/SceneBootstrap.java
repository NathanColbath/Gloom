package org.llw.studio.editor;

import org.llw.studio.assets.AssetDatabase;
import org.llw.studio.ecs.components.SpriteRendererComponent;
import org.llw.studio.scene.Scene;
import org.llw.studio.scripting.ScriptAttachment;
import org.llw.studio.scripting.ScriptComponent;
import org.llw.studio.serialization.SceneSerializer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Bootstraps default scenes for new projects (editor-only).
 */
public final class SceneBootstrap {
    private SceneBootstrap() {
    }

    /**
     * Loads an existing scene file or creates a starter scene with player and camera assets when missing.
     */
    public static void loadOrBootstrap(StudioContext context, AssetDatabase assets, Path scenePath) throws IOException {
        if (Files.exists(scenePath)) {
            context.setEditScene(SceneSerializer.load(scenePath));
            return;
        }
        Files.createDirectories(scenePath.getParent());
        Scene scene = context.editScene();
        SceneSerializer.ensureMainCamera(scene);
        var player = scene.createGameObject("Player");
        var sprite = new SpriteRendererComponent();
        Path playerPath = context.projectRoot().resolve("Assets/player.png");
        if (Files.exists(playerPath)) {
            assets.refresh();
            var asset = assets.getByPath(playerPath);
            if (asset != null) {
                sprite.textureGuid = asset.guid();
            }
        }
        player.addComponent(SpriteRendererComponent.class, sprite);
        Path controllerPath = context.projectRoot().resolve("Assets/Scripts/PlayerController.ts");
        if (!Files.exists(controllerPath)) {
            controllerPath = context.projectRoot().resolve("Assets/Scripts/PlayerController.js");
        }
        if (Files.exists(controllerPath)) {
            assets.refresh();
            var scriptAsset = assets.getByPath(controllerPath);
            if (scriptAsset != null) {
                ScriptComponent script = new ScriptComponent();
                ScriptAttachment attachment = script.addAttachment();
                attachment.scriptGuid = scriptAsset.guid();
                player.addComponent(ScriptComponent.class, script);
            }
        }
        SceneSerializer.save(scene, scenePath);
    }
}
