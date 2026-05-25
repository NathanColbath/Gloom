package org.llw.studio.editor.widgets;

import imgui.ImGui;
import imgui.flag.ImGuiSelectableFlags;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImString;
import org.llw.studio.editor.components.ComponentCatalog;
import org.llw.studio.editor.components.ComponentTypeInfo;
import org.llw.studio.editor.theme.EditorStyle;
import org.llw.studio.scene.GameObject;
import org.llw.studio.scripting.ScriptComponent;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Modal picker for adding components to a {@link GameObject}, grouped by category with search.
 */
public final class AddComponentDialog {
  public static final String POPUP_ID = "add_component_modal";

  private static final float POPUP_WIDTH = 400f;
  private static final float LIST_HEIGHT = 300f;

  private AddComponentDialog() {}

  public static void open() {
    ImGui.openPopup(POPUP_ID);
  }

  /**
   * @return true when a component was added this frame
   */
  public static boolean render(GameObject object, ComponentCatalog catalog, ImString filter) {
    ImGui.setNextWindowSize(POPUP_WIDTH, 0f);
    if (!ImGui.beginPopupModal(POPUP_ID, ImGuiWindowFlags.AlwaysAutoResize)) {
      return false;
    }

    ImGui.text("Add Component");
    EditorStyle.pushMutedText();
    ImGui.textWrapped("Search or browse by category. Components already on this object are shown disabled.");
    EditorStyle.popMutedText();
    ImGui.spacing();

    float searchWidth = ImGui.getContentRegionAvailX();
    SearchInput.render("##add_component_search", filter, "Search components...", searchWidth);
    ImGui.spacing();

    Map<String, List<ComponentTypeInfo>> grouped = groupVisible(catalog, filter.get());
    int available = countAddable(grouped, object);

    ImGui.beginChild("##add_component_list", 0f, LIST_HEIGHT, true);
    boolean added = false;
    if (grouped.isEmpty()) {
      EditorStyle.pushMutedText();
      ImGui.text("No matching components.");
      EditorStyle.popMutedText();
    } else {
      for (Map.Entry<String, List<ComponentTypeInfo>> entry : grouped.entrySet()) {
        renderCategoryLabel(entry.getKey());
        if (renderCategoryEntries(entry.getValue(), object)) {
          added = true;
          break;
        }
      }
    }
    ImGui.endChild();

    ImGui.spacing();
    EditorStyle.pushMutedText();
    ImGui.text(available + " available · " + grouped.size() + " categories");
    EditorStyle.popMutedText();
    ImGui.sameLine(ImGui.getContentRegionAvailX() - 64f);
    if (ImGui.button("Cancel", 64f, 0f)) {
      ImGui.closeCurrentPopup();
    }

    ImGui.endPopup();
    return added;
  }

  private static void renderCategoryLabel(String category) {
    ImGui.spacing();
    EditorStyle.pushMutedText();
    ImGui.text(category.toUpperCase(Locale.ROOT));
    EditorStyle.popMutedText();
  }

  private static boolean renderCategoryEntries(List<ComponentTypeInfo> items, GameObject object) {
    for (ComponentTypeInfo info : items) {
      if (renderEntry(info, object)) {
        return true;
      }
    }
    return false;
  }

  private static boolean renderEntry(ComponentTypeInfo info, GameObject object) {
    boolean onObject = object.hasComponent(info.type());
    boolean isScript = info.type() == ScriptComponent.class;
    boolean disabled = onObject && !isScript;

    if (disabled) {
      ImGui.beginDisabled();
    }

    String label = info.menuName();
    if (disabled) {
      label += "  (added)";
    } else if (isScript && onObject) {
      label += "  (+ attachment)";
    }

    boolean selected = ImGui.selectable(label, false, ImGuiSelectableFlags.None);
    if (ImGui.isItemHovered() && !disabled) {
      ImGui.setTooltip(info.category() + " · " + info.type().getSimpleName());
    }

    if (disabled) {
      ImGui.endDisabled();
    }

    if (!selected || disabled) {
      return false;
    }

    if (isScript) {
      ScriptComponent scripts = object.getComponent(ScriptComponent.class);
      if (scripts == null) {
        scripts = new ScriptComponent();
        object.addComponent(ScriptComponent.class, scripts);
      }
      scripts.addAttachment();
    } else {
      @SuppressWarnings("unchecked")
      Object created = info.defaultFactory().get();
      object.addComponent((Class<Object>) info.type(), created);
    }
    ImGui.closeCurrentPopup();
    return true;
  }

  private static Map<String, List<ComponentTypeInfo>> groupVisible(ComponentCatalog catalog, String filterRaw) {
    String filter = filterRaw == null ? "" : filterRaw.trim().toLowerCase(Locale.ROOT);
    Map<String, List<ComponentTypeInfo>> grouped = new LinkedHashMap<>();
    for (ComponentTypeInfo info : catalog.addable()) {
      if (!matchesFilter(info, filter)) {
        continue;
      }
      grouped.computeIfAbsent(info.category(), k -> new ArrayList<>()).add(info);
    }
    return grouped;
  }

  private static boolean matchesFilter(ComponentTypeInfo info, String filter) {
    if (filter.isEmpty()) {
      return true;
    }
    return info.menuName().toLowerCase(Locale.ROOT).contains(filter)
        || info.category().toLowerCase(Locale.ROOT).contains(filter)
        || info.type().getSimpleName().toLowerCase(Locale.ROOT).contains(filter);
  }

  private static int countAddable(Map<String, List<ComponentTypeInfo>> grouped, GameObject object) {
    int count = 0;
    for (List<ComponentTypeInfo> list : grouped.values()) {
      for (ComponentTypeInfo info : list) {
        if (info.type() == ScriptComponent.class || !object.hasComponent(info.type())) {
          count++;
        }
      }
    }
    return count;
  }
}
