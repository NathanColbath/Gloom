package org.llw.studio.editor.ui;

import org.llw.studio.ecs.ComponentStore;
import org.llw.studio.ecs.EntityId;
import org.llw.studio.ecs.components.HierarchyComponent;
import org.llw.studio.ecs.components.UICanvasComponent;
import org.llw.studio.ecs.components.UIButtonComponent;
import org.llw.studio.ecs.components.UILabelComponent;
import org.llw.studio.ecs.components.UITextFieldComponent;
import org.llw.studio.ecs.components.UIToggleComponent;
import org.llw.studio.scene.Scene;

import java.util.ArrayList;
import java.util.List;

/**
 * Per-session state for the UI Editor panel (active canvas, cached canvas list).
 */
public final class UiEditorState {
    private EntityId activeCanvas = EntityId.none();
    private final List<CanvasEntry> canvasEntries = new ArrayList<>();

    /** @return entity id of the canvas being edited, or none */
    public EntityId activeCanvas() {
        return activeCanvas;
    }

    /**
     * @param canvas canvas root entity
     */
    public void setActiveCanvas(EntityId canvas) {
        activeCanvas = canvas == null ? EntityId.none() : canvas;
    }

    /** @return canvases discovered on the last {@link #refreshCanvases} call */
    public List<CanvasEntry> canvasEntries() {
        return List.copyOf(canvasEntries);
    }

    /**
     * Rebuilds the canvas picker list from the scene.
     *
     * @param scene edit scene
     */
    public void refreshCanvases(Scene scene) {
        canvasEntries.clear();
        if (scene == null) {
            return;
        }
        ComponentStore<UICanvasComponent> store = scene.world().store(UICanvasComponent.class);
        for (int i = 0; i < store.size(); i++) {
            EntityId entity = store.entityAt(i);
            UICanvasComponent canvas = store.componentAt(i);
            String name = sceneName(scene, entity);
            canvasEntries.add(new CanvasEntry(entity, name, canvas));
        }
        if (activeCanvas.isNone() && !canvasEntries.isEmpty()) {
            activeCanvas = canvasEntries.get(0).entity;
        } else if (!activeCanvas.isNone()) {
            boolean found = canvasEntries.stream().anyMatch(e -> e.entity.equals(activeCanvas));
            if (!found && !canvasEntries.isEmpty()) {
                activeCanvas = canvasEntries.get(0).entity;
            }
        }
    }

    /**
     * Sets {@link #activeCanvas} from a selected widget or canvas entity.
     *
     * @param scene  edit scene
     * @param entity selected entity
     */
    public void resolveActiveCanvas(Scene scene, EntityId entity) {
        if (scene == null || entity == null || entity.isNone()) {
            return;
        }
        EntityId canvas = findCanvasAncestor(scene, entity);
        if (!canvas.isNone()) {
            activeCanvas = canvas;
        }
    }

    private static EntityId findCanvasAncestor(Scene scene, EntityId entity) {
        if (scene.world().getComponent(entity, UICanvasComponent.class) != null) {
            return entity;
        }
        HierarchyComponent hierarchy = scene.world().getComponent(entity, HierarchyComponent.class);
        if (hierarchy == null || hierarchy.parentIndex < 0) {
            return EntityId.none();
        }
        return findCanvasAncestor(scene, new EntityId(hierarchy.parentIndex, hierarchy.parentGeneration));
    }

    private static String sceneName(Scene scene, EntityId entity) {
        var name = scene.world().getComponent(entity, org.llw.studio.ecs.components.NameComponent.class);
        if (name != null && name.name() != null && !name.name().isBlank()) {
            return name.name();
        }
        return "Canvas";
    }

    /** @return true when the entity is a UI widget or canvas */
    public static boolean isUiEntity(Scene scene, EntityId entity) {
        if (scene == null || entity == null || entity.isNone()) {
            return false;
        }
        return scene.world().getComponent(entity, UICanvasComponent.class) != null
                || scene.world().getComponent(entity, UILabelComponent.class) != null
                || scene.world().getComponent(entity, UIButtonComponent.class) != null
                || scene.world().getComponent(entity, UIToggleComponent.class) != null
                || scene.world().getComponent(entity, UITextFieldComponent.class) != null;
    }

    /** Canvas root entry for the UI Editor picker. */
    public record CanvasEntry(EntityId entity, String name, UICanvasComponent canvas) {
    }
}
