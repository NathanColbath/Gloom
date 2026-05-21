package org.llw.studio.editor.shell;

import imgui.internal.flag.ImGuiAxis;
import imgui.type.ImInt;

/**
 * Builds the default ImGui dock split tree for studio panels.
 */
final class DockLayout {
  private DockLayout() {}

  /**
   * @param dockspaceId root dock space id from the shell host window
   * @param width       viewport work width in screen pixels
   * @param height      viewport work height in screen pixels
   */
  static void buildDefault(int dockspaceId, float width, float height) {
    imgui.internal.ImGui.dockBuilderRemoveNode(dockspaceId);
    imgui.internal.ImGui.dockBuilderAddNode(
        dockspaceId,
        imgui.internal.flag.ImGuiDockNodeFlags.DockSpace
    );
    imgui.internal.ImGui.dockBuilderSetNodeSize(dockspaceId, width, height);

    ImInt hierarchy = new ImInt();
    ImInt centerRight = new ImInt();
    imgui.internal.ImGui.dockBuilderSplitNode(dockspaceId, ImGuiAxis.X, 0.20f, hierarchy, centerRight);

    ImInt center = new ImInt();
    ImInt inspector = new ImInt();
    imgui.internal.ImGui.dockBuilderSplitNode(centerRight.get(), ImGuiAxis.X, 0.75f, center, inspector);

    ImInt viewStack = new ImInt();
    ImInt bottom = new ImInt();
    imgui.internal.ImGui.dockBuilderSplitNode(center.get(), ImGuiAxis.Y, 0.72f, viewStack, bottom);

    ImInt toolbar = new ImInt();
    ImInt sceneGame = new ImInt();
    imgui.internal.ImGui.dockBuilderSplitNode(viewStack.get(), ImGuiAxis.Y, 0.10f, toolbar, sceneGame);

    ImInt animation = new ImInt();
    ImInt projectConsole = new ImInt();
    imgui.internal.ImGui.dockBuilderSplitNode(bottom.get(), ImGuiAxis.Y, 0.55f, animation, projectConsole);

    ImInt project = new ImInt();
    ImInt console = new ImInt();
    imgui.internal.ImGui.dockBuilderSplitNode(projectConsole.get(), ImGuiAxis.X, 0.55f, project, console);

    imgui.internal.ImGui.dockBuilderDockWindow("Hierarchy", hierarchy.get());
    imgui.internal.ImGui.dockBuilderDockWindow("Toolbar", toolbar.get());
    imgui.internal.ImGui.dockBuilderDockWindow("Scene", sceneGame.get());
    imgui.internal.ImGui.dockBuilderDockWindow("Game", sceneGame.get());
    imgui.internal.ImGui.dockBuilderDockWindow("Animation", animation.get());
    imgui.internal.ImGui.dockBuilderDockWindow("Project", project.get());
    imgui.internal.ImGui.dockBuilderDockWindow("Console", console.get());
    imgui.internal.ImGui.dockBuilderDockWindow("Inspector", inspector.get());
    imgui.internal.ImGui.dockBuilderDockWindow("Tile Palette", inspector.get());
    imgui.internal.ImGui.dockBuilderDockWindow("Shader Graph", animation.get());
    imgui.internal.ImGui.dockBuilderDockWindow("Particles", animation.get());

    imgui.internal.ImGui.dockBuilderFinish(dockspaceId);
  }
}
