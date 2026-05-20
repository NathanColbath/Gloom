package org.llw.studio.editor.widgets;

import imgui.ImGui;
import org.llw.studio.editor.theme.EditorIcons;
import org.llw.studio.editor.theme.EditorStyle;

import java.util.HashMap;
import java.util.Map;

/**
 * Unity-style inspector component header with persisted open state, remove, and options affordances.
 */
public final class ComponentFoldout {
  private static final Map<String, Boolean> openStates = new HashMap<>();

  private ComponentFoldout() {}

  /** Result of {@link #header(String, String, String, boolean, boolean)}. */
  public static final class State {
    private final boolean open;
    private final boolean removeClicked;
    private final boolean optionsClicked;

    State(boolean open, boolean removeClicked, boolean optionsClicked) {
      this.open = open;
      this.removeClicked = removeClicked;
      this.optionsClicked = optionsClicked;
    }

    public boolean open() {
      return open;
    }

    public boolean removeClicked() {
      return removeClicked;
    }

    public boolean optionsClicked() {
      return optionsClicked;
    }
  }

  public static State header(String key, String title, String icon, boolean defaultOpen, boolean removable) {
    boolean open = openStates.getOrDefault(key, defaultOpen);
    float headerHeight = ImGui.getFrameHeight();
    float width = ImGui.getContentRegionAvailX();

    ImGui.pushID(key);
    EditorStyle.pushComponentHeader();

    float arrowWidth = headerHeight;
    if (ImGui.arrowButton("##foldout", open ? imgui.flag.ImGuiDir.Down : imgui.flag.ImGuiDir.Right)) {
      open = !open;
      openStates.put(key, open);
    }
    ImGui.sameLine();

    if (icon != null && !icon.isBlank()) {
      EditorStyle.pushMutedText();
      ImGui.text(icon);
      EditorStyle.popMutedText();
      ImGui.sameLine();
    }

    ImGui.alignTextToFramePadding();
    ImGui.textUnformatted(title);

    boolean removeClicked = false;
    boolean optionsClicked = false;
    float buttonWidth = 22f;
    float right = ImGui.getWindowContentRegionMaxX();

    if (removable) {
      float removeX = right - buttonWidth;
      ImGui.sameLine(removeX);
      if (ImGui.smallButton(EditorIcons.TIMES + "##remove")) {
        removeClicked = true;
      }
    }

    float optionsX = right - (removable ? buttonWidth * 2f + 4f : buttonWidth);
    ImGui.sameLine(optionsX);
    if (ImGui.smallButton(EditorIcons.ELLIPSIS + "##options")) {
      optionsClicked = true;
    }

    EditorStyle.popComponentHeader();
    ImGui.popID();

    openStates.put(key, open);
    return new State(open, removeClicked, optionsClicked);
  }

  public static void beginBody() {
    ImGui.indent(8f);
  }

  public static void endBody() {
    ImGui.unindent(8f);
    ImGui.spacing();
  }
}
