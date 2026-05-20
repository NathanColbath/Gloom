package org.llw.studio.assets;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class AnimationSetSerializerTest {
    @Test
    void roundTrip(@TempDir Path dir) throws Exception {
        Path path = dir.resolve("Player.animation.json");
        AnimationSetDefinition set = new AnimationSetDefinition();
        set.defaultState = "Idle";
        set.states.add(new AnimationStateDefinition("Idle", "clip-1"));
        set.states.add(new AnimationStateDefinition("Walk", "clip-2"));
        set.clips.add(new AnimationClipEntry("clip-1", "Idle", "Idle.anim.json"));
        AnimationSetSerializer.save(path, set);
        AnimationSetDefinition loaded = AnimationSetSerializer.load(path);
        assertEquals("Idle", loaded.defaultState);
        assertEquals(2, loaded.states.size());
        assertEquals("clip-2", loaded.clipGuidForState("Walk"));
    }
}
