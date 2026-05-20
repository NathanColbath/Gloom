package org.llw.studio.editor.assets;

import imgui.ImGui;
import imgui.flag.ImGuiInputTextFlags;
import imgui.type.ImString;
import org.llw.studio.assets.AssetDatabase;
import org.llw.studio.assets.AssetType;
import org.llw.studio.assets.StudioAsset;

/**
 * Slow double-click inline rename for project browser rows.
 */
public final class AssetBrowserRename {
    private static final long SLOW_DOUBLE_CLICK_MIN_MS = 400L;
    private static final long SLOW_DOUBLE_CLICK_MAX_MS = 1200L;

    private String editingGuid = "";
    private final ImString buffer = new ImString(256);
    private long lastClickMs;
    private String lastClickGuid = "";

    public boolean isEditing(String assetGuid) {
        return assetGuid != null && assetGuid.equals(editingGuid);
    }

    public void cancel() {
        editingGuid = "";
    }

    /**
     * Call when the user activates a selectable row (single click).
     *
     * @param asset    clicked asset
     * @param selected whether the asset was already selected before this click
     */
    public void onAssetClicked(StudioAsset asset, boolean selected) {
        if (asset == null || asset.isFolder()) {
            lastClickGuid = "";
            return;
        }
        long now = System.currentTimeMillis();
        if (selected
                && asset.guid().equals(lastClickGuid)
                && now - lastClickMs >= SLOW_DOUBLE_CLICK_MIN_MS
                && now - lastClickMs <= SLOW_DOUBLE_CLICK_MAX_MS) {
            beginEditing(asset);
        }
        lastClickGuid = asset.guid();
        lastClickMs = now;
    }

    /**
     * @return {@code true} while the rename field is shown (skip normal row label)
     */
    public boolean renderInline(StudioAsset asset, AssetDatabase assets, AssetEditorActions actions) {
        if (!isEditing(asset.guid())) {
            return false;
        }
        ImGui.setNextItemWidth(-1f);
        ImGui.setKeyboardFocusHere();
        int flags = ImGuiInputTextFlags.EnterReturnsTrue | ImGuiInputTextFlags.AutoSelectAll;
        boolean committed = ImGui.inputText("##asset_rename_" + asset.guid(), buffer, flags);
        boolean deactivate = ImGui.isItemDeactivatedAfterEdit();
        if (committed || deactivate) {
            commitRename(asset, assets, actions);
        }
        if (ImGui.isKeyPressed(imgui.flag.ImGuiKey.Escape)) {
            cancel();
        }
        return true;
    }

    private void beginEditing(StudioAsset asset) {
        editingGuid = asset.guid();
        buffer.set(asset.friendlyDisplayName());
    }

    private void commitRename(StudioAsset asset, AssetDatabase assets, AssetEditorActions actions) {
        String newStem = buffer.get().trim();
        cancel();
        if (newStem.isBlank() || newStem.equals(asset.friendlyDisplayName())) {
            return;
        }
        actions.rename(asset, newStem);
        assets.refresh();
    }
}
