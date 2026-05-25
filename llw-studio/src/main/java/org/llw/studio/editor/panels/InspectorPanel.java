package org.llw.studio.editor.panels;

import imgui.ImGui;
import imgui.type.ImString;
import org.llw.studio.assets.AssetDatabase;
import org.llw.studio.assets.AssetPreviewCache;
import org.llw.studio.assets.StudioAsset;
import org.llw.studio.editor.EditorDragDrop;
import org.llw.studio.editor.EditorSession;
import org.llw.studio.editor.SelectionService;
import org.llw.studio.editor.StudioContext;
import org.llw.studio.editor.commands.UndoStack;
import org.llw.studio.editor.components.ComponentCatalog;
import org.llw.studio.editor.components.ComponentDrawer;
import org.llw.studio.editor.components.ComponentTypeInfo;
import org.llw.studio.editor.components.InspectorContext;
import org.llw.studio.editor.theme.EditorIcons;
import org.llw.studio.editor.widgets.ComponentFoldout;
import org.llw.studio.editor.widgets.EmptyState;
import org.llw.studio.editor.widgets.InspectorChrome;
import org.llw.studio.editor.widgets.InspectorObjectHeader;
import org.llw.studio.ecs.EntityId;
import org.llw.studio.ecs.components.ActiveComponent;
import org.llw.studio.ecs.components.NameComponent;
import org.llw.studio.ecs.components.Transform2DComponent;
import org.llw.studio.scripting.ScriptComponent;
import org.llw.studio.scene.GameObject;

/**
 * Entity and asset inspector with component drawers and add-component menu.
 */
public final class InspectorPanel implements EditorPanel {
  private static final float ADD_COMPONENT_FOOTER_HEIGHT = 32f;

  private final SelectionService selection;
  private final UndoStack undoStack;
  private final AssetDatabase assets;
  private final AssetPreviewCache previews;
  private final ComponentCatalog catalog;
  private final AssetInspectorPanel assetInspector;
  private final InspectorContext inspectorContext;
  private final ImString nameBuffer = new ImString("", 128);
  private final ImString tagBuffer = new ImString("", 128);
  private final ImString addFilter = new ImString("", 128);
  private EntityId lastRenderedEntity = EntityId.none();

  /**
   * @param selection      entity selection
   * @param undoStack      undo for component adds
   * @param assets         asset database
   * @param previews       thumbnail cache for asset inspector
   * @param catalog        component type registry
   * @param editorSession  session wired into {@link InspectorContext}
   */
  public InspectorPanel(
      SelectionService selection,
      UndoStack undoStack,
      AssetDatabase assets,
      AssetPreviewCache previews,
      ComponentCatalog catalog,
      EditorSession editorSession
  ) {
    this.selection = selection;
    this.undoStack = undoStack;
    this.assets = assets;
    this.previews = previews;
    this.catalog = catalog;
    this.inspectorContext = new InspectorContext(assets, undoStack, selection, editorSession);
    this.assetInspector = new AssetInspectorPanel(assets, previews, catalog, inspectorContext);
  }

  /** {@inheritDoc} */
  @Override
  public String id() {
    return "inspector";
  }

  /** {@inheritDoc} */
  @Override
  public String title() {
    return "Inspector";
  }

  /** {@inheritDoc} */
  @Override
  public void render(StudioContext context) {
    if (!ImGui.begin(title())) {
      ImGui.end();
      return;
    }
    // Inspector follows drag-pinned entity, not live selection, during hierarchy drags.
    EntityId selected = EditorDragDrop.inspectorEntity(selection.selected());
    if (selected.isNone()) {
      StudioAsset asset = assets.selected() != null ? assets.selected() : assets.infoTarget();
      if (asset == null) {
        EmptyState.render("No object selected");
      } else {
        assetInspector.render(context);
      }
      ImGui.end();
      return;
    }
    assets.clearSelection();
    assets.clearInfo();
    inspectorContext.setStudioContext(context);
    // activeScene() returns play clone while running; inspector edits follow that scene.
    GameObject object = context.activeScene().find(selected);
    if (object == null) {
      EmptyState.render("Invalid selection");
      ImGui.end();
      return;
    }

    float scrollHeight = ImGui.getContentRegionAvailY() - ADD_COMPONENT_FOOTER_HEIGHT;
    ImGui.beginChild("##inspector_scroll", 0f, scrollHeight, false);
    InspectorChrome.beginScrollRegion();
    ComponentFoldout.resetCardSpacing();
    renderObjectInspector(object, context);
    InspectorChrome.endScrollRegion();
    ImGui.endChild();

    if (ImGui.button("Add Component", -1f, 0f) && !context.isPlaying()) {
      ImGui.openPopup("add_component_popup");
    }
    renderAddComponentPopup(object);

    ImGui.end();
  }

  private void renderObjectInspector(GameObject object, StudioContext context) {
    EntityId entity = object.entity();
    // Rebind name/tag buffers only when selection changes to avoid clobbering in-progress edits.
    if (!entity.equals(lastRenderedEntity)) {
      lastRenderedEntity = entity;
      NameComponent identity = object.getComponent(NameComponent.class);
      nameBuffer.set(identity == null ? "GameObject" : identity.name());
      tagBuffer.set(identity == null ? "" : identity.tag());
    }

    ActiveComponent active = object.getComponent(ActiveComponent.class);
    boolean activeValue = active != null && active.selfActive;
    activeValue = InspectorObjectHeader.render(nameBuffer, tagBuffer, activeValue, active != null);
    object.setName(nameBuffer.get());
    object.setTag(tagBuffer.get());
    if (active != null) {
      active.selfActive = activeValue;
    }

    String entityKey = entity.index() + ":" + entity.generation();

    var transformInfo = catalog.get(Transform2DComponent.class);
    Transform2DComponent transform = object.transform();
    if (transformInfo != null) {
      String key = entityKey + ":transform";
      ComponentFoldout.State state = ComponentFoldout.header(key, "Transform", EditorIcons.COMPONENT, true, false);
      if (state.open() && !state.removeClicked()) {
        ComponentFoldout.beginBody();
        drawDrawer(transformInfo, transform);
        ComponentFoldout.endBody();
      }
      ComponentFoldout.endComponent();
    }

    for (ComponentTypeInfo info : catalog.all()) {
      if (info.hiddenInInspector() || info.type() == Transform2DComponent.class) {
        continue;
      }
      if (!object.hasComponent(info.type())) {
        continue;
      }
      Object component = object.getComponent(info.type());
      if (component == null) {
        continue;
      }
      // Script attachments render inline without foldout remove — attachments are managed per row.
      if (info.type() == ScriptComponent.class) {
        drawDrawer(info, component);
        continue;
      }
      String key = entityKey + ":" + info.type().getSimpleName();
      ComponentFoldout.State state = ComponentFoldout.header(key, info.menuName(), EditorIcons.COMPONENT, true, !context.isPlaying());
      if (state.removeClicked()) {
        object.removeComponent(info.type());
        ComponentFoldout.endComponent();
        continue;
      }
      if (state.open()) {
        ComponentFoldout.beginBody();
        drawDrawer(info, component);
        ComponentFoldout.endBody();
      }
      ComponentFoldout.endComponent();
    }
  }

  private void renderAddComponentPopup(GameObject object) {
    if (ImGui.beginPopup("add_component_popup")) {
      ImGui.inputText("Filter", addFilter);
      for (ComponentTypeInfo info : catalog.addable()) {
        if (!addFilter.get().isBlank() && !info.menuName().toLowerCase().contains(addFilter.get().toLowerCase())) {
          continue;
        }
        if (info.type() == ScriptComponent.class) {
          if (ImGui.selectable(info.menuName())) {
            ScriptComponent scripts = object.getComponent(ScriptComponent.class);
            if (scripts == null) {
              scripts = new ScriptComponent();
              object.addComponent(ScriptComponent.class, scripts);
            }
            scripts.addAttachment();
            ImGui.closeCurrentPopup();
          }
          continue;
        }
        if (object.hasComponent(info.type())) {
          continue;
        }
        if (ImGui.selectable(info.menuName())) {
          @SuppressWarnings("unchecked")
          Object created = info.defaultFactory().get();
          object.addComponent((Class<Object>) info.type(), created);
          ImGui.closeCurrentPopup();
        }
      }
      ImGui.endPopup();
    }
  }

  @SuppressWarnings("unchecked")
  private void drawDrawer(ComponentTypeInfo info, Object component) {
    ComponentDrawer<Object> drawer = (ComponentDrawer<Object>) info.drawer();
    drawer.draw(component, inspectorContext);
  }
}
