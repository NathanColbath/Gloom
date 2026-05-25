package org.llw.studio.editor.shell;

import imgui.ImGui;
import org.llw.studio.editor.theme.EditorStyle;

import java.awt.Desktop;
import java.nio.file.Path;

/**
 * Main menu bar (File, Edit, Assets, View) rendered each editor frame.
 *
 * <p>Implementation note: Call from {@link EditorShell#render()} before dock space; uses {@code ImGui.beginMainMenuBar}.
 */
public final class AppMenuBar {
  private final EditorMenuActions actions;

  /**
   * @param actions menu command handler
   */
  public AppMenuBar(EditorMenuActions actions) {
    this.actions = actions;
  }

  /** Draws the menu bar for the current ImGui frame. */
  public void render() {
    if (!ImGui.beginMainMenuBar()) {
      return;
    }
    if (ImGui.beginMenu("File")) {
      if (ImGui.menuItem("New Project...")) {
        actions.newProject();
      }
      if (ImGui.menuItem("Open Project...")) {
        actions.openProject();
      }
      ImGui.separator();
      if (ImGui.menuItem("Save Scene")) {
        actions.saveScene();
      }
      if (ImGui.menuItem("Save Project")) {
        actions.saveProject();
      }
      ImGui.separator();
      boolean buildingPlayer = actions.isBuildingPlayer();
      if (buildingPlayer) {
        ImGui.beginDisabled();
      }
      if (ImGui.menuItem("Build Settings...") && !buildingPlayer) {
        actions.buildSettings();
      }
      if (ImGui.menuItem("Build Player") && !buildingPlayer) {
        actions.buildPlayer();
      }
      if (buildingPlayer) {
        ImGui.endDisabled();
      }
      ImGui.separator();
      if (ImGui.menuItem("Exit")) {
        actions.exit();
      }
      ImGui.endMenu();
    }
    if (ImGui.beginMenu("Edit")) {
      if (!actions.canUndo()) {
        ImGui.beginDisabled();
      }
      if (ImGui.menuItem("Undo")) {
        actions.undo();
      }
      if (!actions.canUndo()) {
        ImGui.endDisabled();
      }
      if (!actions.canRedo()) {
        ImGui.beginDisabled();
      }
      if (ImGui.menuItem("Redo")) {
        actions.redo();
      }
      if (!actions.canRedo()) {
        ImGui.endDisabled();
      }
      ImGui.endMenu();
    }
    if (ImGui.beginMenu("Assets")) {
      if (ImGui.menuItem("Create Script")) {
        actions.createScript();
      }
      if (ImGui.menuItem("Create Animation")) {
        actions.createAnimation();
      }
      if (ImGui.menuItem("Create Animation Clip")) {
        actions.createAnimationClip();
      }
      if (ImGui.menuItem("Create Shader Graph")) {
        actions.createShaderGraph();
      }
      if (ImGui.menuItem("Create Particle System")) {
        actions.createParticleSystem();
      }
      if (ImGui.menuItem("Refresh Scripts")) {
        actions.refreshScripts();
      }
      ImGui.endMenu();
    }
    if (ImGui.beginMenu("View")) {
      if (ImGui.menuItem("Animation", "", actions.isAnimationPanelOpen())) {
        actions.toggleAnimationPanel();
      }
      if (ImGui.menuItem("Tile Palette", "", actions.isTilePalettePanelOpen())) {
        actions.toggleTilePalettePanel();
      }
      if (ImGui.menuItem("Shader Graph", "", actions.isShaderGraphPanelOpen())) {
        actions.toggleShaderGraphPanel();
      }
      if (ImGui.menuItem("Particles", "", actions.isParticlePanelOpen())) {
        actions.toggleParticlePanel();
      }
      ImGui.separator();
      if (ImGui.menuItem("Frame Scene")) {
        actions.frameScene();
      }
      if (ImGui.menuItem("Reset Layout")) {
        actions.resetLayout();
      }
      ImGui.endMenu();
    }

    String projectName = actions.projectName();
    if (!projectName.isBlank()) {
      float textWidth = ImGui.calcTextSize(projectName).x;
      // Right-align project label in menu bar without a dedicated child window.
      float right = ImGui.getWindowContentRegionMaxX() - textWidth - 12f;
      if (right > ImGui.getCursorPosX()) {
        ImGui.sameLine(right);
      }
      EditorStyle.pushMutedText();
      ImGui.text(projectName);
      EditorStyle.popMutedText();
    }

    ImGui.endMainMenuBar();
  }

  /**
   * Opens a script source file in the desktop default editor.
   *
   * @param path file to open
   */
  public static void openScriptFile(Path path) {
    try {
      if (Desktop.isDesktopSupported()) {
        Desktop.getDesktop().open(path.toFile());
      }
    } catch (Exception ignored) {
    }
  }
}
