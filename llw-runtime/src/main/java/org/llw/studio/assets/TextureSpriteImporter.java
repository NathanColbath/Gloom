package org.llw.studio.assets;

import com.fasterxml.jackson.databind.node.ObjectNode;

import org.llw.render.core.IntSize;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Ensures texture assets have importer sprite data (default single full-image sprite).
 */
public final class TextureSpriteImporter implements AssetImporter {
    @Override
    public AssetType type() {
        return AssetType.TEXTURE;
    }

    @Override
    public void importAsset(Path assetPath, MetaFile.MetaData meta) throws Exception {
        ensureImported(assetPath, meta);
    }

    /**
     * @param assetPath texture file path
     * @param meta      mutable meta for the texture
     * @return {@code true} if meta was modified
     */
    public static boolean ensureImported(Path assetPath, MetaFile.MetaData meta) throws IOException {
        if (meta.importer == null) {
            meta.importer = new com.fasterxml.jackson.databind.ObjectMapper().createObjectNode();
        }
        ObjectNode texture = TextureSpriteData.textureNode(meta.importer, true);
        if (texture.has("sprites") && texture.path("sprites").isArray() && texture.path("sprites").size() > 0) {
            return false;
        }
        IntSize size = TextureImageSize.read(assetPath);
        String baseName = assetPath.getFileName().toString();
        List<SpriteDefinition> sprites = List.of(fullImageSprite(
                meta.guid,
                baseName,
                size.width(),
                size.height()
        ));
        TextureSpriteData.setSpriteMode(meta.importer, "single");
        TextureSpriteData.ensureImportDefaults(meta.importer);
        TextureSpriteData.replaceSprites(meta.importer, sprites);
        return true;
    }

    /**
     * Applies a grid slice to texture meta, preserving GUIDs where name+rect match.
     */
    public static void applyGridSlice(
            Path assetPath,
            MetaFile.MetaData meta,
            SpriteSliceSettings settings
    ) throws IOException {
        IntSize size = TextureImageSize.read(assetPath);
        String baseName = assetPath.getFileName().toString();
        Map<String, String> previous = TextureSpriteData.guidKeysByRect(meta.importer);
        List<SpriteSlicer.SpriteSliceDraft> drafts = SpriteSlicer.sliceGrid(
                size.width(),
                size.height(),
                baseName,
                settings
        );
        List<SpriteDefinition> sprites = new ArrayList<>();
        for (SpriteSlicer.SpriteSliceDraft draft : drafts) {
            String key = draft.name() + ":" + draft.x() + "," + draft.y() + "," + draft.width() + "," + draft.height();
            String guid = previous.getOrDefault(key, Guid.newGuid());
            sprites.add(new SpriteDefinition(
                    guid,
                    draft.name(),
                    meta.guid,
                    draft.x(),
                    draft.y(),
                    draft.width(),
                    draft.height(),
                    draft.pivotX(),
                    draft.pivotY(),
                    size.width(),
                    size.height()
            ));
        }
        if (sprites.isEmpty()) {
            sprites.add(fullImageSprite(meta.guid, baseName, size.width(), size.height()));
        }
        TextureSpriteData.setSpriteMode(meta.importer, sprites.size() > 1 ? "multiple" : "single");
        TextureSpriteData.ensureImportDefaults(meta.importer);
        TextureSpriteData.writeSliceSettings(meta.importer, settings);
        TextureSpriteData.replaceSprites(meta.importer, sprites);
    }

    /**
     * @return full-image sprite definition with a new GUID
     */
    public static SpriteDefinition fullImageSprite(String textureGuid, String baseName, int width, int height) {
        int dot = baseName == null ? -1 : baseName.lastIndexOf('.');
        String stem = baseName == null || baseName.isBlank()
                ? "sprite"
                : (dot > 0 ? baseName.substring(0, dot) : baseName);
        return new SpriteDefinition(
                Guid.newGuid(),
                stem.isBlank() ? "sprite" : stem,
                textureGuid,
                0,
                0,
                Math.max(1, width),
                Math.max(1, height),
                0.5f,
                0.5f,
                Math.max(1, width),
                Math.max(1, height)
        );
    }

}
