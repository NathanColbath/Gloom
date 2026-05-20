package org.llw.studio.editor.widgets;

import imgui.ImGui;
import org.llw.studio.assets.AssetDatabase;
import org.llw.studio.editor.SelectionService;
import org.llw.studio.editor.StudioContext;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Defers folder drag-drop targets until after browser layout so ImGui hit-testing matches
 * screen positions (grid columns and overlapping widgets otherwise break drops).
 */
public final class AssetBrowserFolderDropState {
    private static final List<FolderDropZone> zones = new ArrayList<>();

    private AssetBrowserFolderDropState() {
    }

    public static void beginFrame() {
        zones.clear();
    }

    public static void register(Path folder, float x1, float y1, float x2, float y2) {
        if (folder == null) {
            return;
        }
        zones.add(new FolderDropZone(folder, x1, y1, x2, y2));
    }

    public static void submitTargets(
            StudioContext context,
            SelectionService selection,
            AssetDatabase assets,
            Runnable onAssetsChanged
    ) {
        for (int i = 0; i < zones.size(); i++) {
            FolderDropZone zone = zones.get(i);
            ImGui.setCursorScreenPos(zone.x1, zone.y1);
            ImGui.pushID("folder_drop_" + i);
            ImGui.invisibleButton("zone", zone.x2 - zone.x1, zone.y2 - zone.y1);
            AssetDropTargets.acceptFolderDrops(
                    context,
                    selection,
                    assets,
                    zone.folder,
                    onAssetsChanged
            );
            ImGui.popID();
        }
    }

    private record FolderDropZone(Path folder, float x1, float y1, float x2, float y2) {
    }
}
