package org.llw.studio.ecs;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Groups and runs {@link EcsSystem} instances in {@link SystemGroup} order each frame.
 * <p>
 * Attached to a {@link World} and driven from editor or play mode update loops.
 */
public final class SystemScheduler {
    private final Map<SystemGroup, List<EcsSystem>> systems = new EnumMap<>(SystemGroup.class);

    /**
     * Creates an empty scheduler with an empty list for each {@link SystemGroup}.
     */
    public SystemScheduler() {
        for (SystemGroup group : SystemGroup.values()) {
            systems.put(group, new ArrayList<>());
        }
    }

    /**
     * Appends a system to the end of the given group's execution list.
     *
     * @param group   phase in which the system runs
     * @param system  system invoked on each {@link #update(World, float)}
     */
    public void add(SystemGroup group, EcsSystem system) {
        systems.get(group).add(system);
    }

    /**
     * Invokes every registered system in group order.
     *
     * @param world      world passed to each system
     * @param deltaTime  elapsed seconds since the previous update
     */
    public void update(World world, float deltaTime) {
        for (SystemGroup group : SystemGroup.values()) {
            for (EcsSystem system : systems.get(group)) {
                system.onUpdate(world, deltaTime);
            }
        }
    }

    /**
     * Removes all systems from every group without destroying the group lists.
     */
    public void clear() {
        for (List<EcsSystem> list : systems.values()) {
            list.clear();
        }
    }
}
