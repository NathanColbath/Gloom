package org.llw.resources;

/**
 * Reference-counted handle to a loaded asset. Call {@link #close()} or {@link #release()} when done.
 *
 * @param <T> loaded asset type
 */
public final class AssetRef<T> implements AutoCloseable {
    private final ResourceManager manager;
    private final String id;
    private final T value;
    private boolean released;

    AssetRef(ResourceManager manager, String id, T value) {
        this.manager = manager;
        this.id = id;
        this.value = value;
    }

    /**
     * Returns the loaded asset instance.
     *
     * @return live object
     * @throws IllegalStateException if already released
     */
    public T get() {
        if (released) {
            throw new IllegalStateException("AssetRef already released: " + id);
        }
        return value;
    }

    /** Returns the logical asset id. */
    public String id() {
        return id;
    }

    /**
     * Decrements the manager ref count for this asset.
     *
     * <p>Idempotent — subsequent calls are ignored.
     */
    public void release() {
        if (!released) {
            released = true;
            manager.releaseRef(id);
        }
    }

    @Override
    public void close() {
        release();
    }
}
