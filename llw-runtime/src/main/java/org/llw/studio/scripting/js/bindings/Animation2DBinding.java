package org.llw.studio.scripting.js.bindings;

import org.graalvm.polyglot.HostAccess;
import org.llw.studio.ecs.EntityId;
import org.llw.studio.ecs.World;
import org.llw.studio.ecs.components.Animation2DComponent;
import org.llw.studio.scripting.js.PlayAnimationBridge;

/**
 * Play-mode API for {@link Animation2DComponent}.
 */
public final class Animation2DBinding {
    private final World world;
    private final EntityId entity;

    public Animation2DBinding(ScriptContext context, EntityId entity) {
        this.world = context.world();
        this.entity = entity;
    }

    @HostAccess.Export
    public String getAnimationGuid() {
        Animation2DComponent anim = component();
        return anim == null ? "" : anim.animationGuid;
    }

    @HostAccess.Export
    public void setAnimationGuid(String guid) {
        Animation2DComponent anim = component();
        if (anim != null) {
            anim.animationGuid = guid == null ? "" : guid;
        }
    }

    @HostAccess.Export
    public String getDefaultState() {
        Animation2DComponent anim = component();
        return anim == null ? "" : anim.defaultState;
    }

    @HostAccess.Export
    public void setDefaultState(String value) {
        Animation2DComponent anim = component();
        if (anim != null) {
            anim.defaultState = value == null ? "" : value;
        }
    }

    @HostAccess.Export
    public String getCurrentState() {
        Animation2DComponent anim = component();
        return anim == null ? "" : anim.currentState;
    }

    @HostAccess.Export
    public void setCurrentState(String value) {
        Animation2DComponent anim = component();
        if (anim != null) {
            anim.currentState = value == null ? "" : value;
        }
    }

    @HostAccess.Export
    public String getClipGuid() {
        Animation2DComponent anim = component();
        return anim == null ? "" : anim.clipGuid;
    }

    @HostAccess.Export
    public void setClipGuid(String guid) {
        Animation2DComponent anim = component();
        if (anim != null) {
            anim.clipGuid = guid == null ? "" : guid;
        }
    }

    @HostAccess.Export
    public boolean getPlayOnStart() {
        Animation2DComponent anim = component();
        return anim != null && anim.playOnStart;
    }

    @HostAccess.Export
    public void setPlayOnStart(boolean value) {
        Animation2DComponent anim = component();
        if (anim != null) {
            anim.playOnStart = value;
        }
    }

    @HostAccess.Export
    public double getSpeed() {
        Animation2DComponent anim = component();
        return anim == null ? 1d : anim.speed;
    }

    @HostAccess.Export
    public void setSpeed(double value) {
        Animation2DComponent anim = component();
        if (anim != null) {
            anim.speed = (float) value;
        }
    }

    @HostAccess.Export
    public boolean getLoop() {
        Animation2DComponent anim = component();
        return anim == null || anim.loop;
    }

    @HostAccess.Export
    public void setLoop(boolean value) {
        Animation2DComponent anim = component();
        if (anim != null) {
            anim.loop = value;
        }
    }

    @HostAccess.Export
    public double getNormalizedTime() {
        return PlayAnimationBridge.normalizedTime(entity);
    }

    @HostAccess.Export
    public void play() {
        PlayAnimationBridge.play(entity);
    }

    @HostAccess.Export
    public void playState(String stateName) {
        PlayAnimationBridge.playState(entity, stateName);
    }

    @HostAccess.Export
    public void stop() {
        PlayAnimationBridge.stop(entity);
    }

    private Animation2DComponent component() {
        return world.getComponent(entity, Animation2DComponent.class);
    }
}
