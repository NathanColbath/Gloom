package org.llw.studio.ecs;

/**
 * Ordered phases for {@link EcsSystem} execution within a {@link SystemScheduler}.
 * <p>
 * Systems in {@link #INPUT} run before {@link #LOGIC}, which runs before {@link #RENDER},
 * in both editor and play mode tick loops.
 */
public enum SystemGroup {
    /** Input polling and intent gathering. */
    INPUT,
    /** Gameplay, animation, and simulation updates. */
    LOGIC,
    /** Drawing and presentation. */
    RENDER
}
