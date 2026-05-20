package org.llw.studio.assets;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Reads and writes {@code importer.tile} settings in asset meta.
 */
public final class TilesetData {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private TilesetData() {
    }

    public static TilesetDefinition parse(
            ObjectNode importer,
            String textureGuid,
            List<SpriteDefinition> sprites
    ) {
        SpriteSliceSettings slice = TextureSpriteData.readSliceSettings(importer);
        int defaultW = slice.cellWidth > 0 ? slice.cellWidth : 32;
        int defaultH = slice.cellHeight > 0 ? slice.cellHeight : 32;

        ObjectNode tile = tileNode(importer, false);
        if (tile == null) {
            return TilesetDefinition.fromSprites(textureGuid, sprites, defaultW, defaultH);
        }

        TilesetDefinition def = new TilesetDefinition(textureGuid);
        def.cellWidth = tile.path("cellWidth").asInt(defaultW);
        def.cellHeight = tile.path("cellHeight").asInt(defaultH);

        if (!tile.has("tiles") || !tile.path("tiles").isArray()) {
            def.tiles.addAll(TilesetDefinition.fromSprites(textureGuid, sprites, def.cellWidth, def.cellHeight).tiles);
            return def;
        }

        for (JsonNode node : tile.path("tiles")) {
            String spriteGuid = node.path("spriteGuid").asText("");
            if (spriteGuid.isBlank()) {
                continue;
            }
            TileDefinition tileDef = new TileDefinition(spriteGuid);
            tileDef.collision = parseCollision(node.path("collision").asText("none"));
            JsonNode ruleNode = node.path("ruleTile");
            if (ruleNode.isObject() && !ruleNode.isEmpty()) {
                tileDef.ruleTile = parseRuleTile(ruleNode);
            }
            def.tiles.add(tileDef);
        }

        // Ensure every sprite has a tile entry
        for (SpriteDefinition sprite : sprites) {
            if (def.tileForSprite(sprite.guid()) == null) {
                def.tiles.add(new TileDefinition(sprite.guid()));
            }
        }
        return def;
    }

    public static void write(ObjectNode importer, TilesetDefinition def) {
        if (def == null) {
            return;
        }
        ObjectNode tile = tileNode(importer, true);
        tile.put("cellWidth", def.cellWidth);
        tile.put("cellHeight", def.cellHeight);
        ArrayNode array = MAPPER.createArrayNode();
        for (TileDefinition tileDef : def.tiles) {
            array.add(tileNode(tileDef));
        }
        tile.set("tiles", array);
    }

    public static void ensureDefaults(ObjectNode importer, String textureGuid, List<SpriteDefinition> sprites) {
        if (tileNode(importer, false) != null) {
            return;
        }
        SpriteSliceSettings slice = TextureSpriteData.readSliceSettings(importer);
        int w = slice.cellWidth > 0 ? slice.cellWidth : 32;
        int h = slice.cellHeight > 0 ? slice.cellHeight : 32;
        write(importer, TilesetDefinition.fromSprites(textureGuid, sprites, w, h));
    }

    private static ObjectNode tileNode(TileDefinition tileDef) {
        ObjectNode node = MAPPER.createObjectNode();
        node.put("spriteGuid", tileDef.spriteGuid);
        node.put("collision", collisionId(tileDef.collision));
        if (tileDef.ruleTile != null && tileDef.ruleTile.isActive()) {
            node.set("ruleTile", ruleTileNode(tileDef.ruleTile));
        }
        return node;
    }

    private static ObjectNode ruleTileNode(RuleTileDefinition ruleTile) {
        ObjectNode node = MAPPER.createObjectNode();
        node.put("defaultSpriteGuid", ruleTile.defaultSpriteGuid);
        ArrayNode rules = MAPPER.createArrayNode();
        for (RuleTileRule rule : ruleTile.rules) {
            ObjectNode ruleNode = MAPPER.createObjectNode();
            ruleNode.set("neighbors", maskNode(rule.neighbors));
            ruleNode.put("spriteGuid", rule.spriteGuid);
            rules.add(ruleNode);
        }
        node.set("rules", rules);
        return node;
    }

    private static RuleTileDefinition parseRuleTile(JsonNode node) {
        RuleTileDefinition def = new RuleTileDefinition();
        def.defaultSpriteGuid = node.path("defaultSpriteGuid").asText("");
        JsonNode rules = node.path("rules");
        if (!rules.isArray()) {
            return def;
        }
        for (JsonNode ruleNode : rules) {
            TileNeighborMask mask = parseMask(ruleNode.path("neighbors"));
            String spriteGuid = ruleNode.path("spriteGuid").asText("");
            def.rules.add(new RuleTileRule(mask, spriteGuid));
        }
        return def;
    }

    private static ObjectNode maskNode(TileNeighborMask mask) {
        ObjectNode node = MAPPER.createObjectNode();
        node.put("north", constraintId(mask.north));
        node.put("northEast", constraintId(mask.northEast));
        node.put("east", constraintId(mask.east));
        node.put("southEast", constraintId(mask.southEast));
        node.put("south", constraintId(mask.south));
        node.put("southWest", constraintId(mask.southWest));
        node.put("west", constraintId(mask.west));
        node.put("northWest", constraintId(mask.northWest));
        return node;
    }

    private static TileNeighborMask parseMask(JsonNode node) {
        TileNeighborMask mask = new TileNeighborMask();
        if (!node.isObject()) {
            return mask;
        }
        mask.north = parseConstraint(node.path("north").asText("dontCare"));
        mask.northEast = parseConstraint(node.path("northEast").asText("dontCare"));
        mask.east = parseConstraint(node.path("east").asText("dontCare"));
        mask.southEast = parseConstraint(node.path("southEast").asText("dontCare"));
        mask.south = parseConstraint(node.path("south").asText("dontCare"));
        mask.southWest = parseConstraint(node.path("southWest").asText("dontCare"));
        mask.west = parseConstraint(node.path("west").asText("dontCare"));
        mask.northWest = parseConstraint(node.path("northWest").asText("dontCare"));
        return mask;
    }

    private static TileNeighborConstraint parseConstraint(String raw) {
        if (raw == null) {
            return TileNeighborConstraint.DONT_CARE;
        }
        return switch (raw.toLowerCase(Locale.ROOT)) {
            case "same" -> TileNeighborConstraint.SAME;
            case "notsame", "not_same" -> TileNeighborConstraint.NOT_SAME;
            default -> TileNeighborConstraint.DONT_CARE;
        };
    }

    private static TileCollisionType parseCollision(String raw) {
        if (raw == null) {
            return TileCollisionType.NONE;
        }
        return switch (raw.toLowerCase(Locale.ROOT)) {
            case "solid" -> TileCollisionType.SOLID;
            default -> TileCollisionType.NONE;
        };
    }

    private static String constraintId(TileNeighborConstraint c) {
        return switch (c) {
            case SAME -> "same";
            case NOT_SAME -> "notSame";
            default -> "dontCare";
        };
    }

    private static String collisionId(TileCollisionType c) {
        return c == TileCollisionType.SOLID ? "solid" : "none";
    }

    public static ObjectNode tileNode(ObjectNode importer, boolean create) {
        if (importer == null) {
            return create ? MAPPER.createObjectNode() : null;
        }
        if (!importer.has("tile")) {
            if (!create) {
                return null;
            }
            importer.set("tile", MAPPER.createObjectNode());
        }
        return (ObjectNode) importer.get("tile");
    }
}
