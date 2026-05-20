package org.llw.player;

import org.llw.resources.ResourceManager;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.llw.studio.assets.AssetDatabase;
import org.llw.studio.assets.PackBootstrap;
import org.llw.studio.serialization.SceneSerializer;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Loads published player content from a {@code content/} directory.
 */
public final class PublishedContent {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final Path contentDir;
    private final GameManifest manifest;
    private final AssetDatabase assets;
    private final ResourceManager resources;

    private PublishedContent(Path contentDir, GameManifest manifest, AssetDatabase assets, ResourceManager resources) {
        this.contentDir = contentDir;
        this.manifest = manifest;
        this.assets = assets;
        this.resources = resources;
    }

    public static PublishedContent load(Path contentDir, ResourceManager resources) throws IOException {
        Path manifestPath = contentDir.resolve("game.manifest.json");
        GameManifest manifest = GameManifest.read(manifestPath);
        AssetDatabase assets = PackBootstrap.bootstrap(contentDir, resources);
        return new PublishedContent(contentDir, manifest, assets, resources);
    }

    public Path contentDir() {
        return contentDir;
    }

    public GameManifest manifest() {
        return manifest;
    }

    public AssetDatabase assets() {
        return assets;
    }

    public ResourceManager resources() {
        return resources;
    }

    public org.llw.studio.scene.Scene loadStartupScene() throws IOException {
        String sceneGuid = manifest.startupSceneGuid();
        if (!resources.isRegistered(sceneGuid)) {
            throw new IOException("Startup scene not found in packs: " + sceneGuid);
        }
        try (var ref = resources.acquireRaw(sceneGuid)) {
            return SceneSerializer.loadJson(new String(ref.get(), StandardCharsets.UTF_8));
        }
    }
}
