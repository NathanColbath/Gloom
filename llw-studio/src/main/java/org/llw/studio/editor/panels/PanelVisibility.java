package org.llw.studio.editor.panels;

import imgui.ImGui;
import imgui.type.ImBoolean;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Tracks open/closed state for optional dock panels (closable tabs + View menu toggles).
 *
 * <p>State is persisted in {@code [StudioPanels]} inside the ImGui layout ini.
 */
public final class PanelVisibility {
    private static final List<String> PERSISTED_PANELS = List.of("animation", "tile_palette", "particle_system");

    private final Path iniPath;
    private final Map<String, ImBoolean> openStates = new LinkedHashMap<>();
    private final Set<String> focusNextFrame = new HashSet<>();
    private boolean dirty;
    private long lastIniReconcileMs;

    /**
     * @param iniPath ImGui layout ini path ({@link org.llw.studio.editor.imgui.ImGuiContext#globalIniPath()})
     */
    public PanelVisibility(Path iniPath) {
        this.iniPath = iniPath;
        for (String panelId : PERSISTED_PANELS) {
            openState(panelId);
        }
        PanelVisibilityIni.load(iniPath, this);
    }

    /**
     * @param panelId stable panel id from {@link EditorPanel#id()}
     * @return mutable open flag passed to {@code ImGui.begin}
     */
    public ImBoolean openState(String panelId) {
        return openStates.computeIfAbsent(panelId, id -> new ImBoolean(true));
    }

    /**
     * @param panelId stable panel id
     * @return whether the panel tab is currently open
     */
    public boolean isOpen(String panelId) {
        return openState(panelId).get();
    }

    /**
     * @param panelId stable panel id
     * @param open    desired open state
     */
    public void setOpen(String panelId, boolean open) {
        if (isOpen(panelId) == open) {
            return;
        }
        openState(panelId).set(open);
        markDirty();
    }

    /**
     * Opens a closable dock panel. Pair with {@link #end()} in a {@code try/finally} block.
     *
     * <p>Skips {@code ImGui.begin} when the panel is closed. When open, {@code ImGui.end()} must
     * always be called afterward, even if this returns {@code false} (collapsed).
     *
     * @param panelId stable panel id
     * @param title   ImGui window title from {@link EditorPanel#title()}
     * @return whether panel body content should be drawn this frame
     */
    public boolean begin(String panelId, String title) {
        return begin(panelId, title, 0);
    }

    /**
     * @param panelId stable panel id
     * @param title   ImGui window title
     * @param flags   ImGui window flags
     * @return whether panel body content should be drawn this frame
     */
    public boolean begin(String panelId, String title, int flags) {
        ImBoolean open = openState(panelId);
        if (!open.get()) {
            return false;
        }
        consumeFocus(panelId);
        boolean draw = ImGui.begin(title, open, flags);
        if (!open.get()) {
            markDirty();
        }
        return draw;
    }

    /** Always call after {@link #begin(String, String)} when the panel is open. */
    public void end() {
        ImGui.end();
    }

    /**
     * Toggles a panel and focuses it when opening.
     *
     * @param panelId stable panel id
     */
    public void toggle(String panelId) {
        boolean next = !isOpen(panelId);
        setOpen(panelId, next);
        if (next) {
            focusNextFrame.add(panelId);
        }
    }

    /** Focuses a panel on the next frame without changing open state. */
    public void focus(String panelId) {
        if (isOpen(panelId)) {
            focusNextFrame.add(panelId);
        }
    }

    /** Reopens all closable panels (used when resetting layout). */
    public void resetAllOpen() {
        focusNextFrame.clear();
        for (ImBoolean open : openStates.values()) {
            open.set(true);
        }
        markDirty();
    }

    /** Writes current visibility into {@code imgui.ini}, even if unchanged. */
    public void persistToIni() {
        PanelVisibilityIni.merge(iniPath, snapshotInternal());
        dirty = false;
    }

    /** Writes pending visibility changes into {@code imgui.ini}. */
    public void flush() {
        if (!dirty) {
            return;
        }
        PanelVisibilityIni.merge(iniPath, snapshotInternal());
        dirty = false;
    }

    /**
     * Re-merges visibility if ImGui overwrote the ini, or if changes are pending.
     *
     * <p>Call once per editor frame.
     */
    public void reconcileIni() {
        long now = System.currentTimeMillis();
        if (dirty) {
            flush();
            lastIniReconcileMs = now;
            return;
        }
        if (now - lastIniReconcileMs < 3000L) {
            return;
        }
        lastIniReconcileMs = now;
        if (!PanelVisibilityIni.hasSection(iniPath)) {
            PanelVisibilityIni.merge(iniPath, snapshotInternal());
        }
    }

    /** @return persisted panel id to open-state map */
    public Map<String, Boolean> snapshot() {
        return snapshotInternal();
    }

    private void markDirty() {
        dirty = true;
    }

    private Map<String, Boolean> snapshotInternal() {
        Map<String, Boolean> states = new LinkedHashMap<>();
        for (String panelId : PERSISTED_PANELS) {
            states.put(panelId, isOpen(panelId));
        }
        return states;
    }

    private void consumeFocus(String panelId) {
        if (focusNextFrame.remove(panelId)) {
            ImGui.setNextWindowFocus();
        }
    }
}
