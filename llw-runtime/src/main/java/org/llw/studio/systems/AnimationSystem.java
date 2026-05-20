package org.llw.studio.systems;

import org.llw.studio.animation.AnimationClip;
import org.llw.studio.animation.AnimationSampler;
import org.llw.studio.assets.AssetDatabase;
import org.llw.studio.ecs.EcsSystem;
import org.llw.studio.ecs.EntityId;
import org.llw.studio.ecs.World;
import org.llw.studio.ecs.components.Animation2DComponent;

import java.util.HashMap;
import java.util.Map;

/**
 * Advances and samples {@link Animation2DComponent} clips in play mode.
 */
public final class AnimationSystem implements EcsSystem {
    private static final class PlaybackState {
        float time;
        boolean playing;
        boolean playOnStartHandled;
    }

    private final AssetDatabase assets;
    private final Map<EntityId, PlaybackState> states = new HashMap<>();
    private World activeWorld;

    public AnimationSystem(AssetDatabase assets) {
        this.assets = assets;
    }

    public void reset() {
        states.clear();
        activeWorld = null;
    }

    public void setPlaying(EntityId entity, boolean playing) {
        PlaybackState state = stateFor(entity);
        state.playing = playing;
        if (playing) {
            state.playOnStartHandled = true;
        }
    }

    public void playState(EntityId entity, String stateName) {
        Animation2DComponent component = component(entity);
        if (component == null || stateName == null || stateName.isBlank()) {
            return;
        }
        boolean sameState = stateName.equals(component.currentState);
        component.currentState = stateName;
        PlaybackState state = stateFor(entity);
        if (!sameState) {
            state.time = 0f;
        }
        state.playing = true;
        state.playOnStartHandled = true;
    }

    public void stop(EntityId entity) {
        PlaybackState state = stateFor(entity);
        state.playing = false;
        state.time = 0f;
    }

    public void setNormalizedTime(EntityId entity, float normalized) {
        Animation2DComponent component = component(entity);
        if (component == null) {
            return;
        }
        String clipGuid = resolveClipGuid(component);
        AnimationClip clip = assets.animationClip(clipGuid);
        if (clip == null || clip.length <= 0f) {
            return;
        }
        PlaybackState state = stateFor(entity);
        state.time = Math.max(0f, Math.min(clip.length, normalized * clip.length));
        if (activeWorld != null) {
            AnimationSampler.apply(activeWorld, entity, clip, state.time, assets);
        }
    }

    public float normalizedTime(EntityId entity) {
        Animation2DComponent component = component(entity);
        if (component == null) {
            return 0f;
        }
        String clipGuid = resolveClipGuid(component);
        AnimationClip clip = assets.animationClip(clipGuid);
        if (clip == null || clip.length <= 0f) {
            return 0f;
        }
        return stateFor(entity).time / clip.length;
    }

    @Override
    public void onUpdate(World world, float deltaTime) {
        activeWorld = world;
        var store = world.store(Animation2DComponent.class);
        for (int i = 0; i < store.size(); i++) {
            EntityId entity = store.entityAt(i);
            Animation2DComponent component = store.componentAt(i);
            String clipGuid = resolveClipGuid(component);
            if (clipGuid == null || clipGuid.isBlank()) {
                continue;
            }
            AnimationClip clip = assets.animationClip(clipGuid);
            if (clip == null) {
                continue;
            }
            PlaybackState state = stateFor(entity);
            if (!state.playOnStartHandled && component.playOnStart) {
                if (component.currentState == null || component.currentState.isBlank()) {
                    component.currentState = component.defaultState;
                }
                state.playing = true;
                state.playOnStartHandled = true;
            }
            if (state.playing && deltaTime > 0f) {
                float speed = component.speed <= 0f ? 1f : component.speed;
                state.time += deltaTime * speed;
                boolean loop = component.loop;
                if (state.time > clip.length) {
                    if (loop && clip.length > 0f) {
                        state.time %= clip.length;
                    } else {
                        state.time = clip.length;
                        state.playing = false;
                    }
                }
            }
            AnimationSampler.apply(world, entity, clip, state.time, assets);
        }
    }

    public static String resolveClipGuid(Animation2DComponent component, AssetDatabase assets) {
        if (component == null) {
            return "";
        }
        if (component.animationGuid != null && !component.animationGuid.isBlank() && assets != null) {
            String state = component.currentState;
            if (state == null || state.isBlank()) {
                state = component.defaultState;
            }
            String fromState = assets.stateClipGuid(component.animationGuid, state);
            if (fromState != null && !fromState.isBlank()) {
                return fromState;
            }
        }
        return component.clipGuid == null ? "" : component.clipGuid;
    }

    private String resolveClipGuid(Animation2DComponent component) {
        return resolveClipGuid(component, assets);
    }

    private PlaybackState stateFor(EntityId entity) {
        return states.computeIfAbsent(entity, id -> new PlaybackState());
    }

    private Animation2DComponent component(EntityId entity) {
        if (activeWorld == null || entity == null || entity.isNone()) {
            return null;
        }
        return activeWorld.getComponent(entity, Animation2DComponent.class);
    }
}
