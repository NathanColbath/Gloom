package org.llw.studio.editor.panels;

import imgui.ImGui;
import imgui.type.ImInt;
import org.llw.render.core.Color;
import org.llw.render.core.IntSize;
import org.llw.render.backend.RenderBackend;
import org.llw.render.graphics.OffscreenTarget;
import org.llw.render.graphics.Texture2d;
import org.llw.studio.assets.AssetDatabase;
import org.llw.studio.editor.EditorSession;
import org.llw.studio.editor.SelectionService;
import org.llw.studio.editor.StudioContext;
import org.llw.studio.editor.commands.UndoStack;
import org.llw.studio.editor.theme.EditorColors;
import org.llw.studio.editor.theme.EditorStyle;
import org.llw.studio.editor.ui.UiEditorInput;
import org.llw.studio.editor.ui.UiEditorPointer;
import org.llw.studio.editor.ui.UiEditorState;
import org.llw.studio.editor.ui.UiWidgetGizmo;
import org.llw.studio.ecs.EntityId;
import org.llw.studio.ecs.components.UICanvasComponent;
import org.llw.studio.render.UiDrawPass;
import org.llw.studio.scene.Scene;
import org.llw.studio.ui.UiCanvasRenderMode;
import org.llw.studio.ui.UiLayout;
import org.llw.studio.ui.UiLayoutContext;

/**
 * Dedicated UI layout view for editing canvas widgets in reference resolution.
 */
public final class UiEditorPanel implements EditorPanel {
    private final RenderBackend backend;
    private final AssetDatabase assets;
    private final EditorSession session;
    private final SelectionService selection;
    private final UndoStack undoStack;
    private final UiEditorInput editorInput;
    private OffscreenTarget target;
    private int lastWidth = 640;
    private int lastHeight = 360;
    private final ImInt canvasComboIndex = new ImInt(0);

    public UiEditorPanel(
            RenderBackend backend,
            AssetDatabase assets,
            EditorSession session,
            SelectionService selection,
            UndoStack undoStack
    ) {
        this.backend = backend;
        this.assets = assets;
        this.session = session;
        this.selection = selection;
        this.undoStack = undoStack;
        this.editorInput = new UiEditorInput(selection, undoStack);
        target = new OffscreenTarget(backend, new IntSize(lastWidth, lastHeight));
    }

    @Override
    public String id() {
        return "ui";
    }

    @Override
    public String title() {
        return "UI";
    }

    @Override
    public void render(StudioContext context) {
        if (!ImGui.begin(title())) {
            ImGui.end();
            return;
        }

        Scene scene = context.editScene();
        UiEditorState uiState = session.uiEditorState();
        uiState.refreshCanvases(scene);

        var entries = uiState.canvasEntries();
        if (entries.isEmpty()) {
            EditorStyle.pushMutedText();
            ImGui.text("Add a UI Canvas to the scene to edit widgets here.");
            EditorStyle.popMutedText();
            ImGui.end();
            return;
        }

        int activeIndex = 0;
        for (int i = 0; i < entries.size(); i++) {
            if (entries.get(i).entity().equals(uiState.activeCanvas())) {
                activeIndex = i;
                break;
            }
        }
        canvasComboIndex.set(activeIndex);
        String[] labels = entries.stream().map(UiEditorState.CanvasEntry::name).toArray(String[]::new);
        if (ImGui.combo("Canvas", canvasComboIndex, labels)) {
            uiState.setActiveCanvas(entries.get(canvasComboIndex.get()).entity());
        }

        EntityId canvasEntity = uiState.activeCanvas();
        UICanvasComponent canvas = scene.world().getComponent(canvasEntity, UICanvasComponent.class);
        if (canvas == null) {
            ImGui.end();
            return;
        }

        if (canvas.renderMode == UiCanvasRenderMode.WORLD_SPACE) {
            EditorStyle.pushMutedText();
            ImGui.textWrapped("World Space — layout edits reference pixels; motion preview in Game view.");
            EditorStyle.popMutedText();
        }

        int refW = Math.max(1, canvas.referenceWidth);
        int refH = Math.max(1, canvas.referenceHeight);
        ImGui.text(refW + " x " + refH);

        float width = ImGui.getContentRegionAvailX();
        float height = ImGui.getContentRegionAvailY();
        int w = Math.max(1, (int) width);
        int h = Math.max(1, (int) height);
        if (w != lastWidth || h != lastHeight) {
            target.dispose();
            target = new OffscreenTarget(backend, new IntSize(w, h));
            lastWidth = w;
            lastHeight = h;
        }

        // Authoring uses reference resolution; preview stretches to panel size via forStretchedImage below.
        target.clear(new Color(EditorColors.VIEWPORT_BG_R, EditorColors.VIEWPORT_BG_G, EditorColors.VIEWPORT_BG_B, 255));
        UiDrawPass.drawCanvas(scene, target, assets.uiFontCache(), canvasEntity, canvas);
        UiLayoutContext ctx = UiLayoutContext.forAuthoring(canvasEntity, refW, refH);
        var items = UiLayout.collect(scene, ctx);
        UiWidgetGizmo.draw(target, items, selection.selected());
        target.flush();

        Texture2d texture = target.colorTexture();
        ImGui.image(texture.id(), width, height, 0f, 1f, 1f, 0f);
        float imageMinX = ImGui.getItemRectMinX();
        float imageMinY = ImGui.getItemRectMinY();
        // Input runs after ImGui.image so pointer coords match the stretched preview item rect.
        ImGui.setCursorScreenPos(imageMinX, imageMinY);
        ImGui.invisibleButton("##ui_preview_hit", width, height);
        UiEditorPointer pointer = UiEditorPointer.forStretchedImage(refW, refH, width, height);
        editorInput.handle(scene, canvasEntity, refW, refH, pointer, imageMinX, imageMinY);

        ImGui.end();
    }

    public void dispose() {
        target.dispose();
    }
}
