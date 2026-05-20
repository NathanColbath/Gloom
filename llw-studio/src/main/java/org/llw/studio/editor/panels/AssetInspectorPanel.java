package org.llw.studio.editor.panels;



import com.fasterxml.jackson.databind.JsonNode;

import imgui.ImGui;

import org.llw.studio.assets.AssetDatabase;

import org.llw.studio.assets.AssetPreviewCache;

import org.llw.studio.assets.AssetType;

import org.llw.studio.assets.StudioAsset;

import org.llw.studio.editor.StudioContext;

import org.llw.studio.editor.components.ComponentCatalog;

import org.llw.studio.editor.components.InspectorContext;

import org.llw.studio.editor.inspector.AnimationSetInspectorEditor;
import org.llw.studio.editor.inspector.TilesetInspectorEditor;
import org.llw.studio.editor.inspector.TextureImportInspectorEditor;
import org.llw.studio.editor.prefab.PrefabAssetEditor;

import org.llw.studio.editor.theme.EditorIcons;

import org.llw.studio.editor.widgets.ComponentFoldout;

import org.llw.studio.editor.widgets.EmptyState;

import org.llw.studio.editor.widgets.PropertyRow;
import org.llw.studio.editor.widgets.SpriteSlicePreview;
import org.llw.studio.assets.SpriteDefinition;

import org.llw.studio.serialization.PrefabSerializer;



/**

 * Inspector UI for a selected project asset (metadata, texture preview, prefab editor).

 */

public final class AssetInspectorPanel {

    private final AssetDatabase assets;

    private final AssetPreviewCache previews;

    private final PrefabAssetEditor prefabEditor;
    private final TilesetInspectorEditor tilesetEditor = new TilesetInspectorEditor();
    private final AnimationSetInspectorEditor animationSetEditor = new AnimationSetInspectorEditor();
    private final TextureImportInspectorEditor textureImportEditor = new TextureImportInspectorEditor();



    /**

     * @param assets            asset database

     * @param previews          thumbnail cache

     * @param catalog           component catalog for prefab editing

     * @param inspectorContext  shared inspector context for prefab drawers

     */

    public AssetInspectorPanel(

            AssetDatabase assets,

            AssetPreviewCache previews,

            ComponentCatalog catalog,

            InspectorContext inspectorContext

    ) {

        this.assets = assets;

        this.previews = previews;

        this.prefabEditor = new PrefabAssetEditor(catalog, inspectorContext);

    }



    /**

     * Draws asset fields for the current selection or info target.

     *

     * @param context studio context (play mode disables prefab edit)

     */

    public void render(StudioContext context) {

        StudioAsset asset = assets.selected() != null ? assets.selected() : assets.infoTarget();

        if (asset == null) {

            EmptyState.render("No asset selected");

            return;

        }

        if (asset.isFolder()) {

            EmptyState.render("Folder: " + asset.displayName());

            return;

        }



        ComponentFoldout.State state = ComponentFoldout.header("asset:" + asset.guid(), "Asset", EditorIcons.IMAGE, true, false);

        if (state.open()) {

            ComponentFoldout.beginBody();

            PropertyRow.readOnlyValue("Name", asset.friendlyDisplayName());

            PropertyRow.readOnlyValue("Type", asset.type().name());

            PropertyRow.readOnlyValue("GUID", asset.guid());

            PropertyRow.readOnlyValue("Path", asset.path().toString());



            if (asset.type() == AssetType.TEXTURE) {

                var texture = previews.preview(asset.guid());

                if (texture != null) {

                    ImGui.image(texture.id(), 128f, 128f, 0f, 1f, 1f, 0f);

                }

                if (ImGui.button("Reimport")) {

                    assets.reimport(asset.guid());

                    previews.invalidate(asset.guid());

                }

                textureImportEditor.render(asset, assets, previews);
                tilesetEditor.render(asset, assets, previews);

            }

            if (asset.type() == AssetType.SPRITE && asset.parentTextureGuid() != null) {

                PropertyRow.readOnlyValue("Parent", assets.displayName(asset.parentTextureGuid()));

                SpriteDefinition slice = assets.sprite(asset.guid());

                var texture = previews.preview(asset.parentTextureGuid());

                if (slice != null && texture != null) {

                    PropertyRow.readOnlyValue("Rect", slice.x() + ", " + slice.y() + "  "
                            + slice.width() + "x" + slice.height());

                    SpriteSlicePreview.draw(texture, slice, 160f);
                    textureImportEditor.renderInherited(asset.parentTextureGuid(), assets);

                } else if (texture != null) {

                    ImGui.image(texture.id(), 128f, 128f, 0f, 1f, 1f, 0f);

                }

            }



            if (asset.type() == AssetType.ANIMATION) {
                animationSetEditor.render(asset, assets);
            }

            if (asset.type() == AssetType.ANIMATION_CLIP && asset.parentAnimationGuid() != null) {
                PropertyRow.readOnlyValue("Parent", assets.displayName(asset.parentAnimationGuid()));
            }

            if (asset.type() == AssetType.PREFAB) {

                try {

                    PrefabSerializer.PrefabData prefab = PrefabSerializer.load(asset.path());

                    int count = 0;

                    for (JsonNode ignored : prefab.objectNodes()) {

                        count++;

                    }

                    PropertyRow.readOnlyValue("Objects", String.valueOf(count));

                } catch (Exception ignored) {

                    PropertyRow.readOnlyValue("Objects", "?");

                }

                prefabEditor.render(context, assets, asset);

            }

            ComponentFoldout.endBody();

        }

    }

}

