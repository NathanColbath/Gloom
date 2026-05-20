package org.llw.studio.editor.panels;

import imgui.ImGui;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiDragDropFlags;
import imgui.flag.ImGuiWindowFlags;
import org.llw.render.graphics.Texture2d;
import org.llw.render.window.Window;
import org.llw.studio.assets.AssetDatabase;
import org.llw.studio.assets.AssetPreviewCache;
import org.llw.studio.assets.AssetType;
import org.llw.studio.assets.StudioAsset;
import org.llw.studio.editor.EditorDragDrop;
import org.llw.studio.editor.SelectionService;
import org.llw.studio.editor.StudioContext;
import org.llw.studio.editor.assets.AssetBrowserNest;
import org.llw.studio.editor.assets.AssetBrowserNest.NestChild;
import org.llw.studio.editor.assets.AssetBrowserRename;
import org.llw.studio.editor.assets.AssetEditorActions;
import org.llw.studio.editor.assets.CreateFolderDialog;
import org.llw.studio.editor.assets.AssetIconKind;
import org.llw.studio.editor.assets.EditorIconRegistry;
import org.llw.studio.editor.shell.AppMenuBar;
import org.llw.studio.editor.shell.EditorMenuActions;
import org.llw.studio.editor.theme.EditorColors;
import org.llw.studio.editor.widgets.AssetBrowserItemInteractions;
import org.llw.studio.editor.widgets.AssetBrowserFolderDropState;
import org.llw.studio.editor.widgets.AssetDropTargets;
import org.llw.studio.editor.widgets.AssetGrid;
import org.llw.studio.editor.widgets.PanelHeader;
import org.llw.studio.editor.widgets.SpriteSlicePreview;
import org.llw.studio.editor.prefab.PrefabEditorActions;
import org.llw.studio.editor.widgets.ToolButton;
import org.llw.studio.ecs.EntityId;
import org.lwjgl.glfw.GLFW;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Project asset browser (list/grid), folder navigation, and asset file operations.
 */
public final class ProjectPanel implements EditorPanel {
  private static final float LIST_ICON_SIZE = 18f;

  private final Window window;
  private final AssetDatabase assets;
  private final AssetPreviewCache previews;
  private final EditorIconRegistry icons;
  private final SelectionService selection;
  private final EditorMenuActions menuActions;
  private final AssetEditorActions assetActions;
  private final ShaderGraphPanel shaderGraphPanel;
  private boolean gridMode = true;
  private String currentFolderGuid;
  private StudioAsset pendingDeleteAsset;
  private final AssetBrowserRename rename = new AssetBrowserRename();
  private final CreateFolderDialog createFolderDialog = new CreateFolderDialog();
  private final Map<String, Boolean> expandedByGuid = new HashMap<>();
  private String createFolderError;

  public ProjectPanel(
      Window window,
      AssetDatabase assets,
      AssetPreviewCache previews,
      EditorIconRegistry icons,
      SelectionService selection,
      EditorMenuActions menuActions,
      AssetEditorActions assetActions,
      ShaderGraphPanel shaderGraphPanel
  ) {
    this.window = window;
    this.assets = assets;
    this.previews = previews;
    this.icons = icons;
    this.selection = selection;
    this.menuActions = menuActions;
    this.assetActions = assetActions;
    this.shaderGraphPanel = shaderGraphPanel;
  }

  @Override
  public String id() {
    return "project";
  }

  @Override
  public String title() {
    return "Project";
  }

  @Override
  public void render(StudioContext context) {
    if (!ImGui.begin(title())) {
      ImGui.end();
      return;
    }
    if (currentFolderGuid == null) {
      currentFolderGuid = assets.rootGuid();
    }
    PanelHeader.begin();
    if (ToolButton.toggle("Grid", gridMode, 44f)) {
      gridMode = true;
    }
    ImGui.sameLine();
    if (ToolButton.toggle("List", !gridMode, 44f)) {
      gridMode = false;
    }
    ImGui.sameLine();
    if (ImGui.button("New Folder", 80f, 0f)) {
      openCreateFolderDialog(currentFolderGuid == null ? assets.rootGuid() : currentFolderGuid);
    }
    ImGui.sameLine();
    if (ImGui.button("Refresh", 64f, 0f)) {
      assets.refresh();
      previews.clear();
      currentFolderGuid = assets.rootGuid();
    }
    PanelHeader.end();

    if (gridMode) {
      renderGrid(context);
    } else {
      renderTree(context);
    }
    acceptEntityDropOnCurrentFolder(context);
    renderPanelContextMenu(context);
    handleKeyboardShortcuts(context);
    handleExternalFileDrop(context);
    renderDeleteConfirmModal();
    renderCreateFolderDialog();
    if (createFolderError != null) {
      ImGui.textColored(1f, 0.35f, 0.35f, 1f, createFolderError);
      createFolderError = null;
    }
    ImGui.end();
  }

  private void handleExternalFileDrop(StudioContext context) {
    if (context != null && context.isPlaying()) {
      window.takeDroppedPaths();
      return;
    }
    List<Path> dropped = window.takeDroppedPaths();
    if (dropped.isEmpty() || !ImGui.isWindowHovered()) {
      return;
    }
    String folderGuid = currentFolderGuid == null ? assets.rootGuid() : currentFolderGuid;
    assetActions.importExternalFiles(dropped, folderGuid);
    previews.clear();
  }

  private void renderTree(StudioContext context) {
    AssetBrowserFolderDropState.beginFrame();
    if (currentFolderGuid == null) {
      return;
    }
    if (!currentFolderGuid.equals(assets.rootGuid())) {
      if (ImGui.selectable("../")) {
        currentFolderGuid = assets.parentFolderGuid(currentFolderGuid);
      }
    }
    for (StudioAsset asset : AssetBrowserNest.childrenOf(assets, currentFolderGuid)) {
      renderNestableListRow(context, asset);
    }
    AssetBrowserFolderDropState.submitTargets(context, selection, assets, this::onAssetsChanged);
  }

  private void renderNestableListRow(StudioContext context, StudioAsset asset) {
    AssetBrowserNest.NestableEntry nest = AssetBrowserNest.entryFor(assets, asset);
    ImGui.pushID(asset.guid());
    boolean expanded = Boolean.TRUE.equals(expandedByGuid.get(asset.guid()));
    if (nest.expandable()) {
      if (renderListChevron(asset.guid(), expanded)) {
        expandedByGuid.put(asset.guid(), !expanded);
        expanded = !expanded;
      }
      ImGui.sameLine(0f, 4f);
    }
    renderAssetRow(context, asset, null, EditorIconRegistry.kindFor(asset));
    if (nest.expandable() && expanded) {
      for (NestChild child : nest.children()) {
        ImGui.indent(24f);
        renderNestChildListRow(context, child);
        ImGui.unindent(24f);
      }
    }
    ImGui.popID();
  }

  private void renderNestChildListRow(StudioContext context, NestChild child) {
    renderAssetRow(context, child.asset(), child.displayLabel(), child.iconKind());
  }

  private boolean renderListChevron(String guid, boolean expanded) {
    ImGui.pushID("chev_" + guid);
    AssetIconKind kind = expanded ? AssetIconKind.CHEVRON_DOWN : AssetIconKind.CHEVRON_RIGHT;
    boolean clicked = icons.imageButton(kind, LIST_ICON_SIZE);
    if (!clicked && icons.icon(kind) == null) {
      clicked = ImGui.button(expanded ? "v" : ">", LIST_ICON_SIZE, LIST_ICON_SIZE);
    }
    ImGui.popID();
    return clicked;
  }

  private void renderGrid(StudioContext context) {
    String folderGuid = currentFolderGuid == null ? assets.rootGuid() : currentFolderGuid;
    AssetBrowserItemInteractions.Context interactions = browserInteractions(context, folderGuid);
    AssetGrid.render(
            assets,
            previews,
            folderGuid,
            asset -> selectAsset(asset),
            asset -> showAssetInfo(asset.guid()),
            this::createScriptInFolder,
            context,
            selection,
            assetActions,
            this::requestDeleteConfirm,
            rename,
            icons,
            expandedByGuid,
            interactions
    );
  }

  /**
   * Accepts hierarchy entity drags on empty project-browser space and the current folder path.
   */
  private void acceptEntityDropOnCurrentFolder(StudioContext context) {
    if (context == null || context.isPlaying()) {
      return;
    }
    Path folder = currentFolderPath();
    if (folder == null) {
      return;
    }
    float width = Math.max(ImGui.getContentRegionAvailX(), 1f);
    float height = Math.max(ImGui.getContentRegionAvailY(), 1f);
    ImGui.invisibleButton("##project_entity_drop", width, height);
    if (!ImGui.beginDragDropTarget()) {
      return;
    }
    String entityPayload = ImGui.acceptDragDropPayload(
            SelectionService.PAYLOAD_ENTITY,
            ImGuiDragDropFlags.AcceptNoDrawDefaultRect,
            String.class
    );
    if (entityPayload != null) {
      EntityId entity = PrefabEditorActions.parseEntityPayload(entityPayload);
      String prefabGuid = PrefabEditorActions.trySavePrefabFromEntity(context, assets, entity, folder);
      if (prefabGuid != null) {
        onAssetsChanged();
      }
    }
    ImGui.endDragDropTarget();
  }

  private Path currentFolderPath() {
    String folderGuid = currentFolderGuid == null ? assets.rootGuid() : currentFolderGuid;
    StudioAsset folder = assets.get(folderGuid);
    return folder == null ? null : folder.path();
  }

  private void selectAsset(StudioAsset asset) {
    if (EditorDragDrop.shouldSuppressSelectionChange()) {
      return;
    }
    assets.select(asset.guid());
    assets.showInfo(asset.guid());
    selection.clear();
  }

  private AssetBrowserItemInteractions.Context browserInteractions(StudioContext context, String folderGuid) {
    return new AssetBrowserItemInteractions.Context(
            context,
            selection,
            assets,
            assetActions,
            folderGuid,
            asset -> showAssetInfo(asset.guid()),
            this::createScriptInFolder,
            folder -> openCreateFolderDialog(folder.guid()),
            this::requestDeleteConfirm,
            folder -> currentFolderGuid = folder.guid(),
            path -> loadScene(context, path),
            AppMenuBar::openScriptFile,
            asset -> {
              if (shaderGraphPanel != null) {
                shaderGraphPanel.openAsset(asset.guid(), asset.path());
              }
            },
            this::onAssetsChanged
    );
  }

  private void onAssetsChanged() {
    previews.clear();
  }

  private void openCreateFolderDialog(String parentFolderGuid) {
    createFolderDialog.open(parentFolderGuid);
  }

  private void renderCreateFolderDialog() {
    createFolderDialog.render(assetActions, message -> createFolderError = message);
  }

  private void showAssetInfo(String guid) {
    if (EditorDragDrop.shouldSuppressSelectionChange()) {
      return;
    }
    assets.showInfo(guid);
    selection.clear();
  }

  private void renderPanelContextMenu(StudioContext context) {
    if (ImGui.beginPopupContextWindow("project_panel_ctx", 1)) {
      boolean blocked = context != null && context.isPlaying();
      if (blocked) {
        ImGui.beginDisabled();
      }
      if (!assetActions.clipboard().canPaste()) {
        ImGui.beginDisabled();
      }
      if (ImGui.menuItem("Paste")) {
        String folderGuid = currentFolderGuid == null ? assets.rootGuid() : currentFolderGuid;
        assetActions.pasteIntoFolder(folderGuid);
      }
      if (!assetActions.clipboard().canPaste()) {
        ImGui.endDisabled();
      }
      if (blocked) {
        ImGui.endDisabled();
      }
      if (ImGui.menuItem("Create Folder")) {
        openCreateFolderDialog(currentFolderGuid == null ? assets.rootGuid() : currentFolderGuid);
      }
      if (ImGui.menuItem("Create Script")) {
        createScriptInCurrentFolder();
      }
      if (ImGui.menuItem("Create Shader Graph")) {
        Path folder = null;
        if (currentFolderGuid != null) {
          StudioAsset folderAsset = assets.get(currentFolderGuid);
          if (folderAsset != null && folderAsset.isFolder()) {
            folder = folderAsset.path();
          }
        }
        menuActions.createShaderGraphInFolder(folder);
      }
      ImGui.endPopup();
    }
  }

  private void handleKeyboardShortcuts(StudioContext context) {
    if (context != null && context.isPlaying()) {
      return;
    }
    if (!ImGui.isWindowFocused(ImGuiWindowFlags.None)) {
      return;
    }
    boolean ctrl = ImGui.getIO().getKeyCtrl();
    StudioAsset selected = assets.selected();
    if (ctrl && ImGui.isKeyPressed(GLFW.GLFW_KEY_C)) {
      assetActions.copySelected();
    }
    if (ctrl && ImGui.isKeyPressed(GLFW.GLFW_KEY_X)) {
      assetActions.cutSelected();
    }
    if (ctrl && ImGui.isKeyPressed(GLFW.GLFW_KEY_V)) {
      String folderGuid = currentFolderGuid == null ? assets.rootGuid() : currentFolderGuid;
      assetActions.pasteIntoFolder(folderGuid);
    }
    if (ctrl && ImGui.isKeyPressed(GLFW.GLFW_KEY_D)) {
      assetActions.duplicateSelected();
    }
    if (ImGui.isKeyPressed(GLFW.GLFW_KEY_DELETE) && selected != null) {
      requestDeleteConfirm(selected);
    }
  }

  private void requestDeleteConfirm(StudioAsset asset) {
    if (asset == null || assetActions.isRootAsset(asset)) {
      return;
    }
    pendingDeleteAsset = asset;
    ImGui.openPopup("confirm_asset_delete");
  }

  private void renderDeleteConfirmModal() {
    if (pendingDeleteAsset == null) {
      return;
    }
    ImGui.setNextWindowSize(360f, 0f);
    if (ImGui.beginPopupModal("confirm_asset_delete", ImGuiWindowFlags.AlwaysAutoResize)) {
      ImGui.textWrapped("Delete \"" + pendingDeleteAsset.friendlyDisplayName() + "\"?");
      ImGui.textWrapped("This cannot be undone.");
      ImGui.separator();
      if (ImGui.button("Delete", 100f, 0f)) {
        assetActions.delete(pendingDeleteAsset);
        pendingDeleteAsset = null;
        ImGui.closeCurrentPopup();
      }
      ImGui.sameLine();
      if (ImGui.button("Cancel", 100f, 0f)) {
        pendingDeleteAsset = null;
        ImGui.closeCurrentPopup();
      }
      ImGui.endPopup();
    }
  }

  private void createScriptInFolder(StudioAsset folder) {
    if (menuActions == null || folder == null || !folder.isFolder()) {
      return;
    }
    menuActions.createScriptInFolder(folder.path());
    currentFolderGuid = folder.guid();
  }

  private void createScriptInCurrentFolder() {
    if (menuActions == null) {
      return;
    }
    StudioAsset folder = assets.get(currentFolderGuid);
    menuActions.createScriptInFolder(folder == null ? null : folder.path());
  }

  private void renderAssetRow(
      StudioContext context,
      StudioAsset asset,
      String displayLabel,
      AssetIconKind iconKind
  ) {
    ImGui.pushID(asset.guid());
    if (!asset.isFolder() && rename.isEditing(asset.guid())) {
      rename.renderInline(asset, assets, assetActions);
      ImGui.popID();
      return;
    }
    drawListThumbnailOrIcon(asset, iconKind);
    ImGui.sameLine(0f, 6f);
    String label = displayLabel != null
            ? displayLabel
            : (asset.isFolder() ? asset.displayName() : asset.friendlyDisplayName());
    boolean selected = assets.selected() == asset;
    if (selected) {
      ImGui.pushStyleColor(ImGuiCol.Header, EditorColors.SELECTION_BG[0], EditorColors.SELECTION_BG[1],
              EditorColors.SELECTION_BG[2], EditorColors.SELECTION_BG[3]);
      ImGui.pushStyleColor(ImGuiCol.HeaderHovered, EditorColors.SELECTION_BG[0], EditorColors.SELECTION_BG[1],
              EditorColors.SELECTION_BG[2], EditorColors.SELECTION_BG[3]);
    } else {
      ImGui.pushStyleColor(ImGuiCol.HeaderHovered, EditorColors.ASSET_BROWSER_HOVER[0],
              EditorColors.ASSET_BROWSER_HOVER[1], EditorColors.ASSET_BROWSER_HOVER[2],
              EditorColors.ASSET_BROWSER_HOVER[3]);
    }
    float avail = ImGui.getContentRegionAvailX();
    if (ImGui.selectable(label + "##sel", selected, 0, avail, LIST_ICON_SIZE)) {
      boolean wasSelected = selected;
      selectAsset(asset);
      if (!asset.isFolder()) {
        rename.onAssetClicked(asset, wasSelected);
      }
    }
    if (selected) {
      ImGui.popStyleColor(2);
    } else {
      ImGui.popStyleColor();
    }
    AssetBrowserItemInteractions.Context interactions =
            browserInteractions(context, currentFolderGuid == null ? assets.rootGuid() : currentFolderGuid);
    AssetBrowserItemInteractions.attach(asset, interactions, false);
    if (asset.isFolder()) {
      var min = ImGui.getItemRectMin();
      var max = ImGui.getItemRectMax();
      AssetBrowserFolderDropState.register(asset.path(), min.x, min.y, max.x, max.y);
    }
    ImGui.popID();
  }

  private void drawListThumbnailOrIcon(StudioAsset asset, AssetIconKind iconKind) {
    if (asset.type() == AssetType.TEXTURE) {
      Texture2d texture = previews.preview(asset.guid());
      if (texture != null) {
        ImGui.image(texture.id(), LIST_ICON_SIZE, LIST_ICON_SIZE, 0f, 1f, 1f, 0f);
        return;
      }
    } else if (asset.type() == AssetType.SPRITE) {
      if (SpriteSlicePreview.drawThumb(assets, previews, asset.guid(), LIST_ICON_SIZE)) {
        return;
      }
    }
    AssetIconKind kind = iconKind != null ? iconKind : EditorIconRegistry.kindFor(asset);
    if (!icons.draw(kind, LIST_ICON_SIZE)) {
      ImGui.dummy(LIST_ICON_SIZE, LIST_ICON_SIZE);
    }
  }

  private void loadScene(StudioContext context, Path path) {
    try {
      context.setEditScene(org.llw.studio.serialization.SceneSerializer.load(path));
    } catch (Exception ignored) {
    }
  }
}
