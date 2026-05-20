package org.llw.studio.editor;

import org.llw.studio.ecs.EntityId;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Editor selection of ECS entities (primary + optional multi-select).
 */
public final class SelectionService {

    /** ImGui drag-drop payload type for entity IDs. */
    public static final String PAYLOAD_ENTITY = "ENTITY_ID";

    private final LinkedHashSet<EntityId> selected = new LinkedHashSet<>();
    private EntityId primary = EntityId.none();

    /** @return primary selected entity, or {@link EntityId#none()} */
    public EntityId selected() {
        return primary;
    }

    /** @return immutable copy of all selected entities */
    public Set<EntityId> allSelected() {
        return Set.copyOf(selected);
    }

    /**
     * Replaces selection with a single entity (or clears if none).
     *
     * @param entity entity to select
     */
    public void select(EntityId entity) {
        if (EditorDragDrop.shouldSuppressSelectionChange()) {
            return;
        }
        selected.clear();
        if (entity != null && !entity.isNone()) {
            selected.add(entity);
            primary = entity;
        } else {
            primary = EntityId.none();
        }
    }

    /**
     * Selects or toggles an entity when {@code additive} is true (Ctrl-style).
     *
     * @param entity    entity to select or toggle
     * @param additive  if true, add/remove from set; if false, replace selection
     */
    public void toggleSelect(EntityId entity, boolean additive) {
        if (EditorDragDrop.shouldSuppressSelectionChange()) {
            return;
        }
        if (entity == null || entity.isNone()) {
            return;
        }
        if (!additive) {
            select(entity);
            return;
        }
        if (selected.contains(entity)) {
            selected.remove(entity);
            if (primary.equals(entity)) {
                primary = selected.isEmpty() ? EntityId.none() : selected.iterator().next();
            }
        } else {
            selected.add(entity);
            primary = entity;
        }
    }

    /** Clears all selection. */
    public void clear() {
        if (EditorDragDrop.shouldSuppressSelectionChange()) {
            return;
        }
        selected.clear();
        primary = EntityId.none();
    }

    /**
     * @param entity entity to test
     * @return whether {@code entity} is in the current selection
     */
    public boolean isSelected(EntityId entity) {
        return selected.contains(entity);
    }

    /** @return number of selected entities */
    public int count() {
        return selected.size();
    }
}
