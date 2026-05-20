package org.llw.studio.assets;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.llw.render.graphics.TextureFilter;
import org.llw.render.graphics.TextureWrap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Reads and writes {@code importer.texture} spritesheet settings in asset meta.
 */
public final class TextureSpriteData {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private TextureSpriteData() {
    }

    public static String spriteMode(ObjectNode importer) {
        ObjectNode texture = textureNode(importer, false);
        if (texture == null) {
            return "single";
        }
        return texture.path("spriteMode").asText("single");
    }

    public static void setSpriteMode(ObjectNode importer, String mode) {
        ObjectNode texture = textureNode(importer, true);
        texture.put("spriteMode", mode == null || mode.isBlank() ? "single" : mode);
    }

    public static SpriteSliceSettings readSliceSettings(ObjectNode importer) {
        SpriteSliceSettings settings = new SpriteSliceSettings();
        ObjectNode texture = textureNode(importer, false);
        if (texture == null || !texture.has("slice")) {
            return settings;
        }
        JsonNode slice = texture.path("slice");
        settings.cellWidth = slice.path("cellWidth").asInt(32);
        settings.cellHeight = slice.path("cellHeight").asInt(32);
        settings.offsetX = slice.path("offsetX").asInt(0);
        settings.offsetY = slice.path("offsetY").asInt(0);
        settings.paddingX = slice.path("paddingX").asInt(0);
        settings.paddingY = slice.path("paddingY").asInt(0);
        settings.columnCount = slice.path("columnCount").asInt(0);
        settings.rowCount = slice.path("rowCount").asInt(0);
        settings.indexFromBottom = slice.path("indexFromBottom").asBoolean(false);
        return settings;
    }

    public static TextureImportSettings readImportSettings(ObjectNode importer) {
        TextureImportSettings settings = new TextureImportSettings();
        ObjectNode texture = textureNode(importer, false);
        if (texture == null) {
            return settings;
        }
        settings.filter = parseFilter(texture.path("filter").asText("linear"));
        settings.wrap = parseWrap(texture.path("wrap").asText("clamp"));
        settings.artFacing = SpriteArtFacing.fromId(texture.path("artFacing").asText("right"));
        return settings;
    }

    public static void writeImportSettings(ObjectNode importer, TextureImportSettings settings) {
        ObjectNode texture = textureNode(importer, true);
        texture.put("filter", filterId(settings.filter));
        texture.put("wrap", wrapId(settings.wrap));
        texture.put("artFacing", settings.artFacing == null ? SpriteArtFacing.RIGHT.id() : settings.artFacing.id());
    }

    public static void ensureImportDefaults(ObjectNode importer) {
        ObjectNode texture = textureNode(importer, true);
        if (!texture.has("filter")) {
            texture.put("filter", "linear");
        }
        if (!texture.has("wrap")) {
            texture.put("wrap", "clamp");
        }
        if (!texture.has("artFacing")) {
            texture.put("artFacing", SpriteArtFacing.RIGHT.id());
        }
    }

    private static TextureFilter parseFilter(String raw) {
        if (raw == null) {
            return TextureFilter.LINEAR;
        }
        return switch (raw.toLowerCase(Locale.ROOT)) {
            case "point", "nearest", "pixel" -> TextureFilter.POINT;
            default -> TextureFilter.LINEAR;
        };
    }

    private static TextureWrap parseWrap(String raw) {
        if (raw == null) {
            return TextureWrap.CLAMP;
        }
        return switch (raw.toLowerCase(Locale.ROOT)) {
            case "repeat" -> TextureWrap.REPEAT;
            default -> TextureWrap.CLAMP;
        };
    }

    private static String filterId(TextureFilter filter) {
        return filter == TextureFilter.POINT ? "point" : "linear";
    }

    private static String wrapId(TextureWrap wrap) {
        return wrap == TextureWrap.REPEAT ? "repeat" : "clamp";
    }

    public static void writeSliceSettings(ObjectNode importer, SpriteSliceSettings settings) {
        ObjectNode texture = textureNode(importer, true);
        ObjectNode slice = MAPPER.createObjectNode();
        slice.put("cellWidth", settings.cellWidth);
        slice.put("cellHeight", settings.cellHeight);
        slice.put("offsetX", settings.offsetX);
        slice.put("offsetY", settings.offsetY);
        slice.put("paddingX", settings.paddingX);
        slice.put("paddingY", settings.paddingY);
        slice.put("columnCount", settings.columnCount);
        slice.put("rowCount", settings.rowCount);
        slice.put("indexFromBottom", settings.indexFromBottom);
        texture.set("slice", slice);
    }

    public static List<SpriteDefinition> parseSprites(
            ObjectNode importer,
            String textureGuid,
            int atlasWidth,
            int atlasHeight
    ) {
        List<SpriteDefinition> result = new ArrayList<>();
        ObjectNode texture = textureNode(importer, false);
        if (texture == null || !texture.has("sprites")) {
            return result;
        }
        JsonNode sprites = texture.path("sprites");
        if (!sprites.isArray()) {
            return result;
        }
        for (JsonNode node : sprites) {
            String guid = node.path("guid").asText("");
            if (guid.isBlank()) {
                continue;
            }
            int w = node.path("width").asInt(0);
            int h = node.path("height").asInt(0);
            if (w <= 0 || h <= 0) {
                continue;
            }
            result.add(new SpriteDefinition(
                    guid,
                    node.path("name").asText("Sprite"),
                    textureGuid,
                    node.path("x").asInt(0),
                    node.path("y").asInt(0),
                    w,
                    h,
                    (float) node.path("pivotX").asDouble(0.5),
                    (float) node.path("pivotY").asDouble(0.5),
                    atlasWidth,
                    atlasHeight
            ));
        }
        return result;
    }

    /**
     * Replaces the sprites array with {@code sprites} only (no merge with prior entries).
     */
    public static void replaceSprites(ObjectNode importer, List<SpriteDefinition> sprites) {
        ObjectNode texture = textureNode(importer, true);
        ArrayNode array = MAPPER.createArrayNode();
        for (SpriteDefinition sprite : sprites) {
            array.add(spriteNode(sprite));
        }
        texture.set("sprites", array);
    }

    /**
     * Writes sprite array to importer, preserving GUIDs for unchanged name+rect keys.
     */
    public static void writeSprites(
            ObjectNode importer,
            List<SpriteDefinition> sprites,
            Map<String, String> previousGuidByRectKey
    ) {
        ObjectNode texture = textureNode(importer, true);
        ArrayNode array = MAPPER.createArrayNode();
        Map<String, String> used = previousGuidByRectKey == null ? Map.of() : previousGuidByRectKey;
        for (SpriteDefinition sprite : sprites) {
            String guid = sprite.guid();
            if (guid == null || guid.isBlank()) {
                String key = sprite.rectKey();
                guid = used.getOrDefault(key, Guid.newGuid());
                sprite = new SpriteDefinition(
                        guid,
                        sprite.name(),
                        sprite.textureGuid(),
                        sprite.x(),
                        sprite.y(),
                        sprite.width(),
                        sprite.height(),
                        sprite.pivotX(),
                        sprite.pivotY(),
                        sprite.atlasWidth(),
                        sprite.atlasHeight()
                );
            }
            array.add(spriteNode(sprite));
        }
        texture.set("sprites", array);
    }

    private static ObjectNode spriteNode(SpriteDefinition sprite) {
        ObjectNode node = MAPPER.createObjectNode();
        node.put("guid", sprite.guid());
        node.put("name", sprite.name());
        node.put("x", sprite.x());
        node.put("y", sprite.y());
        node.put("width", sprite.width());
        node.put("height", sprite.height());
        node.put("pivotX", sprite.pivotX());
        node.put("pivotY", sprite.pivotY());
        return node;
    }

    public static Map<String, String> guidKeysByRect(ObjectNode importer) {
        Map<String, String> map = new HashMap<>();
        ObjectNode texture = textureNode(importer, false);
        if (texture == null || !texture.has("sprites")) {
            return map;
        }
        for (JsonNode node : texture.path("sprites")) {
            String guid = node.path("guid").asText("");
            if (guid.isBlank()) {
                continue;
            }
            String name = node.path("name").asText("Sprite");
            int x = node.path("x").asInt(0);
            int y = node.path("y").asInt(0);
            int w = node.path("width").asInt(0);
            int h = node.path("height").asInt(0);
            map.put(name + ":" + x + "," + y + "," + w + "," + h, guid);
        }
        return map;
    }

    public static ObjectNode textureNode(ObjectNode importer, boolean create) {
        if (importer == null) {
            return create ? MAPPER.createObjectNode() : null;
        }
        if (!importer.has("texture")) {
            if (!create) {
                return null;
            }
            importer.set("texture", MAPPER.createObjectNode());
        }
        return (ObjectNode) importer.get("texture");
    }
}
