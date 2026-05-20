package org.llw.studio.systems;

import org.llw.studio.ecs.EcsSystem;
import org.llw.studio.ecs.World;
import org.llw.studio.scripting.ScriptComponent;
import org.llw.studio.scripting.ScriptAssembly;
import org.llw.studio.scripting.ScriptBehaviour;
import org.llw.studio.scripting.ScriptContext;

import java.util.HashMap;
import java.util.Map;

/**
 * Legacy Java {@link ScriptBehaviour} driver: instantiates behaviours once, calls
 * {@link ScriptBehaviour#onStart()} on the first update, then {@link ScriptBehaviour#onUpdate(float)}
 * every frame thereafter.
 */
public final class ScriptSystem implements EcsSystem {
    private final ScriptAssembly assembly;
    private final Map<org.llw.studio.ecs.EntityId, ScriptBehaviour> instances = new HashMap<>();
    private boolean started;

    /**
     * @param assembly compiled script types available for instantiation
     */
    public ScriptSystem(ScriptAssembly assembly) {
        this.assembly = assembly;
    }

    /**
     * @param world      scene ECS world
     * @param deltaTime  elapsed seconds since the previous logic tick
     */
    @Override
    public void onUpdate(World world, float deltaTime) {
        var scripts = world.store(ScriptComponent.class);
        if (!started) {
            for (int i = 0; i < scripts.size(); i++) {
                var entity = scripts.entityAt(i);
                ScriptComponent component = scripts.componentAt(i);
                for (var attachment : component.attachments) {
                    if (!attachment.enabled || attachment.scriptClassName == null || attachment.scriptClassName.isBlank()) {
                        continue;
                    }
                    ScriptBehaviour behaviour = assembly.create(attachment.scriptClassName);
                    if (behaviour == null) {
                        Class<? extends ScriptBehaviour> type = assembly.findClass(attachment.scriptClassName);
                        if (type != null) {
                            try {
                                behaviour = type.getDeclaredConstructor().newInstance();
                            } catch (ReflectiveOperationException ignored) {
                            }
                        }
                    }
                    if (behaviour != null) {
                        behaviour.bind(new ScriptContext(world, entity));
                        behaviour.onStart();
                        instances.put(entity, behaviour);
                        break;
                    }
                }
            }
            started = true;
        }
        for (ScriptBehaviour behaviour : instances.values()) {
            behaviour.onUpdate(deltaTime);
        }
    }
}
