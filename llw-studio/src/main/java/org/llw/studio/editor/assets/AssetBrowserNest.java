package org.llw.studio.editor.assets;

import org.llw.studio.assets.AnimationSetDefinition;
import org.llw.studio.assets.AnimationStateDefinition;
import org.llw.studio.assets.AssetDatabase;
import org.llw.studio.assets.AssetType;
import org.llw.studio.assets.StudioAsset;

import java.util.ArrayList;
import java.util.List;

/**
 * Shared nesting rules for project browser list and grid views.
 */
public final class AssetBrowserNest {
    private AssetBrowserNest() {
    }

    public record NestChild(StudioAsset asset, String displayLabel, AssetIconKind iconKind) {
    }

    public record NestableEntry(StudioAsset parent, List<NestChild> children) {
        public boolean expandable() {
            return children != null && !children.isEmpty();
        }
    }

    /**
     * @param assets     asset index
     * @param folderGuid current folder GUID
     * @return top-level folder children, excluding clips nested under animations in the same folder
     */
    public static List<StudioAsset> childrenOf(AssetDatabase assets, String folderGuid) {
        List<StudioAsset> result = new ArrayList<>();
        for (StudioAsset asset : assets.children(folderGuid)) {
            if (!isClipShownUnderAnimation(assets, asset)) {
                result.add(asset);
            }
        }
        return result;
    }

    /**
     * @param assets asset index
     * @param parent parent texture or animation asset
     * @return nested virtual children for browser expansion
     */
    public static List<NestChild> nestChildren(AssetDatabase assets, StudioAsset parent) {
        if (parent == null) {
            return List.of();
        }
        if (parent.type() == AssetType.TEXTURE) {
            return spriteNestChildren(assets, parent.guid());
        }
        if (parent.type() == AssetType.ANIMATION) {
            return animationStateChildren(assets, parent.guid());
        }
        return List.of();
    }

    public static NestableEntry entryFor(AssetDatabase assets, StudioAsset parent) {
        return new NestableEntry(parent, nestChildren(assets, parent));
    }

    public static boolean isClipShownUnderAnimation(AssetDatabase assets, StudioAsset asset) {
        if (asset.type() != AssetType.ANIMATION_CLIP || asset.parentAnimationGuid() == null) {
            return false;
        }
        StudioAsset parent = assets.get(asset.parentAnimationGuid());
        if (parent == null || parent.path().getParent() == null || asset.path().getParent() == null) {
            return false;
        }
        return parent.path().getParent().normalize().equals(asset.path().getParent().normalize());
    }

    private static List<NestChild> spriteNestChildren(AssetDatabase assets, String textureGuid) {
        List<NestChild> children = new ArrayList<>();
        for (StudioAsset slice : assets.spriteChildren(textureGuid)) {
            children.add(new NestChild(slice, slice.friendlyDisplayName(), AssetIconKind.SPRITE));
        }
        return children;
    }

    private static List<NestChild> animationStateChildren(AssetDatabase assets, String animationGuid) {
        AnimationSetDefinition set = assets.animationSet(animationGuid);
        if (set == null || set.states.isEmpty()) {
            return List.of();
        }
        List<NestChild> children = new ArrayList<>();
        for (AnimationStateDefinition state : set.states) {
            StudioAsset clip = assets.get(state.clipGuid());
            if (clip == null) {
                continue;
            }
            children.add(new NestChild(clip, state.name(), AssetIconKind.ANIMATION_STATE));
        }
        return children;
    }
}
