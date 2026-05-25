package org.llw.studio.editor.tilemap;

import imgui.ImGui;
import imgui.flag.ImGuiMouseButton;
import org.llw.studio.editor.EditorSession;
import org.llw.studio.editor.SceneToolMode;
import org.llw.studio.editor.SceneToolState;
import org.llw.studio.editor.SelectionService;
import org.llw.studio.editor.commands.TilemapPaintCommand;
import org.llw.studio.editor.commands.UndoStack;
import org.llw.studio.editor.gizmo.GizmoContext;
import org.llw.studio.ecs.EntityId;
import org.llw.studio.ecs.components.TilemapCell;
import org.llw.studio.ecs.components.TilemapComponent;
import org.llw.studio.ecs.components.TilemapLayer;
import org.llw.studio.ecs.components.Transform2DComponent;
import org.llw.studio.tilemap.TilemapMath;
import org.llw.studio.ecs.components.WorldTransformComponent;
import org.llw.studio.scene.Scene;
import org.llw.studio.systems.TransformSystem;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Handles tile paint/erase input in the scene view.
 */
public final class TilemapPaintController {
    private final SelectionService selection;
    private final UndoStack undoStack;
    private final EditorSession session;
    private final SceneToolState toolState;

    private boolean strokeActive;
    private Map<Long, TilemapCell> strokeBefore;
    private final Set<Long> strokeTouched = new HashSet<>();

    public TilemapPaintController(SelectionService selection, UndoStack undoStack, EditorSession session, SceneToolState toolState) {
        this.selection = selection;
        this.undoStack = undoStack;
        this.session = session;
        this.toolState = toolState;
    }

    public void updateActiveTilemap(Scene scene) {
        TilemapEditState edit = session.tilemapEdit();
        EntityId selected = selection.selected();
        if (!selected.isNone()) {
            TilemapComponent tilemap = scene.world().getComponent(selected, TilemapComponent.class);
            if (tilemap != null) {
                edit.activeTilemapEntity = selected;
                if (tilemap.tilesetTextureGuid != null && !tilemap.tilesetTextureGuid.isBlank()) {
                    // keep active sprite if set
                }
                return;
            }
        }
        if (toolState.mode() != SceneToolMode.TILE_PAINT && toolState.mode() != SceneToolMode.TILE_ERASE) {
            return;
        }
    }

    public EntityId activeTilemapEntity(Scene scene) {
        TilemapEditState edit = session.tilemapEdit();
        if (!edit.activeTilemapEntity.isNone()) {
            TilemapComponent tilemap = scene.world().getComponent(edit.activeTilemapEntity, TilemapComponent.class);
            if (tilemap != null) {
                return edit.activeTilemapEntity;
            }
        }
        EntityId selected = selection.selected();
        if (!selected.isNone() && scene.world().getComponent(selected, TilemapComponent.class) != null) {
            edit.activeTilemapEntity = selected;
            return selected;
        }
        return EntityId.none();
    }

    public boolean handle(Scene scene, GizmoContext context, float mouseX, float mouseY) {
        SceneToolMode mode = toolState.mode();
        if (mode != SceneToolMode.TILE_PAINT && mode != SceneToolMode.TILE_ERASE) {
            endStrokeIfNeeded(scene, false);
            return false;
        }

        EntityId entity = activeTilemapEntity(scene);
        if (entity.isNone()) {
            return false;
        }

        TilemapComponent tilemap = scene.world().getComponent(entity, TilemapComponent.class);
        if (tilemap == null || tilemap.tilesetTextureGuid.isBlank()) {
            return false;
        }

        TilemapEditState edit = session.tilemapEdit();
        if (edit.activeSpriteGuid == null || edit.activeSpriteGuid.isBlank()) {
            if (mode == SceneToolMode.TILE_PAINT) {
                return true;
            }
        }

        org.llw.studio.editor.render.EditorWorldTransforms.ensureUpdated(scene);
        WorldTransformComponent world = scene.world().getComponent(entity, WorldTransformComponent.class);
        Transform2DComponent local = scene.world().getComponent(entity, Transform2DComponent.class);
        if (local == null) {
            return false;
        }
        // Paint origin uses world position/scale so nested tilemaps align with parent transforms.
        float originX = world != null ? world.worldX : local.x;
        float originY = world != null ? world.worldY : local.y;
        float scaleX = world != null ? world.worldScaleX : local.scaleX;
        float scaleY = world != null ? world.worldScaleY : local.scaleY;

        var worldPos = context.screenToWorld(mouseX, mouseY); // Same screen→world path as picking/gizmos.
        int cellX = TilemapMath.worldToCellX(worldPos.x, originX, tilemap.cellWidth, scaleX);
        int cellY = TilemapMath.worldToCellY(worldPos.y, originY, tilemap.cellHeight, scaleY);

        boolean erase = mode == SceneToolMode.TILE_ERASE
                || ImGui.getIO().getKeyShift();
        boolean eyedropper = ImGui.getIO().getKeyCtrl();

        if (ImGui.isMouseClicked(ImGuiMouseButton.Left)) {
            if (eyedropper) {
                pickTile(tilemap, edit, cellX, cellY);
                return true;
            }
            beginStroke(tilemap, edit.activeLayerIndex);
        }

        if (ImGui.isMouseDown(ImGuiMouseButton.Left) && !eyedropper) {
            if (!strokeActive) {
                beginStroke(tilemap, edit.activeLayerIndex);
            }
            applyCell(tilemap, edit.activeLayerIndex, cellX, cellY, erase ? null : edit.activeSpriteGuid);
        }

        if (ImGui.isMouseReleased(ImGuiMouseButton.Left)) {
            endStrokeIfNeeded(scene, true);
        }
        return true;
    }

    private void pickTile(TilemapComponent tilemap, TilemapEditState edit, int cellX, int cellY) {
        TilemapLayer layer = tilemap.layerAt(edit.activeLayerIndex);
        TilemapCell cell = layer.getCell(cellX, cellY);
        if (cell != null && cell.spriteGuid != null && !cell.spriteGuid.isBlank()) {
            edit.activeSpriteGuid = cell.spriteGuid;
        }
    }

    private void beginStroke(TilemapComponent tilemap, int layerIndex) {
        TilemapLayer layer = tilemap.layerAt(layerIndex);
        strokeBefore = TilemapPaintCommand.snapshotLayer(layer); // One before snapshot for the whole drag stroke.
        strokeTouched.clear();
        strokeActive = true;
    }

    private void applyCell(TilemapComponent tilemap, int layerIndex, int cellX, int cellY, String spriteGuid) {
        TilemapLayer layer = tilemap.layerAt(layerIndex);
        long key = TilemapPaintCommand.key(cellX, cellY);
        if (!strokeTouched.add(key)) {
            return; // Dedupe drag revisits to the same cell within one stroke.
        }
        if (spriteGuid == null || spriteGuid.isBlank()) {
            layer.removeCell(cellX, cellY);
        } else {
            TilemapCell cell = new TilemapCell();
            cell.spriteGuid = spriteGuid;
            layer.setCell(cellX, cellY, cell);
        }
    }

    private void endStrokeIfNeeded(Scene scene, boolean commit) {
        if (!strokeActive) {
            return;
        }
        strokeActive = false;
        EntityId entity = session.tilemapEdit().activeTilemapEntity;
        if (entity.isNone() || strokeBefore == null) {
            strokeBefore = null;
            strokeTouched.clear();
            return;
        }
        TilemapComponent tilemap = scene.world().getComponent(entity, TilemapComponent.class);
        if (tilemap == null) {
            strokeBefore = null;
            strokeTouched.clear();
            return;
        }
        int layerIndex = session.tilemapEdit().activeLayerIndex;
        TilemapLayer layer = tilemap.layerAt(layerIndex);
        Map<Long, TilemapCell> after = TilemapPaintCommand.snapshotLayer(layer);
        if (commit && !strokeBefore.equals(after)) {
            undoStack.execute(new TilemapPaintCommand(scene, entity, layerIndex, strokeBefore, after)); // One undo step per stroke.
        } else if (!commit) {
            TilemapPaintCommand.restoreCells(layer, strokeBefore); // Tool switch cancels in-progress stroke without pushing undo.
        }
        strokeBefore = null;
        strokeTouched.clear();
    }
}
