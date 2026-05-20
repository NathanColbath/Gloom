package org.llw.studio.editor.inspector;

import imgui.ImGui;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImBoolean;
import imgui.type.ImInt;
import org.llw.studio.assets.AssetDatabase;
import org.llw.studio.assets.RuleTileDefinition;
import org.llw.studio.assets.RuleTileRule;
import org.llw.studio.assets.SpriteDefinition;
import org.llw.studio.assets.TileDefinition;
import org.llw.studio.assets.TileNeighborConstraint;
import org.llw.studio.assets.TileNeighborMask;
import org.llw.studio.assets.TilesetDefinition;

import java.util.ArrayList;
import java.util.List;

/**
 * Modal editor for rule-tile neighbor rules on a tile definition.
 */
public final class RuleTileEditorModal {
    private static final String POPUP_ID = "Rule Tile Editor";
    private boolean open;
    private TileDefinition editingTile;
    private final List<SpriteDefinition> spriteChoices = new ArrayList<>();
    private final ImInt selectedRuleIndex = new ImInt(0);
    private final ImBoolean ruleTileEnabled = new ImBoolean(false);

    public void open(TileDefinition tile, TilesetDefinition tileset, AssetDatabase assets) {
        editingTile = tile == null ? null : tile.copy();
        spriteChoices.clear();
        if (tileset != null) {
            for (TileDefinition t : tileset.tiles) {
                SpriteDefinition sprite = assets.sprite(t.spriteGuid);
                if (sprite != null) {
                    spriteChoices.add(sprite);
                }
            }
        }
        ruleTileEnabled.set(editingTile != null && editingTile.ruleTile != null && editingTile.ruleTile.isActive());
        if (editingTile != null && editingTile.ruleTile == null) {
            editingTile.ruleTile = new RuleTileDefinition();
            editingTile.ruleTile.defaultSpriteGuid = editingTile.spriteGuid;
        }
        selectedRuleIndex.set(0);
        open = true;
        ImGui.openPopup(POPUP_ID);
    }

    /**
     * @return updated tile definition when user applies, or {@code null} when cancelled/closed
     */
    public TileDefinition render() {
        if (editingTile == null) {
            return null;
        }
        if (open) {
            ImGui.openPopup(POPUP_ID);
        }
        TileDefinition result = null;
        if (!ImGui.beginPopupModal(POPUP_ID, ImGuiWindowFlags.AlwaysAutoResize)) {
            return null;
        }
        open = false;
        ImGui.checkbox("Enable Rule Tile", ruleTileEnabled);
            if (ruleTileEnabled.get()) {
                if (editingTile.ruleTile == null) {
                    editingTile.ruleTile = new RuleTileDefinition();
                    editingTile.ruleTile.defaultSpriteGuid = editingTile.spriteGuid;
                }
                drawDefaultSpritePicker();
                ImGui.separator();
                drawRulesList();
            } else {
                editingTile.ruleTile = null;
            }
            if (ImGui.button("Apply")) {
                result = editingTile.copy();
                if (!ruleTileEnabled.get()) {
                    result.ruleTile = null;
                }
                open = false;
                ImGui.closeCurrentPopup();
            }
            ImGui.sameLine();
        if (ImGui.button("Cancel")) {
            open = false;
            ImGui.closeCurrentPopup();
        }
        ImGui.endPopup();
        return result;
    }

    private void drawDefaultSpritePicker() {
        String[] names = spriteChoices.stream().map(SpriteDefinition::name).toArray(String[]::new);
        int current = indexOfGuid(editingTile.ruleTile.defaultSpriteGuid);
        ImInt idx = new ImInt(Math.max(0, current));
        if (names.length > 0 && ImGui.combo("Default Sprite", idx, names)) {
            editingTile.ruleTile.defaultSpriteGuid = spriteChoices.get(idx.get()).guid();
        }
    }

    private void drawRulesList() {
        ImGui.text("Rules (first match wins)");
        if (ImGui.button("Add Rule")) {
            RuleTileRule rule = new RuleTileRule(new TileNeighborMask(), editingTile.spriteGuid);
            editingTile.ruleTile.rules.add(rule);
            selectedRuleIndex.set(editingTile.ruleTile.rules.size() - 1);
        }
        if (editingTile.ruleTile.rules.isEmpty()) {
            return;
        }
        String[] labels = new String[editingTile.ruleTile.rules.size()];
        for (int i = 0; i < labels.length; i++) {
            labels[i] = "Rule " + (i + 1);
        }
        if (selectedRuleIndex.get() >= labels.length) {
            selectedRuleIndex.set(labels.length - 1);
        }
        ImGui.combo("Rule", selectedRuleIndex, labels);
        int ri = selectedRuleIndex.get();
        RuleTileRule rule = editingTile.ruleTile.rules.get(ri);
        drawMaskEditor("North", rule.neighbors.north, v -> rule.neighbors.north = v);
        drawMaskEditor("North East", rule.neighbors.northEast, v -> rule.neighbors.northEast = v);
        drawMaskEditor("East", rule.neighbors.east, v -> rule.neighbors.east = v);
        drawMaskEditor("South East", rule.neighbors.southEast, v -> rule.neighbors.southEast = v);
        drawMaskEditor("South", rule.neighbors.south, v -> rule.neighbors.south = v);
        drawMaskEditor("South West", rule.neighbors.southWest, v -> rule.neighbors.southWest = v);
        drawMaskEditor("West", rule.neighbors.west, v -> rule.neighbors.west = v);
        drawMaskEditor("North West", rule.neighbors.northWest, v -> rule.neighbors.northWest = v);
        String[] names = spriteChoices.stream().map(SpriteDefinition::name).toArray(String[]::new);
        int outputIdx = indexOfGuid(rule.spriteGuid);
        ImInt out = new ImInt(Math.max(0, outputIdx));
        if (names.length > 0 && ImGui.combo("Output Sprite", out, names)) {
            rule.spriteGuid = spriteChoices.get(out.get()).guid();
        }
        if (ImGui.button("Remove Rule")) {
            editingTile.ruleTile.rules.remove(ri);
            selectedRuleIndex.set(Math.max(0, ri - 1));
        }
    }

    private interface ConstraintSetter {
        void set(TileNeighborConstraint value);
    }

    private static void drawMaskEditor(String label, TileNeighborConstraint current, ConstraintSetter setter) {
        String[] options = {"Don't Care", "Same", "Not Same"};
        int idx = switch (current) {
            case SAME -> 1;
            case NOT_SAME -> 2;
            default -> 0;
        };
        ImInt selected = new ImInt(idx);
        if (ImGui.combo(label, selected, options)) {
            setter.set(switch (selected.get()) {
                case 1 -> TileNeighborConstraint.SAME;
                case 2 -> TileNeighborConstraint.NOT_SAME;
                default -> TileNeighborConstraint.DONT_CARE;
            });
        }
    }

    private int indexOfGuid(String guid) {
        for (int i = 0; i < spriteChoices.size(); i++) {
            if (spriteChoices.get(i).guid().equals(guid)) {
                return i;
            }
        }
        return 0;
    }
}
