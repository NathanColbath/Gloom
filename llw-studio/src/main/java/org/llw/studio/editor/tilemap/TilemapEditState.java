package org.llw.studio.editor.tilemap;

import org.llw.studio.ecs.EntityId;

/**
 * Shared editor state for tilemap painting.
 */
public final class TilemapEditState {
    public EntityId activeTilemapEntity = EntityId.none();
    public int activeLayerIndex;
    public String activeSpriteGuid = "";

    public void clear() {
        activeTilemapEntity = EntityId.none();
        activeLayerIndex = 0;
        activeSpriteGuid = "";
    }
}
