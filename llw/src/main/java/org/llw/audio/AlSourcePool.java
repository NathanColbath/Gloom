package org.llw.audio;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

import org.lwjgl.openal.AL10;

/**
 * Recycles OpenAL source objects up to a fixed maximum.
 */
final class AlSourcePool {
    private static final int MAX_SOURCES = 32;

    private final Queue<Integer> freeSources = new ArrayDeque<>();
    private final Set<Integer> activeSources = new HashSet<>();

    /**
     * Pre-allocates a pool of OpenAL sources.
     */
    AlSourcePool() {
        for (int i = 0; i < MAX_SOURCES; i++) {
            freeSources.add(AL10.alGenSources());
        }
    }

    /**
     * Borrows a source for playback.
     *
     * @return OpenAL source name, or {@code 0} if the pool is exhausted
     */
    int acquire() {
        Integer source = freeSources.poll();
        if (source == null) {
            return 0;
        }
        activeSources.add(source);
        return source;
    }

    /**
     * Returns a source to the pool after stopping playback.
     *
     * @param source OpenAL source name
     */
    void release(int source) {
        if (source == 0 || !activeSources.remove(source)) {
            return;
        }
        AL10.alSourceStop(source);
        AL10.alSourcei(source, AL10.AL_BUFFER, 0);
        freeSources.add(source);
    }

    /**
     * Stops and releases every borrowed source.
     */
    void releaseAll() {
        for (int source : new HashSet<>(activeSources)) {
            release(source);
        }
    }

    /**
     * Deletes all OpenAL sources managed by this pool.
     */
    void dispose() {
        releaseAll();
        while (!freeSources.isEmpty()) {
            AL10.alDeleteSources(freeSources.poll());
        }
    }
}
