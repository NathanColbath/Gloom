package org.llw.studio.ecs;

import java.util.Objects;

/**
 * Opaque entity handle combining a recycled index and generation stamp.
 * <p>
 * Used throughout editor and play mode; compare with {@link #equals(Object)} rather than
 * reference identity. {@link #none()} represents absence of an entity.
 */
public final class EntityId {
    private final int index;
    private final int generation;

    /**
     * @param index       dense entity index
     * @param generation  version counter for that index
     */
    public EntityId(int index, int generation) {
        this.index = index;
        this.generation = generation;
    }

    /**
     * @return dense entity index, or negative for {@link #none()}
     */
    public int index() {
        return index;
    }

    /**
     * @return generation stamp paired with {@link #index()}
     */
    public int generation() {
        return generation;
    }

    /**
     * @return sentinel id that does not refer to a live entity
     */
    public static EntityId none() {
        return new EntityId(-1, 0);
    }

    /**
     * @return {@code true} if this id is {@link #none()}
     */
    public boolean isNone() {
        return index < 0;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof EntityId other && index == other.index && generation == other.generation;
    }

    @Override
    public int hashCode() {
        return Objects.hash(index, generation);
    }

    @Override
    public String toString() {
        return "Entity{" + index + ":" + generation + "}";
    }
}
