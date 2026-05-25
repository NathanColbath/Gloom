package org.llw.studio.shadergraph.editor;

import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImGuiKey;
import imgui.flag.ImGuiMouseButton;
import imgui.flag.ImGuiWindowFlags;
import org.llw.studio.shadergraph.model.ShaderGraphDocument;
import org.llw.studio.shadergraph.model.ShaderGraphLink;
import org.llw.studio.shadergraph.model.ShaderGraphNode;
import org.llw.studio.shadergraph.model.ShaderGraphPinCatalog;
import org.llw.studio.shadergraph.model.ShaderGraphPinCatalog.PinDef;
import org.llw.studio.shadergraph.model.ShaderGraphPinRef;
import org.llw.studio.shadergraph.model.ShaderNodeType;
import org.llw.studio.shadergraph.model.ShaderPinType;

import java.util.Iterator;
import java.util.List;
import java.util.UUID;

/**
 * Custom ImGui node graph editor (pan, zoom, link, add/delete nodes).
 */
public final class ShaderGraphCanvas {
    private static final float NODE_WIDTH = 150f;
    private static final float HEADER_HEIGHT = 22f;
    private static final float PIN_HEIGHT = 18f;
    private static final float PIN_RADIUS = 5f;
    private static final float LINK_HIT_DISTANCE = 10f;

    private float panX;
    private float panY = 40f;
    private float zoom = 1f;
    private String linkFromNodeId = "";
    private String linkFromPinId = "";
    private int selectedLinkIndex = -1;

    public void render(ShaderGraphEditorState state) {
        ImGui.beginChild("##ShaderGraphCanvas", 0f, 0f, true, ImGuiWindowFlags.HorizontalScrollbar);
        // Middle-drag pans; wheel zooms around fixed origin (node positions stay in graph space).
        if (ImGui.isWindowHovered() && ImGui.isMouseDragging(ImGuiMouseButton.Middle)) {
            ImVec2 delta = ImGui.getMouseDragDelta(ImGuiMouseButton.Middle);
            panX += delta.x;
            panY += delta.y;
            ImGui.resetMouseDragDelta(ImGuiMouseButton.Middle);
        }
        if (ImGui.isWindowHovered()) {
            float wheel = ImGui.getIO().getMouseWheel();
            if (wheel != 0f) {
                zoom = Math.max(0.35f, Math.min(2.5f, zoom + wheel * 0.08f));
            }
        }

        // Graph origin in screen space — all node/link geometry is offset from here then scaled by zoom.
        float originX = ImGui.getCursorScreenPosX() + panX;
        float originY = ImGui.getCursorScreenPosY() + panY;
        var drawList = ImGui.getWindowDrawList();

        if (ImGui.isWindowFocused() && ImGui.isKeyPressed(ImGuiKey.Escape)) {
            cancelLinkDrag();
            selectedLinkIndex = -1;
        }

        drawGrid(drawList, originX, originY);
        handleLinkInteraction(state, originX, originY);
        // Links under nodes so pins stay clickable; in-progress drag wire drawn last in drawLinks.
        drawLinks(state.document(), drawList, originX, originY);
        drawNodes(state, drawList, originX, originY);

        if (ImGui.isWindowHovered() && ImGui.isMouseClicked(ImGuiMouseButton.Right)
                && !ImGui.isAnyItemHovered()) {
            ImGui.openPopup("##ShaderGraphAddNode");
        }
        renderAddNodePopup(state);

        // Delete removes selected link first; otherwise deletes the selected node and its links.
        if (ImGui.isWindowFocused() && ImGui.isKeyPressed(ImGuiKey.Delete)) {
            if (selectedLinkIndex >= 0) {
                removeLinkAt(state, selectedLinkIndex);
                selectedLinkIndex = -1;
            } else {
                deleteSelected(state);
            }
        }

        ImGui.endChild();
    }

    private void handleLinkInteraction(ShaderGraphEditorState state, float originX, float originY) {
        if (!ImGui.isWindowHovered()) {
            return;
        }
        if (ImGui.isMouseClicked(ImGuiMouseButton.Left) && !isLinkDragActive()) {
            selectedLinkIndex = hitTestLink(state.document(), originX, originY, ImGui.getMousePos());
        }
    }

    private void drawGrid(imgui.ImDrawList drawList, float originX, float originY) {
        float w = ImGui.getContentRegionAvailX();
        float h = ImGui.getContentRegionAvailY();
        int grid = Math.max(8, (int) (32 * zoom));
        int color = 0x20FFFFFF;
        float startX = ImGui.getWindowPosX();
        float startY = ImGui.getWindowPosY();
        for (int x = 0; x < w; x += grid) {
            drawList.addLine(startX + x, startY, startX + x, startY + h, color);
        }
        for (int y = 0; y < h; y += grid) {
            drawList.addLine(startX, startY + y, startX + w, startY + y, color);
        }
        drawList.addLine(originX, startY, originX, startY + h, 0x50FFFFFF);
        drawList.addLine(startX, originY, startX + w, originY, 0x50FFFFFF);
    }

    private void drawLinks(ShaderGraphDocument document, imgui.ImDrawList drawList, float originX, float originY) {
        List<ShaderGraphLink> links = document.links;
        for (int i = 0; i < links.size(); i++) {
            ShaderGraphLink link = links.get(i);
            ShaderGraphNode fromNode = document.nodeById(link.from.nodeId);
            ShaderGraphNode toNode = document.nodeById(link.to.nodeId);
            if (fromNode == null || toNode == null) {
                continue;
            }
            ImVec2 fromPos = pinScreenPos(fromNode, link.from.pinId, false, originX, originY);
            ImVec2 toPos = pinScreenPos(toNode, link.to.pinId, true, originX, originY);
            int color = i == selectedLinkIndex ? 0xFFFFCC44 : 0xFF88CCFF;
            drawBezier(drawList, fromPos, toPos, color);
        }
        if (isLinkDragActive()) {
            ShaderGraphNode fromNode = document.nodeById(linkFromNodeId);
            if (fromNode != null) {
                ImVec2 fromPos = pinScreenPos(fromNode, linkFromPinId, false, originX, originY);
                drawBezier(drawList, fromPos, ImGui.getMousePos(), 0xFFAAEEFF);
            }
        }
    }

    private void drawNodes(ShaderGraphEditorState state, imgui.ImDrawList drawList, float originX, float originY) {
        ShaderGraphDocument document = state.document();
        for (ShaderGraphNode node : document.nodes) {
            float x = originX + node.x * zoom;
            float y = originY + node.y * zoom;
            float w = NODE_WIDTH * zoom;
            float h = nodeHeight(node) * zoom;
            boolean selected = node.id.equals(state.selectedNodeId());
            // Highlight preview root when compile failed so the broken subgraph is obvious.
            boolean compileError = state.lastCompileError() != null
                    && !state.lastCompileError().isBlank()
                    && node.id.equals(state.previewRootNodeId());
            int headerColor = compileError ? 0xFF4444AA : (selected ? 0xFF3A6EA5 : 0xFF2A2A35);
            drawList.addRectFilled(x, y, x + w, y + h, 0xFF1E1E24, 6f);
            drawList.addRectFilled(x, y, x + w, y + HEADER_HEIGHT * zoom, headerColor, 6f);
            drawList.addText(x + 6f, y + 4f, 0xFFFFFFFF, node.type.name());

            // Node drag converts screen delta back to graph space; skip while link drag is active.
            if (selected && ImGui.isMouseDragging(ImGuiMouseButton.Left) && ImGui.isMouseDown(ImGuiMouseButton.Left)
                    && !isLinkDragActive()) {
                ImVec2 delta = ImGui.getMouseDragDelta(ImGuiMouseButton.Left);
                if (ImGui.isMouseDoubleClicked(ImGuiMouseButton.Left)) {
                    ImGui.resetMouseDragDelta(ImGuiMouseButton.Left);
                } else if (delta.x != 0f || delta.y != 0f) {
                    node.x += delta.x / zoom;
                    node.y += delta.y / zoom;
                    state.touch();
                    ImGui.resetMouseDragDelta(ImGuiMouseButton.Left);
                }
            }

            float pinY = y + HEADER_HEIGHT * zoom + 4f;
            for (PinDef pin : ShaderGraphPinCatalog.pinsFor(node.type)) {
                if (handlePinInteraction(state, node, pin, x, pinY, w, true)) {
                    ImGui.setMouseCursor(imgui.flag.ImGuiMouseCursor.Hand);
                }
                drawPin(drawList, node, pin, x, pinY, w, true);
                pinY += PIN_HEIGHT * zoom;
            }
            for (PinDef pin : ShaderGraphPinCatalog.outputPinsFor(node.type)) {
                if (handlePinInteraction(state, node, pin, x, pinY, w, false)) {
                    ImGui.setMouseCursor(imgui.flag.ImGuiMouseCursor.Hand);
                }
                drawPin(drawList, node, pin, x, pinY, w, false);
                pinY += PIN_HEIGHT * zoom;
            }

            if (ImGui.isMouseClicked(ImGuiMouseButton.Left) && !isLinkDragActive()) {
                ImVec2 mouse = ImGui.getMousePos();
                if (mouse.x >= x && mouse.x <= x + w && mouse.y >= y && mouse.y <= y + h
                        && !isOverAnyPin(node, mouse, x, y, w)) {
                    state.setSelectedNodeId(node.id);
                    selectedLinkIndex = -1;
                }
            }
        }
    }

    private boolean handlePinInteraction(
            ShaderGraphEditorState state,
            ShaderGraphNode node,
            PinDef pin,
            float nodeX,
            float pinY,
            float nodeW,
            boolean input
    ) {
        float cx = input ? nodeX + 10f : nodeX + nodeW - 10f;
        float cy = pinY + 6f;
        float hit = 12f * zoom;
        ImVec2 mouse = ImGui.getMousePos();
        if (Math.abs(mouse.x - cx) >= hit || Math.abs(mouse.y - cy) >= hit) {
            return false;
        }

        ShaderGraphDocument document = state.document();

        // Right-click on a pin disconnects all links on that pin (input or output side).
        if (ImGui.isMouseClicked(ImGuiMouseButton.Right)) {
            if (input) {
                disconnectInput(document, node.id, pin.id());
            } else {
                disconnectOutput(document, node.id, pin.id());
            }
            state.touch();
            selectedLinkIndex = -1;
            return true;
        }

        if (ImGui.isMouseClicked(ImGuiMouseButton.Left)) {
            if (!input) {
                beginLinkDrag(node.id, pin.id());
                selectedLinkIndex = -1;
                return true;
            }
            ShaderGraphLink existing = findLinkTo(document, node.id, pin.id());
            if (isLinkDragActive()) {
                completeLink(state, linkFromNodeId, linkFromPinId, node.id, pin.id());
                cancelLinkDrag();
                return true;
            }
            // Click occupied input: rip existing link and start drag from its source (Unity-style rewire).
            if (existing != null) {
                beginLinkDrag(existing.from.nodeId, existing.from.pinId);
                document.links.remove(existing);
                state.touch();
                selectedLinkIndex = -1;
                return true;
            }
        }
        return true;
    }

    private void drawPin(
            imgui.ImDrawList drawList,
            ShaderGraphNode node,
            PinDef pin,
            float nodeX,
            float pinY,
            float nodeW,
            boolean input
    ) {
        float cx = input ? nodeX + 10f : nodeX + nodeW - 10f;
        int pinColor = pinColor(pin.type());
        drawList.addCircleFilled(cx, pinY + 6f, PIN_RADIUS * zoom, pinColor);
        drawList.addText(input ? cx + 10f : nodeX + 8f, pinY, 0xFFCCCCCC, pin.label());
    }

    private void beginLinkDrag(String fromNode, String fromPin) {
        linkFromNodeId = fromNode;
        linkFromPinId = fromPin;
    }

    private void cancelLinkDrag() {
        linkFromNodeId = "";
        linkFromPinId = "";
    }

    private boolean isLinkDragActive() {
        return linkFromNodeId != null && !linkFromNodeId.isBlank();
    }

    private void completeLink(ShaderGraphEditorState state, String fromNode, String fromPin, String toNode, String toPin) {
        if (fromNode.equals(toNode)) {
            return;
        }
        ShaderGraphDocument document = state.document();
        // Each input pin accepts at most one link — replace on drop.
        document.links.removeIf(link -> link.to.nodeId.equals(toNode) && link.to.pinId.equals(toPin));
        ShaderGraphLink link = new ShaderGraphLink();
        link.from = new ShaderGraphPinRef(fromNode, fromPin);
        link.to = new ShaderGraphPinRef(toNode, toPin);
        document.links.add(link);
        state.touch();
    }

    private static void disconnectInput(ShaderGraphDocument document, String nodeId, String pinId) {
        document.links.removeIf(link -> nodeId.equals(link.to.nodeId) && pinId.equals(link.to.pinId));
    }

    private static void disconnectOutput(ShaderGraphDocument document, String fromNodeId, String fromPinId) {
        document.links.removeIf(
                link -> fromNodeId.equals(link.from.nodeId) && fromPinId.equals(link.from.pinId)
        );
    }

    private static ShaderGraphLink findLinkTo(ShaderGraphDocument document, String nodeId, String pinId) {
        for (ShaderGraphLink link : document.links) {
            if (nodeId.equals(link.to.nodeId) && pinId.equals(link.to.pinId)) {
                return link;
            }
        }
        return null;
    }

    private void removeLinkAt(ShaderGraphEditorState state, int index) {
        List<ShaderGraphLink> links = state.document().links;
        if (index < 0 || index >= links.size()) {
            return;
        }
        links.remove(index);
        state.touch();
    }

    private int hitTestLink(ShaderGraphDocument document, float originX, float originY, ImVec2 mouse) {
        // Top-most link wins when beziers overlap.
        List<ShaderGraphLink> links = document.links;
        for (int i = links.size() - 1; i >= 0; i--) {
            ShaderGraphLink link = links.get(i);
            ShaderGraphNode fromNode = document.nodeById(link.from.nodeId);
            ShaderGraphNode toNode = document.nodeById(link.to.nodeId);
            if (fromNode == null || toNode == null) {
                continue;
            }
            ImVec2 fromPos = pinScreenPos(fromNode, link.from.pinId, false, originX, originY);
            ImVec2 toPos = pinScreenPos(toNode, link.to.pinId, true, originX, originY);
            if (distanceToBezier(fromPos, toPos, mouse) <= LINK_HIT_DISTANCE) {
                return i;
            }
        }
        return -1;
    }

    private static float distanceToBezier(ImVec2 from, ImVec2 to, ImVec2 point) {
        float dx = Math.abs(to.x - from.x) * 0.5f;
        float best = Float.MAX_VALUE;
        for (int i = 0; i <= 24; i++) {
            float t = i / 24f;
            float u = 1f - t;
            float x = u * u * u * from.x + 3f * u * u * t * (from.x + dx) + 3f * u * t * t * (to.x - dx) + t * t * t * to.x;
            float y = u * u * u * from.y + 3f * u * u * t * from.y + 3f * u * t * t * to.y + t * t * t * to.y;
            float ddx = x - point.x;
            float ddy = y - point.y;
            best = Math.min(best, (float) Math.sqrt(ddx * ddx + ddy * ddy));
        }
        return best;
    }

    private boolean isOverAnyPin(ShaderGraphNode node, ImVec2 mouse, float x, float y, float w) {
        float pinY = y + HEADER_HEIGHT * zoom + 4f;
        for (PinDef pin : ShaderGraphPinCatalog.pinsFor(node.type)) {
            if (isPinHit(mouse, x, pinY, w, true)) {
                return true;
            }
            pinY += PIN_HEIGHT * zoom;
        }
        for (PinDef pin : ShaderGraphPinCatalog.outputPinsFor(node.type)) {
            if (isPinHit(mouse, x, pinY, w, false)) {
                return true;
            }
            pinY += PIN_HEIGHT * zoom;
        }
        return false;
    }

    private boolean isPinHit(ImVec2 mouse, float nodeX, float pinY, float nodeW, boolean input) {
        float cx = input ? nodeX + 10f : nodeX + nodeW - 10f;
        float hit = 12f * zoom;
        return Math.abs(mouse.x - cx) < hit && Math.abs(mouse.y - (pinY + 6f)) < hit;
    }

    private ImVec2 pinScreenPos(ShaderGraphNode node, String pinId, boolean input, float originX, float originY) {
        float x = originX + node.x * zoom;
        float y = originY + node.y * zoom;
        float w = NODE_WIDTH * zoom;
        float pinY = y + HEADER_HEIGHT * zoom + 4f;
        for (PinDef pin : ShaderGraphPinCatalog.pinsFor(node.type)) {
            if (pin.id().equals(pinId)) {
                return new ImVec2(input ? x + 10f : x + w - 10f, pinY + 6f);
            }
            pinY += PIN_HEIGHT * zoom;
        }
        for (PinDef pin : ShaderGraphPinCatalog.outputPinsFor(node.type)) {
            if (pin.id().equals(pinId)) {
                return new ImVec2(input ? x + 10f : x + w - 10f, pinY + 6f);
            }
            pinY += PIN_HEIGHT * zoom;
        }
        return new ImVec2(x + w * 0.5f, y + HEADER_HEIGHT * zoom);
    }

    private static float nodeHeight(ShaderGraphNode node) {
        int pins = ShaderGraphPinCatalog.pinsFor(node.type).size()
                + ShaderGraphPinCatalog.outputPinsFor(node.type).size();
        return HEADER_HEIGHT + 8f + pins * PIN_HEIGHT;
    }

    private static void drawBezier(imgui.ImDrawList drawList, ImVec2 from, ImVec2 to, int color) {
        float dx = Math.abs(to.x - from.x) * 0.5f;
        drawList.addBezierCubic(
                from.x, from.y,
                from.x + dx, from.y,
                to.x - dx, to.y,
                to.x, to.y,
                color,
                2f,
                24
        );
    }

    private static int pinColor(ShaderPinType type) {
        return switch (type) {
            case FLOAT -> 0xFF88FF88;
            case VEC2 -> 0xFF88CCFF;
            case VEC3 -> 0xFFFFCC88;
            case VEC4 -> 0xFFFF88CC;
        };
    }

    private void renderAddNodePopup(ShaderGraphEditorState state) {
        if (ImGui.beginPopup("##ShaderGraphAddNode")) {
            for (ShaderNodeType type : ShaderNodeType.values()) {
                if (ImGui.menuItem(type.name())) {
                    addNode(state, type);
                }
            }
            ImGui.endPopup();
        }
    }

    private void addNode(ShaderGraphEditorState state, ShaderNodeType type) {
        ShaderGraphNode node = new ShaderGraphNode();
        node.id = type.name().toLowerCase() + "_" + UUID.randomUUID().toString().substring(0, 6);
        node.type = type;
        node.x = 40f + state.document().nodes.size() * 24f;
        node.y = 40f + state.document().nodes.size() * 18f;
        if (type == ShaderNodeType.Color) {
            node.params.put("r", 1f);
            node.params.put("g", 1f);
            node.params.put("b", 1f);
            node.params.put("a", 1f);
        }
        if (type == ShaderNodeType.FloatConstant) {
            node.params.put("value", 1f);
        }
        state.document().nodes.add(node);
        state.setSelectedNodeId(node.id);
        state.touch();
    }

    private void deleteSelected(ShaderGraphEditorState state) {
        String id = state.selectedNodeId();
        if (id == null || id.isBlank()) {
            return;
        }
        ShaderGraphDocument document = state.document();
        document.nodes.removeIf(n -> id.equals(n.id));
        document.links.removeIf(
                link -> id.equals(link.from.nodeId) || id.equals(link.to.nodeId)
        );
        state.setSelectedNodeId("");
        state.touch();
        selectedLinkIndex = -1;
    }
}
