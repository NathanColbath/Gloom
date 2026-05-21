package org.llw.studio.scripting.js.bindings;

import org.graalvm.polyglot.HostAccess;
import org.llw.studio.ecs.EntityId;
import org.llw.studio.ecs.World;
import org.llw.studio.ecs.components.ParticleEmitterComponent;
import org.llw.studio.scripting.js.PlayParticleBridge;

/**
 * Play-mode API for {@link ParticleEmitterComponent}.
 */
public final class ParticleEmitterBinding {
    private final World world;
    private final EntityId entity;

    public ParticleEmitterBinding(ScriptContext context, EntityId entity) {
        this.world = context.world();
        this.entity = entity;
    }

    @HostAccess.Export
    public void play() {
        PlayParticleBridge.play(entity);
        ParticleEmitterComponent component = component();
        if (component != null) {
            component.emitting = true;
        }
    }

    @HostAccess.Export
    public void stop() {
        PlayParticleBridge.stop(entity);
        ParticleEmitterComponent component = component();
        if (component != null) {
            component.emitting = false;
        }
    }

    @HostAccess.Export
    public boolean isPlaying() {
        ParticleEmitterComponent component = component();
        return component != null && component.emitting;
    }

    @HostAccess.Export
    public void burst(int count) {
        PlayParticleBridge.burst(entity, count);
    }

    @HostAccess.Export
    public String getParticleSystemGuid() {
        ParticleEmitterComponent component = component();
        return component == null ? "" : component.particleSystemGuid;
    }

    @HostAccess.Export
    public void setParticleSystemGuid(String guid) {
        ParticleEmitterComponent component = component();
        if (component != null) {
            component.particleSystemGuid = guid == null ? "" : guid;
        }
    }

    private ParticleEmitterComponent component() {
        return world.getComponent(entity, ParticleEmitterComponent.class);
    }
}
