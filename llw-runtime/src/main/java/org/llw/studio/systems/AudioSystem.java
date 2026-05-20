package org.llw.studio.systems;

import org.llw.studio.ecs.EcsSystem;
import org.llw.studio.ecs.EntityId;
import org.llw.studio.ecs.World;
import org.llw.studio.ecs.components.AudioSourceComponent;
import org.llw.studio.scripting.js.PlayAudioBridge;

/**
 * Starts {@link AudioSourceComponent#playOnStart} sources that are not already playing.
 *
 * <p>Complements {@link JsScriptSystem}, which handles the first-frame play-on-start pass;
 * this system covers sources added or re-enabled later in play mode.
 */
public final class AudioSystem implements EcsSystem {
    /**
     * @param world      scene ECS world
     * @param deltaTime  unused
     */
    @Override
    public void onUpdate(World world, float deltaTime) {
        var audioSources = world.store(AudioSourceComponent.class);
        for (int i = 0; i < audioSources.size(); i++) {
            EntityId entity = audioSources.entityAt(i);
            AudioSourceComponent source = audioSources.componentAt(i);
            if (source.playOnStart && !PlayAudioBridge.isPlaying(entity)) {
                PlayAudioBridge.play(entity, source);
            }
        }
    }
}
