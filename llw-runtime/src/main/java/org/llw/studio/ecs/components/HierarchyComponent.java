package org.llw.studio.ecs.components;

import org.llw.studio.ecs.EntityId;

import java.util.ArrayList;
import java.util.List;

/**
 * Parent/child links between entities in a scene tree.
 * <p>
 * Maintained by {@link org.llw.studio.scene.GameObject} in editor and play mode.
 * Parent references store {@link EntityId} index/generation pairs; {@link #parentIndex}
 * {@code -1} means no parent.
 */
public final class HierarchyComponent implements Cloneable {
    /** Parent entity index, or {@code -1} for root-level objects. */
    public int parentIndex = -1;
    /** Parent entity generation matching {@link #parentIndex}. */
    public int parentGeneration;
    /** Ordered list of child entity references. */
    public final List<ChildRef> children = new ArrayList<>();

    /**
     * Stable child reference that survives entity index reuse via generation.
     */
    public static final class ChildRef {
        /** Child entity index. */
        public int index;
        /** Child entity generation. */
        public int generation;

        /** Creates an empty child reference. */
        public ChildRef() {
        }

        /**
         * @param id live child entity id to capture
         */
        public ChildRef(EntityId id) {
            index = id.index();
            generation = id.generation();
        }

        /**
         * @return {@link EntityId} reconstructed from stored index and generation
         */
        public EntityId toEntityId() {
            return new EntityId(index, generation);
        }
    }

    /**
     * @return deep copy including child references
     */
    public HierarchyComponent copy() {
        HierarchyComponent copy = new HierarchyComponent();
        copy.parentIndex = parentIndex;
        copy.parentGeneration = parentGeneration;
        for (ChildRef child : children) {
            ChildRef childCopy = new ChildRef();
            childCopy.index = child.index;
            childCopy.generation = child.generation;
            copy.children.add(childCopy);
        }
        return copy;
    }

    @Override
    public HierarchyComponent clone() {
        return copy();
    }
}
