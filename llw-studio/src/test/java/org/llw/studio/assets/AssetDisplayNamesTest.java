package org.llw.studio.assets;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AssetDisplayNamesTest {
    @Test
    void stripsAnimationExtensions() {
        assertEquals("Player", AssetDisplayNames.animationLabel("Player.animation.json"));
        assertEquals("Idle", AssetDisplayNames.clipLabel("Idle.anim.json"));
    }

    @Test
    void friendlyNamesForAnimationAssets() {
        StudioAsset anim = new StudioAsset(
                "a",
                null,
                AssetType.ANIMATION,
                "Hero.animation.json",
                null
        );
        StudioAsset clip = new StudioAsset(
                "c",
                null,
                AssetType.ANIMATION_CLIP,
                "Walk.anim.json",
                null,
                null,
                "a"
        );
        assertEquals("Hero", anim.friendlyDisplayName());
        assertEquals("Walk", clip.friendlyDisplayName());
    }
}
