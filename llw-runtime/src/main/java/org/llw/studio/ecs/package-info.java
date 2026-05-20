/**
 * Entity-component-system runtime: entities, component stores, deferred commands, and system scheduling.
 *
 * <p>{@link org.llw.studio.ecs.World} is the hub used by both the editor and play-mode clones.
 * World space uses a <strong>Y-down</strong> convention (increasing {@code y} moves downward),
 * consistent with LLW renderables.
 *
 * @see org.llw.studio.ecs.components
 * @see org.llw.studio.scene
 * @see org.llw.studio.systems
 */
package org.llw.studio.ecs;
