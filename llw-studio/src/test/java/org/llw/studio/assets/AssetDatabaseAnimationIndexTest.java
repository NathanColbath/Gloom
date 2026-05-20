package org.llw.studio.assets;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.llw.studio.animation.AnimationClip;
import org.llw.studio.animation.AnimationClipSerializer;
import org.llw.studio.editor.animation.AnimationSetActions;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class AssetDatabaseAnimationIndexTest {
    @Test
    void indexesClipsAndStates(@TempDir Path projectRoot) throws Exception {
        AssetDatabase assets = new AssetDatabase(projectRoot, null);
        String animGuid = AnimationSetActions.createAnimation(assets, null, "Player");
        assertNotNull(animGuid);
        assertFalse(assets.clipChildren(animGuid).isEmpty());
        assertNotNull(assets.stateClipGuid(animGuid, "Idle"));
        AnimationSetDefinition set = assets.animationSet(animGuid);
        assertNotNull(set);
        assertEquals("Idle", set.defaultState);
    }
}
