package org.llw.studio.animation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Loads and saves {@link AnimationClip} JSON assets ({@code .anim.json}).
 */
public final class AnimationClipSerializer {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private AnimationClipSerializer() {
    }

    public static AnimationClip load(Path path) throws IOException {
        JsonNode root = MAPPER.readTree(path.toFile());
        AnimationClip clip = new AnimationClip();
        clip.version = root.path("version").asInt(1);
        clip.length = (float) root.path("length").asDouble(1.0);
        clip.frameRate = (float) root.path("frameRate").asDouble(12.0);
        clip.loop = root.path("loop").asBoolean(true);
        JsonNode tracks = root.path("tracks");
        if (tracks.isArray()) {
            for (JsonNode node : tracks) {
                clip.tracks.add(readTrack(node));
            }
        }
        return clip;
    }

    public static void save(Path path, AnimationClip clip) throws IOException {
        Files.createDirectories(path.getParent());
        ObjectNode root = MAPPER.createObjectNode();
        root.put("version", clip.version);
        root.put("length", clip.length);
        root.put("frameRate", clip.frameRate);
        root.put("loop", clip.loop);
        ArrayNode tracks = root.putArray("tracks");
        for (AnimationTrack track : clip.tracks) {
            tracks.add(writeTrack(track));
        }
        Files.writeString(path, MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(root));
    }

    private static AnimationTrack readTrack(JsonNode node) {
        AnimationTrack track = new AnimationTrack();
        track.path = node.path("path").asText("");
        String typeName = node.path("type").asText("float");
        track.type = "sprite".equals(typeName) ? AnimationTrackType.SPRITE : AnimationTrackType.FLOAT;
        JsonNode keys = node.path("keyframes");
        if (!keys.isArray()) {
            return track;
        }
        if (track.type == AnimationTrackType.SPRITE) {
            for (JsonNode key : keys) {
                track.spriteKeyframes.add(new SpriteKeyframe(
                        (float) key.path("time").asDouble(),
                        key.path("spriteGuid").asText("")
                ));
            }
        } else {
            for (JsonNode key : keys) {
                track.floatKeyframes.add(new FloatKeyframe(
                        (float) key.path("time").asDouble(),
                        (float) key.path("value").asDouble()
                ));
            }
        }
        track.sortKeyframes();
        return track;
    }

    private static ObjectNode writeTrack(AnimationTrack track) {
        ObjectNode node = MAPPER.createObjectNode();
        node.put("path", track.path);
        node.put("type", track.type == AnimationTrackType.SPRITE ? "sprite" : "float");
        ArrayNode keys = node.putArray("keyframes");
        if (track.type == AnimationTrackType.SPRITE) {
            for (SpriteKeyframe key : track.spriteKeyframes) {
                ObjectNode keyNode = keys.addObject();
                keyNode.put("time", key.time());
                keyNode.put("spriteGuid", key.spriteGuid());
            }
        } else {
            for (FloatKeyframe key : track.floatKeyframes) {
                ObjectNode keyNode = keys.addObject();
                keyNode.put("time", key.time());
                keyNode.put("value", key.value());
            }
        }
        return node;
    }
}
