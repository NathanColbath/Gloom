package org.llw.studio.ecs;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Index-generation entity allocator used internally by {@link World}.
 * <p>
 * Recycles entity indices after {@link #destroy(EntityId)} by bumping generations so stale
 * {@link EntityId} handles are detectable via {@link #isAlive(EntityId)}.
 */
final class EntityRegistry {
    private final Deque<Integer> freeList = new ArrayDeque<>();
    private int[] generations = new int[16];
    private int nextIndex;

    EntityId create() {
        int index;
        if (freeList.isEmpty()) {
            index = nextIndex++;
            ensureCapacity(index + 1);
        } else {
            index = freeList.removeFirst();
        }
        int generation = generations[index];
        return new EntityId(index, generation);
    }

    void destroy(EntityId id) {
        if (id.isNone() || !isAlive(id)) {
            return;
        }
        generations[id.index()]++;
        freeList.addLast(id.index());
    }

    boolean isAlive(EntityId id) {
        if (id.isNone() || id.index() >= nextIndex) {
            return false;
        }
        return generations[id.index()] == id.generation();
    }

    void clear() {
        freeList.clear();
        nextIndex = 0;
        generations = new int[16];
    }

    private void ensureCapacity(int size) {
        if (size <= generations.length) {
            return;
        }
        int[] next = new int[Math.max(size, generations.length * 2)];
        System.arraycopy(generations, 0, next, 0, generations.length);
        generations = next;
    }
}
