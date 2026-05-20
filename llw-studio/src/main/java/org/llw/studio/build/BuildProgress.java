package org.llw.studio.build;

/**
 * Reports fractional progress during a long-running build.
 */
@FunctionalInterface
public interface BuildProgress {
    /**
     * @param fraction value in {@code [0, 1]}
     * @param message  human-readable step description
     */
    void report(float fraction, String message);
}
