package org.llw.studio.editor.panels;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Map of dockable {@link EditorPanel} instances by {@link EditorPanel#id()}.
 */
public final class PanelRegistry {
    private final Map<String, EditorPanel> panels = new LinkedHashMap<>();

    /**
     * @param panel panel to add or replace by id
     */
    public void register(EditorPanel panel) {
        panels.put(panel.id(), panel);
    }

    /** @return all registered panels in registration order */
    public List<EditorPanel> all() {
        return new ArrayList<>(panels.values());
    }

    /**
     * @param id panel id
     * @return panel, or null if not registered
     */
    public EditorPanel get(String id) {
        return panels.get(id);
    }
}
