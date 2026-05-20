package org.llw.studio.build;

import com.fasterxml.jackson.databind.JsonNode;
import org.llw.studio.ecs.components.Animation2DComponent;
import org.llw.studio.ecs.components.AudioSourceComponent;
import org.llw.studio.ecs.components.SpriteRendererComponent;
import org.llw.studio.ecs.components.TilemapCell;
import org.llw.studio.ecs.components.TilemapComponent;
import org.llw.studio.ecs.components.TilemapLayer;
import org.llw.studio.scene.Scene;
import org.llw.studio.scripting.ScriptAttachment;
import org.llw.studio.scripting.ScriptComponent;
import org.llw.studio.scripting.js.ScriptFieldApplicator;
import org.llw.studio.scripting.js.ScriptSceneIndex;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Collects asset GUID references from a loaded {@link Scene}.
 */
public final class SceneGuidCollector {
    private SceneGuidCollector() {
    }

    /**
     * @param scene scene graph to scan
     * @return unique non-blank asset GUIDs referenced by components and scripts
     */
    public static Set<String> collectFromScene(Scene scene) {
        if (scene == null) {
            return Set.of();
        }
        Set<String> guids = new LinkedHashSet<>();
        guids.addAll(ScriptSceneIndex.collectGuids(scene));

        var sprites = scene.world().store(SpriteRendererComponent.class);
        for (int i = 0; i < sprites.size(); i++) {
            SpriteRendererComponent sprite = sprites.componentAt(i);
            addGuid(guids, sprite.spriteGuid);
            addGuid(guids, sprite.textureGuid);
            addGuid(guids, sprite.shaderGraphGuid);
        }

        var animations = scene.world().store(Animation2DComponent.class);
        for (int i = 0; i < animations.size(); i++) {
            Animation2DComponent anim = animations.componentAt(i);
            addGuid(guids, anim.animationGuid);
            addGuid(guids, anim.clipGuid);
        }

        var audioSources = scene.world().store(AudioSourceComponent.class);
        for (int i = 0; i < audioSources.size(); i++) {
            addGuid(guids, audioSources.componentAt(i).clipGuid);
        }

        var tilemaps = scene.world().store(TilemapComponent.class);
        for (int i = 0; i < tilemaps.size(); i++) {
            TilemapComponent tilemap = tilemaps.componentAt(i);
            addGuid(guids, tilemap.tilesetTextureGuid);
            if (tilemap.layers != null) {
                for (TilemapLayer layer : tilemap.layers) {
                    if (layer == null || layer.cells == null) {
                        continue;
                    }
                    for (TilemapCell cell : layer.cells.values()) {
                        if (cell != null) {
                            addGuid(guids, cell.spriteGuid);
                        }
                    }
                }
            }
        }

        var scripts = scene.world().store(ScriptComponent.class);
        for (int i = 0; i < scripts.size(); i++) {
            ScriptComponent component = scripts.componentAt(i);
            if (component == null) {
                continue;
            }
            for (ScriptAttachment attachment : component.attachments) {
                collectScriptFieldGuids(guids, attachment);
            }
        }
        return guids;
    }

    /**
     * @param objects prefab object nodes from {@link org.llw.studio.serialization.PrefabSerializer}
     * @return GUID references found in serialized prefab JSON
     */
    public static Set<String> collectFromPrefabObjects(Iterable<JsonNode> objects) {
        Set<String> guids = new LinkedHashSet<>();
        for (JsonNode objectNode : objects) {
            collectPrefabObjectGuids(guids, objectNode);
        }
        return guids;
    }

    private static void collectPrefabObjectGuids(Set<String> guids, JsonNode objectNode) {
        JsonNode spriteRenderer = objectNode.path("spriteRenderer");
        if (spriteRenderer.isObject()) {
            addGuid(guids, spriteRenderer.path("spriteGuid").asText(""));
            addGuid(guids, spriteRenderer.path("textureGuid").asText(""));
            addGuid(guids, spriteRenderer.path("shaderGraphGuid").asText(""));
        }
        JsonNode legacySprite = objectNode.path("sprite");
        if (legacySprite.isObject()) {
            addGuid(guids, legacySprite.path("spriteGuid").asText(""));
            addGuid(guids, legacySprite.path("textureGuid").asText(""));
            addGuid(guids, legacySprite.path("shaderGraphGuid").asText(""));
        }

        JsonNode animation = objectNode.has("animation2D")
                ? objectNode.path("animation2D")
                : objectNode.path("animation2d");
        if (animation.isObject()) {
            addGuid(guids, animation.path("animationGuid").asText(""));
            addGuid(guids, animation.path("clipGuid").asText(""));
        }

        JsonNode audio = objectNode.has("audioSource")
                ? objectNode.path("audioSource")
                : objectNode.path("audio");
        if (audio.isObject()) {
            addGuid(guids, audio.path("clipGuid").asText(""));
        }

        JsonNode tilemap = objectNode.path("tilemap");
        if (tilemap.isObject()) {
            addGuid(guids, tilemap.path("tilesetTextureGuid").asText(""));
            JsonNode layers = tilemap.path("layers");
            if (layers.isArray()) {
                for (JsonNode layerNode : layers) {
                    JsonNode cells = layerNode.path("cells");
                    if (cells.isObject()) {
                        cells.fields().forEachRemaining(entry -> {
                            JsonNode cellNode = entry.getValue();
                            addGuid(guids, cellNode.path("spriteGuid").asText(""));
                        });
                    }
                }
            }
        }

        collectPrefabScriptGuids(guids, objectNode);
    }

    private static void collectPrefabScriptGuids(Set<String> guids, JsonNode objectNode) {
        JsonNode scripts = objectNode.path("scripts");
        if (scripts.isArray()) {
            for (JsonNode attachmentNode : scripts) {
                collectScriptAttachmentGuids(guids, attachmentNode);
            }
            return;
        }
        JsonNode script = objectNode.path("script");
        if (script.isObject()) {
            JsonNode attachments = script.path("attachments");
            if (attachments.isArray()) {
                for (JsonNode attachmentNode : attachments) {
                    collectScriptAttachmentGuids(guids, attachmentNode);
                }
            } else {
                collectScriptAttachmentGuids(guids, script);
            }
        }
    }

    private static void collectScriptAttachmentGuids(Set<String> guids, JsonNode attachmentNode) {
        addGuid(guids, attachmentNode.path("scriptGuid").asText(""));
        JsonNode fields = attachmentNode.path("fields");
        if (fields.isObject()) {
            fields.fields().forEachRemaining(entry -> {
                JsonNode value = entry.getValue();
                addGuid(guids, ScriptFieldApplicator.entityFieldPrefabGuid(value));
            });
        }
    }

    private static void collectScriptFieldGuids(Set<String> guids, ScriptAttachment attachment) {
        if (attachment == null || attachment.fields.isEmpty()) {
            return;
        }
        for (JsonNode value : attachment.fields.values()) {
            addGuid(guids, ScriptFieldApplicator.entityFieldPrefabGuid(value));
        }
    }

    private static void addGuid(Set<String> guids, String guid) {
        if (guid != null && !guid.isBlank()) {
            guids.add(guid);
        }
    }
}
